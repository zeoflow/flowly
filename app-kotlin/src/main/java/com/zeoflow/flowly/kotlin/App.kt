package com.zeoflow.flowly.kotlin

import android.app.Application
import com.zeoflow.flowly.kotlin.AppManager.Companion.instance
import com.zeoflow.flowly.ApplicationManager

class App : Application() {

    init {
        ApplicationManager.addObserver(instance)
    }

}