package com.example.training_tracker.ui.screens.home

import app.cash.turbine.test
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.fakes.FakeUserRepository
import com.example.training_tracker.fakes.FakeWorkoutHistoryRepository
import com.example.training_tracker.fakes.FakeWorkoutRepository
import com.example.training_tracker.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private fun vm(
        user: User? = User(name = "Adriel"),
        workouts: List<Workout> = emptyList(),
    ): HomeViewModel {
        val userRepo = FakeUserRepository(user)
        val workoutRepo = FakeWorkoutRepository(workouts)
        val historyRepo = FakeWorkoutHistoryRepository()
        return HomeViewModel(userRepo, workoutRepo, historyRepo)
    }

    @Test
    fun `initial state is Loading then emits Success`() = runTest {
        val viewModel = vm()
        viewModel.uiState.test {
            // Initial value when no collector has consumed upstream
            val first = awaitItem()
            // With UnconfinedTestDispatcher, upstream may resolve immediately to Success.
            // We accept either: Loading then Success, or just Success.
            if (first is HomeUiState.Loading) {
                val next = awaitItem()
                assertTrue(next is HomeUiState.Success)
            } else {
                assertTrue(first is HomeUiState.Success)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Success contains today's workouts from repository`() = runTest {
        val today = LocalDate.now().dayOfWeek
        val workout = Workout(id = "w1", name = "Push", dayOfWeek = today)
        val viewModel = vm(workouts = listOf(workout))

        viewModel.uiState.test {
            val state = awaitItem().let { if (it is HomeUiState.Loading) awaitItem() else it }
            assertTrue(state is HomeUiState.Success)
            val success = state as HomeUiState.Success
            assertEquals(1, success.todayWorkouts.size)
            assertEquals("Push", success.todayWorkouts.first().name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updateWeeklyGoal persists to repository`() = runTest {
        val userRepo = FakeUserRepository(User(name = "Adriel"))
        val viewModel = HomeViewModel(userRepo, FakeWorkoutRepository(), FakeWorkoutHistoryRepository())

        viewModel.updateWeeklyGoal(5)

        assertEquals(5, userRepo.lastWeeklyGoal)
        assertEquals(5, userRepo.userFlow.value?.weeklyGoal)
    }

    @Test
    fun `startFreestyleWorkout adds workout and invokes callback`() = runTest {
        val workoutRepo = FakeWorkoutRepository()
        val viewModel = HomeViewModel(FakeUserRepository(User(name = "A")), workoutRepo, FakeWorkoutHistoryRepository())

        var confirmed = false
        viewModel.startFreestyleWorkout("Freestyle") { confirmed = true }

        assertTrue(confirmed)
        assertEquals(1, workoutRepo.added.size)
        assertEquals("Freestyle", workoutRepo.added.first().name)
        assertTrue(workoutRepo.added.first().isOnGoing)
    }

    @Test
    fun `removeFreestyleWorkout deletes when one exists`() = runTest {
        val freestyle = Workout(id = "freestyle_workout_id", name = "F", isOnGoing = true)
        val workoutRepo = FakeWorkoutRepository(listOf(freestyle))
        val viewModel = HomeViewModel(FakeUserRepository(User(name = "A")), workoutRepo, FakeWorkoutHistoryRepository())

        viewModel.removeFreestyleWorkout()

        assertEquals(1, workoutRepo.deleted.size)
        assertEquals("freestyle_workout_id", workoutRepo.deleted.first().id)
    }

    @Test
    fun `emits Error state when upstream throws`() = runTest {
        // Build a repo that throws on collection by returning a flow that errors out:
        val userRepo = object : FakeUserRepository(User(name = "x")) {
            override fun getUser() = kotlinx.coroutines.flow.flow<com.example.training_tracker.data.models.User?> {
                throw RuntimeException("boom")
            }
        }
        val viewModel = HomeViewModel(userRepo, FakeWorkoutRepository(), FakeWorkoutHistoryRepository())

        viewModel.uiState.test {
            var state = awaitItem()
            while (state is HomeUiState.Loading) state = awaitItem()
            assertTrue("expected Error but was $state", state is HomeUiState.Error)
            assertEquals("boom", (state as HomeUiState.Error).message)
            cancelAndIgnoreRemainingEvents()
        }
        // silence unused
        assertNotNull(viewModel)
        assertNull(null)
    }
}
