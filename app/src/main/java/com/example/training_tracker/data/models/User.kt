package com.example.training_tracker.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = "default_user",
    val name: String,
    val profilePicture: String? = null,
    val nameDisplay: String? = null,
    val weeklyGoal: Int? = null
)
