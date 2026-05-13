package com.example.training_tracker.ui.screens.registered_workouts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.screens.home.MuscleBadge
import com.example.training_tracker.ui.screens.home.getWorkoutImageRes
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import kotlinx.coroutines.delay
import java.time.DayOfWeek

// --- Configuração de Cores ---
val TextGray = Color(0xFF94A3B8)

@Composable
fun DragAndDropContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val state = remember { DragAndDropState() }
    var containerPositionInWindow by remember { mutableStateOf(Offset.Zero) }
    val density = LocalDensity.current

    CompositionLocalProvider(LocalDragAndDropState provides state) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .onGloballyPositioned { containerPositionInWindow = it.positionInWindow() }
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { offset ->
                            val windowOffset = containerPositionInWindow + offset
                            val entry = state.itemBounds.entries.firstOrNull {
                                it.value.contains(windowOffset)
                            }
                            if (entry != null) {
                                val workout = state.itemData[entry.key]
                                state.draggedItem = workout
                                state.isDragging = true
                                state.dragPosition = entry.value.topLeft
                                state.dragOffset = Offset.Zero
                                state.draggableItemSize =
                                    IntSize(entry.value.width.toInt(), entry.value.height.toInt())
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            state.dragOffset += dragAmount
                        },
                        onDragEnd = { state.isDragging = false },
                        onDragCancel = {
                            state.isDragging = false
                            state.draggedItem = null
                        }
                    )
                }
        ) {
            content()

            if (state.isDragging && state.draggedItem != null) {
                val ghostWidth = with(density) { state.draggableItemSize.width.toDp() }
                val ghostHeight = with(density) { state.draggableItemSize.height.toDp() }

                Box(
                    modifier = Modifier
                        .size(ghostWidth, ghostHeight)
                        .graphicsLayer {
                            val absoluteX = state.dragPosition.x + state.dragOffset.x
                            val absoluteY = state.dragPosition.y + state.dragOffset.y
                            translationX = absoluteX - containerPositionInWindow.x
                            translationY = absoluteY - containerPositionInWindow.y
                            alpha = 0.8f
                            scaleX = 0.95f
                            scaleY = 0.95f
                        }
                ) {
                    ActiveWorkoutCard(
                        title = state.draggedItem!!.name,
                        exercises = state.draggedItem!!.exercises.size,
                        duration = "${state.draggedItem!!.estimatedTime} min",
                        onWorkoutClick = {},
                        workout = state.draggedItem!!
                    )
                }
            }
        }
    }
}

internal class DragAndDropState {
    var isDragging by mutableStateOf(false)
    var dragPosition by mutableStateOf(Offset.Zero)
    var dragOffset by mutableStateOf(Offset.Zero)
    var draggedItem by mutableStateOf<Workout?>(null)
    var draggableItemSize by mutableStateOf(IntSize.Zero)
    val itemBounds = mutableStateMapOf<String, Rect>()
    val itemData = mutableStateMapOf<String, Workout>()
}

