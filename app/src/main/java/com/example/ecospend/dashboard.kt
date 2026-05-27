package com.example.ecospend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class dashboard : AppCompatActivity() {
    private lateinit var tvBalance: TextView
    private lateinit var tvEnvelopesAmount: TextView
    private lateinit var tvExpenditureAmount: TextView
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dashboard)


        tvBalance = findViewById(R.id.tvBalance)
        tvEnvelopesAmount = findViewById(R.id.tvEnvelopeBalance)
        tvExpenditureAmount = findViewById(R.id.tvExpenditureBalance)

        userId = getSharedPreferences("ecospend_prefs", MODE_PRIVATE).getLong("user_id", -1)
        if (userId == -1L) {
            startActivity(Intent(this, landing::class.java))
            finish()
            return
        }

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        loadDashboardData(userId)
    }

    override fun onResume() {
        super.onResume()
        if (userId != -1L) loadDashboardData(userId)
    }

    private fun loadDashboardData(userId: Long) {
        val userDao = DatabaseProvider.getUserDao(this)
        val expenseDao = DatabaseProvider.getExpenseDao(this)
        val categoryDao = DatabaseProvider.getCategoryDao(this)

        lifecycleScope.launch {
            val user = userDao.getUserById(userId) ?: return@launch
            val totalSpent = expenseDao.getTotalSpent(userId) ?: 0.0

            // 1. Current Balance = Starting Balance - Total Spent
            val currentBalance = user.startingBalance - totalSpent
            tvBalance.text = formatZAR(currentBalance)

            // 2. Expenditure = Sum of all expenses
            tvExpenditureAmount.text = formatZAR(totalSpent)

            // 3. Envelopes = Sum of spent amounts per category
            var totalEnvelopesSpent = 0.0
            val categories = categoryDao.getCategoriesForUser(userId)
            for (cat in categories) {
                totalEnvelopesSpent += categoryDao.getSpentAmountForCategory(cat.id) ?: 0.0
            }
            tvEnvelopesAmount.text = formatZAR(totalEnvelopesSpent)
        }
    }

    private fun formatZAR(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return format.format(amount)
    }

    // navigation methods for buttons in the layout
    fun toExpenses(view: View) {
        startActivity(Intent(this, ExpenditureActivity::class.java))
    }

    fun toEnvelopes(view: View) {
        startActivity(Intent(this, envelopes::class.java))
    }

    fun toInvest(view: View) {
        startActivity(Intent(this, invest::class.java))
    }

    fun toTracking(view: View) {
        startActivity(Intent(this, tracking::class.java))
    }
}
