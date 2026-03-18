package com.example.training_tracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.repository.UserRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val uiState = combine(
        userRepository.getUser(),
        workoutRepository.getTodayWorkout()
    ) {
        user, workout ->
        HomeUiState(
            user = user,
            currentDate = getCurrentDate(),
            todayWorkout = workout
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = HomeUiState()
    )
    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("pt", "BR"))
        return LocalDate.now().format(formatter)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                // Pega a instância da nossa GymTrackerApplication
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)

                // Pega o repositório único que está dentro do container
                val userRepository = application.container.userRepository
                val workoutRepository = application.container.workoutRepository

                HomeViewModel(userRepository = userRepository, workoutRepository = workoutRepository )
            }
        }
    }
}