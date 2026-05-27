package com.example.ecospend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RewardDAO {
    @Insert
    suspend fun insertReward(reward: Reward)

    @Query("SELECT * FROM rewards WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getRewardsForUser(userId: Long): List<Reward>

    @Query("SELECT SUM(points) FROM rewards WHERE userId = :userId")
    suspend fun getTotalPoints(userId: Long): Int?
}
