package com.example.training_tracker.ui.screens.workout_details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.mocks.availableExercises
import com.example.training_tracker.ui.screens.home.MainGradientButton
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.Dimens
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailsScreen(
    onNavigateBack: () -> Unit,
    onStartWorkout: (String) -> Unit,
    viewModel: WorkoutDetailsViewModel = viewModel(factory = WorkoutDetailsViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddExerciseDialog by remember { mutableStateOf(false) }

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
                        "DETALHES DO TREINO",
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
        Box(modifier = Modifier
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
                        
                        Text(
                            text = workout.name.uppercase(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        
                        Text(
                            text = "${workout.exercises.size} exercícios no total",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(Dimens.paddingLarge))

                        Text(
                            text = "LISTA DE EXERCÍCIOS",
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
                                                        currentPointerPos = (itemBounds[exercise.id] ?: Offset.Zero) + offset
                                                    },
                                                    onDrag = { change, _ ->
                                                        change.consume()
                                                        val itemRootPos = itemBounds[exercise.id] ?: Offset.Zero
                                                        currentPointerPos = itemRootPos + change.position
                                                        
                                                        hoveredIndex = indexBounds.entries.find { 
                                                            it.value.contains(currentPointerPos) 
                                                        }?.key
                                                    },
                                                    onDragEnd = {
                                                        val fromIndex = workout.exercises.indexOfFirst { it.id == draggedExercise?.id }
                                                        if (fromIndex != -1 && hoveredIndex != null && hoveredIndex != fromIndex) {
                                                            viewModel.moveExercise(fromIndex, hoveredIndex!!)
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
                                    .then(if (isHovered && !isBeingDragged) Modifier.border(2.dp, CyanAccent, RoundedCornerShape(Dimens.cornerRadius)) else Modifier)
                            ) {
                                ExerciseDetailItem(
                                    number = index + 1,
                                    exercise = exercise,
                                    isEditMode = uiState.isEditMode,
                                    onRemove = { viewModel.removeExercise(exercise.id) }
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
                                text = "INICIAR TREINO AGORA",
                                onClick = { onStartWorkout(workout.id) }
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
                        number = (uiState.workout?.exercises?.indexOfFirst { it.id == exercise.id } ?: 0) + 1,
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
                "ADICIONAR EXERCÍCIO",
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
    var expanded by remember { mutableStateOf(false) }

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
            
            Text(
                text = exercise.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            if (isEditMode) {
                Box {
                    IconButton(onClick = { expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opções",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Remover Exercício", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                onRemove()
                                expanded = false
                            }
                        )
                    }
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
        title = { Text("Selecionar Exercício", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar exercício...") },
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
                    modifier = Modifier.fillMaxWidth().weight(1f),
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
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                            Text(
                                text = exercise.name,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        }
    )
}
