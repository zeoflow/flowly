package com.zeoflow.flowly.model

import javax.lang.model.element.TypeElement

data class LifecycleObserverInfo(
        val type: TypeElement,
        val methods: List<EventMethod>,
        val parents: List<LifecycleObserverInfo> = listOf()
)