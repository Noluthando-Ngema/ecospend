package com.example.ecospend

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class tracking : AppCompatActivity() {
    private lateinit var allocationContainer: LinearLayout
    private lateinit var tvWeekly: TextView
    private lateinit var tvMonthly: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tracking)

        allocationContainer = findViewById(R.id.allocationContainer)
        tvWeekly = findViewById(R.id.tvWeekly)
        tvMonthly = findViewById(R.id.tvMonthly)

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        val userId = getSharedPreferences("ecospend_prefs", MODE_PRIVATE).getLong("user_id", -1)
        if (userId != -1L) {
            loadAllocationData(userId)
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

    private fun loadAllocationData(userId: Long) {
        val expenseDao = DatabaseProvider.getExpenseDao(this)

        lifecycleScope.launch {
            // 1. Load Weekly and Monthly Totals
            val weeklyTotal = expenseDao.getPeriodTotal(userId, getWeekStart(), getToday())
            val monthlyTotal = expenseDao.getPeriodTotal(userId, getMonthStart(), getToday())

            tvWeekly.text = formatZAR(weeklyTotal ?: 0.0)
            tvMonthly.text = formatZAR(monthlyTotal ?: 0.0)

            // 2. Load Allocation Data
            val categorySpendList = expenseDao.getSpendByCategory(userId)
            val totalSpendAcrossCategories = categorySpendList.sumOf { it.totalSpent }

            allocationContainer.removeAllViews()

            val inflater = LayoutInflater.from(this@tracking)
            for (item in categorySpendList) {
                val percent = if (totalSpendAcrossCategories > 0) (item.totalSpent / totalSpendAcrossCategories) * 100 else 0.0
                val row = inflater.inflate(R.layout.item_allocation_row, allocationContainer, false)

                val colorView = row.findViewById<View>(R.id.colorDot)
                val nameView = row.findViewById<TextView>(R.id.tvCategoryName)
                val percentView = row.findViewById<TextView>(R.id.tvPercentage)

                val colorHex = item.color ?: "#9E9E9E"
                try {
                    colorView.setBackgroundColor(colorHex.toColorInt())
                } catch (_: Exception) {
                    colorView.setBackgroundColor(Color.GRAY)
                }

                nameView.text = item.name
                val percentText = "${percent.toInt()}%"
                percentView.text = percentText

                allocationContainer.addView(row)
            }
        }
    }

    private fun getWeekStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getMonthStart(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun getToday(): Long {
        return System.currentTimeMillis()
    }

    private fun formatZAR(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
        return format.format(amount)
    }

    fun backToDashboard(view: View) {
        finish()
    }

    // Navigation methods for bottom bar and dashboard links
    fun toDashboard(view: View) {
        val intent = Intent(this, dashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

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
        // already on this page
    }

    fun toInvest(view: View) {
        val intent = Intent(this, invest::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }

    fun toProfile(view: View) {
        // Implement when Profile activity is ready
    }
}
