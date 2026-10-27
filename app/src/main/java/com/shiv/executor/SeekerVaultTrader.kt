package com.shiv.executor

object SeekerVaultTrader {
    suspend fun executeReal(mint: String, amount: Double): Boolean {
        StatusManager.appendLog("SeekerVaultTrader: Executing real trade on Seeker ecosystem for mint $mint")
        return executeSwap(mint, amount)
    }

    suspend fun executeSwap(mint: String, amount: Double): Boolean {
        if (!HotWalletManager.isReady()) {
            StatusManager.appendLog("SeekerVaultTrader Error: HotWalletManager not ready")
            return false
        }
        val pubKey = HotWalletManager.getHotWalletPubkey()
        StatusManager.appendLog("Executing Seeker Vault swap for mint: $mint | Wallet: $pubKey")
        
        val success = HotWalletManager.signAndSendTransaction(mint, amount)
        if (success) {
            PerformanceService.recordTrade(true)
            StatusManager.appendLog("SUCCESS: Seeker Vault swapped \$${amount}SOL for $mint")
        } else {
            PerformanceService.recordTrade(false)
            StatusManager.appendLog("FAIL: Seeker Vault swap rejected for $mint")
        }
        return success
    }
}
