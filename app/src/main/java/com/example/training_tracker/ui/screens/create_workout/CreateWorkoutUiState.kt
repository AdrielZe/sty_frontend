package com.example.training_tracker.ui.screens.create_workout

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.MuscleGroups
import java.time.DayOfWeek

data class CreateWorkoutUiState(
    val workoutName: String = "",
    val selectedDay: DayOfWeek? = null,
    val exercises: List<Exercise> = emptyList(),
    val availableExercises: List<Exercise> = emptyList(),
    val isWorkoutSaved: Boolean = false,
    val showErrors: Boolean = false,
    val pendingExerciseName: String? = null,
    val showMuscleGroupPicker: Boolean = false
) {
    val isNameValid = workoutName.isNotBlank()
    val isDayValid = selectedDay != null
    val isExercisesValid = exercises.isNotEmpty()
    
    val canSave = isNameValid && isDayValid && isExercisesValid
}