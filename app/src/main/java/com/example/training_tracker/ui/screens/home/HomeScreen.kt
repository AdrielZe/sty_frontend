package com.example.training_tracker.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import com.example.training_tracker.ui.theme.GreenGradient
import com.example.training_tracker.ui.theme.Typography
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onClickWorkoutCard: (String?) -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    onNavigateToRegisteredWorkouts: () -> Unit,
    onNavigateToWorkoutsHistory: () -> Unit,
    onClickBrowseWorkouts: () -> Unit,
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    when (homeUiState) {
        is HomeUiState.Loading -> {
            HomeLoadingScreen()
        }
        is HomeUiState.Error -> {
            HomeErrorScreen(message = homeUiState.message)
        }
        is HomeUiState.Success -> {
            HomeContent(
                modifier = modifier,
                onClickWorkoutCard = onClickWorkoutCard,
                onNavigateToCreateWorkout = onNavigateToCreateWorkout,
                onNavigateToRegisteredWorkouts = onNavigateToRegisteredWorkouts,
                onNavigateToWorkoutsHistory = onNavigateToWorkoutsHistory,
                onClickBrowseWorkouts = onClickBrowseWorkouts,
                homeUiState = homeUiState,
                homeViewModel = homeViewModel
            )
        }
    }
}

@Composable
fun HomeLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = CyanAccent)
    }
}

@Composable
fun HomeErrorScreen(message: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message ?: "Ocorreu um erro inesperado",
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    onClickWorkoutCard: (String?) -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    onNavigateToRegisteredWorkouts: () -> Unit,
    onNavigateToWorkoutsHistory: () -> Unit,
    onClickBrowseWorkouts: () -> Unit,
    homeUiState: HomeUiState.Success,
    homeViewModel: HomeViewModel
) {
    var isProfileExpanded by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                onMenuClick = { /* Abrir Drawer */ },
                onProfileClick = { isProfileExpanded = true }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateWorkout,
                containerColor = CyanAccent,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.home_create_new_workout_button),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(horizontal = 24.dp)
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(
                        id = R.string.home_greeting,
                        homeUiState.user?.name
                            ?: stringResource(id = R.string.home_default_user_name)
                    ),
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                homeUiState.currentDate?.let { date ->
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.home_scheduled_workout_today),
                style = Typography.titleMedium.copy(
                    brush = CyanGradient,
                    fontWeight = FontWeight.Bold
                ),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (homeUiState.todayWorkouts.isNotEmpty()) {
                homeUiState.todayWorkouts.forEach { workout ->
                    WorkoutCard(
                        workout = workout,
                        onClick = {
                            onClickWorkoutCard(workout.id)
                        }
                    )
                }
            } else {
                EmptyWorkoutCard()
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                modifier = Modifier
                    .clickable {
                        onClickBrowseWorkouts()
                    }
                    .fillMaxWidth(),
                text = stringResource(R.string.home_browse_workouts),
                textAlign = TextAlign.Center,
                textDecoration = TextDecoration.Underline,
                style = Typography.labelLarge
            )


            Spacer(modifier = Modifier.height(32.dp))

            if (homeUiState.totalWorkoutsCompleted > 0) MomentumCard(count = homeUiState.totalWorkoutsCompleted) else EmptyMomentumCard()

            Spacer(modifier = Modifier.height(16.dp))

            val weeklyGoal = homeUiState.user?.weeklyGoal
            if (weeklyGoal != null && weeklyGoal > 0) {
                WeeklyProgressCard(
                    currentWorkouts = homeUiState.workoutsCompletedThisWeek,
                    goalWorkouts = weeklyGoal,
                    onEditGoal = { showGoalDialog = true }
                )
            } else {
                SetWeeklyGoalCard(onClick = { showGoalDialog = true })
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (isProfileExpanded) {
        Dialog(onDismissRequest = { isProfileExpanded = false }) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(4.dp, CyanGradient), CircleShape)
                    .clickable { isProfileExpanded = false },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.6f),
                    tint = CyanAccent
                )
            }
        }
    }

    if (showGoalDialog) {
        WeeklyGoalPickerDialog(
            currentGoal = homeUiState.user?.weeklyGoal ?: 3,
            onDismiss = { showGoalDialog = false },
            onConfirm = { goal ->
                homeViewModel.updateWeeklyGoal(goal)
                showGoalDialog = false
            }
        )
    }
}

private fun getWorkoutImageRes(workout: Workout?): Int {
    if (workout == null || workout.exercises.isEmpty()) return R.drawable.workout

    val mostFrequentMuscleGroup = workout.exercises
        .mapNotNull { it.muscleGroup }
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }?.key

    return when (mostFrequentMuscleGroup) {
        MuscleGroups.CHEST -> R.drawable.chest_workout
        MuscleGroups.BACK -> R.drawable.back_workout
        MuscleGroups.LEGS -> R.drawable.leg_workout
        MuscleGroups.SHOULDERS -> R.drawable.shoulder_workout
        MuscleGroups.BICEPS -> R.drawable.biceps_workout
        MuscleGroups.TRICEPS -> R.drawable.triceps_workout
        MuscleGroups.ABS -> R.drawable.abs_workout
        else -> R.drawable.biceps_workout
    }
}

