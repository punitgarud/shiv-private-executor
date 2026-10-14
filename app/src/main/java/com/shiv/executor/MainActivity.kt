package com.shiv.executor

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.shiv.executor.services.ShivAutoService

class MainActivity : AppCompatActivity() {
    private lateinit var tvStatus: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val refresh = object : Runnable {
        override fun run() {
            try {
                val running = StatusManager.isServiceRunning(this@MainActivity)
                tvStatus.text = if(running) "● RUNNING - V4.3.2 Locked" else "○ IDLE - Tap START"
                tvStatus.setTextColor(if(running) Color.parseColor("#00FF88") else Color.GRAY)
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
        tvStatus = TextView(this).apply { textSize=14f; setTextColor(Color.WHITE) }
        val btnPerm = Button(this).apply { text="ENABLE NOTIFICATIONS"; setBackgroundColor(Color.parseColor("#FF8800")) }
        val btnStart = Button(this).apply { text="START EXECUTOR" }
        val btnStop = Button(this).apply { text="STOP" }

        btnPerm.setOnClickListener {
            try {
                val i = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(i)
            } catch(e:Exception) {
                val i2 = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(i2)
            }
        }
        btnStart.setOnClickListener {
            val i = Intent(this, ShivAutoService::class.java)
            ContextCompat.startForegroundService(this, i)
            Toast.makeText(this, "Starting V4.3.2", Toast.LENGTH_SHORT).show()
        }
        btnStop.setOnClickListener {
            val i = Intent(this, ShivAutoService::class.java).apply { action="STOP" }
            startService(i)
        }

        lay.addView(tvStatus)
        lay.addView(btnPerm)
        lay.addView(btnStart)
        lay.addView(btnStop)
        setContentView(lay)
    }
    override fun onResume(){ super.onResume(); handler.post(refresh) }
    override fun onPause(){ super.onPause(); handler.removeCallbacks(refresh) }
}
