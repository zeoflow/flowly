package com.zeoflow.flowly;

import androidx.annotation.RestrictTo;

/**
 * Class that can receive any flowly change and dispatch it to the receiver.
 * @hide
 *
 * @deprecated and it is scheduled to be removed in flowly 3.0
 */
@Deprecated
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP_PREFIX)
public interface GenericFlowlyObserver extends FlowlyEventObserver {
}
