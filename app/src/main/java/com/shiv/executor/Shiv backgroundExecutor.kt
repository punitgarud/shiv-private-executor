package com.shiv.executor
import android.app.*
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject
import java.util.*

class ShivBackgroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val client = OkHttpClient()
    private val BRAIN_URL = "https://punitgarud.github.io/Shiv/active_modules.json"
    private var dailyLoss = 0.0
    private var dailyTrades = 0

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val channel = NotificationChannel("shiv", "Shiv V3 REAL", NotificationManager.IMPORTANCE_LOW)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        val notif = Notification.Builder(this, "shiv").setContentTitle("SHIV V3 REAL - 0.01 SOL/trade")
            .setContentText("Risk limit 0.1 SOL/day | Brain connected").setSmallIcon(android.R.drawable.ic_dialog_info).build()
        startForeground(1, notif)
        scope.launch { brainLoop() }
        return START_STICKY
    }

    suspend fun brainLoop() {
        while (true) {
            try {
                val jsonText = client.newCall(Request.Builder().url(BRAIN_URL).build()).execute().body?.string() ?: "{}"
                val brain = JSONObject(jsonText)
                val risk = brain.optJSONObject("risk")
                val maxLoss = risk?.optDouble("maxDailyLossSOL", 0.1) ?: 0.1
                val maxTrade = risk?.optDouble("maxTradeSOL", 0.05) ?: 0.05

                if (dailyLoss >= maxLoss) {
                    println("DAILY LIMIT HIT - STOP TRADING")
                    delay(3600000)
                    continue
                }

                val top4 = brain.optJSONArray("top4")
                if (top4 != null) {
                    for (i in 0 until top4.length()) {
                        val trade = top4.getJSONObject(i)
                        val action = trade.optString("action", "HOLD")
                        val amount = trade.optDouble("amountSOL", 0.0)
                        if (action == "BUY" && amount > 0 && amount <= maxTrade && dailyTrades < 5) {
                            val inputMint = trade.getString("inputMint")
                            val outputMint = trade.getString("outputMint")
                            val slip = trade.optInt("slippageBps", 50)
                            val ok = JupiterRealTrader.executeReal(this@ShivBackgroundService, inputMint, outputMint, amount, slip)
                            if (ok) dailyTrades++
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            delay(120000)
        }
    }
}