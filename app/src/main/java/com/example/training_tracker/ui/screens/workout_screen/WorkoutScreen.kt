package com.example.training_tracker.ui.screens.workout_screen

import android.R
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
import com.example.training_tracker.ui.theme.Typography
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
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
    onCompleteWorkout: () -> Unit,
) {
    var showConfetti by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = workoutUiState.workout?.name ?: "Treino",
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack, // Ícone de setinha
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.secondary // Sua cor de destaque
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                LazyColumn {
                    items(workoutUiState.workout?.exercises ?: emptyList()) { exercise ->
                        ExerciseCard(
                            modifier = Modifier,
                            exercise = exercise,
                            onRepsChange = { setNumber, newValue ->
                                onRepsChange(
                                    exercise.id,
                                    setNumber,
                                    newValue,
                                )
                            },
                            onWeightChange = { setNumber, newValue ->
                                onWeightChange(
                                    exercise.id,
                                    setNumber,
                                    newValue,
                                )
                            },
                            onAddSetClick = { id ->
                                onAddSetClick(
                                    id,
                                )
                            },
                            onRemoveSet = onRemoveSet,
                            onCompleteSet = onCompleteSet,
                            onCompleteExercise = onCompleteExercise,
                        )
                    }

                    item {
                        FinishWorkoutButton(
                            onComplete = {
                                showConfetti = true
                                onCompleteWorkout()
                            },
                            workout = workoutUiState.workout
                        )
                        Spacer(modifier = Modifier.height(32.dp)) // Espaço extra no final da rolagem
                    }
                }
            }

            if (showConfetti) {
                KonfettiView(
                    modifier = Modifier.fillMaxSize(),
                    parties = listOf(
                        Party(
                            speed = 0f,
                            maxSpeed = 30f,
                            damping = 0.9f,
                            spread = 360,
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

@Composable
fun WorkoutNameCard(modifier: Modifier = Modifier, workout: Workout?) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.paddingExtraSmall)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(Dimens.cornerRadius)
            )
            .padding(20.dp),
        color = Color.Transparent,
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = workout?.name.toString(),
                style = Typography.bodyLarge
            )
        }
    }
}

