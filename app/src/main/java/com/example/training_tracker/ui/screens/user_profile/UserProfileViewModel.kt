package com.example.training_tracker.ui.screens.user_profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val userRepository: UserRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    val uiState: StateFlow<UserProfileUiState> = combine(
        userRepository.getUser(),
        workoutHistoryRepository.workoutHistories
    ) { user, histories ->
        if (user != null) {
            val stats = calculateStats(histories)
            UserProfileUiState.Success(user, stats)
        } else {
            UserProfileUiState.Error("Usuário não encontrado")
        }
    }.catch { e ->
        emit(UserProfileUiState.Error(e.message ?: "Erro desconhecido"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserProfileUiState.Loading
    )

    private fun calculateStats(histories: List<com.example.training_tracker.data.models.WorkoutHistory>): UserStats {
        if (histories.isEmpty()) return UserStats()

        var totalSets = 0
        var maxVolume = 0.0
        var heaviestWeight = 0.0
        var heaviestExerciseName: String? = null
        val muscleGroupCount = mutableMapOf<MuscleGroups, Int>()

        histories.forEach { history ->
            var workoutVolume = 0.0
            history.exercises.forEach { exercise ->
                val muscleGroup = exercise.muscleGroup
                if (muscleGroup != null) {
                    muscleGroupCount[muscleGroup] = muscleGroupCount.getOrDefault(muscleGroup, 0) + 1
                }

                exercise.exerciseSets.forEach { set ->
                    totalSets++
                    val weight = set.weight.toDoubleOrNull() ?: 0.0
                    val reps = set.reps.toIntOrNull() ?: 0
                    workoutVolume += weight * reps

                    if (weight > heaviestWeight) {
                        heaviestWeight = weight
                        heaviestExerciseName = exercise.name
                    }
                }
            }
            if (workoutVolume > maxVolume) {
                maxVolume = workoutVolume
            }
        }

        val mostTrainedMuscleGroup = muscleGroupCount.maxByOrNull { it.value }?.key
        val medalCounts = calculateMedalCounts(histories)

        return UserStats(
            mostTrainedMuscleGroup = mostTrainedMuscleGroup,
            maxVolume = maxVolume,
            totalWorkouts = histories.size,
            totalSets = totalSets,
            heaviestExerciseName = heaviestExerciseName,
            heaviestWeight = heaviestWeight,
            medalCounts = medalCounts
        )
    }

    /**
     * Computes all-time best weight per exercise, then tiers each one:
     * Gold ≥ 100 kg · Silver ≥ 50 kg · Bronze < 50 kg
     */
    private fun calculateMedalCounts(
        histories: List<com.example.training_tracker.data.models.WorkoutHistory>
    ): MedalCounts {
        val bestPerExercise = mutableMapOf<String, Double>()

        histories.sortedBy { it.completionDate }.forEach { history ->
            history.exercises.forEach { exercise ->
                if (exercise.type != com.example.training_tracker.data.models.ExerciseType.STRENGTH) return@forEach
                val key = exercise.name.uppercase()
                val sessionMax = exercise.exerciseSets
                    .filter { it.isCompleted }
                    .mapNotNull { it.weight.toDoubleOrNull() }
                    .filter { it > 0.0 }
                    .maxOrNull() ?: return@forEach
                if (sessionMax > (bestPerExercise[key] ?: 0.0)) {
                    bestPerExercise[key] = sessionMax
                }
            }
        }

        var gold = 0; var silver = 0; var bronze = 0
        bestPerExercise.values.forEach { best ->
            when {
                best >= 100.0 -> gold++
                best >= 50.0  -> silver++
                else          -> bronze++
            }
        }
        return MedalCounts(gold = gold, silver = silver, bronze = bronze)
    }

    fun updateProfilePicture(uri: String) {
        viewModelScope.launch {
            userRepository.updateProfilePicture(uri)
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            userRepository.updateUserName(newName)
        }
    }

    fun updateBodyData(weightKg: Float?, ageYears: Int?, gender: String?) {
        viewModelScope.launch {
            userRepository.updateBodyData(weightKg, ageYears, gender)
        }
    }

    fun updateAccentTheme(themeName: String) {
        viewModelScope.launch {
            userRepository.updateAccentTheme(themeName)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val userRepository = application.container.userRepository
                val workoutHistoryRepository = application.container.workoutHistoryRepository
                UserProfileViewModel(
                    userRepository = userRepository,
                    workoutHistoryRepository = workoutHistoryRepository
                )
            }
        }
    }
}
