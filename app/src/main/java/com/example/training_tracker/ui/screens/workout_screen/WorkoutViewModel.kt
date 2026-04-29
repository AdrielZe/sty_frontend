package com.example.training_tracker.ui.screens.workout_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.data.repository.RecordsRepository
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class WorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val recordsRepository: RecordsRepository,
    private val delegate: WorkoutDelegate
) : ViewModel(), WorkoutDelegate by delegate {
    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val _finishedWorkoutSession = MutableStateFlow<Workout?>(null)
    private val _navigateToReport = MutableStateFlow<String?>(null)
    val navigateToReport = _navigateToReport.asStateFlow()

    fun onNavigatedToReport() {
        _navigateToReport.value = null
    }

    val uiState = combine(
        workoutRepository.getWorkoutById(workoutId), _finishedWorkoutSession
    ) { dbWorkout, finishedWorkout ->
        if (finishedWorkout != null) {
            WorkoutUiState(workout = finishedWorkout)
        } else {
            WorkoutUiState(workout = dbWorkout)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    fun togglePauseWorkout() {
        viewModelScope.launch {
            uiState.value.workout?.let { togglePauseWorkout(it) }
        }
    }

    fun addNewSetLine(exerciseId: String) {
        viewModelScope.launch {
            uiState.value.workout?.let { addNewSetLine(it, exerciseId) }
        }
    }

    fun removeSetLine(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            uiState.value.workout?.let { removeSetLine(it, exerciseId, setNumber) }
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            uiState.value.workout?.let { completeSet(it, exerciseId, setNumber) }
        }
    }

    fun completeExercise(exerciseId: String) {
        viewModelScope.launch {
            uiState.value.workout?.let { completeExercise(it, exerciseId) }
        }
    }

    fun reopenExercise(exerciseId: String) {
        viewModelScope.launch {
            uiState.value.workout?.let { reopenExercise(it, exerciseId) }
        }
    }

    fun updateExercise(
        exerciseId: String, setNumber: Int? = 1, newReps: String? = null, newWeight: String? = null
    ) {
        viewModelScope.launch {
            uiState.value.workout?.let {
                updateExercise(it, exerciseId, setNumber ?: 1, newReps, newWeight)
            }
        }
    }

    fun completeWorkout() {
        val currentWorkout = uiState.value.workout ?: return

        // 1. Prepara os dados CONCLUÍDOS
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

        val newHistoryEntry = WorkoutHistory(
            name = currentWorkout.name,
            completionDate = completionDate,
            completionTime = completionTime,
            exercises = completedExercises,
            workoutId = currentWorkout.id,
            difficulty = WorkoutDifficulty.MEDIUM,
            durationMillis = duration
        )

        val historyId = newHistoryEntry.id

        val completedWorkout = currentWorkout.copy(
            exercises = completedExercises,
            isCompleted = true,
            completionDate = completionDate,
            completionTime = completionTime,
            progress = 1f
        )

        _finishedWorkoutSession.value = completedWorkout

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

            val resetExercises = currentWorkout.exercises.map { exercise ->
                val resetSets = listOf(ExerciseSet(set = 1))
                exercise.copy(
                    isCompleted = false, exerciseSets = resetSets
                )
            }

            val resetWorkout = currentWorkout.copy(
                exercises = resetExercises,
                isCompleted = false,
                historyId = historyId,
                isOnGoing = false,
                isPaused = false,
                accumulatedTime = 0L,
                completionDate = null,
                completionTime = null,
                startTime = null,
                progress = 0f
            )

            workoutRepository.updateWorkout(resetWorkout)
            delay(500)
            _navigateToReport.value = historyId
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val workoutHistoryRepository = application.container.workoutHistoryRepository
                val recordsRepository = application.container.recordsRepository

                val savedStateHandle = createSavedStateHandle()
                val delegate = WorkoutDelegateImpl(workoutRepository, recordsRepository)

                WorkoutViewModel(
                    savedStateHandle = savedStateHandle,
                    workoutRepository = workoutRepository,
                    workoutHistoryRepository = workoutHistoryRepository,
                    recordsRepository = recordsRepository,
                    delegate = delegate
                )
            }
        }
    }
}
