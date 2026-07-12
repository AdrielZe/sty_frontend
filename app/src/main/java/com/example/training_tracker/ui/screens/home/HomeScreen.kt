package com.example.training_tracker.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.ui.theme.AppTheme
import com.example.training_tracker.ui.theme.GreenGradient
import com.example.training_tracker.ui.theme.Typography
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle as JTextStyle
import java.util.Locale
import kotlin.math.abs

// =============================================================================
// === Sty design tokens — theme-aware                                       ===
// =============================================================================
private object Sty {
    val BgStart: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF131319) else Color(0xFFFDFDFD)
    val BgEnd: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF0C0C11) else Color(0xFFF5F5FA)
    val Surface: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF181820) else Color(0xFFFFFFFF)
    val Surface2: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF1F1F29) else Color(0xFFF0F4F8)
    val Border: Color
        @Composable get() = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.06f)
    val BorderStrong: Color
        @Composable get() = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.14f) else Color.Black.copy(alpha = 0.12f)
    val TextMain: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFFECECF3) else Color(0xFF1A1A2E)
    val TextDim: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF9A9AAA) else Color(0xFF55556A)
    val TextFaint: Color
        @Composable get() = if (isSystemInDarkTheme()) Color(0xFF61616F) else Color(0xFF9898AA)
    val OnAccent: Color = Color(0xFF08131E)
    val GreenAccent: Color = Color(0xFF5AD479)
    val GoldAccent: Color = Color(0xFFFFC46B)

    val BgBrush: Brush
        @Composable get() = Brush.verticalGradient(listOf(BgStart, BgEnd))
}

// =============================================================================
// === Public entry points                                                   ===
// =============================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onClickWorkoutCard: (String?) -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    onNavigateToRegisteredWorkouts: () -> Unit,
    onNavigateToWorkoutsHistory: () -> Unit,
    onClickBrowseWorkouts: () -> Unit,
    onClickGoToLogin: () -> Unit,
    onNavigateToFreestyleWorkout: () -> Unit = {},
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    when (homeUiState) {
        is HomeUiState.Loading -> HomeLoadingScreen()
        is HomeUiState.Error -> HomeErrorScreen(message = homeUiState.message)
        is HomeUiState.Success -> HomeContent(
            modifier = modifier,
            onClickWorkoutCard = onClickWorkoutCard,
            onNavigateToCreateWorkout = onNavigateToCreateWorkout,
            onClickBrowseWorkouts = onClickBrowseWorkouts,
            onNavigateToFreestyleWorkout = onNavigateToFreestyleWorkout,
            onClickGoToLogin = onClickGoToLogin,
            homeUiState = homeUiState,
            homeViewModel = homeViewModel,
        )
    }
}

@Composable
fun HomeLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Sty.BgBrush),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AppTheme.accent.light)
    }
}

@Composable
fun HomeErrorScreen(message: String?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Sty.BgBrush),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message ?: stringResource(R.string.error),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

