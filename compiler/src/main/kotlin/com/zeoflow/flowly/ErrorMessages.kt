package com.zeoflow.flowly

import com.zeoflow.flowly.model.EventMethod
import javax.lang.model.element.TypeElement

object ErrorMessages {
    const val TOO_MANY_ARGS = "callback method cannot have more than 2 parameters"
    const val TOO_MANY_ARGS_NOT_ON_ANY = "only callback annotated with ON_ANY " +
        "can have 2 parameters"
    const val INVALID_SECOND_ARGUMENT = "2nd argument of a callback method" +
        " must be Lifecycle.Event and represent the current event"
    const val INVALID_FIRST_ARGUMENT = "1st argument of a callback method must be " +
        "a LifecycleOwner which represents the source of the event"
    const val INVALID_METHOD_MODIFIER = "method marked with OnLifecycleEvent annotation can " +
        "not be private"
    const val INVALID_CLASS_MODIFIER = "class containing OnLifecycleEvent methods can not be " +
        "private"
    const val INVALID_STATE_OVERRIDE_METHOD = "overridden method must handle the same " +
        "onState changes as original method"
    const val INVALID_ENCLOSING_ELEMENT =
        "Parent of OnLifecycleEvent should be a class or interface"
    const val INVALID_ANNOTATED_ELEMENT = "OnLifecycleEvent can only be added to methods"

    fun failedToGenerateAdapter(type: TypeElement, failureReason: EventMethod) =
        """
             Failed to generate an Adapter for $type, because it needs to be able to access to
             package private method ${failureReason.method.name()} from ${failureReason.type}
            """.trim()
}
