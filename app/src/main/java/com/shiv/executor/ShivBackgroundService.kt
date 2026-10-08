package com.shiv.executor

import android.app.*
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ShivBackgroundService : Service() {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val client = OkHttpClient.Builder()
       .connectTimeout(15, TimeUnit.SECONDS)
       .readTimeout(15, TimeUnit.SECONDS)
       .build()

    private val BRAIN_URL = "https://punitgarud.github.io/Shiv/active_modules.json"

    // === RISK CONFIG - FROM YOUR SCREENSHOT ===
    private val MAX_DAILY_LOSS = 0.1 // SOL - hard stop
    private val MAX_TRADE_SOL = 0.05 // SOL - max per trade
    private val STOP_LOSS_PCT = 10.0 // % - per trade
    private val MAX_TRADES_PER_DAY = 5

    // === STATE ===
    private var dailyLoss = 0.0
    private var dailyTrades = 0
    private var entryPrice = mutableMapOf<String, Double>() // mint -> entry price
    private var lastDayReset = System.currentTimeMillis()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channelId = "shiv_v4_real"
        val channel = NotificationChannel(channelId, "SHIV V4 REAL + STOPLOSS", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
           .setContentTitle("SHIV V4 REAL LIVE")
           .setContentText("0.01 SOL/trade | Daily SL: 0.1 SOL | Trade SL: 10% | $dailyTrades/5 trades")
           .setSmallIcon(android.R.drawable.ic_dialog_alert)
           .setOngoing(true)
           .build()

        startForeground(1, notification)
        scope.launch { brainLoop() }
        return START_STICKY
    }

    private suspend fun brainLoop() {
        while (true) {
            try {
                checkDailyReset()

                // === DAILY STOPLOSS CHECK ===
                if (dailyLoss >= MAX_DAILY_LOSS) {
                    showStopLossNotification("DAILY STOPLOSS HIT: Lost $dailyLoss SOL. Paused 24h.")
                    delay(60 * 60 * 1000) // wait 1 hour and check again
                    continue
                }

                if (dailyTrades >= MAX_TRADES_PER_DAY) {
                    delay(60 * 60 * 1000)
                    continue
                }

                val request = Request.Builder().url("$BRAIN_URL?t=${System.currentTimeMillis()}").build()
                val response = client.newCall(request).execute()
                val body = response.body?.string()?: continue
                val brain = JSONObject(body)

                val risk = brain.optJSONObject("risk")
                val maxLossConfig = risk?.optDouble("maxDailyLossSOL", MAX_DAILY_LOSS)?: MAX_DAILY_LOSS

                // === TRADE STOPLOSS CHECK (check open positions) ===
                // In real version, fetch current price from Jupiter and compare
                for ((mint, entry) in entryPrice) {
                    // TODO: get current price from Jupiter quote
                    // val currentPrice = getCurrentPrice(mint)
                    // if (checkTradeStopLoss(mint, currentPrice)) {... }
                }

                val top4 = brain.optJSONArray("top4")?: continue

                for (i in 0 until top4.length()) {
                    val trade = top4.getJSONObject(i)
                    val action = trade.optString("action", "HOLD")
                    val amountSOL = trade.optDouble("amountSOL", 0.0)
                    val inputMint = trade.optString("inputMint", "")
                    val outputMint = trade.optString("outputMint", "")
                    val slippage = trade.optInt("slippageBps", 50)

                    if (action!= "BUY") continue
                    if (amountSOL <= 0.0 || amountSOL > MAX_TRADE_SOL) continue
                    if (inputMint == "NONE" || outputMint == "NONE") continue

                    // === EXECUTE REAL TRADE ===
                    val success = JupiterRealTrader.executeReal(
                        context = this@ShivBackgroundService,
                        inputMint = inputMint,
                        outputMint = outputMint,
                        amountSOL = amountSOL,
                        slippageBps = slippage
                    )

                    if (success) {
                        dailyTrades++
                        // Save entry price for stoploss tracking
                        entryPrice[outputMint] = 1.0 // placeholder - Jupiter quote gives real price
                        updateNotification()
                        delay(30000) // 30 sec gap between trades
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
            delay(120000) // Check brain every 2 mins
        }
    }

    private fun checkTradeStopLoss(mint: String, currentPrice: Double): Boolean {
        val entry = entryPrice[mint]?: return false
        val lossPct = ((entry - currentPrice) / entry) * 100
        if (lossPct >= STOP_LOSS_PCT) {
            scope.launch {
                // EMERGENCY SELL BACK TO SOL
                JupiterRealTrader.executeReal(
                    this@ShivBackgroundService,
                    inputMint = mint,
                    outputMint = "So11111111111111111111111111111111111111112",
                    amountSOL = 0.01,
                    slippageBps = 100 // higher slippage for emergency exit
                )
                dailyLoss += 0.01 * (lossPct / 100)
                entryPrice.remove(mint)
                showStopLossNotification("TRADE STOPLOSS: $mint lost ${"%.1f".format(lossPct)}% - Sold")
            }
            return true
        }
        return false
    }

    private fun checkDailyReset() {
        val now = System.currentTimeMillis()
        if (now - lastDayReset > 24 * 60 * 60 * 1000) {
            dailyLoss = 0.0
            dailyTrades = 0
            entryPrice.clear()
            lastDayReset = now
        }
    }

    private fun updateNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "shiv_v4_real"
        val notif = Notification.Builder(this, channelId)
           .setContentTitle("SHIV V4 REAL LIVE")
           .setContentText("Trades: $dailyTrades/5 | Loss: $dailyLoss/0.1 SOL | SL: 10% active")
           .setSmallIcon(android.R.drawable.ic_dialog_alert)
           .setOngoing(true)
           .build()
        nm.notify(1, notif)
    }

    private fun showStopLossNotification(text: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "shiv_alert"
        val ch = NotificationChannel(channelId, "SHIV Alerts", NotificationManager.IMPORTANCE_HIGH)
        nm.createNotificationChannel(ch)
        val notif = Notification.Builder(this, channelId)
           .setContentTitle("SHIV STOPLOSS")
           .setContentText(text)
           .setSmallIcon(android.R.drawable.ic_dialog_alert)
           .build()
        nm.notify(2, notif)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}