package com.shiv.executor

object JupiterRealTrader {
    // BEST DEFAULTS - LOCKED
    var maxDailyLoss = 0.1 // SOL
    var maxPerTradeLossPercent = 8.0 // %
    var maxPerTradeLossSOL = 0.02 // fallback
    var dailyLossSOL = 0.0
    var isTradingEnabled = true
    var defaultSlippageBps = 150
    var defaultPriorityFee = 20000L

    fun resetDaily() {
        dailyLossSOL = 0.0
        isTradingEnabled = true
    }
}