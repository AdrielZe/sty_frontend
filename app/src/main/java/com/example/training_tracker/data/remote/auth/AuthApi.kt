package com.example.training_tracker.data.remote.auth

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("/auth/login")
    suspend fun login(@Body login: LoginRequest) : LoginResponse;
 }