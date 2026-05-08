package com.example.training_tracker.ui.screens.create_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Workout
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class CreateWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val classifier: ExerciseClassifier
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

                    val predictedMuscleGroup = when (result.label) {
                        "peito" -> MuscleGroups.CHEST
                        "costas" -> MuscleGroups.BACK
                        "perna" -> MuscleGroups.LEGS
                        "ombro" -> MuscleGroups.SHOULDERS
                        "biceps" -> MuscleGroups.BICEPS
                        "triceps" -> MuscleGroups.TRICEPS
                        "abdomen" -> MuscleGroups.ABS
                        else -> MuscleGroups.ABS
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
        val newExerciseToDB = Exercise(
            name = name,
            muscleGroup = muscleGroup
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
            val currentState = _draftState.value
            val newWorkout = Workout(
                name = currentState.workoutName,
                exercises = currentState.exercises,
                dayOfWeek = currentState.selectedDay
            )
            viewModelScope.launch {
                try {
                    workoutRepository.addWorkout(newWorkout)
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
                CreateWorkoutViewModel(workoutRepository = workoutRepository, exerciseRepository = exerciseRepository, classifier= classifier)
            }
        }
    }
}