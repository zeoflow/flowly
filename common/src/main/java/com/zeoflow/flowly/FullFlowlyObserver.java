package com.zeoflow.flowly;

interface FullFlowlyObserver extends FlowlyObserver {

    void onCreate(FlowlyOwner owner);

    void onStart(FlowlyOwner owner);

    void onResume(FlowlyOwner owner);

    void onPause(FlowlyOwner owner);

    void onStop(FlowlyOwner owner);

    void onDestroy(FlowlyOwner owner);
}
