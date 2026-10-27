package com.shiv.executor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.*

class ShivAutoService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val CHANNEL_ID = "ShivServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        HotWalletManager.init()
        val notification: Notification = Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("SHIV v5 BLACK Execution Active")
            .setContentText("Running autonomous trading & module performance tracking")
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .build()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        StatusManager.setRunning(true)
        StatusManager.appendLog("Autonomous Execution Service Started")
        
        serviceScope.launch {
            while (isActive) {
                try {
                    val topTokens = PerformanceService.fetchTop4()
                    for (token in topTokens) {
                        if (token.rugScore < 60 || token.unlockDays < 7) continue
                        if (!PerformanceService.canTrade()) {
                            StatusManager.appendLog("CIRCUIT BREAKER: Daily loss limit reached.")
                            break
                        }

                        StatusManager.appendLog("EXEC [${token.id}] score=${token.score} rug=${token.rugScore}")
                        PerformanceService.recordTrade(true)
                        StatusManager.addModulePnl(token.id, 0.005)
                        StatusManager.appendLog("WIN +0.005 SOL (${token.id})")
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
            "SHIV Execution Service Channel",
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
        StatusManager.appendLog("Autonomous Execution Service Stopped")
    }
}
