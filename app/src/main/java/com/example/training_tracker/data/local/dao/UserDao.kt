package com.example.training_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.training_tracker.data.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = 'default_user' LIMIT 1")
    fun getUser(): Flow<User?>

    @Upsert
    suspend fun upsertUser(user: User)

    @Query("UPDATE users SET weeklyGoal = :goal WHERE id = 'default_user'")
    suspend fun updateWeeklyGoal(goal: Int)
}