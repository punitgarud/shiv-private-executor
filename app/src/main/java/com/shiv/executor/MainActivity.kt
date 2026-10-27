package com.shiv.executor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var logsText: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateUI()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HotWalletManager.init()
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xFF000000.toInt())
        }

        val title = TextView(this).apply {
            text = "SHIV v5 BLACK"
            textSize = 22f
            setTextColor(0xFF00FF00.toInt())
            setPadding(0, 0, 0, 24)
        }
        layout.addView(title)

        statusText = TextView(this).apply {
            text = "Initializing..."
            textSize = 14f
            setTextColor(0xFFFFFFFF.toInt())
            setPadding(0, 0, 0, 24)
        }
        layout.addView(statusText)

        val startBtn = Button(this).apply {
            text = "Start Executor Service"
            setOnClickListener {
                val intent = Intent(this@MainActivity, ShivAutoService::class.java)
                startForegroundService(intent)
                StatusManager.setRunning(true)
                StatusManager.appendLog("Service Started")
            }
        }
        layout.addView(startBtn)

        logsText = TextView(this).apply {
            text = "Logs:\n"
            textSize = 12f
            setTextColor(0xFF00FF00.toInt())
            setPadding(0, 24, 0, 0)
        }
        layout.addView(logsText)

        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        handler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(updateRunnable)
    }

    private fun updateUI() {
        val balance = StatusManager.getBalance()
        val loss = StatusManager.getDailyLoss()
        statusText.text = "Balance: $balance SOL | Daily Loss: $loss SOL\nCan Trade: ${PerformanceService.canTrade()}"
        val logs = StatusManager.getLogs().takeLast(10).joinToString("\n")
        logsText.text = "Logs:\n$logs"
    }
}
