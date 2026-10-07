package com.shiv.executor
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import java.io.PrintWriter
import java.io.StringWriter

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            val sw = StringWriter(); e.printStackTrace(PrintWriter(sw))
            runOnUiThread { setContentView(TextView(this).apply { text = "CRASH LOG:\n\n${sw.toString()}" }) }
        }
        super.onCreate(savedInstanceState)
        try {
            val tv = TextView(this).apply { 
                textSize = 16f
                setPadding(40,100,40,40)
                text = "Shiv Executor Running!\n\nBalance: ${ShivBackgroundService.getBalance()}\nDaily Loss: ${RiskManager.dailyLoss}\n\nService not started yet."
            }
            val scroll = ScrollView(this).apply { addView(tv) }
            setContentView(scroll)
            
            // Start service safely
            try { startForegroundService(Intent(this, ShivBackgroundService::class.java)) } catch (_: Exception) {}
        } catch (e: Exception) {
            val sw = StringWriter(); e.printStackTrace(PrintWriter(sw))
            setContentView(TextView(this).apply { text = "ERROR:\n${sw.toString()}" })
        }
    }
}