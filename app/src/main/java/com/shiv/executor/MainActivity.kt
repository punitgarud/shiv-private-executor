package com.shiv.executor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 100, 50, 50)
        }

        val title = TextView(this).apply {
            text = "SHIV V4 REAL\nSTOPLOSS: 10% | Daily: 0.1 SOL\n0.01 SOL/trade"
            textSize = 18f
        }

        val startBtn = Button(this).apply {
            text = "START REAL BOT"
            setOnClickListener {
                val serviceIntent = Intent(this@MainActivity, ShivBackgroundService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
                title.text = "BOT RUNNING...\nCheck notification\nTrading 0.01 SOL with 10% SL"
            }
        }

        val stopBtn = Button(this).apply {
            text = "STOP BOT"
            setOnClickListener {
                stopService(Intent(this@MainActivity, ShivBackgroundService::class.java))
                title.text = "BOT STOPPED"
            }
        }

        layout.addView(title)
        layout.addView(startBtn)
        layout.addView(stopBtn)

        setContentView(layout)
    }
}