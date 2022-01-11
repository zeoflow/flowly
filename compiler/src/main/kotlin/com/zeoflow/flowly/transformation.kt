package com.zeoflow.flowly

import com.zeoflow.flowly.model.AdapterClass
import com.zeoflow.flowly.model.EventMethod
import com.zeoflow.flowly.model.EventMethodCall
import com.zeoflow.flowly.model.InputModel
import com.zeoflow.flowly.model.FlowlyObserverInfo
import com.google.common.collect.HashMultimap
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

private fun mergeAndVerifyMethods(
        processingEnv: ProcessingEnvironment,
        type: TypeElement,
        classMethods: List<EventMethod>,
        parentMethods: List<EventMethod>
): List<EventMethod> {
    // need to update parent methods like that because:
    // 1. visibility can be expanded
    // 2. we want to preserve order
    val updatedParentMethods = parentMethods.map { parentMethod ->
        val overrideMethod = classMethods.find { (method) ->
            processingEnv.elementUtils.overrides(method, parentMethod.method, type)
        }
        if (overrideMethod != null) {
            if (overrideMethod.onFlowlyEvent != parentMethod.onFlowlyEvent) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR,
                        ErrorMessages.INVALID_STATE_OVERRIDE_METHOD, overrideMethod.method
                )
            }
            overrideMethod
        } else {
            parentMethod
        }
    }
    return updatedParentMethods + classMethods.filterNot { updatedParentMethods.contains(it) }
}

fun flattenObservers(
    processingEnv: ProcessingEnvironment,
    world: Map<TypeElement, FlowlyObserverInfo>
): List<FlowlyObserverInfo> {
    val flattened: MutableMap<FlowlyObserverInfo, FlowlyObserverInfo> = mutableMapOf()

    fun traverse(observer: FlowlyObserverInfo) {
        if (observer in flattened) {
            return
        }
        if (observer.parents.isEmpty()) {
            flattened[observer] = observer
            return
        }
        observer.parents.forEach(::traverse)
        val methods = observer.parents
            .map(flattened::get)
            .fold(emptyList<EventMethod>()) { list, parentObserver ->
                mergeAndVerifyMethods(
                    processingEnv, observer.type,
                    parentObserver!!.methods, list
                )
            }

        flattened[observer] = FlowlyObserverInfo(
            observer.type,
            mergeAndVerifyMethods(processingEnv, observer.type, observer.methods, methods)
        )
    }

    world.values.forEach(::traverse)
    return flattened.values.toList()
}

private fun needsSyntheticAccess(type: TypeElement, eventMethod: EventMethod): Boolean {
    val executable = eventMethod.method
    return type.getPackageQName() != eventMethod.packageName() &&
        (executable.isPackagePrivate() || executable.isProtected())
}

private fun validateMethod(
        processingEnv: ProcessingEnvironment,
        world: InputModel,
        type: TypeElement,
        eventMethod: EventMethod
): Boolean {
    if (!needsSyntheticAccess(type, eventMethod)) {
        // no synthetic calls - no problems
        return true
    }

    if (world.isRootType(eventMethod.type)) {
        // we will generate adapters for them, so we can generate all accessors
        return true
    }

    if (world.hasSyntheticAccessorFor(eventMethod)) {
        // previously generated adapter already has synthetic
        return true
    }

    processingEnv.messager.printMessage(
        Diagnostic.Kind.WARNING,
            ErrorMessages.failedToGenerateAdapter(type, eventMethod), type
    )
    return false
}

fun transformToOutput(
    processingEnv: ProcessingEnvironment,
    world: InputModel
): List<AdapterClass> {
    val flatObservers = flattenObservers(processingEnv, world.observersInfo)
    val syntheticMethods = HashMultimap.create<TypeElement, EventMethodCall>()
    val adapterCalls = flatObservers
        // filter out everything that arrived from jars
        .filter { (type) -> world.isRootType(type) }
        // filter out if it needs SYNTHETIC access and we can't generate adapter for it
        .filter { (type, methods) ->
            methods.all { eventMethod ->
                validateMethod(processingEnv, world, type, eventMethod)
            }
        }
        .map { (type, methods) ->
            val calls = methods.map { eventMethod ->
                if (needsSyntheticAccess(type, eventMethod)) {
                    EventMethodCall(eventMethod, eventMethod.type)
                } else {
                    EventMethodCall(eventMethod)
                }
            }
            calls.filter { it.syntheticAccess != null }.forEach { eventMethod ->
                syntheticMethods.put(eventMethod.method.type, eventMethod)
            }
            type to calls
        }.toMap()

    return adapterCalls
        .map { (type, calls) ->
            val methods = syntheticMethods.get(type) ?: emptySet()
            val synthetic = methods.map { eventMethod -> eventMethod!!.method.method }.toSet()
            AdapterClass(type, calls, synthetic)
        }
}
