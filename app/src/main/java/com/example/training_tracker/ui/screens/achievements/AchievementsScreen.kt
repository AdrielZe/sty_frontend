package com.example.training_tracker.ui.screens.conquistas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.North
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/* ─────────────────────────────────────────────────────────────────────────────
 * Palette (local — intentionally NOT using AppTheme.accent so this screen
 * has its own identity).
 * ───────────────────────────────────────────────────────────────────────────── */
private val Gold1   = Color(0xFFFFD56B)
private val Gold2   = Color(0xFFC98A1A)
private val Silver1 = Color(0xFFD8DDE6)
private val Silver2 = Color(0xFF6F7787)
private val Bronze1 = Color(0xFFD99A6C)
private val Bronze2 = Color(0xFF8C4F25)
private val GreenOk = Color(0xFF4CAF50)

private val GoldBrush = Brush.linearGradient(listOf(Gold1, Gold2))

private fun nf(v: Double): String =
    NumberFormat.getInstance(Locale.getDefault()).apply {
        maximumFractionDigits = 1; minimumFractionDigits = 0
    }.format(v)

/* ═════════════════════════════════════════════════════════════════════════════
 * SCREEN
 * ═════════════════════════════════════════════════════════════════════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    uiState: AchievementsUiState,
    onNavigateBack: () -> Unit,
    onYearSelected: (YearFilter) -> Unit,
    onDaySelected: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Gold1)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Title block
            item {
                TitleBlock(totalPRs = uiState.totalPRs, modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 12.dp))
            }

            // Year tabs
            item {
                YearTabs(
                    selected = uiState.selectedYear,
                    onSelect = onYearSelected,
                    modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 14.dp)
                )
            }

            // Champion
            uiState.champion?.let { ch ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        ChampionCard(ch)
                    }
                }
            }

            // Month heatmap
            uiState.thisMonth?.let { m ->
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
                        MonthHeatmap(
                            m = m,
                            selectedDay = uiState.selectedDay,
                            onDayClick = onDaySelected
                        )
                    }
                }
            }

            // Timeline section
            item {
                val timelineLabel = if (uiState.selectedDay != null && uiState.thisMonth != null) {
                    "DIA ${uiState.selectedDay} · ${uiState.thisMonth.monthLabel.uppercase()}"
                } else {
                    "DIÁRIO DE PRs"
                }
                SectionHeader(label = timelineLabel)
            }
            if (uiState.timeline.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum PR registrado nesse dia.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            } else {
                item {
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Timeline(uiState.timeline)
                    }
                }
            }

            // Medals section
            item { SectionHeader(label = "MEDALHAS") }
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    MedalsLegend()
                }
            }
            item {
                MedalsGrid(
                    medals = uiState.medals,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // Goals section
            item { SectionHeader(label = "QUASE LÁ") }
            item {
                Text(
                    text = "Exercícios nos quais você está próximo de bater um recorde.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 4.dp)
                )
            }
            items(uiState.goals.size, key = { uiState.goals[it].exerciseName }) { i ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                    GoalRow(uiState.goals[i])
                }
            }
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * TITLE
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun TitleBlock(totalPRs: Int, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "CONQUISTAS",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Gold1,
                letterSpacing = 3.2.sp,
                fontSize = 9.5.sp
            )
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "$totalPRs",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 30.sp,
                    letterSpacing = (-1).sp
                )
            )
            Text(
                text = "recordes pessoais",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * YEAR TABS
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun YearTabs(
    selected: YearFilter,
    onSelect: (YearFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        YearFilter.values().forEach { yf ->
            val active = yf == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .then(if (active) Modifier.background(GoldBrush) else Modifier)
                    .border(
                        1.dp,
                        if (active) Color.Transparent
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        RoundedCornerShape(100.dp)
                    )
                    .clickable { onSelect(yf) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = yf.label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (active) Color(0xFF08131E)
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        letterSpacing = 1.4.sp,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * SECTION HEADER ─── LABEL ───
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun SectionHeader(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                        Color.Transparent
                    )
                )
            ))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Gold1,
                letterSpacing = 3.2.sp,
                fontSize = 10.sp
            )
        )
        Box(modifier = Modifier
            .weight(1f)
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                        Color.Transparent
                    )
                )
            ))
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * CHAMPION CARD
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun ChampionCard(ch: Champion) {
    val df = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 320.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Gold1.copy(alpha = 0.30f), RoundedCornerShape(28.dp))
            .background(Color(0xFF0A0A10))
    ) {
        // background image
        ch.image?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize().alpha(0.45f)
            )
        }
        // accent glow
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.radialGradient(
                    colors = listOf(Gold1.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(Float.POSITIVE_INFINITY, 0f),
                    radius = 800f
                )
            )
        )
        // dark scrim
        Box(
            modifier = Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF08080E).copy(alpha = 0.25f),
                        Color(0xFF08080E).copy(alpha = 0.55f),
                        Color(0xFF08080E).copy(alpha = 0.92f),
                        Color(0xFF08080E).copy(alpha = 0.96f)
                    )
                )
            )
        )

        // Ribbon (rotated, gold, top-right)
        Ribbon(
            text = "REINANDO",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 34.dp, y = 14.dp)
                .rotate(40f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // eyebrow
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ch.muscleGroup?.let {
                    Text(
                        stringResource(it.resId).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Gold1,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.4.sp,
                            fontSize = 9.5.sp
                        )
                    )
                }
                Box(modifier = Modifier.size(14.dp, 1.dp).background(Color.White.copy(alpha = 0.35f)))
                Text(
                    text = ch.achievedOn.format(df),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.55f),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.4.sp,
                        fontSize = 9.5.sp
                    )
                )
            }

            // exercise name
            Text(
                text = ch.exerciseName,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 28.sp,
                    letterSpacing = (-0.7).sp,
                    lineHeight = 30.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(0.8f)
            )

            Spacer(Modifier.weight(1f, fill = false).height(8.dp))

            // big number + delta
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Gradient text via TextStyle.brush
                    Text(
                        text = nf(ch.weightKg),
                        style = androidx.compose.ui.text.TextStyle(
                            brush = Brush.linearGradient(listOf(Color.White, Gold1)),
                            fontWeight = FontWeight.Black,
                            fontSize = 72.sp,
                            letterSpacing = (-3.5).sp
                        )
                    )
                    Text(
                        text = "kg",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Gold1,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(Gold1.copy(alpha = 0.16f))
                            .border(1.dp, Gold1.copy(alpha = 0.30f), RoundedCornerShape(100.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            Icons.Default.North,
                            contentDescription = null,
                            tint = Gold1,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "+${nf(ch.deltaKg)} kg",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = Gold1,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = (-0.1).sp
                            )
                        )
                    }
                    Text(
                        text = "de ${nf(ch.previousWeightKg)} kg",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White.copy(alpha = 0.50f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // Footer
            Spacer(Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.padding(top = 4.dp),
                thickness = 0.7.dp,
                color = Gold1.copy(alpha = 0.20f)
            )
            Spacer(Modifier.height(2.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Gold1, modifier = Modifier.size(11.dp))
                    Text(
                        "MAIOR PESO ABSOLUTO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Gold1,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            fontSize = 9.5.sp
                        )
                    )
                }
                Text(
                    text = "Sessão ${ch.sessionNumber} · há ${daysAgo(ch.achievedOn)} dias",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp
                    )
                )
            }
        }
    }
}

/* Diagonal gold ribbon banner */
@Composable
private fun Ribbon(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(140.dp)
            .background(GoldBrush)
            .padding(vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFF08131E), modifier = Modifier.size(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF08131E),
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 3.sp,
                    fontSize = 9.sp
                )
            )
        }
    }
}

