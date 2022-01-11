package com.zeoowl.books;

import android.app.Application;
import android.content.Context;

import com.zeoflow.flowly.ProcessFlowlyOwner;

public class App extends Application {

    public App() {
        ProcessFlowlyOwner.addObserver(AppManager.getInstance());
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
