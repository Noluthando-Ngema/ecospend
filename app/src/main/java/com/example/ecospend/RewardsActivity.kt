package com.example.ecospend

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class RewardsActivity : AppCompatActivity() {
    private lateinit var rewardsContainer: LinearLayout
    private lateinit var tvTotalPoints: TextView
    private var userId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rewards)

        val mainView = findViewById<View>(R.id.main)
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        rewardsContainer = findViewById(R.id.rewardsContainer)
        tvTotalPoints = findViewById(R.id.tvTotalPoints)

        userId = getSharedPreferences("ecospend_prefs", MODE_PRIVATE).getLong("user_id", -1)
        if (userId == -1L) {
            finish()
            return
        }

        loadRewards()
    }

    private fun loadRewards() {
        val rewardDao = DatabaseProvider.getRewardDao(this)
        lifecycleScope.launch {
            val totalPoints = rewardDao.getTotalPoints(userId) ?: 0
            tvTotalPoints.text = totalPoints.toString()

            val rewards = rewardDao.getRewardsForUser(userId)
            rewardsContainer.removeAllViews()

            for (reward in rewards) {
                val rewardView = layoutInflater.inflate(R.layout.activity_item_envelope, rewardsContainer, false)
                rewardView.findViewById<TextView>(R.id.tvEnvelopeName).text = reward.title
                rewardView.findViewById<TextView>(R.id.tvEnvelopeDesc).text = reward.description
                rewardView.findViewById<TextView>(R.id.tvEnvelopeBalance).text = "+${reward.points} pts"
                rewardView.findViewById<View>(R.id.viewColor).setBackgroundColor(resources.getColor(R.color.ecospend_dark_green, theme))
                
                rewardsContainer.addView(rewardView)
            }
        }
    }

    fun toDashboard(view: View) { startActivity(Intent(this, dashboard::class.java)) }
    fun toExpenses(view: View) { startActivity(Intent(this, expenditure::class.java)) }
    fun toEnvelopes(view: View) { startActivity(Intent(this, envelopes::class.java)) }
    fun toTracking(view: View) { startActivity(Intent(this, tracking::class.java)) }
    fun toInvest(view: View) { startActivity(Intent(this, invest::class.java)) }
    fun toProfile(view: View) { startActivity(Intent(this, profile::class.java)) }
}
