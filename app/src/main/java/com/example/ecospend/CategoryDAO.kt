package com.example.ecospend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CategoryDAO {
    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY createdAt DESC")
    suspend fun getCategoriesForUser(userId: Long): List<Category>

    @Query("SELECT SUM(e.amount) FROM expenses e WHERE e.categoryId = :categoryId")
    suspend fun getSpentAmountForCategory(categoryId: Long): Double?
}