@Composable
fun SetWeeklyGoalCard(onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.strong_3k)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isDark) 0.2f else 0.4f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = CyanAccent.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Defina sua meta semanal",
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Quantos treinos você fará esta semana?",
                        style = Typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyGoalPickerDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val items = (1..7).toList()
    val listState = rememberLazyListState()

    val selectedGoal by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) currentGoal
            else {
                val viewportCenter =
                    (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
                visibleItemsInfo.minByOrNull { item ->
                    val itemCenter = item.offset + (item.size / 2f)
                    abs(itemCenter - viewportCenter)
                }?.index?.plus(1) ?: currentGoal
            }
        }
    }

    LaunchedEffect(Unit) {
        listState.scrollToItem(currentGoal - 1)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Meta Semanal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = CyanAccent
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Escolha seu objetivo de treinos por semana",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        color = CyanAccent.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {}

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 65.dp)
                    ) {
                        items(items) { num ->
                            val isSelected = num == selectedGoal
                            Box(
                                modifier = Modifier
                                    .height(50.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$num ${if (num == 1) "treino" else "treinos"}",
                                    style = if (isSelected) {
                                        MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = CyanAccent
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "CANCELAR",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedGoal) },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONFIRMAR", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressCard(
    currentWorkouts: Int,
    goalWorkouts: Int,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val progress = if (goalWorkouts > 0) {
        (currentWorkouts.toFloat() / goalWorkouts.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.count_backround)
                    .crossfade(true)
                    .build(),
                contentDescription = "Fundo do card",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Weekly Progress",
                                color = Color.White,
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar meta",
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onEditGoal() },
                                tint = CyanAccent
                            )
                        }
                        Text(
                            text = "Consistency is key",
                            color = Color.LightGray,
                            style = Typography.labelSmall
                        )
                    }

                    Surface(
                        color = CyanAccent.copy(alpha = 0.1f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "$currentWorkouts / $goalWorkouts",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = CyanAccent,
                            style = Typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress)
                                .fillMaxHeight()
                                .background(CyanGradient)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (progress >= 1f) "Goal Reached! 🔥" else "${(progress * 100).toInt()}% completed",
                        color = if (progress >= 1f) Color.White else Color.LightGray,
                        style = Typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}



@Composable
fun CustomTopBar(
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = stringResource(id = R.string.home_app_title),
            style = TextStyle(
                brush = CyanGradient
            ),
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
                2.dp,
                CyanAccent.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onProfileClick() }
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(id = R.string.content_description_profile),
                tint = CyanAccent,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun MainGradientButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = CyanAccent.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = CyanGradient
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun WorkoutCard(modifier: Modifier = Modifier, workout: Workout?, onClick: () -> Unit) {
    val isCompleted = workout?.isCompleted == true
    val isDark = isSystemInDarkTheme()

    val muscleGroups = remember(workout) {
        workout?.exercises?.mapNotNull { it.muscleGroup }?.distinct() ?: emptyList()
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0XFF0D0D0D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getWorkoutImageRes(workout))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isCompleted) 0.2f else if (isDark) 0.5f else 0.7f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (isCompleted) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(
                                    alpha = if (isDark) 0.8f else 0.9f
                                )
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = if (isCompleted) GreenGradient else CyanGradient,
                                alpha = 0.9f
                            )
                    ) {
                        Text(
                            text = if (isCompleted) "COMPLETED" else "TODAY",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = workout?.name ?: stringResource(id = R.string.home_default_workout_name),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (workout?.isOnGoing == true && !isCompleted) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(workout.progress)
                                        .fillMaxHeight()
                                        .background(CyanGradient)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(workout.progress * 100).toInt()}% completed",
                                color = Color.White.copy(alpha = 0.7f),
                                style = Typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.home_workout_exercise_count, workout?.exercises?.size ?: 0),
                            color = Color.White.copy(alpha = 0.7f),
                            style = Typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${workout?.estimatedTime ?: 0} min",
                            color = Color.White.copy(alpha = 0.7f),
                            style = Typography.bodySmall
                        )
                    }

                    if (muscleGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(muscleGroups) { muscle ->
                                MuscleBadge(muscle = muscle)
                            }
                        }
                    }
                }
            }

            if (!isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleBadge(muscle: MuscleGroups) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Text(
            text = muscle.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color.White,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun EmptyWorkoutCard(modifier: Modifier = Modifier) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.relax_no_workout)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isDark) 0.3f else 0.55f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isDark) 0.7f else 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(id = R.string.home_no_workout_scheduled_today),
                    color = Color.White,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessibilityNew,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Dica: faça um leve agachamento ou uma caminhada de 15 minutos",
                        color = Color.White,
                        style = Typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MomentumCard(count: Int) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDark) MaterialTheme.colorScheme.surface else Color(0XFF262525))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.gym)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        Column(modifier = Modifier
            .padding(24.dp)
            .align(Alignment.CenterStart)) {
            Text(
                "KEEP THE\nMOMENTUM.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = CyanAccent,
                    lineHeight = 24.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Row (
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(
                    text = "Total Workouts",
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$count",
                        style = Typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanAccent,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyMomentumCard() {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isDark) MaterialTheme.colorScheme.surface else Color.Black),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.start_today)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        Column(modifier = Modifier
            .padding(24.dp)
            .align(Alignment.CenterStart)) {
            Text(
                "START\nTODAY.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = CyanAccent,
                    lineHeight = 24.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "You haven't completed any workout yet.",
                fontSize = 14.sp,
                color = if (isDark) MaterialTheme.colorScheme.onSurface else Color.White
            )
            Text(
                "Start today to check your progress.",
                fontSize = 14.sp,
                color = if (isDark) MaterialTheme.colorScheme.onSurface else Color.White
            )
        }
    }
}
