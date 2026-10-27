package com.shiv.executor

object HotWalletManager {
    private var isInitialized: Boolean = false
    private var activePublicKey: String = "So11111111111111111111111111111111111111112"

    fun init() {
        isInitialized = true
        StatusManager.appendLog("HotWalletManager initialized")
    }

    fun getPublicKey(): String {
        return activePublicKey
    }

    fun setPublicKey(pubKey: String) {
        activePublicKey = pubKey
    }

    fun isReady(): Boolean {
        return isInitialized
    }

    fun signAndSendTransaction(mint: String, amount: Double): Boolean {
        StatusManager.appendLog("Signing transaction for mint: $mint, amount: $amount SOL")
        // Debug simulation mode
        return true
    }
}
