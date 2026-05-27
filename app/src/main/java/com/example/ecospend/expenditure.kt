package com.example.ecospend

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ExpenditureActivity : AppCompatActivity() {
    private var selectedCategoryId: Long = -1
    private var selectedCategoryColor = "#1A3D2E"
    private var selectedDateMillis: Long = 0
    private var startTime=""
    private var endTime=""
    private var receiptUri: Uri? = null
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expenditure)

        // Handle window insets
        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        userId = getSharedPreferences("ecospend_prefs", MODE_PRIVATE).getLong("user_id", -1)
        if (userId == -1L) {
            finish()
            return
        }

        setupDatePicker()
        setupTimePickers()
        loadCategories()

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            addExpense()
        }
    }

    private fun setupDatePicker() {
        val etDate = findViewById<EditText>(R.id.etDate)
        etDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                cal.set(year, month, day)
                selectedDateMillis = cal.timeInMillis
                val format = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
                etDate.setText(format.format(cal.time))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
    private fun setupTimePickers(){
        val etStart = findViewById<EditText>(R.id.etStartTime)
        val etEnd = findViewById<EditText>(R.id.etEndTime)

        etStart?.setOnClickListener {
            showTimePicker { time ->
                startTime = time
                etStart.setText(time)
            }
        }

        etEnd?.setOnClickListener {
            showTimePicker { time ->
                endTime = time
                etEnd.setText(time)
            }
        }
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val cal = Calendar.getInstance()
        TimePickerDialog(this, { _, hour, minute ->
            val formatted = String.format("%02d:%02d", hour, minute)
            onTimeSelected(formatted)
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
    }

    private fun loadCategories() {
        val container = findViewById<LinearLayout>(R.id.categoryContainer)
        val categoryDao = DatabaseProvider.getCategoryDao(this)

        lifecycleScope.launch {
            val categories = categoryDao.getCategoriesForUser(userId)
            container.removeAllViews()

            if (categories.isEmpty()) {
                Toast.makeText(this@ExpenditureActivity, "Create an envelope first", Toast.LENGTH_SHORT).show()
                return@launch
            }

            for (cat in categories) {
                val btn = Button(this@ExpenditureActivity)
                btn.text = cat.name
                btn.setBackgroundColor(cat.color.toColorInt())
                btn.setTextColor(Color.WHITE)
                btn.setPadding(24, 12, 24, 12)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 0, 12, 0)
                btn.layoutParams = params

                btn.setOnClickListener {
                    selectedCategoryId = cat.id
                    selectedCategoryColor = cat.color
                    updateCategorySelection(btn)
                }

                container.addView(btn)
            }
        }
    }

    private fun updateCategorySelection(selectedBtn: Button) {
        val container = findViewById<LinearLayout>(R.id.categoryContainer)
        for (i in 0 until container.childCount) {
            val btn = container.getChildAt(i) as Button
            btn.alpha = 0.5f
        }
        selectedBtn.alpha = 1.0f
    }

    private fun addExpense() {
        val amountStr = findViewById<EditText>(R.id.etAmount).text.toString().trim()
        val desc = findViewById<EditText>(R.id.etDescription).text.toString().trim()

        if (amountStr.isEmpty() || amountStr == "0.00") {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedDateMillis == 0L) {
            Toast.makeText(this, "Select a date", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedCategoryId == -1L) {
            Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()
        val expenseDao = DatabaseProvider.getExpenseDao(this)
        val goalDao = DatabaseProvider.getGoalDao(this)
        val rewardDao = DatabaseProvider.getRewardDao(this)

        lifecycleScope.launch {
            // 1. insert the expense
            expenseDao.insertExpense(
                Expense(
                    userId = userId,
                    categoryId = selectedCategoryId,
                    amount = amount,
                    description = desc,
                    date = selectedDateMillis,
                    startDateTime = startTime,
                    endDateTime = endTime,
                    receiptPath = receiptUri?.toString()
                )
            )

            // 2. check for "Not passing budget" reward
            val goal = goalDao.getGoalForCategory(selectedCategoryId)
            if (goal != null) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = selectedDateMillis
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val monthStart = cal.timeInMillis
                
                val spentThisMonth = expenseDao.getPeriodTotal(userId, monthStart, System.currentTimeMillis()) ?: 0.0
                
                if (spentThisMonth <= goal.targetAmount) {
                    rewardDao.insertReward(Reward(
                        userId = userId,
                        title = "Budget Conscious",
                        description = "Transaction within your budget limit!",
                        points = 5,
                        type = "WITHIN_BUDGET"
                    ))
                    Toast.makeText(this@ExpenditureActivity, "Goal maintained! +5 points", Toast.LENGTH_SHORT).show()
                }
            }

            Toast.makeText(this@ExpenditureActivity, "Expense added", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    fun onScanReceiptClick(view: View) {
        Toast.makeText(this, "Opening Camera...", Toast.LENGTH_SHORT).show()
    }

    fun backToDashboard(view: View) {
        finish()
    }

    fun toProfile(view: View) {
        startActivity(Intent(this, profile::class.java))
    }

    fun toExpenses(view: View) {//already here
    }
    fun toEnvelopes(view: View) { startActivity(Intent(this, envelopes::class.java)) }
    fun toTracking(view: View) { startActivity(Intent(this, tracking::class.java)) }
    fun toInvest(view: View) { startActivity(Intent(this, invest::class.java)) }
    fun toDashboard(view: View) { startActivity(Intent(this, dashboard::class.java)) }
}
