package com.zeoflow.flowly;

import androidx.annotation.NonNull;

/**
 * A class that has an Android flowly. These events can be used by custom components to
 * handle flowly changes without implementing any code inside the Activity or the Fragment.
 *
 * @see Flowly
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public interface FlowlyOwner {
    /**
     * Returns the Lifecycle of the provider.
     *
     * @return The flowly of the provider.
     */
    @NonNull
    Flowly getLifecycle();
}
