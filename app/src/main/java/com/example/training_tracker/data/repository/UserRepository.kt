package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class UserRepository(private val userDao: UserDao) {

    /**
     * Retorna o usuário do banco de dados.
     * Se o banco estiver vazio (o que não deve acontecer devido ao callback do AppDatabase),
     * fornece um usuário padrão para garantir que a UI sempre tenha algo para exibir.
     */
    fun getUser(): Flow<User?> {
        return userDao.getUser().map { user ->
            user ?: User(id = "default_user", name = "Adriel", nameDisplay = "Adriel")
        }
    }

    suspend fun insertUser(user: User) {
        userDao.upsertUser(user)
    }

    suspend fun updateWeeklyGoal(goal: Int) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(weeklyGoal = goal))
        } else {
            userDao.upsertUser(User(name = "Adriel", weeklyGoal = goal))
        }
    }

    suspend fun updateProfilePicture(uri: String) {
        val currentUser = userDao.getUser().first() ?: User(id = "default_user", name = "Adriel", nameDisplay = "Adriel")
        userDao.upsertUser(currentUser.copy(profilePicture = uri))
    }
}
