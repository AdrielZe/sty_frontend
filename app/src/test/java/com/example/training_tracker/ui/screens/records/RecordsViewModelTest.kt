package com.example.training_tracker.ui.screens.records

import app.cash.turbine.test
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.fakes.FakeExerciseRepository
import com.example.training_tracker.fakes.FakeRecordsRepository
import com.example.training_tracker.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RecordsViewModelTest {

    @get:Rule val mainRule = MainDispatcherRule()

    private fun records() = Records(
        exercisesRecordMap = mutableMapOf(
            "BENCH PRESS" to mutableListOf(100.0, 110.0),
            "SQUAT" to mutableListOf(140.0)
        ),
        cardioRecordsMap = mutableMapOf("TREADMILL" to mutableListOf(5.0))
    )

    private fun exercises() = listOf(
        Exercise(name = "Bench Press", type = ExerciseType.STRENGTH, muscleGroup = MuscleGroups.CHEST),
        Exercise(name = "Squat", type = ExerciseType.STRENGTH, muscleGroup = MuscleGroups.QUADRICEPS),
        Exercise(name = "Treadmill", type = ExerciseType.CARDIO, muscleGroup = MuscleGroups.CARDIO),
    )

    private fun vm(): RecordsViewModel = RecordsViewModel(
        FakeRecordsRepository(records()),
        FakeExerciseRepository(exercises())
    )

    @Test
    fun `initial state is loading then becomes success with records`() = runTest {
        val viewModel = vm()
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.records?.exercisesRecordMap?.size)
            assertEquals(1, state.cardioRecords.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting CHEST filters to chest exercises only`() = runTest {
        val viewModel = vm()
        viewModel.onMuscleGroupSelected(MuscleGroups.CHEST)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.selectedMuscleGroup != MuscleGroups.CHEST) state = awaitItem()
            assertEquals(setOf("BENCH PRESS"), state.records?.exercisesRecordMap?.keys)
            assertTrue(state.cardioRecords.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting CARDIO returns cardio records and empty strength`() = runTest {
        val viewModel = vm()
        viewModel.onMuscleGroupSelected(MuscleGroups.CARDIO)
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.selectedMuscleGroup != MuscleGroups.CARDIO) state = awaitItem()
            assertTrue(state.records?.exercisesRecordMap?.isEmpty() == true)
            assertEquals(setOf("TREADMILL"), state.cardioRecords.keys)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search query filters by name case-insensitively`() = runTest {
        val viewModel = vm()
        viewModel.onSearchQueryChanged("bench")
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading || state.searchQuery != "bench") state = awaitItem()
            assertEquals(setOf("BENCH PRESS"), state.records?.exercisesRecordMap?.keys)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onExerciseSelected populates selectedExerciseHistory and onDismissHistory clears it`() = runTest {
        val viewModel = vm()
        viewModel.onExerciseSelected("Bench Press", listOf(100.0, 110.0))
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedExerciseHistory == null) state = awaitItem()
            assertEquals("Bench Press", state.selectedExerciseHistory?.first)
            assertFalse(state.selectedExerciseIsCardio)
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.onDismissHistory()
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.selectedExerciseHistory != null) state = awaitItem()
            assertNull(state.selectedExerciseHistory)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
