package com.shiv.executor.services
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
data class ModulePerf(val id:String, val name:String, val score:Double, val signal:String, val mint:String, val entryPrice:Double=0.0, val currentPrice:Double=0.0, val priceChangePct:Double=0.0, val sharpe:Double=0.0, val winRate:Double=0.0, val maxDrawdown:Double=0.0, val winStreak:Int=0)
object PerformanceService {
    private val client = OkHttpClient()
    private const val DATA_URL = "https://punitgarud.github.io/Shiv/data.json"
    var lastSolPrice: Double = 152.0
    suspend fun fetchTop4(): List<ModulePerf> = withContext(Dispatchers.IO){
        try{
            val req = Request.Builder().url(DATA_URL).build()
            val body = client.newCall(req).execute().body?.string() ?: return@withContext emptyList()
            val root = JSONObject(body)
            lastSolPrice = root.optDouble("solPriceUsd", 152.0)
            val arr = root.optJSONArray("top4") ?: return@withContext emptyList()
            val list = mutableListOf<ModulePerf>()
            for(i in 0 until arr.length()){
                val o = arr.getJSONObject(i)
                list.add(ModulePerf(o.getString("id"), o.getString("name"), o.getDouble("score"), o.getString("signal"), o.getString("mint"), o.optDouble("entryPrice",0.0), o.optDouble("currentPrice",0.0), o.optDouble("priceChangePct",0.0), o.optDouble("sharpe",0.0), o.optDouble("winRate",0.0), o.optDouble("maxDrawdown",0.0), o.optInt("winStreak",0)))
            }
            list.sortedByDescending{it.score}.take(4)
        }catch(e:Exception){ Log.e("SHIV","fetchTop4",e); emptyList() }
    }
}