// =============================================================================
// === Main scrollable content                                               ===
// =============================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    onClickWorkoutCard: (String?) -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    onClickBrowseWorkouts: () -> Unit,
    onNavigateToFreestyleWorkout: () -> Unit,
    homeUiState: HomeUiState.Success,
    homeViewModel: HomeViewModel,
    onClickGoToLogin: () -> Unit
) {
    var isProfileExpanded by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showFreestyleNameDialog by remember { mutableStateOf(false) }

    println("INITALIZED WITH USER ID: ${homeUiState.user?.id}")


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CustomTopBar(
                onProfileClick = { isProfileExpanded = true },
                user = homeUiState.user
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateWorkout,
                containerColor = AppTheme.accent.light,
                contentColor = Sty.OnAccent,
                shape = CircleShape,
                modifier = Modifier
                    .size(58.dp)
                    .shadow(elevation = 12.dp, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.home_create_new_workout_button),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Sty.BgBrush)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            // === Greeting ============================================================
            GreetingBlock(
                name = homeUiState.user?.name ?: stringResource(R.string.home_default_user_name),
                dateLine = homeUiState.currentDate,
                onLoginClick = onClickGoToLogin
            )

            // === Week strip ==========================================================
            WeekStrip(
                completedThisWeek = homeUiState.workoutsCompletedThisWeek,
                workoutsPerDayOfWeek = homeUiState.workoutsPerDayOfWeek,
                completedWorkoutsPerDayOfWeek = homeUiState.completedWorkoutsPerDayOfWeek,
            )

            // === Today's workout — hero ============================================
            SectionHeader(
                title = stringResource(id = R.string.home_scheduled_workout_today),
                actionLabel = stringResource(R.string.home_browse_workouts).trim(),
                onActionClick = onClickBrowseWorkouts
            )

            if (homeUiState.todayWorkouts.isNotEmpty()) {
                homeUiState.todayWorkouts.forEach { workout ->
                    WorkoutCard(
                        workout = workout,
                        onClick = { onClickWorkoutCard(workout.id) }
                    )
                }
            } else {
                EmptyWorkoutCard()
            }

            // === Quick stats tiles ==================================================
            if (homeUiState.weeklyCalories > 0 || homeUiState.totalWorkoutsCompleted > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.calorias_semana),
                        value = homeUiState.weeklyCalories.toString(),
                        unit = "kcal",
                        accent = true,
                        icon = { Icon(Icons.Default.LocalFireDepartment, null, modifier = Modifier.size(16.dp)) },
                    )
                    StatTile(
                        modifier = Modifier.weight(1f),
                        label = stringResource(R.string.treinos_totais),
                        value = homeUiState.totalWorkoutsCompleted.toString(),
                        unit = null,
                        accent = false,
                        icon = { Icon(Icons.Default.FitnessCenter, null, modifier = Modifier.size(16.dp), tint = Sty.GoldAccent) },
                        iconBg = Sty.GoldAccent.copy(alpha = 0.12f),
                    )
                }
            }

            // === Freestyle ==========================================================
            SectionHeader(title = stringResource(R.string.treino_livre))
            FreestyleWorkoutCard(
                activeWorkout = homeUiState.activeFreestyleWorkout,
                onClick = { showFreestyleNameDialog = true },
                viewModel = homeViewModel
            )

            // === Weekly goal ========================================================
            SectionHeader(title = stringResource(R.string.meta_semanal))
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

            Spacer(modifier = Modifier.height(100.dp))
        }
    }

    if (isProfileExpanded) {
        Dialog(onDismissRequest = { isProfileExpanded = false }) {
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(4.dp, AppTheme.accent.gradient), CircleShape)
                    .clickable { isProfileExpanded = false },
                contentAlignment = Alignment.Center
            ) {
                if (!homeUiState.user?.profilePicture.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(homeUiState.user?.profilePicture)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = stringResource(R.string.foto_de_perfil_expandida),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.gym),
                        placeholder = painterResource(id = R.drawable.gym)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.gym),
                        contentDescription = stringResource(R.string.foto_de_perfil_padrao),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
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

    if (showFreestyleNameDialog) {
        FreestyleWorkoutNameDialog(
            onDismiss = { showFreestyleNameDialog = false },
            onConfirm = { name ->
                homeViewModel.startFreestyleWorkout(name) { onNavigateToFreestyleWorkout() }
                showFreestyleNameDialog = false
            }
        )
    }

}

// =============================================================================
// === Top bar — STY logo + gradient avatar                                  ===
// =============================================================================
@Composable
fun CustomTopBar(
    onProfileClick: () -> Unit,
    user: User?
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.example.training_tracker.ui.components.StyLogo(
            titleSize = 22.sp,
            layout = com.example.training_tracker.ui.components.StyLogoLayout.VERTICAL
        )

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(AppTheme.accent.gradient)
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), CircleShape)
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!user?.profilePicture.isNullOrEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user?.profilePicture)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = stringResource(id = R.string.content_description_profile),
                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = (user?.name?.firstOrNull()?.uppercase() ?: "A"),
                    color = Sty.OnAccent,
                    fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// =============================================================================
