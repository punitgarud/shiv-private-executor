package com.shiv.executor

import android.content.Context
import android.content.SharedPreferences

object StatusManager {
    private const val PREF = "shiv_status"
    fun prefs(c: Context): SharedPreferences = c.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun setServiceRunning(c: Context, running: Boolean) {
        prefs(c).edit().putBoolean("svc_running", running).putLong("svc_last_change", System.currentTimeMillis()).apply()
    }
    fun isServiceRunning(c: Context): Boolean = prefs(c).getBoolean("svc_running", false)

    fun setBalance(c: Context, sol: Double, usdPrice: Double = 152.0) {
        prefs(c).edit().putString("bal_sol", "%.4f".format(sol)).putString("bal_usd", "%.2f".format(sol*usdPrice)).apply()
    }
    fun getBalanceText(c: Context): String {
        val sol = prefs(c).getString("bal_sol", "0.0500")?: "0.0500"
        val usd = prefs(c).getString("bal_usd", "7.60")?: "7.60"
        return "Balance: $sol SOL (\$$usd) | Guard: ON"
    }

    fun setRisk(c: Context, loss: Double = -0.05, mev: Int = 1, sharpe: Double = 1.5) {
        prefs(c).edit().putString("risk", "Loss:${loss} MEV:${mev}% Sharpe>$sharpe").apply()
    }
    fun getRiskText(c: Context): String = prefs(c).getString("risk", "Loss:-0.05 MEV:1% Sharpe>1.5")?: ""

    fun setTop4(c: Context, text: String) { prefs(c).edit().putString("top4", text).apply() }
    fun getTop4(c: Context): String = prefs(c).getString("top4", "Top4: Live | Fee: LOWEST | Scanning Jupiter...")?: ""

    fun appendLog(c: Context, msg: String) {
        val p = prefs(c)
        val old = p.getString("logs", "")?: ""
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val newLogs = (old + "\n[$time] $msg").takeLast(5000) // keep last 5k chars
        p.edit().putString("logs", newLogs).apply()
    }
    fun getLogs(c: Context): String = prefs(c).getString("logs", "Logs: Risk Protected...\nV4.1 Ready")?: ""

    fun setTradeStats(c: Context, open: Int, closed: Int, win: Int) {
        prefs(c).edit().putInt("open", open).putInt("closed", closed).putInt("win", win).apply()
    }
}
