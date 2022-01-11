package com.zeoflow.flowly;

import androidx.annotation.NonNull;

/**
 * @deprecated Use {@code androidx.appcompat.app.AppCompatActivity}
 * which extends {@link FlowlyOwner}, so there are no use cases for this class.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
@Deprecated
public interface FlowlyRegistryOwner extends FlowlyOwner {
    @NonNull
    @Override
    FlowlyRegistry getLifecycle();
}
