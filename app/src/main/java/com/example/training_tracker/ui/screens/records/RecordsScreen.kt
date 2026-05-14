package com.example.training_tracker.ui.screens.records

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
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
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordsScreen(
    uiState: RecordsUiState,
    onNavigateBack: () -> Unit,
    onMuscleGroupSelected: (MuscleGroups?) -> Unit,
    onExerciseClick: (String, List<Double>) -> Unit,
    onDismissHistory: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.recordes_pessoais),
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
            val sortedStrengthRecords = exerciseRecordsMap.toList()
                .sortedByDescending { it.second.maxOrNull() ?: 0.0 }

            val sortedCardioRecords = uiState.cardioRecords.toList()
                .sortedByDescending { it.second.maxOrNull() ?: 0.0 }

            val allEmpty = sortedStrengthRecords.isEmpty() && sortedCardioRecords.isEmpty()

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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RecordsPodium(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            topExercises = sortedStrengthRecords.take(3)
                        )

                        StatsRow(
                            modifier = Modifier.fillMaxWidth(),
                            records = uiState.records,
                            allHistories = (exerciseRecordsMap.values + uiState.cardioRecords.values).toList()
                        )
                    }
                }

                item {
                    RecordsSearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = onSearchQueryChanged,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }

                item {
                    Column {
                        Text(
                            stringResource(R.string.filtrar_por_musculo_upper),
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
                                    label = stringResource(R.string.todos),
                                    isSelected = uiState.selectedMuscleGroup == null,
                                    onClick = { onMuscleGroupSelected(null) }
                                )
                            }
                            items(MuscleGroups.entries.toTypedArray()) { group ->
                                if (group.resId != R.string.stretching) {
                                    MuscleGroupChip(
                                        label = stringResource(group.resId).lowercase()
                                            .replaceFirstChar { it.titlecase() },
                                        isSelected = uiState.selectedMuscleGroup == group,
                                        onClick = { onMuscleGroupSelected(group) }
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        stringResource(R.string.recordes_de_exercicios),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
                    )
                }

                if (allEmpty) {
                    item {
                        EmptyRecordsPlaceholder()
                    }
                } else {
                    items(sortedStrengthRecords) { (exerciseName, weights) ->
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            ExerciseRecordCard(
                                exerciseName = exerciseName,
                                bestValue = weights.maxOrNull() ?: 0.0,
                                history = weights,
                                isCardio = false,
                                onClick = { onExerciseClick(exerciseName, weights) }
                            )
                        }
                    }

                    items(sortedCardioRecords) { (exerciseName, times) ->
                        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                            ExerciseRecordCard(
                                exerciseName = exerciseName,
                                bestValue = times.maxOrNull() ?: 0.0,
                                history = times,
                                isCardio = true,
                                onClick = { onExerciseClick(exerciseName, times) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.selectedExerciseHistory != null) {
            val (name, history) = uiState.selectedExerciseHistory
            ModalBottomSheet(
                onDismissRequest = onDismissHistory,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle(color = CyanAccent.copy(alpha = 0.5f)) }
            ) {
                ExerciseHistoryContent(
                    exerciseName = name,
                    history = history,
                    isCardio = uiState.selectedExerciseIsCardio
                )
            }
        }
    }
}

private fun Double.toTimeString(): String {
    val total = this.toLong()
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}

@Composable
fun RecordsSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChanged,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(CyanAccent),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(16.dp)
                    )
                    .border(1.dp, CyanAccent.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = CyanAccent.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            stringResource(R.string.buscar_exercicio_records),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onQueryChanged("") }
                    )
                }
            }
        },
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun StatsRow(
    modifier: Modifier = Modifier,
    records: com.example.training_tracker.data.models.Records?,
    allHistories: List<List<Double>>
) {
    val bestVolume = records?.volumeRecords?.maxOrNull() ?: 0.0
    val totalExercises = (records?.exercisesRecordMap?.size ?: 0) + (records?.cardioRecordsMap?.size ?: 0)
    val bestImprovement = remember(allHistories) {
        allHistories
            .filter { it.size >= 2 }
            .mapNotNull { history ->
                val oldest = history.last()
                if (oldest > 0) ((history.first() - oldest) / oldest * 100).toInt() else null
            }
            .maxOrNull()
    }

    val volumeFormatted = remember(bestVolume) {
        java.text.NumberFormat.getInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }.format(bestVolume) + " kg"
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CompactStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.melhor_volume),
            value = volumeFormatted,
            icon = Icons.AutoMirrored.Filled.ShowChart
        )
        CompactStatCard(
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.exercicios_registrados),
            value = "$totalExercises",
            icon = Icons.Default.EmojiEvents
        )
        if (bestImprovement != null) {
            CompactStatCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.melhor_melhora),
                value = "+$bestImprovement%",
                icon = Icons.AutoMirrored.Filled.ShowChart,
                valueColor = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
