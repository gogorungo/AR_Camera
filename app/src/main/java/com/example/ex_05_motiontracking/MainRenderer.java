package com.example.ex_05_motiontracking;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    RenderCallBack myCallBack;

    CameraPreView mCamera;

    PointCloudRenderer mPointCloud;

    Sphere sphere;

    //화면이 변환되었다면 true,
    boolean viewprotChanged;

    int width, height;

    interface RenderCallBack {
        void preRender(); // MainActivity 에서 재정의하여 호출토록 함
    }


    // 생성시 renderCallBack을 매개변수로 대입받아 자신의 멤버로 넣는다
    // MainActivity 에서 생성하므로 MainActivity의 것을 받아서 처리가능토록 한다.
    MainRenderer(RenderCallBack myCallBack){

        mCamera = new CameraPreView();

        mPointCloud = new PointCloudRenderer();

        sphere = new Sphere();

        this.myCallBack = myCallBack;
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d("MainRenderer : ","onSurfaceCreated() 실행");

                             // R        G           B       A(투명도) --> 노랑색 (1이 최대)
        GLES20.glClearColor(1.0f,1.0f,0.0f,1.0f);

        mCamera.init();
        mPointCloud.init();
        sphere.init();


    }

    // 화면 크기
    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Log.d("MainRenderer : ","onSurfaceChanged() 실행");

        // 시작위치 x, y, 넓이, 높이
        GLES20.glViewport(0,0,width,height);

        // 화면 바뀌었다고 알려줌
        viewprotChanged = true;
        this.width = width;
        this.height = height;

    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        Log.d("MainRenderer : ","onDrawFrame() 실행");

        // 색상 버퍼 삭제 | 깊이 버퍼 삭제
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // 카메라로부터 새로 받은 영상으로 화면을 업데이트 할 것임
        myCallBack.preRender();

        // 카메라로 받은 화면 그리기
        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);


        // 포인트클라우드 그리기
        mPointCloud.draw();

        // 점 그리기
        sphere.draw();


        if(mLineX != null){
            if(!mLineX.isInited) {
                mLineX.init();
            }
            mLineX.draw();
        }
        if(mLineY != null){
            if(!mLineY.isInited) {
                mLineY.init();
            }
            mLineY.draw();
        }
        if(mLineZ != null){
            if(!mLineZ.isInited) {
                mLineZ.init();
            }
            mLineZ.draw();
        }

    }

    // 화면 변환이 되었다는 것을 지시할 메소드 ==> MainActivity 에서 실행할 것이다


    void onDisplayChanged(){
        viewprotChanged = true;
    }
    
    //ARCore Session, 화면 돌아가는 것
    void updateSession(Session session, int rotation){
        if(viewprotChanged){
            
            // 디스플레이 화면 방향 설정
            session.setDisplayGeometry(rotation, width, height);
            viewprotChanged = false;
            Log.d("MainRenderer : ","updateSession 실행");
        }
    }

    // 카메라를 넘겨준다. 카메라가 없으면 에러난다. null 체크를 해줘야한다.
    int getTextureId(){
      return mCamera==null ? -1 : mCamera.mTextures[0];
    }


    void transformDisplayGeometry(Frame frame){
        mCamera.transformDisplayGeometry(frame);
    }


    void addPoint(float x, float y, float z){

        float [] matrix = new float[16];

        //매트릭스 값 초기화
        Matrix.setIdentityM(matrix, 0);
        // 매트릭스의 값을 x,y,z 로 바꾼다
        Matrix.translateM(matrix,0,x,y,z);

        // 원본, 시작위치, 카피할곳, 시작위치, 총개수
//        System.arraycopy(matrix,0,sphere.mModelMatrix,0,16);
        // 카피를 Sphere에서 한다면 값을 넘겨줘야한다


        sphere.addNOCnt();

        sphere.setmModelMatrix(matrix);
    }


    Line mLineX, mLineY, mLineZ;
//    float [] projMatrix = new float[16];



    void addLineX(float[] pps,  float x, float y, float z){

        mLineX = new Line(pps,x,y,z, Color.RED);
//        mLineX.setmModelMatrix(matrix);

        float [] matrix = new float[16];

        //매트릭스 값 초기화
        Matrix.setIdentityM(matrix, 0);
        // 매트릭스의 값을 x,y,z 로 바꾼다
        Matrix.translateM(matrix,0,x,y,z);
        mLineX.setmModelMatrix(matrix);
    }

    void addLineY(float[] pps, float x, float y, float z){

        mLineY = new Line(pps,x,y,z, Color.GREEN);
//        mLineX.setmModelMatrix(matrix);

        float [] matrix = new float[16];

        //매트릭스 값 초기화
        Matrix.setIdentityM(matrix, 0);
        // 매트릭스의 값을 x,y,z 로 바꾼다
        Matrix.translateM(matrix,0,x,y,z);
        mLineY.setmModelMatrix(matrix);
    }

    void addLineZ(float[] pps,  float x, float y, float z){

        mLineZ = new Line(pps,x,y,z, Color.BLUE);
//        mLineX.setmModelMatrix(matrix);

        float [] matrix = new float[16];

        //매트릭스 값 초기화
        Matrix.setIdentityM(matrix, 0);
        // 매트릭스의 값을 x,y,z 로 바꾼다
        Matrix.translateM(matrix,0,x,y,z);
        mLineZ.setmModelMatrix(matrix);
    }

    void updateProjMatrix(float [] projMatrix){

//        System.arraycopy(projMatrix,0,this.projMatrix,0,16);
        mPointCloud.updateProjMatrix(projMatrix);
        sphere.updateProjMatrix(projMatrix);
        if(mLineX != null) {
            mLineX.updateProjMatrix(projMatrix);
        }
        if(mLineY != null) {
            mLineY.updateProjMatrix(projMatrix);
        }
        if(mLineZ != null) {
            mLineZ.updateProjMatrix(projMatrix);
        }
    }

    void updateViewMatrix(float [] viewMatrix){
        mPointCloud.updateViewMatrix(viewMatrix);
        sphere.updateViewMatrix(viewMatrix);

        if(mLineX != null) {
            mLineX.updateViewMatrix(viewMatrix);
        }
        if(mLineY != null) {
            mLineY.updateViewMatrix(viewMatrix);
        }
        if(mLineZ != null) {
            mLineZ.updateViewMatrix(viewMatrix);
        }
    }

}
