package com.cooloongwu.recordstudio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final String STATE_RESULT_CODE = "result_code";
    private static final String STATE_RESULT_DATA = "result_data";

    private static final int REQUEST_MEDIA_PROJECTION = 1;
    private int mResultCode;
    private Intent mResultData;

    private int mScreenDensity;
    private Surface mSurface;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjectionManager mMediaProjectionManager;
    private SurfaceView mSurfaceView;

    private FFmpeg ffmpeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ffmpeg = FFmpeg.getInstance(this);

        if (savedInstanceState != null) {
            mResultCode = savedInstanceState.getInt(STATE_RESULT_CODE);
            mResultData = savedInstanceState.getParcelable(STATE_RESULT_DATA);
        }

        mSurfaceView = findViewById(R.id.surface);
        mSurface = mSurfaceView.getHolder().getSurface();
        Button btnStart = findViewById(R.id.btn_start);
        Button btnStop = findViewById(R.id.btn_stop);
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mMediaProjectionManager = (MediaProjectionManager)
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private void test() {
        String[] cmd = new String[]{
                "-re",
                "-i",
//                "/sdcard/DCIM/Camera/test.avi",
                "/sdcard/66/seve.mp4",
                "-vcodec",
                "libx264",
                "-acodec",
                "aac",
                "-f",
                "flv",
                "-strict",
                "-2",
                My.streamUrl
        };

        try {
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    super.onSuccess(message);
                    Log.e(TAG, "onSuccess：" + message);
                }

                @Override
                public void onProgress(String message) {
                    super.onProgress(message);
                    Log.e(TAG, "onProgress：" + message);
                }

                @Override
                public void onFailure(String message) {
                    super.onFailure(message);
                    Log.e(TAG, "onFailure：" + message);
                }

                @Override
                public void onStart() {
                    super.onStart();
                    Log.e(TAG, "onStart");
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    Log.e(TAG, "onFinish");
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                test();
//                startScreenCapture();
                break;
            case R.id.btn_stop:
                stopScreenCapture();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScreenCapture();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tearDownMediaProjection();
    }

    private void setUpMediaProjection() {
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mResultCode, mResultData);
    }

    private void setUpVirtualDisplay() {
        Log.i(TAG, "Setting up a VirtualDisplay: " +
                mSurfaceView.getWidth() + "x" + mSurfaceView.getHeight() +
                " (" + mScreenDensity + ")");
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(
                "ScreenCapture",
                mSurfaceView.getWidth(),
                mSurfaceView.getHeight(),
                mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mSurface, null, null);
    }

    private void startScreenCapture() {
        if (mSurface == null) {
            return;
        }
        if (mMediaProjection != null) {
            setUpVirtualDisplay();
        } else if (mResultCode != 0 && mResultData != null) {
            setUpMediaProjection();
            setUpVirtualDisplay();
        } else {
            Log.i(TAG, "Requesting confirmation");
            // This initiates a prompt dialog for the user to confirm screen projection.
            startActivityForResult(
                    mMediaProjectionManager.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION);
        }
    }

    private void stopScreenCapture() {
        if (mVirtualDisplay == null) {
            return;
        }
        mVirtualDisplay.release();
        mVirtualDisplay = null;
    }

    private void tearDownMediaProjection() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.e(TAG, "Starting screen capture");
            mResultCode = resultCode;
            mResultData = data;
            setUpMediaProjection();
            setUpVirtualDisplay();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mResultData != null) {
            outState.putInt(STATE_RESULT_CODE, mResultCode);
            outState.putParcelable(STATE_RESULT_DATA, mResultData);
        }
    }
    /* // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
    }

    *//**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     *//*
    public native String stringFromJNI();*/
}
