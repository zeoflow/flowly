package com.zeoflow.flowly.model

import com.zeoflow.flowly.name
import com.zeoflow.flowly.syntheticName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

data class InputModel(
    // all java files with flowly annotations excluding classes from classpath
        private val rootTypes: Set<TypeElement>,
    // info about all flowly observers including classes from classpath
        val observersInfo: Map<TypeElement, LifecycleObserverInfo>,
    // info about generated adapters from class path
        val generatedAdapters: Map<TypeElement, List<ExecutableElement>>
) {

    /**
     *  Root class is class defined in currently processed module, not in classpath
     */
    fun isRootType(type: TypeElement) = type in rootTypes

    fun hasSyntheticAccessorFor(eventMethod: EventMethod): Boolean {
        val syntheticMethods = generatedAdapters[eventMethod.type] ?: return false
        return syntheticMethods.any { executable ->
            executable.name() == syntheticName(eventMethod.method) &&
                // same number + receiver object
                (eventMethod.method.parameters.size + 1) == executable.parameters.size
        }
    }
}