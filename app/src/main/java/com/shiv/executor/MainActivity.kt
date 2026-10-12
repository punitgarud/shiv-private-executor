package com.shiv.executor
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e("SHIV_BOOT", "boot start")
        try {
            val layout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(0xFF000000.toInt()); setPadding(40,200,40,40) }
            layout.addView(TextView(this).apply { text="SHIV V4 BOOT OK"; setTextColor(Color.GREEN); textSize=24f })
            val btn = Button(this).apply { text="Continue" }
            btn.setOnClickListener { Toast.makeText(this,"OK",Toast.LENGTH_SHORT).show() }
            layout.addView(btn)
            setContentView(layout)
        } catch(e:Exception){ Log.e("SHIV_BOOT","fail",e); setContentView(TextView(this).apply { text=e.toString(); setTextColor(Color.RED) }) }
    }
}
