package com.shiv.executor
import android.app.*
import android.content.Intent
import android.os.IBinder
class ShivBackgroundService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val ch = NotificationChannel("shiv", "Shiv", NotificationManager.IMPORTANCE_LOW)
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(ch)
            val notif = Notification.Builder(this, "shiv").setContentTitle("Shiv Running").setSmallIcon(android.R.drawable.ic_dialog_info).build()
            startForeground(1, notif)
        } catch (_: Exception) {}
        return START_STICKY
    }
    companion object {
        val openPositions = mutableListOf(Position(0.0))
        fun monitorPosition(s: Signal, e: Double) {}
        fun fetch(url: String): String = "{}"
        fun getBalance(): Double = 10.0
        fun executeBuy(s: Signal) {}
        fun executeSell(p: Position) {}
    }
}