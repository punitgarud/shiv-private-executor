package com.shiv.executor.services
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object JupiterService {
    private val client = OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS).readTimeout(15,TimeUnit.SECONDS).build()
    const val JUPITER_QUOTE_API = "https://quote-api.jup.ag/v6"
    const val WSOL_MINT = "So11111111111111111111111111111111111111112"
    const val USDC_MINT = "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"
    var lastImpact:Double = 0.0
    data class FeeOption(val quote:JSONObject, val outAmount:Long, val priceImpact:Double, val score:Double)
    suspend fun getBestLowestFeeQuote(inputMint:String, outputMint:String, amountLamports:Long, slippageBps:Int): JSONObject? = withContext(Dispatchers.IO){
        try{
            val urls = listOf(
                "$JUPITER_QUOTE_API/quote?inputMint=$inputMint&outputMint=$outputMint&amount=$amountLamports&slippageBps=$slippageBps&onlyDirectRoutes=false&asLegacyTransaction=false&platformFeeBps=0",
                "$JUPITER_QUOTE_API/quote?inputMint=$inputMint&outputMint=$outputMint&amount=$amountLamports&slippageBps=$slippageBps&onlyDirectRoutes=true&asLegacyTransaction=false&platformFeeBps=0",
                "$JUPITER_QUOTE_API/quote?inputMint=$inputMint&outputMint=$outputMint&amount=$amountLamports&slippageBps=${slippageBps+150}&onlyDirectRoutes=false&asLegacyTransaction=false&platformFeeBps=0"
            )
            val opts = mutableListOf<FeeOption>()
            for(url in urls){
                try{
                    val req = Request.Builder().url(url).get().build()
                    val resp = client.newCall(req).execute()
                    val body = resp.body?.string() ?: continue
                    if(!resp.isSuccessful) continue
                    val j = JSONObject(body)
                    val outAmount = j.optString("outAmount").toLongOrNull() ?: 0L
                    val priceImpact = j.optString("priceImpactPct").toDoubleOrNull() ?: 100.0
                    val score = priceImpact*10000 - outAmount.toDouble()/1_000_000
                    if(outAmount>0) opts.add(FeeOption(j,outAmount,priceImpact,score))
                    Log.i("SHIV","Route out=$outAmount impact=$priceImpact% score=$score")
                }catch(_:Exception){}
            }
            if(opts.isEmpty()) return@withContext null
            val best = opts.minByOrNull{it.score}!!
            lastImpact = best.priceImpact
            Log.i("SHIV","BEST FEE out=${best.outAmount} impact=${best.priceImpact}")
            best.quote
        }catch(e:Exception){ Log.e("SHIV","Lowest fee failed ${e.message}"); null }
    }
    suspend fun getSwapTransaction(quote:JSONObject, userPubkey:String): String? = withContext(Dispatchers.IO){
        try{
            val bodyJson = JSONObject().put("quoteResponse",quote).put("userPublicKey",userPubkey).put("wrapAndUnwrapSol",true).put("dynamicComputeUnitLimit",true).put("prioritizationFeeLamports",mapOf("autoMultiplier" to 1))
            val req = Request.Builder().url("$JUPITER_QUOTE_API/swap").post(bodyJson.toString().toRequestBody("application/json".toMediaType())).build()
            val resp = client.newCall(req).execute()
            val body = resp.body?.string() ?: return@withContext null
            JSONObject(body).optString("swapTransaction")
        }catch(_:Exception){ null }
    }
}
