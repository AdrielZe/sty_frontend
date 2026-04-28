package com.example.training_tracker.ui.screens.workout_history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.ui.screens.home.MuscleBadge
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.theme.CyanAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    uiState: WorkoutHistoryUiState,
    onDateSelected: (LocalDate?) -> Unit,
    onMoveMonth: (Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onNavigateBack: () -> Unit,
    onClickHistory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "HISTORY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = CyanAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = CyanAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            item {
                HistoryCalendarCard(
                    savedWorkouts = uiState.savedWorkouts,
                    currentMonthDate = uiState.currentCalendarMonth,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = onDateSelected,
                    onMoveMonth = onMoveMonth
                )
            }

            item {
                HistoryFilterRow(uiState.sortOrder, onSortOrderChange)
            }

            item {
                Text(
                    "RECENT ACTIVITIES",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextGray,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            items(uiState.savedWorkouts, key = { it.id }) { workout ->
                HistoryWorkoutCard(
                    workout = workout,
                    onClick = { onClickHistory(workout.id) }
                )
            }
        }
    }
}

@Composable
fun HistoryCalendarCard(
    savedWorkouts: List<WorkoutHistory>,
    currentMonthDate: LocalDate,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    onMoveMonth: (Long) -> Unit
) {
    val monthTitle = currentMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH))
    val daysInMonth = currentMonthDate.lengthOfMonth()
    val firstDayOfMonth = currentMonthDate.withDayOfMonth(1).dayOfWeek.value % 7

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(monthTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp)
                Row {
                    IconButton(onClick = { onMoveMonth(-1) }) { Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurface) }
                    IconButton(onClick = { onMoveMonth(1) }) { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface) }
                }
            }

            Spacer(Modifier.height(16.dp))
            
            val days = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                days.forEach { day ->
                    Text(day, color = TextGray, fontSize = 12.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            var currentDay = 1
            for (week in 0..5) {
                if (currentDay > daysInMonth) break
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    for (dayOfWeek in 0..6) {
                        if ((week == 0 && dayOfWeek < firstDayOfMonth) || currentDay > daysInMonth) {
                            Spacer(Modifier.size(32.dp))
                        } else {
                            val date = currentMonthDate.withDayOfMonth(currentDay)
                            val isWorkoutDay = savedWorkouts.any { it.completionDate == date }
                            val isSelected = selectedDate == date
                            val isToday = date == LocalDate.now()

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) CyanAccent 
                                        else if (isWorkoutDay) CyanAccent.copy(alpha = 0.3f)
                                        else if (isToday) CyanAccent.copy(alpha = 0.1f) 
                                        else Color.Transparent
                                    )
                                    .clickable { onDateSelected(date) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    currentDay.toString(),
                                    color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = if (isWorkoutDay || isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            currentDay++
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            AnimatedVisibility(visible = selectedDate != null) {
                val workoutsOnSelectedDate = savedWorkouts.filter { it.completionDate == selectedDate }
                if (workoutsOnSelectedDate.isNotEmpty()) {
                    val musclesForDay = workoutsOnSelectedDate
                        .flatMap { it.exercises }
                        .mapNotNull { it.muscleGroup }
                        .distinct()
                    
                    if (musclesForDay.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Text(
                                "TARGETED MUSCLES",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGray,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(musclesForDay) { muscle ->
                                    MuscleBadgeHistory(muscle = muscle)
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
fun HistoryFilterRow(currentSort: SortOrder, onSortChange: (SortOrder) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = currentSort == SortOrder.DATE_DESC, label = "All", onClick = { onSortChange(SortOrder.DATE_DESC) })
        FilterChip(selected = currentSort == SortOrder.NAME_ASC, label = "Name", onClick = { onSortChange(SortOrder.NAME_ASC) })
    }
}

@Composable
fun FilterChip(selected: Boolean, label: String, onClick: () -> Unit) {
    Surface(
        color = if (selected) CyanAccent else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .height(36.dp)
            .clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                label,
                color = if (selected) Color.Black else TextGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HistoryWorkoutCard(workout: WorkoutHistory, onClick: () -> Unit) {
    val dateText = remember(workout.completionDate) {
        workout.completionDate.format(DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.getDefault()))
    }
    val volume = remember(workout.exercises) {
        val total = workout.exercises.sumOf { ex -> 
            ex.exerciseSets.sumOf { set -> 
                (set.weight.toDoubleOrNull() ?: 0.0) * (set.reps.toIntOrNull() ?: 0)
            }
        }
        if (total >= 1000) "%.1fk kg".format(total / 1000) else "%.0f kg".format(total)
    }

    val durationMinutes = remember(workout.durationMillis) {
        (workout.durationMillis / 60000).toInt()
    }

    val muscleGroups = remember(workout.exercises) {
        workout.exercises.mapNotNull { it.muscleGroup }.distinct()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getHistoryWorkoutImage(workout))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.6f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
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
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = workout.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.LightGray
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = CyanAccent.copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = volume,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (muscleGroups.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(muscleGroups) { muscle ->
                                MuscleBadge(muscle = muscle)
                            }
                        }
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer, 
                            null, 
                            tint = Color.White.copy(alpha = 0.7f), 
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = " ${durationMinutes} min", 
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleBadgeHistory(muscle: MuscleGroups) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp,  MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
    ) {
        Text(
            text = muscle.name,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

private fun getHistoryWorkoutImage(workout: WorkoutHistory): Int {
    if (workout.exercises.isEmpty()) return R.drawable.workout

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
