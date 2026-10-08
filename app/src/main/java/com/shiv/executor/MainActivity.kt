package com.shiv.executor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    private lateinit var walletText: TextView
    private lateinit var balanceText: TextView
    private lateinit var statusDot: TextView
    private lateinit var statusLabel: TextView
    private lateinit var logText: TextView
    private lateinit var dailyStopInput: TextInputEditText
    private lateinit var perTradeInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var slippageInput: TextInputEditText

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        walletText = findViewById(R.id.walletText)
        balanceText = findViewById(R.id.balanceText)
        statusDot = findViewById(R.id.statusDot)
        statusLabel = findViewById(R.id.statusLabel)
        logText = findViewById(R.id.logText)
        dailyStopInput = findViewById(R.id.dailyStopInput)
        perTradeInput = findViewById(R.id.perTradeInput)
        amountInput = findViewById(R.id.amountInput)
        slippageInput = findViewById(R.id.slippageInput)

        val connectBtn = findViewById<MaterialButton>(R.id.connectBtn)
        val startBtn = findViewById<MaterialButton>(R.id.startBtn)
        val stopBtn = findViewById<MaterialButton>(R.id.stopBtn)

        connectBtn.setOnClickListener {
            scope.launch {
                log("> Connecting to Seeker Vault...")
                val pubkey = SeekerVaultTrader.connectToSeedVault(this@MainActivity)
                if (pubkey.startsWith("ERROR")) {
                    log(pubkey)
                } else {
                    walletText.text = "${pubkey.take(4)}...${pubkey.takeLast(4)}"
                    balanceText.text = "%.4f SOL".format(SeekerVaultTrader.solBalance)
                    log("> Connected: ${SeekerVaultTrader.solBalance} SOL")
                    statusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF00FF88.toInt())
                    statusLabel.text = " CONNECTED"
                }
            }
        }

        startBtn.setOnClickListener {
            try {
                // APPLY BEST DEFAULTS - USER CAN TWEAK BUT FALLBACK TO BEST
                JupiterRealTrader.maxDailyLoss = dailyStopInput.text.toString().toDoubleOrNull()?: 0.1
                JupiterRealTrader.maxPerTradeLossPercent = perTradeInput.text.toString().toDoubleOrNull()?: 8.0
                JupiterRealTrader.defaultSlippageBps = slippageInput.text.toString().toIntOrNull()?: 150
                val amountSol = amountInput.text.toString().toDoubleOrNull()?: 0.02
                val lamports = (amountSol * 1_000_000_000).toLong().toString()

                statusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFF00FF88.toInt())
                statusLabel.text = " RUNNING"

                log("> START V4 REAL")
                log("> Daily Stop: ${JupiterRealTrader.maxDailyLoss} SOL")
                log("> Per Trade: ${JupiterRealTrader.maxPerTradeLossPercent}%")
                log("> Amount: $amountSol SOL (${lamports} lamports)")
                log("> Slippage: ${JupiterRealTrader.defaultSlippageBps} BPS")
                log("> Priority: ${JupiterRealTrader.defaultPriorityFee} lamports")

                scope.launch {
                    val result = SeekerVaultTrader.executeRealTrade(this@MainActivity, lamports, JupiterRealTrader.defaultSlippageBps)
                    log(result)
                }
            } catch (e: Exception) {
                log("ERROR: ${e.message}")
            }
        }

        stopBtn.setOnClickListener {
            JupiterRealTrader.isTradingEnabled = false
            statusDot.backgroundTintList = android.content.res.ColorStateList.valueOf(0xFFFF3B30.toInt())
            statusLabel.text = " STOPPED"
            log("> STOPPED")
            log("> Daily Loss: ${JupiterRealTrader.dailyLossSOL} SOL")
        }
    }

    private fun log(msg: String) {
        logText.append("\n> $msg")
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}