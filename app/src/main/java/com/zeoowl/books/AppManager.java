package com.zeoowl.books;

import android.util.Log;

import com.zeoflow.flowly.Flowly;
import com.zeoflow.flowly.FlowlyObserver;
import com.zeoflow.flowly.OnFlowlyEvent;

public class AppManager implements FlowlyObserver {

    protected AppManager() {

    }

    public static AppManager getInstance() {
        return new AppManager();
    }

    @OnFlowlyEvent(Flowly.Event.ON_APPLICATION_CREATED)
    public void onAppStartUp() {

    }

    @OnFlowlyEvent(Flowly.Event.ON_APPLICATION_STARTED)
    public void onStart() {
        Log.d("AppManager", "onStart");
    }

    @OnFlowlyEvent(Flowly.Event.ON_ACTIVITY_CREATED)
    public void onActivityCreated() {
        Log.d("AppManager", "onActivityCreated");
    }

    @OnFlowlyEvent(Flowly.Event.ON_ACTIVITY_PAUSED)
    public void onActivityPaused() {
        Log.d("AppManager", "onActivityPaused");
    }

    @OnFlowlyEvent(Flowly.Event.ON_ACTIVITY_RESUMED)
    public void onActivityResumed() {
        Log.d("AppManager", "onActivityResumed");
    }
}
