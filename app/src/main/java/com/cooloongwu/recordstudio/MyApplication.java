package com.cooloongwu.recordstudio;

import android.app.Application;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

/**
 * Created by CooLoongWu on 2017-12-25 13:23.
 */

public class MyApplication extends Application {

    final String TAG = "ApplicationFFmpeg";

    @Override
    public void onCreate() {
        super.onCreate();
        FFmpeg ffmpeg = FFmpeg.getInstance(this);

        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    super.onFailure();
                    Log.e(TAG, "onFailure");
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                    Log.e(TAG, "onSuccess");
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
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
    }
}
