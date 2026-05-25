package com.example.training_tracker.ui.screens.workout_history

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.fakes.FakeWorkoutHistoryRepository
import com.example.training_tracker.fakes.FakeWorkoutRepository
import com.example.training_tracker.ui.screens.workout_report.WorkoutDifficulty
import com.example.training_tracker.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutHistoryViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private fun history(
        id: String = "h1",
        name: String = "Push",
        date: LocalDate = LocalDate.of(2026, 5, 1),
        muscle: MuscleGroups = MuscleGroups.CHEST,
    ) = WorkoutHistory(
        id = id,
        name = name,
        completionDate = date,
        exercises = listOf(Exercise(name = "Bench", muscleGroup = muscle)),
        workoutId = "w-$id",
        difficulty = WorkoutDifficulty.MEDIUM,
    )

    private fun vm(items: List<WorkoutHistory>): Pair<WorkoutHistoryViewModel, FakeWorkoutRepository> {
        val workoutRepo = FakeWorkoutRepository()
        val historyRepo = FakeWorkoutHistoryRepository(items)
        return WorkoutHistoryViewModel(historyRepo, workoutRepo, SavedStateHandle()) to workoutRepo
    }

    @Test
    fun `initial Success state lists saved workouts sorted by date desc`() = runTest {
        val older = history(id = "a", date = LocalDate.of(2026, 1, 1))
        val newer = history(id = "b", date = LocalDate.of(2026, 5, 1))
        val (viewModel, _) = vm(listOf(older, newer))

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.savedWorkouts.isEmpty()) state = awaitItem()
            assertEquals(listOf("b", "a"), state.savedWorkouts.map { it.id })
            assertEquals(SortOrder.DATE_DESC, state.sortOrder)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search query filters by name`() = runTest {
        val (viewModel, _) = vm(listOf(
            history(id = "a", name = "Push Day"),
            history(id = "b", name = "Leg Day"),
        ))
        viewModel.onSearchQueryChange("leg")
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.searchQuery != "leg") state = awaitItem()
            assertEquals(listOf("b"), state.savedWorkouts.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sort by NAME_ASC orders alphabetically`() = runTest {
        val (viewModel, _) = vm(listOf(
            history(id = "a", name = "Zebra"),
            history(id = "b", name = "Alpha"),
        ))
        viewModel.onSortOrderChange(SortOrder.NAME_ASC)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.sortOrder != SortOrder.NAME_ASC) state = awaitItem()
            assertEquals(listOf("b", "a"), state.savedWorkouts.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `muscle group filter excludes non-matching workouts`() = runTest {
        val (viewModel, _) = vm(listOf(
            history(id = "a", muscle = MuscleGroups.CHEST),
            history(id = "b", muscle = MuscleGroups.BACK),
        ))
        viewModel.onMuscleGroupSelected(MuscleGroups.BACK)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedMuscleGroup != MuscleGroups.BACK) state = awaitItem()
            assertEquals(listOf("b"), state.savedWorkouts.map { it.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pagination respects PAGE_SIZE`() = runTest {
        val items = (1..25).map { history(id = "h$it", date = LocalDate.of(2026, 1, 1).plusDays(it.toLong())) }
        val (viewModel, _) = vm(items)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.savedWorkouts.isEmpty()) state = awaitItem()
            assertEquals(WorkoutHistoryViewModel.PAGE_SIZE, state.savedWorkouts.size)
            assertEquals(3, state.totalPages)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `duplicateFromHistory adds a new workout via repository`() = runTest {
        val (viewModel, workoutRepo) = vm(emptyList())
        val source = history(id = "src", name = "Chest Day")

        viewModel.duplicateFromHistory(source, DayOfWeek.MONDAY)

        assertEquals(1, workoutRepo.added.size)
        val added = workoutRepo.added.first()
        assertEquals("Chest Day", added.name)
        assertEquals(DayOfWeek.MONDAY, added.dayOfWeek)
        assertTrue(added.id != "src")
    }
}
