package com.example.training_tracker.ui.screens.registered_workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class RegisteredWorkoutsViewModel(
    private val workoutRepository: WorkoutRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    val uiState: StateFlow<RegisteredWorkoutsUiState> = workoutRepository.workouts
        .map { workouts ->
            val grouped = workouts.groupBy { it.dayOfWeek  }
            RegisteredWorkoutsUiState(workoutsByDay = grouped)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500),
            initialValue = RegisteredWorkoutsUiState()
        )

    private val _uiEvent = Channel<String>()

    fun moveWorkout(workout: Workout, newDay: DayOfWeek) {
        val updatedWorkout = workout.copy(dayOfWeek = newDay)
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao mover: ${e.message}")
            }
        }
    }

    fun duplicateWorkout(workout: Workout, targetDay: DayOfWeek) {
        viewModelScope.launch {
            val copy = workout.copy(
                id = java.util.UUID.randomUUID().toString(),
                userId = sessionManager.userIdFlow.first().toString(),
                dayOfWeek = targetDay,
                isCompleted = false,
                isOnGoing = false,
                isPaused = false,
                completionDate = null,
                completionTime = null,
                startTime = null,
                accumulatedTime = 0L,
                historyId = null,
                progress = 0f
            )

            try {
                workoutRepository.addWorkout(copy)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao duplicar: ${e.message}")
            }
        }
    }

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkoutById(workout.id)
            } catch(e: Exception){
                _uiEvent.send("Erro ao deletar: ${e.message}")
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val sessionManager = application.container.sessionManager
                RegisteredWorkoutsViewModel(workoutRepository = workoutRepository, sessionManager = sessionManager)
            }
        }
    }
}
