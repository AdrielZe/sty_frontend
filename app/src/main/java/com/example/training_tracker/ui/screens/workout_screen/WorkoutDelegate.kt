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
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime

interface WorkoutDelegate {
    fun calculateProgress(workout: Workout): Float
    suspend fun togglePauseWorkout(workout: Workout)
    suspend fun addNewSetLine(workout: Workout, exerciseId: String)
    suspend fun removeSetLine(workout: Workout, exerciseId: String, setNumber: Int)
    suspend fun completeSet(workout: Workout, exerciseId: String, setNumber: Int)
    suspend fun updateExercise(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        newReps: String? = null,
        newWeight: String? = null
    )

    suspend fun completeExercise(workout: Workout, exerciseId: String)
    suspend fun reopenExercise(workout: Workout, exerciseId: String)
    suspend fun updateExerciseRecords(workout: Workout): MutableMap<String, MutableList<Double>>
    suspend fun updateVolumeRecord(workout: Workout): MutableList<Double>?
    suspend fun updateSetTechnique(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        technique: Technique
    )

    fun enrichWorkoutWithHistory(workout: Workout, histories: List<WorkoutHistory>): Workout

    suspend fun completeWorkout(
        workout: Workout,
        workoutHistoryRepository: WorkoutHistoryRepository,
        onWorkoutFinished: (Workout) -> Unit,
        onNavigateToReport: (String) -> Unit,
        resetWorkout: suspend (historyId: String) -> Unit
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

    override suspend fun togglePauseWorkout(workout: Workout) {
        val now = System.currentTimeMillis()
        val updatedWorkout = if (workout.isPaused) {
            workout.copy(isPaused = false, startTime = now)
        } else {
            val elapsedSinceStart = if (workout.startTime != null) now - workout.startTime else 0L
            workout.copy(
                isPaused = true,
                accumulatedTime = workout.accumulatedTime + elapsedSinceStart,
                startTime = null
            )
        }
        workoutRepository.updateWorkout(updatedWorkout)
    }

    override suspend fun updateSetTechnique(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        technique: Technique
    ) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        set.copy(technique = technique)
                    } else {
                        set
                    }
                }
                exercise.copy(exerciseSets = updatedSets)
            } else {
                exercise
            }
        }

        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(updatedWorkout)
    }

    override suspend fun addNewSetLine(workout: Workout, exerciseId: String) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val maxSetNumber = exercise.exerciseSets.maxOfOrNull { it.set } ?: 0
                exercise.copy(exerciseSets = exercise.exerciseSets + ExerciseSet(set = maxSetNumber + 1))
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(
            updatedWorkout.copy(
                progress = calculateProgress(
                    updatedWorkout
                )
            )
        )
    }

    override suspend fun removeSetLine(workout: Workout, exerciseId: String, setNumber: Int) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(exerciseSets = exercise.exerciseSets.filter { it.set != setNumber })
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(
            updatedWorkout.copy(
                progress = calculateProgress(
                    updatedWorkout
                )
            )
        )
    }

    override suspend fun completeSet(workout: Workout, exerciseId: String, setNumber: Int) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) set.copy(isCompleted = true) else set
                }
                exercise.copy(exerciseSets = updatedSets)
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(
            updatedWorkout.copy(
                progress = calculateProgress(
                    updatedWorkout
                )
            )
        )
    }

    override suspend fun updateExercise(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        newReps: String?,
        newWeight: String?,
    ) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        if (exercise.type == ExerciseType.CARDIO) {
                            set.copy(distance = newWeight ?: set.distance, time = newReps ?: set.time)
                        } else {
                            set.copy(reps = newReps ?: set.reps, weight = newWeight ?: set.weight)
                        }
                    } else set
                }
                exercise.copy(exerciseSets = updatedSets)
            } else exercise
        }
        workoutRepository.updateWorkout(workout.copy(exercises = updatedExercises))
    }

    override suspend fun completeExercise(workout: Workout, exerciseId: String) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(
                    isCompleted = true,
                    exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = true) })
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(
            updatedWorkout.copy(
                progress = calculateProgress(
                    updatedWorkout
                )
            )
        )
    }

    override suspend fun reopenExercise(workout: Workout, exerciseId: String) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(
                    isCompleted = false,
                    exerciseSets = exercise.exerciseSets.map { it.copy(isCompleted = false) })
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(
            updatedWorkout.copy(
                progress = calculateProgress(
                    updatedWorkout
                )
            )
        )
    }

    private fun String.parseToDouble(): Double {
        return this.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    override suspend fun updateExerciseRecords(workout: Workout): MutableMap<String, MutableList<Double>> {
        return withContext(Dispatchers.IO) {
            val mapOfRecords: MutableMap<String, MutableList<Double>> = mutableMapOf()
            var records = recordsRepository.getRecord()
            var isFirstTime = false

            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            var recordsUpdated = false

            workout.exercises.filter { it.type != ExerciseType.CARDIO }.forEach { exercise ->
                val maxWeightThisWorkout = exercise.exerciseSets.maxOfOrNull {
                    it.weight.parseToDouble()
                }

                if (maxWeightThisWorkout != null && maxWeightThisWorkout > 0.0) {
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

    override suspend fun updateVolumeRecord(workout: Workout): MutableList<Double>? {
        return withContext(Dispatchers.IO) {
            var records = recordsRepository.getRecord()
            var isFirstTime = false

            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            val totalVolumeWorkout = workout.exercises
                .filter { it.type != ExerciseType.CARDIO }
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
                if (exercise.type == ExerciseType.CARDIO) {
                    set.copy(
                        previousDistance = lastSet?.distance ?: "",
                        previousTime = lastSet?.time ?: "",
                        technique = set.technique ?: Technique.NORMAL,
                    )
                } else {
                    set.copy(
                        previousWeight = lastSet?.weight ?: "",
                        previousReps = lastSet?.reps ?: "",
                        technique = set.technique ?: Technique.NORMAL,
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
        resetWorkout: suspend (String) -> Unit
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

        val newHistoryEntry = generateHistoryEntry(
            workout,
            completionDate,
            completionTime,
            completedExercises,
            duration
        )
        val historyId = newHistoryEntry.id

        onWorkoutFinished(completedWorkout)

        withContext(Dispatchers.IO) {
            val exerciseRecords = updateExerciseRecords(completedWorkout)
            val volumeRecords = updateVolumeRecord(completedWorkout)

            val newHistoryEntryRecords = newHistoryEntry.copy(
                records = Records(
                    exercisesRecordMap = exerciseRecords,
                    volumeRecords = volumeRecords
                )
            )

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
        completedExercises: List<Exercise>,
        duration: Long
    ): WorkoutHistory {
        val workoutId =
            if (workout.id == "freestyle_workout_id") "freestyle_workout_id" else workout.id

        return WorkoutHistory(
            name = workout.name,
            completionDate = completionDate,
            completionTime = completionTime,
            exercises = completedExercises,
            workoutId = workoutId,
            difficulty = WorkoutDifficulty.MEDIUM,
            durationMillis = duration
        )
    }
}