// === Greeting                                                              ===
// =============================================================================
@Composable
private fun GreetingBlock(name: String, dateLine: String?, onLoginClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)) {
        if (!dateLine.isNullOrBlank()) {
            Text(
                text = dateLine.uppercase(Locale.getDefault()),
                color = Sty.TextDim,
                fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = buildAnnotatedString {
                append("Olá, ")
                withStyle(SpanStyle(color = AppTheme.accent.light)) { append(name) }
                append(".")
            },
            color = Sty.TextMain,
            fontFamily = com.example.training_tracker.ui.theme.Montserrat,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            letterSpacing = (-0.5).sp,
            lineHeight = 33.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = buildAnnotatedString {
                append("Já tem conta?")
                withStyle(SpanStyle(color = AppTheme.accent.light)) { append("Faça login!") }
                append(".")
            },
            modifier = Modifier
                .padding(top = 4.dp)
                .clickable { onLoginClick() },
            color = Sty.TextMain,
            fontFamily = com.example.training_tracker.ui.theme.Montserrat,
            fontWeight = FontWeight.Black,
            fontSize = 30.sp,
            letterSpacing = (-0.5).sp,
            lineHeight = 33.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// =============================================================================
// === Week strip                                                            ===
// =============================================================================
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeekStrip(
    completedThisWeek: Int,
    workoutsPerDayOfWeek: Map<DayOfWeek, Int>,
    completedWorkoutsPerDayOfWeek: Map<DayOfWeek, Int>,
) {
    val today = remember { LocalDate.now() }
    val monday = remember(today) { today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong()) }
    val days = remember(today, completedWorkoutsPerDayOfWeek) {
        (0..6).map { i ->
            val date = monday.plusDays(i.toLong())
            val initial = date.dayOfWeek
                .getDisplayName(JTextStyle.NARROW, Locale("pt", "BR"))
                .uppercase(Locale("pt", "BR"))
            val completedOnDay = completedWorkoutsPerDayOfWeek[date.dayOfWeek] ?: 0
            val status = when {
                date.isEqual(today) -> WeekStatus.TODAY
                date.isBefore(today) && completedOnDay > 0 -> WeekStatus.DONE
                date.isBefore(today) -> WeekStatus.REST
                else -> WeekStatus.PLANNED
            }
            Triple(initial, status, date.dayOfWeek)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            days.forEach { (label, status, dayOfWeek) ->
                val scheduled = workoutsPerDayOfWeek[dayOfWeek] ?: 0
                val completed = completedWorkoutsPerDayOfWeek[dayOfWeek] ?: 0
                WeekDayCell(
                    modifier = Modifier.weight(1f),
                    label = label,
                    status = status,
                    workoutCount = scheduled,
                    allCompleted = scheduled > 0 && completed >= scheduled,
                )
            }
        }
    }
}

private enum class WeekStatus { DONE, TODAY, PLANNED, REST }

@Composable
private fun WeekDayCell(
    modifier: Modifier,
    label: String,
    status: WeekStatus,
    workoutCount: Int,
    allCompleted: Boolean,
) {
    val isToday = status == WeekStatus.TODAY
    val bg: Brush = if (isToday) AppTheme.accent.gradient else Brush.linearGradient(listOf(Sty.Surface, Sty.Surface))
    val borderColor = if (isToday) Color.Transparent else Sty.Border

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(bg)
                .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(14.dp))
                .padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = label,
                fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                letterSpacing = 0.6.sp,
                color = if (isToday) Sty.OnAccent.copy(alpha = 0.85f) else Sty.TextDim
            )
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .then(
                        when (status) {
                            WeekStatus.DONE -> Modifier.background(AppTheme.accent.light)
                            WeekStatus.TODAY -> Modifier.background(Sty.OnAccent.copy(alpha = 0.85f))
                            WeekStatus.PLANNED -> Modifier.border(1.dp, Sty.BorderStrong, CircleShape)
                            WeekStatus.REST -> Modifier.background(Sty.Surface2)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (workoutCount > 0) {
                    Text(
                        text = workoutCount.toString(),
                        fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp,
                        lineHeight = 9.sp,
                        color = when (status) {
                            WeekStatus.DONE -> Sty.OnAccent
                            WeekStatus.TODAY -> AppTheme.accent.light
                            else -> Sty.TextDim
                        },
                        style = LocalTextStyle.current.copy(
                            platformStyle = PlatformTextStyle(includeFontPadding = false)
                        )
                    )
                }
            }
        }

        if (allCompleted) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.TopCenter)
                    .clip(CircleShape)
                    .background(Sty.GreenAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Check, null,
                    tint = Sty.OnAccent,
                    modifier = Modifier.size(9.dp)
                )
            }
        }
    }
}

