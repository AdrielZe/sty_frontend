package com.example.training_tracker.ui.screens.exercise_data

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Technique
import com.example.training_tracker.ui.screens.records.EvolutionChart
import com.example.training_tracker.ui.theme.AppTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.abs

private val GainGreen = Color(0xFF4CAF50)
private val LossRed = Color(0xFFE53935)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    uiState: ExerciseDetailUiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val log = uiState.log
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = (log?.name ?: "").uppercase(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.accent.light,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppTheme.accent.light)
                }
            }

            log == null || log.sessions.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.dados_nenhum_exercicio),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            else -> ExerciseDetailContent(
                log = log,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}

@Composable
private fun ExerciseDetailContent(
    log: ExerciseLog,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
    ) {
        item {
            log.muscleGroup?.let { group ->
                Text(
                    text = stringResource(group.resId).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = AppTheme.accent.light,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }

        item {
            HeroStatGrid(
                log = log,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        item {
            VariationRow(
                log = log,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        item {
            ProgressionChartCard(
                title = stringResource(R.string.dados_progressao_peso),
                series = log.weightSeriesNewestFirst,
                formatLabel = { "${formatWeight(it)} kg" },
                pointFormatter = { formatWeight(it) },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            ProgressionChartCard(
                title = stringResource(R.string.dados_progressao_volume),
                series = log.volumeSeriesNewestFirst,
                formatLabel = { formatVolume(it) },
                pointFormatter = { formatWeight(it) },
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        item {
            Text(
                stringResource(R.string.dados_historico_sessoes),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Sessions newest-first; index maps back into the ascending list for comparison.
        val newestFirst = log.sessions.reversed()
        itemsIndexed(newestFirst) { index, session ->
            val previous = newestFirst.getOrNull(index + 1)
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                SessionCard(
                    session = session,
                    previous = previous,
                    isLatest = index == 0,
                    initiallyExpanded = index == 0
                )
            }
        }
    }
}

@Composable
private fun HeroStatGrid(
    log: ExerciseLog,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.EmojiEvents,
                value = "${formatWeight(log.allTimePR)} kg",
                label = stringResource(R.string.dados_recorde),
                highlight = true
            )
            HeroStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                value = "${formatWeight(log.bestOneRepMax)} kg",
                label = stringResource(R.string.dados_rm_estimado)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HeroStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.AutoMirrored.Filled.ShowChart,
                value = formatVolume(log.bestVolume),
                label = stringResource(R.string.dados_volume)
            )
            HeroStatTile(
                modifier = Modifier.weight(1f),
                icon = Icons.Default.CalendarMonth,
                value = "${log.sessionCount}",
                label = stringResource(R.string.dados_total_sessoes)
            )
        }
    }
}

@Composable
private fun HeroStatTile(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String,
    highlight: Boolean = false
) {
    Card(
        modifier = modifier.border(
            0.5.dp,
            AppTheme.accent.light.copy(alpha = if (highlight) 0.5f else 0.25f),
            RoundedCornerShape(18.dp)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (highlight)
                AppTheme.accent.light.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppTheme.accent.light,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 10.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun VariationRow(
    log: ExerciseLog,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VariationCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.dados_variacao_peso),
                subtitle = stringResource(R.string.dados_variacao_media_sub),
                pct = log.weightProgressPct
            )
            VariationCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.dados_variacao_volume),
                subtitle = stringResource(R.string.dados_variacao_media_sub),
                pct = log.volumeProgressPct
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VariationCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.dados_variacao_peso),
                subtitle = stringResource(R.string.dados_variacao_total_sub),
                pct = log.weightTotalProgressPct
            )
            VariationCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.dados_variacao_volume),
                subtitle = stringResource(R.string.dados_variacao_total_sub),
                pct = log.volumeTotalProgressPct
            )
        }
    }
}

