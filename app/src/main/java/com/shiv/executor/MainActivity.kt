package com.shiv.executor
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        try { Thread.setDefaultUncaughtExceptionHandler { _, e -> e.printStackTrace() } } catch(_:Exception){}
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setBackgroundColor(0xFF000000.toInt()); setPadding(40,100,40,40) }
        layout.addView(TextView(this).apply { text="SHIV V4 TEST BOOT OK"; setTextColor(Color.WHITE); textSize=22f })
        setContentView(layout)
        Toast.makeText(this,"Boot OK - MainActivity works",Toast.LENGTH_LONG).show()
    }
}
