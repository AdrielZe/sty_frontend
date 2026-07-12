package com.example.training_tracker.data.remote.auth

import java.util.UUID

data class LoginResponse (
    val name: String,
    val id: UUID
)