private fun daysAgo(date: java.time.LocalDate): Int =
    java.time.temporal.ChronoUnit.DAYS.between(date, java.time.LocalDate.now()).toInt().coerceAtLeast(0)

/* ═════════════════════════════════════════════════════════════════════════════
 * MONTH HEATMAP
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun MonthHeatmap(
    m: MonthStats,
    selectedDay: Int? = null,
    onDayClick: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = m.monthLabel,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 0.6.sp,
                        fontSize = 18.sp
                    )
                )
                Text(
                    text = "ESTE MÊS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 2.4.sp,
                        fontSize = 9.5.sp
                    )
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                MonthStatCell(value = "${m.prCount}",                 label = "PRs")
                MonthStatCell(value = "${m.exerciseCount}",           label = "Exerc.")
                MonthStatCell(value = "↑${m.volumeDeltaPct}%",        label = "Volume", highlight = true)
            }
        }
        Spacer(Modifier.height(14.dp))

        // 7-column grid of day cells
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 360.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            userScrollEnabled = false
        ) {
            items(m.daysInMonth) { idx ->
                val day = idx + 1
                val isPr = m.prDays.contains(day)
                val isToday = day == m.today
                val isFuture = day > m.today
                val isSelected = day == selectedDay
                HeatCell(
                    day = day,
                    isPr = isPr,
                    isToday = isToday,
                    isFuture = isFuture,
                    isSelected = isSelected,
                    onClick = { if (!isFuture) onDayClick(day) }
                )
            }
        }
    }
}

@Composable
private fun MonthStatCell(value: String, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = if (highlight) GreenOk else MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.2).sp,
                fontSize = 14.sp
            )
        )
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                letterSpacing = 1.6.sp,
                fontSize = 9.sp
            )
        )
    }
}

@Composable
private fun HeatCell(
    day: Int,
    isPr: Boolean,
    isToday: Boolean,
    isFuture: Boolean,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val cellShape = RoundedCornerShape(8.dp)

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(cellShape)
            .then(
                when {
                    isSelected && isPr -> Modifier.background(GoldBrush)
                    isSelected         -> Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    isPr               -> Modifier.background(GoldBrush)
                    isFuture           -> Modifier
                    else               -> Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                }
            )
            .then(
                when {
                    isSelected -> Modifier.border(2.dp, MaterialTheme.colorScheme.primary, cellShape)
                    isToday && isPr -> Modifier.border(1.5.dp, Color.White, cellShape)
                    isToday    -> Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary, cellShape)
                    isFuture   -> Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f), cellShape)
                    else       -> Modifier
                }
            )
            .then(if (!isFuture) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Text(
            text = "$day",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = when {
                    isSelected         -> MaterialTheme.colorScheme.primary
                    isPr               -> Color(0xFF08131E).copy(alpha = 0.65f)
                    isFuture           -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    else               -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                },
                fontSize = 9.5.sp
            ),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 2.dp)
        )
    }
}

/* ═════════════════════════════════════════════════════════════════════════════
 * TIMELINE
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun Timeline(entries: List<PrEntry>) {
    // Group by month label
    val grouped = remember(entries) {
        val out = mutableListOf<Any>()
        var lastMonth: String? = null
        entries.forEach { e ->
            val label = e.date.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale("pt", "BR")).uppercase()
            if (label != lastMonth) {
                out.add("HEADER:$label")
                lastMonth = label
            }
            out.add(e)
        }
        out
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Vertical rail
        Box(
            modifier = Modifier
                .padding(start = 44.dp)
                .fillMaxHeight()
                .width(1.5.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            Gold2,
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    )
                )
        )
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
            grouped.forEach { item ->
                when (item) {
                    is String -> {
                        if (item.startsWith("HEADER:")) {
                            TimelineMonthHeader(item.removePrefix("HEADER:"))
                        }
                    }
                    is PrEntry -> TimelineEntry(item)
                }
            }
        }
    }
}

@Composable
private fun TimelineMonthHeader(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(44.dp - 4.dp))
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(Gold1)
                .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
        )
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Gold1,
                letterSpacing = 2.4.sp,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun TimelineEntry(e: PrEntry) {
    val df = remember { DateTimeFormatter.ofPattern("dd", Locale("pt", "BR")) }
    val mf = remember { DateTimeFormatter.ofPattern("LLL", Locale("pt", "BR")) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Date column (right-aligned within 44dp)
        Box(modifier = Modifier.width(44.dp).padding(top = 6.dp), contentAlignment = Alignment.TopEnd) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    e.date.format(df),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.7).sp,
                        fontSize = 18.sp
                    )
                )
                Text(
                    e.date.format(mf).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 2.sp,
                        fontSize = 9.sp
                    )
                )
            }
        }
        Spacer(Modifier.width(0.dp))

        // Node (positioned on rail)
        if (e.isCrown) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(GoldBrush)
                    .border(1.5.dp, Gold1, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = Color(0xFF08131E),
                    modifier = Modifier.size(11.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .offset(x = (-4).dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
            )
        }

        Spacer(Modifier.width(12.dp))

        // Card
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(14.dp))
                .then(
                    if (e.isCrown) Modifier.border(1.dp, Gold1.copy(alpha = 0.30f), RoundedCornerShape(14.dp))
                    else Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
                )
                .background(
                    if (e.isCrown) Brush.linearGradient(
                        listOf(Gold1.copy(alpha = 0.06f), MaterialTheme.colorScheme.surface)
                    )
                    else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surface))
                )
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    e.exerciseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.2).sp,
                        fontSize = 14.sp
                    ),
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                DeltaPill(deltaKg = e.deltaKg, isCrown = e.isCrown)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                e.muscleGroup?.let {
                    Text(
                        stringResource(it.resId).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp,
                            fontSize = 9.5.sp
                        )
                    )
                    Dot()
                }
                Text(
                    text = "${nf(e.previousWeightKg)} → ",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        fontSize = 11.5.sp
                    )
                )
                Text(
                    text = "${nf(e.weightKg)} kg",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.5.sp
                    )
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "Sessão ${e.sessionNumber}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
                Dot()
                Text(
                    e.workoutName,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun DeltaPill(deltaKg: Double, isCrown: Boolean) {
    val bg = if (isCrown) Gold1.copy(alpha = 0.16f) else GreenOk.copy(alpha = 0.14f)
    val fg = if (isCrown) Gold1 else GreenOk
    val border = if (isCrown) Gold1.copy(alpha = 0.30f) else GreenOk.copy(alpha = 0.30f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(100.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Icon(Icons.Default.North, contentDescription = null, tint = fg, modifier = Modifier.size(10.dp))
        Text(
            "+${nf(deltaKg)}",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = fg,
                fontSize = 11.sp,
                letterSpacing = (-0.1).sp
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
 * MEDALS
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun MedalsLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)
    ) {
        LegendDot(color = Gold1,   label = "Ouro",   sub = "≥ 100kg")
        LegendDot(color = Silver1, label = "Prata", sub = "≥ 50kg")
        LegendDot(color = Bronze1, label = "Bronze", sub = "< 50kg")
    }
}

@Composable
private fun LegendDot(color: Color, label: String, sub: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    letterSpacing = 1.4.sp,
                    fontSize = 9.5.sp
                )
            )
            Text(
                sub,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 9.sp
                )
            )
        }
    }
}

@Composable
private fun MedalsGrid(medals: List<Medal>, modifier: Modifier = Modifier) {
    if (medals.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Você ainda não tem nenhuma medalha.\nVolte aqui depois de realizar seus treinos.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            )
        }
        return
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        medals.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                row.forEach { m ->
                    Box(modifier = Modifier.weight(1f)) { MedalView(m) }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MedalView(m: Medal) {
    val (ring1, ring2, ribbon1, ribbon2, abbrColor, ribbonTextColor) = when (m.tier) {
        MedalTier.GOLD   -> sixOf(Gold1,   Gold2,   Gold1,   Gold2,   Color(0xFF08131E), Color(0xFF08131E))
        MedalTier.SILVER -> sixOf(Silver1, Silver2, Silver1, Silver2, Color(0xFF08131E), Color(0xFF08131E))
        MedalTier.BRONZE -> sixOf(Bronze1, Bronze2, Bronze1, Bronze2, Color.White,        Color.White)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // disc
        Box(
            modifier = Modifier.size(96.dp).offset(y = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            // ridged ring via sweep gradient with many alternating stops
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stops = buildList<Pair<Float, Color>> {
                    val n = 60
                    for (i in 0..n) {
                        val t = i / n.toFloat()
                        val c = if (i % 2 == 0) ring1 else ring2
                        add(t to c)
                    }
                }.toTypedArray()
                drawCircle(brush = Brush.sweepGradient(*stops))
            }
            // inner cap (image + tint)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(ring1, ring2)))
            ) {
                m.image?.let {
                    Image(
                        painter = painterResource(it),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.45f)
                    )
                }
                // gloss
                Box(
                    modifier = Modifier.matchParentSize().background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.40f), Color.Transparent),
                            center = Offset(28f, 22f),
                            radius = 60f
                        )
                    )
                )
                Text(
                    m.abbreviation,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = abbrColor,
                        fontSize = 24.sp,
                        letterSpacing = 0.4.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            // tier mark
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ring1, modifier = Modifier.size(10.dp))
            }
        }

        // ribbon
        Spacer(Modifier.height(0.dp))
        Box(
            modifier = Modifier
                .offset(y = (-12).dp)
                .clip(RibbonShape)
                .background(Brush.linearGradient(listOf(ribbon1, ribbon2)))
                .padding(horizontal = 18.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    nf(m.weightKg),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = ribbonTextColor,
                        letterSpacing = (-0.3).sp,
                        fontSize = 15.sp
                    )
                )
                Text(
                    "kg",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = ribbonTextColor.copy(alpha = 0.75f),
                        fontSize = 9.5.sp
                    ),
                    modifier = Modifier.padding(bottom = 1.dp)
                )
            }
        }

        Text(
            m.exerciseName,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = (-0.1).sp,
                fontSize = 12.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        m.muscleGroup?.let {
            Text(
                stringResource(it.resId).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                    letterSpacing = 2.sp,
                    fontSize = 9.sp
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** Ribbon with chevron cutout left + right (matches the HTML clip-path). */
private val RibbonShape: Shape = GenericShape { size: Size, _: LayoutDirection ->
    val midY = size.height / 2f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width * 0.92f, midY)
    lineTo(size.width, size.height)
    lineTo(0f, size.height)
    lineTo(size.width * 0.08f, midY)
    close()
}

