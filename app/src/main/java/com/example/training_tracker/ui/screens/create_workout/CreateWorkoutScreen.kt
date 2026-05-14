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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Search
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
            text = stringResource(R.string.adicione_pelo_menos_3_exercicios_para_salvar_seu_treino),
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
fun ExerciseItem(exercise: Exercise, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Dimens.cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // A Column recebe o weight(1f) para empurrar a lixeira para o canto,
            // garantindo que textos grandes quebrem de linha sem sobrepor o botão.
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

                // Renderiza as tags dependendo do tipo de exercício
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (exercise.type) {
                        ExerciseType.STRENGTH -> {
                            exercise.muscleGroup?.let { muscle ->
                                ExerciseBadge(
                                    text = stringResource(muscle.resId).uppercase(),
                                    backgroundColor = AppTheme.accent.light
                                )
                            }
                        }
                        ExerciseType.CARDIO -> {
                            ExerciseBadge(
                                text = "CARDIO",
                                backgroundColor = Color(0xFFFF9800) // Laranja para cardio
                            )

                            val cardioDetails = listOfNotNull(
                                exercise.time?.takeIf { it.isNotBlank() }?.let { "$it min" },
                                exercise.distance?.takeIf { it.isNotBlank() }?.let { "$it km" }
                            ).joinToString(" • ")

                            if (cardioDetails.isNotEmpty()) {
                                Text(
                                    text = cardioDetails,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        ExerciseType.STRETCHING -> {
                            ExerciseBadge(
                                text = "ALONGAMENTO",
                                backgroundColor = Color(0xFF4CAF50) // Verde para alongamento
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // A lixeira sem weight() assume apenas o tamanho do próprio ícone
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(id = R.string.content_description_remove),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
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
