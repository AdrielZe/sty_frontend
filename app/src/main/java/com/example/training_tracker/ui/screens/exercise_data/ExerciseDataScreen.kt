package com.example.training_tracker.ui.screens.exercise_data

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.ui.screens.records.MiniSparkline
import com.example.training_tracker.ui.screens.records.MuscleGroupChip
import com.example.training_tracker.ui.screens.records.RecordsSearchBar
import com.example.training_tracker.ui.theme.AppTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

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
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.dados_de_exercicios),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.accent.light,
                            letterSpacing = 1.sp
                        )
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
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
        ) {
            item {
                ExerciseDataSummaryRow(
                    uiState = uiState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                )
            }

            item {
                RecordsSearchBar(
                    query = uiState.searchQuery,
                    onQueryChanged = onSearchQueryChanged,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            if (uiState.availableMuscleGroups.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
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
                            MuscleGroupChip(
                                label = stringResource(group.resId).lowercase()
                                    .replaceFirstChar { it.titlecase(Locale.getDefault()) },
                                isSelected = uiState.selectedMuscleGroup == group,
                                onClick = { onMuscleGroupSelected(group) }
                            )
                        }
                    }
                }
            }

            if (uiState.exercises.isEmpty()) {
                item {
                    EmptyExerciseData(
                        hasNoData = uiState.totalExercises == 0
                    )
                }
            } else {
                items(uiState.exercises, key = { it.name }) { log ->
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
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

@Composable
private fun ExerciseDataSummaryRow(
    uiState: ExerciseDataUiState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
            icon = Icons.AutoMirrored.Filled.ShowChart,
            value = formatVolume(uiState.totalVolumeLifted),
            label = stringResource(R.string.dados_volume_total)
        )
    }
}

@Composable
private fun SummaryStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier.border(
            0.5.dp,
            AppTheme.accent.light.copy(alpha = 0.3f),
            RoundedCornerShape(16.dp)
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                icon,
                contentDescription = null,
                tint = AppTheme.accent.light,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 9.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ExerciseLogCard(
    log: ExerciseLog,
    onClick: () -> Unit
) {
    val lastDate = log.lastSession?.date
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    val progressPct = log.weightProgressPct?.toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(0.5.dp, AppTheme.accent.light.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.name.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    log.muscleGroup?.let { group ->
                        Text(
                            text = stringResource(group.resId).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = AppTheme.accent.light.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontSize = 9.sp
                            )
                        )
                    }
                    lastDate?.let {
                        Text(
                            text = stringResource(R.string.dados_ultima_vez, it.format(dateFormatter)),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .background(AppTheme.accent.gradient, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${formatWeight(log.allTimePR)} kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        ),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InfoChip(
                        text = if (log.sessionCount == 1)
                            stringResource(R.string.dados_sessao_count_single, log.sessionCount)
                        else
                            stringResource(R.string.dados_sessoes_count, log.sessionCount)
                    )
                    if (progressPct != null) {
                        val badgeColor = when {
                            progressPct > 0 -> Color(0xFF4CAF50)
                            progressPct < 0 -> Color(0xFFE53935)
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        }
                        Box(
                            modifier = Modifier
                                .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${if (progressPct >= 0) "+" else ""}$progressPct%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                if (log.sessionCount >= 2) {
                    MiniSparkline(
                        history = log.weightSeriesNewestFirst,
                        modifier = Modifier
                            .width(64.dp)
                            .height(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 10.sp
            )
        )
    }
}

@Composable
private fun EmptyExerciseData(hasNoData: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Insights,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(
                if (hasNoData) R.string.dados_nenhum_exercicio
                else R.string.dados_nenhum_resultado
            ),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
        )
        if (hasNoData) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                stringResource(R.string.dados_nenhum_exercicio_sub),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 48.dp)
            )
        }
    }
}
