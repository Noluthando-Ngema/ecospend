package com.example.ecospend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GoalDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Query("SELECT * FROM goals WHERE userId = :userId")
    suspend fun getGoalsForUser(userId: Long): List<Goal>

    @Query("SELECT * FROM goals WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getGoalForCategory(categoryId: Long): Goal?

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoal(goalId: Long)
}
