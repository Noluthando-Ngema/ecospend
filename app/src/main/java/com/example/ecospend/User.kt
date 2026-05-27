package com.example.ecospend


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val surname: String,
    val email: String,
    val passwordHash: String,
    val startingBalance: Double = 42760.98, // default for demo
    val currency: String = "ZAR",
    val createdAt: Long = System.currentTimeMillis()
)