package com.shiv.executor

object StatusManager {
    private var balance: Double = 1.0
    private var dailyLoss: Double = 0.0
    private var trades: Int = 0
    private var pnl: Double = 0.0
    private var lastSolPrice: Double = 150.0
    private var isRunning: Boolean = false
    private val logs = mutableListOf<String>()
    
    // Per-module performance tracking (Module ID -> PnL in SOL)
    private val modulePnlMap = mutableMapOf(
        "BUBBLEMAPS" to 0.03,
        "SEEKER_ECOSYSTEM_CORE" to 0.02,
        "UNLOCKS" to 0.025,
        "RUGCHECK" to 0.015
    )

    @Synchronized fun getBalance(): Double = balance
    @Synchronized fun setBalance(b: Double) { balance = b }

    @Synchronized fun getLogs(): List<String> = logs.toList()
    @Synchronized fun appendLog(log: String) {
        if (logs.size > 100) logs.removeAt(0)
        logs.add(log)
    }
    @Synchronized fun clearLogs() { logs.clear() }

    @Synchronized fun getModulePnl(): Map<String, Double> = modulePnlMap.toMap()
    @Synchronized fun addModulePnl(moduleId: String, amount: Double) {
        val current = modulePnlMap[moduleId] ?: 0.0
        modulePnlMap[moduleId] = current + amount
    }

    @Synchronized fun getDailyLoss(): Double = dailyLoss
    @Synchronized fun addDailyLoss(amount: Double) { dailyLoss += amount }
    @Synchronized fun setDailyLoss(loss: Double) { dailyLoss = loss }

    @Synchronized fun getTrades(): Int = trades
    @Synchronized fun setTrades(t: Int) { trades = t }

    @Synchronized fun getPnl(): Double = pnl
    @Synchronized fun setPnl(p: Double) { pnl = p }

    @Synchronized fun getLastSolPrice(): Double = lastSolPrice
    @Synchronized fun setLastSolPrice(price: Double) { lastSolPrice = price }

    @Synchronized fun isRunning(): Boolean = isRunning
    @Synchronized fun setRunning(r: Boolean) { isRunning = r }
}
