package com.zeoflow.flowly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * An internal implementation of {@link LifecycleObserver} that relies on reflection.
 */
class ReflectiveGenericLifecycleObserver implements LifecycleEventObserver {
    private final Object mWrapped;
    private final ClassesInfoCache.CallbackInfo mInfo;

    ReflectiveGenericLifecycleObserver(Object wrapped) {
        mWrapped = wrapped;
        mInfo = ClassesInfoCache.sInstance.getInfo(mWrapped.getClass());
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        mInfo.invokeCallbacks(source, event, mWrapped);
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event, @Nullable Object... args) {
        mInfo.invokeCallbacks(source, event, mWrapped, args);
    }
}
