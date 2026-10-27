package com.shiv.executor

object RiskEngine {
    fun evaluateTokenRisk(token: TopToken): Boolean {
        // Enforce strict checks aligned with live web analysis modules
        if (token.rugScore < 70) {
            StatusManager.appendLog("REJECT [${token.id}]: RugScore ${token.rugScore} < 70 threshold")
            return false
        }
        if (token.unlockDays < 14) {
            StatusManager.appendLog("REJECT [${token.id}]: UnlockDays ${token.unlockDays} < 14 days")
            return false
        }
        if (token.sharpe < 1.2) {
            StatusManager.appendLog("REJECT [${token.id}]: Sharpe Ratio ${token.sharpe} < 1.2")
            return false
        }
        return true
    }
}
