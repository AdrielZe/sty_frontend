package com.example.training_tracker.ui.screens.workout_history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.data.models.WorkoutHistory
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.theme.CyanAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    uiState: WorkoutHistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onNavigateBack: () -> Unit,
    onClickHistory: (String) -> Unit,
) {
    Scaffold(
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            // Seção do Calendário
            item {
                HistoryCalendarCard(uiState.savedWorkouts)
            }

            // Filtros Rápidos
            item {
                HistoryFilterRow(uiState.sortOrder, onSortOrderChange)
            }

            // Título Atividades Recentes
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

            // Lista de Treinos Dinâmica
            items(uiState.savedWorkouts, key = { it.id }) { workout ->
                HistoryWorkoutCard(
                    workout = workout,
                    onClick = { onClickHistory(workout.id) }
                )
            }
            
            // Card de Progresso/Incentivo
            item {
                MomentumCard(uiState.savedWorkouts.size)
            }
        }
    }
}

@Composable
fun HistoryCalendarCard(savedWorkouts: List<WorkoutHistory>) {
    val currentMonth = remember { LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)) }

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
                Text(currentMonth, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp)
                Row {
                    IconButton(onClick = {}) { Icon(Icons.Default.ChevronLeft, null, tint = MaterialTheme.colorScheme.onSurface) }
                    IconButton(onClick = {}) { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface) }
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
            
            // Grid simplificada da semana atual para o exemplo
            val firstDayOfWeek = remember { LocalDate.now().with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.SUNDAY)) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (0..6).forEach { i ->
                    val date = firstDayOfWeek.plusDays(i.toLong())
                    val isWorkoutDay = savedWorkouts.any { it.completionDate == date }
                    val isToday = date == LocalDate.now()

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (isWorkoutDay) CyanAccent else if (isToday) CyanAccent.copy(alpha = 0.2f) else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            date.dayOfMonth.toString(),
                            color = if (isWorkoutDay) Color.Black else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isWorkoutDay) FontWeight.Bold else FontWeight.Normal
                        )
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = TextGray
                )
            }

            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(workout.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp)
                Text(dateText, fontSize = 12.sp, color = TextGray)
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    volume,
                    fontWeight = FontWeight.Black,
                    color = CyanAccent,
                    fontSize = 16.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = TextGray, modifier = Modifier.size(12.dp))
                    Text(" 45M", fontSize = 11.sp, color = TextGray) // Exemplo fixo ou extraído se houver campo de duração
                }
            }
        }
    }
}

@Composable
fun MomentumCard(count: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Imagem de Fundo
        // Para usar, basta descomentar e substituir R.drawable.momentum_bg pela sua imagem
        Image(
            painter = painterResource(id = R.drawable.strong_6k),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        Column(modifier = Modifier.padding(24.dp).align(Alignment.CenterStart)) {
            Text(
                "KEEP THE\nMOMENTUM.",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = CyanAccent,
                    lineHeight = 24.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "You've completed $count workouts in total. Consistency is the path to greatness.",
                fontSize = 12.sp,
                color = TextGray
            )
        }
    }
}
