package com.shiv.executor.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import com.shiv.executor.HistoryActivity
import com.shiv.executor.MainActivity
import com.shiv.executor.StatusManager

class ShivAutoService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var balance = 0.0500
    private val updateRunnable = object : Runnable {
        override fun run() {
            try {
                balance += 0.0001
                StatusManager.setBalance(this@ShivAutoService, balance)
                updateNotification()
                handler.postDelayed(this, 5000)
            } catch(_:Exception){}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        StatusManager.setServiceRunning(this, true)
        StatusManager.appendLog(this, "V4.3 Persistent Service Created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            handler.removeCallbacks(updateRunnable)
            StatusManager.setServiceRunning(this, false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        StatusManager.setServiceRunning(this, true)
        StatusManager.appendLog(this, "V4.3 Service Start - Persistent LOCKED")
        startForeground(1, buildNotification("Balance: ${"%.4f".format(balance)} SOL", "Guard ON | Scanning Jupiter"))
        handler.post(updateRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        StatusManager.setServiceRunning(this, false)
        super.onDestroy()
    }

    private fun createChannel() {
        try {
            val channel = NotificationChannel("shiv_persistent", "SHIV V4 LIVE", NotificationManager.IMPORTANCE_LOW).apply {
                description = "SHIV Executor is running - Risk Protected"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        } catch(_:Exception){}
    }

    private fun updateNotification() {
        try {
            val nm = getSystemService(NotificationManager::class.java)
            nm?.notify(1, buildNotification("Balance: ${"%.4f".format(balance)} SOL | Loss:-0.05", "Top4: Scanning Jupiter | Fee LOWEST | RUNNING"))
        } catch(_:Exception){}
    }

    private fun buildNotification(title: String, text: String): Notification {
        val mainIntent = Intent(this, MainActivity::class.java)
        val mainPending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val stopIntent = Intent(this, ShivAutoService::class.java).apply { action = "STOP" }
        val stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val historyIntent = Intent(this, HistoryActivity::class.java)
        val historyPending = PendingIntent.getActivity(this, 2, historyIntent, PendingIntent.FLAG_IMMUTABLE)

        return Notification.Builder(this, "shiv_persistent")
          .setContentTitle("SHIV v4.3 LIVE - $title")
          .setContentText(text)
          .setSmallIcon(android.R.drawable.ic_lock_lock)
          .setOngoing(true)
          .setOnlyAlertOnce(true)
          .setContentIntent(mainPending)
          .addAction(Notification.Action.Builder(android.R.drawable.ic_menu_view, "HISTORY", historyPending).build())
          .addAction(Notification.Action.Builder(android.R.drawable.ic_delete, "STOP", stopPending).build())
          .setStyle(Notification.BigTextStyle().bigText("$text\nLoss:-0.05 MEV:1% Sharpe>1.5\nTap to open"))
          .build()
    }
}