internal val LocalDragAndDropState = compositionLocalOf { DragAndDropState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisteredWorkoutsScreen(
    onNavigateBack: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    onCreateWorkoutClick: (DayOfWeek?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RegisteredWorkoutsViewModel = viewModel(factory = RegisteredWorkoutsViewModel.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val listState = rememberLazyListState()
    
    DragAndDropContainer(modifier = modifier) {
        val state = LocalDragAndDropState.current
        var columnBounds by remember { mutableStateOf(Rect.Zero) }

        LaunchedEffect(state.isDragging) {
            while (state.isDragging) {
                val currentY = state.dragPosition.y + state.dragOffset.y
                val threshold = 250f
                if (columnBounds != Rect.Zero) {
                    if (currentY < columnBounds.top + threshold) listState.scrollBy(-12f)
                    else if (currentY > columnBounds.bottom - threshold) listState.scrollBy(12f)
                }
                delay(16)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.agenda_de_treinos),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back), tint = CyanAccent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
        ) { paddingValues ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .onGloballyPositioned { columnBounds = it.boundsInWindow() },
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp)
            ) {
                item { WeeklyPlanHeader() }

                DayOfWeek.entries.forEach { day ->
                    val workouts = uiState.workoutsByDay[day] ?: emptyList()
                    item {
                        DropTarget(onDrop = { workout ->
                            viewModel.moveWorkout(workout, day)
                            state.draggedItem = null
                        }) { isOver ->
                            DaySection(day = day.toLocalizedName(), count = workouts.size, isHighlighted = isOver) {
                                if (workouts.isEmpty()) {
                                    NoActivityCard(onCreateWorkoutClick = { onCreateWorkoutClick(day) })
                                } else {
                                    workouts.forEach { workout ->
                                        DragTarget(data = workout) {
                                            ActiveWorkoutCard(
                                                title = workout.name,
                                                exercises = workout.exercises.size,
                                                duration = "${workout.estimatedTime} min",
                                                onWorkoutClick = onWorkoutClick,
                                                onDeleteClick = { viewModel.deleteWorkout(workout) },
                                                onDuplicateClick = { targetDay -> viewModel.duplicateWorkout(workout, targetDay) },
                                                workout = workout
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveWorkoutCard(
    title: String,
    exercises: Int,
    duration: String,
    onWorkoutClick: (String) -> Unit,
    onDeleteClick: () -> Unit = {},
    onDuplicateClick: (DayOfWeek) -> Unit = {},
    workout: Workout
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDuplicateDialog by remember { mutableStateOf(false) }

    val muscleGroups = remember(workout) {
        workout.exercises.mapNotNull { it.muscleGroup }.distinct()
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.workout_details_remove_exercise_confirm_title)) },
            text = {
                Text(stringResource(R.string.tem_certeza_que_deseja_remover_o_treino, workout.name))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.workout_details_remove_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.workout_details_cancel_button))
                }
            }
        )
    }

    if (showDuplicateDialog) {
        DuplicateWorkoutDialog(
            workoutName = workout.name,
            currentDay = workout.dayOfWeek,
            onDismiss = { showDuplicateDialog = false },
            onConfirm = { targetDay ->
                onDuplicateClick(targetDay)
                showDuplicateDialog = false
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(CyanAccent.copy(alpha = 0.25f), Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onWorkoutClick(workout.id) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // IMAGEM DO TREINO — rounded square with image + gradient overlay
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(getWorkoutImageRes(workout))
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.4f))
                            )
                        )
                )
            }

            Spacer(Modifier.width(14.dp))

            // TEXTOS
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WorkoutInfoChip(
                        icon = Icons.AutoMirrored.Filled.List,
                        label = stringResource(R.string.exercicios, exercises)
                    )
                    WorkoutInfoChip(
                        icon = Icons.Default.Schedule,
                        label = duration
                    )
                }
                if (muscleGroups.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(muscleGroups) { muscle ->
                            MuscleBadgeRegistered(muscle = muscle)
                        }
                    }
                }
            }

            // MENU DROPDOWN
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.content_description_more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.duplicar_treino)) },
                        onClick = {
                            showDuplicateDialog = true
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = CyanAccent)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.workout_details_remove_button)) },
                        onClick = {
                            showDeleteDialog = true
                            showMenu = false
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleBadgeRegistered(muscle: MuscleGroups) {
    Surface(
        color = CyanAccent.copy(alpha = 0.08f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, CyanAccent.copy(alpha = 0.25f))
    ) {
        Text(
            text = stringResource(muscle.resId),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = CyanAccent,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun WorkoutInfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(12.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DuplicateWorkoutDialog(
    workoutName: String,
    currentDay: DayOfWeek?,
    onDismiss: () -> Unit,
    onConfirm: (DayOfWeek) -> Unit
) {
    var selectedDay by remember { mutableStateOf<DayOfWeek?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = stringResource(R.string.duplicar_treino),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = workoutName,
                    style = MaterialTheme.typography.bodySmall,
                    color = CyanAccent,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.selecione_o_dia_para_duplicar),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayOfWeek.entries.chunked(4).forEach { rowDays ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowDays.forEach { day ->
                                val isSelected = day == selectedDay
                                val isCurrent = day == currentDay
                                val locale = java.util.Locale.getDefault()
                                val label = day.getDisplayName(
                                    java.time.format.TextStyle.SHORT,
                                    locale
                                ).replaceFirstChar { it.titlecase(locale) }

                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedDay = day },
                                    shape = RoundedCornerShape(10.dp),
                                    color = when {
                                        isSelected -> CyanAccent
                                        isCurrent -> CyanAccent.copy(alpha = 0.12f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    },
                                    border = if (isCurrent && !isSelected)
                                        BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f))
                                    else null
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isSelected -> Color.White
                                                isCurrent -> CyanAccent
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        if (isCurrent) {
                                            Spacer(Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isSelected) Color.White else CyanAccent)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (currentDay != null) {
                    Text(
                        text = stringResource(R.string.dia_atual_indicado),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedDay?.let { onConfirm(it) } },
                enabled = selectedDay != null
            ) {
                Text(
                    text = stringResource(R.string.duplicar),
                    color = if (selectedDay != null) CyanAccent
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.workout_details_cancel_button))
            }
        }
    )
}

@Composable
fun DaySection(
    day: String,
    count: Int,
    isHighlighted: Boolean = false,
    content: @Composable () -> Unit
) {
    val highlightAlpha by animateFloatAsState(if (isHighlighted) 0.12f else 0f, label = "highlight")
    val hasWorkouts = count > 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CyanAccent.copy(alpha = highlightAlpha),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(if (isHighlighted) 8.dp else 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                if (hasWorkouts) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CyanAccent)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    day,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isHighlighted || hasWorkouts) CyanAccent
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                )
            }
            Surface(
                color = if (hasWorkouts) CyanAccent.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                shape = CircleShape
            ) {
                Text(
                    count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = if (hasWorkouts) CyanAccent else TextGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        content()
    }
}

