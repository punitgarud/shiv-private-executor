package com.shiv.executor.services

import android.content.Context
import com.shiv.executor.StatusManager

object PerformanceService {

    fun recordTrade(context: Context, pnlSol: Double, isWin: Boolean) {
        val currentTrades = StatusManager.getTrades(context)
        StatusManager.setTrades(context, currentTrades + 1)
        
        val currentPnl = StatusManager.getPnl(context)
        StatusManager.setPnl(context, currentPnl + pnlSol)

        if (pnlSol < 0) {
            StatusManager.addDailyLoss(context, kotlin.math.abs(pnlSol))
        }

        val msg = if (isWin) "WIN +${"%.4f".format(pnlSol)} SOL" else "LOSS ${"%.4f".format(pnlSol)} SOL"
        StatusManager.appendLog(context, msg)
    }

    fun canTrade(context: Context): Boolean {
        val dailyLoss = StatusManager.getDailyLoss(context)
        // stop if daily loss > 0.05 SOL
        if (dailyLoss >= 0.05) return false
        return true
    }

    fun resetDailyIfNeeded(context: Context) {
        // you can add date check here
        // for now manual reset
    }

    fun getWrappedSolMint(): String {
        return JupiterService.WSOL_MINT
    }
}
