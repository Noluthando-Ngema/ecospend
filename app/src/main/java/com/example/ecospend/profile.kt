package com.example.ecospend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class profile : AppCompatActivity() {
    private lateinit var tvUsername: TextView
    private lateinit var tvBudget: TextView
    private lateinit var tvExpenses: TextView
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var goalsContainer: LinearLayout
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        tvUsername = findViewById(R.id.tvUsername)
        tvBudget = findViewById(R.id.tvBudget)
        tvExpenses = findViewById(R.id.tvExpenses)
        categoriesContainer = findViewById(R.id.categoriesContainer)
        goalsContainer = findViewById(R.id.goalsContainer)

        userId = getSharedPreferences("ecospend_prefs", MODE_PRIVATE).getLong("user_id", -1)
        if (userId == -1L) {
            startActivity(Intent(this, landing::class.java))
            finish()
            return
        }

        loadProfileData()
    }

    private fun loadProfileData() {
        val userDao = DatabaseProvider.getUserDao(this)
        val expenseDao = DatabaseProvider.getExpenseDao(this)
        val categoryDao = DatabaseProvider.getCategoryDao(this)
        val goalDao = DatabaseProvider.getGoalDao(this)

        lifecycleScope.launch {
            val user = userDao.getUserById(userId) ?: return@launch
            tvUsername.text = "${user.name} ${user.surname}"
            tvBudget.text = formatZAR(user.startingBalance)

            val totalSpent = expenseDao.getTotalSpent(userId) ?: 0.0
            tvExpenses.text = formatZAR(totalSpent)

            // Load Categories
            val categories = categoryDao.getCategoriesForUser(userId)
            categoriesContainer.removeAllViews()
            for (cat in categories) {
                val tv = TextView(this@profile)
                tv.text = cat.name
                tv.setPadding(0, 8, 0, 8)
                tv.setTextColor(resources.getColor(R.color.ecospend_dark_green, theme))
                categoriesContainer.addView(tv)
            }

            // Load Goals Summary
            val goals = goalDao.getGoalsForUser(userId)
            goalsContainer.removeAllViews()
            if (goals.isEmpty()) {
                val tv = TextView(this@profile)
                tv.text = "No active goals"
                tv.setTextColor(resources.getColor(R.color.ecospend_dark_green, theme))
                goalsContainer.addView(tv)
            } else {
                for (goal in goals) {
                    val category = categories.find { it.id == goal.categoryId }
                    val tv = TextView(this@profile)
                    tv.text = "Goal: ${category?.name ?: "Unknown"} - ${formatZAR(goal.targetAmount)}"
                    tv.setPadding(0, 4, 0, 4)
                    tv.setTextColor(resources.getColor(R.color.ecospend_dark_green, theme))
                    goalsContainer.addView(tv)
                }
            }
        }
    }

    private fun formatZAR(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return format.format(amount)
    }

    fun toGoals(view: View) {
        val intent = Intent(this, GoalsActivity::class.java)
        startActivity(intent)
    }

    fun toRewards(view: View) {
        val intent = Intent(this, RewardsActivity::class.java)
        startActivity(intent)
    }


    fun toProfile(view: View) {
        // Already here
    }

    fun toDashboard(view: View) {
        val intent = Intent(this, dashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

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
        val intent = Intent(this, invest::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }
    fun backToLogin(view: View) {
        val intent = Intent(this, login::class.java)
        startActivity(intent)
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
    }
}
