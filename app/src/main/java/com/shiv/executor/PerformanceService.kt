package com.shiv.executor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

data class TopToken(
    val id: String,
    val name: String,
    val score: Int,
    val mint: String,
    val signal: String,
    val sharpe: Double,
    val winRate: Int,
    val maxDrawdown: Double,
    val rugScore: Int,
    val unlockDays: Int
)

object PerformanceService {
    private const val DATA_URL = "https://punitgarud.github.io/Shiv/data.json"

    suspend fun fetchTop4(): List<TopToken> = withContext(Dispatchers.IO) {
        try {
            val response = URL(DATA_URL).readText()
            val json = JSONObject(response)
            StatusManager.setLastSolPrice(json.optDouble("solPriceUsd", 150.0))
            val top4Array = json.getJSONArray("top4")
            val list = mutableListOf<TopToken>()
            for (i in 0 until top4Array.length()) {
                val item = top4Array.getJSONObject(i)
                list.add(
                    TopToken(
                        id = item.optString("id", "JUP"),
                        name = item.optString("name", "Jupiter Scan"),
                        score = item.optInt("score", 90),
                        mint = item.optString("mint", "So11111111111111111111111111111111111111112"),
                        signal = item.optString("signal", "BUY"),
                        sharpe = item.optDouble("sharpe", 2.0),
                        winRate = item.optInt("winRate", 75),
                        maxDrawdown = item.optDouble("maxDrawdown", -2.5),
                        rugScore = item.optInt("rugScore", 80),
                        unlockDays = item.optInt("unlockDays", 30)
                    )
                )
            }
            list
        } catch (e: Exception) {
            StatusManager.appendLog("Fetch Error: ${e.message}")
            fallback()
        }
    }

    fun fallback(): List<TopToken> {
        return listOf(
            TopToken("JUP", "Jupiter Scan", 92, "So11111111111111111111111111111111111111112", "BUY", 2.1, 78, -2.3, 85, 30)
        )
    }

    fun recordTrade(success: Boolean) {
        if (!success) {
            StatusManager.addDailyLoss(0.01)
        }
        StatusManager.setTrades(StatusManager.getTrades() + 1)
        StatusManager.setPnl(StatusManager.getPnl() + if (success) 0.02 else -0.01)
    }

    fun canTrade(): Boolean {
        val maxAllowedLoss = StatusManager.getBalance() * 0.03 // Dynamic 3% Max Drawdown Circuit Breaker
        return StatusManager.getDailyLoss() < maxAllowedLoss
    }
}