// =============================================================================
// === Section header                                                        ===
// =============================================================================
@Composable
fun HomeSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) = SectionHeader(title, actionLabel, onActionClick, modifier)

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title.uppercase(Locale.getDefault()),
            fontFamily = com.example.training_tracker.ui.theme.Montserrat,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 2.sp,
            color = Sty.TextDim
        )
        if (actionLabel != null && onActionClick != null) {
            Row(
                modifier = Modifier.clickable { onActionClick() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = AppTheme.accent.light
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = AppTheme.accent.light,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

// =============================================================================
// === Hero workout card                                                     ===
// =============================================================================
@Composable
fun WorkoutCard(modifier: Modifier = Modifier, workout: Workout?, onClick: () -> Unit) {
    val isCompleted = workout?.isCompleted == true
    val muscleGroups = remember(workout) {
        workout?.exercises?.mapNotNull { it.muscleGroup }?.distinct() ?: emptyList()
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Sty.Surface)
            .border(BorderStroke(1.dp, Sty.Border), RoundedCornerShape(26.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(getWorkoutImageRes(workout))
                .build(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = if (isCompleted) 0.25f else 0.85f
        )
        // Scrim
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.35f to Color.Black.copy(alpha = 0.10f),
                        1f to Color.Black.copy(alpha = 0.78f)
                    )
                )
        )

        // Content (chips + title + meta)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // chip row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                userScrollEnabled = true
            ) {
                item {
                    StyChip(
                        text = if (isCompleted) stringResource(R.string.completed) else "HOJE",
                        accent = !isCompleted,
                        accentBrush = if (isCompleted) GreenGradient else AppTheme.accent.gradient
                    )
                }
                items(muscleGroups) { mg ->
                    StyChip(text = stringResource(id = mg.resId).uppercase(Locale.getDefault()))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = workout?.name ?: stringResource(R.string.home_default_workout_name),
                color = Color.White,
                fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = (-0.25).sp,
                lineHeight = 26.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 64.dp)
            )
            // Inline progress bar if in-progress
            if (workout?.isOnGoing == true && !isCompleted) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(workout.progress)
                            .fillMaxHeight()
                            .background(AppTheme.accent.gradient)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            // meta row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FitnessCenter, null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = stringResource(
                            id = R.string.home_workout_exercise_count,
                            workout?.exercises?.size ?: 0
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.5.sp,
                        letterSpacing = 0.6.sp,
                        maxLines = 1
                    )
                }
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.5f))
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Timer, null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "${workout?.estimatedTime ?: 0} min",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.5.sp,
                        letterSpacing = 0.6.sp,
                        maxLines = 1
                    )
                }
            }
        }

        // Play CTA — bottom right
        if (!isCompleted) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(50.dp)
                    .shadow(elevation = 14.dp, shape = CircleShape, spotColor = AppTheme.accent.light)
                    .clip(CircleShape)
                    .background(AppTheme.accent.light)
                    .clickable { onClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow, null,
                    tint = Sty.OnAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun StyChip(
    text: String,
    accent: Boolean = false,
    accentBrush: Brush = AppTheme.accent.gradient
) {
    val shape = RoundedCornerShape(100.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .then(
                if (accent) Modifier.background(accentBrush)
                else Modifier
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.18f)), shape)
            )
            .padding(horizontal = 9.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = if (accent) Sty.OnAccent else Color.White,
            fontFamily = com.example.training_tracker.ui.theme.Montserrat,
            fontWeight = FontWeight.Black,
            fontSize = 10.sp,
            letterSpacing = 1.2.sp
        )
    }
}

