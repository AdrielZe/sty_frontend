package com.example.training_tracker.ui.screens.login

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.remote.auth.AuthApi
import com.example.training_tracker.data.remote.auth.LoginRequest
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent
import java.util.UUID

class LoginViewModel(
    private val sessionManager: SessionManager,
    private val authApi: AuthApi,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChanged(user: String) {
        _uiState.update { it.copy(user = user, errorMessage = null) }
    }

    fun onSenhaChanged(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun onLoginClick() {
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val user = userRepository.getUser().first()!!;
                val response = authApi.login(LoginRequest(currentState.user, currentState.password))

                val returnedUuid = response.id

                sessionManager.saveSession(userId = returnedUuid)

                userRepository.updateUserId(user.id, returnedUuid.toString())



                val userAtualizado = userRepository.getUser().first()

                // 4. Agora sim você vai ver o ID novo impresso!
                println("ID DEPOIS do update: ${userAtualizado?.id}")



                _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Credenciais inválidas")
                }
            }
        }
    }

    // Chamado após a navegação para resetar o evento de sucesso
    fun onLoginHandled() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                LoginViewModel(
                    sessionManager = application.container.sessionManager,
                    authApi = application.container.authApi,
                    userRepository = application.container.userRepository
                )
            }
        }
    }
}