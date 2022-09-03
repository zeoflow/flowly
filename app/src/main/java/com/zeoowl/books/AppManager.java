package com.zeoowl.books;

import android.util.Log;

import androidx.lifecycle.DefaultLifecycleObserver;

import com.zeoflow.flowly.ApplicationObserver;

public class AppManager implements DefaultLifecycleObserver, ApplicationObserver {

    protected AppManager() {

    }

    public static AppManager getInstance() {
        return new AppManager();
    }

    @Override
    public void onApplicationCreate() {
        Log.d("AppManager", "onAppCreate");
    }
}