// =============================================================================
// === Empty workout card (rest day)                                         ===
// =============================================================================
@Composable
fun EmptyWorkoutCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Sty.Surface)
            .border(BorderStroke(1.dp, Sty.Border), RoundedCornerShape(26.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.relax_no_workout)
                .build(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.25f
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            StyChip(text = "DESCANSO", accent = false)
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(id = R.string.home_no_workout_scheduled_today),
                color = Color.White,
                fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                fontWeight = FontWeight.Black,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.25).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.dica_faca_um_leve_agachamento_ou_uma_caminhada_de_15_minutos),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

// =============================================================================
// === Stat tiles                                                            ===
// =============================================================================
@Composable
private fun StatTile(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String?,
    accent: Boolean,
    icon: @Composable () -> Unit,
    iconBg: Color = AppTheme.accent.light.copy(alpha = 0.12f),
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Sty.Surface)
            .border(BorderStroke(1.dp, Sty.Border), RoundedCornerShape(20.dp))
            .padding(14.dp)
            .defaultMinSize(minHeight = 96.dp)
    ) {
        if (accent) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(AppTheme.accent.gradient)
            )
        }
        // floater icon top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(28.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides AppTheme.accent.light
            ) { icon() }
        }
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = label.uppercase(Locale.getDefault()),
                color = Sty.TextDim,
                fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 10.5.sp,
                letterSpacing = 1.4.sp
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    color = Sty.TextMain,
                    fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                    fontWeight = FontWeight.Black,
                    fontSize = 28.sp,
                    letterSpacing = (-0.5).sp,
                    lineHeight = 28.sp
                )
                if (!unit.isNullOrBlank()) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = unit,
                        color = Sty.TextDim,
                        fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

// =============================================================================
// === Freestyle row                                                         ===
// =============================================================================
@Composable
fun FreestyleWorkoutCard(
    activeWorkout: Workout? = null,
    viewModel: HomeViewModel,
    onClick: () -> Unit
) {
    val isLocked = activeWorkout != null
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            containerColor = Sty.Surface,
            title = {
                Text(
                    text = stringResource(R.string.cancelar_treino),
                    style = Typography.titleMedium,
                    color = Sty.TextMain
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.tem_certeza_que_deseja_cancelar_e_remover_o_treino,
                        activeWorkout?.name ?: "this"
                    ),
                    style = Typography.bodyLarge,
                    color = Sty.TextDim
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeFreestyleWorkout()
                    showCancelDialog = false
                }) {
                    Text(
                        text = stringResource(R.string.confirmar),
                        color = AppTheme.accent.light,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(text = stringResource(R.string.cancelar), color = Sty.TextDim)
                }
            }
        )
    }

    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(Sty.Surface)
            .border(BorderStroke(1.dp, if (isLocked) Sty.Border else Sty.BorderStrong), shape)
            .clickable(enabled = !isLocked) { onClick() }
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isLocked) Color.White.copy(alpha = 0.05f)
                    else AppTheme.accent.light.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Bolt,
                contentDescription = null,
                tint = if (isLocked) Sty.TextFaint else AppTheme.accent.light,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.treino_livre).uppercase(Locale.getDefault()),
                fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp,
                color = if (isLocked) Sty.TextFaint else AppTheme.accent.light
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isLocked)
                    stringResource(
                        R.string.treino_livre_em_andamento_finalize_ou_cancele_esse_treino_antes_de_iniciar_outro,
                        activeWorkout.name
                    )
                else
                    stringResource(R.string.nao_planejou_seu_treino_inicie_um_treino_livre_e_adicione_os_exercicios_na_hora),
                fontSize = 11.5.sp,
                lineHeight = 15.sp,
                color = Sty.TextDim
            )
            if (isLocked) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.cancelar_treino),
                    fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = AppTheme.accent.light,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { showCancelDialog = true }
                        .padding(vertical = 2.dp)
                )
            }
        }
        if (!isLocked) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                null,
                tint = AppTheme.accent.light.copy(alpha = 0.8f)
            )
        }
    }
}

