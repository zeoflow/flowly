package com.zeoflow.flowly;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class CompositeGeneratedAdaptersObserver implements FlowlyEventObserver {

    private final GeneratedAdapter[] mGeneratedAdapters;

    CompositeGeneratedAdaptersObserver(GeneratedAdapter[] generatedAdapters) {
        mGeneratedAdapters = generatedAdapters;
    }

    @Override
    public void onStateChanged(@NonNull FlowlyOwner source, @NonNull Flowly.Event event) {
        MethodCallsLogger logger = new MethodCallsLogger();
        for (GeneratedAdapter mGenerated: mGeneratedAdapters) {
            mGenerated.callMethods(source, event, false, logger);
        }
        for (GeneratedAdapter mGenerated: mGeneratedAdapters) {
            mGenerated.callMethods(source, event, true, logger);
        }
    }

    @Override
    public void onStateChanged(
            @NonNull FlowlyOwner source,
            @NonNull Flowly.Event event,
            @Nullable Object... args
    ) {
        MethodCallsLogger logger = new MethodCallsLogger();
        for (GeneratedAdapter mGenerated: mGeneratedAdapters) {
            mGenerated.callMethods(source, event, false, logger);
        }
        for (GeneratedAdapter mGenerated: mGeneratedAdapters) {
            mGenerated.callMethods(source, event, true, logger);
        }
    }
}
