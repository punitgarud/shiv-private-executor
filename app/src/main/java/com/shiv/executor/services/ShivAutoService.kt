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

    private val runnable = object : Runnable {
        override fun run() {
            try {
                balance += 0.0001
                StatusManager.setBalance(this@ShivAutoService, balance)
                // Simulate trade every 30s
                if ((System.currentTimeMillis()/1000)%30==0L) {
                    StatusManager.incTrades(this@ShivAutoService)
                    StatusManager.addPnl(this@ShivAutoService, 0.0001)
                    StatusManager.appendLog(this@ShivAutoService, "Auto trade +0.0001 SOL | ${StatusManager.getModules(this@ShivAutoService)}")
                }
                updateNotif()
                handler.postDelayed(this, 5000)
            } catch(_:Exception){}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        StatusManager.setRunning(this, true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP") {
            handler.removeCallbacks(runnable)
            StatusManager.setRunning(this, false)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }
        val notif = buildNotif()
        startForeground(1, notif)
        StatusManager.setRunning(this, true)
        StatusManager.appendLog(this, "Service STARTED 24/7")
        handler.removeCallbacks(runnable)
        handler.post(runnable)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        StatusManager.setRunning(this, false)
        super.onDestroy()
    }

    private fun createChannel() {
        val ch = NotificationChannel("shiv_persistent", "SHIV LIVE", NotificationManager.IMPORTANCE_LOW).apply {
            description = "24/7 executor - amoled"
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(ch)
    }

    private fun updateNotif() {
        try {
            getSystemService(NotificationManager::class.java)?.notify(1, buildNotif())
        } catch(_:Exception){}
    }

    private fun buildNotif(): Notification {
        val mods = StatusManager.getModules(this)
        val trades = StatusManager.getTrades(this)
        val pnl = StatusManager.getPnl(this)
        val bal = StatusManager.getBalance(this)

        val mainIntent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stopIntent = Intent(this, ShivAutoService::class.java).apply { action="STOP" }
        val stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val title = "SHIV LIVE"
        val text = "Mods:$mods | Trades:$trades | PnL:${"%.4f".format(pnl)} | Bal:${"%.4f".format(bal)}"

        return Notification.Builder(this, "shiv_persistent")
           .setContentTitle(title)
           .setContentText(text)
           .setSmallIcon(android.R.drawable.presence_away)
           .setOngoing(true)
           .setOnlyAlertOnce(true)
           .setContentIntent(pending)
           .addAction(Notification.Action.Builder(null, "STOP", stopPending).build())
           .setStyle(Notification.BigTextStyle().bigText("$text\nTap to open dashboard\nLoss limit -0.05 | 24/7"))
           .build()
    }
}
