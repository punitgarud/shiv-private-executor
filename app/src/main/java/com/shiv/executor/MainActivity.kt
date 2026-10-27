package com.shiv.executor

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import kotlinx.coroutines.*

class MainActivity : Activity() {
    private lateinit var statusText: TextView
    private lateinit var moduleStatsText: TextView
    private lateinit var logsText: TextView
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HotWalletManager.init(applicationContext)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#0A0A0A"))
            setPadding(20, 20, 20, 20)
        }

        // App Header
        val header = TextView(this).apply {
            text = "⚡ SHIV v5 BLACK [SEEKER dApp]"
            textSize = 18f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#00FF66"))
            setPadding(0, 0, 0, 8)
        }
        rootLayout.addView(header)

        // Status Card View
        statusText = TextView(this).apply {
            text = "Loading Wallet & Balance..."
            textSize = 12f
            setTextColor(Color.parseColor("#DDDDDD"))
            setBackgroundColor(Color.parseColor("#161616"))
            setPadding(16, 16, 16, 16)
        }
        rootLayout.addView(statusText)

        // Module Performance Breakdown Card
        moduleStatsText = TextView(this).apply {
            text = "Module PnL Performance:\nLoading..."
            textSize = 11f
            setTextColor(Color.parseColor("#00E5FF"))
            setBackgroundColor(Color.parseColor("#121212"))
            setPadding(16, 16, 16, 16)
        }
        val modParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        modParams.setMargins(0, 10, 0, 10)
        moduleStatsText.layoutParams = modParams
        rootLayout.addView(moduleStatsText)

        // Control Buttons Row 1 (Service & Swap)
        val buttonLayout1 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 4)
        }

        val btnToggleService = Button(this).apply {
            text = "Toggle Service"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#00FF66"))
            setTextColor(Color.parseColor("#000000"))
            setOnClickListener {
                val intent = Intent(this@MainActivity, ShivAutoService::class.java)
                startForegroundService(intent)
                Toast.makeText(this@MainActivity, "SHIV Background Service Started", Toast.LENGTH_SHORT).show()
            }
        }

        val btnConnectWallet = Button(this).apply {
            text = "Connect Wallet"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#00E5FF"))
            setTextColor(Color.parseColor("#000000"))
            setOnClickListener {
                showWalletConnectDialog()
            }
        }

        buttonLayout1.addView(btnToggleService)
        buttonLayout1.addView(btnConnectWallet)
        rootLayout.addView(buttonLayout1)

        // Control Buttons Row 2 (Fund Wallet & Withdraw)
        val buttonLayout2 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 4)
        }

        val btnFundWallet = Button(this).apply {
            text = "+ Fund (0.5 SOL)"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#00FF66"))
            setOnClickListener {
                HotWalletManager.deposit(0.5)
                Toast.makeText(this@MainActivity, "Deposited +0.5 SOL", Toast.LENGTH_SHORT).show()
            }
        }

        val btnWithdrawWallet = Button(this).apply {
            text = "- Withdraw (0.1 SOL)"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#FF5555"))
            setOnClickListener {
                val success = HotWalletManager.withdraw(0.1)
                val msg = if (success) "Withdrew 0.1 SOL" else "Withdrawal failed (Insufficient funds)"
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
            }
        }

        buttonLayout2.addView(btnFundWallet)
        buttonLayout2.addView(btnWithdrawWallet)
        rootLayout.addView(buttonLayout2)

        // Control Buttons Row 3 (History & Clear)
        val buttonLayout3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 10)
        }

        val btnViewHistory = Button(this).apply {
            text = "History"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#00FF66"))
            setOnClickListener {
                startActivity(Intent(this@MainActivity, HistoryActivity::class.java))
            }
        }

        val btnClearLogs = Button(this).apply {
            text = "Clear Logs"
            textSize = 11f
            setBackgroundColor(Color.parseColor("#222222"))
            setTextColor(Color.parseColor("#FF5555"))
            setOnClickListener {
                StatusManager.clearLogs()
                StatusManager.appendLog("Logs cleared by user")
            }
        }

        buttonLayout3.addView(btnViewHistory)
        buttonLayout3.addView(btnClearLogs)
        rootLayout.addView(buttonLayout3)

        // Logs Header
        val logsHeader = TextView(this).apply {
            text = "Live Execution Telemetry:"
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(Color.parseColor("#00FF66"))
            setPadding(0, 4, 0, 4)
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
            text = "SHIV v5 BLACK initialized.\n"
            textSize = 10f
            setTextColor(Color.parseColor("#00FF66"))
            typeface = android.graphics.Typeface.MONOSPACE
        }
        scrollView.addView(logsText)
        rootLayout.addView(scrollView)

        setContentView(rootLayout)
        startStatusPoller()
    }

    private fun showWalletConnectDialog() {
        val input = EditText(this).apply {
            hint = "Enter Solana Public Key / Address"
            setTextColor(Color.WHITE)
            setHintTextColor(Color.GRAY)
        }

        AlertDialog.Builder(this)
            .setTitle("Connect Solana Wallet")
            .setMessage("Enter your Solana wallet address or Seeker Seed Vault pubkey:")
            .setView(input)
            .setPositiveButton("Connect") { _, _ ->
                val pubKey = input.text.toString().trim()
                if (pubKey.length > 30) {
                    HotWalletManager.importWallet(pubKey)
                    Toast.makeText(this, "Wallet Connected Successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Invalid Solana Address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                    
                    val modulePnl = StatusManager.getModulePnl()
                    val statsBuilder = StringBuilder("Module PnL Performance:\n")
                    for ((mod, pnlVal) in modulePnl) {
                        statsBuilder.append(" • $mod: +$pnlVal SOL\n")
                    }
                    moduleStatsText.text = statsBuilder.toString().trim()

                    val logs = StatusManager.getLogs().takeLast(30).joinToString("\n")
                    logsText.text = logs.ifEmpty { "Scanning analysis modules..." }
                } catch (e: Exception) {
                    // Ignore transient polling errors
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
