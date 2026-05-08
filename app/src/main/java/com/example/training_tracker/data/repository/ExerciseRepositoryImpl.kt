package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.ExerciseDao
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.domain.repository.ExerciseRepository

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {
    override val exercises = exerciseDao.getAllExercises()

    override suspend fun addExercise(exercise: Exercise) {
        exerciseDao.insert(exercise)
    }

    override suspend fun updateExercise(exercise: Exercise) {
        exerciseDao.update(exercise)
    }

    override suspend fun deleteExercise(exercise: Exercise) {
        exerciseDao.delete(exercise)
    }

}