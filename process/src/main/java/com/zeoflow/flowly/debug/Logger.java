package com.zeoflow.flowly.debug;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

import com.zeoflow.flowly.runtime.BuildConfig;

@SuppressWarnings("unused")
public class Logger {

    @Size(min = 1L, max = 23L)
    private final String tag;

    public Logger(@NonNull @Size(min = 1L, max = 23L) String tag) {
        this.tag = tag;
    }

    public void d(@NonNull String message) {
        if (message.isEmpty()) {
            return;
        }
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.d(tag, message);
    }

    public void e(@NonNull String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.e(tag, message);
    }

    public void w(@NonNull String message) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        Log.w(tag, message);
    }

}
