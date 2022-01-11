package com.zeoflow.flowly.model

import com.zeoflow.flowly.OnFlowlyEvent
import com.zeoflow.flowly.getPackageQName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

data class EventMethod(
    val method: ExecutableElement,
    val onFlowlyEvent: OnFlowlyEvent,
    val type: TypeElement
) {

    fun packageName() = type.getPackageQName()
}

data class EventMethodCall(val method: EventMethod, val syntheticAccess: TypeElement? = null)