/* small helper because we have >5 colors and Kotlin can't destructure a List */
private data class Six<A>(val a: A, val b: A, val c: A, val d: A, val e: A, val f: A)
private fun <A> sixOf(a: A, b: A, c: A, d: A, e: A, f: A) = Six(a, b, c, d, e, f)
private operator fun <A> Six<A>.component1() = a
private operator fun <A> Six<A>.component2() = b
private operator fun <A> Six<A>.component3() = c
private operator fun <A> Six<A>.component4() = d
private operator fun <A> Six<A>.component5() = e
private operator fun <A> Six<A>.component6() = f

/* ═════════════════════════════════════════════════════════════════════════════
 * GOALS
 * ═════════════════════════════════════════════════════════════════════════════ */

@Composable
private fun GoalRow(goal: Goal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    goal.exerciseName,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        letterSpacing = (-0.1).sp
                    )
                )
                goal.muscleGroup?.let {
                    Text(
                        stringResource(it.resId).uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp,
                            fontSize = 9.5.sp
                        )
                    )
                }
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f), modifier = Modifier.size(11.dp))
                Text(
                    goal.etaLabel,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontSize = 10.5.sp
                    )
                )
            }
        }

        // bar
        GoalBar(progressPct = goal.progressPct)

        // footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                buildString { append(nf(goal.currentKg)); append(" kg") },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 11.sp
                )
            )
            Text(
                "${goal.progressPct}%",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Gold1,
                    fontSize = 12.sp,
                    letterSpacing = (-0.1).sp
                )
            )
            Text(
                buildString { append(nf(goal.targetKg)); append(" kg") },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
private fun GoalBar(progressPct: Int) {
    val pct = progressPct.coerceIn(0, 100) / 100f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
    ) {
        // track
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(100.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        )
        // fill
        Box(
            modifier = Modifier
                .fillMaxWidth(pct)
                .height(8.dp)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(100.dp))
                .background(Brush.horizontalGradient(listOf(Gold2, Gold1)))
        )
        // marker dot
        Box(
            modifier = Modifier
                .fillMaxWidth(pct)
                .align(Alignment.CenterStart),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Gold1)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
            )
        }
    }
}
