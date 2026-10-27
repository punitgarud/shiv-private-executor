package com.shiv.executor

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import kotlinx.coroutines.*

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var logsText: TextView
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HotWalletManager.init()

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A0A"))
            setPadding(24, 24, 24, 24)
        }

        // App Header
        val header = TextView(this).apply {
            text = "⚡ SHIV v5 BLACK [SEEKER EDITION]"
            textSize = 20f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#00FF66"))
            setPadding(0, 0, 0, 12)
        }
        rootLayout.addView(header)

        // Status Card View
        statusText = TextView(this).apply {
            text = "Balance: 1.0 SOL | Daily Loss: 0.0 SOL\nCan Trade: true | Wallet: Connected"
            textSize = 13f
            setTextColor(Color.parseColor("#DDDDDD"))
            setBackgroundColor(Color.parseColor("#161616"))
            setPadding(20, 20, 20, 20)
        }
        rootLayout.addView(statusText)

        // Control Buttons Row 1
        val buttonLayout1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 16, 0, 8)
            gravity = Gravity.CENTER
        }

        val btnToggleService = Button(this).apply {
            text = "Toggle Service"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#00FF66"))
            setTextColor(Color.parseColor("#000000"))
            setOnClickListener {
                val intent = Intent(this@MainActivity, ShivAutoService::class.java)
                startForegroundService(intent)
                Toast.makeText(this@MainActivity, "SHIV Background Service Started", Toast.LENGTH_SHORT).show()
            }
        }

        val btnManualTrade = Button(this).apply {
            text = "Instant Swap"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#00FF66"))
            setOnClickListener {
                scope.launch(Dispatchers.IO) {
                    val success = JupiterRealTrader.executeSwap("So11111111111111111111111111111111111111112", 0.01)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Manual Swap Result: $success", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        buttonLayout1.addView(btnToggleService)
        buttonLayout1.addView(btnManualTrade)
        rootLayout.addView(buttonLayout1)

        // Control Buttons Row 2
        val buttonLayout2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 0, 0, 16)
            gravity = Gravity.CENTER
        }

        val btnViewHistory = Button(this).apply {
            text = "Execution History"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#00FF66"))
            setOnClickListener {
                startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
            }
        }

        val btnClearLogs = Button(this).apply {
            text = "Clear Logs"
            textSize = 12f
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#FF5555"))
            setOnClickListener {
                StatusManager.clearLogs()
                StatusManager.appendLog("Logs cleared by user")
            }
        }

        buttonLayout2.addView(btnViewHistory)
        buttonLayout2.addView(btnClearLogs)
        rootLayout.addView(buttonLayout2)

        // Logs Header
        val logsHeader = TextView(this).apply {
            text = "Live Execution & 40-Module Telemetry:"
            textSize = 14f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#00FF66"))
            setPadding(0, 8, 0, 8)
        }
        rootLayout.addView(logsHeader)

        // Scrollable Live Logs
        val scrollView = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        logsText = TextView(this).apply {
            text = "SHIV v5 BLACK initialized successfully.\n"
            textSize = 11f
            setTextColor(Color.parseColor("#00FF66"))
            typeface = android.graphics.Typeface.MONOSPACE
        }
        scrollView.addView(logsText)
        rootLayout.addView(scrollView)

        setContentView(rootLayout)
        startStatusPoller()
    }

    private fun startStatusPoller() {
        scope.launch {
            while (isActive) {
                try {
                    val balance = StatusManager.getBalance()
                    val dailyLoss = StatusManager.getDailyLoss()
                    val canTrade = PerformanceService.canTrade()
                    val pubKey = HotWalletManager.getHotWalletPubkey()

                    statusText.text = "Balance: $balance SOL | Daily Loss: $dailyLoss SOL\nCan Trade: $canTrade | Wallet: ${pubKey.take(6)}...${pubKey.takeLast(4)}"
                    
                    val logs = StatusManager.getLogs().takeLast(35).joinToString("\n")
                    logsText.text = logs.ifEmpty { "Scanning analysis modules..." }
                } catch (e: Exception) {
                    // Ignore transient UI refresh exceptions
                }
                delay(1500)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
