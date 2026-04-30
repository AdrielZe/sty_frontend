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
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.RecordsRepository
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
        viewModelScope.launch {
            delegate.completeWorkout(
                workout = currentWorkout,
                workoutHistoryRepository = workoutHistoryRepository,
                onWorkoutFinished = { completedWorkout ->
                    _finishedWorkoutSession.value = completedWorkout
                },
                onNavigateToReport = { historyId ->
                    _navigateToReport.value = historyId
                },
                resetWorkout = { historyId ->
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
                }
            )
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
