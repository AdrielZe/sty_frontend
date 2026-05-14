package com.example.training_tracker.ui.screens.workout_details

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.components.MuscleGroupPickerDialog
import com.example.training_tracker.ui.screens.create_workout.ExerciseBadge
import com.example.training_tracker.ui.screens.home.MainGradientButton
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.Dimens
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    onEditWorkoutName: (String) -> Unit,
    viewModel: WorkoutEditViewModel = viewModel(factory = WorkoutEditViewModel.Factory)
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
                        text = if (uiState.isEditMode) stringResource(id = R.string.workout_edit_title) else stringResource(
                            R.string.iniciar_treino
                        ),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .onGloballyPositioned { parentRootPosition = it.positionInRoot() }
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppTheme.accent.light)
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

                            // 👇 1. A Column principal agora tem weight(1f) para garantir
                            // que pare de crescer antes de empurrar o botão de editar para fora.
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = workout.name.uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                // 👇 2. Linha de métricas secundárias
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = stringResource(
                                            id = R.string.workout_details_total_exercises,
                                            workout.exercises.size
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        // 👇 3. PROTEÇÃO: Se o texto "12 exercícios" for muito longo em algum idioma,
                                        // ele recebe reticências e protege o ícone do cronômetro à direita.
                                        modifier = Modifier.weight(1f, fill = false),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    // CRONÔMETRO
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = AppTheme.accent.light,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${workout.estimatedTime} min",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1 // Garante que o tempo nunca quebre linha
                                        )
                                    }
                                }
                            }

                            // 👇 4. Espaçamento de segurança entre os textos e o botão de editar
                            if (uiState.isEditMode) {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { showEditWorkoutNameDialog = true }
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
                            color = AppTheme.accent.light,
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
                                        if (isHovered && !isBeingDragged) AppTheme.accent.light.copy(alpha = 0.1f) else Color.Transparent,
                                        RoundedCornerShape(Dimens.cornerRadius)
                                    )
                                    .then(
                                        if (isHovered && !isBeingDragged) Modifier.border(
                                            2.dp,
                                            AppTheme.accent.light,
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

        uiState.pendingExerciseName?.let { pendingName ->
            if (uiState.showMuscleGroupPicker) {
                MuscleGroupPickerDialog(
                    exerciseName = pendingName,
                    onDismiss = { viewModel.dismissMuscleGroupPicker() },
                    onMuscleGroupSelected = { muscleGroup ->
                        viewModel.onMuscleGroupSelected(muscleGroup)
                    }
                )
            }
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
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.accent.light.copy(alpha = 0.05f),
        border = BorderStroke(2.dp, AppTheme.accent.light.copy(alpha = 0.3f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = AppTheme.accent.light)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(id = R.string.workout_details_add_exercise_button),
                color = AppTheme.accent.light,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun ExerciseDetailItem(
    number: Int,
    exercise: Exercise,
    isEditMode: Boolean,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge do Número
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = AppTheme.accent.light.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = number.toString(),
                        color = AppTheme.accent.light,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                val exerciseType = exercise.type ?: ExerciseType.STRENGTH

                when (exerciseType) {
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

//                        val cardioDetails = listOfNotNull(
//                            exercise.time?.takeIf { it.isNotBlank() }?.let { "$it min" },
//                            exercise.distance?.takeIf { it.isNotBlank() }?.let { "$it km" }
//                        ).joinToString(" • ")

//                        if (cardioDetails.isNotEmpty()) {
//                            Text(
//                                text = cardioDetails,
//                                style = MaterialTheme.typography.labelSmall,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                fontWeight = FontWeight.Bold
//                            )
//                        }
                    }
                    ExerciseType.STRETCHING -> {
                        ExerciseBadge(
                            text = "ALONGAMENTO",
                            backgroundColor = Color(0xFF4CAF50)
                        )
                    }
                }
            }

            if (isEditMode) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
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
fun AddExerciseSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    availableExercises: List<Exercise>
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = remember(searchQuery, availableExercises) {
        val query = searchQuery.trim()
        if (query.isEmpty()) availableExercises
        else availableExercises.filter { it.name.contains(query, ignoreCase = true) }
    }

    val showCreateOption = remember(searchQuery, filteredExercises) {
        searchQuery.isNotBlank() && filteredExercises.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp) // Limita a altura para não sumir com os botões
                .padding(vertical = 16.dp)
                .border(1.dp, AppTheme.accent.light.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone de Topo (Contexto Visual)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AppTheme.accent.light.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter, // Ícone de exercício
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(id = R.string.workout_details_select_exercise_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Campo de Pesquisa Moderno
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            stringResource(id = R.string.workout_details_search_exercise_placeholder),
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.accent.light) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accent.light,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        cursorColor = AppTheme.accent.light,
                        focusedContainerColor = AppTheme.accent.light.copy(alpha = 0.02f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de Exercícios
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showCreateOption) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(searchQuery.trim()) },
                                shape = RoundedCornerShape(12.dp),
                                color = AppTheme.accent.light.copy(alpha = 0.05f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = AppTheme.accent.light)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "${stringResource(R.string.criar)} \"${searchQuery.trim()}\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AppTheme.accent.light,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    items(filteredExercises) { exercise ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(exercise.name) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = exercise.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )

                                // Tag de Grupo Muscular (Localizada)
                                Surface(
                                    color = AppTheme.accent.light,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        text = stringResource(exercise.muscleGroup?.resId ?: R.string.detalhes_do_treino_desconhecido),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botão Cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        stringResource(id = R.string.workout_details_cancel_button),
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AddCardioSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    availableExercises: List<Exercise>
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = remember(searchQuery, availableExercises) {
        val availableCardioExercises = availableExercises.filter { it.muscleGroup == MuscleGroups.CARDIO }
        val query = searchQuery.trim()
        if (query.isEmpty()) availableCardioExercises
        else availableCardioExercises.filter { it.name.contains(query, ignoreCase = true) }
    }

    val showCreateOption = remember(searchQuery, filteredExercises) {
        searchQuery.isNotBlank() && filteredExercises.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp) // Limita a altura para não sumir com os botões
                .padding(vertical = 16.dp)
                .border(1.dp, AppTheme.accent.light.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone de Topo (Contexto Visual)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AppTheme.accent.light.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RunCircle, // Ícone de corrida
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.selecionar_cardio),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Campo de Pesquisa Moderno
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.buscar_cardio),
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.accent.light) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accent.light,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        cursorColor = AppTheme.accent.light,
                        focusedContainerColor = AppTheme.accent.light.copy(alpha = 0.02f)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de Exercícios
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showCreateOption) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(searchQuery.trim()) },
                                shape = RoundedCornerShape(12.dp),
                                color = AppTheme.accent.light.copy(alpha = 0.05f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = AppTheme.accent.light)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "${stringResource(R.string.criar)} \"${searchQuery.trim()}\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AppTheme.accent.light,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    items(filteredExercises) { exercise ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(exercise.name) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = exercise.name,
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )

                                // Tag de Grupo Muscular (Localizada)
                                Surface(
                                    color = AppTheme.accent.light,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        text = stringResource(exercise.muscleGroup?.resId ?: R.string.detalhes_do_treino_desconhecido),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botão Cancelar
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        stringResource(id = R.string.workout_details_cancel_button),
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}



