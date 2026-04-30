package com.example.training_tracker.ui.screens.workout_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.extensions.isValidToComplete
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import com.example.training_tracker.ui.theme.Dimens
import com.example.training_tracker.ui.theme.Typography
import com.example.training_tracker.ui.utils.ExerciseCardUtils
import com.example.training_tracker.ui.utils.FinishWorkoutButton
import com.example.training_tracker.ui.utils.InputTextBox
import com.example.training_tracker.ui.utils.WorkoutTopBar
import kotlinx.coroutines.delay
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@Composable
fun WorkoutScreen(
    workoutUiState: WorkoutUiState,
    onWeightChange: (String, Int, String) -> Unit,
    onRepsChange: (String, Int, String) -> Unit,
    onCompleteSet: (String, Int) -> Unit,
    onAddSetClick: (String) -> Unit,
    onBackClick: () -> Unit,
    onRemoveSet: (String, Int) -> Unit,
    onCompleteExercise: (String) -> Unit,
    onReopenExercise: (String) -> Unit,
    onCompleteWorkout: () -> Unit,
    onRemoveExercise: (String) -> Unit = {},
    onTogglePause: () -> Unit = {}
) {
    val currentOnWeightChange by rememberUpdatedState(onWeightChange)
    val currentOnRepsChange by rememberUpdatedState(onRepsChange)
    val currentOnCompleteSet by rememberUpdatedState(onCompleteSet)
    val currentOnAddSetClick by rememberUpdatedState(onAddSetClick)
    val currentOnRemoveSet by rememberUpdatedState(onRemoveSet)
    val currentOnCompleteExercise by rememberUpdatedState(onCompleteExercise)
    val currentOnReopenExercise by rememberUpdatedState(onReopenExercise)
    val currentOnCompleteWorkout by rememberUpdatedState(onCompleteWorkout)
    val currentOnRemoveExercise by rememberUpdatedState(onRemoveExercise)

    var showConfetti by remember { mutableStateOf(false) }
    val workout = workoutUiState.workout

    var hasInitialized by rememberSaveable(workout?.id) { mutableStateOf(false) }
    var expandedExercises by rememberSaveable { mutableStateOf(setOf<String>()) }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(workout?.isOnGoing, workout?.isCompleted, workout?.isPaused) {
        if (workout?.isOnGoing == true && !workout.isCompleted && !workout.isPaused) {
            currentTime = System.currentTimeMillis() // Sync immediately
            while (true) {
                delay(1000)
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val elapsedTime = remember(
        workout?.startTime,
        currentTime,
        workout?.isCompleted,
        workout?.accumulatedTime,
        workout?.isPaused
    ) {
        val baseTime = workout?.accumulatedTime ?: 0L
        if (workout?.startTime != null && workout.isCompleted == false && workout.isPaused == false) {
            val diff = currentTime - workout.startTime
            if (diff > 0) baseTime + diff else baseTime
        } else {
            baseTime
        }
    }

    LaunchedEffect(workout?.id) {
        if (!hasInitialized && workout != null && !workout.isCompleted) {
            val loadedExercises = workout.exercises
            if (loadedExercises.isNotEmpty()) {
                val firstIncompleteId = loadedExercises.firstOrNull { !it.isCompleted }?.id
                if (firstIncompleteId != null) {
                    expandedExercises = setOf(firstIncompleteId)
                }
                hasInitialized = true
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar =
            {
                val exercisesFinished = workout?.exercises?.count { it.isCompleted } ?: 0
                val totalCount = workout?.exercises?.size ?: 0
                val percentage =
                    if (totalCount > 0) (exercisesFinished.toFloat() / totalCount.toFloat()) * 100 else 0f

                WorkoutTopBar(
                    workoutName = workout?.name
                        ?: stringResource(id = R.string.workout_screen_default_workout_name),
                    progressPercentage = percentage,
                    finishedCount = exercisesFinished,
                    totalCount = totalCount,
                    onBackClick = onBackClick,
                    elapsedTime = elapsedTime,
                    isPaused = workout?.isPaused ?: false,
                    onPauseToggle = onTogglePause
                )
            },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
            ) {
                val exercises = workout?.exercises ?: emptyList()

                itemsIndexed(
                    items = exercises,
                    key = { _, exercise -> exercise.id }
                ) { index, exercise ->

                    val previousExercise = if (index > 0) exercises[index - 1] else null
                    val isLocked = remember(exercise, previousExercise) {
                        if (index == 0) {
                            false
                        } else {
                            val hasProgress = exercise.isCompleted || exercise.exerciseSets.any {
                                it.isCompleted || (it.weight.isNotBlank() && it.weight != "0") || (it.reps.isNotBlank() && it.reps != "0")
                            }
                            !(previousExercise?.isCompleted ?: true) && !hasProgress
                        }
                    }

                    val onRepsChangeLambda = remember(exercise.id) {
                        { setNumber: Int, newValue: String ->
                            currentOnRepsChange(exercise.id, setNumber, newValue)
                        }
                    }
                    val onWeightChangeLambda = remember(exercise.id) {
                        { setNumber: Int, newValue: String ->
                            currentOnWeightChange(exercise.id, setNumber, newValue)
                        }
                    }
                    val onAddSetClickLambda = remember(exercise.id) {
                        { id: String -> currentOnAddSetClick(id) }
                    }
                    val onCompleteExerciseLambda = remember(exercise.id, index) {
                        {
                            currentOnCompleteExercise(exercise.id)
                            val nextExercise =
                                exercises.drop(index + 1).firstOrNull { !it.isCompleted }
                            expandedExercises = if (nextExercise != null) {
                                setOf(nextExercise.id)
                            } else {
                                emptySet()
                            }
                        }
                    }
                    val onRemoveExerciseLambda = remember(exercise.id) {
                        { id: String -> currentOnRemoveExercise(id) }
                    }
                    val onReopenExerciseLambda = remember(exercise.id) {
                        { currentOnReopenExercise(exercise.id) }
                    }
                    val onExpandedChangeLambda = remember(exercise.id) {
                        { isNowExpanded: Boolean ->
                            expandedExercises = if (isNowExpanded) {
                                expandedExercises + exercise.id
                            } else {
                                expandedExercises - exercise.id
                            }
                        }
                    }

                    ExerciseCardUtils(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        exercise = exercise,
                        isExpanded = expandedExercises.contains(exercise.id),
                        onRepsChange = onRepsChangeLambda,
                        isLocked = isLocked,
                        onWeightChange = onWeightChangeLambda,
                        onAddSetClick = onAddSetClickLambda,
                        onRemoveSet = currentOnRemoveSet,
                        onCompleteExercise = onCompleteExerciseLambda,
                        onReopenExercise = onReopenExerciseLambda,
                        onExpandedChange = onExpandedChangeLambda,
                        onRemoveExercise = onRemoveExerciseLambda,
                        workout = workout
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    FinishWorkoutButton(
                        onComplete = {
                            showConfetti = true
                            expandedExercises = emptySet()
                            exercises.forEach { ex ->
                                currentOnCompleteExercise(ex.id)
                            }
                            currentOnCompleteWorkout()
                        },
                        workout = workout
                    )
                }
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





