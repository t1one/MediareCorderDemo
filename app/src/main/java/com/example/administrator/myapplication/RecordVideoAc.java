package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;


import java.io.File;
import java.io.IOException;
import java.util.List;

public class RecordVideoAc extends Activity implements View.OnClickListener, SurfaceHolder.Callback {
    private static final int START_TIME = 1;
    private static final int OVER_TIME = 2;
    private int time = 0;
    private Handler handler = new Handler() {
        @Override
        public void dispatchMessage(Message msg) {
            switch (msg.what) {
                case OVER_TIME:
                    tv_video_ing.setVisibility(View.GONE);
                    surfaceview.setEnabled(false);
                    btn_video_up.setEnabled(true);
                    btn_video_play.setEnabled(true);
                    destriyMediarecorder();
                    if (camera != null) {
                        closeLamp();
                        camera.stopPreview();
                        destriyCamera();
                    }


                    break;
                case START_TIME:
                    tv_video_time.setText("00:0" + time);
                    if (time >= 5) {
                        handler.sendEmptyMessage(OVER_TIME);
                        break;
                    }
                    time++;
                    handler.sendEmptyMessageDelayed(START_TIME, 1000);
                    break;
            }
            super.dispatchMessage(msg);

        }
    };

    private Button btn_video_start;// 开始录制按钮
    private Button btn_video_up;// 停止录制按钮
    private MediaRecorder mediarecorder;// 录制视频的类
    private SurfaceView surfaceview;// 显示视频的控件

    private SurfaceHolder surfaceHolder;
    private File fileName;
    private Button btn_lamp_open;

    private Camera camera;
    private Button btn_lamp_close;
    boolean isInit;
    private MediaPlayer mPlayer;
    private TextView tv_video_time;
    private Button btn_video_play;
    private String filePath;
    private Button bt_ac_close;
    private static int VIDEO_DATA = 100;
    private Camera.AutoFocusCallback callback;
    private TextView tv_video_ing;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        // 选择支持半透明模式,在有surfaceview的activity中使用。
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        fileName = new File(Environment.getExternalStorageDirectory()
                .getAbsolutePath() + "/11111111");
        if (fileName.mkdir()) {
            Log.e("filename", "添加目录成功");
        }
        callback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    ToastUtil.showToast(getApplication(), "对焦成功");
                }
            }
        };
        setContentView(R.layout.recorded_video_ac);
        init();
    }

    private void init() {
        btn_video_start = (Button) this.findViewById(R.id.btn_video_start);
        btn_video_up = (Button) this.findViewById(R.id.btn_video_up);
        tv_video_time = (TextView) this.findViewById(R.id.tv_video_time);
        btn_lamp_open = (Button) this.findViewById(R.id.btn_lamp_open);
        btn_lamp_close = (Button) this.findViewById(R.id.btn_lamp_close);
        btn_video_play = (Button) this.findViewById(R.id.btn_video_play);
        bt_ac_close = (Button) this.findViewById(R.id.bt_ac_close);
        tv_video_ing = (TextView) this.findViewById(R.id.tv_video_ing);
        btn_video_start.setOnClickListener(this);
        btn_video_up.setOnClickListener(this);
        btn_lamp_open.setOnClickListener(this);
        btn_lamp_close.setOnClickListener(this);
        btn_video_play.setOnClickListener(this);
        bt_ac_close.setOnClickListener(this);
        surfaceview = (SurfaceView) this.findViewById(R.id.videoView);
        surfaceview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.autoFocus(callback);
            }
        });
        SurfaceHolder holder = surfaceview.getHolder();// 取得holder
        holder.addCallback(this); // holder加入回调接口
        // setType必须设置，要不出错.
       holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_video_start:
                tv_video_ing.setVisibility(View.VISIBLE);
                btn_video_start.setEnabled(false);
                // 关闭预览并释放资源
//                if (camera != null) {
////                        camera.stopPreview();
//                    camera.release();
//                    camera = null;
//                }
                time = 0;
                mediarecorder = new MediaRecorder();// 创建mediarecorder对象
                if (camera == null) {
                    camera = Camera.open();
                    ToastUtil.showToast(this, "开始失败");
                }

                // 设置摄像头预览顺时针旋转90度，才能使预览图像显示为正确的，而不是逆时针旋转90度的。
                camera.setDisplayOrientation(90);
                camera.unlock();
                mediarecorder.setCamera(camera);
                mediarecorder.setOrientationHint(90);

                // 设置录制视频源为Camera(相机)
                mediarecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                mediarecorder.setPreviewDisplay(surfaceHolder
                        .getSurface());
//                设置录制完成后视频的封装格式THREE_GPP为3gp.MPEG_4为mp4
                mediarecorder
                        .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                // 设置录制的视频编码h263 h264
                mediarecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//                设置视频录制的分辨率。必须放在设置编码和格式的后面，否则报错
//                mediarecorder.setVideoSize(176, 144);
//                mediarecorder.setVideoSize(320, 240);
//                mediarecorder.setVideoSize(720, 480);
//                mediarecorder.setVideoSize(800, 480);//这个分辨率三星note 4是不支持的
//                mediarecorder.setVideoSize(1280, 720);
                //动态搞定分辨率
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                mediarecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
                Log.e("videoFrameWidth", profile.videoFrameWidth + "" + profile.videoFrameHeight);
