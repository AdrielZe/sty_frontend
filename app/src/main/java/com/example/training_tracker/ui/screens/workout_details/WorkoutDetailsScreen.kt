package com.example.training_tracker.ui.screens.workout_details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.mocks.availableExercises
import com.example.training_tracker.ui.screens.home.MainGradientButton
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import com.example.training_tracker.ui.theme.Dimens
import kotlin.math.max
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onEditWorkoutName: (String) -> Unit,
    viewModel: WorkoutDetailsViewModel = viewModel(factory = WorkoutDetailsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showEditWorkoutNameDialog by remember { mutableStateOf(false) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }

    val scrollState = rememberScrollState()

    var draggedExercise by remember { mutableStateOf<Exercise?>(null) }
    var dragStartOffsetInItem by remember { mutableStateOf(Offset.Zero) }
    val snackbarHostState = remember { SnackbarHostState() }
    var currentPointerPos by remember { mutableStateOf(Offset.Zero) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }

    val itemBounds = remember { mutableMapOf<String, Offset>() }
    val indexBounds = remember { mutableMapOf<Int, Rect>() }
    var parentRootPosition by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(id = R.string.workout_details_title),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onGloballyPositioned { parentRootPosition = it.positionInRoot() }
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            } else {
                val workout = uiState.workout
                if (workout != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = Dimens.paddingLarge)
                    ) {
                        Spacer(modifier = Modifier.height(Dimens.paddingMedium))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workout.name.uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.workout_details_total_exercises,
                                            workout.exercises.size
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = CyanAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${workout.estimatedTime} min",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (uiState.isEditMode) {
                                IconButton(
                                    onClick = {
                                        showEditWorkoutNameDialog = true;
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                        Text(
                            text = stringResource(id = R.string.workout_details_exercise_list_title),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                        workout.exercises.forEachIndexed { index, exercise ->
                            val isBeingDragged = draggedExercise?.id == exercise.id
                            val isHovered = hoveredIndex == index

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { layoutCoordinates ->
                                        indexBounds[index] = Rect(
                                            layoutCoordinates.positionInRoot(),
                                            layoutCoordinates.size.toSize()
                                        )
                                        itemBounds[exercise.id] = layoutCoordinates.positionInRoot()
                                    }
                                    .then(
                                        if (uiState.isEditMode) {
                                            Modifier.pointerInput(exercise) {
                                                detectDragGesturesAfterLongPress(
                                                    onDragStart = { offset ->
                                                        draggedExercise = exercise
                                                        dragStartOffsetInItem = offset
                                                        currentPointerPos = (itemBounds[exercise.id]
                                                            ?: Offset.Zero) + offset
                                                    },
                                                    onDrag = { change, _ ->
                                                        change.consume()
                                                        val itemRootPos =
                                                            itemBounds[exercise.id] ?: Offset.Zero
                                                        currentPointerPos =
                                                            itemRootPos + change.position

                                                        hoveredIndex = indexBounds.entries.find {
                                                            it.value.contains(currentPointerPos)
                                                        }?.key
                                                    },
                                                    onDragEnd = {
                                                        val fromIndex =
                                                            workout.exercises.indexOfFirst { it.id == draggedExercise?.id }
                                                        if (fromIndex != -1 && hoveredIndex != null && hoveredIndex != fromIndex) {
                                                            viewModel.moveExercise(
                                                                fromIndex,
                                                                hoveredIndex!!
                                                            )
                                                        }
                                                        draggedExercise = null
                                                        hoveredIndex = null
                                                    },
                                                    onDragCancel = {
                                                        draggedExercise = null
                                                        hoveredIndex = null
                                                    }
                                                )
                                            }
                                        } else Modifier
                                    )
                                    .alpha(if (isBeingDragged) 0f else 1f)
                                    .background(
                                        if (isHovered && !isBeingDragged) CyanAccent.copy(alpha = 0.1f) else Color.Transparent,
                                        RoundedCornerShape(Dimens.cornerRadius)
                                    )
                                    .then(
                                        if (isHovered && !isBeingDragged) Modifier.border(
                                            2.dp,
                                            CyanAccent,
                                            RoundedCornerShape(Dimens.cornerRadius)
                                        ) else Modifier
                                    )
                            ) {
                                ExerciseDetailItem(
                                    number = index + 1,
                                    exercise = exercise,
                                    isEditMode = uiState.isEditMode,
                                    onRemove = { exerciseToDelete = exercise }
                                )
                            }
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                        }

                        if (uiState.isEditMode) {
                            AddExerciseButton(onClick = { showAddExerciseDialog = true })
                            Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                        }

                        if (uiState.canStartWorkout) {
                            Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                            MainGradientButton(
                                text = stringResource(id = R.string.workout_details_start_workout_button),
                                onClick = {
                                    viewModel.startWorkout()
                                    onStartWorkout(workout.id)
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingLarge))
                    }
                }
            }

            // Ghost item
            draggedExercise?.let { exercise ->
                val ghostX = currentPointerPos.x - parentRootPosition.x - dragStartOffsetInItem.x
                val ghostY = currentPointerPos.y - parentRootPosition.y - dragStartOffsetInItem.y

                Box(
                    modifier = Modifier
                        .offset { IntOffset(ghostX.roundToInt(), ghostY.roundToInt()) }
                        .padding(horizontal = Dimens.paddingLarge)
                        .fillMaxWidth()
                        .alpha(0.8f)
                        .shadow(12.dp, RoundedCornerShape(Dimens.cornerRadius))
                ) {
                    ExerciseDetailItem(
                        number = (uiState.workout?.exercises?.indexOfFirst { it.id == exercise.id }
                            ?: 0) + 1,
                        exercise = exercise,
                        isEditMode = true,
                        onRemove = {}
                    )
                }
            }
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

        if (showEditWorkoutNameDialog) {
            EditWorkoutNameDialog(
                onDismiss = { showEditWorkoutNameDialog = false },
                onConfirm = { viewModel.updateWorkoutName(it) },
                workout = uiState.workout
            )
        }

        exerciseToDelete?.let { exercise ->
            AlertDialog(
                onDismissRequest = { exerciseToDelete = null },
                title = { Text(stringResource(R.string.workout_details_remove_exercise_confirm_title)) },
                text = { 
                    Text(stringResource(R.string.workout_details_remove_exercise_confirm_message, exercise.name)) 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.removeExercise(exercise.id)
                            exerciseToDelete = null
                        }
                    ) {
                        Text(stringResource(R.string.workout_details_remove_button), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { exerciseToDelete = null }) {
                        Text(stringResource(R.string.workout_details_cancel_button))
                    }
                }
            )
        }
    }
}

