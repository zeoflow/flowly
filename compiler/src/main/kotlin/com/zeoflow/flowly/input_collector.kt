package com.zeoflow.flowly

import com.zeoflow.flowly.model.EventMethod
import com.zeoflow.flowly.model.InputModel
import com.zeoflow.flowly.model.FlowlyObserverInfo
import com.zeoflow.flowly.model.getAdapterName
import com.google.auto.common.MoreElements
import com.google.auto.common.MoreTypes
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

fun collectAndVerifyInput(
    processingEnv: ProcessingEnvironment,
    roundEnv: RoundEnvironment
): InputModel {
    val validator = Validator(processingEnv)
    val worldCollector = ObserversCollector(processingEnv)
    val roots = roundEnv.getElementsAnnotatedWith(OnFlowlyEvent::class.java).map { elem ->
        if (elem.kind != ElementKind.METHOD) {
            validator.printErrorMessage(ErrorMessages.INVALID_ANNOTATED_ELEMENT, elem)
            null
        } else {
            val enclosingElement = elem.enclosingElement
            if (validator.validateClass(enclosingElement)) {
                MoreElements.asType(enclosingElement)
            } else {
                null
            }
        }
    }.filterNotNull().toSet()
    roots.forEach { worldCollector.collect(it) }
    val observersInfo = worldCollector.observers
    val generatedAdapters = worldCollector.observers.keys
        .mapNotNull { type ->
            worldCollector.generatedAdapterInfoFor(type)?.let { type to it }
        }.toMap()
    return InputModel(roots, observersInfo, generatedAdapters)
}

class ObserversCollector(processingEnv: ProcessingEnvironment) {
    val typeUtils: Types = processingEnv.typeUtils
    val elementUtils: Elements = processingEnv.elementUtils
    val lifecycleObserverTypeMirror: TypeMirror =
        elementUtils.getTypeElement(FlowlyObserver::class.java.canonicalName).asType()
    val validator = Validator(processingEnv)
    val observers: MutableMap<TypeElement, FlowlyObserverInfo> = mutableMapOf()

    fun collect(type: TypeElement): FlowlyObserverInfo? {
        if (type in observers) {
            return observers[type]
        }
        val parents = (listOf(type.superclass) + type.interfaces)
            .filter { typeUtils.isAssignable(it, lifecycleObserverTypeMirror) }
            .filterNot { typeUtils.isSameType(it, lifecycleObserverTypeMirror) }
            .map { collect(MoreTypes.asTypeElement(it)) }
            .filterNotNull()
        val info = createObserverInfo(type, parents)
        if (info != null) {
            observers[type] = info
        }
        return info
    }

    fun generatedAdapterInfoFor(type: TypeElement): List<ExecutableElement>? {
        val packageName = if (type.getPackageQName().isEmpty()) "" else "${type.getPackageQName()}."
        val adapterType = elementUtils.getTypeElement(packageName + getAdapterName(type))
        return adapterType?.methods()
            ?.filter { executable -> isSyntheticMethod(executable) }
    }

    private fun createObserverInfo(
        typeElement: TypeElement,
        parents: List<FlowlyObserverInfo>
    ): FlowlyObserverInfo? {
        if (!validator.validateClass(typeElement)) {
            return null
        }
        val methods = typeElement.methods().filter { executable ->
            MoreElements.isAnnotationPresent(executable, OnFlowlyEvent::class.java)
        }.map { executable ->
            val onState = executable.getAnnotation(OnFlowlyEvent::class.java)
            if (validator.validateMethod(executable, onState.value)) {
                EventMethod(executable, onState, typeElement)
            } else {
                null
            }
        }.filterNotNull()
        return FlowlyObserverInfo(typeElement, methods, parents)
    }
}

class Validator(val processingEnv: ProcessingEnvironment) {

    fun printErrorMessage(msg: CharSequence, elem: Element) {
        processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, msg, elem)
    }

    fun validateParam(
        param: VariableElement,
        expectedType: Class<*>,
        errorMsg: String
    ): Boolean {
        if (!MoreTypes.isTypeOf(expectedType, param.asType())) {
            printErrorMessage(errorMsg, param)
            return false
        }
        return true
    }

    fun validateMethod(method: ExecutableElement, event: Flowly.Event): Boolean {
        if (Modifier.PRIVATE in method.modifiers) {
            printErrorMessage(ErrorMessages.INVALID_METHOD_MODIFIER, method)
            return false
        }
        val params = method.parameters
        if ((params.size > 2)) {
            printErrorMessage(ErrorMessages.TOO_MANY_ARGS, method)
            return false
        }

        if (params.size == 2 && event != Flowly.Event.ON_ANY) {
            printErrorMessage(ErrorMessages.TOO_MANY_ARGS_NOT_ON_ANY, method)
            return false
        }

        if (params.size == 2 && !validateParam(
                params[1], Flowly.Event::class.java,
                        ErrorMessages.INVALID_SECOND_ARGUMENT
            )
        ) {
            return false
        }

        if (params.size > 0) {
            return validateParam(
                params[0], FlowlyOwner::class.java,
                    ErrorMessages.INVALID_FIRST_ARGUMENT
            )
        }
        return true
    }

    fun validateClass(classElement: Element): Boolean {
        if (!MoreElements.isType(classElement)) {
            printErrorMessage(ErrorMessages.INVALID_ENCLOSING_ELEMENT, classElement)
            return false
        }
        if (Modifier.PRIVATE in classElement.modifiers) {
            printErrorMessage(ErrorMessages.INVALID_CLASS_MODIFIER, classElement)
            return false
        }
        return true
    }
}
