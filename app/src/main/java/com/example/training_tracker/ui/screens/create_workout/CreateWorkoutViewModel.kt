package com.example.training_tracker.ui.screens.create_workout

import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.domain.repository.ExerciseRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.UUID

class CreateWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val classifier: ExerciseClassifier,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _draftState = MutableStateFlow(CreateWorkoutUiState())

    val uiState: StateFlow<CreateWorkoutUiState> = combine(
        _draftState,
        exerciseRepository.exercises
    ) { draft, available ->
        draft.copy(availableExercises = available)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateWorkoutUiState()
    )

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()


    fun updateWorkoutName(name: String) {
        _draftState.update {
            it.copy(workoutName = name)
        }
    }

    fun updateSelectedDay(day: DayOfWeek) {
        _draftState.update {
            it.copy(selectedDay = day)
        }
    }

    fun addExercise(exerciseName: String) {
        if (exerciseName.isBlank()) return

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
                        _draftState.update {
                            it.copy(
                                pendingExerciseName = nameFormatted,
                                showMuscleGroupPicker = true
                            )
                        }
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
                    _draftState.update {
                        it.copy(
                            pendingExerciseName = nameFormatted,
                            showMuscleGroupPicker = true
                        )
                    }
                }
            }
        } else {
            val exerciseToAdd = existingExercise.copy(id = java.util.UUID.randomUUID().toString())
            _draftState.update {
                it.copy(exercises = it.exercises + exerciseToAdd)
            }
        }
    }

    fun addCardioExercise(exerciseName: String) {
        if (exerciseName.isBlank()) return

        val nameFormatted = exerciseName
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        val cardioExercises = uiState.value.availableExercises.filter { it.type == ExerciseType.CARDIO }
        val existingExercise = cardioExercises.find {
            it.name.equals(nameFormatted, ignoreCase = true)
        }

        if (existingExercise == null) {
            viewModelScope.launch(Dispatchers.Default) {
                    saveNewExercise(nameFormatted, MuscleGroups.CARDIO)
                }
        } else {
            val exerciseToAdd = existingExercise.copy(id = java.util.UUID.randomUUID().toString())
            _draftState.update {
                it.copy(exercises = it.exercises + exerciseToAdd)
            }
        }
    }

    fun addStretchingExercise(exerciseName: String) {
        if (exerciseName.isBlank()) return

        val nameFormatted = exerciseName
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        val stretchingExercises = uiState.value.availableExercises.filter { it.type == ExerciseType.STRETCHING }
        val existingExercise = stretchingExercises.find {
            it.name.equals(nameFormatted, ignoreCase = true)
        }

        if (existingExercise == null) {
            viewModelScope.launch(Dispatchers.Default) {
                saveNewExercise(nameFormatted, MuscleGroups.STRETCHING)
            }
        } else {
            val exerciseToAdd = existingExercise.copy(id = java.util.UUID.randomUUID().toString())
            _draftState.update {
                it.copy(exercises = it.exercises + exerciseToAdd)
            }
        }
    }

    fun onMuscleGroupSelected(muscleGroup: MuscleGroups) {
        val name = uiState.value.pendingExerciseName ?: return
        viewModelScope.launch(Dispatchers.Default) {
            saveNewExercise(name, muscleGroup)
        }
    }

    fun dismissMuscleGroupPicker() {
        _draftState.update {
            it.copy(
                showMuscleGroupPicker = false,
                pendingExerciseName = null
            )
        }
    }

    private suspend fun saveNewExercise(name: String, muscleGroup: MuscleGroups) {
        val type = when (muscleGroup) {
            MuscleGroups.CARDIO -> ExerciseType.CARDIO
            MuscleGroups.STRETCHING -> ExerciseType.STRETCHING
            else -> ExerciseType.STRENGTH
        }
        val newExerciseToDB = Exercise(
            name = name,
            muscleGroup = muscleGroup,
            type = type
        )
        exerciseRepository.addExercise(newExerciseToDB)

        val exerciseToAdd = newExerciseToDB.copy(id = java.util.UUID.randomUUID().toString())
        _draftState.update {
            it.copy(
                exercises = it.exercises + exerciseToAdd,
                showMuscleGroupPicker = false,
                pendingExerciseName = null
            )
        }
    }

    fun toggleExerciseExpanded(exerciseId: String) {
        _draftState.update {
            it.copy(expandedExerciseId = if (it.expandedExerciseId == exerciseId) null else exerciseId)
        }
    }

    fun setExerciseSetCount(exerciseId: String, count: Int) {
        val clamped = count.coerceIn(1, 10)
        _draftState.update { state ->
            val exercises = state.exercises.map { exercise ->
                if (exercise.id != exerciseId) return@map exercise
                val current = exercise.exerciseSets
                val newSets = when {
                    clamped > current.size -> current + (current.size + 1..clamped).map { n ->
                        ExerciseSet(set = n, exerciseId = UUID.fromString(exercise.id))
                    }
                    clamped < current.size -> current.take(clamped)
                    else -> current
                }
                exercise.copy(exerciseSets = newSets)
            }
            state.copy(exercises = exercises)
        }
    }

    fun updateExerciseSetTarget(exerciseId: String, setNumber: Int, targetReps: String, targetWeight: String) {
        _draftState.update { state ->
            val exercises = state.exercises.map { exercise ->
                if (exercise.id != exerciseId) return@map exercise
                val newSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) set.copy(targetReps = targetReps, targetWeight = targetWeight)
                    else set
                }
                exercise.copy(exerciseSets = newSets)
            }
            state.copy(exercises = exercises)
        }
    }

    fun removeExercise(exercise: Exercise) {
        _draftState.update {
            it.copy(exercises = it.exercises - exercise)
        }

        viewModelScope.launch {
            try {
                val exerciseInDb = uiState.value.availableExercises.find {
                    it.name.equals(exercise.name, ignoreCase = true)
                }

                exerciseInDb?.let {
                    if (!it.isDefault) {
                        exerciseRepository.deleteExercise(it)
                    }
                }
            } catch (e: Exception) {
                _uiEvent.send("Erro ao excluir do banco: ${e.message}")
            }
        }
    }

    fun saveWorkout() {
        if (_draftState.value.canSave) {
            viewModelScope.launch {
                val currentState = _draftState.value
                val newWorkout = Workout(
                    name = currentState.workoutName,
                    userId = sessionManager.userIdFlow.first().toString(),
                    exercises = currentState.exercises,
                    dayOfWeek = currentState.selectedDay
                )

                try {
                    workoutRepository.addWorkout(workout = newWorkout)
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e("TREINO", "ERRO O TREINO: ", e)
                    _uiEvent.send("Erro ao salvar o treino: ${e.message}")
                }
            }
            _draftState.update { it.copy(isWorkoutSaved = true) }
        } else {
            _draftState.update { it.copy(showErrors = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val exerciseRepository = application.container.exerciseRepository
                val classifier = application.container.exerciseClassifier
                val sessionManager = application.container.sessionManager
                CreateWorkoutViewModel(workoutRepository = workoutRepository, exerciseRepository = exerciseRepository, sessionManager = sessionManager, classifier= classifier)
            }
        }
    }
}