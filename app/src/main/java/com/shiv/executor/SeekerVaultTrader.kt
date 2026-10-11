package com.shiv.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeekerVaultTrader {
    suspend fun executeSwapWithVault(activity: android.app.Activity, mint: String, amountLamports: Long, slippageBps: Int, isBuy: Boolean): String = withContext(Dispatchers.IO) {
        try {
            // Delegate to proven JupiterRealTrader which already handles MobileWalletAdapter correctly
            // Seeker Vault = same Jupiter quote + swap but signed via Seeker secure element path in future
            JupiterRealTrader.executeRealSwap(activity, mint, amountLamports, isBuy)
        } catch (e: Exception) { "SEEKER VAULT ERROR: ${e.message}" }
    }
}
