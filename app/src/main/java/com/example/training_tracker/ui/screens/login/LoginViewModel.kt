package com.example.training_tracker.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChanged(usuario: String) {
        _uiState.update { it.copy(usuario = usuario, errorMessage = null) }
    }

    fun onSenhaChanged(senha: String) {
        _uiState.update { it.copy(senha = senha, errorMessage = null) }
    }

    fun onLoginClick() {
        val currentState = _uiState.value

        // Validação básica
        if (currentState.usuario.isBlank() || currentState.senha.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Preencha todos os campos") }
            return
        }

        // Simulação de chamada de rede
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            delay(1500) // Simulando um delay de API

            // Lógica de sucesso (substitua pela sua regra real)
            if (currentState.usuario == "admin" && currentState.senha == "1234") {
                _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Usuário ou senha incorretos"
                    )
                }
            }
        }
    }

    // Chamado após a navegação para resetar o evento de sucesso
    fun onLoginHandled() {
        _uiState.update { it.copy(isLoginSuccessful = false) }
    }
}