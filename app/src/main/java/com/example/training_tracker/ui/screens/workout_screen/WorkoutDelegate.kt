package com.example.training_tracker.ui.screens.workout_screen

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.Technique
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.domain.repository.RecordsRepository
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.session_manager.SessionManager
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

interface WorkoutDelegate {
    fun calculateProgress(workout: Workout): Float

    // 👇 Agora retornam Workout em vez de Unit (e tirei o suspend pois são síncronas)
    fun togglePauseWorkout(workout: Workout): Workout
    fun addNewSetLine(workout: Workout, exerciseId: String): Workout
    fun removeSetLine(workout: Workout, exerciseId: String, setNumber: Int): Workout
    fun completeSet(workout: Workout, exerciseId: String, setNumber: Int): Workout
    fun updateExercise(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        newReps: String? = null,
        newWeight: String? = null
    ): Workout
    fun completeExercise(workout: Workout, exerciseId: String): Workout
    fun reopenExercise(workout: Workout, exerciseId: String): Workout
    fun updateSetTechnique(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        technique: Technique
    ): Workout

    // Essas aqui continuam suspend porque lidam com o Records/History Repository
    suspend fun updateExerciseRecords(workout: Workout): Pair<MutableMap<String, MutableList<Double>>, MutableMap<String, MutableList<Double>>>
    suspend fun updateVolumeRecord(workout: Workout): MutableList<Double>?
    fun enrichWorkoutWithHistory(workout: Workout, histories: List<WorkoutHistory>): Workout
    suspend fun completeWorkout(
        workout: Workout,
        workoutHistoryRepository: WorkoutHistoryRepository,
        onWorkoutFinished: (Workout) -> Unit,
        onNavigateToReport: (String) -> Unit,
        resetWorkout: suspend (historyId: String) -> Unit,
        sessionManager: SessionManager
    )
}

