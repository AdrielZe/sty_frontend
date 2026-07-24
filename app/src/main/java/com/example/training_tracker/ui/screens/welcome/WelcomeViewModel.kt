package com.example.training_tracker.ui.screens.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.training_tracker.data.models.User
import com.example.training_tracker.domain.repository.UserRepository
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.flow.first
import java.util.UUID

class WelcomeViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    fun saveUser(name: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val user = User(
                name = name,
                nameDisplay = name
            )
            userRepository.insertUser(user)
            sessionManager.saveUnloggedSession(UUID.fromString(userRepository.getUser().first()?.id))
            onComplete()
        }

    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                WelcomeViewModel(application.container.userRepository, application.container.sessionManager)
            }
        }
    }
}