@Composable
private fun VariationCard(
    modifier: Modifier = Modifier,
    label: String,
    subtitle: String,
    pct: Double?
) {
    val color = when {
        pct == null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        pct > 0 -> GainGreen
        pct < 0 -> LossRed
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    }
    val display = when {
        pct == null -> "—"
        else -> "${if (pct >= 0) "+" else ""}${"%.1f".format(pct)}%"
    }
    Card(
        modifier = modifier.border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                display,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = color,
                    fontSize = 22.sp
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 9.sp
                ),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    fontSize = 8.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ProgressionChartCard(
    title: String,
    series: List<Double>,
    formatLabel: (Double) -> String,
    pointFormatter: (Double) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, AppTheme.accent.light.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Insights,
                    contentDescription = null,
                    tint = AppTheme.accent.light,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        letterSpacing = 0.5.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (series.size > 1) {
                val maxVal = series.maxOrNull() ?: 1.0
                val minVal = series.minOrNull() ?: 0.0
                val midVal = (maxVal + minVal) / 2.0
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(54.dp)
                                .padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            listOf(maxVal, midVal, minVal).forEach { v ->
                                Text(
                                    formatLabel(v),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        EvolutionChart(
                            history = series,
                            modifier = Modifier.fillMaxSize(),
                            valueFormatter = pointFormatter,
                            showSegmentDeltas = true
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(R.string.dados_dados_insuficientes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SessionCard(
    session: ExerciseSession,
    previous: ExerciseSession?,
    isLatest: Boolean,
    initiallyExpanded: Boolean
) {
    var expanded by remember { mutableStateOf(initiallyExpanded) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "sessionChevron"
    )
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                0.5.dp,
                AppTheme.accent.light.copy(alpha = if (isLatest) 0.45f else 0.18f),
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = session.date.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isLatest) AppTheme.accent.light
                                else MaterialTheme.colorScheme.onSurface
                            )
                        )
                        session.time?.let {
                            Text(
                                text = "  •  ${it.format(timeFormatter)}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                    Text(
                        text = session.workoutName,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.rotate(chevronRotation)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SessionMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.FitnessCenter,
                    value = "${formatWeight(session.topWeight)} kg",
                    caption = stringResource(R.string.dados_pico),
                    deltaPct = previous?.let { pctChange(it.topWeight, session.topWeight) }
                )
                SessionMetric(
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.ShowChart,
                    value = formatVolume(session.totalVolume),
                    caption = stringResource(R.string.dados_volume_sessao),
                    deltaPct = previous?.let { pctChange(it.totalVolume, session.totalVolume) }
                )
                SessionMetric(
                    modifier = Modifier.weight(1f),
                    icon = null,
                    value = "${session.setCount}×${session.totalReps}",
                    caption = stringResource(R.string.dados_series_reps),
                    deltaPct = null
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(
                        color = AppTheme.accent.light.copy(alpha = 0.12f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    session.sets.forEach { set ->
                        SetRow(set)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionMetric(
    modifier: Modifier = Modifier,
    icon: ImageVector?,
    value: String,
    caption: String,
    deltaPct: Double?
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 13.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            caption,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        if (deltaPct != null && abs(deltaPct) >= 0.05) {
            val color = if (deltaPct >= 0) GainGreen else LossRed
            val arrow = if (deltaPct >= 0) "▲" else "▼"
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "$arrow ${"%.0f".format(abs(deltaPct))}%",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 8.sp
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SetRow(set: LoggedSet) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(AppTheme.accent.light.copy(alpha = 0.18f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "${set.setNumber}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = AppTheme.accent.light,
                    fontSize = 11.sp
                )
            )
        }
        Text(
            text = stringResource(R.string.dados_reps_peso, set.reps, formatWeight(set.weight)),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            )
        )
        if (set.technique != Technique.NORMAL) {
            Text(
                text = stringResource(set.technique.label),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = AppTheme.accent.light.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                ),
                maxLines = 1
            )
        } else {
            Text(
                text = "${formatWeight(set.volume)} kg",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontSize = 10.sp
                )
            )
        }
    }
}

private fun pctChange(old: Double, new: Double): Double? {
    if (old <= 0.0) return null
    return (new - old) / old * 100.0
}
