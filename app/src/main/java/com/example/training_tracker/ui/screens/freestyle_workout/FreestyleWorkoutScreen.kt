package com.example.training_tracker.ui.screens.freestyle_workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.extensions.isValidToComplete
import com.example.training_tracker.ui.components.MuscleGroupPickerDialog
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.screens.workout_details.AddExerciseSelectionDialog
import com.example.training_tracker.ui.screens.workout_screen.ErrorDialog
import com.example.training_tracker.ui.screens.workout_screen.FinishWorkoutButton
import com.example.training_tracker.ui.screens.workout_screen.SetLine
import com.example.training_tracker.ui.screens.workout_screen.WorkoutTopBar
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Typography
import com.example.training_tracker.ui.utils.ExerciseCardUtils
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun FreestyleWorkoutScreen(
    onBackClick: () -> Unit,
    onNavigateToReport: (String) -> Unit,
    viewModel: FreestyleWorkoutViewModel = viewModel(factory = FreestyleWorkoutViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val workout = uiState.workout
    val navigateToReportId by viewModel.navigateToReport.collectAsState()
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(workout.isOnGoing, workout.isCompleted, workout.isPaused) {
        if (workout.isOnGoing && !workout.isCompleted && !workout.isPaused) {
            currentTime = System.currentTimeMillis() // Sync immediately
            while (true) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }
    LaunchedEffect(navigateToReportId) {
        navigateToReportId?.let {
            onNavigateToReport(it)
            viewModel.onNavigatedToReport()
        }
    }

    var expandedExercises by remember { mutableStateOf(setOf<String>()) }
    var showConfetti by remember { mutableStateOf(false) }

    val elapsedTime = remember(
        workout.startTime,
        currentTime,
        workout.isCompleted,
        workout.accumulatedTime,
        workout.isPaused
    ) {
        val baseTime = workout?.accumulatedTime ?: 0L
        if (workout.startTime != null && !workout.isCompleted && !workout.isPaused) {
            val diff = currentTime - workout.startTime
            if (diff > 0) baseTime + diff else baseTime
        } else {
            baseTime
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            val workout = uiState.workout
            val exercisesFinished = workout.exercises.count { it.isCompleted }
            val totalCount = workout.exercises.size
            val percentage = if (totalCount > 0) (exercisesFinished.toFloat() / totalCount.toFloat()) * 100 else 0f

            WorkoutTopBar(
                workoutName = workout.name,
                progressPercentage = percentage,
                finishedCount = exercisesFinished,
                totalCount = totalCount,
                onBackClick = onBackClick,
                elapsedTime = elapsedTime,
                isPaused = workout.isPaused,
                onPauseToggle = { viewModel.togglePauseWorkout() }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showExercisePicker(true) },
                containerColor = CyanAccent,
                contentColor = Color.Black,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("ADD EXERCISE", fontWeight = FontWeight.Bold) },
                shape = RoundedCornerShape(16.dp)
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                itemsIndexed(
                    items = uiState.workout.exercises,
                    key = { _, exercise -> exercise.id }
                ) { index, exercise ->
                    ExerciseCardUtils(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        exercise = exercise,
                        isExpanded = expandedExercises.contains(exercise.id),
                        onExpandedChange = { isExpanded ->
                            expandedExercises = if (isExpanded) expandedExercises + exercise.id else expandedExercises - exercise.id
                        },
                        onRepsChange = { setNum, reps -> viewModel.updateExercise(exercise.id, setNum, newReps = reps) },
                        onWeightChange = { setNum, weight -> viewModel.updateExercise(exercise.id, setNum, newWeight = weight) },
                        onAddSetClick = { viewModel.addNewSetLine(exercise.id) },
                        onRemoveSet = { _, setNum -> viewModel.removeSetLine(exercise.id, setNum) },
                        onCompleteSet = { _, setNum -> viewModel.completeSet(exercise.id, setNum) },
                        onCompleteExercise = { viewModel.completeExercise(exercise.id) },
                        onReopenExercise = { viewModel.reopenExercise(exercise.id) },
                        onRemoveExercise = { viewModel.removeExercise(exercise.id)},
                        workout = uiState.workout
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    FinishWorkoutButton(
                        onComplete = {
                            showConfetti = true
                            viewModel.completeWorkout()
                        },
                        workout = uiState.workout
                    )
                }
            }

            if (uiState.showExercisePicker) {
                AddExerciseSelectionDialog(
                    onDismiss = { viewModel.showExercisePicker(false) },
                    onSelect = { name ->
                        viewModel.addExerciseByName(name)
                    },
                    availableExercises = uiState.availableExercises
                )
            }

            val pendingName = uiState.pendingExerciseName
            if (uiState.showMuscleGroupPicker && pendingName != null) {
                MuscleGroupPickerDialog(
                    exerciseName = pendingName,
                    onDismiss = { viewModel.dismissMuscleGroupPicker() },
                    onMuscleGroupSelected = { muscleGroup ->
                        viewModel.onMuscleGroupSelected(muscleGroup)
                    }
                )
            }

            if (showConfetti) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(
                        Party(
                            speed = 0f, maxSpeed = 30f, damping = 0.9f, spread = 360,
                            colors = listOf(
                                0xFF66c9e8.toInt(),
                                0xFF008B8B.toInt(),
                                0xFF52d6ff.toInt(),
                                0xFFFFFFFF.toInt()
                            ),
                            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
                            position = Position.Relative(0.5, 0.3)
                        )
                    )
                )
            }
        }
    }
}

