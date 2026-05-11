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
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = CyanAccent)
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
    workout: Workout
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember {mutableStateOf(false)}

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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        CyanAccent.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        onClick = { onWorkoutClick(workout.id) }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // IMAGEM DO TREINO
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(getWorkoutImage(workout))
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.width(16.dp))

                // COLUNA DOS TEXTOS (Título + Infos)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        // 👇 1. PROTEÇÃO DO TÍTULO: Se for muito longo, corta com "..."
                        maxLines = 1, // ou 2, se preferir que ocupe mais espaço
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.exercicios, exercises),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold,
                            // 👇 2. PROTEÇÃO DO TEXTO 1: weight(1f, fill = false) permite que o texto encolha
                            // e receba "..." caso a tela seja muito fina, sem destruir o ícone de tempo!
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(Modifier.width(12.dp))

                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = CyanAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " $duration",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                            fontWeight = FontWeight.Bold,
                            // 👇 3. PROTEÇÃO DO TEXTO 2
                            modifier = Modifier.weight(1f, fill = false),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // MENU DROPDOWN
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Remover") },
                            onClick = {
                                showDeleteDialog = true
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }

            // LISTA DE MÚSCULOS
            if (muscleGroups.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(muscleGroups) { muscle ->
                        MuscleBadgeRegistered(muscle = muscle)
                    }
                }
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}

@Composable
fun MuscleBadgeRegistered(muscle: MuscleGroups) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp,  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
    ) {
        Text(
            text = stringResource(muscle.resId),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}
/**
 * Retorna o drawable ID baseado no MuscleGroup predominante do treino.
 */
fun getWorkoutImage(workout: Workout): Int {
    if (workout.exercises.isEmpty()) return R.drawable.workout

    // Encontra o grupo muscular mais frequente nos exercícios do treino
    val mostFrequentMuscleGroup = workout.exercises
        .mapNotNull { it.muscleGroup }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }?.key

    return when (mostFrequentMuscleGroup) {
        MuscleGroups.CHEST -> R.drawable.chest_workout
        MuscleGroups.BACK -> R.drawable.back_workout
        MuscleGroups.QUADRICEPS -> R.drawable.leg_workout
        MuscleGroups.HAMSTRINGS -> R.drawable.hamstrings_workout_home
        MuscleGroups.CALF -> R.drawable.calf_workout_home
        MuscleGroups.GLUTE -> R.drawable.glute_workout_home
        MuscleGroups.SHOULDERS -> R.drawable.shoulder_workout
        MuscleGroups.BICEPS -> R.drawable.biceps_workout
        MuscleGroups.TRICEPS -> R.drawable.triceps_workout
        MuscleGroups.ABS -> R.drawable.abs_workout
        else -> R.drawable.biceps_workout
    }
}

@Composable
fun DaySection(
    day: String,
    count: Int,
    isHighlighted: Boolean = false,
    content: @Composable () -> Unit
) {
    val backgroundColor by animateFloatAsState(if (isHighlighted) 0.15f else 0f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = CyanAccent.copy(alpha = backgroundColor),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(if (isHighlighted) 8.dp else 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                day,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isHighlighted) CyanAccent else MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 1.sp
                )
            )
            Spacer(Modifier.width(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Text(
                    count.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    fontSize = 10.sp,
                    color = TextGray,
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f))
            .clickable { onCreateWorkoutClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = CyanAccent.copy(alpha = 0.5f))
            Text(stringResource(R.string.nenhuma_atividade), fontWeight = FontWeight.Bold, color = TextGray.copy(alpha = 0.6f))
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