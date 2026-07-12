package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.internal.userAgent
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userApi: UserApi
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return userDao.getUser()
    }

    override suspend fun insertUser(user: User) {
        userDao.upsertUser(user)
    }

    override suspend fun updateWeeklyGoal(goal: Int) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(weeklyGoal = goal))
        }
    }

    override suspend fun updateProfilePicture(uri: String) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(profilePicture = uri))
        }
    }

    override suspend fun fetchProfilePictureFromDb(userId: UUID): String? {
        return try {
            val response = userApi.getUserProfilePicture(userId)
            val imageUrl = response.url

            val currentUser = userDao.getUser().first()
            if (currentUser != null) {
                userDao.upsertUser(currentUser.copy(profilePicture = imageUrl))
            }

            imageUrl
        } catch (e: HttpException) {
            if (e.code() == 404) {
                null
            } else {
                e.printStackTrace()
                null
            }
        } catch (e: IOException) {
            // Sem internet ou erro de conexão
            e.printStackTrace()
            null
        }
    }
    override suspend fun updateUserName(newName: String) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(name = newName))
        }
    }

    override suspend fun updateBodyData(weightKg: Float?, ageYears: Int?, gender: String?) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(weightKg = weightKg, ageYears = ageYears, gender = gender))
        }
    }

    override suspend fun updateAccentTheme(themeName: String) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(accentThemeName = themeName))
        }
    }

    override suspend fun updateUserId(oldLocalId: String, newId: String) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
            userDao.updateUserId(oldLocalId, newId)
        }
    }

    override suspend fun deleteAllUsers() {
        userDao.deleteAllUsers()
    }

    override suspend fun updateProfilePictureRemote(newPicture: String, id: String) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
           // userApi.getUserProfilePicture()
            userDao.updateProfilePicture(newPicture, id)
        }
    }
}