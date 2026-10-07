package com.shiv.executor
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(50,100,50,50) }
        val status = TextView(this).apply { text = "Shiv Executor - Idle\nBalance: ${ShivBackgroundService.getBalance()} SOL\nDaily Loss: ${RiskManager.dailyLoss}"; textSize = 18f }
        val btn = Button(this).apply { text = "Start Background Service" }
        btn.setOnClickListener {
            startForegroundService(Intent(this, ShivBackgroundService::class.java))
            status.text = "Service Started!"
        }
        layout.addView(status); layout.addView(btn)
        setContentView(layout)
    }
}