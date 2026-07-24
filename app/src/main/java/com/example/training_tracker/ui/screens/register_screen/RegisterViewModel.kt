package com.example.training_tracker.ui.screens.register_screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import retrofit2.HttpException
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.remote.auth.AuthApi
import com.example.training_tracker.data.remote.auth.RegisterRequest
import com.example.training_tracker.data.remote.sync.SyncConfiguration
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class RegisterViewModel(
    private val authApi: AuthApi,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val syncConfiguration: SyncConfiguration
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onRegisterClick() {
        val currentState = _uiState.value

        if (currentState.email.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val localUser = userRepository.getUser().first()!!

                authApi.register(
                    RegisterRequest(
                        id = UUID.fromString(localUser.id),
                        name = localUser.name,
                        email = currentState.email,
                        password = currentState.password
                    )
                )

                syncConfiguration.syncPendingWorkouts()

                _uiState.update { it.copy(isLoading = false, isRegisterSuccessful = true) }

            } catch (e: Exception) {
                val resolvedErrorMessage = when (e) {
                    is HttpException -> {
                        val errorBody = e.response()?.errorBody()?.string()
                        val errorCode = e.code()

                        println("DEBUG RETROFIT: Code: $errorCode | Body: $errorBody")

                        when (errorCode) {
                            400 -> "Invalid data provided."
                            409 -> "Email already registered."
                            in 500..599 -> "Server error. Please try again later."
                            else -> "An unexpected network error occurred (Code: $errorCode). Detalhe: $errorBody"
                        }
                    }
                    is IOException -> {
                        "No internet connection or server is unreachable."
                    }
                    else -> {
                        "An unexpected application error occurred: ${e.message}"
                    }
                }

                e.printStackTrace()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = resolvedErrorMessage
                    )
                }
            }
        }
    }

    fun onRegisterHandled() {
        _uiState.update { it.copy(isRegisterSuccessful = false) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                RegisterViewModel(
                    authApi = application.container.authApi,
                    userRepository = application.container.userRepository,
                    workoutRepository = application.container.workoutRepository,
                    syncConfiguration = application.container.syncConfiguration
                )
            }
        }
    }
}