package com.zeoflow.flowly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class SingleGeneratedAdapterObserver implements FlowlyEventObserver {

    private final GeneratedAdapter mGeneratedAdapter;

    SingleGeneratedAdapterObserver(GeneratedAdapter generatedAdapter) {
        mGeneratedAdapter = generatedAdapter;
    }

    @Override
    public void onStateChanged(
            @NonNull FlowlyOwner source,
            @NonNull Flowly.Event event
    ) {
        mGeneratedAdapter.callMethods(source, event, false, null);
        mGeneratedAdapter.callMethods(source, event, true, null);
    }

    @Override
    public void onStateChanged(
            @NonNull FlowlyOwner source,
            @NonNull Flowly.Event event,
            @Nullable Object... args
    ) {
        mGeneratedAdapter.callMethods(source, event, false, null);
        mGeneratedAdapter.callMethods(source, event, true, null);
    }
}
