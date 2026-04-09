package com.example.training_tracker.ui.screens.registered_workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisteredWorkoutsScreen(
    onNavigateBack: () -> Unit,
    onWorkoutClick: (String) -> Unit,
    viewModel: RegisteredWorkoutsViewModel = viewModel(factory = RegisteredWorkoutsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val days = DayOfWeek.entries.toTypedArray()
    val snackbarHostState = remember { SnackbarHostState() }

    var draggedWorkout by remember { mutableStateOf<Workout?>(null) }
    var dragStartOffsetInItem by remember { mutableStateOf(Offset.Zero) }
    var currentPointerPos by remember { mutableStateOf(Offset.Zero) }
    var hoveredDay by remember { mutableStateOf<DayOfWeek?>(null) }

    val dayBounds = remember { mutableMapOf<DayOfWeek, androidx.compose.ui.geometry.Rect>() }
    val itemBounds = remember { mutableMapOf<String, Offset>() }
    var parentRootPosition by remember { mutableStateOf(Offset.Zero) }
    var containerHeight by remember { mutableStateOf(0f) }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

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
                        stringResource(id = R.string.registered_workouts_title),
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
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .onGloballyPositioned {
                parentRootPosition = it.positionInRoot()
                containerHeight = it.size.height.toFloat()
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = Dimens.paddingLarge, horizontal = Dimens.paddingSmall),
                verticalArrangement = Arrangement.spacedBy(Dimens.paddingLarge)
            ) {
                days.forEach { day ->
                    val isHovered = hoveredDay == day
                    val workouts = uiState.workoutsByDay[day] ?: emptyList()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { layoutCoordinates ->
                                val rect = androidx.compose.ui.geometry.Rect(
                                    layoutCoordinates.positionInRoot(),
                                    layoutCoordinates.size.run { androidx.compose.ui.geometry.Size(width.toFloat(), height.toFloat()) }
                                )
                                dayBounds[day] = rect
                            }
                            .background(
                                if (isHovered) CyanAccent.copy(alpha = 0.1f) else Color.Transparent,
                                RoundedCornerShape(Dimens.cornerRadius)
                            )
                            .then(if (isHovered) Modifier.border(2.dp, CyanAccent, RoundedCornerShape(Dimens.cornerRadius)) else Modifier)
                            .padding(Dimens.paddingSmall)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isHovered) CyanAccent else CyanAccent, // Cor mais forte
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(Dimens.paddingSmall))
                            Text(
                                text = "(${workouts.size})",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isHovered) CyanAccent else CyanAccent.copy(alpha = 0.8f),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Spacer(modifier = Modifier.height(Dimens.paddingSmall))

                        if (workouts.isEmpty()) {
                            Text(
                                text = stringResource(id = R.string.registered_workouts_drag_empty_state),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(Dimens.paddingSmall)
                            )
                        } else {
                            workouts.forEach { workout ->
                                val isBeingDragged = draggedWorkout?.id == workout.id

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .alpha(if (isBeingDragged) 0f else 1f)
                                        .onGloballyPositioned {
                                            itemBounds[workout.id] = it.positionInRoot()
                                        }
                                        .pointerInput(workout) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { offset ->
                                                    draggedWorkout = workout
                                                    dragStartOffsetInItem = offset
                                                    currentPointerPos = (itemBounds[workout.id] ?: Offset.Zero) + offset
                                                },
                                                onDrag = { change, _ ->
                                                    change.consume()

                                                    val itemRootPos = itemBounds[workout.id] ?: Offset.Zero
                                                    currentPointerPos = itemRootPos + change.position

                                                    val relativeY = currentPointerPos.y - parentRootPosition.y
                                                    val scrollThreshold = 100.dp.toPx()

                                                    if (relativeY < scrollThreshold) {
                                                        coroutineScope.launch { scrollState.scrollBy(-20f) }
                                                    } else if (relativeY > containerHeight - scrollThreshold) {
                                                        coroutineScope.launch { scrollState.scrollBy(20f) }
                                                    }

                                                    hoveredDay = dayBounds.entries.find { it.value.contains(currentPointerPos) }?.key
                                                },
                                                onDragEnd = {
                                                    hoveredDay?.let { newDay ->
                                                        if (newDay != workout.dayOfWeek) {
                                                            viewModel.moveWorkout(workout, newDay)
                                                        }
                                                    }
                                                    draggedWorkout = null
                                                    hoveredDay = null
                                                },
                                                onDragCancel = {
                                                    draggedWorkout = null
                                                    hoveredDay = null
                                                }
                                            )
                                        }
                                ) {
                                    WorkoutItem(
                                        workout = workout,
                                        onDelete = { viewModel.deleteWorkout(workout) },
                                        onClick = { onWorkoutClick(workout.id) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(Dimens.paddingSmall))
                            }
                        }
                    }
                }
            }

            draggedWorkout?.let { workout ->
                val ghostX = currentPointerPos.x - parentRootPosition.x - dragStartOffsetInItem.x
                val ghostY = currentPointerPos.y - parentRootPosition.y - dragStartOffsetInItem.y

                Box(
                    modifier = Modifier
                        .offset { IntOffset(ghostX.roundToInt(), ghostY.roundToInt()) }
                        .padding(horizontal = Dimens.paddingSmall)
                        .fillMaxWidth()
                        .alpha(0.8f)
                        .shadow(12.dp, RoundedCornerShape(Dimens.cornerRadius))
                ) {
                    WorkoutItem(
                        workout = workout,
                        onDelete = {},
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun WorkoutItem(
    workout: Workout,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(Dimens.cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.paddingMedium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = workout.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )
                Text(
                    text = stringResource(id = R.string.registered_workouts_exercise_count, workout.exercises.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(id = R.string.content_description_more_options),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.registered_workouts_remove_workout_menu)) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        },
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
        }
    }
}