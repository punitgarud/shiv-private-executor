package com.shiv.executor

import okhttp3.*
import java.util.concurrent.TimeUnit

object JupiterRealTrader {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    var dailyLossSOL = 0.0
    var maxDailyLoss = 0.1
    var maxPerTradeLossPercent = 10.0
    var isTradingEnabled = true
    var lastQuote = "No quote yet"

    fun checkStoploss(currentTradeLossSOL: Double, entryPrice: Double, currentPrice: Double): Boolean {
        if (dailyLossSOL >= maxDailyLoss) {
            isTradingEnabled = false
            return true
        }
        val lossPercent = ((entryPrice - currentPrice) / entryPrice) * 100
        if (lossPercent >= maxPerTradeLossPercent) {
            dailyLossSOL += currentTradeLossSOL
            return true
        }
        return false
    }

    fun getJupiterQuote(): String {
        try {
            val url = "https://quote-api.jup.ag/v6/quote?inputMint=So11111111111111111111111111111111111111112&outputMint=EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v&amount=10000000&slippageBps=100"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: "Empty"
            lastQuote = if (body.length > 100) body.substring(0,100) + "..." else body
            return body
        } catch (e: Exception) {
            lastQuote = "ERROR: ${e.message}"
            return lastQuote
        }
    }
}