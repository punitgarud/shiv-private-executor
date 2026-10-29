package com.shiv.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CryptoMarketService {
    private const val DEXSCREENER_API = "https://api.dexscreener.com/latest/dex/tokens/"

    suspend fun fetchTokenPrice(tokenMint: String): Double = withContext(Dispatchers.IO) {
        try {
            val url = URL("$DEXSCREENER_API$tokenMint")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000

            if (conn.responseCode == 200) {
                val response = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val pairs = json.optJSONArray("pairs")
                if (pairs != null && pairs.length() > 0) {
                    val firstPair = pairs.getJSONObject(0)
                    return@withContext firstPair.optDouble("priceUsd", 0.0)
                }
            }
        } catch (e: Exception) {
            StatusMarketLog("DexScreener fetch error: ${e.localizedMessage}")
        }
        return@withContext 0.0
    }

    private fun StatusMarketLog(msg: String) {
        StatusManager.appendLog(msg)
    }
}
