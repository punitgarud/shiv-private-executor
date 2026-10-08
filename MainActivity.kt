package com.shiv.executor
import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.*

class MainActivity : Activity() {
    private val scope = CoroutineScope(Dispatchers.Main)
    lateinit var wallet: MobileWalletAdapter
    var pubkey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = MobileWalletAdapter(this)
        val layout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(60,150,60,60) }
        val tv = TextView(this).apply { text = "Shiv Executor - Seeker\nNot connected"; textSize = 18f }
        val btn = Button(this).apply { text = "Connect Seeker Wallet" }
        btn.setOnClickListener {
            scope.launch {
                try {
                    tv.text = "Connecting to Seed Vault..."
                    pubkey = wallet.connect()
                    tv.text = "Connected: $pubkey\nBalance: ${ShivBackgroundService.getBalance()}\nReady to trade"
                    btn.text = "Wallet Connected ✅"
                } catch (e: Exception) { tv.text = "Connect failed: ${e.message}" }
            }
        }
        layout.addView(tv); layout.addView(btn); setContentView(layout)
    }
}