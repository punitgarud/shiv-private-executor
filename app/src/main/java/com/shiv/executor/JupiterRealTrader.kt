package com.shiv.executor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
object JupiterRealTrader {
    var maxDailyLoss = 0.1
    var maxPerTradeLossPercent = 8.0
    var maxPerTradeLossSOL = 0.02
    var dailyLossSOL = 0.0
    var isTradingEnabled = true
    var defaultSlippageBps = 150
    var defaultPriorityFee = 20000L
    fun resetDaily() { dailyLossSOL = 0.0; isTradingEnabled = true }
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
            val amountLamports = (amountSOL * 1_000_000_000).toLong()
            val quoteUrl = "https://quote-api.jup.ag/v6/quote?inputMint=$inputMint&outputMint=$outputMint&amount=$amountLamports&slippageBps=$slippageBps"
            val quoteReq = Request.Builder().url(quoteUrl).build()
            val quoteRes = client.newCall(quoteReq).execute()
            val quoteBody = quoteRes.body?.string()?: return@withContext false
            if (quoteBody.contains("error")) { android.util.Log.e("SHIV", "Quote err: $quoteBody"); return@withContext false }
            val quoteJson = JSONObject(quoteBody)
            val pubkey = com.shiv.executor.services.HotWalletManager.getHotWalletPubkey(context)?: ""
            if (pubkey.isEmpty()) { android.util.Log.e("SHIV","No hot_pubkey"); return@withContext false }
            val swapBody = JSONObject()
            swapBody.put("quoteResponse", quoteJson)
            swapBody.put("userPublicKey", pubkey)
            swapBody.put("wrapAndUnwrapSol", true)
            val request = Request.Builder().url("https://quote-api.jup.ag/v6/swap").post(swapBody.toString().toRequestBody(JSON_MEDIA)).build()
            val swapRes = client.newCall(request).execute()
            val swapResult = swapRes.body?.string()?: return@withContext false
            val swapJson = JSONObject(swapResult)
            val txBase64 = swapJson.optString("swapTransaction", "")
            if (txBase64.isEmpty()) { android.util.Log.e("SHIV","No swapTransaction: $swapResult"); return@withContext false }
            withContext(Dispatchers.Main) {
                val intent = android.content.Intent().apply {
                    action = "com.solana.mobilewalletadapter.action.SIGN_AND_SEND"
                    putExtra("transaction", txBase64)
                }
                try { context.startActivity(intent) } catch (e: Exception) { android.util.Log.e("SHIV","MWA intent fail", e) }
            }
            true
        } catch (e: Exception) { android.util.Log.e("SHIV","executeReal crash", e); false }
    }
}
