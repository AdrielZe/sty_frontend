package com.example.training_tracker.ui.screens.conquistas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class AchievementsViewModel(
    private val workoutHistoryRepository: WorkoutHistoryRepository
) : ViewModel() {

    private val _selectedYear = MutableStateFlow(YearFilter.All)
    private val _selectedDay  = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<AchievementsUiState> = combine(
        workoutHistoryRepository.workoutHistories,
        _selectedYear,
        _selectedDay
    ) { histories, yearFilter, selectedDay ->
        buildUiState(histories, yearFilter, selectedDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AchievementsUiState(isLoading = true)
    )

    fun onYearSelected(year: YearFilter) {
        _selectedYear.value = year
        _selectedDay.value = null   // limpa seleção de dia ao trocar ano
    }

    /** Seleciona ou deseleciona (toggle) um dia do mês no heatmap. */
    fun onDaySelected(day: Int) {
        _selectedDay.value = if (_selectedDay.value == day) null else day
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Main aggregation
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildUiState(
        allHistories: List<WorkoutHistory>,
        yearFilter: YearFilter,
        selectedDay: Int?
    ): AchievementsUiState {
        val sortedAll = allHistories.sortedBy { it.completionDate }

        // All PR events across full history (used for champion, medals, totalPRs)
        val allPrEvents = detectPrEvents(sortedAll)

        // PR events filtered by selected year
        val yearFiltered = if (yearFilter.year != null) {
            allPrEvents.filter { it.date.year == yearFilter.year }
        } else {
            allPrEvents
        }

        // If a day is selected in the heatmap, further filter to that day of the current month
        val today = LocalDate.now()
        val filteredPrEvents = if (selectedDay != null) {
            allPrEvents.filter {
                it.date.year        == today.year  &&
                it.date.monthValue  == today.monthValue &&
                it.date.dayOfMonth  == selectedDay
            }
        } else {
            yearFiltered
        }

        val totalPRs = allPrEvents.size

        // Champion: heaviest PR within the selected year (or all-time if no year filter)
        val champion: Champion? = yearFiltered
            .maxByOrNull { it.weightKg }
            ?.let { entry ->
                Champion(
                    exerciseName = entry.exerciseName,
                    muscleGroup = entry.muscleGroup,
                    image = null,
                    weightKg = entry.weightKg,
                    previousWeightKg = entry.previousWeightKg,
                    deltaKg = entry.deltaKg,
                    achievedOn = entry.date,
                    sessionNumber = entry.sessionNumber
                )
            }

        // Current month heatmap
        val thisMonth = buildMonthStats(sortedAll, allPrEvents)

        // Timeline: filtered period, newest first
        val timeline = filteredPrEvents.sortedByDescending { it.date }

        // Medals and goals use all-time data regardless of year filter
        val medals = buildMedals(allPrEvents)
        val goals = buildGoals(allPrEvents)

        return AchievementsUiState(
            isLoading = false,
            totalPRs = totalPRs,
            champion = champion,
            thisMonth = thisMonth,
            timeline = timeline,
            medals = medals,
            goals = goals,
            selectedYear = yearFilter,
            selectedDay = selectedDay
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PR detection
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Walks histories in chronological order and emits a [PrEntry] every time
     * an exercise hits a new maximum weight.
     */
    private fun detectPrEvents(sortedHistories: List<WorkoutHistory>): List<PrEntry> {
        // running max weight per exercise (key = uppercase name)
        val runningMax = mutableMapOf<String, Double>()
        // how many sessions included each exercise (for sessionNumber)
        val sessionCount = mutableMapOf<String, Int>()
        val prEvents = mutableListOf<PrEntry>()

        for (history in sortedHistories) {
            // ── collect max weight per exercise in this session ──
            val sessionMax = mutableMapOf<String, Pair<Double, Exercise>>()
            val exerciseKeysThisSession = mutableSetOf<String>()

            for (exercise in history.exercises) {
                if (exercise.type != ExerciseType.STRENGTH) continue
                val key = exercise.name.uppercase()
                exerciseKeysThisSession.add(key)

                val maxWeight = exercise.exerciseSets
                    .filter { it.isCompleted }
                    .mapNotNull { it.weight.toDoubleOrNull() }
                    .filter { it > 0.0 }
                    .maxOrNull() ?: continue

                val current = sessionMax[key]
                if (current == null || maxWeight > current.first) {
                    sessionMax[key] = Pair(maxWeight, exercise)
                }
            }

            // ── increment session counter for every exercise seen this session ──
            for (key in exerciseKeysThisSession) {
                sessionCount[key] = (sessionCount[key] ?: 0) + 1
            }

            // ── emit PR events ──
            for ((key, pair) in sessionMax) {
                val (maxWeight, exercise) = pair
                val prev = runningMax[key] ?: 0.0
                if (maxWeight > prev) {
                    prEvents.add(
                        PrEntry(
                            date = history.completionDate,
                            exerciseName = exercise.name,
                            muscleGroup = exercise.muscleGroup,
                            weightKg = maxWeight,
                            previousWeightKg = prev,
                            deltaKg = maxWeight - prev,
                            sessionNumber = sessionCount[key] ?: 1,
                            workoutName = history.name,
                            isCrown = false   // updated below
                        )
                    )
                    runningMax[key] = maxWeight
                }
            }
        }

        // Mark the single all-time crown (highest absolute weight)
        val crownWeight = prEvents.maxOfOrNull { it.weightKg } ?: return prEvents
        return prEvents.map { if (it.weightKg == crownWeight) it.copy(isCrown = true) else it }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Month heatmap
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildMonthStats(
        sortedHistories: List<WorkoutHistory>,
        allPrEvents: List<PrEntry>
    ): MonthStats {
        val today = LocalDate.now()
        val prevMonthStart = today.withDayOfMonth(1).minusMonths(1)

        val thisMonthHistories = sortedHistories.filter {
            it.completionDate.year == today.year &&
            it.completionDate.monthValue == today.monthValue
        }
        val prevMonthHistories = sortedHistories.filter {
            it.completionDate.year == prevMonthStart.year &&
            it.completionDate.monthValue == prevMonthStart.monthValue
        }

        val thisMonthPrs = allPrEvents.filter {
            it.date.year == today.year && it.date.monthValue == today.monthValue
        }

        val thisVol = thisMonthHistories.sumOf { volumeOf(it) }
        val prevVol = prevMonthHistories.sumOf { volumeOf(it) }
        val deltaPct = when {
            prevVol > 0.0 -> ((thisVol - prevVol) / prevVol * 100).toInt()
            thisVol > 0.0 -> 100
            else          -> 0
        }

        val uniqueExercisesThisMonth = thisMonthHistories
            .flatMap { it.exercises }
            .filter { it.type == ExerciseType.STRENGTH }
            .map { it.name.uppercase() }
            .toSet()
            .size

        return MonthStats(
            monthLabel = today.month
                .getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
                .replaceFirstChar { it.uppercase() },
            daysInMonth = today.lengthOfMonth(),
            today = today.dayOfMonth,
            prDays = thisMonthPrs.map { it.date.dayOfMonth }.toSet(),
            prCount = thisMonthPrs.size,
            exerciseCount = uniqueExercisesThisMonth,
            volumeDeltaPct = deltaPct
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Medals
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildMedals(prEvents: List<PrEntry>): List<Medal> {
        // Best weight ever per exercise
        val best = mutableMapOf<String, PrEntry>()
        for (entry in prEvents) {
            val key = entry.exerciseName.uppercase()
            if ((best[key]?.weightKg ?: 0.0) < entry.weightKg) best[key] = entry
        }

        return best.values
            .sortedByDescending { it.weightKg }
            .take(20)
            .map { entry ->
                ConquistasAggregator.toMedal(
                    exerciseName = entry.exerciseName,
                    muscleGroup = entry.muscleGroup,
                    image = null,
                    weightKg = entry.weightKg
                )
            }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Goals
    // ─────────────────────────────────────────────────────────────────────────

    private fun buildGoals(prEvents: List<PrEntry>): List<Goal> {
        val best = mutableMapOf<String, PrEntry>()
        for (entry in prEvents) {
            val key = entry.exerciseName.uppercase()
            if ((best[key]?.weightKg ?: 0.0) < entry.weightKg) best[key] = entry
        }

        return best.values
            .sortedByDescending { it.weightKg }
            .take(10)
            .map { entry ->
                val current = entry.weightKg
                val target = nextMilestone(current)
                val progress = ((current / target) * 100).toInt().coerceIn(0, 99)
                val remaining = target - current
                val etaLabel = when {
                    remaining <= 5.0  -> "≈ 2 semanas"
                    remaining <= 15.0 -> "≈ 1 mês"
                    else              -> "≈ 2+ meses"
                }
                Goal(
                    exerciseName = entry.exerciseName,
                    muscleGroup = entry.muscleGroup,
                    currentKg = current,
                    targetKg = target,
                    progressPct = progress,
                    etaLabel = etaLabel
                )
            }
    }

    /**
     * Returns the next "round" milestone above [current].
     * E.g. 47 kg → 50 kg, 105 kg → 120 kg.
     */
    private fun nextMilestone(current: Double): Double {
        val milestones = listOf(
            10.0, 20.0, 30.0, 40.0, 50.0, 60.0, 70.0, 80.0, 90.0,
            100.0, 120.0, 140.0, 160.0, 180.0, 200.0, 250.0, 300.0
        )
        return milestones.firstOrNull { it > current }
            ?: (Math.ceil((current * 1.1) / 10.0) * 10.0)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private fun volumeOf(history: WorkoutHistory): Double =
        history.exercises.sumOf { exercise ->
            exercise.exerciseSets
                .filter { it.isCompleted }
                .sumOf { set ->
                    val w = set.weight.toDoubleOrNull() ?: 0.0
                    val r = set.reps.toIntOrNull() ?: 0
                    w * r
                }
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Factory
    // ─────────────────────────────────────────────────────────────────────────

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as GymTrackerApplication
                AchievementsViewModel(
                    workoutHistoryRepository = app.container.workoutHistoryRepository
                )
            }
        }
    }
}
