package com.example.ecospend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.launch

class login : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView
    private lateinit var tvUserNotFound: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        etEmail = findViewById(R.id.etLoginEmail)
        etPassword = findViewById(R.id.etLoginPassword)
        tvEmailError = findViewById(R.id.tvLoginEmailError)
        tvPasswordError = findViewById(R.id.tvLoginPasswordError)
        tvUserNotFound = findViewById(R.id.tvUserNotFound)

        // hyperlink to register
        val tvGoToRegister = findViewById<TextView>(R.id.tvGoToRegister)
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, register::class.java))
            finish()
        }

        // login button
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        var isValid = true

        tvEmailError.visibility = View.GONE
        tvPasswordError.visibility = View.GONE
        tvUserNotFound.visibility = View.GONE

        if (email.isEmpty()) {
            tvEmailError.text = "Email is required"
            tvEmailError.visibility = View.VISIBLE
            isValid = false
        }

        if (password.isEmpty()) {
            tvPasswordError.text = "Password is required"
            tvPasswordError.visibility = View.VISIBLE
            isValid = false
        }

        if (!isValid) return

        val userDao = DatabaseProvider.getUserDao(this)

        lifecycleScope.launch {
            val user = userDao.getUserByEmail(email)

            if (user == null) {
                tvEmailError.text = "No account found with this email"
                tvEmailError.visibility = View.VISIBLE
                return@launch
            }

            val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)

            if (!result.verified) {
                tvPasswordError.text = "Incorrect password"
                tvPasswordError.visibility = View.VISIBLE
                return@launch
            }

            // Save logged in user ID to SharedPreferences
            getSharedPreferences("ecospend_prefs", MODE_PRIVATE)
                .edit()
                .putLong("user_id", user.id)
                .apply()

            Toast.makeText(this@login, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()

            startActivity(Intent(this@login, dashboard::class.java))
            finish()
        }
    }
}