@Composable
fun AddExerciseButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(Dimens.cornerRadius),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            CyanAccent.copy(alpha = 0.5f)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = CyanAccent
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(id = R.string.workout_details_add_exercise_button),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun ExerciseDetailItem(
    number: Int,
    exercise: Exercise,
    isEditMode: Boolean,
    onRemove: () -> Unit,
    elevation: androidx.compose.ui.unit.Dp = 2.dp
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Dimens.cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier.padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(8.dp),
                color = CyanAccent.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number.toString(),
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(Dimens.paddingMedium))

            Row(
                modifier = Modifier
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Surface(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    color = CyanAccent,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        text = exercise.muscleGroup.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isEditMode) {
                IconButton(onClick = onRemove) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.content_description_remove),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AddExerciseSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    availableExercises: List<Exercise>
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filtro normal para a lista de baixo
    val filteredExercises = remember(searchQuery, availableExercises) {
        val query = searchQuery.trim()
        if (query.isEmpty()) {
            availableExercises
        } else {
            availableExercises.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    // A BLINDAGEM: Essa variável agora reage instantaneamente ao que é digitado.
    // Ela só será 'true' se houver texto E nenhum exercício tiver o nome exato.
    val showCreateOption = remember(searchQuery, filteredExercises) {
        searchQuery.isNotBlank() && filteredExercises.isEmpty()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(id = R.string.workout_details_select_exercise_dialog_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(stringResource(id = R.string.workout_details_search_exercise_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        focusedLabelColor = CyanAccent
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {

                    // USAMOS A VARIÁVEL BLINDADA AQUI
                    if (showCreateOption) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(searchQuery.trim()) },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Transparent
                            ) {
                                Text(
                                    text = searchQuery.trim(),
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = 0.5f
                                )
                            )
                        }
                    }

                    items(filteredExercises) { exercise ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(exercise.name) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent
                        ) {
                            Row(modifier = Modifier
                                .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
//                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = exercise.name,
                                    modifier = Modifier.padding(12.dp).weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Surface(
                                    modifier = Modifier.padding(4.dp),
                                    color = CyanAccent,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .padding(4.dp).weight(1f),
                                        text = exercise.muscleGroup.toString(),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.5f
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(id = R.string.workout_details_cancel_button),
                    color = Color.Gray
                )
            }
        }
    )
}

@Composable
fun EditWorkoutNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    workout: Workout?,
) {
    var nameText by rememberSaveable {mutableStateOf(workout?.name)  }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Editar o nome do treino",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            OutlinedTextField(
                value = nameText ?: "",
                onValueChange = { nameText = it },
                placeholder = { Text(text = workout?.name ?: "Treino") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanAccent,
                    focusedLabelColor = CyanAccent
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nameText?.isNotBlank() == true) {
                        onConfirm(nameText ?: "")
                        onDismiss()
                    }
                }
            ) {
                Text("Confirmar", color = CyanAccent, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}