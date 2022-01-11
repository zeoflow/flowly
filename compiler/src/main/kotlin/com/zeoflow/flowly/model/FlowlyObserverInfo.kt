package com.zeoflow.flowly.model

import javax.lang.model.element.TypeElement

data class FlowlyObserverInfo(
        val type: TypeElement,
        val methods: List<EventMethod>,
        val parents: List<FlowlyObserverInfo> = listOf()
)