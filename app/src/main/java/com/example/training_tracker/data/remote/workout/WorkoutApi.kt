package com.example.training_tracker.data.remote.workout;


import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST;
import retrofit2.http.Path
import java.time.DayOfWeek
import java.util.UUID

interface WorkoutApi {
    @POST("/workout")
    suspend fun createWorkout(@Body workoutRequest: WorkoutRequest);

    @POST("/workout/sync")
    suspend fun createWorkouts(@Body workoutsRequest: List<WorkoutRequest>)

    @GET("/workout/user/{userId}/{dayOfWeek}")
    suspend fun getWorkoutByDay(
        @Path("userId") id: UUID,
        @Path("dayOfWeek") dayOfWeek: DayOfWeek
    ) : List<WorkoutResponse>

}