package com.example.training_tracker.ui.screens.freestyle_workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.ui.screens.workout_screen.ExerciseCard
import com.example.training_tracker.ui.screens.workout_screen.FinishWorkoutButton
import com.example.training_tracker.ui.screens.workout_screen.WorkoutTopBar
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.extensions.isValidToComplete
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.screens.workout_details.AddExerciseSelectionDialog
import com.example.training_tracker.ui.screens.workout_screen.ErrorDialog
import com.example.training_tracker.ui.screens.workout_screen.SetLine
import com.example.training_tracker.ui.screens.workout_screen.WorkoutViewModel
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.Typography

@Composable
fun FreestyleWorkoutScreen(
    onBackClick: () -> Unit,
    onNavigateToReport: (String) -> Unit,
    viewModel: FreestyleWorkoutViewModel = viewModel(factory = FreestyleWorkoutViewModel .Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val navigateToReportId by viewModel.navigateToReport.collectAsState()

    LaunchedEffect(navigateToReportId) {
        navigateToReportId?.let {
            onNavigateToReport(it)
            viewModel.onNavigatedToReport()
        }
    }

    var expandedExercises by remember { mutableStateOf(setOf<String>()) }

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
                elapsedTime = 0L,
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
                    FreestyleExerciseCard(
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
                        onRemoveExercise = { viewModel.removeExercise(exercise.id)}
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    FinishWorkoutButton(
                        onComplete = { viewModel.completeWorkout() },
                        workout = uiState.workout
                    )
                }
            }

            if (uiState.showExercisePicker) {
                AddExerciseSelectionDialog(
                    onDismiss = { viewModel.showExercisePicker(false) },
                    onSelect = { name ->
                        viewModel.addExerciseByName(name)
                        viewModel.showExercisePicker(false)
                    },
                    availableExercises = uiState.availableExercises
                )
            }
        }
    }
}


