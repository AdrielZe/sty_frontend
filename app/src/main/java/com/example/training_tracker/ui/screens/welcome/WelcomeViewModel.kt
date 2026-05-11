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

class WelcomeViewModel(private val userRepository: UserRepository) : ViewModel() {
    fun saveUser(name: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val user = User(
                id = "default_user",
                name = name,
                nameDisplay = name
            )
            userRepository.insertUser(user)
            onComplete()
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                WelcomeViewModel(application.container.userRepository)
            }
        }
    }
}
