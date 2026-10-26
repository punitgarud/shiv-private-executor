package com.shiv.executor

object StatusManager {
    private var balance: Double = 1.0
    private var dailyLoss: Double = 0.0
    private var trades: Int = 0
    private var pnl: Double = 0.0
    private var isRunning: Boolean = false
    private val logs = mutableListOf<String>()
    private var modules = mutableMapOf("JUP" to true, "FEE" to true, "RISK" to true, "SHARPE" to true)

    @Synchronized fun getBalance(): Double = balance
    @Synchronized fun setBalance(b: Double) { balance = b }

    @Synchronized fun getLogs(): List<String> = logs.toList()
    @Synchronized fun appendLog(log: String) {
        if (logs.size > 100) logs.removeAt(0)
        logs.add(log)
    }

    @Synchronized fun getModules(): Map<String, Boolean> = modules.toMap()
    @Synchronized fun setModules(m: Map<String, Boolean>) { modules.clear(); modules.putAll(m) }

    @Synchronized fun getDailyLoss(): Double = dailyLoss
    @Synchronized fun addDailyLoss(amount: Double) { dailyLoss += amount }
    @Synchronized fun setDailyLoss(loss: Double) { dailyLoss = loss }

    @Synchronized fun setTrades(t: Int) { trades = t }
    @Synchronized fun setPnl(p: Double) { pnl = p }
    @Synchronized fun setRunning(r: Boolean) { isRunning = r }
}