class WorkoutDelegateImpl(
    private val workoutRepository: WorkoutRepository,
    private val recordsRepository: RecordsRepository
) : WorkoutDelegate {

    override fun calculateProgress(workout: Workout): Float {
        val totalExercises = workout.exercises.size
        if (totalExercises == 0) return 0f
        val completedExercises = workout.exercises.count { it.isCompleted }
        return completedExercises.toFloat() / totalExercises
    }

    override fun togglePauseWorkout(workout: Workout): Workout {
        val now = System.currentTimeMillis()
        return if (workout.isPaused) {
            workout.copy(isPaused = false, startTime = now)
        } else {
            val elapsedSinceStart = if (workout.startTime != null) now - workout.startTime else 0L
            workout.copy(
                isPaused = true,
                accumulatedTime = workout.accumulatedTime + elapsedSinceStart,
                startTime = null
            )
        }
    }

    override fun updateSetTechnique(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        technique: Technique
    ): Workout {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) set.copy(technique = technique) else set
                }
                exercise.copy(exerciseSets = updatedSets)
            } else exercise
        }
        return workout.copy(exercises = updatedExercises)
    }

    override fun addNewSetLine(workout: Workout, exerciseId: String): Workout {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val maxSetNumber = exercise.exerciseSets.maxOfOrNull { it.set } ?: 0
                exercise.copy(exerciseSets = exercise.exerciseSets + ExerciseSet(set = maxSetNumber + 1, exerciseId = UUID.fromString(exerciseId)))
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        return updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
    }

    override fun removeSetLine(workout: Workout, exerciseId: String, setNumber: Int): Workout {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(exerciseSets = exercise.exerciseSets.filter { it.set != setNumber })
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        return updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
    }

    override fun completeSet(workout: Workout, exerciseId: String, setNumber: Int): Workout {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) set.copy(isCompleted = !set.isCompleted) else set // (Opcional: !set.isCompleted permite desmarcar)
                }
                exercise.copy(exerciseSets = updatedSets)
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        return updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
    }

    override fun updateExercise(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        newReps: String?,
        newWeight: String?
    ): Workout {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        when (exercise.type) {
                            ExerciseType.CARDIO -> set.copy(distance = newWeight ?: set.distance, time = newReps ?: set.time)
                            ExerciseType.STRETCHING -> set.copy(time = newReps ?: set.time)
                            else -> set.copy(reps = newReps ?: set.reps, weight = newWeight ?: set.weight)
                        }
                    } else set
                }
                exercise.copy(exerciseSets = updatedSets)
            } else exercise
        }
        return workout.copy(exercises = updatedExercises)
    }

    override fun completeExercise(workout: Workout, exerciseId: String): Workout {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(
                    isCompleted = true,
                    exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = true) })
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        return updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
    }

    override fun reopenExercise(workout: Workout, exerciseId: String): Workout {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(
                    isCompleted = false,
                    exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = false) })
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        return updatedWorkout.copy(progress = calculateProgress(updatedWorkout))
    }

    private fun String?.parseToDouble(): Double {
        return this?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
    }

    override suspend fun updateExerciseRecords(workout: Workout): Pair<MutableMap<String, MutableList<Double>>, MutableMap<String, MutableList<Double>>> {
        return withContext(Dispatchers.IO) {
            val strengthNewRecords: MutableMap<String, MutableList<Double>> = mutableMapOf()
            val cardioNewRecords: MutableMap<String, MutableList<Double>> = mutableMapOf()
            var records = recordsRepository.getRecord()
            var isFirstTime = false

            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            var recordsUpdated = false

            workout.exercises.filter { it.type == ExerciseType.STRENGTH }.forEach { exercise ->
                val maxWeightThisWorkout = exercise.exerciseSets.maxOfOrNull {
                    it.weight.parseToDouble()
                }

                if (maxWeightThisWorkout != null && maxWeightThisWorkout > 0.0) {
                    val exerciseRecords =
                        records.exercisesRecordMap.getOrPut(exercise.name) { mutableListOf() }
                    val currentBest = exerciseRecords.firstOrNull()

                    if (currentBest == null || maxWeightThisWorkout > currentBest) {
                        exerciseRecords.add(0, maxWeightThisWorkout)
                        strengthNewRecords.getOrPut(exercise.name) { mutableListOf() }
                            .add(0, maxWeightThisWorkout)
                        recordsUpdated = true
                    }
                }
            }

            workout.exercises.filter { it.type == ExerciseType.CARDIO }.forEach { exercise ->
                val maxTimeThisWorkout = exercise.exerciseSets.maxOfOrNull {
                    it.time.parseTimeToSeconds()
                }

                if (maxTimeThisWorkout != null && maxTimeThisWorkout > 0.0) {
                    val exerciseRecords =
                        records.cardioRecordsMap.getOrPut(exercise.name) { mutableListOf() }
                    val currentBest = exerciseRecords.firstOrNull()

                    if (currentBest == null || maxTimeThisWorkout > currentBest) {
                        exerciseRecords.add(0, maxTimeThisWorkout)
                        cardioNewRecords.getOrPut(exercise.name) { mutableListOf() }
                            .add(0, maxTimeThisWorkout)
                        recordsUpdated = true
                    }
                }
            }

            if (recordsUpdated || isFirstTime) {
                if (isFirstTime) {
                    recordsRepository.addRecord(records)
                } else {
                    recordsRepository.updateRecord(records)
                }
            }

            strengthNewRecords to cardioNewRecords
        }
    }

    private fun String?.parseTimeToSeconds(): Double {
        if (this.isNullOrBlank()) return 0.0
        val parts = this.split(":").map { it.toDoubleOrNull() ?: 0.0 }
        return when (parts.size) {
            2 -> parts[0] * 60 + parts[1]
            3 -> parts[0] * 3600 + parts[1] * 60 + parts[2]
            else -> 0.0
        }
    }

    override suspend fun updateVolumeRecord(workout: Workout): MutableList<Double>? {
        return withContext(Dispatchers.IO) {
            var records = recordsRepository.getRecord()
            var isFirstTime = false

            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            val totalVolumeWorkout = workout.exercises
                .filter { it.type == ExerciseType.STRENGTH }
                .sumOf { exercise ->
                    exercise.exerciseSets.sumOf { set ->
                        val weight = set.weight.parseToDouble()
                        val reps = set.reps.toIntOrNull() ?: 0
                        weight * reps
                    }
                }

            val bestVolume = records.volumeRecords?.firstOrNull() ?: 0.0

            if (totalVolumeWorkout > bestVolume) {
                if (records.volumeRecords == null) records.volumeRecords = mutableListOf()
                records.volumeRecords?.add(0, totalVolumeWorkout)

                if (isFirstTime) {
                    recordsRepository.addRecord(records)
                } else {
                    recordsRepository.updateRecord(records)
                }

                return@withContext records.volumeRecords
            }

            if (isFirstTime) {
                recordsRepository.addRecord(records)
            }

            null
        }
    }

    override fun enrichWorkoutWithHistory(
        workout: Workout,
        histories: List<WorkoutHistory>
    ): Workout {
        val sortedHistories = histories.sortedWith(
            compareByDescending<WorkoutHistory> { it.completionDate }
                .thenByDescending { it.completionTime ?: LocalTime.MIN }
        )

        val updatedExercises = workout.exercises.map { exercise ->
            val lastPerformance = sortedHistories.firstOrNull { history ->
                history.exercises.any { it.name.equals(exercise.name, ignoreCase = true) }
            }?.exercises?.find { it.name.equals(exercise.name, ignoreCase = true) }

            val updatedSets = exercise.exerciseSets.map { set ->
                val lastSet = lastPerformance?.exerciseSets?.find { it.set == set.set }
                val safeTechnique = set.technique ?: Technique.NORMAL
                val safePreviousReps = lastSet?.reps.orEmpty()
                val safePreviousWeight = lastSet?.weight.orEmpty()
                val safePreviousTime = lastSet?.time.orEmpty()
                val safePreviousDistance = lastSet?.distance.orEmpty()
                when (exercise.type) {
                    ExerciseType.CARDIO -> set.copy(
                        previousDistance = safePreviousDistance,
                        previousTime = safePreviousTime,
                        technique = safeTechnique,
                    )
                    ExerciseType.STRETCHING -> set.copy(
                        previousTime = safePreviousTime,
                        technique = safeTechnique,
                    )
                    else -> set.copy(
                        previousWeight = safePreviousWeight,
                        previousReps = safePreviousReps,
                        technique = safeTechnique,
                    )
                }
            }
            exercise.copy(exerciseSets = updatedSets)
        }
        return workout.copy(exercises = updatedExercises)
    }

    override suspend fun completeWorkout(
        workout: Workout,
        workoutHistoryRepository: WorkoutHistoryRepository,
        onWorkoutFinished: (Workout) -> Unit,
        onNavigateToReport: (String) -> Unit,
        resetWorkout: suspend (String) -> Unit,
        sessionManager: SessionManager
    ) {
        val completedExercises = workout.exercises.map { exercise ->
            val completedSets = exercise.exerciseSets.map { set ->
                set.copy(isCompleted = true)
            }
            exercise.copy(
                isCompleted = true, exerciseSets = completedSets
            )
        }

        val now = System.currentTimeMillis()
        val duration = workout.accumulatedTime + if (workout.startTime != null) {
            now - workout.startTime
        } else {
            0L
        }

        val completionDate = LocalDate.now()
        val completionTime = LocalTime.now()

        val completedWorkout = workout.copy(
            exercises = completedExercises,
            isCompleted = true,
            completionDate = completionDate,
            completionTime = completionTime,
            progress = 1f
        )

        println("USER ID DEBUG HISTORY: ${sessionManager.getUserId()}")
        val newHistoryEntry = generateHistoryEntry(
            workout = workout,
            completionDate = completionDate,
            completionTime = completionTime,
            completedExercises = completedExercises,
            duration = duration,
            userId = sessionManager.getUserId()
        )

        val historyId = newHistoryEntry.id

        onWorkoutFinished(completedWorkout)

        withContext(Dispatchers.IO) {
            val (strengthRecords, cardioRecords) = updateExerciseRecords(completedWorkout)
            val volumeRecords = updateVolumeRecord(completedWorkout)

            val newHistoryEntryRecords = newHistoryEntry.copy(
                records = Records(
                    exercisesRecordMap = strengthRecords,
                    volumeRecords = volumeRecords,
                    cardioRecordsMap = cardioRecords
                )
            )

            println("USER ID DEBUG HISTORY22222222222: $newHistoryEntryRecords")

            workoutHistoryRepository.addWorkoutHistory(newHistoryEntryRecords)

            resetWorkout(historyId)

            delay(1000)
            onNavigateToReport(historyId)
        }
    }

    private fun generateHistoryEntry(
        workout: Workout,
        completionDate: LocalDate,
        completionTime: LocalTime,
        completedExercises: List<Exercise> = emptyList(),
        duration: Long,
        userId: UUID?
    ): WorkoutHistory {
        val workoutId = workout.id

        return WorkoutHistory(
            id = workout.historyId ?: UUID.randomUUID().toString(),
            name = workout.name,
            completionDate = completionDate,
            completionTime = completionTime,
            exercises = completedExercises,
            workoutId = workoutId,
            difficulty = WorkoutDifficulty.MEDIUM,
            durationMillis = duration,
            userId = userId.toString(),
            isCompleted = true
        )
    }
}
