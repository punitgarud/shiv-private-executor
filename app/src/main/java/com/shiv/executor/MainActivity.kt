package com.shiv.executor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shiv.executor.services.ShivAutoService

class MainActivity : AppCompatActivity() {
    private lateinit var tvStatus: TextView
    private lateinit var tvBal: TextView
    private lateinit var tvTop4: TextView
    private lateinit var tvLog: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            try {
                val running = StatusManager.isServiceRunning(this@MainActivity)
                tvStatus.text = "Status: ${if(running) "● RUNNING - Persistent Locked" else "○ IDLE"} | ${if(running) "Notification Active" else "Tap START"}"
                tvStatus.setTextColor(if(running) Color.parseColor("#00FF88") else Color.GRAY)
                tvBal.text = StatusManager.getBalanceText(this@MainActivity)
                tvTop4.text = StatusManager.getTop4(this@MainActivity)
                tvLog.text = StatusManager.getLogs(this@MainActivity).takeLast(2000)
            } catch(_:Exception){}
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val lay = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40,100,40,40)
            setBackgroundColor(Color.BLACK)
        }
        tvStatus = TextView(this).apply { textSize = 14f; setTextColor(Color.WHITE); setPadding(0,0,0,20) }
        tvBal = TextView(this).apply { setTextColor(Color.GREEN); textSize = 15f; setPadding(0,0,0,10) }
        tvTop4 = TextView(this).apply { setTextColor(Color.CYAN); setPadding(0,0,0,20) }
        tvLog = TextView(this).apply { setTextColor(Color.LTGRAY); textSize = 12f }
        val btnStart = Button(this).apply { text = "START EXECUTOR (PERSISTENT)" }
        val btnStop = Button(this).apply { text = "STOP" }

        btnStart.setOnClickListener {
            try {
                val i = Intent(this, ShivAutoService::class.java)
                ContextCompat.startForegroundService(this, i)
                StatusManager.setServiceRunning(this, true)
                StatusManager.appendLog(this, "MainActivity: Start clicked - persistent")
                Toast.makeText(this, "V4.3 Persistent Started - check notification panel", Toast.LENGTH_LONG).show()
            } catch(e:Exception){ Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show() }
        }
        btnStop.setOnClickListener {
            val i = Intent(this, ShivAutoService::class.java).apply { action = "STOP" }
            startService(i)
        }

        lay.addView(tvStatus)
        lay.addView(tvBal)
        lay.addView(tvTop4)
        lay.addView(btnStart)
        lay.addView(btnStop)
        lay.addView(tvLog)
        setContentView(lay)
    }

    override fun onResume() { super.onResume(); handler.post(refresh) }
    override fun onPause() { super.onPause(); handler.removeCallbacks(refresh) }
}
