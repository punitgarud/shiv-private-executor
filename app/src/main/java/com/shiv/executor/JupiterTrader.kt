package com.shiv.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

object JupiterRealTrader {

    private val client = OkHttpClient()
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    suspend fun executeReal(
        context: android.content.Context,
        inputMint: String,
        outputMint: String,
        amountSOL: Double,
        slippageBps: Int
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Convert SOL to lamports
            val amountLamports = (amountSOL * 1_000_000_000).toLong()

            // 1. Get Jupiter quote
            val quoteUrl = "https://quote-api.jup.ag/v6/quote?inputMint=$inputMint&outputMint=$outputMint&amount=$amountLamports&slippageBps=$slippageBps"
            val quoteReq = Request.Builder().url(quoteUrl).build()
            val quoteRes = client.newCall(quoteReq).execute()
            val quoteBody = quoteRes.body?.string() ?: return@withContext false
            val quoteJson = JSONObject(quoteBody)

            // 2. Get swap transaction
            val swapBody = JSONObject()
            swapBody.put("quoteResponse", quoteJson)
            swapBody.put("userPublicKey", "YOUR_PUBKEY_HERE") // TODO: put your Seeker wallet pubkey
            swapBody.put("wrapAndUnwrapSol", true)

            val request = Request.Builder()
                .url("https://quote-api.jup.ag/v6/swap")
                .post(swapBody.toString().toRequestBody(JSON_MEDIA))
                .build()

            val swapRes = client.newCall(request).execute()
            val swapResult = swapRes.body?.string() ?: return@withContext false
            val swapJson = JSONObject(swapResult)
            val txBase64 = swapJson.optString("swapTransaction", "")

            if (txBase64.isEmpty()) return@withContext false

            // 3. REAL SIGNING VIA INTENT - No library needed
            // This triggers Seeker Seed Vault
            withContext(Dispatchers.Main) {
                val intent = android.content.Intent().apply {
                    action = "com.solana.mobilewalletadapter.action.SIGN_AND_SEND"
                    putExtra("transaction", txBase64)
                    addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback: log transaction for manual send
                    android.util.Log.d("SHIV_REAL", "TX Ready: $txBase64")
                }
            }
            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}