package com.example.training_tracker.domain.classifiers

interface ExerciseClassifier {
    fun classify(exerciseName: String) : String?
}