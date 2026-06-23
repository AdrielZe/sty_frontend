package com.example.training_tracker.data.remote.exercise

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ExerciseApi {
    @POST("/exercise")
    suspend fun addExercise(@Body exercise: ExerciseRequest): Response<ExerciseResponse>
}