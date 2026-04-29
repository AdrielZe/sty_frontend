package com.example.training_tracker.ui.screens.workout_screen

import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.RecordsRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface WorkoutDelegate {
    fun calculateProgress(workout: Workout): Float
    suspend fun togglePauseWorkout(workout: Workout)
    suspend fun addNewSetLine(workout: Workout, exerciseId: String)
    suspend fun removeSetLine(workout: Workout, exerciseId: String, setNumber: Int)
    suspend fun completeSet(workout: Workout, exerciseId: String, setNumber: Int)
    suspend fun updateExercise(workout: Workout, exerciseId: String, setNumber: Int, newReps: String? = null, newWeight: String? = null)
    suspend fun completeExercise(workout: Workout, exerciseId: String)
    suspend fun reopenExercise(workout: Workout, exerciseId: String)
    suspend fun updateExerciseRecords(workout: Workout): MutableMap<String, MutableList<Int>>
    suspend fun updateVolumeRecord(workout: Workout): MutableList<Int>?
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

    override suspend fun addNewSetLine(workout: Workout, exerciseId: String) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val maxSetNumber = exercise.exerciseSets.maxOfOrNull { it.set } ?: 0
                exercise.copy(exerciseSets = exercise.exerciseSets + ExerciseSet(set = maxSetNumber + 1))
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(updatedWorkout.copy(progress = calculateProgress(updatedWorkout)))
    }

    override suspend fun removeSetLine(workout: Workout, exerciseId: String, setNumber: Int) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                exercise.copy(exerciseSets = exercise.exerciseSets.filter { it.set != setNumber })
            } else exercise
        }
        val updatedWorkout = workout.copy(exercises = updatedExercises)
        workoutRepository.updateWorkout(updatedWorkout.copy(progress = calculateProgress(updatedWorkout)))
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
        workoutRepository.updateWorkout(updatedWorkout.copy(progress = calculateProgress(updatedWorkout)))
    }

    override suspend fun updateExercise(
        workout: Workout,
        exerciseId: String,
        setNumber: Int,
        newReps: String?,
        newWeight: String?
    ) {
        val updatedExercises = workout.exercises.map { exercise ->
            if (exercise.id == exerciseId) {
                val updatedSets = exercise.exerciseSets.map { set ->
                    if (set.set == setNumber) {
                        set.copy(reps = newReps ?: set.reps, weight = newWeight ?: set.weight)
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
        workoutRepository.updateWorkout(updatedWorkout.copy(progress = calculateProgress(updatedWorkout)))
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
        workoutRepository.updateWorkout(updatedWorkout.copy(progress = calculateProgress(updatedWorkout)))
    }

    override suspend fun updateExerciseRecords(workout: Workout): MutableMap<String, MutableList<Int>> {
        return withContext(Dispatchers.IO) {
            val mapOfRecords: MutableMap<String, MutableList<Int>> = mutableMapOf()
            var records = recordsRepository.getRecord()
            var isFirstTime = false

            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            var recordsUpdated = false

            workout.exercises.forEach { exercise ->
                val maxWeightThisWorkout = exercise.exerciseSets.maxByOrNull {
                    it.weight.toDoubleOrNull() ?: 0.0
                }?.weight?.toInt()

                if (maxWeightThisWorkout != null) {
                    val exerciseRecords =
                        records!!.exercisesRecordMap.getOrPut(exercise.name) { mutableListOf() }
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
                    recordsRepository.addRecord(records!!)
                } else {
                    recordsRepository.updateRecord(records!!)
                }
            }

            mapOfRecords
        }
    }

    override suspend fun updateVolumeRecord(workout: Workout): MutableList<Int>? {
        return withContext(Dispatchers.IO) {
            var records = recordsRepository.getRecord()
            var isFirstTime = false

            if (records == null) {
                records = Records()
                isFirstTime = true
            }

            val totalVolumeWorkout = workout.exercises.sumOf { exercise ->
                exercise.exerciseSets.sumOf { set ->
                    val weight = set.weight.toDoubleOrNull() ?: 0.0
                    val reps = set.reps.toIntOrNull() ?: 0
                    weight * reps
                }
            }.toInt()

            val bestVolume = records!!.volumeRecords?.firstOrNull() ?: 0

            if (totalVolumeWorkout > bestVolume) {
                if (records!!.volumeRecords == null) records!!.volumeRecords = mutableListOf()
                records!!.volumeRecords?.add(0, totalVolumeWorkout)

                if (isFirstTime) {
                    recordsRepository.addRecord(records!!)
                } else {
                    recordsRepository.updateRecord(records!!)
                }

                return@withContext records!!.volumeRecords
            }

            if (isFirstTime) {
                recordsRepository.addRecord(records!!)
            }

            null
        }
    }
}
