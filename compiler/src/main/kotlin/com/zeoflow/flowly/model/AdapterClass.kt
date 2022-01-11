package com.zeoflow.flowly.model

import com.zeoflow.flowly.Lifecycling
import com.zeoflow.flowly.getPackage
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

data class AdapterClass(
        val type: TypeElement,
        val calls: List<EventMethodCall>,
        val syntheticMethods: Set<ExecutableElement>
)

fun getAdapterName(type: TypeElement): String {
    val packageElement = type.getPackage()
    val qName = type.qualifiedName.toString()
    val partialName = if (packageElement.isUnnamed) qName else qName.substring(
        packageElement.qualifiedName.toString().length + 1
    )
    return Lifecycling.getAdapterName(partialName)
}
