package com.shiv.executor
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.shiv.executor.services.ShivAutoService
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try { CrashHandler.init() } catch(_:Exception){}
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(0xFF000000.toInt()); setPadding(40,100,40,40) }
        layout.addView(TextView(this).apply { text="SHIV PRIVATE EXECUTOR V4\nRisk Protected\n"; setTextColor(Color.WHITE); textSize=20f })
        val btnStart = Button(this).apply { text="Start Auto (Biometric)"; setBackgroundColor(0xFF00FF88.toInt()); setTextColor(Color.BLACK) }; layout.addView(btnStart)
        val btnHistory = Button(this).apply { text="History & PnL + Export CSV"; setBackgroundColor(0xFF121212.toInt()); setTextColor(Color.WHITE) }; layout.addView(btnHistory)
        val btnStop = Button(this).apply { text="Stop Service"; setBackgroundColor(0xFF1A1A1A.toInt()); setTextColor(Color.GRAY) }; layout.addView(btnStop)
        layout.addView(TextView(this).apply { text="Status: Idle\nTop4: Live | Fee: Lowest | Loss:-0.05 | MEV:1%\nBalance Guard: ON | Sharpe>1.5"; setTextColor(Color.GRAY); setPadding(0,30,0,0) })
        btnHistory.setOnClickListener{
            try { startActivity(Intent(this, HistoryActivity::class.java)) }
            catch(e:Exception){ Toast.makeText(this,"History not built yet: ${e.message}",Toast.LENGTH_LONG).show() }
        }
        btnStop.setOnClickListener{ stopService(Intent(this, ShivAutoService::class.java)); Toast.makeText(this,"Stopped",Toast.LENGTH_SHORT).show() }
        btnStart.setOnClickListener{
            try {
                val prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object:BiometricPrompt.AuthenticationCallback(){
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult){
                        val svc = Intent(this@MainActivity, ShivAutoService::class.java)
                        ContextCompat.startForegroundService(this@MainActivity, svc)
                        Toast.makeText(this@MainActivity,"V4 Risk Protected Started",Toast.LENGTH_LONG).show()
                    }
                })
                prompt.authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("Unlock SHIV").setSubtitle("Start trading with biometric").setNegativeButtonText("Cancel").build())
            } catch(e:Exception){ Toast.makeText(this,"Biometric error: ${e.message}\nStarting directly",Toast.LENGTH_LONG).show()
                val svc = Intent(this, ShivAutoService::class.java); ContextCompat.startForegroundService(this, svc) }
        }
        if(intent.getStringExtra("action")=="refill"){
            val amt = intent.getDoubleExtra("amount",0.5)
            AlertDialog.Builder(this).setTitle("Refill Hot Wallet?").setMessage("Low balance (<0.05 SOL).\nRefill $amt SOL from Seed Vault now?").setPositiveButton("Refill $amt SOL"){_,_-> Toast.makeText(this,"Refilling $amt SOL - implement Seed Vault transfer here",Toast.LENGTH_LONG).show() }.setNegativeButton("Later",null).show()
        }
        setContentView(layout)
    }
}
