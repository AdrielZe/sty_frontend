package com.example.training_tracker.data.remote.auth

import java.util.UUID

data class RegisterRequest(
    val id: UUID,
    val name: String,
    val email: String,
    val password: String,
    val role: String = "USER"
) {
}