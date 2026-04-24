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
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.ui.utils.countTotalReps
import com.example.training_tracker.ui.utils.countTotalSets
import com.example.training_tracker.ui.utils.generateHeroTitle
import com.example.training_tracker.ui.utils.generateTotalWeightedInfo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class WorkoutReportViewModel(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    applicationContext: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<WorkoutReportUiState> = workoutHistoryRepository
        .getWorkoutById(workoutId)
        .map { workout ->

            Log.d("DEBUG", "Buscando treino com ID: $workoutId")
            val heroTitle = workout?.difficulty.generateHeroTitle(context = applicationContext)
            val totalWeightLiftedInfo = generateTotalWeightedInfo(context = applicationContext,
                workout?.exercises ?: emptyList()
            )
            val totalSets = countTotalSets(workout?.exercises ?: emptyList())
            val totalReps = countTotalReps(workout?.exercises ?: emptyList())

            val durationMinutes = if (workout != null) {
                (workout.durationMillis / 60000).toInt()
            } else 0

            println("Received Records: ${workout?.records}")

            WorkoutReportUiState(
                workoutDifficulty = workout?.difficulty,
                heroSectionTitle = heroTitle,
                completionDate = workout?.completionDate,
                totalWeightLiftedInfo = totalWeightLiftedInfo,
                totalSets = totalSets,
                totalReps = totalReps,
                totalMinutes = durationMinutes,
                exercises = workout?.exercises ?: emptyList(),
                records = workout?.records
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
                WorkoutReportViewModel(
                    workoutHistoryRepository = workoutHistoryRepository,
                    savedStateHandle = createSavedStateHandle(),
                    applicationContext = application
                )
            }
        }
    }
}
