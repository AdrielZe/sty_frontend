package com.example.training_tracker.ui.screens.registered_workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class RegisteredWorkoutsViewModel(
    private val workoutRepository: WorkoutRepository
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
    val uiEvent = _uiEvent.receiveAsFlow()

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

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            try {
                workoutRepository.deleteWorkout(workout)
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
                RegisteredWorkoutsViewModel(workoutRepository = workoutRepository)
            }
        }
    }
}
