package com.shiv.executor

import android.util.Base64
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object SeekerVaultTrader {
    // ===== PASTE YOUR HELIUS KEY HERE =====
    private const val HELIUS_KEY = "f63bce9d-ab3b-48fa-a562-d8181b566d7d"
    private val RPC_URL = "https://mainnet.helius-rpc.com/?api-key=$HELIUS_KEY"
    // ======================================

    var userPublicKey: String? = null
    var solBalance: Double = 0.0

    suspend fun connectToSeedVault(activity: android.app.Activity): String {
        return withContext(Dispatchers.IO) {
            try {
                val walletAdapter = MobileWalletAdapter()
                var resultPubkey = ""
                walletAdapter.transact(activity) { authResult ->
                    resultPubkey = authResult.accounts.firstOrNull()?.publicKey?.toString()?: ""
                    userPublicKey = resultPubkey
                    getSolBalance(resultPubkey)
                }
                resultPubkey
            } catch (e: Exception) { "ERROR: ${e.message}" }
        }
    }

    private fun getSolBalance(pubkey: String): Double {
        try {
            val client = OkHttpClient()
            val json = JSONObject().apply {
                put("jsonrpc", "2.0"); put("id", 1)
                put("method", "getBalance")
                put("params", org.json.JSONArray().put(pubkey))
            }
            val req = Request.Builder().url(RPC_URL)
            val resp = client.newCall(req).execute().body?.string()?: return 0.0
            val lamports = JSONObject(resp).getJSONObject("result").getLong("value")
            solBalance = lamports / 1_000_000_000.0
            return solBalance
        } catch (e: Exception) { return 0.0 }
    }

    suspend fun executeRealTrade(activity: android.app.Activity, amountLamports: String, slippageBps: Int = 150): String {
        return withContext(Dispatchers.IO) {
            try {
                if (userPublicKey == null) return@withContext "Connect Seeker first"
                if (JupiterRealTrader.dailyLossSOL >= JupiterRealTrader.maxDailyLoss) {
                    JupiterRealTrader.isTradingEnabled = false
                    return@withContext "DAILY STOPLOSS ${JupiterRealTrader.maxDailyLoss} SOL HIT"
                }

                val client = OkHttpClient()

                // 1. Quote with best slippage default 150 BPS
                val quoteUrl = "https://quote-api.jup.ag/v6/quote?inputMint=So11111111111111111111111111111111111111112&outputMint=EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v&amount=$amountLamports&slippageBps=$slippageBps"
                val quoteResp = client.newCall(Request.Builder().url(quoteUrl).build()).execute().body?.string()?: return@withContext "Quote failed"

                // 2. Swap tx with best priority fee 20000
                val swapJson = JSONObject().apply {
                    put("quoteResponse", JSONObject(quoteResp))
                    put("userPublicKey", userPublicKey)
                    put("wrapAndUnwrapSol", true)
                    put("dynamicComputeUnitLimit", true)
                    put("prioritizationFeeLamports", JupiterRealTrader.defaultPriorityFee)
                }
                val swapReq = Request.Builder().url("https://quote-api.jup.ag/v6/swap")
                val swapRespStr = client.newCall(swapReq).execute().body?.string()?: return@withContext "Swap API failed"
                val swapTxBase64 = JSONObject(swapRespStr).getString("swapTransaction")
                val swapTxBytes = Base64.decode(swapTxBase64, Base64.DEFAULT)

                // 3. Sign via Seed Vault + Send via Helius Mumbai edge
                val walletAdapter = MobileWalletAdapter()
                var txSignature = ""
                walletAdapter.transact(activity) { authResult ->
                    val signed = authResult.signTransactions(arrayOf(swapTxBytes))
                    val signedTxBase64 = Base64.encodeToString(signed.signedPayloads[0], Base64.NO_WRAP)
                    val sendJson = JSONObject().apply {
                        put("jsonrpc", "2.0"); put("id", 1)
                        put("method", "sendTransaction")
                        put("params", org.json.JSONArray().put(signedTxBase64).put(JSONObject().put("encoding","base64")))
                    }
                    val sendReq = Request.Builder().url(RPC_URL)
                    val sendResp = client.newCall(sendReq).execute().body?.string()?: ""
                    txSignature = JSONObject(sendResp).optString("result", sendResp)
                }
                "SUCCESS: $txSignature | ${JupiterRealTrader.dailyLossSOL}/${JupiterRealTrader.maxDailyLoss} SOL loss"
            } catch (e: Exception) { "TRADE ERROR: ${e.message}" }
        }
    }
}