@Composable
fun FreestyleExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit,
    isLocked: Boolean = false,
    onCompleteSet: (String, Int) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onAddSetClick: (String) -> Unit,
    onRemoveSet: (String, Int) -> Unit,
    onCompleteExercise: () -> Unit,
    onRemoveExercise: (String) -> Unit,
    onReopenExercise: () -> Unit,
) {
    var isDeleteMode by rememberSaveable(exercise.id) { mutableStateOf(false) }
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showCompleteDialog by rememberSaveable { mutableStateOf(false) }
    var showErrorDialog by rememberSaveable { mutableStateOf(false) }
    var errorText by rememberSaveable { mutableStateOf("") }

    val totalSets = exercise.exerciseSets.size
    val cardImage = when (exercise.muscleGroup) {
        MuscleGroups.CHEST -> R.drawable.chest
        MuscleGroups.BACK -> R.drawable.back
        MuscleGroups.LEGS -> R.drawable.legs
        MuscleGroups.SHOULDERS -> R.drawable.shoulders
        MuscleGroups.ABS -> R.drawable.abs
        MuscleGroups.BICEPS -> R.drawable.biceps
        MuscleGroups.TRICEPS -> R.drawable.triceps
        else -> R.drawable.ic_launcher_background
    }

    LaunchedEffect(exercise.exerciseSets.isEmpty()) {
        if (exercise.exerciseSets.isEmpty()) {
            isDeleteMode = false
        }
    }

    if (showErrorDialog) {
        ErrorDialog(
            text = errorText,
            onDismissRequest = { showErrorDialog = false }
        )
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    text = "Finalizar Exercício",
                    style = Typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "Tem certeza que deseja finalizar o exercício ${exercise.name}? Ele será marcado como concluído e não poderá mais ser editado.",
                    style = Typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (exercise.isValidToComplete()) {
                            showCompleteDialog = false
                            onExpandedChange(!isExpanded)
                            onCompleteExercise()
                        } else {
                            showCompleteDialog = false
                            errorText =
                                "Erro! Preencha todos os campos de texto e de repetições antes de continuar"
                            showErrorDialog = true
                        }
                    }
                ) {
                    Text(
                        text = "Confirmar",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCompleteDialog = false }
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color.Gray
                    )
                }
            }
        )
    }

    if (exercise.isCompleted) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onExpandedChange(!isExpanded) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.5f
                )
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF4CAF50).copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            exercise.name,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50),
                            fontSize = 16.sp
                        )
                        Text(
                            "${exercise.exerciseSets.size} Sets",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }

                    Box {
                        IconButton(onClick = { isMenuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                null,
                                tint = TextGray,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(id = R.string.workout_screen_reopen_exercise_menu),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                onClick = { onReopenExercise(); isMenuExpanded = false },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Refresh,
                                        null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextGray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Spacer(Modifier.height(16.dp))
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally)
                                .background(color = Color.White)
                                .border(width = 2.dp, color = CyanAccent),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(cardImage)
                                .crossfade(true)
                                .build(),
                            contentScale = ContentScale.Fit,
                            contentDescription = null
                        )
                        exercise.exerciseSets.forEach { set ->
                            SetLine(
                                modifier = Modifier.fillMaxWidth(),
                                exercise = exercise,
                                inputValueReps = set.reps,
                                inputValueWeight = set.weight,
                                isDeleteMode = false,
                                isCompleted = true,
                                onRepsChange = { _, _ -> },
                                onWeightChange = { _, _ -> },
                                onDeleteClick = { },
                                onCompleteClick = { },
                                setNumber = set.set,
                            )
                        }
                    }
                }
            }
        }
    } else if (!isLocked) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    brush = AppTheme.brushes.backgroundGradient,
                    shape = RoundedCornerShape(24.dp)
                )
                .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(Color.Transparent)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "SETS: $totalSets",
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                        Text(
                            exercise.name,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 22.sp,
                            lineHeight = 28.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            IconButton(onClick = { isMenuExpanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(id = R.string.content_description_options),
                                    tint = TextGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = isMenuExpanded,
                                onDismissRequest = { isMenuExpanded = false }
                            ) {
                                if (!isDeleteMode) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(id = R.string.workout_screen_remove_sets_menu),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = {
                                            isDeleteMode = !isDeleteMode; isMenuExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(id = R.string.workout_screen_remove_exercise),
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        onClick = { onRemoveExercise(exercise.id); isMenuExpanded = false },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Delete,
                                                null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    )
                                } else {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "Done",
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        onClick = {
                                            isDeleteMode = !isDeleteMode; isMenuExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Done,
                                                null,
                                                tint = CyanAccent
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(CyanAccent.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FitnessCenter,
                                null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Column {
                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f),
                            color = CyanAccent.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            val activeSet =
                                exercise.exerciseSets.firstOrNull { !it.isCompleted }?.set ?: 1
                            Text(
                                "CURRENT SET: $activeSet",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = CyanAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        AsyncImage(
                            modifier = Modifier
                                .height(150.dp)
                                .width(300.dp)
                                .clip(CircleShape)
                                .weight(1f)
                                .border(width = 1.dp, color = CyanAccent)
                                .background(color = Color.White),
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(cardImage)
                                .crossfade(true)
                                .build(),
                            contentScale = ContentScale.Fit,
                            contentDescription = null
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    exercise.exerciseSets.forEach { set ->
                        SetLine(
                            modifier = Modifier.fillMaxWidth(),
                            exercise = exercise,
                            inputValueReps = set.reps,
                            inputValueWeight = set.weight,
                            isDeleteMode = isDeleteMode,
                            isCompleted = set.isCompleted,
                            onRepsChange = { _, newText ->
                                onRepsChange(set.set, newText)
                            },
                            onWeightChange = { _, newText ->
                                onWeightChange(set.set, newText)
                            },
                            onDeleteClick = { onRemoveSet(exercise.id, set.set) },
                            onCompleteClick = { onCompleteSet(exercise.id, set.set) },
                            setNumber = set.set,
                        )
                    }

                    if (!isDeleteMode) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { onAddSetClick(exercise.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent)
                        ) {
                            Icon(Icons.Default.Add, null, tint = Color.Black)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "ADD SET",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        }

                        TextButton(
                            onClick = { showCompleteDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                "FINALIZE EXERCISE",
                                color = TextGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    } else {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .alpha(0.4f)
                .clickable { onExpandedChange(!isExpanded) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lock, null, tint = TextGray, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        exercise.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Text(
                        "${exercise.exerciseSets.size} Sets • ${exercise.exerciseSets.firstOrNull()?.reps ?: 0} Reps",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}