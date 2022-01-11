package com.zeoflow.flowly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class SingleGeneratedAdapterObserver implements LifecycleEventObserver {

    private final GeneratedAdapter mGeneratedAdapter;

    SingleGeneratedAdapterObserver(GeneratedAdapter generatedAdapter) {
        mGeneratedAdapter = generatedAdapter;
    }

    @Override
    public void onStateChanged(
            @NonNull LifecycleOwner source,
            @NonNull Lifecycle.Event event
    ) {
        mGeneratedAdapter.callMethods(source, event, false, null);
        mGeneratedAdapter.callMethods(source, event, true, null);
    }

    @Override
    public void onStateChanged(
            @NonNull LifecycleOwner source,
            @NonNull Lifecycle.Event event,
            @Nullable Object... args
    ) {
        mGeneratedAdapter.callMethods(source, event, false, null);
        mGeneratedAdapter.callMethods(source, event, true, null);
    }
}
