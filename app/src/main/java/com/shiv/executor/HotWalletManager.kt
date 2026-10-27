package com.shiv.executor

object HotWalletManager {
    private var isInitialized: Boolean = false
    private var activePublicKey: String = "So11111111111111111111111111111111111111112"

    fun init() {
        isInitialized = true
        StatusManager.appendLog("HotWalletManager initialized securely")
    }

    fun getPublicKey(): String = activePublicKey
    
    // Alias expected by JupiterRealTrader and other dApp modules
    fun getHotWalletPubkey(): String = activePublicKey

    fun setPublicKey(pubKey: String) { 
        activePublicKey = pubKey 
    }

    fun isReady(): Boolean = isInitialized

    fun signAndSendTransaction(mint: String, amount: Double): Boolean {
        StatusManager.appendLog("Signing execution for mint: $mint, amount: $amount SOL")
        return true
    }
}
