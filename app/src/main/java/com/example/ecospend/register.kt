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

class register : AppCompatActivity() {
    private lateinit var etName: EditText
    private lateinit var etSurname: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvEmailError: TextView
    private lateinit var tvPasswordError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvEmailError = findViewById(R.id.tvEmailError)
        tvPasswordError = findViewById(R.id.tvPasswordError)

        findViewById<Button>(R.id.btnCreateAccount).setOnClickListener {
            validateAndSubmit()
        }

        findViewById<TextView>(R.id.tvGoToLogin).setOnClickListener {
            startActivity(Intent(this, login::class.java))
            finish()
        }
    }

    private fun validateAndSubmit() {
        val name = etName.text.toString().trim()
        val surname = etSurname.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        var isValid = true
        tvEmailError.visibility = View.GONE
        tvPasswordError.visibility = View.GONE

        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.text = "Enter a valid email address"
            tvEmailError.visibility = View.VISIBLE
            isValid = false
        }

        val passwordRegex = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"
        if (password.isNotEmpty() && !password.matches(passwordRegex.toRegex())) {
            tvPasswordError.text = "Password must be at least 8 characters, with a number, uppercase, and special character"
            tvPasswordError.visibility = View.VISIBLE
            isValid = false
        }

        if (isValid && name.isNotEmpty() && surname.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            val userDao = DatabaseProvider.getUserDao(this)
            lifecycleScope.launch {
                try {
                    val existing = userDao.getUserByEmail(email)
                    if (existing != null) {
                        tvEmailError.text = "Email already registered"
                        tvEmailError.visibility = View.VISIBLE
                        return@launch
                    }
                    
                    val hash = BCrypt.withDefaults().hashToString(12, password.toCharArray())
                    userDao.insertUser(User(name = name, surname = surname, email = email, passwordHash = hash))

                    Toast.makeText(this@register, "Account created successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@register, login::class.java))
                    finish()
                } catch (e: Exception) {
                    Toast.makeText(this@register, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
