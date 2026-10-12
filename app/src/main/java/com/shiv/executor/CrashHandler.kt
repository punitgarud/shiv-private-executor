package com.shiv.executor
import android.util.Log
object CrashHandler {
    fun init() {
        val old = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                Log.e("SHIV_CRASH", "Crash in ${t.name}: ${e.message}", e)
                e.printStackTrace()
            } catch(_:Exception){}
            old?.uncaughtException(t, e)
        }
    }
}
