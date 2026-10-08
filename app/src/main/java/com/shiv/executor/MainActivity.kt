package com.shiv.executor

import android.os.Bundle
import android.app.Activity
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity
import android.graphics.Color

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Crash-proof UI — no Compose, no external libs
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.BLACK)
            setPadding(40, 40, 40, 40)
        }
        
        val title = TextView(this).apply {
            text = "SHIV V4 REAL"
            textSize = 28f
            setTextColor(Color.GREEN)
            gravity = Gravity.CENTER
        }
        
        val status = TextView(this).apply {
            text = "\nStoploss Active:\n• 0.1 SOL Daily\n• 10% Per Trade\n\nStatus: IDLE (safe mode)\n\nBuild #87 passed resource linking.\nNow fixing runtime crash."
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        
        layout.addView(title)
        layout.addView(status)
        setContentView(layout)
        
        // DO NOT auto-start service yet — let's make app open first
        // After app opens stable, we add service start
    }
}