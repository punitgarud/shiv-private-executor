package com.shiv.executor

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object HotWalletManager {
    private var isInitialized: Boolean = false
    private var activePublicKey: String = "So11111111111111111111111111111111111111112"
    private var liveBalance: Double = 1.0
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

    suspend fun fetchOnChainBalance(): Double = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.mainnet-beta.solana.com")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 4000
            conn.readTimeout = 4000

            val payload = """
                {
                    "jsonrpc": "2.0",
                    "id": 1,
                    "method": "getBalance",
                    "params": ["$activePublicKey"]
                }
            """.trimIndent()

            conn.outputStream.write(payload.toByteArray())
            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val result = json.optJSONObject("result")
                if (result != null) {
                    val lamports = result.optLong("value", 0L)
                    liveBalance = lamports / 1_000_000_000.0
                    StatusManager.setBalance(liveBalance)
                    return@withContext liveBalance
                }
            }
        } catch (e: Exception) {
            // Fallback to current cached balance if offline/transient
        }
        return@withContext StatusManager.getBalance()
    }

    fun deposit(amount: Double): Boolean {
        liveBalance = StatusManager.getBalance() + amount
        StatusManager.setBalance(liveBalance)
        StatusManager.appendLog("DEPOSIT: Added +$amount SOL. New Balance: $liveBalance SOL")
        return true
    }

    fun withdraw(amount: Double): Boolean {
        val current = StatusManager.getBalance()
        if (current < amount) {
            StatusManager.appendLog("WITHDRAW FAIL: Insufficient balance ($current SOL)")
            return false
        }
        liveBalance = current - amount
        StatusManager.setBalance(liveBalance)
        StatusManager.appendLog("WITHDRAW: Sent -$amount SOL. Remaining: $liveBalance SOL")
        return true
    }
}
