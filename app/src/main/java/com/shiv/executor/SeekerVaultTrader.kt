package com.shiv.executor
object SeekerVaultTrader {
    suspend fun executeSwapWithVault(activity: android.app.Activity, mint: String, amountLamports: Long, slippageBps: Int, isBuy: Boolean): String {
        return JupiterRealTrader.executeTrade(activity, mint, amountLamports, isBuy)
    }
}
