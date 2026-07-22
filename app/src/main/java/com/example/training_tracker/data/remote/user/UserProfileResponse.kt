package com.example.training_tracker.data.remote.user

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("picture")
    val picture: String,

    @SerializedName("username")
    val username: String
)