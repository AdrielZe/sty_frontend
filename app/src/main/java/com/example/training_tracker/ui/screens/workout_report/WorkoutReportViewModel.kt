package com.example.training_tracker.ui.screens.workout_report

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

class WorkoutReportViewModel(
    val workoutHistoryRepository: WorkoutHistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val workoutId: String = checkNotNull(savedStateHandle["workoutId"])
    private val _uiEvent = Channel<String>()

    val uiEvent = _uiEvent.receiveAsFlow()

    val uiState: StateFlow<WorkoutReportUiState> = workoutHistoryRepository
        .getWorkoutById(workoutId)
        .map { workout ->

            WorkoutReportUiState(
                workoutDifficulty = workout?.difficulty,//Criar enum de workout difficulty em workout e workout history
                heroSectionTitle = ,//Gerar dentro da view model um texto aleatorio com base na dificuldade do treino
                completionDate = workout.completionDate,
                totalWeightLiftedInfo =, // calcular aqui com base nos exercises feitos (montar o objeto TotalWeightLiftedInfo)
                totalSets = ,//calcular aqui com base nos exercises feitos
                totalReps = ,//calcular aqui com base nos exercises feitos
                totalMinutes = ,//calcular aqui com base nos exercises feitos
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(500),
            initialValue = WorkoutReportUiState()
        )
}