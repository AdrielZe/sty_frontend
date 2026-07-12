package com.example.training_tracker.ui.screens.login

data class LoginUiState(
    val usuario: String = "",
    val senha: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false
)