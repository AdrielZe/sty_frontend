package com.example.training_tracker.ui.screens.workout_screen

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.Technique
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.domain.repository.RecordsRepository
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.service.WorkoutTimerService
import com.example.training_tracker.session_manager.SessionManager
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

class WorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val recordsRepository: RecordsRepository,
    private val delegate: WorkoutDelegate,
    private val sessionManager: SessionManager,
    private val appContext: Context
) : ViewModel(), WorkoutDelegate by delegate {

    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])

    private val _activeWorkoutSession = MutableStateFlow<Workout?>(null)
    private val _finishedWorkoutSession = MutableStateFlow<Workout?>(null)

    private val _navigateToReport = MutableStateFlow<String?>(null)
    val navigateToReport = _navigateToReport.asStateFlow()

    val elapsedTime: StateFlow<Long> = WorkoutTimerService.elapsedMs

    val uiState = _activeWorkoutSession.map { workout ->
        if (_finishedWorkoutSession.value != null) {
            WorkoutUiState(workout = _finishedWorkoutSession.value)
        } else {
            WorkoutUiState(workout = workout)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    init {
        // 1. CARREGAR OU RESTAURAR O TREINO
        viewModelScope.launch {
            combine(
                workoutRepository.getWorkoutById(workoutId),
                workoutHistoryRepository.workoutHistories
            ) { dbWorkout, histories ->

                if (dbWorkout != null && _activeWorkoutSession.value == null && _finishedWorkoutSession.value == null) {
                    val today = LocalDate.now()

                    val draftHistory = histories.find {
                        it.workoutId == dbWorkout.id && !it.isCompleted && it.completionDate == today
                    }

                    val serviceAlreadyRunning = WorkoutTimerService.elapsedMs.value > 0L

                    if (draftHistory != null) {
                        _activeWorkoutSession.value = dbWorkout.copy(
                            historyId = draftHistory.id,
                            exercises = draftHistory.exercises,
                            accumulatedTime = if (serviceAlreadyRunning)
                                WorkoutTimerService.elapsedMs.value
                            else
                                draftHistory.durationMillis,
                            isOnGoing = true,
                            isPaused = false
                        )
                        if (!serviceAlreadyRunning) {
                            startTimerService(draftHistory.durationMillis)
                        }
                    } else {
                        val freshWorkoutForToday = enrichWorkoutWithHistory(dbWorkout, histories)
                        _activeWorkoutSession.value = freshWorkoutForToday.copy(
                            historyId = UUID.randomUUID().toString(),
                            isOnGoing = true
                        )
                        if (!serviceAlreadyRunning) {
                            startTimerService(0L)
                        }
                    }
                }
            }.collectLatest { }
        }

        // 2. O AUTO-SAVE BACKGROUND JOB
        viewModelScope.launch(Dispatchers.IO) {
            _activeWorkoutSession.collectLatest { currentWorkout ->
                if (currentWorkout != null && currentWorkout.isOnGoing) {
                    val draft = WorkoutHistory(
                        id = currentWorkout.historyId ?: return@collectLatest,
                        userId = sessionManager.getUserId()?.toString(),
                        workoutId = currentWorkout.id,
                        name = currentWorkout.name,
                        completionDate = LocalDate.now(),
                        exercises = currentWorkout.exercises,
                        difficulty = WorkoutDifficulty.MEDIUM,
                        durationMillis = WorkoutTimerService.elapsedMs.value,
                        isCompleted = false
                    )
                    workoutHistoryRepository.insert(draft)
                }
            }
        }
    }

    private fun startTimerService(accumulatedMs: Long) {
        val intent = Intent(appContext, WorkoutTimerService::class.java).apply {
            action = WorkoutTimerService.ACTION_START
            putExtra(WorkoutTimerService.EXTRA_ACCUMULATED_MS, accumulatedMs)
            putExtra(WorkoutTimerService.EXTRA_WORKOUT_ID, workoutId)
        }
        appContext.startService(intent)
    }

    private fun stopTimerService() {
        appContext.startService(
            Intent(appContext, WorkoutTimerService::class.java).apply {
                action = WorkoutTimerService.ACTION_STOP
            }
        )
    }

    fun onNavigatedToReport() {
        _navigateToReport.value = null
    }

    fun togglePauseWorkout() {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                val updated = togglePauseWorkout(current)
                _activeWorkoutSession.value = updated
                val serviceAction = if (updated.isPaused) WorkoutTimerService.ACTION_PAUSE
                                    else WorkoutTimerService.ACTION_RESUME
                appContext.startService(
                    Intent(appContext, WorkoutTimerService::class.java).apply {
                        action = serviceAction
                    }
                )
            }
        }
    }

    fun addNewSetLine(exerciseId: String) {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                _activeWorkoutSession.value = addNewSetLine(current, exerciseId)
            }
        }
    }

    fun removeSetLine(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                _activeWorkoutSession.value = removeSetLine(current, exerciseId, setNumber)
            }
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                _activeWorkoutSession.value = completeSet(current, exerciseId, setNumber)
            }
        }
    }

    fun completeExercise(exerciseId: String) {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                _activeWorkoutSession.value = completeExercise(current, exerciseId)
            }
        }
    }

    fun reopenExercise(exerciseId: String) {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                _activeWorkoutSession.value = reopenExercise(current, exerciseId)
            }
        }
    }

    fun updateSetTechnique(exerciseId: String, setNumber: Int, technique: Technique) {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                _activeWorkoutSession.value = updateSetTechnique(current, exerciseId, setNumber, technique)
            }
        }
    }

    fun updateExercise(exerciseId: String, setNumber: Int? = 1, newReps: String? = null, newWeight: String? = null) {
        viewModelScope.launch {
            _activeWorkoutSession.value?.let { current ->
                _activeWorkoutSession.value = updateExercise(current, exerciseId, setNumber ?: 1, newReps, newWeight)
            }
        }
    }

    fun completeWorkout() {
        val currentWorkout = _activeWorkoutSession.value ?: return
        val finalElapsed = WorkoutTimerService.elapsedMs.value
        stopTimerService()
        val workoutWithFinalTime = currentWorkout.copy(
            accumulatedTime = finalElapsed,
            startTime = null
        )
        viewModelScope.launch {
            delegate.completeWorkout(
                workout = workoutWithFinalTime,
                workoutHistoryRepository = workoutHistoryRepository,
                onWorkoutFinished = { completedWorkout ->
                    _finishedWorkoutSession.value = completedWorkout
                    _activeWorkoutSession.value = null
                },
                onNavigateToReport = { historyId ->
                    _navigateToReport.value = historyId
                },
                resetWorkout = { historyId ->
                    val resetExercises = currentWorkout.exercises.map { exercise ->
                        val resetSets = listOf(ExerciseSet(set = 1, exerciseId = UUID.fromString(exercise.id)))
                        exercise.copy(isCompleted = false, exerciseSets = resetSets)
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
                },
                sessionManager = sessionManager
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (_finishedWorkoutSession.value == null) {
            // Treino não foi concluído — serviço continua rodando em background
            // (usuário pode voltar ao treino)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val workoutHistoryRepository = application.container.workoutHistoryRepository
                val recordsRepository = application.container.recordsRepository
                val sessionManager = application.container.sessionManager
                val savedStateHandle = createSavedStateHandle()
                val delegate = WorkoutDelegateImpl(workoutRepository, recordsRepository)

                WorkoutViewModel(
                    savedStateHandle = savedStateHandle,
                    workoutRepository = workoutRepository,
                    workoutHistoryRepository = workoutHistoryRepository,
                    recordsRepository = recordsRepository,
                    delegate = delegate,
                    sessionManager = sessionManager,
                    appContext = application.applicationContext
                )
            }
        }
    }
}
