package com.example.ecospend

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {
    @Insert
    suspend fun insertExpense(expense: Expense): Long

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId")
    suspend fun getTotalSpent(userId: Long): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date >= :startTime AND date <= :endTime")
    suspend fun getPeriodTotal(userId: Long, startTime: Long, endTime: Long): Double?

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getExpensesForUser(userId: Long): Flow<List<Expense>>

    @Query("""
        SELECT c.id as categoryId, c.name, c.color, 
               SUM(e.amount) as totalSpent
        FROM expenses e
        INNER JOIN categories c ON e.categoryId = c.id
        WHERE e.userId = :userId
        GROUP BY c.id
        ORDER BY totalSpent DESC
    """)
    suspend fun getSpendByCategory(userId: Long): List<CategorySpend>
}

data class CategorySpend(
    val categoryId: Long,
    val name: String,
    val color: String?,
    val totalSpent: Double
)
