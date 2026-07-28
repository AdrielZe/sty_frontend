package com.example.training_tracker.data.remote.user

import android.graphics.Picture
import com.example.training_tracker.data.models.User
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface UserApi {
    @Multipart
    @POST("/user/{id}/picture")
    suspend fun updateUserProfilePicture(
        @Path("id") userId: UUID,
        @Part file: MultipartBody.Part
    ) : PictureResponse

    @GET("/user/{id}/picture")
    suspend fun getUserProfilePicture(@Path("id") userId: UUID) : PictureResponse

    @GET("/user/{id}")
    suspend fun getUserProfile(@Path("id") userId: UUID): UserProfileResponse

    @POST("/user/{id}/weeklyGoal")
    suspend fun setUserWeeklyGoal(
        @Path("id") userId: UUID,
        @Body request: WeeklyGoalRequestDto
    ) : UserProfileResponse
}