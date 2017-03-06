package com.example.administrator.myapplication;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.w3c.dom.Text;

public class CameraAc extends Activity implements Callback {
    /**
     * Called when the activity is first created.
     */
    private final static int VIDEO_ERROR_NET = 1;
    private final static int VIDEO_ERROR_WEB = 2;
    private final static int VIDEO_SUCCESS_UP = 0;
    private final static int VIDEO_SUCCESS_DOWN = 4;
    private final static int VIDEO_SHOW_BAR = 3;
    Handler handle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);

            pb = (ProgressBar) findViewById(R.id.progressBar1);
            switch (msg.what) {
                case VIDEO_SUCCESS_UP:
                    tv_up.setText(message);
                    tv_up.setText("上传成功。");
                    pb.setVisibility(ProgressBar.GONE);
                    bt_up.setVisibility(View.GONE);
                    down = true;
                    if (!TextUtils.isEmpty(message)) {
                        try {
                            chageName();
                        } catch (Exception e) {

                        }
                    }
                    break;
                case VIDEO_ERROR_NET:
                    tv_up.setText("无可用网络。");
                    tv_down.setText("无可用网络。");
                    pb.setVisibility(ProgressBar.GONE);
                    break;
                case VIDEO_ERROR_WEB:
                    tv_up.setText("找不到服务器地址");
                    tv_down.setText("找不到服务器地址");
                    pb.setVisibility(ProgressBar.GONE);
                    break;
                case VIDEO_SHOW_BAR:
                    if (down) {
                        tv_down.setVisibility(View.VISIBLE);
                        bt_down.setVisibility(View.GONE);
                    } else {
                        tv_up.setVisibility(View.VISIBLE);
                        bt_up.setVisibility(View.GONE);
                        bt_local_up.setVisibility(View.GONE);
                    }
                    pb.setVisibility(View.VISIBLE);
                    break;
                case VIDEO_SUCCESS_DOWN:
                    tv_down.setVisibility(View.VISIBLE);
                    tv_down.setText("下载成功。");
                    pb.setVisibility(ProgressBar.GONE);
                    bt_down.setVisibility(View.GONE);
                    bofang.setVisibility(View.VISIBLE);
                    mSurfaceView.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }


    };
    private Button bt_local_up;

    private void chageName() {
        File file = new File(uploadVideoPath);   //指定文件名及路径
        String filename = file.getParent();
        messageFile = new File(filename + "/" + message);
        file.renameTo(messageFile);
    }

    File messageFile;
    private ProgressBar pb;
    private TextView tv_up;
    private boolean down = false;
    private TextView tv_down;
    private String downFilePath = "http://10.30.26.63:8087/test1/servlet/Videoservlet2";

    private String uploadFilePath = "http://10.30.26.63:8087/test1/servlet/Videoservlet";
    private String uploadVideoPath = "";
    private Button bt_up;
    private Button bt_down;
    private String message;
    private Button bofang;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private MediaPlayer mPlayer;
    private String openVideoPath;
    private final static int VIDEO_CAMERA_DATA = 100;
    private final static int VIDEO_LOCAL_DATA = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv_up = (TextView) findViewById(R.id.tv_up);
        tv_down = (TextView) findViewById(R.id.tv_down);
        bt_up = (Button) findViewById(R.id.bt_up);
        bt_down = (Button) findViewById(R.id.bt_down);
        bofang = (Button) findViewById(R.id.bofang);
        bt_local_up = (Button) findViewById(R.id.bt_local_up);
        mSurfaceView = (SurfaceView) findViewById(R.id.videoView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setKeepScreenOn(true);
        bt_down.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (down) {

                    if (TextUtils.isEmpty(message)) {
                        openVideoPath = Environment.getExternalStorageDirectory()
                                .getAbsolutePath()
                                + "/222222/"
                                + System.currentTimeMillis() + ".mp4";
                        new Upload(openVideoPath).start();
                    } else {
                        handle.sendEmptyMessage(VIDEO_SUCCESS_DOWN);
                    }


                } else {
                    ToastUtil.showToast(getApplication(), "请先上传");
                }

            }
        });
        bt_up.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                down = false;
                Intent intent = new Intent(CameraAc.this, RecordVideoAc.class);
                startActivityForResult(intent, VIDEO_CAMERA_DATA);

            }
        });
        bofang.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(message)) {
                    play(openVideoPath);
                    Log.e("openVideoPath", openVideoPath);
                } else {
                    play(messageFile.getPath());
                    Log.e("uploadVideoPath", messageFile.getPath());
                }

            }
        });
        bt_local_up.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                down = false;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("video/*");
                startActivityForResult(intent, VIDEO_LOCAL_DATA);
            }
        });
