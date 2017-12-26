package com.cooloongwu.recordstudio;

import android.app.Application;
import android.util.Log;

import nl.bravobit.ffmpeg.FFmpeg;

/**
 * Created by CooLoongWu on 2017-12-25 13:23.
 */

public class MyApplication extends Application {

    final String TAG = "ApplicationFFmpeg";

    @Override
    public void onCreate() {
        super.onCreate();

        if (FFmpeg.getInstance(this).isSupported()) {
            Log.e(TAG, "FFmpeg支持");
        } else {
            Log.e(TAG, "FFmpeg不支持");
        }
    }
}
