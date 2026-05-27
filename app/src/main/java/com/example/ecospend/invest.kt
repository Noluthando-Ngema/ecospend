package com.example.ecospend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class invest : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_invest)
        
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        findViewById<ImageView>(R.id.ivBack).setOnClickListener {
            val intent=Intent(this,dashboard::class.java)
            startActivity(intent)
        }
        findViewById<ImageView>(R.id.ivProfile).setOnClickListener {
            val intent=Intent(this,profile::class.java)
            startActivity(intent)
        }
    }

    fun backToDashboard(view: View) {
        finish()
    }

    // Navigation methods for bottom bar
    fun toExpenses(view: View) {
        val intent = Intent(this, ExpenditureActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    fun toEnvelopes(view: View) {
        val intent = Intent(this, envelopes::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    fun toTracking(view: View) {
        val intent = Intent(this, tracking::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    fun toInvest(view: View) {
       val intent=Intent(this,invest::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }
}
