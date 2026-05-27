package com.example.training_tracker.ui.screens.workout_report

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.ui.utils.CalorieCalculator
import com.example.training_tracker.ui.utils.countTotalReps
import com.example.training_tracker.ui.utils.countTotalSets
import com.example.training_tracker.ui.utils.generateHeroTitle
import com.example.training_tracker.ui.utils.generateTotalWeightedInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class WorkoutReportViewModel(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    private val userRepository: UserRepository,
    applicationContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<WorkoutReportUiState> = combine(
        workoutHistoryRepository.getWorkoutById(workoutId),
        userRepository.getUser()
    ) { workout, user ->
        Log.d("DEBUG", "Buscando treino com ID: $workoutId")
        val heroTitle = workout?.difficulty.generateHeroTitle(context = applicationContext)
        val totalWeightLiftedInfo = generateTotalWeightedInfo(
            context = applicationContext,
            workout?.exercises ?: emptyList()
        )
        val totalSets = countTotalSets(workout?.exercises ?: emptyList())
        val totalReps = countTotalReps(workout?.exercises ?: emptyList())

        val durationMinutes = if (workout != null) {
            (workout.durationMillis / 60000).toInt()
        } else 0

        val calories = if (workout != null && user?.weightKg != null) {
            CalorieCalculator.calculate(
                exercises = workout.exercises,
                durationMs = workout.durationMillis,
                weightKg = user.weightKg,
                ageYears = user.ageYears,
                gender = user.gender
            )
        } else null

        val sessionPRs: Map<String, Double> = workout?.records
            ?.exercisesRecordMap
            ?.mapValues { (_, list) -> list.firstOrNull() ?: 0.0 }
            ?.filter { (_, weight) -> weight > 0.0 }
            ?: emptyMap()

        WorkoutReportUiState(
            workoutName = workout?.name ?: "",
            workoutDifficulty = workout?.difficulty,
            heroSectionTitle = heroTitle,
            completionDate = workout?.completionDate,
            completionTime = workout?.completionTime,
            totalWeightLiftedInfo = totalWeightLiftedInfo,
            totalSets = totalSets,
            totalReps = totalReps,
            totalMinutes = durationMinutes,
            exercises = workout?.exercises ?: emptyList(),
            records = workout?.records,
            caloriesBurned = calories,
            sessionPRs = sessionPRs,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = WorkoutReportUiState()
    )

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutHistoryRepository = application.container.workoutHistoryRepository
                val userRepository = application.container.userRepository
                WorkoutReportViewModel(
                    workoutHistoryRepository = workoutHistoryRepository,
                    userRepository = userRepository,
                    savedStateHandle = createSavedStateHandle(),
                    applicationContext = application
                )
            }
        }
    }
}
