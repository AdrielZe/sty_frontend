package com.example.training_tracker.data.repository

import com.example.training_tracker.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepository {

    private val mockedUser = User(
        name = "Adriel",
    )
    private val _user = MutableStateFlow<User?>(mockedUser)

    fun getUser(): StateFlow<User?> {
        return _user.asStateFlow()
    }

    fun setUser(newUser: User) {
        _user.value = newUser
    }

}