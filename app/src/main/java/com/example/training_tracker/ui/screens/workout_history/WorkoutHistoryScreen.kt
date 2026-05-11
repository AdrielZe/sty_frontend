package com.example.training_tracker.ui.screens.workout_history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutHistoryScreen(
    uiState: WorkoutHistoryUiState,
    onDateSelected: (LocalDate?) -> Unit,
    onMuscleGroupSelected: (MuscleGroups?) -> Unit,
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
                        stringResource(R.string.historico),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = CyanAccent
                        )
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
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.filtrar_por_musculo),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextGray,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(end = 24.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = uiState.selectedMuscleGroup == null,
                                label = stringResource(R.string.todos),
                                onClick = { onMuscleGroupSelected(null) }
                            )
                        }
                        items(MuscleGroups.entries.toTypedArray()) { muscle ->
                            FilterChip(
                                selected = uiState.selectedMuscleGroup == muscle,
                                label = stringResource(muscle.resId).lowercase()
                                    .replaceFirstChar { it.titlecase() },
                                onClick = { onMuscleGroupSelected(muscle) }
                            )
                        }
                    }
                }
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(R.string.ordenar_por),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextGray,
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    HistoryFilterRow(uiState.sortOrder, onSortOrderChange)
                }
            }

            item {
                Text(
                    stringResource(R.string.atividades_recentes),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextGray,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            if (uiState.savedWorkouts.isEmpty()) {
                item {
                    EmptyHistoryPlaceholder()
                }
            } else {
                items(uiState.savedWorkouts, key = { it.id }) { workout ->
                    HistoryWorkoutCard(
                        workout = workout,
                        onClick = { onClickHistory(workout.id) }
                    )
                }
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
    // 1. Título do mês traduzido (ex: Maio 2024 ou May 2024)
    val monthTitle = currentMonthDate.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    )
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    // 2. Quantidade de dias no mês (CORREÇÃO DO ERRO)
    val daysInMonth = currentMonthDate.lengthOfMonth()

    // 3. Dias da semana traduzidos automaticamente (D, S, T, Q...)
    val daysOfWeekLabels = remember {
        val symbols = java.text.DateFormatSymbols.getInstance(Locale.getDefault())
        val shortDays = symbols.shortWeekdays // Retorna ["", "dom.", "seg.", ...]
        // Calendário começa no Domingo (índice 1 no DateFormatSymbols)
        listOf(1, 2, 3, 4, 5, 6, 7).map { index ->
            shortDays[index].first().toString().uppercase()
        }
    }

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
                Text(
                    monthTitle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 20.sp
                )
                Row {
                    IconButton(onClick = { onMoveMonth(-1) }) {
                        Icon(
                            Icons.Default.ChevronLeft,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { onMoveMonth(1) }) {
                        Icon(
                            Icons.Default.ChevronRight,
                            null,
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Renderiza os cabeçalhos (D, S, T, Q...)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                daysOfWeekLabels.forEach { day ->
                    Text(
                        day,
                        color = TextGray,
                        fontSize = 12.sp,
                        modifier = Modifier.width(32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            var currentDay = 1
            for (week in 0..5) {
                if (currentDay > daysInMonth) break
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
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
                val workoutsOnSelectedDate =
                    savedWorkouts.filter { it.completionDate == selectedDate }
                if (workoutsOnSelectedDate.isNotEmpty()) {
                    val musclesForDay = workoutsOnSelectedDate
                        .flatMap { it.exercises }
                        .mapNotNull { it.muscleGroup }
                        .distinct()

                    if (musclesForDay.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(
                                modifier = Modifier.padding(bottom = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                stringResource(R.string.musculos_trabalhados),
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp), // Pequeno respiro nas bordas
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            FilterChip(
                selected = currentSort == SortOrder.DATE_DESC,
                label = stringResource(R.string.mais_recentes),
                onClick = { onSortChange(SortOrder.DATE_DESC) })
        }
        item {
            FilterChip(
                selected = currentSort == SortOrder.DATE_ASC,
                label = stringResource(R.string.mais_antigos),
                onClick = { onSortChange(SortOrder.DATE_ASC) })
        }
        item {
            FilterChip(
                selected = currentSort == SortOrder.NAME_ASC,
                label = "A-Z",
                onClick = { onSortChange(SortOrder.NAME_ASC) })
        }
        item {
            FilterChip(
                selected = currentSort == SortOrder.NAME_DESC,
                label = "Z-A",
                onClick = { onSortChange(SortOrder.NAME_DESC) })
        }
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
    val isDark = isSystemInDarkTheme()

    // ... Seus formatadores de data, tempo, volume e músculos continuam iguais ...
    val dateText = remember(workout.completionDate) {
        workout.completionDate.format(
            DateTimeFormatter.ofPattern(
                "dd MMM, yyyy",
                Locale.getDefault()
            )
        )
    }

    val timeText = remember(workout.completionTime) {
        val locale = Locale.getDefault()
        val pattern = if (locale.language == "pt") "HH:mm" else "h:mm a"
        workout.completionTime?.format(DateTimeFormatter.ofPattern(pattern, locale)) ?: ""
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
            // 👇 1. Troca do height para defaultMinSize para permitir expansão
            .defaultMinSize(minHeight = 160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0XFF0D0D0D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // 👇 2. Removido o fillMaxSize() do Box raiz para ele não travar o tamanho
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getHistoryWorkoutImage(workout))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                // 👇 3. matchParentSize nas imagens de fundo
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.6f
            )

            Box(
                modifier = Modifier
                    // 👇 3. matchParentSize no gradiente também
                    .matchParentSize()
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
                    .fillMaxWidth()
                    // 👇 4. defaultMinSize aqui para garantir o Arrangement.SpaceBetween
                    .defaultMinSize(minHeight = 160.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // --- LINHA DO TOPO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) { // O weight aqui salva a Row do volume!
                        Text(
                            text = workout.name.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            // O título já estava perfeito:
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = dateText,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray,
                                // Adicionada pequena proteção para idioma/tela fina
                                modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (timeText.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(2.dp)
                                        .background(Color.LightGray, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = timeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.LightGray.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp)) // Respiro seguro para o título não colar no volume

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

                Spacer(modifier = Modifier.height(16.dp)) // Garante que topo e base não se esmaguem

                // --- LINHA DA BASE ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (muscleGroups.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            // O weight(1f) foi brilhante aqui! Impede a Row de empurrar o tempo
                            modifier = Modifier.weight(1f)
                        ) {
                            items(muscleGroups) { muscle ->
                                MuscleBadge(muscle = muscle)
                            }
                        }
                    } else {
                        // Um spacer vazio com weight para empurrar o tempo para a direita caso não haja músculos
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // 👇 5. Respiro adicionado para que a lista de tags não grude no tempo de duração
                    Spacer(modifier = Modifier.width(16.dp))

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
                            fontWeight = FontWeight.Bold,
                            maxLines = 1 // Garante que o texto de tempo fique em linha única
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
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
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
fun EmptyHistoryPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.HistoryToggleOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.size(60.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.complete_treinos_para_ver_o_seu_hist_rico),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    }
}