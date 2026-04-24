package com.example.training_tracker.ui.screens.workout_screen

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.data.repository.RecordsRepository
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import com.example.training_tracker.ui.screens.home.HomeViewModel
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WorkoutViewModel(
    savedStateHandle: SavedStateHandle,
    private val workoutRepository: WorkoutRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val recordsRepository: RecordsRepository
) : ViewModel() {
    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val _finishedWorkoutSession = MutableStateFlow<Workout?>(null)
    private val _navigateToReport = MutableStateFlow<String?>(null)
    val navigateToReport = _navigateToReport.asStateFlow()

    fun onNavigatedToReport() {
        _navigateToReport.value = null
    }

    fun Exercise.isValidToComplete(): Boolean {
        return exerciseSets.all { set ->
            val weight = set.weight.toDoubleOrNull() ?: 0.0
            val reps = set.reps.toIntOrNull() ?: 0

            weight > 0 && reps > 0
        }
    }

    val uiState = combine(
        workoutRepository.getWorkoutById(workoutId), _finishedWorkoutSession
    ) { dbWorkout, finishedWorkout ->

        // Se já finalizamos o treino agora, congelamos a tela com a versão pronta!
        if (finishedWorkout != null) {
            WorkoutUiState(workout = finishedWorkout)
        } else {
            // Se ainda estamos treinando, a tela reflete o banco de dados normalmente
            WorkoutUiState(workout = dbWorkout)
        }

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WorkoutUiState()
    )

    private fun calculateProgress(workout: Workout): Float {
        val totalSets = workout.exercises.sumOf { it.exerciseSets.size }
        if (totalSets == 0) return 0f
        val completedSets = workout.exercises.sumOf { it.exerciseSets.count { set -> set.isCompleted } }
        return completedSets.toFloat() / totalSets
    }

    fun togglePauseWorkout() {
        val currentWorkout = uiState.value.workout ?: return
        val now = System.currentTimeMillis()

        val updatedWorkout = if (currentWorkout.isPaused) {
            // Unpausing: set new startTime to now
            currentWorkout.copy(isPaused = false, startTime = now)
        } else {
            // Pausing: add elapsed time to accumulatedTime and clear startTime
            val elapsedSinceStart = if (currentWorkout.startTime != null) now - currentWorkout.startTime else 0L
            currentWorkout.copy(
                isPaused = true,
                accumulatedTime = currentWorkout.accumulatedTime + elapsedSinceStart,
                startTime = null
            )
        }

        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun addNewSetLine(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExerciseSetLine = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val maxSetNumber = exercise.exerciseSets.maxOfOrNull { it.set } ?: 0

                val updatedSetList = exercise.exerciseSets + ExerciseSet(set = maxSetNumber + 1)

                exercise.copy(exerciseSets = updatedSetList)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExerciseSetLine)
        val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
        viewModelScope.launch {
            workoutRepository.updateWorkout(workoutWithProgress)
        }
    }

    fun removeSetLine(exerciseId: String, setNumber: Int) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedExercisesSet = exercise.exerciseSets.filter { it.set != setNumber }
                exercise.copy(exerciseSets = updatedExercisesSet)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
        viewModelScope.launch {
            workoutRepository.updateWorkout(workoutWithProgress)
        }
    }

    fun completeSet(exerciseId: String, setNumber: Int) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedExerciseSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        set.copy(isCompleted = true)
                    } else {
                        set
                    }
                }
                exercise.copy(exerciseSets = updatedExerciseSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
        viewModelScope.launch {
            workoutRepository.updateWorkout(workoutWithProgress)
        }
    }

    fun completeExercise(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExerciseList = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    set.copy(isCompleted = true)
                }

                exercise.copy(isCompleted = true, exerciseSets = updatedSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExerciseList)
        val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))

        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    fun reopenExercise(exerciseId: String) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExerciseList = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    set.copy(isCompleted = false)
                }
                exercise.copy(isCompleted = false, exerciseSets = updatedSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExerciseList)
        val workoutWithProgress = updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
        viewModelScope.launch {
            workoutRepository.updateWorkout(workoutWithProgress)
        }
    }

    suspend fun updateExerciseRecords(): MutableMap<String, MutableList<Int>> {
        return withContext(Dispatchers.IO) {
            val mapOfRecords: MutableMap<String, MutableList<Int>> = mutableMapOf()
            val currentWorkout = uiState.value.workout ?: return@withContext mapOfRecords

            var records = recordsRepository.getRecord()
            var isFirstTime = false

            // Proteção caso o banco esteja vazio
            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            var recordsUpdated = false

            currentWorkout.exercises.forEach { exercise ->
                val maxWeightThisWorkout = exercise.exerciseSets.maxByOrNull {
                    it.weight.toDoubleOrNull() ?: 0.0
                }?.weight?.toInt()

                if (maxWeightThisWorkout != null) {
                    val exerciseRecords =
                        records.exercisesRecordMap.getOrPut(exercise.name) { mutableListOf() }
                    val currentBest = exerciseRecords.firstOrNull()

                    if (currentBest == null || maxWeightThisWorkout > currentBest) {
                        exerciseRecords.add(0, maxWeightThisWorkout)
                        mapOfRecords.getOrPut(exercise.name) { mutableListOf() }
                            .add(0, maxWeightThisWorkout)
                        recordsUpdated = true
                    }
                }
            }

            // Salva as alterações de exercícios
            if (recordsUpdated || isFirstTime) {
                if (isFirstTime) {
                    recordsRepository.addRecord(records)
                } else {
                    recordsRepository.updateRecord(records)
                }
            }

            mapOfRecords
        }
    }

    suspend fun updateVolumeRecord(): MutableList<Int>? { // 1. Mudamos o retorno para a Lista
        return withContext(Dispatchers.IO) {
            val currentWorkout = uiState.value.workout ?: return@withContext null

            var records = recordsRepository.getRecord()
            var isFirstTime = false

            // Proteção caso o banco esteja vazio (útil se essa for a primeira função a rodar)
            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            val totalVolumeWorkout = currentWorkout.exercises.sumOf { exercise ->
                exercise.exerciseSets.sumOf { set ->
                    val weight = set.weight.toDoubleOrNull() ?: 0.0
                    val reps = set.reps.toIntOrNull() ?: 0
                    weight * reps
                }
            }.toInt()

            val bestVolume = records.volumeRecords?.firstOrNull() ?: 0

            if (totalVolumeWorkout > bestVolume) {
                // Adiciona o novo recorde na posição 0
                records.volumeRecords?.add(0, totalVolumeWorkout)

                // Salva a alteração de volume
                if (isFirstTime) {
                    recordsRepository.addRecord(records)
                } else {
                    recordsRepository.updateRecord(records)
                }

                // 2. Retornamos a lista inteira já atualizada com o novo valor na pos 0
                return@withContext records.volumeRecords
            }

            // Se for a primeira vez e o volume for 0 (treino vazio), garantimos a criação do documento
            if (isFirstTime) {
                recordsRepository.addRecord(records)
            }

            null // Retorna nulo se o recorde não foi batido
        }
    }

    fun completeWorkout() {
        val currentWorkout = uiState.value.workout ?: return

        viewModelScope.launch(Dispatchers.IO) {
            println("CURRENT RECORDS OBJECT: ${recordsRepository.getRecord()}")
        }
        // 1. Prepara os dados CONCLUÍDOS (Para o Histórico e para Congelar a Tela
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

        val newHistoryEntry = WorkoutHistory(
            name = currentWorkout.name,
            completionDate = java.time.LocalDate.now(),
            exercises = completedExercises,
            workoutId = currentWorkout.id,
            difficulty = WorkoutDifficulty.MEDIUM,
            durationMillis = duration
        )

        val historyId = newHistoryEntry.id

        val completedWorkout = currentWorkout.copy(
            exercises = completedExercises,
            isCompleted = true,
            completionDate = java.time.LocalDate.now(),
            progress = 1f
        )

        _finishedWorkoutSession.value = completedWorkout

        viewModelScope.launch(Dispatchers.IO) {

            val exerciseRecords = updateExerciseRecords()
            val volumeRecords = updateVolumeRecord()

            val newHistoryEntryRecords = newHistoryEntry.copy(
                records = Records(
                    exercisesRecordMap = exerciseRecords,
                    volumeRecords = volumeRecords
                )
            )

            workoutHistoryRepository.addWorkoutHistory(newHistoryEntryRecords)

            val resetExercises = currentWorkout.exercises.map { exercise ->
                val resetSets = exercise.exerciseSets.map { set ->
                    set.copy(
                        isCompleted = false, reps = "",   // Apaga as reps
                        weight = ""  // Apaga os pesos
                    )
                }
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
                startTime = null,
                progress = 0f
            )

            workoutRepository.updateWorkout(resetWorkout)
            delay(500)
            _navigateToReport.value = historyId

        }
    }

    fun checkCompletedWorkout(exerciseId: String) {

    }

    fun updateExercise(
        exerciseId: String, setNumber: Int? = 1, newReps: String? = null, newWeight: String? = null
    ) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedExercises = currentWorkout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        set.copy(
                            reps = newReps ?: set.reps, weight = newWeight ?: set.weight
                        )
                    } else {
                        set
                    }
                }

                exercise.copy(exerciseSets = updatedSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = currentWorkout.copy(exercises = updatedExercises)
        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
        }
    }

    private fun updateCurrentWorkout(updateAction: (Workout) -> Workout) {
        val currentWorkout = uiState.value.workout ?: return

        val updatedWorkout = updateAction(currentWorkout)

        viewModelScope.launch {
            workoutRepository.updateWorkout(updatedWorkout)
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

                WorkoutViewModel(
                    savedStateHandle = savedStateHandle,
                    workoutRepository = workoutRepository,
                    workoutHistoryRepository = workoutHistoryRepository,
                    recordsRepository = recordsRepository
                )
            }
        }
    }
}
