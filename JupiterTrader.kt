package com.shiv.executor

import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object SOL_MINT { const val MINT = "So11111111111111111111111111111111111111112" }

data class Quote(val priceImpact: Double, val transaction: String, val entryPrice: Double)

private val client = OkHttpClient()

suspend fun getJupiterQuote(fromMint: Any, tokenMint: String, amountSol: Double): Quote {
    val lamports = (amountSol * 1_000_000_000).toLong()
    val url = "https://quote-api.jup.ag/v6/quote?inputMint=${SOL_MINT.MINT}&outputMint=$tokenMint&amount=$lamports&slippageBps=300"
    val req = Request.Builder().url(url).build()
    val res = client.newCall(req).execute().body?.string() ?: throw Exception("quote fail")
    val json = JSONObject(res)
    val priceImpact = json.optDouble("priceImpactPct", 0.0)
    return Quote(priceImpact, "", json.optDouble("outAmount", 0.0))
}

class JupiterTrader(private val wallet: MobileWalletAdapter, private val userPubkey: String) {
    suspend fun executeBuy(signal: Signal): Boolean {
        // 1. Get quote
        val quoteUrl = "https://quote-api.jup.ag/v6/quote?inputMint=${SOL_MINT.MINT}&outputMint=${signal.token}&amount=${(signal.amount*1e9).toLong()}&slippageBps=800"
        val quoteJson = JSONObject(client.newCall(Request.Builder().url(quoteUrl).build()).execute().body!!.string())
        if (quoteJson.optDouble("priceImpactPct", 0.0) > RiskManager.MAX_SLIPPAGE_PCT) return false

        // 2. Get swap tx
        val swapReq = Request.Builder().url("https://quote-api.jup.ag/v6/swap")
            .post(okhttp3.RequestBody.create(okhttp3.MediaType.parse("application/json"),
                """{"quoteResponse": $quoteJson, "userPublicKey": "$userPubkey", "wrapAndUnwrapSol": true}"""))
            .build()
        val swapTx = JSONObject(client.newCall(swapReq).execute().body!!.string()).getString("swapTransaction")

        // 3. Sign via Seeker Seed Vault - will popup on your Seeker
        wallet.signAndSend(swapTx)
        
        // 4. Monitor
        ShivBackgroundService.monitorPosition(signal, quoteJson.optDouble("outAmount", 0.0))
        return true
    }
}