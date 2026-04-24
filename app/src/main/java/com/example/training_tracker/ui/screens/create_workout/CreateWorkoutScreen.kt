package com.example.training_tracker.ui.screens.create_workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.mocks.availableExercises
import com.example.training_tracker.ui.screens.home.MainGradientButton
import com.example.training_tracker.ui.screens.workout_details.AddExerciseSelectionDialog
import com.example.training_tracker.ui.screens.workout_screen.ErrorDialog
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
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
    val isButtonEnabled by remember {
        derivedStateOf { uiState.exercises.size >= 3 }
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
            text = "Adicione pelo menos 3 exercícios para poder salver seu treino",
            onDismissRequest = { showErrorMessage = false }
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
                        color = CyanAccent
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

            // Nome do Treino
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
                    focusedBorderColor = CyanAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error
                )
            )

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            // Seleção do Dia
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

            // Adicionar Exercício
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
                    .height(56.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .border(
                        width = 1.dp,
                        color = if (uiState.showErrors && !uiState.isExercisesValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline.copy(
                            alpha = 0.5f
                        ),
                        shape = RoundedCornerShape(Dimens.cornerRadius)
                    )
                    .clickable { showAddExerciseDialog = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(id = R.string.create_workout_click_to_select),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            // Lista de Exercícios Adicionados
            Text(
                text = stringResource(id = R.string.create_workout_added_exercises_title),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = CyanAccent,
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
                availableExercises = uiState.availableExercises
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
            val backgroundColor = if (isSelected) CyanAccent else Color.Transparent
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            val borderColor = when {
                isSelected -> CyanAccent
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
                .padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = exercise.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
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
fun SaveWorkoutButton(
    text: String,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val gradientColors = listOf(
        CyanAccent,
        MaterialTheme.colorScheme.primary
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = CyanAccent.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = if (isEnabled) AppTheme.brushes.primaryGradient else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
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
                text = if (isEnabled) text else "Adicione pelo menos 3 exercícios",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}