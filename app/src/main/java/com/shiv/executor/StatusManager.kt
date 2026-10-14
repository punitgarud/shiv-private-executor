package com.shiv.executor
import android.content.Context
object StatusManager {
    private const val PREF="shiv_status"
    private const val KEY_RUNNING="running"
    fun setRunning(c:Context, r:Boolean){ c.getSharedPreferences(PREF,0).edit().putBoolean(KEY_RUNNING,r).apply() }
    fun isServiceRunning(c:Context):Boolean {
        return c.getSharedPreferences(PREF,0).getBoolean(KEY_RUNNING,false)
    }
}
