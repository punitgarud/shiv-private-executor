package com.shiv.executor
import android.util.Log
object CrashHandler {
    fun init() {
        val old = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            try {
                Log.e("SHIV_CRASH", "Crash in ${t.name}", e)
                val sw = java.io.StringWriter()
                e.printStackTrace(java.io.PrintWriter(sw))
                val file = java.io.File("/data/data/com.shiv.executor/files/crash.log")
                file.parentFile?.mkdirs()
                file.writeText(sw.toString())
                // also external
                val ext = java.io.File("/sdcard/Download/shiv_crash.log")
                try { ext.writeText(sw.toString()) } catch(_:Exception){}
            } catch(_:Exception){}
            old?.uncaughtException(t, e)
        }
    }
}
