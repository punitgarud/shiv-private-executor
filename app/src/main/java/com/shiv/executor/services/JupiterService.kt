package com.shiv.executor.services
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
object JupiterService {
    private val client = OkHttpClient()
    var lastImpact: Double = 0.0
    suspend fun getBestQuote(amountLamports: Long, slippageBps: Int): JSONObject? = withContext(Dispatchers.IO){
        try {
            val url = "https://quote-api.jup.ag/v6/quote?inputMint=So11111111111111111111111111111111111111112&outputMint=EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v&amount=$amountLamports&slippageBps=$slippageBps"
            val req = Request.Builder().url(url).build()
            val res = client.newCall(req).execute().body?.string()?: return@withContext null
            val o = JSONObject(res)
            lastImpact = o.optDouble("priceImpactPct", 0.0)
            o
        } catch(e: Exception){ Log.e("SHIV","quote",e); null }
    }
    suspend fun getBestQuoteReverse(amountLamports: Long, slippageBps: Int): JSONObject? = withContext(Dispatchers.IO){
        try {
            val url = "https://quote-api.jup.ag/v6/quote?inputMint=EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v&outputMint=So11111111111111111111111111111111111111112&amount=$amountLamports&slippageBps=$slippageBps"
            val req = Request.Builder().url(url).build()
            val res = client.newCall(req).execute().body?.string()?: return@withContext null
            val o = JSONObject(res)
            lastImpact = o.optDouble("priceImpactPct", 0.0)
            o
        } catch(e: Exception){ Log.e("SHIV","quoteRev",e); null }
    }
}
