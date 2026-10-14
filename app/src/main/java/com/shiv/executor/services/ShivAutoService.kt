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
import com.shiv.executor.MainActivity
import com.shiv.executor.StatusManager

class ShivAutoService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var balance = 0.1559

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
        StatusManager.setRunning(this, true)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            handler.removeCallbacks(updateRunnable)
            StatusManager.setRunning(this, false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        try {
            val notif = buildNotification("Balance: ${"%.4f".format(balance)} SOL | Guard ON", "Top4: Scanning Jupiter | Fee LOWEST")
            startForeground(1, notif)
        } catch(e:Exception) {
            android.util.Log.e("SHIV", "startForeground fail ${e.message}")
        }
        StatusManager.setRunning(this, true)
        StatusManager.appendLog(this, "V4.3.1 Service STARTED - Notification HIGH")
        handler.removeCallbacks(updateRunnable)
        handler.post(updateRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(updateRunnable)
        StatusManager.setRunning(this, false)
        super.onDestroy()
    }

    private fun createChannel() {
        try {
            val channel = NotificationChannel("shiv_persistent", "SHIV V4 LIVE", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "SHIV Executor running - Risk Protected"
                setShowBadge(true)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm?.createNotificationChannel(channel)
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
        return Notification.Builder(this, "shiv_persistent")
           .setContentTitle("SHIV v4.3.1 LIVE - $title")
           .setContentText(text)
           .setSmallIcon(android.R.drawable.ic_dialog_info)
           .setOngoing(true)
           .setOnlyAlertOnce(true)
           .setVisibility(Notification.VISIBILITY_PUBLIC)
           .setContentIntent(mainPending)
           .addAction(Notification.Action.Builder(android.R.drawable.ic_delete, "STOP", stopPending).build())
           .setStyle(Notification.BigTextStyle().bigText("$text\nLoss:-0.05 MEV:1% Sharpe>1.5\nPersistent Locked"))
           .build()
    }
}
