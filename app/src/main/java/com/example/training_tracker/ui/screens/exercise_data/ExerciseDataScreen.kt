package com.example.training_tracker.ui.screens.exercise_data

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.ui.screens.records.MuscleGroupChip
import com.example.training_tracker.ui.screens.records.RecordsSearchBar
import com.example.training_tracker.ui.theme.AppTheme
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

/* ─────────────────────────────────────────────────────────────────────────────
 * Local UI-only state for sort. Doesn't touch ExerciseDataViewModel.
 * ───────────────────────────────────────────────────────────────────────────── */
private enum class ExerciseDataSort(@androidx.annotation.StringRes val labelRes: Int) {
    RECENT(R.string.dados_sort_recente),
    WEIGHT(R.string.dados_sort_peso),
    PROGRESS(R.string.dados_sort_progresso),
    VOLUME(R.string.dados_sort_volume);

    fun apply(list: List<ExerciseLog>): List<ExerciseLog> = when (this) {
        RECENT   -> list.sortedByDescending { it.lastSession?.date ?: LocalDate.MIN }
        WEIGHT   -> list.sortedByDescending { it.allTimePR }
        PROGRESS -> list.sortedByDescending { it.weightProgressPct ?: Double.NEGATIVE_INFINITY }
        VOLUME   -> list.sortedByDescending { it.totalVolumeLifted }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDataScreen(
    uiState: ExerciseDataUiState,
    onNavigateBack: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onMuscleGroupSelected: (MuscleGroups?) -> Unit,
    onExerciseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var sort by rememberSaveable { mutableStateOf(ExerciseDataSort.RECENT) }

    val sortedExercises = remember(uiState.exercises, sort) { sort.apply(uiState.exercises) }

    // Spotlight: best positive weight-progression exercise with >=2 sessions.
    val spotlight: ExerciseLog? = remember(uiState.exercises) {
        uiState.exercises
            .filter { (it.weightProgressPct ?: 0.0) > 0.0 && it.sessionCount >= 2 }
            .maxByOrNull { it.weightProgressPct ?: 0.0 }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            stringResource(R.string.dados_eyebrow),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AppTheme.accent.light,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.6.sp,
                                fontSize = 9.5.sp
                            )
                        )
                        Text(
                            stringResource(R.string.dados_de_exercicios),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.3).sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.voltar),
                            tint = AppTheme.accent.light
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* future: filter sheet */ }) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppTheme.accent.light)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
        ) {
            // Spotlight hero
            spotlight?.let { ex ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        SpotlightCard(ex)
                    }
                }
            }

            // Stats row
            item {
                ExerciseDataSummaryRow(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            // Search
            item {
                RecordsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = onSearchQueryChanged,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Sort segmented
            item {
                SortSegmented(
                    selected = sort,
                    onSelect = { sort = it },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Muscle chips
            if (uiState.availableMuscleGroups.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            MuscleGroupChip(
                                label = stringResource(R.string.todos),
                                isSelected = uiState.selectedMuscleGroup == null,
                                onClick = { onMuscleGroupSelected(null) }
                            )
                        }
                        items(uiState.availableMuscleGroups) { group ->
                            MuscleGroupChipWithThumb(
                                group = group,
                                isSelected = uiState.selectedMuscleGroup == group,
                                onClick = { onMuscleGroupSelected(group) }
                            )
                        }
                    }
                }
            }

            // Results header
            if (sortedExercises.isNotEmpty()) {
                item {
                    ResultsHead(
                        count = sortedExercises.size,
                        sort = sort,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            // List / empty
            if (sortedExercises.isEmpty()) {
                item {
                    EmptyExerciseData(
                        hasNoData = uiState.totalExercises == 0,
                        onResetFilters = {
                            onSearchQueryChanged("")
                            onMuscleGroupSelected(null)
                        }
                    )
                }
            } else {
                items(sortedExercises, key = { it.name }) { log ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ExerciseLogCard(
                            log = log,
                            onClick = { onExerciseClick(log.name) }
                        )
                    }
                }
            }
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * SPOTLIGHT
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun SpotlightCard(ex: ExerciseLog) {
    val drawable = ex.muscleGroup?.let { muscleGroupImage(it) }
    val progress = ex.weightProgressPct?.toInt()
    val latestWeight = ex.lastSession?.topWeight ?: ex.allTimePR

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, AppTheme.accent.light.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        drawable?.let { res ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(res).crossfade(false).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(0.30f)
            )
        }
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    )
                )
            )
        )
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.radialGradient(
                    colors = listOf(AppTheme.accent.light.copy(alpha = 0.30f), Color.Transparent),
                    radius = 600f
                )
            )
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // eyebrow
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(AppTheme.accent.gradient)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        stringResource(R.string.dados_em_destaque),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black,
                            letterSpacing = 1.8.sp,
                            fontSize = 9.5.sp
                        )
                    )
                }
                ex.muscleGroup?.let {
                    Text(
                        stringResource(it.resId).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            letterSpacing = 1.6.sp,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }

            // name + progress badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = ex.name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.4).sp,
                        fontSize = 22.sp
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ProgressBadge(value = progress)
                    if (progress != null) {
                        Text(
                            text = stringResource(R.string.dados_variacao_media_sub).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.40f),
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.2.sp,
                                fontSize = 8.sp
                            )
                        )
                    }
                }
            }

            // big number + aux stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = formatWeight(latestWeight),
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-1).sp,
                                fontSize = 40.sp
                            )
                        )
                        Text(
                            text = "kg",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                            ),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Text(
                        stringResource(R.string.dados_recorde_pessoal).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                            letterSpacing = 1.6.sp,
                            fontSize = 9.sp
                        )
                    )
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AuxStat("${ex.sessionCount}", stringResource(R.string.dados_label_sessoes))
                    AuxStat(formatVolume(ex.totalVolumeLifted), stringResource(R.string.dados_label_volume))
                }
            }

            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f)
            )
            HeroSparkline(
                data = ex.weightSeriesNewestFirst,
                modifier = Modifier.fillMaxWidth().height(64.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = stringResource(R.string.dados_sessoes_count, ex.sessionCount),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
                Text(
                    text = stringResource(R.string.dados_hoje_kg, formatWeight(latestWeight)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AppTheme.accent.light,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun AuxStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.2).sp,
                fontSize = 15.sp
            )
        )
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                letterSpacing = 1.4.sp,
                fontSize = 9.sp
            )
        )
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * PROGRESS BADGE
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun ProgressBadge(value: Int?) {
    if (value == null) return
    val (bg, fg, icon) = when {
        value > 0 -> Triple(Color(0xFF4CAF50).copy(alpha = 0.15f), Color(0xFF4CAF50), Icons.Default.ArrowUpward)
        value < 0 -> Triple(Color(0xFFE53935).copy(alpha = 0.15f), Color(0xFFE53935), Icons.Default.ArrowDownward)
        else      -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
            Icons.Default.Remove
        )
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(0.5.dp, fg.copy(alpha = 0.30f), RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
        Text(
            text = "${if (value > 0) "+" else ""}$value%",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = fg,
                fontSize = 11.sp
            )
        )
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * SUMMARY ROW
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun ExerciseDataSummaryRow(uiState: ExerciseDataUiState, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.FitnessCenter,
            value = "${uiState.totalExercises}",
            label = stringResource(R.string.dados_total_exercicios)
        )
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CalendarMonth,
            value = "${uiState.totalSessions}",
            label = stringResource(R.string.dados_total_sessoes)
        )
        SummaryStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.TrendingUp,
            value = formatVolume(uiState.totalVolumeLifted),
            label = stringResource(R.string.dados_volume_total)
        )
    }
}

