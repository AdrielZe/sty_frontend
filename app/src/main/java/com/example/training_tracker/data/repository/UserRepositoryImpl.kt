package com.example.training_tracker.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.training_tracker.data.local.dao.UserDao
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.remote.sync.SyncConfiguration
import com.example.training_tracker.data.remote.sync.SyncDataWorker
import com.example.training_tracker.data.remote.sync.SyncManager
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.data.remote.user.UserProfileResponse
import com.example.training_tracker.data.remote.user.WeeklyGoalRequestDto
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.internal.userAgent
import retrofit2.HttpException
import java.io.File
import java.io.IOException
import java.util.UUID

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val userApi: UserApi,
    private val sessionManager: SessionManager,
    private val syncManager: SyncManager,
    private val context: Context
) : UserRepository {

    override fun getUser(): Flow<User?> {
        return userDao.getUser()
    }

    override suspend fun insertUser(user: User) {
        userDao.upsertUser(user)
    }

    override suspend fun updateWeeklyGoal(goal: Int) {
        val currentUser = userDao.getUser().firstOrNull() ?: return
        val updatedUser = currentUser.copy(weeklyGoal = goal, isSynced = false)

        userDao.upsertUser(updatedUser)

        if (sessionManager.isLoggedIn.first()) {
            try {
                userApi.setUserWeeklyGoal(UUID.fromString(updatedUser.id), WeeklyGoalRequestDto(weeklyGoal = goal))
                userDao.upsertUser(updatedUser.copy(isSynced = true))
                Log.d("Update Weekly Goal", "User Weekly Goal was updated successfully.")
            } catch (e: Exception) {
                Log.e("Update Weekly Goal", "API failed. Data is safe locally and will sync later.", e)
                syncManager.scheduleGlobalSync()
            }
        }
    }

    override suspend fun updateProfilePicture(uriString: String) {
        val currentUser = userDao.getUser().firstOrNull() ?: return
        userDao.upsertUser(currentUser.copy(profilePicture = uriString))

        if (sessionManager.isLoggedIn.first()) {
            try {
                val uri = Uri.parse(uriString)

                val file = getFileFromUri(context, uri)

                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                userApi.updateUserProfilePicture(UUID.fromString(currentUser.id), body)
                file.delete()
            } catch (e: Exception) {
                syncManager.scheduleGlobalSync()
            }
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
            e.printStackTrace()
            null
        }
    }

    override suspend fun fetchUserProfileFromRemote(userId: UUID) {
       try {
           val profile: UserProfileResponse = userApi.getUserProfile(userId)
           Log.d("Fetch user profile", "profile fetched successfully $profile")
           updateLocalData(profile)
        } catch (e: Exception) {
            Log.e("ERROR", "AN ERROR HAS OCCURED", e)
            null
        }
    }

    override suspend fun updateLocalUsername(newName: String) {
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

    override suspend fun updateLocalProfilePicture(newPicture: String) {
        val currentUser = userDao.getUser().first()
        if (currentUser != null) {
           // userApi.getUserProfilePicture()
            userDao.updateProfilePicture(newPicture, currentUser.id)
        }
    }

    override suspend fun updateLocalData(userResponse: UserProfileResponse) {
        val currentUser = getUser().first()
        if (currentUser != null) {
            userDao.upsertUser(currentUser.copy(
                name = userResponse.username,
                profilePicture = userResponse.picture,
                weeklyGoal = userResponse.weeklyGoal
            ))
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("Não foi possível acessar a imagem")

        val tempFile = File(context.cacheDir, "profile_pic_${System.currentTimeMillis()}.jpg")

        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return tempFile
    }

}