@Composable
fun AddStretchingSelectionDialog(
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
    availableExercises: List<Exercise>
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredExercises = remember(searchQuery, availableExercises) {
        val stretchingExercises = availableExercises.filter { it.muscleGroup == MuscleGroups.STRETCHING }
        val query = searchQuery.trim()
        if (query.isEmpty()) stretchingExercises
        else stretchingExercises.filter { it.name.contains(query, ignoreCase = true) }
    }

    val showCreateOption = remember(searchQuery, filteredExercises) {
        searchQuery.isNotBlank() && filteredExercises.isEmpty()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp)
                .padding(vertical = 16.dp)
                .border(1.dp, AppTheme.accent.light.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(AppTheme.accent.light.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SelfImprovement,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.selecionar_alongamento),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(text = stringResource(R.string.buscar_alongamento), color = Color.Gray.copy(alpha = 0.6f))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppTheme.accent.light) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accent.light,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        cursorColor = AppTheme.accent.light,
                        focusedContainerColor = AppTheme.accent.light.copy(alpha = 0.02f)
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showCreateOption) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onSelect(searchQuery.trim()) },
                                shape = RoundedCornerShape(12.dp),
                                color = AppTheme.accent.light.copy(alpha = 0.05f)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = AppTheme.accent.light)
                                    Spacer(Modifier.width(12.dp))
                                    Text(
                                        text = "${stringResource(R.string.criar)} \"${searchQuery.trim()}\"",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AppTheme.accent.light,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    items(filteredExercises) { exercise ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onSelect(exercise.name) },
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = exercise.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Surface(color = AppTheme.accent.light, shape = RoundedCornerShape(6.dp)) {
                                    Text(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        text = stringResource(exercise.muscleGroup?.resId ?: R.string.detalhes_do_treino_desconhecido),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    Text(stringResource(id = R.string.workout_details_cancel_button), color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
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
                placeholder = { Text(text = workout?.name ?: stringResource(R.string.treino_min)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AppTheme.accent.light,
                    focusedLabelColor = AppTheme.accent.light
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
                Text(stringResource(R.string.confirmar), color = AppTheme.accent.light, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancelar), color = Color.Gray)
            }
        }
    )
}

@Composable
fun RemoveExerciseDialog(
    exerciseName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .padding(16.dp)
                .border(1.dp, Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.workout_details_remove_exercise_confirm_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.workout_details_remove_exercise_confirm_message, exerciseName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.workout_details_cancel_button), color = Color.Gray)
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.workout_details_remove_button), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}