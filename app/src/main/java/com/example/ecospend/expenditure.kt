package com.example.ecospend

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class expenditure : AppCompatActivity() {
    private var selectedCategoryId: Long = -1
    private var selectedCategoryColor = "#1A3D2E"
    private var selectedDateMillis: Long = 0
    private var startTime = ""
    private var endTime = ""
    private var receiptUri: Uri? = null
    private var userId: Long = -1L

    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_expenditure)

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
            startActivity(Intent(this, landing::class.java))
            finish()
            return
        }

        setupCameraLaunchers()
        setupDatePicker()
        setupTimePickers()
        loadCategories()

        findViewById<Button>(R.id.btnAddExpense).setOnClickListener {
            addExpense()
        }
    }

    private fun setupCameraLaunchers() {
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Toast.makeText(this, "Receipt Captured!", Toast.LENGTH_SHORT).show()
                // receiptUri is already set before launching
            }
        }

        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                openCamera()
            } else {
                Toast.makeText(this, "Camera permission required to scan receipts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCamera() {
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: Exception) {
            null
        }

        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                it
            )
            receiptUri = photoURI
            takePictureLauncher.launch(photoURI)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("RECEIPT_${timeStamp}_", ".jpg", storageDir)
    }

    fun onScanReceiptClick(view: View) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
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

    private fun setupTimePickers() {
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
        val container = findViewById<LinearLayout>(R.id.categoryContainer) ?: return
        val categoryDao = DatabaseProvider.getCategoryDao(this)

        lifecycleScope.launch {
            try {
                val categories = categoryDao.getCategoriesForUser(userId)
                container.removeAllViews()

                if (categories.isEmpty()) {
                    Toast.makeText(this@expenditure, "Please create an Envelope first!", Toast.LENGTH_LONG).show()
                    return@launch
                }

                val density = resources.displayMetrics.density
                for (cat in categories) {
                    val btn = Button(this@expenditure).apply {
                        text = cat.name
                        setTextColor(Color.WHITE)
                        backgroundTintList = ColorStateList.valueOf(cat.color.toColorInt())
                        transformationMethod = null

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, (12 * density).toInt(), 0)
                        layoutParams = params

                        setOnClickListener {
                            selectedCategoryId = cat.id
                            selectedCategoryColor = cat.color
                            updateCategorySelection(this)
                        }
                    }
                    container.addView(btn)
                }
            } catch (e: Exception) {
                Toast.makeText(this@expenditure, "Error loading envelopes", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCategorySelection(selectedBtn: Button) {
        val container = findViewById<LinearLayout>(R.id.categoryContainer) ?: return
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
            Toast.makeText(this, "Select an envelope", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()
        val expenseDao = DatabaseProvider.getExpenseDao(this)

        lifecycleScope.launch {
            try {
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
                Toast.makeText(this@expenditure, "Expense added", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@expenditure, "Failed to save expense", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toDashboard(view: View) { finish() }
    fun toExpenses(view: View) { }
    fun toEnvelopes(view: View) { startActivity(Intent(this, envelopes::class.java)) }
    fun toTracking(view: View) { startActivity(Intent(this, tracking::class.java)) }
    fun toInvest(view: View) { startActivity(Intent(this, invest::class.java)) }
    fun toProfile(view: View) { startActivity(Intent(this, profile::class.java)) }
}