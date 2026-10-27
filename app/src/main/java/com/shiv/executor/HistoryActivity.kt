package com.shiv.executor

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView

class HistoryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(0xFF000000.toInt())
        }

        val title = TextView(this).apply {
            text = "SHIV v5 BLACK Execution History"
            textSize = 20f
            setTextColor(0xFF00FF00.toInt())
            setPadding(0, 0, 0, 16)
        }
        layout.addView(title)

        val listView = ListView(this)
        val logs = StatusManager.getLogs()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logs)
        listView.adapter = adapter
        layout.addView(listView)

        setContentView(layout)
    }
}
