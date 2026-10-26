package com.shiv.executor.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.shiv.executor.PerformanceService
import com.shiv.executor.StatusManager
import kotlinx.coroutines.*

class ShivAutoService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val CHANNEL_ID = "ShivServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("SHIV v5 BLACK Active")
            .setContentText("Scanning 40 analysis sites & monitoring data.json")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        com.shiv.executor.StatusManager.setRunning(true)
        com.shiv.executor.StatusManager.appendLog("Service Started")
        serviceScope.launch {
            while (isActive) {
                try {
                    val topTokens = PerformanceService.fetchTop4()
                    for (token in topTokens) {
                        if (token.rugScore < 60) {
                            StatusManager.appendLog("SKIP ${token.id}: rugScore ${token.rugScore} < 60")
                            continue
                        }
                        if (token.unlockDays < 7) {
                            StatusManager.appendLog("SKIP ${token.id}: unlockDays ${token.unlockDays} < 7")
                            continue
                        }
                        if (!PerformanceService.canTrade()) {
                            StatusManager.appendLog("PAUSE: Daily loss limit reached (>0.05 SOL)")
                            break
                        }

                        StatusManager.appendLog("EXEC [${token.id}] score=${token.score} rug=${token.rugScore} solPrice=${PerformanceService.lastSolPrice}")
                        PerformanceService.recordTrade(true)
                        StatusManager.appendLog("WIN +0.00 SOL (${token.id})")
                    }
                } catch (e: Exception) {
                    StatusManager.appendLog("Sync Error: ${e.message}")
                }
                delay(60000)
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "SHIV Foreground Service Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        StatusManager.setRunning(false)
        StatusManager.appendLog("Service Stopped")
    }
}
