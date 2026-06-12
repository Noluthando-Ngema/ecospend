package com.example.ecospend

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, Category::class, Expense::class, Goal::class, Reward::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDAO
    abstract fun categoryDao(): CategoryDAO
    abstract fun expenseDao(): ExpenseDAO
    abstract fun goalDao(): GoalDAO
    abstract fun rewardDao(): RewardDAO
}
