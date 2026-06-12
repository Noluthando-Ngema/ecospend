package com.example.ecospend

import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class GoalsActivity : AppCompatActivity() {
    private lateinit var categoryContainer: LinearLayout
    private lateinit var activeGoalsContainer: LinearLayout
    private lateinit var etGoalAmount: EditText
    private var selectedCategoryId: Long = -1
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_goals)

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        categoryContainer = findViewById(R.id.categoryContainer)
        activeGoalsContainer = findViewById(R.id.activeGoalsContainer)
        etGoalAmount = findViewById(R.id.etGoalAmount)

        userId = getSharedPreferences("ecospend_prefs", MODE_PRIVATE).getLong("user_id", -1)
        if (userId == -1L) {
            finish()
            return
        }

        loadCategories()
        loadGoals()

        findViewById<Button>(R.id.btnSetGoal).setOnClickListener {
            setGoal()
        }
    }

    private fun loadCategories() {
        val categoryDao = DatabaseProvider.getCategoryDao(this)
        lifecycleScope.launch {
            val categories = categoryDao.getCategoriesForUser(userId)
            categoryContainer.removeAllViews()
            for (cat in categories) {
                val btn = Button(this@GoalsActivity)
                btn.text = cat.name
                btn.setBackgroundColor(cat.color.toColorInt())
                btn.setTextColor(Color.WHITE)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 12, 0)
                btn.layoutParams = params
                btn.setOnClickListener {
                    selectedCategoryId = cat.id
                    updateCategorySelection(btn, cat.color)
                }
                categoryContainer.addView(btn)
            }
        }
    }

    private fun updateCategorySelection(selectedBtn: Button, color: String) {
        for (i in 0 until categoryContainer.childCount) {
            val btn = categoryContainer.getChildAt(i) as Button
            btn.alpha = 0.5f
        }
        selectedBtn.alpha = 1.0f
    }

    private fun setGoal() {
        val amountStr = etGoalAmount.text.toString().trim()
        if (selectedCategoryId == -1L || amountStr.isEmpty()) {
            Toast.makeText(this, "Select a category and enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()
        val goalDao = DatabaseProvider.getGoalDao(this)
        val rewardDao = DatabaseProvider.getRewardDao(this)

        lifecycleScope.launch {
            val goal = Goal(userId = userId, categoryId = selectedCategoryId, targetAmount = amount)
            goalDao.insertGoal(goal)
            
            // Give reward for setting a goal
            rewardDao.insertReward(Reward(
                userId = userId,
                title = "Goal Setter",
                description = "You set a new financial goal!",
                points = 10,
                type = "GOAL_SET"
            ))

            Toast.makeText(this@GoalsActivity, "Goal established and reward earned!", Toast.LENGTH_SHORT).show()
            etGoalAmount.text.clear()
            loadGoals()
        }
    }

    private fun loadGoals() {
        val goalDao = DatabaseProvider.getGoalDao(this)
        val categoryDao = DatabaseProvider.getCategoryDao(this)
        val expenseDao = DatabaseProvider.getExpenseDao(this)

        lifecycleScope.launch {
            val goals = goalDao.getGoalsForUser(userId)
            activeGoalsContainer.removeAllViews()

            for (goal in goals) {
                val category = categoryDao.getCategoriesForUser(userId).find { it.id == goal.categoryId } ?: continue
                val spent = expenseDao.getPeriodTotal(userId, getMonthStart(), System.currentTimeMillis()) ?: 0.0
                
                val view = layoutInflater.inflate(R.layout.activity_item_envelope, activeGoalsContainer, false)
                view.findViewById<TextView>(R.id.tvEnvelopeName).text = "Goal: ${category.name}"
                view.findViewById<TextView>(R.id.tvEnvelopeDesc).text = "Target: ${formatZAR(goal.targetAmount)}"
                
                val statusText = if (spent > goal.targetAmount) "OVER BUDGET" else "ON TRACK"
                view.findViewById<TextView>(R.id.tvEnvelopeBalance).text = statusText
                view.findViewById<View>(R.id.viewColor).setBackgroundColor(category.color.toColorInt())
                
                activeGoalsContainer.addView(view)
            }
        }
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

    private fun formatZAR(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "ZA")).format(amount)
    }

    fun toDashboard(view: View) { startActivity(Intent(this, dashboard::class.java)) }
    fun toExpenses(view: View) { startActivity(Intent(this, expenditure::class.java)) }
    fun toEnvelopes(view: View) { startActivity(Intent(this, envelopes::class.java)) }
    fun toTracking(view: View) { startActivity(Intent(this, tracking::class.java)) }
    fun toInvest(view: View) { startActivity(Intent(this, invest::class.java)) }
    fun toProfile(view: View) { startActivity(Intent(this, profile::class.java)) }
}