@Composable
fun WeeklyPlanHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "07",
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                stringResource(R.string.plano_semanal),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                stringResource(R.string.consistencia_e_a_chave),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = CyanAccent,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun NoActivityCard(onCreateWorkoutClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(CyanAccent.copy(alpha = 0.2f), CyanAccent.copy(alpha = 0.05f))
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .background(CyanAccent.copy(alpha = 0.03f))
            .clickable { onCreateWorkoutClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CyanAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Column {
            Text(
                text = stringResource(R.string.nenhuma_atividade),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(R.string.toque_para_adicionar_treino),
                style = MaterialTheme.typography.labelSmall,
                color = CyanAccent.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun DragTarget(data: Workout, content: @Composable () -> Unit) {
    val state = LocalDragAndDropState.current
    Box(modifier = Modifier.onGloballyPositioned {
        if (it.isAttached) {
            state.itemBounds[data.id] = it.boundsInWindow()
            state.itemData[data.id] = data
        }
    }) { content() }
    DisposableEffect(data.id) {
        onDispose {
            state.itemBounds.remove(data.id)
            state.itemData.remove(data.id)
        }
    }
}

@Composable
fun DropTarget(onDrop: (Workout) -> Unit, content: @Composable (isOver: Boolean) -> Unit) {
    val state = LocalDragAndDropState.current
    var isOver by remember { mutableStateOf(false) }
    var rect by remember { mutableStateOf(Rect.Zero) }
    val currentDragPosition = if (state.isDragging) {
        state.dragPosition + state.dragOffset + Offset(state.draggableItemSize.width / 2f, state.draggableItemSize.height / 2f)
    } else Offset.Zero
    LaunchedEffect(currentDragPosition, state.isDragging) {
        if (state.isDragging) isOver = rect.contains(currentDragPosition)
        else {
            if (isOver && state.draggedItem != null) onDrop(state.draggedItem!!)
            isOver = false
        }
    }
    Box(modifier = Modifier.onGloballyPositioned { rect = it.boundsInWindow() }) { content(isOver) }
}

@Composable
fun DayOfWeek.toLocalizedName(): String {
    val locale = java.util.Locale.getDefault()

    return this.getDisplayName(java.time.format.TextStyle.FULL, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
}