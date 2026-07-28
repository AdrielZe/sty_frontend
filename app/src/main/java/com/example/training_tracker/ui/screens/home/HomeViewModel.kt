package com.example.training_tracker.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.isFreestyleWorkout
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.ui.utils.CalorieCalculator
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val workoutHistoryRepository: WorkoutHistoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val todayWorkoutsFlow = sessionManager.userIdFlow.flatMapLatest { userId ->
        println("EXECUTADO! USER ID: ${userId}")
        if (userId != null) {
            workoutRepository.getWorkoutsByDay(LocalDate.now().dayOfWeek, userId)
        } else {
            flowOf(null)
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    private val freestyleWorkoutFlow = sessionManager.userIdFlow.flatMapLatest { userId ->
        if (userId != null) {
            workoutRepository.getWorkoutById(Workout.freestyleWorkoutId(userId.toString()))
        } else {
            flowOf(null)
        }
    }

    private val baseState = combine(
        userRepository.getUser(),
        todayWorkoutsFlow,
        workoutHistoryRepository.getHistoryByDate(LocalDate.now()),
        workoutHistoryRepository.workoutHistories,
        freestyleWorkoutFlow
    ) { user, workouts, todayHistory, workoutHistories, freestyleWorkout ->

        val workoutsWithStatus = workouts?.filter { !it.isFreestyleWorkout() }?.map { workout ->
            val isCompletedToday = todayHistory.any { history ->
                history.workoutId == workout.id && history.isCompleted
            }

            val draftHistory = workoutHistories.find { history ->
                history.workoutId == workout.id && !history.isCompleted && history.completionDate == LocalDate.now()
            }

            if (isCompletedToday) {
                workout.copy(isCompleted = true, progress = 1f, isOnGoing = false)

            } else if (draftHistory != null) {
                val totalExercises = draftHistory.exercises.size
                val completedExercises = draftHistory.exercises.count { it.isCompleted }
                val currentProgress = if (totalExercises > 0) completedExercises.toFloat() / totalExercises else 0f

                workout.copy(
                    isCompleted = false,
                    isOnGoing = true,
                    progress = currentProgress
                )

            } else {
                workout.copy(isCompleted = false, isOnGoing = false, progress = 0f)
            }
        }?.toMutableList()

        val activeFreestyle = if (freestyleWorkout != null && freestyleWorkout.isOnGoing) {
            freestyleWorkout
        } else {
            null
        }

        if (activeFreestyle != null) {
            workoutsWithStatus?.add(activeFreestyle)
        }

        val today = LocalDate.now()
        val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

        val thisWeekHistories = workoutHistories.filter { history ->
            val historyDate = history.completionDate
            history.isCompleted &&
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
            totalWorkoutsCompleted = thisWeekHistories.size,
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
        for (workout in allWorkouts.filter { !it.isFreestyleWorkout() }) {
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
            val userId = sessionManager.userIdFlow.first() ?: return@launch
            val freestyleId = Workout.freestyleWorkoutId(userId.toString())
            val existing = workoutRepository.getWorkoutById(freestyleId).first()

            if (existing != null && existing.isOnGoing) {
                onConfirm()
                return@launch
            }

            if (existing != null) {
                workoutRepository.updateWorkout(existing.copy(name = name, isOnGoing = true, startTime = System.currentTimeMillis()))
            } else {
                workoutRepository.addWorkout(
                    Workout(
                        id = freestyleId,
                        name = name,
                        dayOfWeek = LocalDate.now().dayOfWeek,
                        userId = userId.toString(),
                        isOnGoing = true,
                        startTime = System.currentTimeMillis(),
                        exercises = emptyList()
                    )
                )
            }
            onConfirm()
        }
    }

    fun removeFreestyleWorkout(){
        viewModelScope.launch {
            try {
                val userId = sessionManager.userIdFlow.first() ?: return@launch
                val freestyleId = Workout.freestyleWorkoutId(userId.toString())
                val existing = workoutRepository.getWorkoutById(freestyleId).first()
                existing?.let {
                    workoutRepository.deleteWorkoutById(existing.id)
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
                val sessionManger = application.container.sessionManager

                HomeViewModel(userRepository = userRepository, workoutRepository = workoutRepository, workoutHistoryRepository = workoutHistoryRepository, sessionManager = sessionManger)
            }
        }
    }
}
