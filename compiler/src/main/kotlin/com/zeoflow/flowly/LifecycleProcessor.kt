package com.zeoflow.flowly

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedAnnotationTypes("OnLifecycleEvent")
class LifecycleProcessor : AbstractProcessor() {
    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        val input = collectAndVerifyInput(processingEnv, roundEnv)
        writeModels(transformToOutput(processingEnv, input), processingEnv)
        return true
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }
}
