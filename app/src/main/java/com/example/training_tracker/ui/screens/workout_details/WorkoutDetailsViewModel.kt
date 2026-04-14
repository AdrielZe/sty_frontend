package com.example.training_tracker.ui.screens.workout_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.ExerciseRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val canStart: Boolean = savedStateHandle.get<String>("canStart")?.toBoolean() ?: true
    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<WorkoutDetailsUiState> = combine(
        workoutRepository.getWorkoutById(workoutId),
        exerciseRepository.exercises
    ) { workout, exercisesList ->
        WorkoutDetailsUiState(
            isLoading = false,
            workout = workout,
            canStartWorkout = canStart,
            isEditMode = !canStart,
            availableExercises = exercisesList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = WorkoutDetailsUiState(isLoading = true)
    )

    fun updateWorkoutName(name: String) {
        val currentWorkout = uiState.value.workout ?: return
        val updatedWorkout = currentWorkout.copy(name = name.uppercase())
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao atualizar nome do treino: ${e.message}")
            }
        }
    }
    fun startWorkout() {
        val currentWorkout = uiState.value.workout ?: return
        val updatedWorkout = currentWorkout.copy(isOnGoing = true)
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao iniciar treino: ${e.message}")
            }
        }
    }

    fun addExercise(name: String) {
        val currentWorkout = uiState.value.workout ?: return
        val newExercise = Exercise(name = name)
        val updatedWorkout = currentWorkout.copy(
            exercises = currentWorkout.exercises + newExercise
        )
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao adicionar exercício: ${e.message}")
            }
        }
    }

    fun removeExercise(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return
        val updatedWorkout = currentWorkout.copy(
            exercises = currentWorkout.exercises.filter { it.id != exerciseId }
        )

        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao adicionar exercício: ${e.message}")
            }
        }
    }
    fun moveExercise(fromIndex: Int, toIndex: Int) {
        val currentWorkout = uiState.value.workout ?: return
        val exercises = currentWorkout.exercises.toMutableList()
        if (fromIndex !in exercises.indices || toIndex !in exercises.indices) return

        val item = exercises.removeAt(fromIndex)
        exercises.add(toIndex, item)

        val updatedWorkout = currentWorkout.copy(exercises = exercises)
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao adicionar exercício: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val exerciseRepository = application.container.exerciseRepository
                WorkoutDetailsViewModel(
                    workoutRepository = workoutRepository,
                    exerciseRepository = exerciseRepository,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
