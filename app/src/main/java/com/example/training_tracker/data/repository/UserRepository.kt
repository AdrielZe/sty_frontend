package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserRepository(private val userDao: UserDao) {

    /**
     * Retorna o usuário do banco de dados.
     * Se for nulo, significa que o usuário ainda não realizou o onboarding.
     */
    fun getUser(): Flow<User?> {
        return userDao.getUser()
    }

    suspend fun insertUser(user: User) {
        userDao.upsertUser(user)
    }

    suspend fun updateWeeklyGoal(goal: Int) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(weeklyGoal = goal))
        }
    }

    suspend fun updateProfilePicture(uri: String) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(profilePicture = uri))
        }
    }
}
