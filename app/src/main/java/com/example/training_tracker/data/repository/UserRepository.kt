package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class UserRepository(private val userDao: UserDao) {

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
        } else {
            // Se por algum motivo o usuário não existir, cria um novo com o goal
            userDao.upsertUser(User(name = "Adriel", weeklyGoal = goal))
        }
    }
}