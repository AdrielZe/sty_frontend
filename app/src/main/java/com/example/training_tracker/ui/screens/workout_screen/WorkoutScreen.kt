package com.example.training_tracker.ui.screens.workout_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import com.example.training_tracker.ui.theme.Dimens
import com.example.training_tracker.ui.theme.Typography
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
    onCompleteWorkout: () -> Unit,
) {
    var showConfetti by remember { mutableStateOf(false) }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            WorkoutTopBar(onBackClick = onBackClick)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // HERO SECTION do Treino
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = workoutUiState.workout?.name
                                ?: stringResource(id = R.string.workout_screen_default_workout_name),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = 40.sp
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row {
                            Text(
                                text = stringResource(id = R.string.workout_screen_in_progress_label),
                                style = TextStyle(
                                    brush = CyanGradient
                                ),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = " - 54.3% of 100%",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        WorkoutProgressBar(
                            modifier = Modifier,
                            progress = 0.4f,
                            workoutUiState.workout
                        )


                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                val exercises = workoutUiState.workout?.exercises ?: emptyList()
                itemsIndexed(
                    items = exercises,
                    // Usamos uma chave composta para garantir unicidade mesmo em casos de dados duplicados
                    key = { index, exercise -> "${exercise.id}_$index" }
                ) { _, exercise ->
                    ExerciseCard(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        exercise = exercise,
                        onRepsChange = { setNumber, newValue ->
                            onRepsChange(
                                exercise.id,
                                setNumber,
                                newValue
                            )
                        },
                        onWeightChange = { setNumber, newValue ->
                            onWeightChange(
                                exercise.id,
                                setNumber,
                                newValue
                            )
                        },
                        onAddSetClick = { id -> onAddSetClick(id) },
                        onRemoveSet = onRemoveSet,
                        onCompleteSet = onCompleteSet,
                        onCompleteExercise = { onCompleteExercise(exercise.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    FinishWorkoutButton(
                        onComplete = {
                            showConfetti = true
                            onCompleteWorkout()
                        },
                        workout = workoutUiState.workout
                    )
                    Spacer(modifier = Modifier.height(40.dp))
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

@Composable
fun WorkoutTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.content_description_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    onCompleteSet: (String, Int) -> Unit,
    onRepsChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onAddSetClick: (String) -> Unit,
    onRemoveSet: (String, Int) -> Unit,
    onCompleteExercise: (String) -> Unit,
) {
    var isDeleteMode by remember { mutableStateOf(false) }
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isExpanded by rememberSaveable(key = exercise.id) { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }

    if (exercise.exerciseSets.isEmpty()) {
        isDeleteMode = false
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
                        showCompleteDialog = false
                        isExpanded = false
                        onCompleteExercise(exercise.id) // Dispara a função aqui!
                    }
                ) {
                    Text(
                        text = "Confirmar",
                        // Usando as cores do seu tema para manter o padrão
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCompleteDialog = false } // Apenas fecha
                ) {
                    Text(
                        text = "Cancelar",
                        color = Color.Gray
                    )
                }
            }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isExpanded) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                brush = AppTheme.brushes.backgroundGradient,
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .background(
                        color = Color.Transparent
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exercise.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (exercise.isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        if (!isDeleteMode && !exercise.isCompleted) {
                            IconButton(onClick = { isMenuExpanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = stringResource(id = R.string.content_description_options),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else if (isDeleteMode) {
                            IconButton(onClick = { isDeleteMode = false }) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = stringResource(id = R.string.content_description_confirm),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = isMenuExpanded,
                            onDismissRequest = { isMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        stringResource(id = R.string.workout_screen_remove_sets_menu),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = { isDeleteMode = !isDeleteMode; isMenuExpanded = false },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            )
                        }
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = if (isExpanded) stringResource(id = R.string.content_description_collapse) else stringResource(
                                id = R.string.content_description_expand
                            ),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)) {
                        Text(
                            modifier = Modifier.weight(0.15f),
                            text = stringResource(id = R.string.workout_screen_set_column),
                            textAlign = TextAlign.Center,
                            style = Typography.labelMedium,
                            color = Color.Gray
                        )
                        Text(
                            modifier = Modifier.weight(0.3f),
                            text = stringResource(id = R.string.workout_screen_weight_column),
                            textAlign = TextAlign.Center,
                            style = Typography.labelMedium,
                            color = Color.Gray
                        )
                        Text(
                            modifier = Modifier.weight(0.3f),
                            text = stringResource(id = R.string.workout_screen_reps_column),
                            textAlign = TextAlign.Center,
                            style = Typography.labelMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.weight(0.15f))
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

                    if (!isDeleteMode && !exercise.isCompleted) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            AddSetButton(
                                modifier = Modifier.weight(0.5f),
                                onAddSetClick = { onAddSetClick(exercise.id) }
                            )
                            FinishExerciseButton(
                                modifier = Modifier.weight(0.5f),
                                onFinishExercise = {
                                    showCompleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddSetButton(modifier: Modifier, onAddSetClick: () -> Unit) {
    TextButton(
        onClick = onAddSetClick,
        modifier = modifier.height(48.dp)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = stringResource(id = R.string.content_description_add),
            tint = CyanAccent
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            stringResource(id = R.string.workout_screen_add_set_button),
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun FinishExerciseButton(modifier: Modifier = Modifier, onFinishExercise: () -> Unit) {
    Button(
        onClick = onFinishExercise,
        modifier = modifier
            .height(48.dp)
            // 1. Aplicamos o background e o shape ao container do botão completo
            .background(
                brush = AppTheme.brushes.primaryGradient,
                shape = RoundedCornerShape(20.dp)
            ),
        // 2. Removemos a cor padrão do botão para que o gradiente apareça
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        // 3. Garantimos que o conteúdo interno não tenha padding extra indesejado
        contentPadding = PaddingValues(horizontal = 16.dp), // Ajuste conforme necessário
        shape = RoundedCornerShape(20.dp) // Essencial para o clique respeitar a forma
    ) {
        Text(
            // 4. O texto agora só se preocupa com o seu próprio estilo e gradiente
            text = "Finalizar exercício",
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
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

    val canCompleteWorkout = remember(workout) {
        workout?.exercises?.all { exercise ->
            exercise.exerciseSets.all { set ->
                set.weight.isNotBlank() && set.weight != "0" && set.reps.isNotBlank() && set.reps != "0"
            }
        } == true
    }

    LaunchedEffect(isPressed, workout?.isCompleted) {
        if (workout?.isCompleted == false) {
            if (isPressed && canCompleteWorkout) {
                progress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(1500, easing = LinearEasing)
                )
                if (progress.value == 1f) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete()
                }
            } else {
                progress.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(300, easing = LinearEasing)
                )
            }
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = if (canCompleteWorkout && workout?.isCompleted == false) 16.dp else 0.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = CyanAccent.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                if (canCompleteWorkout && workout?.isCompleted == false) {
                    CyanGradient
                } else if (workout?.isCompleted == true) {
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                } else {
                    Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray))
                }
            )
            .pointerInput(canCompleteWorkout, workout?.isCompleted) {
                detectTapGestures(
                    onPress = {
                        if (canCompleteWorkout && workout?.isCompleted == false) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    }
                )
            }
            .drawBehind {
                if (progress.value > 0f) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.2f),
                        size = size.copy(width = size.width * progress.value)
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when {
                workout?.isCompleted == true -> stringResource(id = R.string.workout_screen_finish_button_completed)
                !canCompleteWorkout -> stringResource(id = R.string.workout_screen_finish_button_fill_sets)
                else -> stringResource(id = R.string.workout_screen_finish_button_hold)
            },
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun InputTextBox(
    modifier: Modifier = Modifier,
    isLocked: Boolean = false,
    inputValue: String,
    onValueChange: (String) -> Unit
) {
    // 1. Criamos um estado local que guarda o texto E a posição do cursor
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = inputValue, selection = TextRange(inputValue.length)))
    }

    // 2. Mantemos o estado local sincronizado caso o valor mude externamente
    // (ex: limpa o campo ou carrega dados do banco)
    LaunchedEffect(inputValue) {
        if (inputValue != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = inputValue,
                selection = TextRange(inputValue.length) // Força o cursor pro final
            )
        }
    }

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
            // 3. Usamos o TextFieldValue em vez da String
            value = textFieldValue,
            onValueChange = { newValue ->
                // Filtramos a string interna de TextFieldValue
                if (newValue.text.all { it.isDigit() || it == '.' || it == ',' }) {
                    // Atualiza o estado local imediatamente (preservando o cursor onde o usuário digitou)
                    textFieldValue = newValue
                    // Notifica o ViewModel/pai passando apena a String, como você já fazia
                    onValueChange(newValue.text)
                }
            },
            enabled = !isLocked,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Composable
fun ErrorDialog(
    onDismissRequest: () -> Unit,
    text: String
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(
                text = stringResource(id = R.string.dialog_error_title),
                style = Typography.titleMedium
            )
        },
        text = { Text(text = text, style = Typography.bodyLarge) },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text(
                    stringResource(id = R.string.dialog_error_confirm_button),
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
    )
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
    var errorText by remember { mutableStateOf("") }

    val defaultErrorText = stringResource(id = R.string.workout_screen_default_error_message)
    val removeSavedSetError = stringResource(id = R.string.workout_screen_error_remove_saved_set)
    val saveIncompleteSetError =
        stringResource(id = R.string.workout_screen_error_save_incomplete_set)

    LaunchedEffect(Unit) {
        if (errorText.isEmpty()) errorText = defaultErrorText
    }

    if (showErrorDialog) {
        ErrorDialog(text = errorText, onDismissRequest = { showErrorDialog = false })
    }

    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.workout_screen_save_set_dialog_title),
                    style = Typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.workout_screen_save_set_dialog_message,
                        setNumber
                    ),
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
                        stringResource(id = R.string.workout_screen_save_button),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text(stringResource(id = R.string.dialog_cancel_button), color = Color.Gray)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = {
                Text(
                    text = stringResource(id = R.string.workout_screen_remove_set_dialog_title),
                    style = Typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.workout_screen_remove_set_dialog_message,
                        setNumber
                    ),
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
                    Text(
                        stringResource(id = R.string.workout_screen_remove_button),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(
                        stringResource(id = R.string.dialog_cancel_button),
                        color = MaterialTheme.colorScheme.secondary
                    )
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
                onValueChange = { onWeightChange(setNumber, it) }
            )

            InputTextBox(
                modifier = Modifier
                    .weight(0.15f)
                    .height(50.dp),
                isLocked = isCompleted,
                inputValue = inputValueReps,
                onValueChange = { onRepsChange(setNumber, it) }
            )

            Box(
                modifier = Modifier
                    .weight(0.1f)
                    .height(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (isDeleteMode) {
                            if (isCompleted) {
                                errorText = removeSavedSetError
                                showErrorDialog = true
                            } else {
                                showDeleteDialog = true
                            }
                        } else {
                            if (!isCompleted) {
                                if (inputValueReps != "0" && inputValueReps != "" && inputValueWeight != "" && inputValueWeight != "0") {
                                    showCompleteDialog = true
                                } else {
                                    errorText = saveIncompleteSetError
                                    showErrorDialog = true
                                }
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isDeleteMode) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.content_description_delete),
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutProgressBar(modifier: Modifier = Modifier, progress: Float, workout: Workout?) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec, label = ""
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "1 of 8 exercises done",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CyanAccent
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxSize()
                    .background(CyanGradient)
            )
        }
    }
}