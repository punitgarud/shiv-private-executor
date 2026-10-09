package com.shiv.executor.services
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
object JupiterService {
    private val client = OkHttpClient()
    private const val API = "https://quote-api.jup.ag/v6"
    const val WSOL = "So11111111111111111111111111111111111111112"
    const val USDC = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
    var lastImpact: Double = 0.0
    var lastOutAmount: Long = 0
    suspend fun getBestQuote(amountLamports: Long, slippageBps: Int) = getBestInternal(WSOL, USDC, amountLamports, slippageBps)
    suspend fun getBestQuoteReverse(amountLamports: Long, slippageBps: Int) = getBestInternal(USDC, WSOL, amountLamports, slippageBps)
    private suspend fun getBestInternal(input:String, output:String, amount:Long, slippage:Int): JSONObject? = withContext(Dispatchers.IO) {
        val urls = listOf(
            "$API/quote?inputMint=$input&outputMint=$output&amount=$amount&slippageBps=$slippage&onlyDirectRoutes=false",
            "$API/quote?inputMint=$input&outputMint=$output&amount=$amount&slippageBps=$slippage&onlyDirectRoutes=true",
            "$API/quote?inputMint=$input&outputMint=$output&amount=$amount&slippageBps=${slippage+150}&onlyDirectRoutes=false"
        )
        var best: JSONObject? = null; var bestScore = Double.MAX_VALUE
        for (url in urls) {
            try {
                val resp = client.newCall(Request.Builder().url(url).build()).execute()
                val body = resp.body?.string() ?: continue
                if(!resp.isSuccessful) continue
                val j = JSONObject(body)
                val out = j.optString("outAmount").toLongOrNull() ?: 0L
                val impact = j.optString("priceImpactPct").toDoubleOrNull() ?: 100.0
                val score = impact*10000 - out/1e6
                if(score<bestScore && out>0){ bestScore=score; best=j; lastImpact=impact; lastOutAmount=out }
            } catch(_:Exception){}
        }
        Log.i("SHIV","Best impact=${lastImpact}% out=$lastOutAmount $input->$output")
        best
    }
}