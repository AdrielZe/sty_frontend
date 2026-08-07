package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.models.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    val exercises : Flow<List<Exercise>>
    suspend fun addExercise(exercise: Exercise)
    suspend fun addExercises(exercises: List<Exercise>)
    suspend fun updateExercise(exercise: Exercise)
    suspend fun deleteExercise(exercise: Exercise)
}