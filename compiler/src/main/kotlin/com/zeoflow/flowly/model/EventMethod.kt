package com.zeoflow.flowly.model

import com.zeoflow.flowly.OnLifecycleEvent
import com.zeoflow.flowly.getPackageQName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

data class EventMethod(
        val method: ExecutableElement,
        val onLifecycleEvent: OnLifecycleEvent,
        val type: TypeElement
) {

    fun packageName() = type.getPackageQName()
}

data class EventMethodCall(val method: EventMethod, val syntheticAccess: TypeElement? = null)