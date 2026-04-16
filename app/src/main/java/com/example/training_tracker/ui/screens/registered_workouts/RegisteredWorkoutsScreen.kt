package com.example.training_tracker.ui.screens.registered_workouts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.routes.Routes
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import kotlinx.coroutines.delay
import java.time.DayOfWeek

// --- Configuração de Cores Groffit Velocity ---

val TextGray = Color(0xFF94A3B8)

// --- Estado do Drag and Drop Melhorado ---
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
                            val entry = state.itemBounds.entries.firstOrNull { it.value.contains(windowOffset) }
                            if (entry != null) {
                                val workout = state.itemData[entry.key]
                                state.draggedItem = workout
                                state.isDragging = true
                                state.dragPosition = entry.value.topLeft
                                state.dragOffset = Offset.Zero
                                state.draggableItemSize = IntSize(entry.value.width.toInt(), entry.value.height.toInt())
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            state.dragOffset += dragAmount
                        },
                        onDragEnd = {
                            state.isDragging = false
                            // Não limpamos o draggedItem aqui para que o DropTarget possa lê-lo
                        },
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
                        duration = "55 min",
                        consistency = 0.78f,
                        onWorkoutClick = {},
                        workout = state.draggedItem!!
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisteredWorkoutsScreen(
    onNavigateBack: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    viewModel: RegisteredWorkoutsViewModel = viewModel(factory = RegisteredWorkoutsViewModel.Factory)
) {
    val uiState = viewModel.uiState.collectAsState().value
    val listState = rememberLazyListState()
    
    DragAndDropContainer {
        val state = LocalDragAndDropState.current
        var columnBounds by remember { mutableStateOf(Rect.Zero) }

        LaunchedEffect(state.isDragging) {
            while (state.isDragging) {
                val currentY = state.dragPosition.y + state.dragOffset.y
                val threshold = 250f
                
                if (columnBounds != Rect.Zero) {
                    if (currentY < columnBounds.top + threshold) {
                        listState.scrollBy(-12f)
                    } else if (currentY > columnBounds.bottom - threshold) {
                        listState.scrollBy(12f)
                    }
                }
                delay(16)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "WORKOUT SCHEDULE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                letterSpacing = 1.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { onNavigateBack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, tint = CyanAccent)
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Mais opções */ }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null, tint = CyanAccent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* Adicionar treino */ },
                    containerColor = Color.Transparent,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CyanGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Workout", modifier = Modifier.size(28.dp))
                    }
                }
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
                            state.draggedItem = null // Limpa o item após o drop bem sucedido
                        }) { isOver ->
                            DaySection(
                                day = day.name,
                                count = workouts.size,
                                isHighlighted = isOver
                            ) {
                                if (workouts.isEmpty()) {
                                    NoActivityCard()
                                } else {
                                    workouts.forEach { workout ->
                                        DragTarget(data = workout) {
                                            ActiveWorkoutCard(
                                                title = workout.name,
                                                exercises = workout.exercises.size,
                                                duration = "55 min",
                                                consistency = 0.78f,
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
fun DragTarget(
    data: Workout,
    content: @Composable () -> Unit
) {
    val state = LocalDragAndDropState.current
    
    Box(
        modifier = Modifier.onGloballyPositioned {
            if (it.isAttached) {
                state.itemBounds[data.id] = it.boundsInWindow()
                state.itemData[data.id] = data
            }
        }
    ) {
        content()
    }

    DisposableEffect(data.id) {
        onDispose {
            state.itemBounds.remove(data.id)
            state.itemData.remove(data.id)
        }
    }
}

@Composable
fun DropTarget(
    onDrop: (Workout) -> Unit,
    content: @Composable (isOver: Boolean) -> Unit
) {
    val state = LocalDragAndDropState.current
    var isOver by remember { mutableStateOf(false) }
    var rect by remember { mutableStateOf(Rect.Zero) }

    val currentDragPosition = if (state.isDragging) {
        state.dragPosition + state.dragOffset + Offset(
            state.draggableItemSize.width / 2f,
            state.draggableItemSize.height / 2f
        )
    } else {
        Offset.Zero
    }

    LaunchedEffect(currentDragPosition, state.isDragging) {
        if (state.isDragging) {
            isOver = rect.contains(currentDragPosition)
        } else {
            // Quando isDragging passa para false, se estiver sobre o alvo, executa o drop
            if (isOver && state.draggedItem != null) {
                onDrop(state.draggedItem!!)
            }
            isOver = false
        }
    }

    Box(
        modifier = Modifier.onGloballyPositioned {
            rect = it.boundsInWindow()
        }
    ) {
        content(isOver)
    }
}

@Composable
fun WeeklyPlanHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            "07",
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                "WEEKLY PLAN",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                "CONSISTENCY IS KEY",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp
                )
            )
        }
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
                color = MaterialTheme.colorScheme.onSurface .copy(alpha = 0.1f),
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
fun ActiveWorkoutCard(
    title: String,
    exercises: Int,
    duration: String,
    consistency: Float,
    onWorkoutClick: (String) -> Unit,
    onDeleteClick: () -> Unit = {},
    workout: Workout
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = AppTheme.brushes.backgroundGradient,
                shape = RoundedCornerShape(24.dp)
            )
            .border(width = 0.05.dp, color = CyanAccent, shape = RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = { onWorkoutClick(workout.id) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color = MaterialTheme.colorScheme.onSecondary)
                    ,contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FitnessCenter, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp)
                    Spacer(Modifier.height(5.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        Text(" $exercises exercises", fontSize = 12.sp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(12.dp))
                        Icon(Icons.Default.Timer, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                        Text(" $duration", fontSize = 12.sp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Remover") },
                            onClick = {
                                onDeleteClick()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSecondary)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(consistency)
                        .fillMaxHeight()
                        .background(CyanGradient)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CONSISTENCY", fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.Bold)
                Text("${(consistency * 100).toInt()}% MATCH", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RestDayCard(icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.1f), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(8.dp))
            Text("Rest Day", fontWeight = FontWeight.Bold, color = TextGray)
            Text("No workouts scheduled. Drag here to add.", fontSize = 11.sp, color = TextGray.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun NoActivityCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .border(1.dp,  MaterialTheme.colorScheme.surface.copy(alpha = 0.6f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = { /* Add */ },
                modifier = Modifier.background(Color.Transparent, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint =  MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            }
            Spacer(Modifier.height(8.dp))
            Text("No Activity", fontWeight = FontWeight.Bold, color = TextGray)
            Text("No workouts scheduled. Drag here to add.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun GroffitBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.Home.name,
            onClick = { onNavigate(Routes.Home.name) },
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
            label = { Text("HOME") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.RegisteredWorkouts.name,
            onClick = { onNavigate(Routes.RegisteredWorkouts.name) },
            icon = { Icon(Icons.Default.ListAlt, contentDescription = null) },
            label = { Text("WORKOUTS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.WorkoutHistory.name,
            onClick = { onNavigate(Routes.WorkoutHistory.name) },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("HISTORY") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )

        NavigationBarItem(
            selected = currentRoute == "PROFILE_ROUTE",
            onClick = { /* onNavigate(Routes.Profile.name) */ },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("PROFILE") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )
    }
}
