package com.shiv.executor

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val listView = findViewById<ListView>(R.id.listViewHistory)
        val logs = StatusManager.getLogs()
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, logs)
        listView?.adapter = adapter
    }
}
