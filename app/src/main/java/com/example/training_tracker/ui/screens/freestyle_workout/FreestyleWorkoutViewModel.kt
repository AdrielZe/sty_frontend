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
import com.example.training_tracker.data.models.Technique
import com.example.training_tracker.data.models.Workout
import java.time.DayOfWeek
import com.example.training_tracker.domain.repository.ExerciseRepository
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import com.example.training_tracker.session_manager.SessionManager
import com.example.training_tracker.ui.screens.workout_screen.WorkoutDelegate
import com.example.training_tracker.ui.screens.workout_screen.WorkoutDelegateImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class FreestyleWorkoutViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val workoutRepository: WorkoutRepository,
    private val classifier: ExerciseClassifier,
    private val delegate: WorkoutDelegate,
    private val sessionManager: SessionManager
) : ViewModel(), WorkoutDelegate by delegate {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val freestyleWorkoutFlow = sessionManager.userIdFlow.flatMapLatest { userId ->
        if (userId != null) {
            workoutRepository.getWorkoutById(Workout.freestyleWorkoutId(userId.toString()))
        } else {
            flowOf(null)
        }
    }

    private val _showExercisePicker = MutableStateFlow(false)
    private val _navigateToReport = MutableStateFlow<String?>(null)
    val navigateToReport = _navigateToReport.asStateFlow()

    private val _pendingExerciseName = MutableStateFlow<String?>(null)
    private val _showMuscleGroupPicker = MutableStateFlow(false)
    private val _showSaveRoutineDialog = MutableStateFlow(false)
    private val _pendingReportId = MutableStateFlow<String?>(null)

    private val _finishedWorkoutSession = MutableStateFlow<Workout?>(null)

    val uiState: StateFlow<FreestyleWorkoutUiState> = combine(
        sessionManager.userIdFlow,
        freestyleWorkoutFlow,
        exerciseRepository.exercises,
        workoutHistoryRepository.workoutHistories,
        combine(
            _showExercisePicker,
            _finishedWorkoutSession,
            _pendingExerciseName,
            combine(_showMuscleGroupPicker, _showSaveRoutineDialog) { a, b -> a to b }
        ) { showPicker, finishedWorkout, pendingName, (showMusclePicker, showSaveRoutine) ->
            InternalState(showPicker, finishedWorkout, pendingName, showMusclePicker, showSaveRoutine)
        }
    ) { userId, workout, availableExercises, histories, internalState ->
        if (internalState.finishedWorkoutSession != null) {
            FreestyleWorkoutUiState(
                workout = internalState.finishedWorkoutSession,
                showSaveRoutineDialog = internalState.showSaveRoutineDialog
            )
        } else if (workout != null) {
            val enrichedWorkout = enrichWorkoutWithHistory(workout, histories)
            FreestyleWorkoutUiState(
                workout = enrichedWorkout,
                availableExercises = availableExercises,
                showExercisePicker = internalState.showExercisePicker,
                pendingExerciseName = internalState.pendingExerciseName,
                showMuscleGroupPicker = internalState.showMuscleGroupPicker
            )
        } else {
            FreestyleWorkoutUiState(
                workout = Workout(
                    id = userId?.toString().orEmpty(),
                    userId = userId?.toString(),
                    name = "Freestyle Workout",
                    isOnGoing = false
                ),
                availableExercises = availableExercises,
                showExercisePicker = internalState.showExercisePicker,
                pendingExerciseName = internalState.pendingExerciseName,
                showMuscleGroupPicker = internalState.showMuscleGroupPicker
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FreestyleWorkoutUiState()
    )

    private data class InternalState(
        val showExercisePicker: Boolean,
        val finishedWorkoutSession: Workout?,
        val pendingExerciseName: String?,
        val showMuscleGroupPicker: Boolean,
        val showSaveRoutineDialog: Boolean = false
    )

    init {
        ensureFreestyleWorkoutExists()
    }

    private fun ensureFreestyleWorkoutExists() {
        viewModelScope.launch {
            val userId = sessionManager.userIdFlow.first() ?: return@launch
            val freestyleId = Workout.freestyleWorkoutId(userId.toString())
            val existing = workoutRepository.getWorkoutById(freestyleId).first()
            if (existing == null) {
                workoutRepository.addWorkout(
                    Workout(
                        id = freestyleId,
                        userId = userId.toString(),
                        name = "Freestyle Workout",
                        isOnGoing = false,
                        startTime = System.currentTimeMillis(),
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
            val updated = togglePauseWorkout(uiState.value.workout)
            workoutRepository.updateWorkout(updated)
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
            val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
            viewModelScope.launch {
                workoutRepository.updateWorkout(workoutWithProgress)
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
        val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
        workoutRepository.updateWorkout(workoutWithProgress)
        
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
            val updated = addNewSetLine(uiState.value.workout, exerciseId)
            workoutRepository.updateWorkout(updated)
        }
    }

    fun removeSetLine(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            val updated = removeSetLine(uiState.value.workout, exerciseId, setNumber)
            workoutRepository.updateWorkout(updated)
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int) {
        viewModelScope.launch {
            val updated = completeSet(uiState.value.workout, exerciseId, setNumber)
            workoutRepository.updateWorkout(updated)
        }
    }

    fun updateExercise(
        exerciseId: String,
        setNumber: Int,
        newReps: String? = null,
        newWeight: String? = null
    ) {
        viewModelScope.launch {
            val updated = updateExercise(uiState.value.workout, exerciseId, setNumber, newReps, newWeight)
            workoutRepository.updateWorkout(updated)
        }
    }

    fun completeExercise(exerciseId: String) {
        viewModelScope.launch {
            val updated = completeExercise(uiState.value.workout, exerciseId)
            workoutRepository.updateWorkout(updated)
        }
    }

    fun reopenExercise(exerciseId: String) {
        viewModelScope.launch {
            val updated = reopenExercise(uiState.value.workout, exerciseId)
            workoutRepository.updateWorkout(updated)
        }
    }

    fun updateSetTechnique(exerciseId: String, setNumber: Int, technique: Technique) {
        viewModelScope.launch {
            val updated = updateSetTechnique(uiState.value.workout, exerciseId, setNumber, technique)
            workoutRepository.updateWorkout(updated)
        }
    }

    fun completeWorkout() {
        viewModelScope.launch {
            delegate.completeWorkout(
                workout = uiState.value.workout,
                workoutHistoryRepository = workoutHistoryRepository,
                onWorkoutFinished = { completedWorkout ->
                    _finishedWorkoutSession.value = completedWorkout
                },
                onNavigateToReport = { historyId ->
                    _pendingReportId.value = historyId
                    _showSaveRoutineDialog.value = true
                },
                resetWorkout = {
                    // Preserva id/userId da linha atual em vez de recriar do zero,
                    // já que o id do freestyle workout é o próprio userId do dono.
                    workoutRepository.updateWorkout(
                        uiState.value.workout.copy(
                            name = "Freestyle Workout",
                            isOnGoing = false,
                            startTime = null,
                            exercises = emptyList(),
                            accumulatedTime = 0L,
                            isPaused = false,
                            completionDate = null,
                            completionTime = null,
                            progress = 0f
                        )
                    )
                },
                sessionManager = sessionManager
            )
        }
    }

    fun saveAsRoutine(name: String, dayOfWeek: DayOfWeek) {
        val exercises = _finishedWorkoutSession.value?.exercises ?: return
        val cleanExercises = exercises.map { exercise ->
            exercise.copy(
                id = java.util.UUID.randomUUID().toString(),
                isCompleted = false,
                exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = false) }
            )
        }
        viewModelScope.launch {
            workoutRepository.addWorkout(
                Workout(
                    name = name,
                    exercises = cleanExercises,
                    userId = sessionManager.userIdFlow.first().toString(),
                    dayOfWeek = dayOfWeek,
                    isOnGoing = false
                ),
            )
            dismissSaveRoutineDialog()
        }
    }

    fun dismissSaveRoutineDialog() {
        _showSaveRoutineDialog.value = false
        _navigateToReport.value = _pendingReportId.value
        _pendingReportId.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val recordsRepository = application.container.recordsRepository
                val sessionManager = application.container.sessionManager
                
                FreestyleWorkoutViewModel(
                    exerciseRepository = application.container.exerciseRepository,
                    workoutHistoryRepository = application.container.workoutHistoryRepository,
                    workoutRepository = workoutRepository,
                    classifier = application.container.exerciseClassifier,
                    delegate = WorkoutDelegateImpl(workoutRepository, recordsRepository),
                    sessionManager = sessionManager
                )
            }
        }
    }
}
