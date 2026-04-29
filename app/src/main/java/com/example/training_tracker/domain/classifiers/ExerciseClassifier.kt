package com.example.training_tracker.domain.classifiers

data class ClassificationResult(
    val label: String?,
    val confidence: Float
)

interface ExerciseClassifier {
    fun classify(exerciseName: String) : ClassificationResult
}