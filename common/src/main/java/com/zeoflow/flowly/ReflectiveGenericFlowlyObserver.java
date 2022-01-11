package com.zeoflow.flowly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An internal implementation of {@link FlowlyObserver} that relies on reflection.
 */
class ReflectiveGenericFlowlyObserver implements FlowlyEventObserver {
    private final Object mWrapped;
    private final ClassesInfoCache.CallbackInfo mInfo;

    ReflectiveGenericFlowlyObserver(Object wrapped) {
        mWrapped = wrapped;
        mInfo = ClassesInfoCache.sInstance.getInfo(mWrapped.getClass());
    }

    @Override
    public void onStateChanged(@NonNull FlowlyOwner source, @NonNull Flowly.Event event) {
        mInfo.invokeCallbacks(source, event, mWrapped);
    }

    @Override
    public void onStateChanged(@NonNull FlowlyOwner source, @NonNull Flowly.Event event, @Nullable Object... args) {
        mInfo.invokeCallbacks(source, event, mWrapped, args);
    }
}
