package com.example.training_tracker.ui.screens.create_workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.ui.screens.home.MainGradientButton
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateWorkoutViewModel = viewModel(factory = CreateWorkoutViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var exerciseName by remember { mutableStateOf("") }
    var showExerciseError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isWorkoutSaved) {
        if (uiState.isWorkoutSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "CRIAR NOVO TREINO",
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
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = Dimens.paddingLarge)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(Dimens.paddingMedium))

            // Nome do Treino
            Text(
                text = "Qual o nome do treino?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (uiState.showErrors && !uiState.isNameValid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            OutlinedTextField(
                value = uiState.workoutName,
                onValueChange = { viewModel.updateWorkoutName(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ex: Treino A - Superior") },
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
                text = "Qual o dia da semana?",
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
                text = "Adicionar Exercícios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if ((uiState.showErrors && !uiState.isExercisesValid) || showExerciseError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                OutlinedTextField(
                    value = exerciseName,
                    onValueChange = { 
                        exerciseName = it
                        if (it.isNotBlank()) showExerciseError = false
                    },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Nome do exercício") },
                    shape = RoundedCornerShape(Dimens.cornerRadius),
                    isError = showExerciseError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )
                IconButton(
                    onClick = {
                        if (exerciseName.isNotBlank()) {
                            viewModel.addExercise(exerciseName)
                            exerciseName = ""
                            showExerciseError = false
                        } else {
                            showExerciseError = true
                        }
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (showExerciseError) MaterialTheme.colorScheme.error else CyanAccent, 
                            RoundedCornerShape(Dimens.cornerRadius)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            // Lista de Exercícios
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (uiState.showErrors && !uiState.isExercisesValid)
                            Modifier.border(1.dp, MaterialTheme.colorScheme.error, RoundedCornerShape(Dimens.cornerRadius))
                        else Modifier
                    ),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingSmall)
            ) {
                items(uiState.exercises) { exercise ->
                    ExerciseItem(
                        exercise = exercise,
                        onDelete = { viewModel.removeExercise(exercise) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

            // Botão Salvar
            MainGradientButton(
                text = "SALVAR TREINO",
                onClick = { viewModel.saveWorkout() }
            )
            Spacer(modifier = Modifier.height(Dimens.paddingLarge))
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
                    contentDescription = "Remover",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}