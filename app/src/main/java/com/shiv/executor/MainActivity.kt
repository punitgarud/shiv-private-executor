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
            setPadding(32, 32, 32, 32)
        }

        // App Header
        val header = TextView(this).apply {
            text = "⚡ SHIV v5 BLACK"
            textSize = 24f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#00FF66"))
            setPadding(0, 0, 0, 16)
        }
        rootLayout.addView(header)

        // Status Card View
        statusText = TextView(this).apply {
            text = "Balance: 1.0 SOL | Daily Loss: 0.0 SOL\nCan Trade: true | Wallet: Connected"
            textSize = 14f
            setTextColor(Color.parseColor("#CCCCCC"))
            setBackgroundColor(Color.parseColor("#161616"))
            setPadding(24, 24, 24, 24)
        }
        rootLayout.addView(statusText)

        // Action Buttons Layout
        val buttonLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 24, 0, 24)
            gravity = Gravity.CENTER
        }

        val btnToggleService = Button(this).apply {
            text = "Toggle Executor Service"
            setBackgroundColor(Color.parseColor("#00FF66"))
            setTextColor(Color.parseColor("#000000"))
            setOnClickListener {
                val intent = Intent(this@MainActivity, ShivAutoService::class.java)
                startForegroundService(intent)
                Toast.makeText(this@MainActivity, "SHIV Service Started", Toast.LENGTH_SHORT).show()
            }
        }

        val btnViewHistory = Button(this).apply {
            text = "View History"
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#00FF66"))
            setOnClickListener {
                startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
            }
        }

        buttonLayout.addView(btnToggleService)
        buttonLayout.addView(btnViewHistory)
        rootLayout.addView(buttonLayout)

        // Logs Header
        val logsHeader = TextView(this).apply {
            text = "Live Execution Logs:"
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#00FF66"))
            setPadding(0, 16, 0, 8)
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
            text = "Initializing SHIV v5 BLACK Seeker dApp...\n"
            textSize = 12f
            setTextColor(Color.parseColor("#00FF66"))
            typeface = android.graphics.Typeface.MONOSPACE
        }
        scrollView.addView(logsText)
        rootLayout.addView(scrollView)

        setContentView(rootLayout)

        // Periodically refresh UI status and logs
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
                    
                    val logs = StatusManager.getLogs().takeLast(30).joinToString("\n")
                    logsText.text = logs.ifEmpty { "Scanning 40 analysis modules..." }
                } catch (e: Exception) {
                    // Ignore transient UI refresh errors
                }
                delay(2000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
