package com.example.training_tracker.session_manager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val historyRepository: WorkoutHistoryRepository
    // adicionar mais(workout, exercises, etc)
) : ViewModel() {

    private val _navigateToWorkout = Channel<String>()
    val navigateToWorkout = _navigateToWorkout.receiveAsFlow()

    fun onNotificationWorkoutTapped(workoutId: String) {
        viewModelScope.launch {
            _navigateToWorkout.send(workoutId)
        }
    }

    val isLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedIn
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    suspend fun logout() {
        sessionManager.clearSession()
        userRepository.deleteAllUsers()
        workoutRepository.deleteAllWorkouts()
        historyRepository.deleteAll()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as GymTrackerApplication

                val sessionManager = application.container.sessionManager
                val userRepository = application.container.userRepository
                val workoutRepository = application.container.workoutRepository
                val historyRepository = application.container.workoutHistoryRepository

                return MainViewModel(sessionManager, userRepository, workoutRepository, historyRepository) as T
            }
        }
    }
}