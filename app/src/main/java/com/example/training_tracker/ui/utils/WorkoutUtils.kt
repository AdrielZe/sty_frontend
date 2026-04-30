package com.example.training_tracker.ui.utils

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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

@Composable
fun ExerciseCardUtils(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    isExpanded: Boolean = false,
    onExpandedChange: (Boolean) -> Unit,
    isLocked: Boolean = false,
    onRepsChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onAddSetClick: (String) -> Unit,
    onRemoveSet: (String, Int) -> Unit,
    onCompleteExercise: () -> Unit,
    onReopenExercise: () -> Unit,
    onRemoveExercise: (String) -> Unit = {},
    workout: Workout?
) {
    var isDeleteMode by rememberSaveable(exercise.id) { mutableStateOf(false) }
    var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showCompleteDialog by rememberSaveable { mutableStateOf(false) }
    var showErrors by rememberSaveable(exercise.id) { mutableStateOf(false) }

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


    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.finalizar_exercicio),
                    style = Typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.tem_certeza_que_deseja_finalizar_o_exercicio_ele_sera_marcado_como_concluido_e_nao_podera_mais_ser_editado,
                        exercise.name
                    ),
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
                            showErrors = false
                        } else {
                            showCompleteDialog = false
                            showErrors = true
                        }
                    }
                ) {
                    Text(
                        text = stringResource(R.string.confirmar),
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
                        text = stringResource(R.string.cancelar),
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
                            text = stringResource(
                                R.string.number_series,
                                exercise.exerciseSets.size
                            ),
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
                                inputValueReps = set.reps,
                                inputValueWeight = set.weight,
                                isDeleteMode = false,
                                isCompleted = true,
                                onRepsChange = { _, _ -> },
                                onWeightChange = { _, _ -> },
                                onDeleteClick = { },
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
                                    if (workout?.id == "freestyle_workout_id") {
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
                                    }
                                } else {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                stringResource(R.string.pronto),
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
                                stringResource(R.string.serie_atual, activeSet),
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
                        val isWeightError = showErrors && (set.weight.replace(',', '.').toDoubleOrNull() ?: 0.0) <= 0.0
                        val isRepsError = showErrors && (set.reps.toIntOrNull() ?: 0) <= 0

                        SetLine(
                            modifier = Modifier.fillMaxWidth(),
                            inputValueReps = set.reps,
                            inputValueWeight = set.weight,
                            isDeleteMode = isDeleteMode,
                            isCompleted = set.isCompleted,
                            isErrorWeight = isWeightError,
                            isErrorReps = isRepsError,
                            onRepsChange = { _, newText ->
                                onRepsChange(set.set, newText)
                            },
                            onWeightChange = { _, newText ->
                                onWeightChange(set.set, newText)
                            },
                            onDeleteClick = { onRemoveSet(exercise.id, set.set) },
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
                                stringResource(R.string.adicionar_serie),
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
                                stringResource(R.string.finalizar_exercicio_max),
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
                        stringResource(
                            R.string.series_reps,
                            exercise.exerciseSets.size,
                            exercise.exerciseSets.firstOrNull()?.reps ?: 0
                        ),
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

@Composable
fun InputTextBox(
    modifier: Modifier = Modifier,
    label: String,
    isLocked: Boolean = false,
    isError: Boolean = false,
    maxLength: Int = Int.MAX_VALUE,
    inputValue: String,
    onValueChange: (String) -> Unit
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = inputValue, selection = TextRange(inputValue.length)))
    }

    LaunchedEffect(inputValue) {
        if (inputValue != textFieldValue.text) {
            textFieldValue = textFieldValue.copy(
                text = inputValue,
                selection = TextRange(inputValue.length)
            )
        }
    }

    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = if (isError) Color.Red else TextGray, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> Color.Red
                        isLocked -> Color.Transparent
                        else -> CyanAccent.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    if (newValue.text.length <= maxLength && newValue.text.all { it.isDigit() || it == '.' || it == ',' }) {
                        textFieldValue = newValue
                        onValueChange(newValue.text)
                    }
                },
                enabled = !isLocked,
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
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
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
fun SetLine(
    modifier: Modifier = Modifier,
    isDeleteMode: Boolean,
    isCompleted: Boolean = false,
    onDeleteClick: () -> Unit,
    setNumber: Int,
    inputValueWeight: String,
    inputValueReps: String,
    isErrorWeight: Boolean = false,
    isErrorReps: Boolean = false,
    onRepsChange: (Int, String) -> Unit,
    onWeightChange: (Int, String) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    val defaultErrorText = stringResource(id = R.string.workout_screen_default_error_message)
    val removeSavedSetError = stringResource(id = R.string.workout_screen_error_remove_saved_set)

    LaunchedEffect(Unit) {
        if (errorText.isEmpty()) errorText = defaultErrorText
    }

    if (showErrorDialog) {
        ErrorDialog(text = errorText, onDismissRequest = { showErrorDialog = false })
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

    Column(
        modifier = modifier
            .padding(vertical = Dimens.paddingSmall)
            .clickable(enabled = !isCompleted) {
                if (isDeleteMode) {
                    showDeleteDialog = true
                }
            }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            InputTextBox(
                label = stringResource(R.string.serie_upper),
                modifier = Modifier.weight(1f),
                isLocked = true,
                inputValue = setNumber.toString(),
                onValueChange = {},
            )

            InputTextBox(
                label = stringResource(R.string.peso_kg),
                modifier = Modifier.weight(2f),
                maxLength = 4,
                isLocked = isCompleted,
                isError = isErrorWeight,
                inputValue = inputValueWeight,
                onValueChange = {
                    onWeightChange(setNumber, it)
                },
            )

            InputTextBox(
                label = "REPS",
                modifier = Modifier.weight(2f),
                isLocked = isCompleted,
                isError = isErrorReps,
                maxLength = 3,
                inputValue = inputValueReps,
                onValueChange = { onRepsChange(setNumber, it) },
            )
        }

        if (isDeleteMode && !isCompleted) {
            TextButton(
                onClick = {
                    showDeleteDialog = true
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.deletar_serie), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }
        }
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

    val currentWorkout by rememberUpdatedState(workout)
    val canCompleteWorkout by remember {
        derivedStateOf {
            val exercises = currentWorkout?.exercises ?: emptyList()
            val hasMinimumExercises = exercises.count { it.isCompleted } >= 3
            val allSetsFilled = exercises.all { ex ->
                ex.exerciseSets.all { set ->
                    set.weight.isNotBlank() && set.weight != "0" && set.reps.isNotBlank() && set.reps != "0"
                }
            }
            hasMinimumExercises && allSetsFilled
        }
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
                (workout?.exercises?.size
                    ?: 0) < 1 -> stringResource(R.string.comece_adicionando_um_exerc_cio)

                (workout?.exercises?.count { it.isCompleted }
                    ?: 0) < 3 -> stringResource(R.string.finalize_pelo_menos_3_exerc_cios)

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
fun WorkoutTopBar(
    workoutName: String,
    progressPercentage: Float,
    finishedCount: Int,
    totalCount: Int,
    onBackClick: () -> Unit,
    elapsedTime: Long = 0L,
    isPaused: Boolean = false,
    onPauseToggle: () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage / 100f,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "TopBarProgress"
    )

    val formattedTime = remember(elapsedTime) {
        val hours = (elapsedTime / 3600000)
        val minutes = (elapsedTime / 60000) % 60
        val seconds = (elapsedTime / 1000) % 60
        if (hours > 0) {
            "%02d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, top = 8.dp, end = 24.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.content_description_back),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = workoutName.uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(
                                R.string.de_exercicios_completos,
                                finishedCount,
                                totalCount
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGray
                        )
                        if (elapsedTime > 0) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(TextGray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                Icons.Default.Timer,
                                contentDescription = null,
                                tint = if (isPaused) Color.Gray else CyanAccent,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formattedTime,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isPaused) Color.Gray else CyanAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onPauseToggle,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (isPaused) stringResource(R.string.resume) else stringResource(
                                        R.string.pause
                                    ),
                                    tint = CyanAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
            Text(
                text = "%.0f%%".format(progressPercentage),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = CyanAccent
            )
        }

        // Progress line as bottom border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(CyanGradient)
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

