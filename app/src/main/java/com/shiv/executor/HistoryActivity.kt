package com.shiv.executor
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.shiv.executor.services.PerformanceService
import com.shiv.executor.services.db.ShivDatabase
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
class HistoryActivity : AppCompatActivity() {
    private val scope = CoroutineScope(Dispatchers.Main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scroll = ScrollView(this).apply { setBackgroundColor(0xFF000000.toInt()) }
        val layout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(30,60,30,30) }
        scroll.addView(layout)
        val title = TextView(this).apply { text="SHIV V4 HISTORY & RISK\n"; setTextColor(Color.WHITE); textSize=18f }; layout.addView(title)
        val btnExport = Button(this).apply { text="Export CSV (Tax Report)"; setBackgroundColor(0xFF121212.toInt()); setTextColor(Color.WHITE) }; layout.addView(btnExport)
        val stats = TextView(this).apply { setTextColor(Color.GRAY); textSize=12f; setPadding(0,20,0,20) }; layout.addView(stats)
        val list = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }; layout.addView(list)
        scope.launch {
import com.shiv.executor.services.db.ShivDatabase
            val history = withContext(Dispatchers.IO){ db.positionDao().getAllHistory() }
            val open = withContext(Dispatchers.IO){ db.positionDao().getAll() }
            val sells = history.filter{it.type=="SELL"}; val wins = sells.count{it.pnlPct>0}; val losses = sells.count{it.pnlPct<=0}
            val totalPnlPct = sells.sumOf{it.pnlPct}; val totalPnlUsd = sells.sumOf{ it.pnlPct/100 * it.amountSOL * PerformanceService.lastSolPrice }
            val winRate = if((wins+losses)>0) wins*100.0/(wins+losses) else 0.0
            stats.text = "SOL $${"%.2f".format(PerformanceService.lastSolPrice)} | LossLimit:-0.05 MEV:1%\nOpen:${open.size} Closed:${wins+losses} Win:$wins Loss:$losses\nWinRate:${"%.1f".format(winRate)}% Total:${"%.2f".format(totalPnlPct)}% ($${"%.2f".format(totalPnlUsd)})\nBuffer:0.02 Fee:LOWEST Sharpe:>1.5\n\n--- OPEN ---\n" + open.joinToString("\n"){ "• ${it.moduleName} entry=${"%.1f".format(it.entryScore)} $${it.entryPrice}" }
            btnExport.setOnClickListener {
                scope.launch {
                    val csv = withContext(Dispatchers.IO){
                        val sb = StringBuilder("type,module,moduleId,amountSOL,scoreAtTrade,pnlPct,pnlUsd,solPrice,timestamp,txSig\n")
                        history.forEach{
                            val pnlUsd = it.pnlPct/100*it.amountSOL*PerformanceService.lastSolPrice
                            sb.append("${it.type},${it.moduleName},${it.moduleId},${it.amountSOL},${it.scoreAtTrade},${it.pnlPct},${pnlUsd},${PerformanceService.lastSolPrice},${SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(it.timestamp))},${it.txSig}\n")
                        }; sb.toString()
                    }
                    val file = java.io.File(getExternalFilesDir(null),"shiv_trades_${System.currentTimeMillis()}.csv")
                    file.writeText(csv); Toast.makeText(this@HistoryActivity,"Exported: ${file.name}", Toast.LENGTH_LONG).show()
                    val uri = FileProvider.getUriForFile(this@HistoryActivity, "${packageName}.provider", file)
                    val share = Intent(Intent.ACTION_SEND).apply { type="text/csv"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
                    startActivity(Intent.createChooser(share,"Share Tax Report"))
                }
            }
            for(h in history.take(100)){
                val color = if(h.type=="BUY") 0xFF00FF88.toInt() else if(h.pnlPct>0) 0xFF00FF88.toInt() else 0xFFFF3344.toInt()
                val usd = h.pnlPct/100*h.amountSOL*PerformanceService.lastSolPrice
                val tv = TextView(this@HistoryActivity).apply { text="${h.type} ${h.moduleName} | ${"%.2f".format(h.pnlPct)}% ($${"%.2f".format(usd)}) | ${SimpleDateFormat("MM-dd HH:mm").format(Date(h.timestamp))}"; setTextColor(color); textSize=11f; setPadding(0,6,0,6) }
                list.addView(tv)
            }
        }
        setContentView(scroll)
    }
}