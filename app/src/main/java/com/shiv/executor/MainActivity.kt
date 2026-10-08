package com.shiv.executor

import android.os.Bundle
import android.app.Activity
import android.widget.*
import android.view.Gravity
import android.graphics.Color
import android.content.Intent
import android.os.Build

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var logText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(30, 60, 30, 30)
        }

        val title = TextView(this).apply {
            text = "SHIV V4 REAL"
            textSize = 26f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
        }

        statusText = TextView(this).apply {
            text = "Stoploss Active:\n• 0.1 SOL Daily\n• 10% Per Trade\n\nStatus: IDLE"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0,20,0,20)
        }

        val startBtn = Button(this).apply {
            text = "START V4 REAL TRADING"
            setBackgroundColor(Color.GREEN)
            setTextColor(Color.BLACK)
        }

        val stopBtn = Button(this).apply {
            text = "STOP TRADING"
            setBackgroundColor(Color.RED)
            setTextColor(Color.WHITE)
        }
val connectBtn = Button(this).apply {
    text = "CONNECT SEEKER VAULT"
    setBackgroundColor(Color.CYAN)
    setTextColor(Color.BLACK)
}
connectBtn.setOnClickListener {
    // Run in coroutine
    kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.Main) {
        logText.append("\n[Seeker] Connecting to Seed Vault...")
        val pubkey = SeekerVaultTrader.connectToSeedVault(this@MainActivity)
        logText.append("\n[Seeker] Wallet: ${pubkey.take(8)}...")
        logText.append("\n[Seeker] Balance: ${SeekerVaultTrader.solBalance} SOL")
        statusText.text = "Connected: ${SeekerVaultTrader.solBalance} SOL"
    }
}
layout.addView(connectBtn, 2) // Add near top

        logText = TextView(this).apply {
            text = "Log: Ready.\nBuild #87 fixed.\nWaiting for START..."
            textSize = 12f
            setTextColor(Color.GRAY)
        }

        val scroll = ScrollView(this).apply { addView(logText) }

        startBtn.setOnClickListener {
            try {
                statusText.text = "Status: RUNNING\nStoploss: ACTIVE"
                logText.append("\n[4:46] Starting Jupiter V4...")
                logText.append("\n[4:46] Stoploss checks enabled")

                val intent = Intent(this, ShivBackgroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                logText.append("\n[4:46] Background service started")
            } catch (e: Exception) {
                logText.append("\nERROR: ${e.message}")
            }
        }

        stopBtn.setOnClickListener {
            try {
                stopService(Intent(this, ShivBackgroundService::class.java))
                statusText.text = "Status: STOPPED\nStoploss: INACTIVE"
                logText.append("\n[STOPPED] Trading halted")
            } catch (e: Exception) {
                logText.append("\nERROR STOP: ${e.message}")
            }
        }

        layout.addView(title)
        layout.addView(statusText)
        layout.addView(startBtn)
        layout.addView(stopBtn)
        layout.addView(scroll)

        setContentView(layout)
    }
}