//        int codecID = 28;
//        int res = avcodeDecoder(codecID);
//
//        if (res == 1) {
//            bt_down.setText("Success!");
//        } else {
//            bt_down.setText("Failed!");
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == VIDEO_CAMERA_DATA) {
            uploadVideoPath = data.getStringExtra("uploadVideoPath");
            if (!TextUtils.isEmpty(uploadVideoPath)) {
                new Upload(uploadVideoPath).start();
            } else {
                ToastUtil.showToast(getApplication(), "没有上传资源");
            }
        } else if (requestCode == VIDEO_LOCAL_DATA) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                if (uri == null) {
                    return;
                } else {
                    Log.e("uri.getPath()", uri.getPath());
                    File file = new File(uri.getPath());
                    uploadVideoPath = uri.getPath();
                    file.length();
                    Log.e("file.length()", file.length() + "");
                    file.getName();
                    Log.e("file.getName();", file.getName());
                    if (file.length() > 10000000) {
                        ToastUtil.showToast(getApplication(), "大小不能超过10M");
                        return;
                    }
                    String prefix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
                    Log.e("prefix", prefix);
                    if (!prefix.equalsIgnoreCase("mp4")) {
                        ToastUtil.showToast(getApplication(), "不支持此文件格式");
                        return;
                    }
                    new Upload(uri.getPath()).start();
                }
            }


        }
    }

    public class Upload extends Thread {
        String filepath;

        public Upload(String filepath) {
            super();
            this.filepath = filepath;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            if (!down) {
                ConnectionDetector cd = new ConnectionDetector(
                        CameraAc.this);
                if (cd.isConnectingToInternet()) {
//                    if (cd.checkURL(uploadFilePath)) {
                    uploadFile(filepath);

//                    } else {
//                        handle.sendEmptyMessage(2);
//                    }
                } else {
                    handle.sendEmptyMessage(VIDEO_ERROR_NET);
                }
            } else {
                ConnectionDetector cd = new ConnectionDetector(
                        CameraAc.this);
                if (cd.isConnectingToInternet()) {
//                    if (cd.checkURL(downFilePath)) {
                    downFile(filepath);

//                    } else {
//                        handle.sendEmptyMessage(2);
//                    }
                } else {
                    handle.sendEmptyMessage(VIDEO_ERROR_NET);
                }
            }

        }

    }

    public void downFile(String imageFilePath) {
        handle.sendEmptyMessage(3);
        String actionUrl = downFilePath;
        String params = "name=" + message;
        try {
            URL url = new URL(actionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);

            con.setRequestMethod("POST");
            con.getOutputStream().write(params.getBytes("utf8"));
            DataInputStream di = new DataInputStream(con.getInputStream());
            File file = new File(imageFilePath);

            FileOutputStream fStream = new FileOutputStream(file);
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int length = -1;

            while ((length = di.read(buffer)) != -1) {

                fStream.write(buffer, 0, length);
            }

            fStream.close();
            di.close();
            handle.sendEmptyMessage(VIDEO_SUCCESS_DOWN);
        } catch (Exception e) {
            e.printStackTrace();
            handle.sendEmptyMessage(VIDEO_ERROR_WEB);
        }
    }

    public void uploadFile(String imageFilePath) {
        handle.sendEmptyMessage(VIDEO_SHOW_BAR);
        String actionUrl = uploadFilePath;
        try {
            URL url = new URL(actionUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setDoInput(true);
            con.setDoOutput(true);
            con.setUseCaches(false);

            con.setRequestMethod("POST");

            DataOutputStream ds = new DataOutputStream(con.getOutputStream());
            File file = new File(imageFilePath);

            FileInputStream fStream = new FileInputStream(file);
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int length = -1;

            while ((length = fStream.read(buffer)) != -1) {

                ds.write(buffer, 0, length);
            }

            fStream.close();
            ds.flush();

            InputStream is = con.getInputStream();
            int ch;
            StringBuffer b = new StringBuffer();
            while ((ch = is.read()) != -1) {
                b.append((char) ch);
            }
            Log.e("wwww", b.toString());
            message = b.toString();
            ds.close();
            handle.sendEmptyMessage(VIDEO_SUCCESS_UP);
        } catch (Exception e) {
            e.printStackTrace();
            handle.sendEmptyMessage(VIDEO_ERROR_WEB);
        }
    }

    public void play(String fileName) {
        stop();
        mPlayer = new MediaPlayer();
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mPlayer.setDisplay(mSurfaceHolder); // 定义一个SurfaceView播放它
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer arg0) {
                stop();
                // canvas.drawColor(Color.TRANSPARENT,
                // PorterDuff.Mode.CLEAR);
            }
        });
        try {
            mPlayer.setDataSource(fileName);
            mPlayer.prepare();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mPlayer.start();
    }

    // 结束播放时：

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        // TODO Auto-generated method stub
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        mSurfaceView = null;
        if (mSurfaceHolder != null) {
            mSurfaceHolder = null;
        }
    }
}