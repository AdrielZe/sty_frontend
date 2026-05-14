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
    val weeklyGoal: Int? = null,
    @androidx.room.ColumnInfo("weightKg") val weightKg: Float? = null,
    @androidx.room.ColumnInfo("ageYears") val ageYears: Int? = null,
    @androidx.room.ColumnInfo("gender") val gender: String? = null, // "MALE" | "FEMALE" | "OTHER"
)
