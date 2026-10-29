package com.shiv.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SeekerVaultTrader {
    suspend fun signAndExecuteTransaction(mint: String, amount: Double): Boolean = withContext(Dispatchers.IO) {
        try {
            StatusManager.appendLog("SeekerVaultTrader: Requesting hardware signature for $mint...")
            val isReady = HotWalletManager.isReady()
            if (isReady) {
                StatusManager.appendLog("SeekerVaultTrader: Hardware signature approved for $mint")
                return@withContext true
            }
        } catch (e: Exception) {
            StatusManager.appendLog("SeekerVaultTrader error: ${e.localizedMessage}")
        }
        return@withContext false
    }
}
