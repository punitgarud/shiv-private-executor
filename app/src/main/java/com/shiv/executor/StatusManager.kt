package com.shiv.executor
import android.content.Context

object StatusManager {
    private const val PREF="shiv_status"
    private const val KEY_RUNNING="running"
    private const val KEY_BAL="balance"
    private const val KEY_LOG="log"
    private const val KEY_DAILY_LOSS="daily_loss"
    private const val KEY_TRADES="trades"
    private const val KEY_PNL="pnl"
    private const val KEY_MODULES="modules"

    fun setRunning(c:Context, r:Boolean){ c.getSharedPreferences(PREF,0).edit().putBoolean(KEY_RUNNING,r).apply() }
    fun isRunning(c:Context):Boolean { return c.getSharedPreferences(PREF,0).getBoolean(KEY_RUNNING,false) }
    fun isServiceRunning(c:Context):Boolean = isRunning(c)
    fun setServiceRunning(c:Context, r:Boolean){ setRunning(c,r) }

    fun setBalance(c:Context, b:Double){ c.getSharedPreferences(PREF,0).edit().putFloat(KEY_BAL,b.toFloat()).apply() }
    fun getBalance(c:Context):Double { return c.getSharedPreferences(PREF,0).getFloat(KEY_BAL,0.05f).toDouble() }

    fun setTrades(c:Context, t:Int){ c.getSharedPreferences(PREF,0).edit().putInt(KEY_TRADES,t).apply() }
    fun getTrades(c:Context):Int { return c.getSharedPreferences(PREF,0).getInt(KEY_TRADES,0) }

    fun setPnl(c:Context, p:Double){ c.getSharedPreferences(PREF,0).edit().putFloat(KEY_PNL,p.toFloat()).apply() }
    fun getPnl(c:Context):Double { return c.getSharedPreferences(PREF,0).getFloat(KEY_PNL,0f).toDouble() }

    fun setModules(c:Context, m:String){ c.getSharedPreferences(PREF,0).edit().putString(KEY_MODULES,m).apply() }
    fun getModules(c:Context):String { return c.getSharedPreferences(PREF,0).getString(KEY_MODULES,"JUP,FEE")?:"JUP,FEE" }

    fun appendLog(c:Context, msg:String){
        val prefs=c.getSharedPreferences(PREF,0)
        val old=prefs.getString(KEY_LOG,"")?:""
        val time=java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val n="[$time] $msg\n$old"
        prefs.edit().putString(KEY_LOG,n.take(5000)).apply()
    }
    fun getLogs(c:Context):String { return c.getSharedPreferences(PREF,0).getString(KEY_LOG,"")?:"" }

    fun addDailyLoss(c:Context, loss:Double){
        val prefs=c.getSharedPreferences(PREF,0)
        val cur=prefs.getFloat(KEY_DAILY_LOSS,0f).toDouble()
        prefs.edit().putFloat(KEY_DAILY_LOSS,(cur+loss).toFloat()).apply()
    }
    fun getDailyLoss(c:Context):Double { return c.getSharedPreferences(PREF,0).getFloat(KEY_DAILY_LOSS,0f).toDouble() }
    fun resetDailyLoss(c:Context){ c.getSharedPreferences(PREF,0).edit().putFloat(KEY_DAILY_LOSS,0f).apply() }
}
