package com.example.training_tracker.ui.screens.records

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    uiState: RecordsUiState,
    onNavigateBack: () -> Unit,
    onMuscleGroupSelected: (MuscleGroups?) -> Unit,
    onExerciseClick: (String, List<Double>) -> Unit,
    onDismissHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PERSONAL RECORDS",
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
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanAccent)
            }
        } else {
            val exerciseRecordsMap = uiState.records?.exercisesRecordMap ?: emptyMap()
            val sortedRecords = exerciseRecordsMap.toList()
                .sortedByDescending { it.second.maxOrNull() ?: 0.0 }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        RecordsPodium(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            topExercises = sortedRecords.take(3)
                        )

                        RecordOverviewCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "BEST VOLUME",
                            value = "${uiState.records?.volumeRecords?.maxOrNull() ?: 0.0} kg",
                            icon = Icons.AutoMirrored.Filled.ShowChart
                        )
                    }
                }

                item {
                    Column {
                        Text(
                            "FILTER BY MUSCLE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                        )
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                MuscleGroupChip(
                                    label = "ALL",
                                    isSelected = uiState.selectedMuscleGroup == null,
                                    onClick = { onMuscleGroupSelected(null) }
                                )
                            }
                            items(MuscleGroups.entries.toTypedArray()) { group ->
                                MuscleGroupChip(
                                    label = group.name,
                                    isSelected = uiState.selectedMuscleGroup == group,
                                    onClick = { onMuscleGroupSelected(group) }
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "EXERCISE PERSONAL BESTS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
                    )
                }

                if (exerciseRecordsMap.isEmpty()) {
                    item {
                        EmptyRecordsPlaceholder()
                    }
                } else {
                    items(sortedRecords) { (exerciseName, weights) ->
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            ExerciseRecordCard(
                                exerciseName = exerciseName,
                                bestWeight = weights.maxOrNull() ?: 0.0,
                                history = weights,
                                onClick = { onExerciseClick(exerciseName, weights) }
                            )
                        }
                    }
                }
            }
        }

        // Modal de Histórico e Gráfico
        if (uiState.selectedExerciseHistory != null) {
            val (name, history) = uiState.selectedExerciseHistory
            ModalBottomSheet(
                onDismissRequest = onDismissHistory,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = CyanAccent.copy(alpha = 0.5f)) }
            ) {
                ExerciseHistoryContent(
                    exerciseName = name,
                    history = history
                )
            }
        }
    }
}

@Composable
fun ExerciseHistoryContent(
    exerciseName: String,
    history: List<Double>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = exerciseName.uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Black,
                color = CyanAccent,
                letterSpacing = 1.sp
            )
        )
        Text(
            text = "EVOLUTION CHART",
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Gráfico Simples
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(16.dp)
        ) {
            if (history.size > 1) {
                EvolutionChart(history = history)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Add more records to see evolution",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "RECORD HISTORY",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Lista de Histórico
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            history.forEachIndexed { index, weight ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (index == 0) "Current Record" else "Previous mark",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == 0) CyanAccent else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "$weight kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun EvolutionChart(history: List<Double>) {
    val maxWeight = history.maxOrNull()?.toFloat() ?: 1f
    val minWeight = history.minOrNull()?.toFloat() ?: 0f
    val range = if (maxWeight == minWeight) 1f else maxWeight - minWeight

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = width / (history.size - 1)

        val points = history.mapIndexed { index, weight ->
            val x = index * spacing
            val y = height - ((weight.toFloat() - minWeight) / range) * height * 0.8f - (height * 0.1f)
            Offset(x, y)
        }

        val path = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = path,
            color = CyanAccent,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        points.forEach { point ->
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = CyanAccent,
                radius = 4.dp.toPx(),
                center = point,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

@Composable
fun MuscleGroupChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) CyanAccent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else CyanAccent.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
fun RecordsPodium(
    modifier: Modifier = Modifier,
    topExercises: List<Pair<String, List<Double>>>
) {
    Card(
        modifier = modifier
            .border(
                width = 0.5.dp,
                color = CyanAccent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TOP LIFTS",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = CyanAccent,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                PodiumPillar(
                    weight = topExercises.getOrNull(1)?.second?.maxOrNull() ?: 0.0,
                    label = "2nd",
                    heightFraction = 0.65f,
                    exerciseName = topExercises.getOrNull(1)?.first ?: "-",
                    pillarWidth = 48.dp
                )
                // 1st Place
                PodiumPillar(
                    weight = topExercises.getOrNull(0)?.second?.maxOrNull() ?: 0.0,
                    label = "1st",
                    heightFraction = 0.95f,
                    exerciseName = topExercises.getOrNull(0)?.first ?: "-",
                    isGold = true,
                    pillarWidth = 56.dp
                )
                // 3rd Place
                PodiumPillar(
                    weight = topExercises.getOrNull(2)?.second?.maxOrNull() ?: 0.0,
                    label = "3rd",
                    heightFraction = 0.5f,
                    exerciseName = topExercises.getOrNull(2)?.first ?: "-",
                    pillarWidth = 48.dp
                )
            }
        }
    }
}

@Composable
fun PodiumPillar(
    weight: Double,
    label: String,
    heightFraction: Float,
    exerciseName: String,
    pillarWidth: androidx.compose.ui.unit.Dp = 40.dp,
    isGold: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Text(
            exerciseName.uppercase(),
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isGold) CyanAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(72.dp)
        )
        Text(
            if (weight > 0.0) "$weight kg" else "-",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                fontSize = 12.sp
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(pillarWidth)
                .fillMaxHeight(heightFraction)
                .background(
                    brush = if (isGold) CyanGradient else SolidColor(CyanAccent.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                label,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isGold) Color.Black else MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun RecordOverviewCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier
            .border(
                width = 0.5.dp,
                color = CyanAccent.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                )
                Text(
                    value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 24.sp
                    )
                )
            }
            Icon(
                icon,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun ExerciseRecordCard(
    exerciseName: String,
    bestWeight: Double,
    history: List<Double>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(
                width = 0.1.dp,
                color = CyanAccent.copy(alpha = 0.2f),
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.2f
            )
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exerciseName.uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.5.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    stringResource(R.string.maior_recorde_pessoal),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(CyanGradient, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        "$bestWeight kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyRecordsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(R.string.nenhum_recorde_registrado),
            style = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            stringResource(R.string.complete_treinos_para_ver_seu_progresso),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        )
    }
}
