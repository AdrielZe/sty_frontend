package com.example.training_tracker.fakes

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.domain.repository.ExerciseRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExerciseRepository(initial: List<Exercise> = emptyList()) : ExerciseRepository {
    private val _exercises = MutableStateFlow(initial)
    override val exercises = _exercises

    override suspend fun addExercise(exercise: Exercise) {
        _exercises.value = _exercises.value + exercise
    }
    override suspend fun updateExercise(exercise: Exercise) {
        _exercises.value = _exercises.value.map { if (it.id == exercise.id) exercise else it }
    }
    override suspend fun deleteExercise(exercise: Exercise) {
        _exercises.value = _exercises.value.filterNot { it.id == exercise.id }
    }
}
