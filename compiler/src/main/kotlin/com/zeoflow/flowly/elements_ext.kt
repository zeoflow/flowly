package com.zeoflow.flowly

import com.google.auto.common.MoreElements
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.PackageElement
import javax.lang.model.element.TypeElement
import javax.lang.model.util.ElementFilter

fun Element.getPackage(): PackageElement = MoreElements.getPackage(this)

fun Element.getPackageQName() = getPackage().qualifiedName.toString()

fun ExecutableElement.name() = simpleName.toString()

fun ExecutableElement.isPackagePrivate() = !modifiers.any {
    it == Modifier.PUBLIC || it == Modifier.PROTECTED || it == Modifier.PRIVATE
}

fun ExecutableElement.isProtected() = modifiers.contains(Modifier.PROTECTED)

fun TypeElement.methods(): List<ExecutableElement> = ElementFilter.methodsIn(enclosedElements)

private const val SYNTHETIC = "__synthetic_"

fun syntheticName(method: ExecutableElement) = "$SYNTHETIC${method.simpleName}"

fun isSyntheticMethod(method: ExecutableElement) = method.name().startsWith(SYNTHETIC)