// =============================================================================
// === Weekly goal card                                                      ===
// =============================================================================
@Composable
fun WeeklyProgressCard(
    currentWorkouts: Int,
    goalWorkouts: Int,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (goalWorkouts > 0) {
        (currentWorkouts.toFloat() / goalWorkouts.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(Sty.Surface)
            .border(BorderStroke(1.dp, Sty.Border), shape)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.consistencia_e_a_chave),
                        color = Sty.TextMain,
                        fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = (-0.2).sp,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.editar_meta),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onEditGoal() },
                        tint = Sty.TextFaint
                    )
                }
                Spacer(Modifier.height(2.dp))
                val remaining = (goalWorkouts - currentWorkouts).coerceAtLeast(0)
                Text(
                    text = if (remaining > 0)
                        "$remaining ${if (remaining == 1) stringResource(R.string.treino) else stringResource(R.string.treinos)} restantes para bater a meta"
                    else
                        stringResource(R.string.objetivo_atingido),
                    color = Sty.TextDim,
                    fontSize = 12.sp,
                    lineHeight = 15.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(AppTheme.accent.light.copy(alpha = 0.10f))
                    .border(BorderStroke(1.dp, AppTheme.accent.light.copy(alpha = 0.30f)), RoundedCornerShape(100.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "$currentWorkouts / $goalWorkouts",
                    fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 0.4.sp,
                    color = AppTheme.accent.light
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape)
                .background(Sty.Surface2)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(AppTheme.accent.gradient)
            )
        }

        Spacer(Modifier.height(14.dp))

        // pill dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until goalWorkouts) {
                val filled = i < currentWorkouts
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (filled) Modifier.background(AppTheme.accent.gradient)
                            else Modifier.background(Sty.Surface2)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (filled) {
                        Icon(
                            Icons.Default.Check, null,
                            tint = Sty.OnAccent,
                            modifier = Modifier.size(13.dp)
                        )
                    } else {
                        Text(
                            text = "${i + 1}",
                            color = Sty.TextFaint,
                            fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SetWeeklyGoalCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(shape)
            .background(Sty.Surface)
            .border(BorderStroke(1.dp, Sty.Border), shape)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(AppTheme.accent.light.copy(alpha = 0.12f))
                .border(BorderStroke(1.dp, AppTheme.accent.light.copy(alpha = 0.3f)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, null, tint = AppTheme.accent.light, modifier = Modifier.size(22.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.defina_sua_meta_semanal),
                color = Sty.TextMain,
                fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                letterSpacing = (-0.2).sp
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.quantos_treinos_voce_fara_esta_semana),
                color = Sty.TextDim,
                fontSize = 12.sp,
                lineHeight = 15.sp
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = AppTheme.accent.light
        )
    }
}

