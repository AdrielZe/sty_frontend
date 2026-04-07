package com.example.training_tracker.ui.screens.workout_screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.WorkoutRepository
import com.example.training_tracker.ui.screens.home.HomeViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {
    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])


    val uiState = workoutRepository.getWorkoutById(workoutId)
        .map { workout ->
            WorkoutUiState(
                workout = workout
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = WorkoutUiState()
        )

    fun addNewSetLine(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExerciseSetLine = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val maxSetNumber = exercise.exerciseSets.maxOfOrNull { it.set } ?: 0

                val updatedSetList = exercise.exerciseSets + ExerciseSet(set = maxSetNumber + 1)

                exercise.copy(exerciseSets = updatedSetList)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExerciseSetLine)
        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun removeSetLine(exerciseId: String, setNumber: Int) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedExercisesSet = exercise.exerciseSets.filter { it.set != setNumber}
                exercise.copy(exerciseSets = updatedExercisesSet)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedExerciseSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        set.copy(isCompleted = true)
                    } else {
                        set
                    }
                }
                exercise.copy(exerciseSets = updatedExerciseSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun completeExercise(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExerciseList = currentWorkout.exercises.map {exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    set.copy(isCompleted = true)
                }

                exercise.copy(isCompleted = true, exerciseSets = updatedSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExerciseList)

        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun completeWorkout() {
            val currentWorkout = uiState.value.workout ?: return

            val updatedExercises = currentWorkout.exercises.map { exercise ->
                val updatedSets = exercise.exerciseSets.map { set ->
                    set.copy(isCompleted = true)
                }
                exercise.copy(
                    isCompleted = true,
                    exerciseSets = updatedSets
                )
            }
            val completedWorkout = currentWorkout.copy(
                exercises = updatedExercises,
                isCompleted = true,
                completionDate = System.currentTimeMillis()
            )

        viewModelScope.launch {
            workoutRepository.updateWorkout(completedWorkout)
        }
         //   workoutRepository.saveWorkoutToHistory(completedWorkout)

    }


    fun updateExercise(exerciseId: String, setNumber: Int ?= 1, newReps: String? = null, newWeight: String? = null) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        set.copy(
                            reps = newReps ?: set.reps,
                            weight = newWeight ?: set.weight
                        )
                    } else {
                        set
                    }
                }

                exercise.copy(exerciseSets = updatedSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    private fun updateCurrentWorkout(updateAction: (Workout) -> Workout) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedWorkout = updateAction(currentWorkout)

        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository

                val savedStateHandle = createSavedStateHandle()

                WorkoutViewModel(savedStateHandle = savedStateHandle, workoutRepository = workoutRepository )
            }
        }
    }
}
