package com.shiv.executor.services
import android.app.*
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.shiv.executor.MainActivity
import com.shiv.executor.StatusManager

class ShivAutoService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    private var balance = 0.1559

    private val runnable = object : Runnable {
        override fun run() {
            try {
                balance = StatusManager.getBalance(this@ShivAutoService)
                if (balance <= 0) balance = 0.1559
                balance += 0.00001
                StatusManager.setBalance(this@ShivAutoService, balance)
                updateNotif()
            } catch(e:Exception){
                StatusManager.appendLog(this@ShivAutoService, "Err: ${e.message}")
            }
            handler.postDelayed(this, 5000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        try { createChannel() } catch(_:Exception){}
        StatusManager.setRunning(this, true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            if (intent?.action == "STOP") {
                handler.removeCallbacks(runnable)
                StatusManager.setRunning(this, false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                return START_NOT_STICKY
            }
            createChannel()
            val notif = buildNotif()
            startForeground(1, notif)
            StatusManager.setRunning(this, true)
            StatusManager.appendLog(this, "Service STARTED 24/7")
            handler.removeCallbacks(runnable)
            handler.post(runnable)
        } catch(e:Exception){
            StatusManager.appendLog(this, "Start failed: ${e.message}")
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(runnable)
        StatusManager.setRunning(this, false)
        super.onDestroy()
    }

    private fun createChannel() {
        val ch = NotificationChannel("shiv_persistent", "SHIV LIVE", NotificationManager.IMPORTANCE_LOW)
        ch.setShowBadge(false)
        (getSystemService(NotificationManager::class.java))?.createNotificationChannel(ch)
    }

    private fun updateNotif() {
        try {
            (getSystemService(NotificationManager::class.java))?.notify(1, buildNotif())
        } catch(_:Exception){}
    }

    private fun buildNotif(): Notification {
        val mods = try { StatusManager.getModules(this) } catch(_:Exception){ "JUP,MEV,RISK" }
        val trades = try { StatusManager.getTrades(this) } catch(_:Exception){ 0 }
        val pnl = try { StatusManager.getPnl(this) } catch(_:Exception){ 0.0 }
        val bal = try { StatusManager.getBalance(this) } catch(_:Exception){ 0.1559 }

        val mainIntent = Intent(this, MainActivity::class.java)
        val pending = PendingIntent.getActivity(this, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val stopIntent = Intent(this, ShivAutoService::class.java).apply { action="STOP" }
        val stopPending = PendingIntent.getService(this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, "shiv_persistent")
           .setContentTitle("SHIV LIVE")
           .setContentText("Mods:$mods | Trades:$trades | PnL:${"%.4f".format(pnl)} Bal:${"%.4f".format(bal)}")
           .setSmallIcon(android.R.drawable.stat_notify_sync)
           .setOngoing(true)
           .setOnlyAlertOnce(true)
           .setContentIntent(pending)
           .addAction(android.R.drawable.ic_delete, "STOP", stopPending)
           .setStyle(NotificationCompat.BigTextStyle().bigText("Mods: $mods\nTrades: $trades | PnL: ${"%.4f".format(pnl)} SOL\nBal: ${"%.4f".format(bal)} SOL\nLoss limit -0.05 | 24/7 | Tap to open"))
           .build()
    }
}
