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
    private const val HELIUS_KEY = "f63bce9d-ab3b-48fa-a562-d8181b566d7d"
    private val RPC_URL = "https://mainnet.helius-rpc.com/?api-key=$HELIUS_KEY"
    private val client = OkHttpClient()

    suspend fun executeSwapWithVault(activity: android.app.Activity, mint: String, amountLamports: Long, slippageBps: Int, isBuy: Boolean): String = withContext(Dispatchers.IO) {
        try {
            val inputMint = if(isBuy) "So11111111111111111111111111111111111111112" else mint
            val outputMint = if(isBuy) mint else "So11111111111111111111111111111111111111112"
            val quoteUrl = "https://quote-api.jup.ag/v6/quote?inputMint=$inputMint&outputMint=$outputMint&amount=$amountLamports&slippageBps=$slippageBps"
            val quoteReq = Request.Builder().url(quoteUrl).build()
            val quoteResStr = client.newCall(quoteReq).execute().body?.string()?: return@withContext "Quote failed"
            val quoteJson = JSONObject(quoteResStr)

            val swapJson = JSONObject().apply {
                put("quoteResponse", quoteJson)
                put("userPublicKey", "11111111111111111111111111111111")
                put("wrapAndUnwrapSol", true)
                put("dynamicComputeUnitLimit", true)
                put("prioritizationFeeLamports", JupiterRealTrader.defaultPriorityFee)
            }
            val swapReq = Request.Builder().url("https://quote-api.jup.ag/v6/swap")
              .post(swapJson.toString().toRequestBody("application/json".toMediaType())).build()
            val swapRespStr = client.newCall(swapReq).execute().body?.string()?: return@withContext "Swap API failed"
            val swapTxBase64 = JSONObject(swapRespStr).getString("swapTransaction")
            val swapTxBytes = Base64.decode(swapTxBase64, Base64.DEFAULT)

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
                  .post(sendJson.toString().toRequestBody("application/json".toMediaType())).build()
                val sendResp = client.newCall(sendReq).execute().body?.string()?: ""
                txSignature = JSONObject(sendResp).optString("result", sendResp)
            }
            "SUCCESS: $txSignature | ${JupiterRealTrader.dailyLossSOL}/${JupiterRealTrader.maxDailyLoss} SOL loss"
        } catch (e: Exception) { "TRADE ERROR: ${e.message}" }
    }
}
