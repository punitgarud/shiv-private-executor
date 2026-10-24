package com.shiv.executor

import android.content.Context
import android.content.SharedPreferences

object StatusManager {
    private const val PREFS = "shiv_prefs"
    
    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    }

    fun getTrades(context: Context): Int {
        return prefs(context).getInt("trades", 0)
    }
    fun setTrades(context: Context, v: Int) {
        prefs(context).edit().putInt("trades", v).apply()
    }
    fun getPnl(context: Context): Double {
        return prefs(context).getFloat("pnl", 0f).toDouble()
    }
    fun setPnl(context: Context, v: Double) {
        prefs(context).edit().putFloat("pnl", v.toFloat()).apply()
    }
    fun getDailyLoss(context: Context): Double {
        return prefs(context).getFloat("daily_loss", 0f).toDouble()
    }
    fun addDailyLoss(context: Context, v: Double) {
        val cur = getDailyLoss(context)
        prefs(context).edit().putFloat("daily_loss", (cur + v).toFloat()).apply()
    }
    fun appendLog(context: Context, msg: String) {
        val cur = prefs(context).getString("logs", "") ?: ""
        prefs(context).edit().putString("logs", cur + "\n" + msg).apply()
    }
    fun getLogs(context: Context): String {
        return prefs(context).getString("logs", "") ?: ""
    }
    fun setStatus(context: Context, s: String) {
        prefs(context).edit().putString("status", s).apply()
    }
}
