package com.zeoowl.books;

import android.app.Application;

import androidx.lifecycle.ProcessLifecycleOwner;

import com.zeoflow.flowly.ApplicationManager;

public class App extends Application {

    public App() {
        ProcessLifecycleOwner.get().getLifecycle().addObserver(AppManager.getInstance());
        ApplicationManager.addObserver(AppManager.getInstance());
    }
}
