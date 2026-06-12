package com.example.ecospend

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.util.Locale

class envelopes : AppCompatActivity() {
    private lateinit var colorContainer: LinearLayout
    private lateinit var activeContainer: LinearLayout
    private var selectedColor = "#1A3D2E"
    private var userId: Long = -1L

    private val colorOptions = listOf(
        "#F4E6D6", "#1A3D2E", "#8B5C3A", "#D4A373", "#FEA05A"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_envelopes)


        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        colorContainer = findViewById(R.id.colorContainer)
        activeContainer = findViewById(R.id.activeEnvelopesContainer)
        userId = getSharedPreferences("ecospend_prefs", MODE_PRIVATE).getLong("user_id", -1)
        if (userId == -1L) {
            finish()
            return
        }
        setupColorPicker()
        loadEnvelopes()

        findViewById<Button>(R.id.btnCreateEnvelope).setOnClickListener {
            createEnvelope()
        }
    }

    private fun setupColorPicker() {
        colorContainer.removeAllViews()
        for (color in colorOptions) {
            val colorView = View(this)
            val params = LinearLayout.LayoutParams(48, 48)
            params.setMargins(0, 0, 16, 0)
            colorView.layoutParams = params
            colorView.setBackgroundColor(color.toColorInt())
            colorView.background = ContextCompat.getDrawable(this, R.drawable.bg_circle)

            colorView.setOnClickListener {
                selectedColor = color
                highlightSelectedColor(colorView)
            }

            colorContainer.addView(colorView)
        }
        // Select first color by default
        if (colorContainer.childCount > 0) {
            colorContainer.getChildAt(0)?.performClick()
        }
    }

    private fun highlightSelectedColor(selectedView: View) {
        for (i in 0 until colorContainer.childCount) {
            val child = colorContainer.getChildAt(i)
            child.elevation = 0f
        }
        selectedView.elevation = 8f
    }

    private fun createEnvelope() {
        val name = findViewById<EditText>(R.id.etEnvelopeName).text.toString().trim()
        val desc = findViewById<EditText>(R.id.etEnvelopeDesc).text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show()
            return
        }

        val categoryDao = DatabaseProvider.getCategoryDao(this)
        val rewardDao = DatabaseProvider.getRewardDao(this)

        lifecycleScope.launch {
            val category = Category(
                userId = userId,
                name = name,
                description = desc.ifEmpty { null },
                color = selectedColor
            )
            categoryDao.insertCategory(category)

            // Grant reward for creating a category
            rewardDao.insertReward(Reward(
                userId = userId,
                title = "Envelope Architect",
                description = "Created a new envelope: $name",
                points = 50,
                type = "CATEGORY_CREATED"
            ))

            Toast.makeText(this@envelopes, "Envelope created! +50 points earned", Toast.LENGTH_SHORT).show()

            findViewById<EditText>(R.id.etEnvelopeName).text.clear()
            findViewById<EditText>(R.id.etEnvelopeDesc).text.clear()

            loadEnvelopes() // refresh list
        }
    }

    private fun loadEnvelopes() {
        val categoryDao = DatabaseProvider.getCategoryDao(this)

        lifecycleScope.launch {
            val categories = categoryDao.getCategoriesForUser(userId)

            activeContainer.removeAllViews()

            if (categories.isEmpty()) {
                activeContainer.visibility = View.GONE
                return@launch
            }

            activeContainer.visibility = View.VISIBLE

            for (cat in categories) {
                val spent = categoryDao.getSpentAmountForCategory(cat.id) ?: 0.0
                addEnvelopeToUI(cat.name, cat.description, cat.color, spent)
            }
        }
    }

    private fun addEnvelopeToUI(name: String, desc: String?, color: String, spent: Double) {
        // reference to activity_item_envelope which contains the correct IDs
        val envelopeView = layoutInflater.inflate(R.layout.activity_item_envelope, activeContainer, false)

        envelopeView.findViewById<TextView>(R.id.tvEnvelopeName).text = name
        envelopeView.findViewById<TextView>(R.id.tvEnvelopeDesc).text = desc ?: ""
        envelopeView.findViewById<TextView>(R.id.tvEnvelopeBalance).text = formatZAR(spent)
        envelopeView.findViewById<View>(R.id.viewColor).setBackgroundColor(color.toColorInt())

        activeContainer.addView(envelopeView)
    }

    private fun formatZAR(amount: Double): String {
        return String.format(Locale.getDefault(), "R%.2f", amount)
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
        // Already here
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

    fun toDashboard(view: View) {
        val intent = Intent(this, dashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent)
    }
}
