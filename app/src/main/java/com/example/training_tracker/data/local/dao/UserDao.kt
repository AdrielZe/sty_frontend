package com.example.training_tracker.data.local.dao

import android.media.quality.PictureProfile
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.training_tracker.data.models.User
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<User?>

    @Upsert
    suspend fun upsertUser(user: User)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Query("UPDATE users SET weeklyGoal = :goal WHERE id = :userId")
    suspend fun updateWeeklyGoal(userId: String, goal: Int)

    @Query("UPDATE users SET id = :newId WHERE id = :OldLocalId")
    suspend fun updateUserId(OldLocalId: String, newId: String)

    @Query("UPDATE users SET profilePicture = :newProfilePicture WHERE id= :userId")
    suspend fun updateProfilePicture(newProfilePicture: String, userId: String)

}