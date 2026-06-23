package com.example.training_tracker.data.repository

import android.util.Log
import com.example.training_tracker.data.local.dao.ExerciseDao
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.remote.exercise.ExerciseApi
import com.example.training_tracker.data.remote.exercise.ExerciseRequest
import com.example.training_tracker.domain.repository.ExerciseRepository

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
    private val exerciseApi: ExerciseApi
) : ExerciseRepository {
    override val exercises = exerciseDao.getAllExercises()

    override suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insert(exercise)

        try {
            val response = exerciseApi.addExercise(
                ExerciseRequest(
                    id = exercise.id,
                    name = exercise.name,
                    exerciseType = exercise.type,
                    muscleGroup = exercise.muscleGroup!!
                )
            )

            if (response.isSuccessful) {
                Log.d("SYNC", "Exercício enviado com sucesso para o Spring Boot!")
                // No futuro: exerciseDao.updateSyncStatus(exercise.id, true)
            } else {
                Log.e("SYNC", "O servidor recusou: ${response.code()} - ${response.errorBody()?.string()}")
            }

        } catch (e: Exception) {
            Log.w("SYNC", "Sem internet ou falha de rede. O dado está seguro no Room para envio futuro. Erro: ${e.message}")

            // No futuro: exerciseDao.updateSyncStatus(exercise.id, false)
        }
    }

    override suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise)
    }

    override suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.delete(exercise)
    }
}