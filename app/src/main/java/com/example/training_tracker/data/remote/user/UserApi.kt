package com.example.training_tracker.data.remote.user

import android.graphics.Picture
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.UUID

interface UserApi {

    @GET("/user/picture/{id}")
    suspend fun getUserProfilePicture(@Path("id") userId: UUID) : PictureResponse

}