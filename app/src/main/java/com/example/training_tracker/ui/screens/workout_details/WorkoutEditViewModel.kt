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
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.domain.repository.ExerciseRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutEditViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val classifier: ExerciseClassifier,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val canStart: Boolean = savedStateHandle.get<String>("canStart")?.toBoolean() ?: true
    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    private val _pendingExerciseName = MutableStateFlow<String?>(null)
    private val _showMuscleGroupPicker = MutableStateFlow(false)

    val uiState: StateFlow<WorkoutEditUiState> = combine(
        workoutRepository.getWorkoutById(workoutId),
        exerciseRepository.exercises,
        _pendingExerciseName,
        _showMuscleGroupPicker
    ) { workout, exercisesList, pendingName, showMusclePicker ->
        WorkoutEditUiState(
            isLoading = false,
            workout = workout,
            canStartWorkout = canStart,
            isEditMode = !canStart,
            availableExercises = exercisesList,
            pendingExerciseName = pendingName,
            showMuscleGroupPicker = showMusclePicker
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = WorkoutEditUiState(isLoading = true)
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
                    val result = classifier.classify(nameFormatted)
                    
                    if (result.confidence < 0.85f) {
                        _pendingExerciseName.value = nameFormatted
                        _showMuscleGroupPicker.value = true
                        return@launch
                    }

                    val predictedMuscleGroup = try {
                        if (result.label != null) {
                            MuscleGroups.valueOf(result.label)
                        } else {
                            MuscleGroups.ABS
                        }
                    } catch (e: IllegalArgumentException) {
                        MuscleGroups.ABS
                    }

                    saveNewExercise(nameFormatted, predictedMuscleGroup)
                } catch (e: Exception) {
                    _pendingExerciseName.value = nameFormatted
                    _showMuscleGroupPicker.value = true
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

    fun onMuscleGroupSelected(muscleGroup: MuscleGroups) {
        val name = _pendingExerciseName.value ?: return
        viewModelScope.launch(Dispatchers.Default) {
            saveNewExercise(name, muscleGroup)
        }
    }

    fun dismissMuscleGroupPicker() {
        _showMuscleGroupPicker.value = false
        _pendingExerciseName.value = null
    }

    private suspend fun saveNewExercise(name: String, muscleGroup: MuscleGroups) {
        val currentWorkout = uiState.value.workout ?: return
        
        val newExerciseToDB = Exercise(
            name = name,
            muscleGroup = muscleGroup
        )
        exerciseRepository.addExercise(newExerciseToDB)

        val exerciseToAdd = newExerciseToDB.copy(id = java.util.UUID.randomUUID().toString())
        val updatedWorkout = currentWorkout.copy(
            exercises = currentWorkout.exercises + exerciseToAdd,
            estimatedTime = (currentWorkout.exercises.size + 1) * 10
        )
        workoutRepository.updateWorkout(updatedWorkout)
        
        _showMuscleGroupPicker.value = false
        _pendingExerciseName.value = null
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
    fun updateExerciseMuscleGroup(exerciseId: String, muscleGroup: MuscleGroups) {
        val currentWorkout = uiState.value.workout ?: return
        val derivedType = when (muscleGroup) {
            MuscleGroups.CARDIO -> ExerciseType.CARDIO
            MuscleGroups.STRETCHING -> ExerciseType.STRETCHING
            else -> ExerciseType.STRENGTH
        }
        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) exercise.copy(muscleGroup = muscleGroup, type = derivedType) else exercise
        }
        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            try {
                workoutRepository.updateWorkout(updatedWorkout)
            } catch (e: Exception) {
                _uiEvent.send("Erro ao atualizar grupo muscular: ${e.message}")
            }
        }
    }

    fun setExerciseSetCount(exerciseId: String, count: Int) {
        val clamped = count.coerceIn(1, 10)
        val currentWorkout = uiState.value.workout ?: return
        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id != exerciseId) return@map exercise
            val current = exercise.exerciseSets
            val newSets = when {
                clamped > current.size -> current + (current.size + 1..clamped).map { n -> ExerciseSet(set = n) }
                clamped < current.size -> current.take(clamped)
                else -> current
            }
            exercise.copy(exerciseSets = newSets)
        }
        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            try { workoutRepository.updateWorkout(updatedWorkout) }
            catch (e: Exception) { _uiEvent.send("Erro ao atualizar séries: ${e.message}") }
        }
    }

    fun updateExerciseSetTarget(exerciseId: String, setNumber: Int, targetReps: String, targetWeight: String) {
        val currentWorkout = uiState.value.workout ?: return
        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id != exerciseId) return@map exercise
            val newSets = exercise.exerciseSets.map { set ->
                if (set.set == setNumber) set.copy(targetReps = targetReps, targetWeight = targetWeight)
                else set
            }
            exercise.copy(exerciseSets = newSets)
        }
        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            try { workoutRepository.updateWorkout(updatedWorkout) }
            catch (e: Exception) { _uiEvent.send("Erro ao atualizar metas: ${e.message}") }
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
                WorkoutEditViewModel(
                    workoutRepository = workoutRepository,
                    exerciseRepository = exerciseRepository,
                    classifier = classifier,
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