@Composable
private fun SummaryStatCard(modifier: Modifier = Modifier, icon: ImageVector, value: String, label: String) {
    Card(
        modifier = modifier.border(0.5.dp, AppTheme.accent.light.copy(alpha = 0.20f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(AppTheme.accent.light.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = AppTheme.accent.light, modifier = Modifier.size(16.dp))
            }
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    letterSpacing = (-0.4).sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp,
                    fontSize = 9.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * SORT SEGMENTED
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun SortSegmented(
    selected: ExerciseDataSort,
    onSelect: (ExerciseDataSort) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(0.5.dp, AppTheme.accent.light.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ExerciseDataSort.values().forEach { mode ->
            val isActive = mode == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .then(if (isActive) Modifier.background(AppTheme.accent.gradient) else Modifier)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(mode.labelRes),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isActive) Color.Black else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        letterSpacing = 0.4.sp,
                        fontSize = 11.sp
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * MUSCLE CHIP WITH THUMBNAIL
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun MuscleGroupChipWithThumb(group: MuscleGroups, isSelected: Boolean, onClick: () -> Unit) {
    val thumb = muscleGroupImage(group)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(
                if (isSelected) AppTheme.accent.light.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) AppTheme.accent.light.copy(alpha = 0.5f)
                else AppTheme.accent.light.copy(alpha = 0.18f),
                shape = RoundedCornerShape(100.dp)
            )
            .clickable { onClick() }
            .padding(start = 6.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(thumb).crossfade(false).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Text(
            text = stringResource(group.resId).replaceFirstChar { it.titlecase(Locale.getDefault()) },
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = if (isSelected) AppTheme.accent.light else MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp
            )
        )
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * RESULTS HEAD
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun ResultsHead(count: Int, sort: ExerciseDataSort, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = if (count == 1)
                stringResource(R.string.dados_results_count_single, count)
            else
                stringResource(R.string.dados_results_count, count),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 1.4.sp,
                fontSize = 10.sp
            )
        )
        Text(
            text = stringResource(R.string.dados_ordenado_por, stringResource(sort.labelRes).lowercase()),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                letterSpacing = 1.4.sp,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * EXERCISE LOG CARD
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun ExerciseLogCard(log: ExerciseLog, onClick: () -> Unit) {
    val drawable = log.muscleGroup?.let { muscleGroupImage(it) }
    val progress = log.weightProgressPct?.toInt()
    val lastDate = log.lastSession?.date
    val daysAgo = lastDate?.let { ChronoUnit.DAYS.between(it, LocalDate.now()).toInt() }
    val trendColor = when {
        progress != null && progress > 0 -> Color(0xFF4CAF50)
        progress != null && progress < 0 -> Color(0xFFE53935)
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .border(0.5.dp, AppTheme.accent.light.copy(alpha = 0.18f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header row
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(0.5.dp, AppTheme.accent.light.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                ) {
                    drawable?.let {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(it).crossfade(false).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.2).sp,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        log.muscleGroup?.let {
                            Text(
                                text = stringResource(it.resId).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.accent.light,
                                    letterSpacing = 1.6.sp,
                                    fontSize = 9.sp
                                )
                            )
                            Dot()
                        }
                        Text(
                            text = if (log.sessionCount == 1)
                                stringResource(R.string.dados_sessao_count_single, log.sessionCount)
                            else
                                stringResource(R.string.dados_sessoes_count, log.sessionCount),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                fontSize = 11.sp
                            )
                        )
                        daysAgo?.let {
                            Dot()
                            Text(
                                text = when (it) {
                                    0 -> stringResource(R.string.dados_hoje)
                                    1 -> stringResource(R.string.dados_ha_um_dia)
                                    else -> stringResource(R.string.dados_ha_dias, it)
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AppTheme.accent.gradient)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = formatWeight(log.allTimePR),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    letterSpacing = (-0.2).sp,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = "kg",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                ),
                                modifier = Modifier.padding(bottom = 1.dp)
                            )
                        }
                    }
                    ProgressBadge(value = progress)
                }
            }

            // Full-width sparkline
            if (log.sessionCount >= 2) {
                TrendSparkline(
                    data = log.weightSeriesNewestFirst,
                    color = trendColor,
                    modifier = Modifier.fillMaxWidth().height(36.dp)
                )
            }

            // Last session pills
            log.lastSession?.let { session ->
                HorizontalDivider(
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.dados_ult).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            letterSpacing = 2.sp,
                            fontSize = 9.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Row(
                        modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        session.sets.take(3).forEach { s -> SetPill(weight = s.weight, reps = s.reps) }
                        if (session.sets.size > 3) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "+${session.sets.size - 3}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                    Text(
                        text = formatVolume(log.totalVolumeLifted),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            letterSpacing = (-0.1).sp,
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun SetPill(weight: Double, reps: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppTheme.accent.light.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        Text(
            formatWeight(weight),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.accent.light,
                fontSize = 11.sp
            )
        )
        Text(
            "×",
            style = MaterialTheme.typography.labelSmall.copy(
                color = AppTheme.accent.light.copy(alpha = 0.45f),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp
            )
        )
        Text(
            "$reps",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.accent.light,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun Dot() {
    Box(
        modifier = Modifier
            .size(2.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f))
    )
}

/* ═════════════════════════════════════════════════════════════════════════════
 * SPARKLINES (smooth area + line, end-point dot)
 * Mirrors the HTML prototype: receives newest-first, reverses internally.
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun HeroSparkline(data: List<Double>, modifier: Modifier = Modifier) {
    TrendSparkline(data = data, color = AppTheme.accent.light, showEndPoint = true, modifier = modifier)
}

@Composable
private fun TrendSparkline(
    data: List<Double>,
    color: Color,
    showEndPoint: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chronological = remember(data) { data.reversed() }
    if (chronological.isEmpty()) return
    val maxV = chronological.maxOrNull()?.toFloat() ?: 1f
    val minV = chronological.minOrNull()?.toFloat() ?: 0f
    val range = if (maxV == minV) 1f else maxV - minV

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val pad = 4f
        val innerW = w - pad * 2
        val innerH = h - pad * 2
        val n = chronological.size
        if (n == 1) {
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            return@Canvas
        }
        val step = innerW / (n - 1)
        val pts = chronological.mapIndexed { i, v ->
            Offset(
                x = pad + i * step,
                y = pad + innerH * (1f - (v.toFloat() - minV) / range)
            )
        }
        val line = Path().apply {
            moveTo(pts.first().x, pts.first().y)
            for (i in 1 until pts.size) {
                val prev = pts[i - 1]
                val cur = pts[i]
                val midX = (prev.x + cur.x) / 2f
                cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
            }
        }
        val area = Path().apply {
            addPath(line)
            lineTo(pts.last().x, h)
            lineTo(pts.first().x, h)
            close()
        }
        drawPath(
            area,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.35f), Color.Transparent),
                startY = 0f, endY = h
            )
        )
        drawPath(line, color = color, style = Stroke(width = 1.6.dp.toPx(), cap = StrokeCap.Round))

        if (showEndPoint) {
            val last = pts.last()
            drawCircle(color = Color.Black.copy(alpha = 0.85f), radius = 5.dp.toPx(), center = last)
            drawCircle(color = color, radius = 3.dp.toPx(), center = last)
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * EMPTY STATE
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun EmptyExerciseData(hasNoData: Boolean, onResetFilters: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, AppTheme.accent.light.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(AppTheme.accent.light.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Insights,
                contentDescription = null,
                tint = AppTheme.accent.light,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(
                if (hasNoData) R.string.dados_nenhum_exercicio
                else R.string.dados_nenhum_resultado
            ),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.2).sp,
                fontSize = 16.sp
            )
        )
        Text(
            stringResource(
                if (hasNoData) R.string.dados_nenhum_exercicio_sub
                else R.string.dados_nenhum_resultado_sub
            ),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = 12.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (!hasNoData) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppTheme.accent.light.copy(alpha = 0.12f))
                    .border(1.dp, AppTheme.accent.light.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
                    .clickable { onResetFilters() }
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                Text(
                    stringResource(R.string.dados_limpar_filtros).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTheme.accent.light,
                        letterSpacing = 1.4.sp,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * Maps a MuscleGroups entry to a drawable id from res/drawable/.
 * All entries point to drawables that already exist in your project.
 * ═════════════════════════════════════════════════════════════════════════════ */
@DrawableRes
private fun muscleGroupImage(group: MuscleGroups): Int = when (group) {
    MuscleGroups.CHEST      -> R.drawable.chest_workout
    MuscleGroups.BACK       -> R.drawable.back_workout
    MuscleGroups.TRICEPS    -> R.drawable.triceps_workout
    MuscleGroups.BICEPS     -> R.drawable.biceps_workout
    MuscleGroups.SHOULDERS  -> R.drawable.shoulder_workout
    MuscleGroups.QUADRICEPS -> R.drawable.quadriceps_workout
    MuscleGroups.HAMSTRINGS -> R.drawable.hamstrings_workout
    MuscleGroups.CALF       -> R.drawable.calf_workout
    MuscleGroups.GLUTE      -> R.drawable.glute_workout
    MuscleGroups.ABS        -> R.drawable.abs_workout
    MuscleGroups.CARDIO     -> R.drawable.cardio_workout
    MuscleGroups.STRETCHING -> R.drawable.stretching_workout
}
