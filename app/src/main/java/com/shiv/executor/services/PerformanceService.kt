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
        return StatusManager.getDailyLoss(context) < 0.05
    }
}