@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    expanded: Boolean = false,
    onCompleteSet: (String, Int) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onCompleteExercise: (String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onAddSetClick: (String) -> Unit,
    onRemoveSet: (String, Int) -> Unit,
) {
    var isDeleteMode by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    var isExpanded by remember(exercise.isCompleted) { mutableStateOf(!exercise.isCompleted) }

    if (exercise.exerciseSets.isEmpty()) {
        isDeleteMode = false
    }

    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.04f)
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exercise.name,
                    style = Typography.titleLarge,
                    color = if (exercise.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        if (isDeleteMode == false) {
                            if (exercise.isCompleted == false) {
                                IconButton(onClick = { isMenuExpanded = true }) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Transparent,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "Opções",
                                            tint = MaterialTheme.colorScheme.secondary,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                        } else {
                            IconButton(onClick = { isDeleteMode = false }) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.Black.copy(alpha = 0.03f),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirmar",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Remover séries") },
                                onClick = {
                                    isDeleteMode = !isDeleteMode
                                    isMenuExpanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        if (isDeleteMode) Icons.Default.CheckCircle else Icons.Default.Delete,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) "Recolher" else "Expandir",
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.weight(0.1f),
                            text = "Série",
                            textAlign = TextAlign.Center,
                            style = Typography.bodyMedium
                        )

                        Text(
                            modifier = Modifier.weight(0.2f),
                            text = "Peso (Kg)",
                            textAlign = TextAlign.Center,
                            style = Typography.bodyMedium
                        )

                        Text(
                            modifier = Modifier.weight(0.2f),
                            text = "Repetições",
                            textAlign = TextAlign.Center,
                            style = Typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.weight(0.1f))
                    }

                    exercise.exerciseSets.forEach { set ->
                        SetLine(
                            modifier = Modifier.fillMaxWidth(),
                            exercise = exercise,
                            inputValueReps = set.reps,
                            inputValueWeight = set.weight,
                            isDeleteMode = isDeleteMode,
                            isCompleted = set.isCompleted,
                            onRepsChange = { _, newText -> onRepsChange(set.set, newText) },
                            onWeightChange = { _, newText -> onWeightChange(set.set, newText) },
                            onDeleteClick = { onRemoveSet(exercise.id, set.set) },
                            onCompleteClick = { onCompleteSet(exercise.id, set.set) },
                            setNumber = set.set,
                        )
                    }

                    if (!isDeleteMode) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (!exercise.isCompleted) {
                                AddSetButton(
                                    modifier = Modifier,
                                    onAddSetClick = { onAddSetClick(exercise.id) })
                                HoldToCompleteButton(
                                    modifier = Modifier,
                                    onComplete = { onCompleteExercise(exercise.id) },
                                    exercise = exercise
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SetLine(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    isDeleteMode: Boolean,
    isCompleted: Boolean = false,
    onDeleteClick: () -> Unit,
    onCompleteClick: () -> Unit,
    setNumber: Int,
    inputValueWeight: String,
    inputValueReps: String,
    onRepsChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("Ocorreu um erro") }

    if (showErrorDialog) {
        ErrorDialog(text = errorText, onDismissRequest = { showErrorDialog = false })
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text(text = "Salvar Série", style = Typography.titleMedium) },
            text = {
                Text(
                    text = "Deseja salvar a série $setNumber? Ela será bloqueada para edição.",
                    style = Typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCompleteDialog = false
                        onCompleteClick()
                    }
                ) {
                    Text(
                        "Salvar",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancelar", color = Color.Gray)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(text = "Remover Série", style = Typography.titleMedium)
            },
            text = {
                Text(
                    text = "Tem certeza que deseja remover a série $setNumber?",
                    style = Typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteClick()
                    }
                ) {
                    Text("Remover", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.secondary)
                }
            }
        )
    }
    Card(
        modifier = modifier
            .padding(vertical = Dimens.paddingMedium)
            .height(65.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = Dimens.paddingExtraSmall, horizontal = Dimens.paddingExtraSmall)
                .fillMaxSize()
                .height(36.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier
                    .weight(0.1f)
                    .height(36.dp)
                    .wrapContentSize(Alignment.Center),
                text = setNumber.toString(),
                textAlign = TextAlign.Center,
                style = Typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            InputTextBox(
                modifier = Modifier
                    .weight(0.15f)
                    .height(50.dp),
                isLocked = isCompleted,
                inputValue = inputValueWeight,
                onRepsChange = { onWeightChange(setNumber, it) }
            )

            InputTextBox(
                modifier = Modifier
                    .weight(0.15f)
                    .height(50.dp),
                isLocked = isCompleted,
                inputValue = inputValueReps,
                onRepsChange = { onRepsChange(setNumber, it) }
            )

            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .height(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (isDeleteMode) {
                            if (isCompleted) {
                                errorText = "Não é possível remover uma série que já foi salva."
                                showErrorDialog = true
                            } else {
                                showDeleteDialog = true
                            }
                        } else {
                            if (!isCompleted) {
                                if (inputValueReps != "0" && inputValueReps != "" && inputValueWeight != "" && inputValueWeight != "0") {
                                    showCompleteDialog = true
                                } else {
                                    errorText =
                                        "Não é possível salvar uma série sem peso ou sem repetições"
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDeleteMode) Icons.Default.Delete else Icons.Default.CheckCircle,
                    contentDescription = if (isDeleteMode) "Deletar" else "Check",
                    tint = if (isDeleteMode) {
                        Color.Gray
                    } else {
                        if (isCompleted) Color.Gray else MaterialTheme.colorScheme.secondary
                    },
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
fun InputTextBox(
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    inputValue: String,
    onRepsChange: (String) -> Unit
) {
    Box(
        modifier = modifier
            .padding(start = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(vertical = 4.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicTextField(
            value = inputValue,
            onValueChange = { onRepsChange(it) },
            enabled = !isLocked,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                if (inputValue.isEmpty()) {
                    Text(
                        text = "0",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun AddSetButton(
    modifier: Modifier,
    onAddSetClick: () -> Unit
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 16.dp),
        onClick = {
            onAddSetClick()
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Check",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Adicionar série",
                style = Typography.titleSmall
            )
        }
    }
}




// PONTO DE ATENÇÃO : O ideal é remover esse mutableStateOf interno e basear a lógica diretamente no exercise.isCompleted.

@Composable
fun HoldToCompleteButton(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    onComplete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val progress = remember { Animatable(0f) }
    val hapticFeedback = LocalHapticFeedback.current

    if (showErrorDialog == true) {
        ErrorDialog(
            text = "Preencha todos os campos das séries antes de concluir o exercício",
            onDismissRequest = { showErrorDialog = false })
    }

    val isExerciseEmpty = remember(exercise.exerciseSets) {
        exercise.exerciseSets.any { set ->
            set.weight.isBlank() || set.weight == "0" || set.reps.isBlank() || set.reps == "0"
        }
    }
    LaunchedEffect(isPressed) {
        if (isPressed && !isCompleted) {
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000, easing = LinearEasing)
            )
            if (progress.value == 1f) {
                if (isExerciseEmpty == false) {
                    println("ENTERING HEREEEEEE")
                    isCompleted = true
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete()
                } else {
                    showErrorDialog = true
                }

            }
        } else if (!isCompleted) {
            progress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(45.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.onSecondary)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (!isCompleted) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    }
                )
            }
            .drawBehind {
                drawRect(
                    color = secondaryColor, // Verde de conclusão
                    size = size.copy(width = size.width * progress.value)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isCompleted) "Exercício Concluído" else "Concluir o exercício",
            style = Typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
fun FinishWorkoutButton(
    modifier: Modifier = Modifier,
    workout: Workout?,
    onComplete: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val progress = remember { Animatable(0f) }
    val hapticFeedback = LocalHapticFeedback.current
    val secondaryColor = MaterialTheme.colorScheme.secondary


    val canCompleteWorkout = remember(workout) {
        workout?.exercises?.all { exercise ->
            exercise.exerciseSets.all { set ->
                set.weight.isNotBlank() && set.weight != "0" && set.reps.isNotBlank() && set.reps != "0"
            }
        } == true
    }

    LaunchedEffect(isPressed, workout?.isCompleted) {
        if (workout?.isCompleted == false) {
            if (isPressed) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 1500, easing = LinearEasing)
                )
                if (progress.value == 1f) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete() // Isso vai avisar o ViewModel!
                }
            } else {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                )
            }
        }
    }

    if (canCompleteWorkout) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    color = MaterialTheme.colorScheme.onSecondary
                )
                .border(2.dp, secondaryColor.copy(alpha = 0.5f), RoundedCornerShape(30.dp))
                .pointerInput(workout?.isCompleted) {
                    detectTapGestures(
                        onPress = {
                            if (workout?.isCompleted == false) {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        }
                    )
                }
                .drawBehind {
                    drawRect(
                        color = secondaryColor,
                        size = size.copy(width = size.width * progress.value)
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (workout?.isCompleted == true) "Treino Finalizado!" else "Segure para finalizar o treino",
                style = Typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(
                    color = Color.Gray
                )
                .border(2.dp, Color.Transparent, RoundedCornerShape(30.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Segure para finalizar o treino",
                style = Typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Normal
            )
        }
    }
}


@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    text: String
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = "Opa!", style = Typography.titleMedium) },
        text = { Text(text = text, style = Typography.bodyLarge) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(
                    "Ok",
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
    )
}