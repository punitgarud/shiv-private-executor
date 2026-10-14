package com.shiv.executor

import android.content.Context

object StatusManager {
    fun prefs(c: Context) = c.getSharedPreferences("shiv_status", Context.MODE_PRIVATE)
    fun setServiceRunning(c: Context, running: Boolean) {
        prefs(c).edit().putBoolean("svc_running", running).putLong("last", System.currentTimeMillis()).apply()
    }
    fun isServiceRunning(c: Context): Boolean = prefs(c).getBoolean("svc_running", false)
    fun setBalance(c: Context, sol: Double) {
        val usd = sol * 152.0
        prefs(c).edit().putString("bal_sol", "%.4f".format(sol)).putString("bal_usd", "%.2f".format(usd)).apply()
    }
    fun getBalanceText(c: Context): String {
        val sol = prefs(c).getString("bal_sol", "0.0500")?: "0.0500"
        val usd = prefs(c).getString("bal_usd", "7.60")?: "7.60"
        return "Balance: $sol SOL ($$usd) | Guard: ON"
    }
    fun setTop4(c: Context, t: String) { prefs(c).edit().putString("top4", t).apply() }
    fun getTop4(c: Context): String = prefs(c).getString("top4", "Top4: Scanning Jupiter | Fee: LOWEST | MEV 1%")?: ""
    fun appendLog(c: Context, msg: String) {
        val p = prefs(c)
        val old = p.getString("logs", "")?: ""
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val newLogs = (old + "\n[$time] $msg").takeLast(5000)
        p.edit().putString("logs", newLogs).apply()
    }
    fun getLogs(c: Context): String = prefs(c).getString("logs", "")?: ""
}
