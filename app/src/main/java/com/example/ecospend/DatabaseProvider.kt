package com.example.ecospend

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        if (db == null) {
            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "ecospend_db"
            ).fallbackToDestructiveMigration() // added for version update
            .build()
        }
        return db!!
    }

    fun getUserDao(context: Context): UserDAO {
        return getDatabase(context).userDao()
    }

    fun getCategoryDao(context: Context): CategoryDAO {
        return getDatabase(context).categoryDao()
    }

    fun getExpenseDao(context: Context): ExpenseDAO {
        return getDatabase(context).expenseDao()
    }

    fun getGoalDao(context: Context): GoalDAO {
        return getDatabase(context).goalDao()
    }

    fun getRewardDao(context: Context): RewardDAO {
        return getDatabase(context).rewardDao()
    }
}
