package com.example.training_tracker.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.ui.utils.CalorieCalculator
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

private const val TAG = "HomeViewModel"
class HomeViewModel(
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    private val FREESTYLE_WORKOUT_ID = "freestyle_workout_id"

    private val baseState = combine(
        userRepository.getUser(),
        workoutRepository.getWorkoutsByDay(LocalDate.now().dayOfWeek),
        workoutHistoryRepository.getHistoryByDate(LocalDate.now()),
        workoutHistoryRepository.workoutHistories,
        workoutRepository.getWorkoutById(FREESTYLE_WORKOUT_ID)
    ) { user, workouts, todayHistory, workoutHistories, freestyleWorkout ->
        // Mapeia os treinos do template para ver se foram feitos hoje
        val workoutsWithStatus = workouts.map { workout ->
            val isCompletedToday = todayHistory.any { history -> history.workoutId == workout.id }
            workout.copy(isCompleted = isCompletedToday)
        }.toMutableList()

        // Adiciona o Freestyle Workout se ele estiver em andamento (isOnGoing == true)
        val activeFreestyle = if (freestyleWorkout != null && freestyleWorkout.isOnGoing) {
            freestyleWorkout
        } else {
            null
        }

        if (activeFreestyle != null) {
            workoutsWithStatus.add(activeFreestyle)
        }

        // Calcula treinos feitos na semana atual (Segunda a Domingo)
        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val thisWeekHistories = workoutHistories.filter { history ->
            val historyDate = history.completionDate
            (historyDate.isEqual(startOfWeek) || historyDate.isAfter(startOfWeek)) &&
            (historyDate.isEqual(endOfWeek) || historyDate.isBefore(endOfWeek))
        }

        val weeklyCalories = if (user?.weightKg != null) {
            thisWeekHistories.sumOf { history ->
                CalorieCalculator.calculate(
                    exercises = history.exercises,
                    durationMs = history.durationMillis,
                    weightKg = user.weightKg,
                    ageYears = user.ageYears,
                    gender = user.gender
                )
            }
        } else 0

        val completedPerDay = thisWeekHistories
            .groupingBy { it.completionDate.dayOfWeek }
            .eachCount()

        HomeUiState.Success(
            user = user,
            currentDate = getCurrentDate(),
            todayWorkouts = workoutsWithStatus,
            totalWorkoutsCompleted = workoutHistories.size,
            workoutsCompletedThisWeek = thisWeekHistories.size,
            activeFreestyleWorkout = activeFreestyle,
            weeklyCalories = weeklyCalories,
            completedWorkoutsPerDayOfWeek = completedPerDay,
        ) as HomeUiState
    }

    val uiState: StateFlow<HomeUiState> = combine(
        baseState,
        workoutRepository.workouts
    ) { state, allWorkouts ->
        if (state !is HomeUiState.Success) return@combine state
        val currentWeekMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val perDay = mutableMapOf<DayOfWeek, Int>()
        for (workout in allWorkouts.filter { it.id != FREESTYLE_WORKOUT_ID }) {
            val isRescheduledThisWeek = workout.rescheduledWeekStart == currentWeekMonday && workout.rescheduledToDayOfWeek != null
            if (isRescheduledThisWeek) {
                val newDay = workout.rescheduledToDayOfWeek!!
                perDay[newDay] = (perDay[newDay] ?: 0) + 1
            } else if (workout.dayOfWeek != null) {
                val day = workout.dayOfWeek
                perDay[day] = (perDay[day] ?: 0) + 1
            }
        }
        state.copy(workoutsPerDayOfWeek = perDay)
    }
    .catch { e ->
        emit(HomeUiState.Error(e.message))
    }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState.Loading
    )

    fun updateWeeklyGoal(goal: Int) {
        viewModelScope.launch {
            userRepository.updateWeeklyGoal(goal)
        }
    }

    fun startFreestyleWorkout(name: String, onConfirm: () -> Unit) {
        viewModelScope.launch {
            val existing = workoutRepository.getWorkoutById(FREESTYLE_WORKOUT_ID).first()
            if (existing != null) {
                workoutRepository.updateWorkout(existing.copy(name = name, isOnGoing = true, startTime = System.currentTimeMillis()))
            } else {
                workoutRepository.addWorkout(
                    Workout(
                        id = FREESTYLE_WORKOUT_ID,
                        name = name,
                        isOnGoing = true,
                        startTime = System.currentTimeMillis()
                    )
                )
            }
            onConfirm()
        }
    }

    fun removeFreestyleWorkout(){
        viewModelScope.launch {
            try {
                val existing = workoutRepository.getWorkoutById(FREESTYLE_WORKOUT_ID).first()
                existing?.let {
                    workoutRepository.deleteWorkout(existing)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao remover treino", e)
            }
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
