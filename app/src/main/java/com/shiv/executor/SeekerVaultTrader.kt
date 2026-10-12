package com.shiv.executor
object SeekerVaultTrader {
    suspend fun executeSwapWithVault(activity: android.app.Activity, mint: String, amountLamports: Long, slippageBps: Int, isBuy: Boolean): String {
        val inputMint = if (isBuy) "So11111111111111111111111111111111111111112" else mint
        val outputMint = if (isBuy) mint else "So11111111111111111111111111111111111111112"
        val amountSol = amountLamports / 1_000_000_000.0
        val ok = JupiterRealTrader.executeReal(activity, inputMint, outputMint, amountSol, slippageBps)
        return if (ok) "TX_SENT_MWA" else "FAILED"
    }
}
