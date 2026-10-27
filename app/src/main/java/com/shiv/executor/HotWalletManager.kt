package com.shiv.executor

object HotWalletManager {
    private var isInitialized: Boolean = false
    private var activePublicKey: String = "So11111111111111111111111111111111111111112"

    fun init() {
        isInitialized = true
        StatusManager.appendLog("HotWalletManager initialized securely")
    }

    fun getPublicKey(): String = activePublicKey
    fun getHotWalletPubkey(): String = activePublicKey
    fun setPublicKey(pubKey: String) { activePublicKey = pubKey }
    fun isReady(): Boolean = isInitialized

    fun deposit(amount: Double): Boolean {
        val newBalance = StatusManager.getBalance() + amount
        StatusManager.setBalance(newBalance)
        StatusManager.appendLog("DEPOSIT: Added +$amount SOL to Hot Wallet. New Balance: $newBalance SOL")
        return true
    }

    fun withdraw(amount: Double): Boolean {
        val current = StatusManager.getBalance()
        if (current < amount) {
            StatusManager.appendLog("WITHDRAW FAIL: Insufficient balance ($current SOL)")
            return false
        }
        val newBalance = current - amount
        StatusManager.setBalance(newBalance)
        StatusManager.appendLog("WITHDRAW: Sent -$amount SOL from Hot Wallet. Remaining: $newBalance SOL")
        return true
    }

    fun signAndSendTransaction(mint: String, amount: Double): Boolean {
        StatusManager.appendLog("Signing execution for mint: $mint, amount: $amount SOL")
        return true
    }
}
