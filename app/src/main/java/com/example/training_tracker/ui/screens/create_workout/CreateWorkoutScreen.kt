package com.example.training_tracker.ui.screens.create_workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.ui.components.MuscleGroupPickerDialog
import com.example.training_tracker.ui.screens.workout_details.AddCardioSelectionDialog
import com.example.training_tracker.ui.screens.workout_details.AddExerciseSelectionDialog
import com.example.training_tracker.ui.screens.workout_details.AddStretchingSelectionDialog
import com.example.training_tracker.ui.theme.AppTheme

import com.example.training_tracker.ui.theme.Dimens
import com.example.training_tracker.ui.utils.CardioTimePickerDialog
import com.example.training_tracker.ui.utils.ErrorDialog
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    onNavigateBack: () -> Unit,
    initialDayOfWeek: DayOfWeek? = null,
    viewModel: CreateWorkoutViewModel = viewModel(factory = CreateWorkoutViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showErrorMessage by remember { mutableStateOf(false) }
    var snackbarHostState = remember { SnackbarHostState() }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showAddCardioDialog by remember { mutableStateOf(false) }
    var showAddStretchingDialog by remember { mutableStateOf(false) }
    val isButtonEnabled by remember {
        derivedStateOf { uiState.exercises.size >= 1 }
    }
    val scrollState = rememberScrollState()

    LaunchedEffect(initialDayOfWeek) {
        if (initialDayOfWeek != null) {
            viewModel.updateSelectedDay(initialDayOfWeek)
        }
    }

    LaunchedEffect(uiState.isWorkoutSaved) {
        if (uiState.isWorkoutSaved) {
            onNavigateBack()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (showErrorMessage) {
        ErrorDialog(
            text = stringResource(R.string.adicione_pelo_menos_1_exercicios_para_salvar_seu_treino),
            onDismissRequest = { showErrorMessage = false }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.create_workout_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = AppTheme.accent.light
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.content_description_back),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .padding(horizontal = Dimens.paddingLarge)
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {

            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            Text(
                text = stringResource(id = R.string.create_workout_name_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (uiState.showErrors && !uiState.isNameValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            OutlinedTextField(
                value = uiState.workoutName,
                onValueChange = { viewModel.updateWorkoutName(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(id = R.string.create_workout_name_placeholder)) },
                shape = RoundedCornerShape(Dimens.cornerRadius),
                isError = uiState.showErrors && !uiState.isNameValid,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.accent.light,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            Text(
                text = stringResource(id = R.string.create_workout_day_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (uiState.showErrors && !uiState.isDayValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            DaySelector(
                selectedDay = uiState.selectedDay,
                onDaySelected = { viewModel.updateSelectedDay(it) },
                isError = uiState.showErrors && !uiState.isDayValid
            )

            Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))

            // CARDIO
            Text(
                text = stringResource(R.string.adicionar_alongamento),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (uiState.showErrors && !uiState.isDayValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.showErrors && !uiState.isExercisesValid)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .clickable { showAddStretchingDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.clique_aqui_para_selecionar_o_alongamento),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))

            Text(
                text = stringResource(R.string.adicionar_cardio),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (uiState.showErrors && !uiState.isDayValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.showErrors && !uiState.isExercisesValid)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .clickable { showAddCardioDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stringResource(R.string.clique_aqui_para_selecionar_o_cardio),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingExtraLarge))

            Text(
                text = stringResource(id = R.string.create_workout_add_exercise_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (uiState.showErrors && !uiState.isExercisesValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.showErrors && !uiState.isExercisesValid)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .clickable { showAddExerciseDialog = true }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = stringResource(id = R.string.create_workout_click_to_select),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            Text(
                text = stringResource(id = R.string.create_workout_added_exercises_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.accent.light,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))

            uiState.exercises.forEach { exercise ->
                ExerciseItem(
                    exercise = exercise,
                    isExpanded = uiState.expandedExerciseId == exercise.id,
                    onToggleExpand = { viewModel.toggleExerciseExpanded(exercise.id) },
                    onSetCountChange = { count -> viewModel.setExerciseSetCount(exercise.id, count) },
                    onSetTargetChange = { setNum, reps, weight ->
                        viewModel.updateExerciseSetTarget(exercise.id, setNum, reps, weight)
                    },
                    onDelete = { viewModel.removeExercise(exercise) }
                )
                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            }

            if (uiState.showErrors && !uiState.isExercisesValid) {
                Text(
                    text = stringResource(id = R.string.create_workout_error_no_exercise),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            // Botão Salvar
            SaveWorkoutButton(
                text = stringResource(id = R.string.create_workout_save_button),
                isEnabled = isButtonEnabled,
                onClick = {
                    if (isButtonEnabled) {
                        viewModel.saveWorkout()
                    }
                }
            )
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
        }

        if (showAddExerciseDialog) {
            AddExerciseSelectionDialog(
                onDismiss = { showAddExerciseDialog = false },
                onSelect = { name ->
                    viewModel.addExercise(name)
                    showAddExerciseDialog = false
                },
                availableExercises = uiState.availableExercises.filter { it.type == ExerciseType.STRENGTH }
            )
        }

        if (showAddCardioDialog) {
            AddCardioSelectionDialog(
                onDismiss = { showAddCardioDialog = false },
                onSelect = { name ->
                    viewModel.addCardioExercise(name)
                    showAddCardioDialog = false
                },
                availableExercises = uiState.availableExercises.filter { it.type == ExerciseType.CARDIO }
            )
        }

        if (showAddStretchingDialog) {
            AddStretchingSelectionDialog(
                onDismiss = { showAddStretchingDialog = false },
                onSelect = { name ->
                    viewModel.addStretchingExercise(name)
                    showAddStretchingDialog = false
                },
                availableExercises = uiState.availableExercises.filter { it.type == ExerciseType.STRETCHING }
            )
        }
    }
}

@Composable
fun DaySelector(
    selectedDay: DayOfWeek?,
    onDaySelected: (DayOfWeek) -> Unit,
    isError: Boolean = false
) {
    val days = DayOfWeek.entries.toTypedArray()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
    ) {
        items(days) { day ->
            val isSelected = day == selectedDay
            val backgroundColor = if (isSelected) AppTheme.accent.light else Color.Transparent
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            val borderColor = when {
                isSelected -> AppTheme.accent.light
                isError -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.outline
            }

            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 40.dp)
                    .clip(RoundedCornerShape(Dimens.cornerRadius))
                    .background(backgroundColor)
                    .border(1.dp, borderColor, RoundedCornerShape(Dimens.cornerRadius))
                    .clickable { onDaySelected(day) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale("pt", "BR")).uppercase(),
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ExerciseItem(
    exercise: Exercise,
    isExpanded: Boolean = false,
    onToggleExpand: () -> Unit = {},
    onSetCountChange: (Int) -> Unit = {},
    onSetTargetChange: (setNumber: Int, reps: String, weight: String) -> Unit = { _, _, _ -> },
    onDelete: () -> Unit
) {
    val isStrength = exercise.type == ExerciseType.STRENGTH
    val isCardio = exercise.type == ExerciseType.CARDIO
    val isStretching = exercise.type == ExerciseType.STRETCHING
    var showTimePickerForSet by remember { mutableStateOf<Int?>(null) }
    val setCount = exercise.exerciseSets.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(Dimens.cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ── Header row ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.paddingMedium),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    val serieLabel = stringResource(R.string.serie)
                    val seriesLabel = stringResource(R.string.series)
                    val muscleBadgeText = exercise.muscleGroup?.let { stringResource(it.resId).uppercase() }
                    val firstSetTarget = exercise.exerciseSets.firstOrNull { it.targetReps.isNotBlank() }?.targetReps ?: ""
                    val summaryText = when (exercise.type) {
                        ExerciseType.STRENGTH -> if (firstSetTarget.isNotBlank()) "$setCount × $firstSetTarget reps"
                            else "$setCount ${if (setCount == 1) serieLabel else seriesLabel}"
                        else -> if (firstSetTarget.isNotBlank()) "$setCount × $firstSetTarget"
                            else "$setCount ${if (setCount == 1) serieLabel else seriesLabel}"
                    }

                    LazyRow(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = true
                    ) {
                        when (exercise.type) {
                            ExerciseType.STRENGTH -> {
                                if (muscleBadgeText != null) {
                                    item {
                                        ExerciseBadge(
                                            text = muscleBadgeText,
                                            backgroundColor = AppTheme.accent.light
                                        )
                                    }
                                }
                                item { SetsSummaryChip(summaryText, isExpanded, onToggleExpand) }
                            }
                            ExerciseType.CARDIO -> {
                                item { ExerciseBadge(text = "CARDIO", backgroundColor = Color(0xFFFF9800)) }
                                item { SetsSummaryChip(summaryText, isExpanded, onToggleExpand) }
                            }
                            ExerciseType.STRETCHING -> {
                                item { ExerciseBadge(text = "ALONGAMENTO", backgroundColor = Color(0xFF4CAF50)) }
                                item { SetsSummaryChip(summaryText, isExpanded, onToggleExpand) }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.content_description_remove),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            // ── Expandable sets panel ────────────────────────────────────────
            AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                        // Set count stepper
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.paddingMedium, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            IconButton(
                                onClick = { onSetCountChange(setCount - 1) },
                                enabled = setCount > 1,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    "−",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (setCount > 1) AppTheme.accent.light
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                            Text(
                                text = "$setCount ${if (setCount == 1) stringResource(R.string.serie) else stringResource(R.string.series)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            IconButton(
                                onClick = { onSetCountChange(setCount + 1) },
                                enabled = setCount < 10,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Text(
                                    "+",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (setCount < 10) AppTheme.accent.light
                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                            }
                        }

                        // Column headers
                        val headerLabelStyle = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.paddingMedium),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Set", style = headerLabelStyle, modifier = Modifier.width(28.dp), textAlign = TextAlign.Center)
                            if (isStrength) {
                                Text(stringResource(R.string.m_n), style = headerLabelStyle, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(stringResource(R.string.m_x), style = headerLabelStyle, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text(stringResource(R.string.peso_kg), style = headerLabelStyle, modifier = Modifier.weight(1.3f), textAlign = TextAlign.Center)
                            } else if (isCardio) {
                                Text(stringResource(R.string.tempo_hh_mm), style = headerLabelStyle, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }  else if (isStretching) {
                                Text("TEMPO (SEG)", style = headerLabelStyle, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // One row per set
                        exercise.exerciseSets.forEach { set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = Dimens.paddingMedium, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${set.set}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.accent.light,
                                    modifier = Modifier.width(28.dp),
                                    textAlign = TextAlign.Center
                                )
                                if (isStrength) {
                                    val repsParts = set.targetReps.split("-")
                                    val minReps = repsParts.getOrElse(0) { "" }
                                    val maxReps = repsParts.getOrElse(1) { "" }
                                    val isMaxError = maxReps.isNotBlank() &&
                                        minReps.toIntOrNull() != null &&
                                        maxReps.toIntOrNull() != null &&
                                        maxReps.toInt() < minReps.toInt()

                                    OutlinedTextField(
                                        value = minReps,
                                        onValueChange = { raw ->
                                            val v = raw.filter { it.isDigit() }.take(3)
                                            onSetTargetChange(set.set, if (maxReps.isBlank()) v else "$v-$maxReps", set.targetWeight)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        placeholder = { Text("8", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTheme.accent.light, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    )
                                    OutlinedTextField(
                                        value = maxReps,
                                        onValueChange = { raw ->
                                            val v = raw.filter { it.isDigit() }.take(3)
                                            onSetTargetChange(set.set, if (v.isBlank()) minReps else "$minReps-$v", set.targetWeight)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp),
                                        placeholder = { Text("12", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                        isError = isMaxError,
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTheme.accent.light, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), errorBorderColor = MaterialTheme.colorScheme.error)
                                    )
                                    OutlinedTextField(
                                        value = set.targetWeight,
                                        onValueChange = { onSetTargetChange(set.set, set.targetReps, it.filter { c -> c.isDigit() || c == '.' }.take(6)) },
                                        modifier = Modifier
                                            .weight(1.3f)
                                            .height(52.dp),
                                        placeholder = { Text("80", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                                        shape = RoundedCornerShape(8.dp),
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppTheme.accent.light, unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                                    )
                                } else {
                                    // Cardio / Stretching — time only (tap to open picker)
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                            .border(
                                                1.dp,
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { showTimePickerForSet = set.set },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = set.targetReps.ifBlank { if (isStretching) "0" else "00:00" },
                                            style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                                            color = if (set.targetReps.isBlank())
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                            else
                                                MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
        }
    }

    showTimePickerForSet?.let { targetSetNum ->
        val currentTime = exercise.exerciseSets.find { it.set == targetSetNum }?.targetReps ?: ""
        CardioTimePickerDialog(
            initialTime = currentTime,
            showHours = isCardio,
            secondsOnly = isStretching,
            onDismiss = { showTimePickerForSet = null },
            onConfirm = { formatted ->
                onSetTargetChange(targetSetNum, formatted, "")
                showTimePickerForSet = null
            }
        )
    }
}


@Composable
private fun SetsSummaryChip(summaryText: String, isExpanded: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(AppTheme.accent.light.copy(alpha = 0.12f))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = summaryText,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            fontSize = 11.sp,
            color = AppTheme.accent.light
        )
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown
            else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = AppTheme.accent.light,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
fun ExerciseBadge(text: String, backgroundColor: Color) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            color = Color.Black
        )
    }
}

@Composable
fun SaveWorkoutButton(
    text: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 👇 1. defaultMinSize permite que o botão cresça se o texto de aviso for longo
            .defaultMinSize(minHeight = 64.dp)
            .shadow(
                elevation = if (isEnabled) 16.dp else 0.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = AppTheme.accent.light.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = if (isEnabled) AppTheme.brushes.primaryGradient else Brush.horizontalGradient(
                    listOf(Color.Gray, Color.DarkGray)
                )
            )
            // 👇 2. Só permite o clique se estiver habilitado
            .clickable(enabled = isEnabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            // 👇 3. Padding interno para o texto não encostar nas bordas se quebrar linha
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (isEnabled) text else stringResource(R.string.adicione_pelo_menos_1_exercicios),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                // 👇 4. Centralização e proteção contra transbordamento
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                // 👇 5. Impede que o texto "empurre" o ícone para fora da tela
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}
