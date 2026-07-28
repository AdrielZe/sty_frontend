package com.example.training_tracker.data.remote.user

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class UserProfileResponse(
    @SerializedName("id")
    val userId: UUID,

    @SerializedName("picture")
    val picture: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("weeklyGoal")
    val weeklyGoal: Int
)