package com.example.training_tracker.data.remote.auth

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("id")
    val userId: String
) {
}