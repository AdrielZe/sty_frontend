package com.example.training_tracker.fakes

import com.example.training_tracker.data.models.User
import com.example.training_tracker.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository(initial: User? = null) : UserRepository {
    val userFlow = MutableStateFlow(initial)
    var lastWeeklyGoal: Int? = null

    override fun getUser() = userFlow
    override suspend fun insertUser(user: User) { userFlow.value = user }
    override suspend fun updateWeeklyGoal(goal: Int) {
        lastWeeklyGoal = goal
        userFlow.value = userFlow.value?.copy(weeklyGoal = goal)
    }
    override suspend fun updateProfilePicture(uri: String) {
        userFlow.value = userFlow.value?.copy(profilePicture = uri)
    }
    override suspend fun updateUserName(newName: String) {
        userFlow.value = userFlow.value?.copy(name = newName)
    }
    override suspend fun updateBodyData(weightKg: Float?, ageYears: Int?, gender: String?) {
        userFlow.value = userFlow.value?.copy(weightKg = weightKg, ageYears = ageYears, gender = gender)
    }
    override suspend fun updateAccentTheme(themeName: String) {
        userFlow.value = userFlow.value?.copy(accentThemeName = themeName)
    }
}
