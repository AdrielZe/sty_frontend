package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

interface UserRepository {
    fun getUser(): Flow<User?>
    suspend fun insertUser(user: User)
    suspend fun updateWeeklyGoal(goal: Int)
    suspend fun updateProfilePicture(uri: String)
    suspend fun updateUserName(newName: String)
    suspend fun updateBodyData(weightKg: Float?, ageYears: Int?, gender: String?)
    suspend fun updateAccentTheme(themeName: String)
    suspend fun fetchProfilePictureFromDb(userId: UUID): String
}
