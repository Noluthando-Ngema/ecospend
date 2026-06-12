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
import java.util.Calendar

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

        // navigation to Register
        findViewById<TextView>(R.id.tvGoToRegister).setOnClickListener {
            startActivity(Intent(this, register::class.java))
            finish()
        }

        // login button listener
        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            performLogin()
        }
    }
    fun loginUser(view: View) {
        performLogin()
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim().lowercase()
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
            try {
                // 1. Fetch user by email
                val user = userDao.getUserByEmail(email)

                if (user == null) {
                    tvEmailError.text = "No account found with this email"
                    tvEmailError.visibility = View.VISIBLE
                    return@launch
                }

                // 2. Verify password hash
                val result = BCrypt.verifyer().verify(password.toCharArray(), user.passwordHash)

                if (!result.verified) {
                    tvPasswordError.text = "Incorrect password"
                    tvPasswordError.visibility = View.VISIBLE
                    return@launch
                }
                // 3. Update Streak Logic
                val nowMillis = System.currentTimeMillis()
                var newStreak = user.loginStreak

                if (user.lastLoginDate == 0L) {
                    newStreak = 1
                } else {
                    val lastCal = Calendar.getInstance().apply { timeInMillis = user.lastLoginDate }
                    val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }

                    // Only update if it's a different day
                    if (nowCal.get(Calendar.DAY_OF_YEAR) != lastCal.get(Calendar.DAY_OF_YEAR) ||
                        nowCal.get(Calendar.YEAR) != lastCal.get(Calendar.YEAR)) {

                        // Check if it's exactly the next day for consecutive streak
                        lastCal.add(Calendar.DAY_OF_YEAR, 1)
                        if (nowCal.get(Calendar.DAY_OF_YEAR) == lastCal.get(Calendar.DAY_OF_YEAR) &&
                            nowCal.get(Calendar.YEAR) == lastCal.get(Calendar.YEAR)) {
                            newStreak++
                        } else {
                            newStreak = 1 // Reset if more than 1 day missed
                        }
                    }
                }
                // 4. Save updated user data
                val updatedUser = user.copy(
                    loginStreak = newStreak,
                    lastLoginDate = nowMillis
                )
                userDao.updateUser(updatedUser)

                // 5. Store session and navigate
                getSharedPreferences("ecospend_prefs", MODE_PRIVATE)
                    .edit()
                    .putLong("user_id", user.id)
                    .apply()

                Toast.makeText(this@login, "Welcome back, ${user.name}!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@login, dashboard::class.java))
                finish()

            } catch (e: Exception) {
                Toast.makeText(this@login, "Database Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}



