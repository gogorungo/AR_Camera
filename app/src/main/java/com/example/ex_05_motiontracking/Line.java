package com.example.ex_05_motiontracking;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Line {

    // 점. 고정되어있으므로 그대로 써야한다
    // GPU 를 이용하여 고속 계산하여 화면 처리하기 위한 코드
    String vertexShaderString =
            "attribute vec3 aPosition;" // 3개의 값
                    + "attribute vec4 aColor;" // 4개의 값
                    + "uniform mat4 uMVPMatrix;" //(4X4 형태의 상수로 지정)
                    + "varying vec4 vColor;" // 4개의 값

                    + "void main () {"

                    + "vColor = aColor;"
                    + "gl_Position = uMVPMatrix * vec4(aPosition.x, aPosition.y, aPosition.z, 1.0);"
                    // gl_Position : OpenGL에 있는 변수 이용 > 계산식 uMVPMatrix * vPosition

                    + "}";

    // 화면에 어떻게 그려지는지
    String fragmentShaderString =
            // 정밀도 중간
            "precision mediump float;"
                    + "varying vec4 vColor;" // 4 개 (점들) 컬러를 받겠다
                    + "void main() {"
                    + "gl_FragColor = vColor;"
                    +"}";


    float [] mModelMatrix = new float[16];
    float [] mViewMatrix = new float[16];
    float [] mProjMatrix = new float[16];




    // 색깔 (빨간색에 가까운 색)
//    float [] mColor = {0.2f, 0.5f, 0.8f, 1.0f };



    FloatBuffer mVertices;
    FloatBuffer mColors;
    ShortBuffer mIndices;
    int mProgram;

    boolean isInited = false;



    // 점 개수 상수화
//    final int POINT_COUNT = 20;



    // 버퍼로 만들어서 쪼개 보낸다
    public Line(float [] end , float x, float y, float z, int color){

        // x, y, z 에서 end[0] , end[1], end[2] 로 갈거야
        float [] vertices = {x, y, z, end[0],end[1],end[2]};

        // 시작과 끝이므로 두번
        float [] mColor = new float[]{
                Color.red(color)/255.f,
                Color.green(color)/255.f,
                Color.blue(color)/255.f,
                1.0f,
                Color.red(color)/255.f,
                Color.green(color)/255.f,
                Color.blue(color)/255.f,
                1.0f
        };

        short [] indices = {0,1};

        Log.d("선이야 : ",Color.red(color)+","+Color.green(color)+","+Color.blue(color));

        // buffer로 변환

        //점
        mVertices = ByteBuffer.allocateDirect(vertices.length * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVertices.put(vertices);
        mVertices.position(0);


        //색
        mColors = ByteBuffer.allocateDirect(mColor.length * 4).
                order(ByteOrder.nativeOrder()).asFloatBuffer();
        mColors.put(mColor);
        mColors.position(0);


        // 순서
        // short 는 * 2
        mIndices = ByteBuffer.allocateDirect(indices.length * 2).
                order(ByteOrder.nativeOrder()).asShortBuffer();
        mIndices.put(indices);
        mIndices.position(0);


    }



    // 초기화
    void init(){
        // shading 입체감
        // 점위치 계산식
        // 기존에 GPU로 연산하던 코드를 가져다가 사용
        // 점 쉐이더 생성
        int vShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vShader,vertexShaderString);

        // 컴파일
        GLES20.glCompileShader(vShader);

        // 텍스처
        int fShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fShader,fragmentShaderString);

        // 컴파일
        GLES20.glCompileShader(fShader);


        // mProgram = vertexShader + fragmentShader
        mProgram = GLES20.glCreateProgram();
        // 점위치 계산식 합치기
        GLES20.glAttachShader(mProgram,vShader);
        // 색상 계산식 합치기
        GLES20.glAttachShader(mProgram,fShader);

        GLES20.glLinkProgram(mProgram); // 도형 렌더링 계산식 정보를 넣는다.

        isInited = true;
    }


    int mPositionHandle, mColorHandle, mMVPMatrixHandle;


    float lineWidth = 5.0f;
    void lineWidth(int i){
        lineWidth = (float) i;
    }


    // 도형 그리기 --> MyGLRenderer.onDrawFrame() 에서 호출하여 그리기
    void draw(){

        //계산된 렌더링 정보 사용한다.
        GLES20.glUseProgram(mProgram);


        // 핸들러

        // 점, 색 계산방식
        int position = GLES20.glGetAttribLocation(mProgram, "aPosition");
        int color = GLES20.glGetAttribLocation(mProgram, "aColor");
        int mvp = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        float [] mvpMatrix = new float[16];
        float [] mvMatrix = new float[16];


        // 합친다
        Matrix.multiplyMM(mvMatrix, 0, mViewMatrix, 0 , mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mProjMatrix, 0 , mvMatrix, 0);

        // mvp 번호에 해당하는 변수에 mvpMatrix 대입
        GLES20.glUniformMatrix4fv(mvp, 1, false, mvpMatrix,0);


        // 점, 색 번호에 해당하는 변수에 각각 대입
        // position, 개수, 자료형, 정규화 할것이냐, 스타일 간격(자료형), 좌표
        // 점 float * 3점 (삼각형)
        GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT,false,4 * 3,mVertices);


        // 색 float * rgba
        GLES20.glVertexAttribPointer(color, 3, GLES20.GL_FLOAT,false,4 * 4,mColors);



        // GPU 활성화
        GLES20.glEnableVertexAttribArray(position);
        GLES20.glEnableVertexAttribArray(color);

        // 선 두께
//        GLES20.glLineWidth(5.0f);

        GLES20.glLineWidth(lineWidth);

        // 그린다
        //                       선으로 그린다,         순서의 보유량,       순서 자료형,      순서 내용
        GLES20.glDrawElements(GLES20.GL_LINES,mIndices.capacity(),GLES20.GL_UNSIGNED_SHORT, mIndices);

        // GPU 비활성화
        GLES20.glDisableVertexAttribArray(position);

    }

    // 캡슐화
    void setmModelMatrix(float [] matrix){
        System.arraycopy(matrix,0,mModelMatrix,0,16);
    }

    void updateProjMatrix(float [] projMatrix) {
        System.arraycopy(projMatrix, 0, this.mProjMatrix, 0, 16);
    }

    void updateViewMatrix(float [] viewMatrix) {
        System.arraycopy(viewMatrix, 0, this.mViewMatrix, 0, 16);
    }


}
