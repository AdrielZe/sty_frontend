package com.example.training_tracker.ui.screens.exercise_data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

const val EXERCISE_NAME_ARG = "exerciseName"

class ExerciseDetailViewModel(
    private val exerciseName: String,
    workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    val uiState: StateFlow<ExerciseDetailUiState> = workoutHistoryRepository.workoutHistories
        .map { histories ->
            val today = LocalDate.now()
            val allLogs = ExerciseDataAggregator.build(histories)
            val log = allLogs.find { it.name.equals(exerciseName, ignoreCase = true) }

            // --- Cálculo do destaque do dia ---
            // Para cada exercício feito hoje, calcula a variação de volume vs última sessão anterior.
            data class DayScore(val name: String, val changePct: Double?, val volume: Double, val peak: Double)

            val todayScores: List<DayScore> = allLogs.mapNotNull { exLog ->
                val todaySessions = exLog.sessions.filter { it.date == today }
                val todaySession = todaySessions.lastOrNull() ?: return@mapNotNull null
                // Compara com o dia anterior; se não houver, compara com sessão anterior do mesmo dia.
                val prevSession = exLog.sessions.lastOrNull { it.date < today }
                    ?: if (todaySessions.size > 1) todaySessions.dropLast(1).last() else null
                val changePct = prevSession?.let { prev ->
                    if (prev.totalVolume > 0.0)
                        (todaySession.totalVolume - prev.totalVolume) / prev.totalVolume * 100.0
                    else null
                }
                DayScore(exLog.name, changePct, todaySession.totalVolume, todaySession.topWeight)
            }

            // O destaque é o exercício com maior % de aumento.
            // Se nenhum tem sessão anterior (todos pela primeira vez), usa o maior volume absoluto.
            val topHighlightName: String? = if (todayScores.isEmpty()) {
                null
            } else {
                val withChange = todayScores.filter { it.changePct != null }
                if (withChange.isNotEmpty()) {
                    withChange.maxByOrNull { it.changePct!! }?.name
                } else {
                    todayScores.maxByOrNull { it.volume }?.name
                }
            }

            val thisScore = todayScores.find { it.name.equals(exerciseName, ignoreCase = true) }
            val todayHighlight = thisScore?.let { score ->
                val logTodaySessions = log?.sessions?.filter { it.date == today } ?: emptyList()
                val prevSession = log?.sessions?.lastOrNull { it.date < today }
                    ?: if (logTodaySessions.size > 1) logTodaySessions.dropLast(1).last() else null
                val peakChangePct = prevSession?.let { prev ->
                    if (prev.topWeight > 0.0)
                        (score.peak - prev.topWeight) / prev.topWeight * 100.0
                    else null
                }
                TodayHighlightData(
                    todayVolume = score.volume,
                    volumeChangePct = score.changePct,
                    todayPeak = score.peak,
                    peakChangePct = peakChangePct,
                    isTopHighlight = topHighlightName?.equals(exerciseName, ignoreCase = true) == true
                )
            }

            ExerciseDetailUiState(isLoading = false, log = log, todayHighlight = todayHighlight)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExerciseDetailUiState(isLoading = true)
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val savedStateHandle = createSavedStateHandle()
                val name = savedStateHandle.get<String>(EXERCISE_NAME_ARG).orEmpty()
                ExerciseDetailViewModel(
                    exerciseName = name,
                    workoutHistoryRepository = application.container.workoutHistoryRepository
                )
            }
        }
    }
}
