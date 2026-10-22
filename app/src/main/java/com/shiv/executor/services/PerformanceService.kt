package com.shiv.executor.services
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
data class TopModule(val id:String, val name:String, val score:Double, val mint:String, val signal:String, val entryPrice:Double=0.0, val currentPrice:Double=0.0, val priceChangePct:Double=0.0, val sharpe:Double=0.0, val winRate:Double=0.0, val maxDrawdown:Double=0.0, val winStreak:Int=0)
object PerformanceService {
    private val client = OkHttpClient()
    var lastSolPrice = 152.0
    private val urls = listOf("https://punitgarud.github.io/Shiv/data.json","https://punitgarud.github.io/Shiv/api/top4.json")
    suspend fun fetchTop4(): List<TopModule> = withContext(Dispatchers.IO){
        for(url in urls){
            try{
                val body = client.newCall(Request.Builder().url(url).build()).execute().body?.string() ?: continue
                val j = JSONObject(body)
                if(j.has("solPriceUsd")) lastSolPrice = j.getDouble("solPriceUsd")
                val arr = j.getJSONArray("top4")
                val list = mutableListOf<TopModule>()
                for(i in 0 until arr.length()){
                    val o = arr.getJSONObject(i)
                    list.add(TopModule(o.getString("id"),o.getString("name"),o.getDouble("score"),o.optString("mint",JupiterService.WSOL),o.getString("signal"),o.optDouble("entryPrice",0.0),o.optDouble("currentPrice",0.0),o.optDouble("priceChangePct",0.0),o.optDouble("sharpe",0.0),o.optDouble("winRate",0.0),o.optDouble("maxDrawdown",0.0),o.optInt("winStreak",0)))
                }
                return@withContext list.sortedByDescending{it.score}.take(4)
            }catch(e:Exception){ Log.e("SHIV","$url ${e.message}") }
        }
        emptyList()
    }
}
