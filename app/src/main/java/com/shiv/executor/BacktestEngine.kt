package com.shiv.executor

object BacktestEngine {
    data class BacktestResult(val expectedWinRate: Double, val simulatedPnl: Double, val riskScore: Int)

    fun runSimulation(tokens: List<TopToken>): BacktestResult {
        var wins = 0
        var totalPnl = 0.0
        for (token in tokens) {
            if (token.rugScore >= 75 && token.sharpe >= 1.5) {
                wins++
                totalPnl += 0.015
            } else {
                totalPnl -= 0.005
            }
        }
        val winRate = if (tokens.isNotEmpty()) (wins.toDouble() / tokens.size.toDouble()) * 100.0 else 0.0
        StatusManager.appendLog("Backtest Simulation complete: WinRate=${winRate}%, PnL=${totalPnl} SOL")
        return BacktestResult(winRate, totalPnl, if (winRate > 70.0) 85 else 50)
    }
}
