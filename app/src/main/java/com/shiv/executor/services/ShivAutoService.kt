package com.shiv.executor.services
import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.shiv.executor.MainActivity
import com.shiv.executor.services.db.PositionEntity
import com.shiv.executor.services.db.ShivDatabase
import com.shiv.executor.services.db.TradeHistoryEntity
import kotlinx.coroutines.*

class ShivAutoService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var running = false
    private val amountSOL = 0.1
    private val slippagePct = 8
    private val takeProfitPct = 15.0
    private val stopLossPct = 8.0
    private val dailyLossLimitSOL = 0.05
    private val mevImpactLimit = 1.0
    private val lowBalanceThreshold = 0.05
    private val refillAmount = 0.5
    private var lastLowBalanceNotify = 0L
    private lateinit var db: ShivDatabase

    override fun onCreate() { 
        super.onCreate()
        db = ShivDatabase.getDatabase(this)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("shiv_auto","SHIV Auto",NotificationManager.IMPORTANCE_LOW))
        nm.createNotificationChannel(NotificationChannel("shiv_alert","SHIV Alerts",NotificationManager.IMPORTANCE_HIGH))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(running) return START_STICKY; running=true
        val pendingMain = PendingIntent.getActivity(this,0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val notif = Notification.Builder(this,"shiv_auto").setContentTitle("SHIV V4 RISK PROTECTED").setContentText("Top4+LowFee+LossGuard+MEV+BalGuard | 0.1/8%/0.02").setSmallIcon(android.R.drawable.ic_media_play).setContentIntent(pendingMain).build()
        startForeground(1, notif)
        scope.launch {
            while(isActive) {
                try {
                    if(!HotWalletManager.hasHotWallet(this@ShivAutoService)){ delay(15000); continue }
                    val bal = HotWalletManager.getBalanceSOL(this@ShivAutoService)
                    if(bal in 0.001..lowBalanceThreshold && System.currentTimeMillis()-lastLowBalanceNotify > 600000){
                        notifyAlert("LOW BALANCE: ${"%.3f".format(bal)} SOL","Tap to refill $refillAmount SOL from Seed Vault. Trading paused.", true); lastLowBalanceNotify = System.currentTimeMillis()
                    }
                    if (shouldPauseForLoss()) { notifyAlert("SHIV PAUSED - LOSS LIMIT","Daily loss -$dailyLossLimitSOL SOL hit. Paused 1h.", false); delay(3600000); continue }
                    if(bal in 0.001..lowBalanceThreshold){ delay(30000); continue }
                    val top4 = PerformanceService.fetchTop4()
                    val openPositions = db.positionDao().getAll().associateBy { it.moduleId }
                    for(m in top4){
                        if(m.signal=="BUY" && (m.sharpe < 1.5 || m.winRate < 65 || m.maxDrawdown < -10.0)) continue
                        when(m.signal.uppercase()){
                            "BUY" -> if(m.score>=85 && !openPositions.containsKey(m.id)){
                                val q = JupiterService.getBestQuote((amountSOL*1e9).toLong(), slippagePct*100)
                                if(q==null || JupiterService.lastImpact > mevImpactLimit) continue
                                db.positionDao().insert(PositionEntity(moduleId=m.id, moduleName=m.name, entryScore=m.score, amountSOL=amountSOL, entryPrice=m.entryPrice, mint=m.mint, timestamp=System.currentTimeMillis()))
                                db.positionDao().insertHistory(TradeHistoryEntity(moduleId=m.id, moduleName=m.name, type="BUY", action="BUY", amountSOL=amountSOL, amountSol=amountSOL, scoreAtTrade=m.score, timestamp=System.currentTimeMillis()))
                            }
                            "SELL" -> {
                                val pos = openPositions[m.id] ?: continue
                                val pnl = (m.score - pos.entryScore)/pos.entryScore*100
                                if(m.score<50 || pnl>=takeProfitPct || pnl<=-stopLossPct){
                                    val q = JupiterService.getBestQuoteReverse((amountSOL*1e9).toLong(), slippagePct*100)
                                    if(q==null || JupiterService.lastImpact > mevImpactLimit) continue
                                    db.positionDao().delete(m.id)
                                    db.positionDao().insertHistory(TradeHistoryEntity(moduleId=m.id, moduleName=m.name, type="SELL", action="SELL", amountSOL=amountSOL, amountSol=amountSOL, scoreAtTrade=m.score, pnlPct=pnl, pnl=pnl, timestamp=System.currentTimeMillis()))
                                }
                            }
                        }
                    }
                } catch(e:Exception){ Log.e("SHIV","risk",e) }
                delay(15000)
            }
        }
        return START_STICKY
    }

    private suspend fun shouldPauseForLoss(): Boolean {
        val history = db.positionDao().getHistory()
        val todayLossSOL = history.filter{ it.timestamp > System.currentTimeMillis()-86400000 && it.type=="SELL" && it.pnlPct<0 }.sumOf{ it.pnlPct/100 * it.amountSOL }
        return todayLossSOL <= -dailyLossLimitSOL
    }

    private fun notifyAlert(title:String, text:String, withRefillAction:Boolean){
        val nm = getSystemService(NotificationManager::class.java)
        val builder = Notification.Builder(this,"shiv_alert").setContentTitle(title).setContentText(text).setSmallIcon(android.R.drawable.stat_notify_error).setStyle(Notification.BigTextStyle().bigText(text))
        if(withRefillAction){
            val refillIntent = Intent(this, MainActivity::class.java).apply { putExtra("action","refill"); putExtra("amount",refillAmount) }
            val pending = PendingIntent.getActivity(this,1, refillIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            builder.addAction(Notification.Action.Builder(null,"Refill $refillAmount SOL", pending).build())
        }
        nm.notify(2, builder.build())
    }
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() { scope.cancel(); super.onDestroy() }
}