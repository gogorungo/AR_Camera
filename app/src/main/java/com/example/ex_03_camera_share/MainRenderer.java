package com.example.ex_03_camera_share;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.google.ar.core.Frame;
import com.google.ar.core.Session;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainRenderer implements GLSurfaceView.Renderer {

    RenderCallBack myCallBack;

    CameraPreView mCamera;

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

        this.myCallBack = myCallBack;
    }


    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        Log.d("MainRenderer : ","onSurfaceCreated() 실행");

                             // R        G           B       A(투명도) --> 노랑색 (1이 최대)
        GLES20.glClearColor(1.0f,1.0f,0.0f,1.0f);

        mCamera.init();

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

        GLES20.glDepthMask(false);
        mCamera.draw();
        GLES20.glDepthMask(true);
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

}
