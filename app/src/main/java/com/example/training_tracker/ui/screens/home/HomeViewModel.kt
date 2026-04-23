package com.example.training_tracker.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.repository.UserRepository
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

class HomeViewModel(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    val uiState = combine(
        userRepository.getUser(),
        workoutRepository.getWorkoutsByDay(LocalDate.now().dayOfWeek),
        workoutHistoryRepository.getHistoryByDate(LocalDate.now()),
        workoutHistoryRepository.workoutHistories
    ) { user, workouts, todayHistory, workoutHistories ->

        // Mapeia os treinos do template para ver se foram feitos hoje
        val workoutsWithStatus = workouts.map { workout ->
            val isCompletedToday = todayHistory.any { history -> history.workoutId == workout.id }
            workout.copy(isCompleted = isCompletedToday)
        }

        // Calcula treinos feitos na semana atual (Segunda a Domingo)
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        
        val workoutsThisWeek = workoutHistories.count { history ->
            val historyDate = history.completionDate
            (historyDate.isEqual(startOfWeek) || historyDate.isAfter(startOfWeek)) && 
            (historyDate.isEqual(endOfWeek) || historyDate.isBefore(endOfWeek))
        }

        HomeUiState(
            user = user,
            currentDate = getCurrentDate(),
            todayWorkouts = workoutsWithStatus,
            totalWorkoutsCompleted = workoutHistories.size,
            workoutsCompletedThisWeek = workoutsThisWeek
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(500),
        initialValue = HomeUiState()
    )

    fun updateWeeklyGoal(goal: Int) {
        viewModelScope.launch {
            userRepository.updateWeeklyGoal(goal)
        }
    }

    private fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("pt", "BR"))
        return LocalDate.now().format(formatter)
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val userRepository = application.container.userRepository
                val workoutRepository = application.container.workoutRepository
                val workoutHistoryRepository = application.container.workoutHistoryRepository

                HomeViewModel(userRepository = userRepository, workoutRepository = workoutRepository, workoutHistoryRepository = workoutHistoryRepository)
            }
        }
    }
}