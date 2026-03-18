package com.example.training_tracker.data.models

data class User(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
)