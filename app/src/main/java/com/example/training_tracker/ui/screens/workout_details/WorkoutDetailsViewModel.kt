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
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.ExerciseRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutDetailsViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val classifier: ExerciseClassifier,
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
        val updatedWorkout = currentWorkout.copy(
            isOnGoing = true,
            startTime = System.currentTimeMillis()
        )
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao iniciar treino: ${e.message}")
            }
        }
    }

    fun addExercise(exerciseName: String) {
        if (exerciseName.isBlank()) return
        val currentWorkout = uiState.value.workout ?: return

        val nameFormatted = exerciseName
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        val existingExercise = uiState.value.availableExercises.find {
            it.name.equals(nameFormatted, ignoreCase = true)
        }

        if (existingExercise == null) {
            viewModelScope.launch(Dispatchers.Default) {
                try {
                    val predictedMuscleKey = classifier.classify(nameFormatted)
                    val predictedMuscleGroup = when (predictedMuscleKey) {
                        "peito" -> MuscleGroups.CHEST
                        "costas" -> MuscleGroups.BACK
                        "perna" -> MuscleGroups.LEGS
                        "ombro" -> MuscleGroups.SHOULDERS
                        "biceps" -> MuscleGroups.BICEPS
                        "triceps" -> MuscleGroups.TRICEPS
                        "abdomen" -> MuscleGroups.ABS
                        else -> MuscleGroups.ABS
                    }

                    val newExerciseToDB = Exercise(
                        name = nameFormatted,
                        muscleGroup = predictedMuscleGroup
                    )
                    exerciseRepository.addExercise(newExerciseToDB)

                    val exerciseToAdd = newExerciseToDB.copy(id = java.util.UUID.randomUUID().toString())
                    val updatedWorkout = currentWorkout.copy(
                        exercises = currentWorkout.exercises + exerciseToAdd,
                        estimatedTime = (currentWorkout.exercises.size + 1) * 10
                    )
                    workoutRepository.updateWorkout(updatedWorkout)
                } catch (e: Exception) {
                    _uiEvent.send("Erro ao classificar exercício: ${e.message}")
                }
            }
        } else {
            val exerciseToAdd = existingExercise.copy(id = java.util.UUID.randomUUID().toString())
            val newExercises = currentWorkout.exercises + exerciseToAdd
            val updatedWorkout = currentWorkout.copy(
                exercises = newExercises,
                estimatedTime = newExercises.size * 10
            )
            viewModelScope.launch {
                try {
                    workoutRepository.updateWorkout(updatedWorkout)
                } catch (e: Exception) {
                    _uiEvent.send("Erro ao adicionar exercício: ${e.message}")
                }
            }
        }
    }

    fun removeExercise(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return
        val newExercises = currentWorkout.exercises.filter { it.id != exerciseId }
        val updatedWorkout = currentWorkout.copy(
            exercises = newExercises,
            estimatedTime = newExercises.size * 10
        )

        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao remover exercício: ${e.message}")
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
                _uiEvent.send("Erro ao mover exercício: ${e.message}")
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val exerciseRepository = application.container.exerciseRepository
                val classifier = application.container.exerciseClassifier
                WorkoutDetailsViewModel(
                    workoutRepository = workoutRepository,
                    exerciseRepository = exerciseRepository,
                    classifier = classifier,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