fun CompactStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier.border(0.5.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = valueColor,
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
fun ExerciseHistoryContent(
    exerciseName: String,
    history: List<Double>,
    isCardio: Boolean = false,
) {
    val chronologicalHistory = remember(history) { history.reversed() }
    val maxVal = chronologicalHistory.maxOrNull() ?: 1.0
    val minVal = chronologicalHistory.minOrNull() ?: 0.0
    val midVal = (maxVal + minVal) / 2.0

    val formatLabel: (Double) -> String = { v ->
        if (isCardio) v.toTimeString()
        else java.text.NumberFormat.getInstance(Locale.getDefault()).apply {
            maximumFractionDigits = 1
            minimumFractionDigits = 0
        }.format(v) + " kg"
    }

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
            text = stringResource(R.string.grafico_de_evolucao),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    RoundedCornerShape(16.dp)
                )
                .padding(start = 8.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
        ) {
            if (history.size > 1) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Y-axis labels
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(52.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            formatLabel(maxVal),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            maxLines = 1
                        )
                        Text(
                            formatLabel(midVal),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            maxLines = 1
                        )
                        Text(
                            formatLabel(minVal),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            ),
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    EvolutionChart(
                        history = history,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.adicione_mais_recordes_para_acompanhar_a_evolucao),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = stringResource(R.string.historico_de_recordes),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            history.forEachIndexed { index, value ->
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
                        text = if (index == 0) stringResource(R.string.recorde_atual) else stringResource(R.string.recorde_anterior),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (index == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (index == 0) CyanAccent else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = if (isCardio) value.toTimeString() else "$value kg",
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
fun EvolutionChart(
    history: List<Double>,
    modifier: Modifier = Modifier
) {
    val chronologicalHistory = remember(history) { history.reversed() }
    val maxWeight = chronologicalHistory.maxOrNull()?.toFloat() ?: 1f
    val minWeight = chronologicalHistory.minOrNull()?.toFloat() ?: 0f
    val range = if (maxWeight == minWeight) 1f else maxWeight - minWeight

    val progress = remember { Animatable(0f) }
    LaunchedEffect(history) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
    }

    Canvas(modifier = modifier) {
        val animProgress = progress.value
        val width = size.width
        val height = size.height
        val spacing = if (chronologicalHistory.size > 1) width / (chronologicalHistory.size - 1) else width

        val points = chronologicalHistory.mapIndexed { index, weight ->
            val x = index * spacing
            val y = height - ((weight.toFloat() - minWeight) / range) * height * 0.8f - (height * 0.1f)
            Offset(x, y)
        }

        clipRect(right = width * animProgress) {
            // Gradient fill path
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                lineTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyanAccent.copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // Line path
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    lineTo(points[i].x, points[i].y)
                }
            }
            drawPath(
                path = linePath,
                color = CyanAccent,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )

            // Points
            points.forEach { point ->
                drawCircle(color = Color.White, radius = 4.dp.toPx(), center = point)
                drawCircle(
                    color = CyanAccent, radius = 4.dp.toPx(), center = point,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun MiniSparkline(
    history: List<Double>,
    modifier: Modifier = Modifier
) {
    val chronologicalHistory = remember(history) { history.reversed() }
    val maxVal = chronologicalHistory.maxOrNull()?.toFloat() ?: 1f
    val minVal = chronologicalHistory.minOrNull()?.toFloat() ?: 0f
    val range = if (maxVal == minVal) 1f else maxVal - minVal

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val spacing = if (chronologicalHistory.size > 1) width / (chronologicalHistory.size - 1) else width

        val points = chronologicalHistory.mapIndexed { index, v ->
            Offset(
                x = index * spacing,
                y = height - ((v.toFloat() - minVal) / range) * height * 0.8f - height * 0.1f
            )
        }

        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            lineTo(points.first().x, points.first().y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
            lineTo(points.last().x, height)
            close()
        }
        drawPath(
            fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(CyanAccent.copy(alpha = 0.4f), Color.Transparent),
                startY = 0f, endY = height
            )
        )

        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) lineTo(points[i].x, points[i].y)
        }
        drawPath(linePath, color = CyanAccent, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.top_exercicios),
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
                PodiumPillar(
                    weight = topExercises.getOrNull(1)?.second?.maxOrNull() ?: 0.0,
                    label = "2nd",
                    heightFraction = 0.65f,
                    exerciseName = topExercises.getOrNull(1)?.first ?: "-",
                    pillarWidth = 48.dp
                )
                PodiumPillar(
                    weight = topExercises.getOrNull(0)?.second?.maxOrNull() ?: 0.0,
                    label = "1st",
                    heightFraction = 0.95f,
                    exerciseName = topExercises.getOrNull(0)?.first ?: "-",
                    isGold = true,
                    pillarWidth = 56.dp
                )
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
fun ExerciseRecordCard(
    exerciseName: String,
    bestValue: Double,
    history: List<Double>,
    isCardio: Boolean = false,
    onClick: () -> Unit
) {
    val displayValue = remember(bestValue, isCardio) {
        if (isCardio) {
            bestValue.toTimeString()
        } else {
            java.text.NumberFormat.getInstance(Locale.getDefault()).apply {
                maximumFractionDigits = 1
                minimumFractionDigits = 0
            }.format(bestValue) + " kg"
        }
    }

    val improvementPct = remember(history) {
        if (history.size >= 2) {
            val oldest = history.last()
            if (oldest > 0) ((history.first() - oldest) / oldest * 100).toInt() else null
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .border(0.1.dp, CyanAccent.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
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
                        text = exerciseName.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = 0.5.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isCardio) stringResource(R.string.maior_tempo_registrado)
                               else stringResource(R.string.maior_recorde_pessoal),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .background(CyanGradient, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = displayValue,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        ),
                        maxLines = 1
                    )
                }
            }

            if (history.size >= 2) {
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
                        // PR count chip
                        Box(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.prs_count, history.size),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            )
                        }

                        // Improvement badge
                        if (improvementPct != null) {
                            val badgeColor = when {
                                improvementPct > 0 -> Color(0xFF4CAF50)
                                improvementPct < 0 -> Color(0xFFE53935)
                                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            }
                            Box(
                                modifier = Modifier
                                    .background(
                                        badgeColor.copy(alpha = 0.15f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${if (improvementPct >= 0) "+" else ""}$improvementPct%",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    // Mini sparkline
                    MiniSparkline(
                        history = history,
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
