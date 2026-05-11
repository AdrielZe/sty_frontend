package com.example.training_tracker.ui.screens.freestyle_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.data.repository.ExerciseRepository
import com.example.training_tracker.data.repository.RecordsRepository
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class FreestyleWorkoutViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val recordsRepository: RecordsRepository,
    private val classifier: ExerciseClassifier
) : ViewModel() {

    private val _uiState = MutableStateFlow(FreestyleWorkoutUiState())
    val uiState = combine(
        _uiState,
        exerciseRepository.exercises
    ) { state, availableExercises ->
        state.copy(availableExercises = availableExercises)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FreestyleWorkoutUiState()
    )

    private val _navigateToReport = MutableStateFlow<String?>(null)
    val navigateToReport = _navigateToReport.asStateFlow()

    fun onNavigatedToReport() {
        _navigateToReport.value = null
    }

    fun togglePauseWorkout() {
        _uiState.update { state ->
            val currentWorkout = state.workout
            val now = System.currentTimeMillis()
            val updatedWorkout = if (currentWorkout.isPaused) {
                currentWorkout.copy(isPaused = false, startTime = now)
            } else {
                val elapsedSinceStart = if (currentWorkout.startTime != null) now - currentWorkout.startTime else 0L
                currentWorkout.copy(
                    isPaused = true,
                    accumulatedTime = currentWorkout.accumulatedTime + elapsedSinceStart,
                    startTime = null
                )
            }
            state.copy(workout = updatedWorkout)
        }
    }

    fun showExercisePicker(show: Boolean) {
        _uiState.update { it.copy(showExercisePicker = show) }
    }

    fun addExerciseByName(exerciseName: String) {
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
                    val predictedMuscleKey = classifier.classify(nameFormatted)
                    val predictedMuscleGroup = try {
                        if (result.label != null) {
                            MuscleGroups.valueOf(result.label)
                        } else {
                            MuscleGroups.ABS
                        }
                    } catch (e: IllegalArgumentException) {
                        MuscleGroups.ABS
                    }

                    val newExerciseToDB = Exercise(
                        name = nameFormatted,
                        muscleGroup = predictedMuscleGroup
                    )
                    exerciseRepository.addExercise(newExerciseToDB)

                    val exerciseToAdd = newExerciseToDB.copy(
                        id = UUID.randomUUID().toString(),
                        exerciseSets = listOf(ExerciseSet(set = 1)),
                        isCompleted = false
                    )
                    
                    _uiState.update { state ->
                        state.copy(
                            workout = state.workout.copy(exercises = state.workout.exercises + exerciseToAdd),
                            showExercisePicker = false
                        )
                    }
                } catch (e: Exception) {
                    // Handle error if needed
                }
            }
        } else {
            val exerciseToAdd = existingExercise.copy(
                id = UUID.randomUUID().toString(),
                exerciseSets = listOf(ExerciseSet(set = 1)),
                isCompleted = false
            )
            _uiState.update { state ->
                state.copy(
                    workout = state.workout.copy(exercises = state.workout.exercises + exerciseToAdd),
                    showExercisePicker = false
                )
            }
        }
    }

    fun addExerciseToWorkout(baseExercise: Exercise) {
        addExerciseByName(baseExercise.name)
    }

    fun addNewSetLine(exerciseId: String) {
        _uiState.update { state ->
            val updatedExercises = state.workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    val maxSetNumber = exercise.exerciseSets.maxOfOrNull { it.set } ?: 0
                    exercise.copy(exerciseSets = exercise.exerciseSets + ExerciseSet(set = maxSetNumber + 1))
                } else exercise
            }
            state.copy(workout = state.workout.copy(exercises = updatedExercises))
        }
    }

    fun removeSetLine(exerciseId: String, setNumber: Int) {
        _uiState.update { state ->
            val updatedExercises = state.workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    exercise.copy(exerciseSets = exercise.exerciseSets.filter { it.set != setNumber })
                } else exercise
            }
            state.copy(workout = state.workout.copy(exercises = updatedExercises))
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int) {
        _uiState.update { state ->
            val updatedExercises = state.workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    val updatedSets = exercise.exerciseSets.map { set ->
                        if (set.set == setNumber) set.copy(isCompleted = true) else set
                    }
                    exercise.copy(exerciseSets = updatedSets)
                } else exercise
            }
            state.copy(workout = state.workout.copy(exercises = updatedExercises))
        }
    }

    fun updateExercise(exerciseId: String, setNumber: Int, newReps: String? = null, newWeight: String? = null) {
        _uiState.update { state ->
            val updatedExercises = state.workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    val updatedSets = exercise.exerciseSets.map { set ->
                        if (set.set == setNumber) {
                            set.copy(reps = newReps ?: set.reps, weight = newWeight ?: set.weight)
                        } else set
                    }
                    exercise.copy(exerciseSets = updatedSets)
                } else exercise
            }
            state.copy(workout = state.workout.copy(exercises = updatedExercises))
        }
    }

    fun completeExercise(exerciseId: String) {
        _uiState.update { state ->
            val updatedExercises = state.workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    exercise.copy(isCompleted = true, exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = true) })
                } else exercise
            }
            state.copy(workout = state.workout.copy(exercises = updatedExercises))
        }
    }

    fun reopenExercise(exerciseId: String) {
        _uiState.update { state ->
            val updatedExercises = state.workout.exercises.map { exercise ->
                if (exercise.id == exerciseId) {
                    exercise.copy(isCompleted = false, exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = false) })
                } else exercise
            }
            state.copy(workout = state.workout.copy(exercises = updatedExercises))
        }
    }

    fun completeWorkout() {
        val currentWorkout = _uiState.value.workout
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val duration = currentWorkout.accumulatedTime + if (currentWorkout.startTime != null) now - currentWorkout.startTime else 0L
            
            val completedExercises = currentWorkout.exercises.map { exercise ->
                exercise.copy(isCompleted = true, exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = true) })
            }

            val exerciseRecords = updateExerciseRecords(completedExercises)
            val volumeRecords = updateVolumeRecord(completedExercises)

            val newHistoryEntry = WorkoutHistory(
                name = currentWorkout.name,
                completionDate = java.time.LocalDate.now(),
                exercises = completedExercises,
                workoutId = "freestyle",
                difficulty = WorkoutDifficulty.MEDIUM,
                durationMillis = duration,
                records = Records(exercisesRecordMap = exerciseRecords, volumeRecords = volumeRecords)
            )

            workoutHistoryRepository.addWorkoutHistory(newHistoryEntry)
            delay(500)
            _navigateToReport.value = newHistoryEntry.id
        }
    }

    private suspend fun updateExerciseRecords(completedExercises: List<Exercise>): MutableMap<String, MutableList<Int>> {
        return withContext(Dispatchers.IO) {
            val mapOfRecords: MutableMap<String, MutableList<Int>> = mutableMapOf()
            var records = recordsRepository.getRecord() ?: Records()
            var recordsUpdated = false

            completedExercises.forEach { exercise ->
                val maxWeight = exercise.exerciseSets.maxOfOrNull { it.weight.toDoubleOrNull() ?: 0.0 }?.toInt() ?: 0
                if (maxWeight > 0) {
                    val exerciseRecords = records.exercisesRecordMap.getOrPut(exercise.name) { mutableListOf() }
                    if (exerciseRecords.isEmpty() || maxWeight > exerciseRecords.first()) {
                        exerciseRecords.add(0, maxWeight)
                        mapOfRecords[exercise.name] = mutableListOf(maxWeight)
                        recordsUpdated = true
                    }
                }
            }

            if (recordsUpdated) recordsRepository.updateRecord(records)
            mapOfRecords
        }
    }

    private suspend fun updateVolumeRecord(completedExercises: List<Exercise>): MutableList<Int>? {
        return withContext(Dispatchers.IO) {
            val totalVolume = completedExercises.sumOf { exercise ->
                exercise.exerciseSets.sumOf { (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0) }
            }.toInt()

            var records = recordsRepository.getRecord() ?: Records()
            val bestVolume = records.volumeRecords?.firstOrNull() ?: 0

            if (totalVolume > bestVolume) {
                records.volumeRecords?.add(0, totalVolume)
                recordsRepository.updateRecord(records)
                return@withContext records.volumeRecords
            }
            null
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                FreestyleWorkoutViewModel(
                    exerciseRepository = application.container.exerciseRepository,
                    workoutHistoryRepository = application.container.workoutHistoryRepository,
                    recordsRepository = application.container.recordsRepository,
                    classifier = application.container.exerciseClassifier
                )
            }
        }
    }
}
