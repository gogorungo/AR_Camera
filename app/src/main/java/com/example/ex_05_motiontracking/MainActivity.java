package com.example.ex_05_motiontracking;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.PointCloud;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.CameraNotAvailableException;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Session mSession;

    GLSurfaceView mySerView;

    MainRenderer mRenderer;

    Config mConfig; // ARCore session 설정 정보를 받을 변수

    TextView my_textView;

    SeekBar seekBar;

    String ttt = "";

    // 디스플레이 화면의 X, Y
    float displayX, displayY;

    // 터치 했는지 안했는지
    boolean mTouched = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 타이틀바 없애기
        hideStatusBar();
        setContentView(R.layout.activity_main);

        mySerView = (GLSurfaceView) findViewById(R.id.glsurfaceview);

        my_textView = (TextView) findViewById(R.id.my_textView);

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

        findViewById(R.id.blackBtn).setOnClickListener(onClickListener);
        findViewById(R.id.whiteBtn).setOnClickListener(onClickListener);
        findViewById(R.id.redBtn).setOnClickListener(onClickListener);
        findViewById(R.id.greenBtn).setOnClickListener(onClickListener);
        findViewById(R.id.blueBtn).setOnClickListener(onClickListener);


        seekBar = (SeekBar) findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    mRenderer.mLineX.lineWidth(i);
                    mRenderer.mLineY.lineWidth(i);
                    mRenderer.mLineZ.lineWidth(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


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



                // 여기서부터가 PointCloud 설정 구간

                // ARCore 에 정의된 클래스
                // 현재 프레임에서 특정있는 점들에 대한 포인트 값 (3차원 좌표값)을 받을 객체
                PointCloud pointCloud = frame.acquirePointCloud();

                // 포인트 값을 적용시키기 위해 mainRenderer -> PointCloud.update() 실행
                mRenderer.mPointCloud.update(pointCloud);

                // 사용이 끝난 포인트 자원해제 (반드시)
                pointCloud.release();

                // 화면 터치시 작업 시작
                if(mTouched){



//                    Log.d("preRender : ", "건드렸다 : " + displayX + "," + displayY);

                    List<HitResult> arr = frame.hitTest(displayX,displayY);



//                    Log.d("preRender : ", displayX + "," + displayY + ","+arr);

                    int i = 0;

                    ttt = "";
                    for(HitResult hr : arr){

                        // 축
                        Pose pose = hr.getHitPose();

                        float [] xx = pose.getXAxis();
                        float [] yy = pose.getYAxis();
                        float [] zz = pose.getZAxis();
                        
                        // 센터좌표. qx,qy,qz는 회전
                        mRenderer.addPoint(pose.tx(),pose.ty(),pose.tz());

                        // x축
                        mRenderer.addLineX(xx, pose.tx(),pose.ty(),pose.tz());
                        mRenderer.addLineY(yy, pose.tx(),pose.ty(),pose.tz());
                        mRenderer.addLineZ(zz, pose.tx(),pose.ty(),pose.tz());

//                        Log.d("arr " + i + " : ", hr.toString());

                        Log.d("arr " + i + " : ", pose.toString());

                        ttt += pose.toString() + "\n";

                        i++;
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            my_textView.setText(ttt);

                        }
                    });

                    // 터치 플래그를 초기화
                    mTouched = false;

                }
                
                // 화면 터치시 작업 끝
                
                // 카메라 frame 에서 받는다
                // mPointCloud 에서 렌더링 할때 카메라의 좌표계산을 받아서 처리
                Camera camera = frame.getCamera();

                float [] projMatrix = new float[16];
                float [] viewMatrix = new float[16];

                camera.getProjectionMatrix(projMatrix,0,0.1f,100.0f);
                camera.getViewMatrix(viewMatrix,0);

//                mRenderer.mPointCloud.updateMatrix(viewMatrix, projMatrix);

                mRenderer.updateProjMatrix(projMatrix);
                mRenderer.updateViewMatrix(viewMatrix);


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

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.blackBtn:
                    mRenderer.sphere.selectNum(0);
                    break;

                case R.id.whiteBtn:
                    mRenderer.sphere.selectNum(1);
                    break;
                case R.id.redBtn:
                    mRenderer.sphere.selectNum(2);
                    break;
                case R.id.greenBtn:
                    mRenderer.sphere.selectNum(3);
                    break;
                case R.id.blueBtn:
                    mRenderer.sphere.selectNum(4);
                    break;
            }
        }
    };



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

//        mySerView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d("MainActivity : ", "건드렸다 : " + event.getX() + ","+ event.getY());

        displayX = event.getX();
        displayY = event.getY();


        // 건드렸으면 true
        mTouched = true;

        return true;
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

    void hideStatusBar(){

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
    }
}