package com.shiv.executor

import android.net.Uri
import android.util.Base64
import com.solana.mobilewalletadapter.clientlib.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object SeekerVaultTrader {
    var userPublicKey: String? = null
    var solBalance: Double = 0.0

    // Step 1: Connect to Seed Vault and get wallet address
    suspend fun connectToSeedVault(activity: android.app.Activity): String {
        return withContext(Dispatchers.IO) {
            val walletAdapter = MobileWalletAdapter()
            val result = walletAdapter.transact(activity) { authResult ->
                userPublicKey = authResult.accounts.firstOrNull()?.publicKey?.toString()
                // Get balance via RPC
                getSolBalance(userPublicKey!!)
            }
            userPublicKey?: "Failed to connect"
        }
    }

    private fun getSolBalance(pubkey: String): Double {
        try {
            val client = OkHttpClient()
            val json = JSONObject().apply {
                put("jsonrpc", "2.0")
                put("id", 1)
                put("method", "getBalance")
                put("params", org.json.JSONArray().put(pubkey))
            }
            val req = Request.Builder()
               .url("https://api.mainnet-beta.solana.com")
               .post(json.toString().toRequestBody("application/json".toMediaType()))
               .build()
            val resp = client.newCall(req).execute().body?.string()?: ""
            val lamports = JSONObject(resp).getJSONObject("result").getLong("value")
            solBalance = lamports / 1_000_000_000.0
            return solBalance
        } catch (e: Exception) { return 0.0 }
    }

    // Step 2: Build Jupiter Swap TX and ask Seed Vault to sign + send
    suspend fun executeRealTrade(activity: android.app.Activity, amountLamports: String): String {
        return withContext(Dispatchers.IO) {
            try {
                if (userPublicKey == null) return@withContext "Not connected to Seed Vault"
                if (JupiterRealTrader.dailyLossSOL >= 0.1) return@withContext "DAILY STOPLOSS 0.1 SOL HIT - Trading stopped"

                val client = OkHttpClient()

                // 1. Get quote
                val quoteUrl = "https://quote-api.jup.ag/v6/quote?inputMint=So11111111111111111111111111111111111111112&outputMint=EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v&amount=$amountLamports&slippageBps=100"
                val quoteReq = Request.Builder().url(quoteUrl).build()
                val quoteResp = client.newCall(quoteReq).execute().body?.string()?: return@withContext "Quote failed"

                // 2. Get swap transaction (unsigned)
                val swapJson = JSONObject().apply {
                    put("quoteResponse", JSONObject(quoteResp))
                    put("userPublicKey", userPublicKey)
                    put("wrapAndUnwrapSol", true)
                }
                val swapReq = Request.Builder()
                   .url("https://quote-api.jup.ag/v6/swap")
                   .post(swapJson.toString().toRequestBody("application/json".toMediaType()))
                   .build()
                val swapRespStr = client.newCall(swapReq).execute().body?.string()?: return@withContext "Swap API failed"
                val swapTxBase64 = JSONObject(swapRespStr).getString("swapTransaction")
                val swapTxBytes = Base64.decode(swapTxBase64, Base64.DEFAULT)

                // 3. Ask Seed Vault to sign
                val walletAdapter = MobileWalletAdapter()
                walletAdapter.transact(activity) { authResult ->
                    val signed = authResult.signTransactions(arrayOf(swapTxBytes))
                    val signedTxBase64 = Base64.encodeToString(signed.signedPayloads[0], Base64.NO_WRAP)

                    // 4. Send signed tx to RPC
                    val sendJson = JSONObject().apply {
                        put("jsonrpc", "2.0"); put("id", 1)
                        put("method", "sendTransaction")
                        put("params", org.json.JSONArray().put(signedTxBase64).put(JSONObject().put("encoding","base64")))
                    }
                    val sendReq = Request.Builder()
                       .url("https://api.mainnet-beta.solana.com")
                       .post(sendJson.toString().toRequestBody("application/json".toMediaType()))
                       .build()
                    val sendResp = client.newCall(sendReq).execute().body?.string()?: ""
                    sendResp
                }
                "Trade sent! Tx signed by Seeker Seed Vault"
            } catch (e: Exception) {
                "ERROR: ${e.message}"
            }
        }
    }
}