//                设置录制的视频帧率。必须放在设置编码和格式的后面，否则报错
                mediarecorder.setVideoFrameRate(30);
//                mediarecorder.setMaxDuration(1000000);
                mediarecorder.setVideoEncodingBitRate(5 * 1024 * 1024);
//                mediarecorder.setProfile(CamcorderProfile
//                     .get(CamcorderProfile.QUALITY_CIF));
                // 设置视频文件输出的路径
                filePath = fileName.getPath() + "/" + System.currentTimeMillis() + ".mp4";
                mediarecorder.setOutputFile(filePath);

                Log.e("fileName", fileName.getPath());
                try {

                    // 准备录制
                    mediarecorder.prepare();

                    // 开始录制
                    mediarecorder.start();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                handler.sendEmptyMessageDelayed(START_TIME, 1000);


                break;
            case R.id.btn_video_up:
                dataBack();
                break;
            case R.id.btn_lamp_open:
                if (camera == null) {
                    camera = Camera.open();
                    try {
                        camera.setPreviewDisplay(surfaceHolder);
                        camera.setDisplayOrientation(90);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ToastUtil.showToast(this, "打开闪光灯失败");
                }
                openLamp();
                break;
            case R.id.btn_lamp_close:
                if (camera == null) {
                    ToastUtil.showToast(this, "关闭闪光灯失败");
                    return;
                }
                closeLamp();
                break;
            case R.id.btn_video_play:
                if (TextUtils.isEmpty(filePath)) {
                    ToastUtil.showToast(this, "没有找到播放源");
                    return;
                }
                tv_video_time.setText("正在播放视频");
                play(filePath);
                break;
            case R.id.bt_ac_close:
                finish();
                break;
        }

    }

    private void dataBack() {
        destriyMediarecorder();
        destriyCamera();

        Intent intent = new Intent();
        intent.putExtra("uploadVideoPath", filePath);
        setResult(VIDEO_DATA, intent);
        finish();
    }

    private void openLamp() {
        camera.startPreview();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
        ToastUtil.showToast(this, "打开闪光灯");
    }

    private void closeLamp() {
        Camera.Parameters parameters1 = camera.getParameters();
        parameters1.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters1);
        ToastUtil.showToast(this, "关闭闪光灯");
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        surfaceHolder = holder;
//        initCamera(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // 将holder，这个holder为开始在oncreat里面取得的holder，将它赋给surfaceHolder
        surfaceHolder = holder;
        isInit = false;
        camera = Camera.open();
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        camera.setDisplayOrientation(90);
//        camera.unlock();
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            destriyCamera();
        }
        surfaceview = null;
        if (surfaceHolder != null) {
            surfaceHolder = null;
        }
        if (mediarecorder != null) {
            destriyMediarecorder();
        }
    }

    private void initCamera(int width, int height) {
        if (!isInit) {
            // viewWidth = width;
            // viewHeight = height;
            isInit = true;
            Log.e("wwwwww", width + "====" + height);
            Camera.Parameters parameters = camera.getParameters();
            // 摄像头旋转
            // if (android.os.Build.VERSION_CODES.GINGERBREAD >
            // android.os.Build.VERSION.SDK_INT) {
            // } else {
            // parameters.set("rotation", 180);
            // }
            List<Camera.Size> previewSizes = camera.getParameters().getSupportedPreviewSizes();
            double aspectTolerance = Double.MAX_VALUE;
            int preWidth = 0, preHeight = 0;
            double scale = ((double) width) / height;
            Log.e("wwwwww", scale + "");
            for (int i = 0, len = previewSizes.size(); i < len; i++) {
                Camera.Size s = previewSizes.get(i);
                double sizeScale = ((double) s.height) / s.width;
                if (Math.abs(scale - sizeScale) < aspectTolerance) {
                    aspectTolerance = Math.abs(scale - sizeScale);
                    preWidth = s.height;
                    preHeight = s.width;
                    Log.e("wwwwww", s.width + " " + s.height);
                }
            }
            if (preWidth != 0) {
                parameters.setPreviewSize(preWidth, preHeight);
                // mSurface.setLayoutParams(new LayoutParams(720, 1280));
                Camera.Size s = parameters.getPreviewSize();
                Log.e("wwwwww", s.width + " " + s.height);
            }
        }
    }


    public void play(String fileName) {
        if (camera != null) {

            closeLamp();
            destriyCamera();
        }
        destriyMediaPlayer();
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDisplay(surfaceHolder); // 定义一个SurfaceView播放它
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                destriyMediaPlayer();
            }
        });
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mPlayer.start();
    }


    private void destriyMediarecorder() {
        if (mediarecorder != null) {
            // 停止录制
            mediarecorder.stop();
            // 释放资源
            mediarecorder.release();
            mediarecorder = null;
        }
    }

    private void destriyMediaPlayer() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void destriyCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }

    }

    @Override
    protected void onDestroy() {
        destriyMediarecorder();
        destriyCamera();
        destriyMediaPlayer();
        super.onDestroy();
    }

//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        switch (keyCode) {
//            case KeyEvent.KEYCODE_BACK:
//                return true;
//
//        }
//        return super.onKeyDown(keyCode, event);
//    }

}