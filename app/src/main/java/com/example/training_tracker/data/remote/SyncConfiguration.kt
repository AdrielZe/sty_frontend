package com.example.training_tracker.data.remote

import com.example.training_tracker.data.remote.auth.AuthApi
import com.example.training_tracker.data.remote.auth.LoginRequest
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.first
import java.util.UUID

data class SyncConfiguration(
    private val userRepository: UserRepository,
    private val authApi: AuthApi,
    private val userApi: UserApi
) {
    suspend operator fun invoke(oldId: String, newId: String) {
      //  val user = userRepository.getUser().first()
        userRepository.updateUserId(oldId, newId)
        userRepository.fetchUserProfileFromRemote(UUID.fromString(newId))
    }

}