package com.shiv.executor
data class Signal(val token: String, val amount: Double, val confidence: Int, val signal: String = "BUY")
data class Position(val pnlPercent: Double)
class MobileWalletAdapter { fun signAndSend(t: String): String = t }

object RiskManager {
    const val MAX_DAILY_LOSS_SOL = 0.3; const val MAX_POSITION_PCT = 30; const val MAX_SLIPPAGE_PCT = 8
    const val MIN_CONFIDENCE = 85; const val COOLDOWN_AFTER_LOSS_MIN = 60; const val MAX_TRADES_PER_HOUR = 6
    var dailyLoss = 0.0; var tradesThisHour = 0; var lastLossTime = 0L
    fun canTrade(signal: Signal, balance: Double): Boolean {
        if (signal.confidence < MIN_CONFIDENCE) return false
        if (dailyLoss >= MAX_DAILY_LOSS_SOL) return false
        if (tradesThisHour >= MAX_TRADES_PER_HOUR) return false
        if (System.currentTimeMillis() - lastLossTime < COOLDOWN_AFTER_LOSS_MIN * 60 * 1000) return false
        if (signal.amount > balance * MAX_POSITION_PCT / 100) return false
        return true
    }
}