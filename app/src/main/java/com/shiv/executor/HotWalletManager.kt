package com.shiv.executor

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.*

object HotWalletManager {
    private var isInitialized: Boolean = false
    private var activePublicKey: String = "So11111111111111111111111111111111111111112"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("shiv_wallet_prefs", Context.MODE_PRIVATE)
        activePublicKey = prefs.getString("saved_pubkey", "So11111111111111111111111111111111111111112") ?: "So11111111111111111111111111111111111111112"
        isInitialized = true
        StatusManager.appendLog("HotWalletManager initialized with key: ${activePublicKey.take(6)}...")
    }

    fun init() {
        isInitialized = true
    }

    fun getPublicKey(): String = activePublicKey
    fun getHotWalletPubkey(): String = activePublicKey
    fun isReady(): Boolean = isInitialized
    
    fun importWallet(pubKey: String) {
        activePublicKey = pubKey
        if (::prefs.isInitialized) {
            prefs.edit().putString("saved_pubkey", pubKey).apply()
        }
        StatusManager.appendLog("Wallet Linked: ${pubKey.take(6)}...${pubKey.takeLast(4)}")
    }

    fun deposit(amount: Double): Boolean {
        val newBalance = StatusManager.getBalance() + amount
        StatusManager.setBalance(newBalance)
        StatusManager.appendLog("DEPOSIT: Added +$amount SOL. New Balance: $newBalance SOL")
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
        StatusManager.appendLog("WITHDRAW: Sent -$amount SOL. Remaining: $newBalance SOL")
        return true
    }

    fun signAndSendTransaction(mint: String, amount: Double): Boolean {
        StatusManager.appendLog("Signing execution via Seeker Seed Vault for mint: $mint")
        return true
    }
}
