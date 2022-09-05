package com.zeoflow.flowly.kotlin

import android.util.Log
import com.zeoflow.flowly.ApplicationObserver

open class AppManager protected constructor() : ApplicationObserver {
    override fun onApplicationCreate() {
        Log.d("AppManager", "onAppCreate")
    }

    companion object {
        @JvmStatic
        val instance: AppManager
            get() = AppManager()
    }
}