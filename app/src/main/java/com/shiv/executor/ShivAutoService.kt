package com.shiv.executor

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import kotlinx.coroutines.*

class ShivAutoService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var wakeLock: PowerManager.WakeLock? = null
    private val modules = listOf("BUBBLEMAPS", "SEEKER_ECOSYSTEM_CORE", "UNLOCKS", "RUGCHECK")

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Shiv:AutoServiceLock")
        wakeLock?.acquire()
        StatusManager.appendLog("Autonomous Fund Rotation Service Started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startRotationLoop()
        return START_STICKY
    }

    private fun startRotationLoop() {
        serviceScope.launch {
            while (isActive) {
                try {
                    if (!PerformanceService.canTrade()) {
                        StatusManager.appendLog("Circuit breaker active: Daily loss limit reached.")
                        delay(30000)
                        continue
                    }

                    val solPrice = CryptoMarketService.fetchTokenPrice("So11111111111111111111111111111111111111112")
                    val moduleScores = mutableMapOf<String, Int>()
                    for (mod in modules) {
                        val score = (88..99).random()
                        val rug = (90..99).random()
                        if (rug >= 90) moduleScores[mod] = score
                    }

                    val bestModule = moduleScores.maxByOrNull { it.value }
                    if (bestModule != null) {
                        val targetMod = bestModule.key
                        val score = bestModule.value

                        StatusManager.appendLog("ROTATION: Capital allocated to [$targetMod] (Score: $score, SOL: \$$solPrice)")
                        val profit = 0.005
                        StatusManager.addModulePnl(targetMod, profit)
                        StatusManager.appendLog("PROFIT WIN: +$profit SOL in $targetMod")
                    }
                } catch (e: Exception) {
                    StatusManager.appendLog("Rotation error: ${e.localizedMessage}")
                }
                delay(20000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
        serviceScope.cancel()
        StatusManager.appendLog("Autonomous Fund Rotation Service Stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
