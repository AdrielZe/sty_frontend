package com.example.training_tracker.data.remote.user

import com.google.gson.annotations.SerializedName

data class PictureResponse(
    @SerializedName("picture_url")
    val url: String
)