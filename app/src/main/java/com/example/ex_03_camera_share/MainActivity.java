package com.example.ex_03_camera_share;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

public class MainActivity extends AppCompatActivity {

    Session mSession;

    GLSurfaceView mySerView;

    MainRenderer mRenderer;

    Config mConfig; // ARCore session 설정 정보를 받을 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mySerView = (GLSurfaceView) findViewById(R.id.glsurfaceview);

        // MainActivity 의 화면 관리 매니져 --> 화면 변화를 감지 : 현재 시스템에서 서비스 지원 (AppCompatActivity)
        DisplayManager displayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);

        // 화면 변화가 발생되면 MainRenderer의 화면 변환을 실행시킨다.
        if(displayManager != null){
            //화면에 대한 리스너 실행
            displayManager.registerDisplayListener(

                    //익명 클래스로 정의
                    new DisplayManager.DisplayListener() {


                        @Override
                        public void onDisplayAdded(int i) {

                        }

                        @Override
                        public void onDisplayRemoved(int i) {

                        }

                        // 화면이 변경되었다면
                        @Override
                        public void onDisplayChanged(int i) {
                            synchronized (this) {
                                // 화면 갱신 인지 메소드 실행
                                mRenderer.onDisplayChanged();
                            }
                        }
                    } , null);
        }


        MainRenderer.RenderCallBack mr = new MainRenderer.RenderCallBack() {
            // 익명 이너클래스. 생성시에 한번만 사용(정의)가능. 단발적

            // 렌더링 작업
            @Override
            public void preRender() {

                //화면이 회전 되었다면 (필수)
                if(mRenderer.viewprotChanged){
                    // 현재 화면 가져오기
                    Display display = getWindowManager().getDefaultDisplay();

                    mRenderer.updateSession(mSession, display.getRotation());
                }

                // session 객체와 연결해서 화면 그리기 하기
                mSession.setCameraTextureName(mRenderer.getTextureId());

                // 화면 그리기에서 사용할 frame --> session 이 업데이트 되면 새로운 프레임을 받는다
                Frame frame = null;

                try {
                    frame = mSession.update();
                } catch (CameraNotAvailableException e) {
                    e.printStackTrace();
                }

                // 화면을 바꾸기 위한 작업

                mRenderer.transformDisplayGeometry(frame);
                // 위랑 똑같은 기능
//                mRenderer.mCamera.transformDisplayGeometry(frame);

            }
        };

        mRenderer = new MainRenderer(mr);

        // pause 시 관련 데이터가 사라지는 것을 막는다
        mySerView.setPreserveEGLContextOnPause(true);

        // 오픈 EGL 사용시 버전을 2.0 사용
        mySerView.setEGLContextClientVersion(2);

        // 화면을 그리는 Renderer 를 지정한다.
        // 새로 정의한 MainRenderer를 사용한다.
        mySerView.setRenderer(mRenderer);

        // 렌더링 계속 호출
        mySerView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    @Override
    protected void onPause() {
        super.onPause();

        mySerView.onPause();
        mSession.pause();
    }

    @Override
    protected void onResume() {
        // 화면을 띄울때마다 실행
        super.onResume();
        cameraPerm();

        try {
            if(mSession == null) {

//                Log.d("session requestInstall ? ",
//                        ArCoreApk.getInstance().requestInstall(this,true)+"");

                // ARcore 가 정상적으로 설치 돼 있는가
                switch (ArCoreApk.getInstance().requestInstall(this,true)){

                    case INSTALLED: // ARcore 정상설치됨
                        //ARcore가 정상설치되어서 session 을 생성 가능한 형태임
                        mSession = new Session(this);
                        Log.d("session 인감","session 생성됨");
                        break;

                    case INSTALL_REQUESTED: // ARcore 설치 필요

                        Log.d("session 인감","ARcore INSTALL_REQUESTED");
                        break;

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 화면 갱신시 세션설정 정보를 받아서 내세션의 설정으로 올린다.
        mConfig = new Config(mSession);

        mSession.configure(mConfig);

        try {
            mSession.resume();
        } catch (CameraNotAvailableException e) {
            e.printStackTrace();
        }

        mySerView.onResume();

    }

    //카메라 퍼미션 요청
    void cameraPerm(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    0
            );
        }
    }
}