package com.example.training_tracker.data.remote.history

import com.example.training_tracker.data.models.WorkoutHistory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface HistoryApi {
    @POST("/history")
    suspend fun createHistory(@Body historyRequest: HistoryRequestDto): WorkoutHistory

    @POST("/history/all")
    suspend fun createAll(@Body historyRequest: AllHistoryRequestDto): HistoryResponseDto

    @GET("/history")
    suspend fun getHistories(
        @Query("userId") userId: String,
    ) : HistoryResponseDto


}