package com.shiv.executor
import android.content.Context
object StatusManager {
    private const val PREF="shiv_status"
    fun setRunning(c:Context, r:Boolean){ c.getSharedPreferences(PREF,0).edit().putBoolean("running",r).apply() }
    fun isRunning(c:Context):Boolean = c.getSharedPreferences(PREF,0).getBoolean("running",false)
    fun isServiceRunning(c:Context):Boolean = isRunning(c)

    fun setServiceRunning(c:Context, r:Boolean){ setRunning(c,r) }

    fun getBalance(c:Context):Double = c.getSharedPreferences(PREF,0).getFloat("bal",0.1559f).toDouble()
    fun setBalance(c:Context, b:Double){ c.getSharedPreferences(PREF,0).edit().putFloat("bal",b.toFloat()).apply() }

    fun getPnl(c:Context):Double = c.getSharedPreferences(PREF,0).getFloat("pnl",0f).toDouble()
    fun addPnl(c:Context, d:Double){ val n=getPnl(c)+d; c.getSharedPreferences(PREF,0).edit().putFloat("pnl",n.toFloat()).apply() }

    fun getTrades(c:Context):Int = c.getSharedPreferences(PREF,0).getInt("trades",0)
    fun incTrades(c:Context){ c.getSharedPreferences(PREF,0).edit().putInt("trades",getTrades(c)+1).apply() }

    fun getModules(c:Context):String = c.getSharedPreferences(PREF,0).getString("mods","JUP,MEV,RISK")?:"JUP,MEV,RISK"
    fun setModules(c:Context, s:String){ c.getSharedPreferences(PREF,0).edit().putString("mods",s).apply() }

    fun appendLog(c:Context, m:String){
        val p=c.getSharedPreferences(PREF,0)
        val old=p.getString("log","")?:""
        val tm=java.text.SimpleDateFormat("HH:mm:ss",java.util.Locale.US).format(java.util.Date())
        p.edit().putString("log","[$tm] $m\n$old".take(4000)).apply()
    }
    fun getLogs(c:Context):String = c.getSharedPreferences(PREF,0).getString("log","")?:""
}
