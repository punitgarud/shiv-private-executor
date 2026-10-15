package com.shiv.executor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shiv.executor.services.ShivAutoService

class MainActivity : AppCompatActivity() {
    private lateinit var tvStatus: TextView
    private lateinit var tvPnl: TextView
    private lateinit var tvLogs: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val modsList = listOf("JUP" to "Jupiter Scan", "MEV" to "MEV Shield 1%", "RISK" to "Risk Guard -0.05", "SHARPE" to "Sharpe>1.5", "FEE" to "Fee Lowest", "AUTO" to "Auto Sell")

    private val refresh = object : Runnable {
        override fun run() {
            try {
                val run = StatusManager.isRunning(this@MainActivity)
                val trades = StatusManager.getTrades(this@MainActivity)
                val pnl = StatusManager.getPnl(this@MainActivity)
                val bal = StatusManager.getBalance(this@MainActivity)
                tvStatus.text = if(run) "● RUNNING 24/7" else "○ IDLE"
                tvPnl.text = "Bal: ${"%.4f".format(bal)} SOL\nPnL: ${"%.4f".format(pnl)} SOL\nTrades: $trades\nMods: ${StatusManager.getModules(this@MainActivity)}"
                tvLogs.text = StatusManager.getLogs(this@MainActivity).ifEmpty { "No trades yet\nExecutor will log here..." }
            } catch(_:Exception){}
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val scroll = ScrollView(this).apply { setBackgroundColor(Color.BLACK) }
        val lay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32,60,32,32)
            setBackgroundColor(Color.BLACK)
        }

        fun tv(s:String, size:Float=13f, col:Int=Color.WHITE): TextView {
            return TextView(this).apply { text=s; textSize=size; setTextColor(col); setPadding(0,10,0,10) }
        }

        tvStatus = tv("○ IDLE", 16f, Color.WHITE)
        tvPnl = tv("Bal: -\nPnL: -\nTrades: 0", 13f, Color.parseColor("#AAAAAA"))
        val titleMods = tv("SELECT MODULES (AMOLED BLACK)", 12f, Color.parseColor("#777777"))

        val boxContainer = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        val checks = mutableListOf<CheckBox>()
        val currentMods = StatusManager.getModules(this).split(",").map{it.trim()}
        modsList.forEach { (code,name) ->
            val cb = CheckBox(this).apply {
                text = "$code - $name"
                isChecked = currentMods.contains(code)
                setTextColor(Color.WHITE)
                buttonTintList = android.content.res.ColorStateList.valueOf(Color.WHITE)
            }
            checks.add(cb)
            boxContainer.addView(cb)
        }

        val btnSaveMods = Button(this).apply {
            text="SAVE MODULES"
            setBackgroundColor(Color.parseColor("#111111"))
            setTextColor(Color.WHITE)
        }
        btnSaveMods.setOnClickListener {
            val sel = checks.filter{it.isChecked}.map{ it.text.toString().split(" -")[0] }.joinToString(",")
            if(sel.isEmpty()){ Toast.makeText(this,"Select at least 1",Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            StatusManager.setModules(this, sel)
            Toast.makeText(this,"Saved: $sel",Toast.LENGTH_SHORT).show()
        }

        val btnPerm = Button(this).apply { text="ENABLE NOTIFICATIONS (for 24/7)"; setBackgroundColor(Color.parseColor("#111111")); setTextColor(Color.WHITE) }
        btnPerm.setOnClickListener {
            val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply { putExtra(Settings.EXTRA_APP_PACKAGE, packageName) }
            try { startActivity(i) } catch(e:Exception){
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply { data = android.net.Uri.parse("package:$packageName") })
            }
        }

        val btnStart = Button(this).apply { text="START 24/7 EXECUTOR"; setBackgroundColor(Color.WHITE); setTextColor(Color.BLACK) }
        btnStart.setOnClickListener {
            val i = Intent(this, ShivAutoService::class.java)
            ContextCompat.startForegroundService(this, i)
            Toast.makeText(this,"Running 24/7 in background",Toast.LENGTH_SHORT).show()
        }

        val btnStop = Button(this).apply { text="STOP"; setBackgroundColor(Color.parseColor("#111111")); setTextColor(Color.WHITE) }
        btnStop.setOnClickListener {
            val i = Intent(this, ShivAutoService::class.java).apply { action="STOP" }
            startService(i)
        }

        tvLogs = tv("Logs...", 11f, Color.parseColor("#666666"))

        lay.addView(tvStatus)
        lay.addView(tvPnl)
        lay.addView(titleMods)
        lay.addView(boxContainer)
        lay.addView(btnSaveMods)
        lay.addView(btnPerm)
        lay.addView(btnStart)
        lay.addView(btnStop)
        lay.addView(tv("LOGS / TRADES",12f,Color.parseColor("#777777")))
        lay.addView(tvLogs)

        scroll.addView(lay)
        setContentView(scroll)
    }

    override fun onResume() { super.onResume(); handler.post(refresh) }
    override fun onPause() { super.onPause(); handler.removeCallbacks(refresh) }
}
