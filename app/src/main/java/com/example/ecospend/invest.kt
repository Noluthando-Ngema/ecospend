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

    fun toDashboard(view: View) {
        val intent = Intent(this, dashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)

    }
    fun toProfile(view: View) {
        val intent = Intent(this, profile::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    // navigation methods for bottom bar
    fun toExpenses(view: View) {
        val intent = Intent(this, expenditure::class.java)
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
       //already here
    }

}
