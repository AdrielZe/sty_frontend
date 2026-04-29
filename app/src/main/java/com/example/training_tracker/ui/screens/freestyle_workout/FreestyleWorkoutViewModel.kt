package com.example.training_tracker.ui.screens.freestyle_workout

import android.util.Log
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
import com.example.training_tracker.data.repository.WorkoutRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import com.example.training_tracker.ui.screens.workout_screen.WorkoutDelegate
import com.example.training_tracker.ui.screens.workout_screen.WorkoutDelegateImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

private const val TAG = "FreestyleWorkoutViewModel"

class FreestyleWorkoutViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val workoutRepository: WorkoutRepository,
    private val classifier: ExerciseClassifier,
    private val delegate: WorkoutDelegate
) : ViewModel(), WorkoutDelegate by delegate {

    private val FREESTYLE_WORKOUT_ID = "freestyle_workout_id"

    private val _showExercisePicker = MutableStateFlow(false)
    private val _navigateToReport = MutableStateFlow<String?>(null)
    val navigateToReport = _navigateToReport.asStateFlow()

    private val _pendingExerciseName = MutableStateFlow<String?>(null)
    private val _showMuscleGroupPicker = MutableStateFlow(false)

    private val _finishedWorkoutSession = MutableStateFlow<Workout?>(null)

    val uiState: StateFlow<FreestyleWorkoutUiState> = combine(
        workoutRepository.getWorkoutById(FREESTYLE_WORKOUT_ID),
        exerciseRepository.exercises,
        _showExercisePicker,
        _finishedWorkoutSession,
        combine(_pendingExerciseName, _showMuscleGroupPicker) { name, show -> name to show }
    ) { workout, availableExercises, showPicker, finishedWorkout, pendingInfo ->
        val (pendingName, showMusclePicker) = pendingInfo

        if (finishedWorkout != null) {
            FreestyleWorkoutUiState(workout = finishedWorkout)
        } else {
            FreestyleWorkoutUiState(
                workout = workout ?: Workout(
                    id = FREESTYLE_WORKOUT_ID,
                    name = "Freestyle Workout",
                    isOnGoing = false
                ),
                availableExercises = availableExercises,
                showExercisePicker = showPicker,
                pendingExerciseName = pendingName,
                showMuscleGroupPicker = showMusclePicker
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FreestyleWorkoutUiState()
    )

    init {
        ensureFreestyleWorkoutExists()
    }

    private fun ensureFreestyleWorkoutExists() {
        viewModelScope.launch {
            val existing = workoutRepository.getWorkoutById(FREESTYLE_WORKOUT_ID).first()
            if (existing == null) {
                workoutRepository.addWorkout(
                    Workout(
                        id = FREESTYLE_WORKOUT_ID,
                        name = "Freestyle Workout",
                        isOnGoing = false,
                        startTime = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun onNavigatedToReport() {
        _navigateToReport.value = null
    }

    fun togglePauseWorkout() {
        viewModelScope.launch {
            togglePauseWorkout(uiState.value.workout)
        }
    }

    fun showExercisePicker(show: Boolean) {
        _showExercisePicker.value = show
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
                    val result = classifier.classify(nameFormatted)
                    
                    if (result.confidence < 0.85f) {
                        _pendingExerciseName.value = nameFormatted
                        _showMuscleGroupPicker.value = true
                        _showExercisePicker.value = false
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
                    _pendingExerciseName.value = nameFormatted
                    _showMuscleGroupPicker.value = true
                    _showExercisePicker.value = false
                }
            }
        } else {
            val exerciseToAdd = existingExercise.copy(
                id = UUID.randomUUID().toString(),
                exerciseSets = listOf(ExerciseSet(set = 1)),
                isCompleted = false
            )
            val updatedWorkout = uiState.value.workout.copy(
                exercises = uiState.value.workout.exercises + exerciseToAdd
            )
            viewModelScope.launch {
                workoutRepository.updateWorkout(updatedWorkout)
            }
            _showExercisePicker.value = false
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
        val newExerciseToDB = Exercise(
            name = name,
            muscleGroup = muscleGroup
        )
        exerciseRepository.addExercise(newExerciseToDB)

        val exerciseToAdd = newExerciseToDB.copy(
            id = UUID.randomUUID().toString(),
            exerciseSets = listOf(ExerciseSet(set = 1)),
            isCompleted = false
        )

        val updatedWorkout = uiState.value.workout.copy(
            exercises = uiState.value.workout.exercises + exerciseToAdd
        )
        workoutRepository.updateWorkout(updatedWorkout)
        
        _showExercisePicker.value = false
        _showMuscleGroupPicker.value = false
        _pendingExerciseName.value = null
    }

    fun removeExercise(exerciseId: String) {
        val currentWorkout = uiState.value.workout
        val updatedExercises = currentWorkout.exercises.filter { it.id != exerciseId }
        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))

        viewModelScope.launch {
            workoutRepository.updateWorkout(workoutWithProgress)
        }
    }

    fun addNewSetLine(exerciseId: String) {
        viewModelScope.launch {
            addNewSetLine(uiState.value.workout, exerciseId)
        }
    }

    fun removeSetLine(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            removeSetLine(uiState.value.workout, exerciseId, setNumber)
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            completeSet(uiState.value.workout, exerciseId, setNumber)
        }
    }

    fun updateExercise(
        exerciseId: String,
        setNumber: Int,
        newReps: String? = null,
        newWeight: String? = null
    ) {
        viewModelScope.launch {
            updateExercise(uiState.value.workout, exerciseId, setNumber, newReps, newWeight)
        }
    }

    fun completeExercise(exerciseId: String) {
        viewModelScope.launch {
            completeExercise(uiState.value.workout, exerciseId)
        }
    }

    fun reopenExercise(exerciseId: String) {
        viewModelScope.launch {
            reopenExercise(uiState.value.workout, exerciseId)
        }
    }

    fun completeWorkout() {
        val currentWorkout = uiState.value.workout

        val completedExercises = currentWorkout.exercises.map { exercise ->
            val completedSets = exercise.exerciseSets.map { set ->
                set.copy(isCompleted = true)
            }
            exercise.copy(
                isCompleted = true, exerciseSets = completedSets
            )
        }

        val now = System.currentTimeMillis()
        val duration = currentWorkout.accumulatedTime + if (currentWorkout.startTime != null) {
            now - currentWorkout.startTime
        } else {
            0L
        }

        val completionDate = LocalDate.now()
        val completionTime = LocalTime.now()

        val completedWorkout = currentWorkout.copy(
            exercises = completedExercises,
            isCompleted = true,
            completionDate = completionDate,
            completionTime = completionTime,
            progress = 1f
        )

        val newHistoryEntry = WorkoutHistory(
            name = currentWorkout.name,
            completionDate = completionDate,
            completionTime = completionTime,
            exercises = completedExercises,
            workoutId = FREESTYLE_WORKOUT_ID,
            difficulty = WorkoutDifficulty.MEDIUM,
            durationMillis = duration
        )

        _finishedWorkoutSession.value = completedWorkout
        val historyId = newHistoryEntry.id

        viewModelScope.launch(Dispatchers.IO) {
            val exerciseRecords = updateExerciseRecords(completedWorkout)
            val volumeRecords = updateVolumeRecord(completedWorkout)

            val newHistoryEntryRecords = newHistoryEntry.copy(
                records = Records(
                    exercisesRecordMap = exerciseRecords,
                    volumeRecords = volumeRecords
                )
            )

            workoutHistoryRepository.addWorkoutHistory(newHistoryEntryRecords)

            workoutRepository.updateWorkout(
                Workout(
                    id = FREESTYLE_WORKOUT_ID,
                    name = "Freestyle Workout",
                    isOnGoing = false,
                    startTime = null,
                    exercises = emptyList(),
                    accumulatedTime = 0L,
                    isPaused = false,
                    completionDate = null,
                    completionTime = null
                )
            )

            delay(1000)
            _navigateToReport.value = historyId
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val recordsRepository = application.container.recordsRepository
                
                FreestyleWorkoutViewModel(
                    exerciseRepository = application.container.exerciseRepository,
                    workoutHistoryRepository = application.container.workoutHistoryRepository,
                    workoutRepository = workoutRepository,
                    classifier = application.container.exerciseClassifier,
                    delegate = WorkoutDelegateImpl(workoutRepository, recordsRepository)
                )
            }
        }
    }
}
