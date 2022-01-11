package com.zeoflow.flowly;

import androidx.annotation.NonNull;

/**
 * Callback interface for listening to {@link FlowlyOwner} state changes.
 * If a class implements both this interface and {@link FlowlyEventObserver}, then
 * methods of {@code DefaultLifecycleObserver} will be called first, and then followed by the call
 * of {@link FlowlyEventObserver#onStateChanged(FlowlyOwner, Flowly.Event)}
 * <p>
 * If a class implements this interface and in the same time uses {@link OnFlowlyEvent}, then
 * annotations will be ignored.
 */
@SuppressWarnings("unused")
public interface DefaultFlowlyObserver extends FullFlowlyObserver {

    /**
     * Notifies that {@code ON_CREATE} event occurred.
     * <p>
     * This method will be called after the {@link FlowlyOwner}'s {@code onCreate}
     * method returns.
     *
     * @param owner the component, whose state was changed
     */
    @Override
    default void onCreate(@NonNull FlowlyOwner owner) {
    }

    /**
     * Notifies that {@code ON_START} event occurred.
     * <p>
     * This method will be called after the {@link FlowlyOwner}'s {@code onStart} method returns.
     *
     * @param owner the component, whose state was changed
     */
    @Override
    default void onStart(@NonNull FlowlyOwner owner) {
    }

    /**
     * Notifies that {@code ON_RESUME} event occurred.
     * <p>
     * This method will be called after the {@link FlowlyOwner}'s {@code onResume}
     * method returns.
     *
     * @param owner the component, whose state was changed
     */
    @Override
    default void onResume(@NonNull FlowlyOwner owner) {
    }

    /**
     * Notifies that {@code ON_PAUSE} event occurred.
     * <p>
     * This method will be called before the {@link FlowlyOwner}'s {@code onPause} method
     * is called.
     *
     * @param owner the component, whose state was changed
     */
    @Override
    default void onPause(@NonNull FlowlyOwner owner) {
    }

    /**
     * Notifies that {@code ON_STOP} event occurred.
     * <p>
     * This method will be called before the {@link FlowlyOwner}'s {@code onStop} method
     * is called.
     *
     * @param owner the component, whose state was changed
     */
    @Override
    default void onStop(@NonNull FlowlyOwner owner) {
    }

    /**
     * Notifies that {@code ON_DESTROY} event occurred.
     * <p>
     * This method will be called before the {@link FlowlyOwner}'s {@code onDestroy} method
     * is called.
     *
     * @param owner the component, whose state was changed
     */
    @Override
    default void onDestroy(@NonNull FlowlyOwner owner) {
    }
}