// =============================================================================
// === Weekly goal picker (kept functional, restyled)                        ===
// =============================================================================
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

    LaunchedEffect(Unit) { listState.scrollToItem(currentGoal - 1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Sty.Surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.meta_semanal).uppercase(Locale.getDefault()),
                    fontFamily = com.example.training_tracker.ui.theme.Montserrat,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    letterSpacing = 2.sp,
                    color = AppTheme.accent.light
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.escolha_seu_objetivo_de_treinos_por_semana),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = Sty.TextDim
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        color = AppTheme.accent.light.copy(alpha = 0.10f),
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
                                modifier = Modifier.height(50.dp).fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$num ${if (num == 1) stringResource(R.string.treino) else stringResource(R.string.treinos)}",
                                    style = if (isSelected) {
                                        MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.accent.light
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyLarge.copy(
                                            color = Sty.TextFaint
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(R.string.cancelar),
                        modifier = Modifier.clickable { onDismiss() }.padding(12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Sty.TextDim
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedGoal) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent.light),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONFIRMAR", color = Sty.OnAccent, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FreestyleWorkoutNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(1.dp, AppTheme.accent.light.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Sty.Surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AppTheme.accent.light.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.nome_do_treino),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Sty.TextMain
                )
                Text(
                    text = stringResource(R.string.como_quer_chamar_o_seu_treino),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Sty.TextDim,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.ex_treino_de_sexta),
                            color = Sty.TextFaint
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, color = Sty.TextMain),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accent.light,
                        unfocusedBorderColor = Sty.BorderStrong,
                        cursorColor = AppTheme.accent.light,
                        focusedContainerColor = AppTheme.accent.light.copy(alpha = 0.02f),
                        focusedTextColor = Sty.TextMain,
                        unfocusedTextColor = Sty.TextMain
                    )
                )
                Spacer(Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancelar),
                            color = Sty.TextDim,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = { if (workoutName.isNotBlank()) onConfirm(workoutName) },
                        enabled = workoutName.isNotBlank(),
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.accent.light,
                            contentColor = Sty.OnAccent,
                            disabledContainerColor = AppTheme.accent.light.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.iniciar),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
// === Workout image resolver (unchanged contract — used by other screens)   ===
// =============================================================================
fun getWorkoutImageRes(workout: Workout?): Int {
    if (workout == null || workout.exercises.isEmpty()) return R.drawable.workout
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
        MuscleGroups.CARDIO -> R.drawable.cardio_workout
        MuscleGroups.STRETCHING -> R.drawable.stretching_workout
        else -> R.drawable.biceps_workout
    }
}

// =============================================================================
// === Used by RegisteredWorkoutsScreen / WorkoutHistoryScreen — preserved   ===
// =============================================================================
@Composable
fun MuscleBadge(muscle: MuscleGroups) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = muscle.resId),
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = 18.sp
                )
            )
        }
    }
}

// Used by WorkoutEditScreen — kept identical to previous shape.
@Composable
fun MainGradientButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = AppTheme.accent.light.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(brush = AppTheme.accent.gradient)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Add, null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}

// =============================================================================
// Kept for back-compat — no longer referenced from HomeContent, but still
// callable in case other screens link to them.
// =============================================================================
@Composable
fun MomentumCard(count: Int) {
    val isSystemDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSystemDark) Sty.Surface else Color(0xFF262525))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(R.drawable.gym).build(),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.CenterStart)
        ) {
            Text(
                stringResource(R.string.mantenha_o_foco),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = AppTheme.accent.light,
                    lineHeight = 24.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.treinos_totais),
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AppTheme.accent.light,
                )
            }
        }
    }
}

@Composable
fun EmptyMomentumCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Sty.Surface),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(R.drawable.start_today).build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )
        Column(modifier = Modifier.padding(24.dp).align(Alignment.CenterStart)) {
            Text(
                stringResource(R.string.comece_hoje),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = AppTheme.accent.light,
                    lineHeight = 24.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.voce_nao_completou_nenhum_treino_ainda),
                fontSize = 14.sp,
                color = Color.White
            )
            Text(
                stringResource(R.string.comece_hoje_para_acompanhar_seu_progresso),
                fontSize = 14.sp,
                color = Color.White
            )
        }
    }
}

@Composable
fun WeeklyCaloriesCard(calories: Int) {
    val fireColor = Color(0xFFFF5722)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Sty.Surface),
        border = BorderStroke(1.dp, fireColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(fireColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment, null,
                        tint = fireColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.calorias_semana),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Sty.TextDim,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.calorias_queimadas, calories),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = fireColor
                        )
                    )
                }
            }
        }
    }
}
