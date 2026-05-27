package com.example.training_tracker.ui.screens.workout_report

/*
 * WorkoutReportScreen — the post-workout feedback the user sees after finalizing
 * a session. Beautiful, dense, celebratory. Drop-in: takes a finished Workout
 * plus an optional comparison payload (deltas vs previous session + PRs) and a
 * couple of callbacks. Does NOT mutate state, does NOT call viewmodels — every
 * piece of data flows in via parameters so existing logic stays intact.
 *
 * Wire it up in your NavHost like:
 *
 *     composable("workout_report/{id}") { backStackEntry ->
 *         val workout = viewModel.lastFinishedWorkout
 *         WorkoutReportScreen(
 *             workout = workout,
 *             previous = viewModel.previousWorkoutComparison(workout),
 *             prs      = viewModel.detectedPRs(workout),
 *             onClose  = { navController.popBackStack("home", inclusive = false) },
 *             onShare  = { /* TODO */ }
 *         )
 *     }
 *
 * Visual language matches HomeScreen.kt / WorkoutScreenV2.kt — same Sty tokens,
 * same accent (AppTheme.accent), same surface chrome.
 */

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.FileProvider
import androidx.core.view.drawToBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.ui.theme.AppTheme
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// =============================================================================
//  Helpers
// =============================================================================

private fun muscleImage(group: MuscleGroups?): Int = when (group) {
    MuscleGroups.CHEST       -> com.example.training_tracker.R.drawable.chest
    MuscleGroups.BACK        -> com.example.training_tracker.R.drawable.back
    MuscleGroups.TRICEPS     -> com.example.training_tracker.R.drawable.triceps
    MuscleGroups.BICEPS      -> com.example.training_tracker.R.drawable.biceps
    MuscleGroups.SHOULDERS   -> com.example.training_tracker.R.drawable.shoulders
    MuscleGroups.QUADRICEPS  -> com.example.training_tracker.R.drawable.legs
    MuscleGroups.HAMSTRINGS  -> com.example.training_tracker.R.drawable.legs
    MuscleGroups.CALF        -> com.example.training_tracker.R.drawable.legs
    MuscleGroups.GLUTE       -> com.example.training_tracker.R.drawable.legs
    MuscleGroups.ABS         -> com.example.training_tracker.R.drawable.abs
    MuscleGroups.CARDIO      -> com.example.training_tracker.R.drawable.cardio
    MuscleGroups.STRETCHING  -> com.example.training_tracker.R.drawable.stretching
    null                     -> com.example.training_tracker.R.drawable.workout
}

// =============================================================================
//  Tokens — mirror the Sty palette used in HomeScreen.kt / WorkoutScreenV2.kt
// =============================================================================
private object RepSty {
    val BgStart = Color(0xFF131319)
    val BgEnd = Color(0xFF0C0C11)
    val Surface = Color(0xFF181820)
    val Surface2 = Color(0xFF1F1F29)
    val Border = Color.White.copy(alpha = 0.07f)
    val BorderStrong = Color.White.copy(alpha = 0.14f)
    val TextMain = Color(0xFFECECF3)
    val TextDim = Color(0xFF9A9AAA)
    val TextFaint = Color(0xFF61616F)
    val OnAccent = Color(0xFF08131E)
    val Green = Color(0xFF5AD479)
    val Coral = Color(0xFFFF7A6B)
    val Gold1 = Color(0xFFFFD56B)
    val Gold2 = Color(0xFFC98A1A)
    val Gold = Color(0xFFFFC46B)

    val BgBrush = Brush.verticalGradient(listOf(BgStart, BgEnd))
    val GoldGrad = Brush.linearGradient(listOf(Gold1, Gold2))
}

// =============================================================================
//  Public data shapes — caller fills these from its viewmodel. Optional.
// =============================================================================

/** A single new personal record set during this workout. */
data class WorkoutReportPR(
    val exerciseName: String,
    val muscleGroup: MuscleGroups?,
    val previousKg: Double,
    val newKg: Double,
)

/** Comparison values vs the previous time this workout was performed. */
data class WorkoutReportDeltas(
    val volumePct: Int? = null,
    val setsPct: Int? = null,
    val exercisesPct: Int? = null,
    val repsPct: Int? = null,
)

/** Optional comparison payload — pass null on first run. */
data class WorkoutReportContext(
    val deltas: WorkoutReportDeltas = WorkoutReportDeltas(),
    val prs: List<WorkoutReportPR> = emptyList(),
    val completedAt: LocalDateTime = LocalDateTime.now(),
)

// =============================================================================
//  Computed report stats (derived locally — no viewmodel needed)
// =============================================================================
private data class TopSet(
    val exerciseName: String,
    val muscleGroup: MuscleGroups?,
    val weightKg: Double,
    val reps: Int,
    val setNumber: Int,
)

private data class MuscleSlice(val group: MuscleGroups?, val volume: Double, val pct: Int)

private data class DerivedStats(
    val totalSets: Int,
    val completedSets: Int,
    val totalReps: Int,
    val totalVolume: Double,
    val durationMs: Long,
    val exerciseCount: Int,
    val top: TopSet?,
    val muscleDist: List<MuscleSlice>,
)

private fun deriveStats(exercises: List<Exercise>, durationMs: Long): DerivedStats {
    val exs = exercises
    val totalSets = exs.sumOf { it.exerciseSets.size }
    val completedSets = exs.sumOf { ex -> ex.exerciseSets.count { it.isCompleted } }
    val totalReps = exs.sumOf { ex ->
        ex.exerciseSets.sumOf { s -> if (s.isCompleted) (s.reps.toIntOrNull() ?: 0) else 0 }
    }
    val totalVolume = exs.sumOf { ex ->
        ex.exerciseSets.sumOf { s ->
            if (s.isCompleted) (s.weight.toDoubleOrNull() ?: 0.0) * (s.reps.toIntOrNull() ?: 0)
            else 0.0
        }
    }

    var top: TopSet? = null
    exs.forEach { ex ->
        ex.exerciseSets.forEachIndexed { idx, s ->
            if (!s.isCompleted) return@forEachIndexed
            val w = s.weight.toDoubleOrNull() ?: 0.0
            val r = s.reps.toIntOrNull() ?: 0
            val cur = top
            if (w > 0 && (cur == null || w > cur.weightKg || (w == cur.weightKg && r > cur.reps))) {
                top = TopSet(ex.name, ex.muscleGroup, w, r, idx + 1)
            }
        }
    }

    val byMuscle = mutableMapOf<MuscleGroups?, Double>()
    exs.forEach { ex ->
        val v = ex.exerciseSets.sumOf { s ->
            if (s.isCompleted) (s.weight.toDoubleOrNull() ?: 0.0) * (s.reps.toIntOrNull() ?: 0)
            else 0.0
        }
        if (v > 0) byMuscle[ex.muscleGroup] = (byMuscle[ex.muscleGroup] ?: 0.0) + v
    }
    val muscleTotal = byMuscle.values.sum().coerceAtLeast(1.0)
    val muscleDist = byMuscle.entries
        .map { (g, v) -> MuscleSlice(g, v, ((v / muscleTotal) * 100).roundToInt()) }
        .sortedByDescending { it.volume }

    val duration: Long = durationMs

    val exerciseCount = exs.count { ex -> ex.exerciseSets.any { it.isCompleted } }

    return DerivedStats(
        totalSets = totalSets,
        completedSets = completedSets,
        totalReps = totalReps,
        totalVolume = totalVolume,
        durationMs = duration,
        exerciseCount = exerciseCount,
        top = top,
        muscleDist = muscleDist,
    )
}

private fun fmtClock(ms: Long): String {
    val total = (ms / 1000L).coerceAtLeast(0L)
    val h = total / 3600L
    val m = (total % 3600L) / 60L
    val s = total % 60L
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun fmtInt(v: Double): String =
    String.format(Locale("pt", "BR"), "%,d", v.roundToInt()).replace(',', '.')

private fun fmtKg(v: Double): String =
    if (v % 1.0 == 0.0) v.toInt().toString() else "%.1f".format(Locale.getDefault(), v)

/** Converte string "ss", "mm:ss" ou "hh:mm:ss" em segundos totais. */
private fun parseTimeToSeconds(time: String?): Int {
    if (time.isNullOrBlank()) return 0
    val parts = time.trim().split(":").mapNotNull { it.toIntOrNull() }
    return when (parts.size) {
        1 -> parts[0]                                    // só segundos (alongamento)
        2 -> parts[0] * 60 + parts[1]                   // mm:ss ou hh:mm
        3 -> parts[0] * 3600 + parts[1] * 60 + parts[2] // hh:mm:ss
        else -> 0
    }
}

/** Formata segundos totais em "mm:ss" ou "h:mm:ss" se >= 1 hora. */
private fun fmtSeconds(total: Int): String {
    if (total <= 0) return "—"
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}

private fun muscleLabel(g: MuscleGroups?): String = when (g) {
    MuscleGroups.CHEST -> "PEITO"
    MuscleGroups.BACK -> "COSTAS"
    MuscleGroups.QUADRICEPS -> "QUADRÍCEPS"
    MuscleGroups.HAMSTRINGS -> "POSTERIOR"
    MuscleGroups.CALF -> "PANTURRILHA"
    MuscleGroups.GLUTE -> "GLÚTEO"
    MuscleGroups.SHOULDERS -> "OMBROS"
    MuscleGroups.ABS -> "ABDÔMEN"
    MuscleGroups.BICEPS -> "BÍCEPS"
    MuscleGroups.TRICEPS -> "TRÍCEPS"
    MuscleGroups.CARDIO -> "CARDIO"
    MuscleGroups.STRETCHING -> "ALONGAMENTO"
    null -> "—"
    else -> g.name
}

// =============================================================================
//  Public entry point
// =============================================================================
@Composable
fun WorkoutReportScreen(
    uiState: WorkoutReportUiState,
    onClose: () -> Unit,
    onShare: () -> Unit = {},
) {
    val durationMs = uiState.totalMinutes * 60_000L
    val stats = remember(uiState.exercises, durationMs) { deriveStats(uiState.exercises, durationMs) }
    var feeling by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val handleShare: () -> Unit = {
        scope.launch(Dispatchers.IO) {
            try {
                val bitmap = withContext(Dispatchers.Main) {
                    view.drawToBitmap(Bitmap.Config.ARGB_8888)
                }
                val imagesDir = File(context.cacheDir, "images")
                imagesDir.mkdirs()
                val imageFile = File(imagesDir, "workout_report.png")
                imageFile.outputStream().use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    imageFile
                )
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(
                    shareIntent,
                    context.getString(com.example.training_tracker.R.string.relatorio_compartilhar_titulo)
                )
                withContext(Dispatchers.Main) {
                    context.startActivity(chooser)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        containerColor = RepSty.BgEnd,
        topBar = {
            ReportTopBar(onClose = onClose, onShare = handleShare)
        },
        bottomBar = {
            ReportFooter(onClose = onClose, onShare = handleShare)
        }
    ) { inner ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(RepSty.BgBrush)
        ) {
            // soft accent haze top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                AppTheme.accent.light.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = inner.calculateTopPadding(), bottom = inner.calculateBottomPadding()),
                contentPadding = PaddingValues(top = 0.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                item {
                    HeroBlock(
                        workoutName = uiState.workoutName.ifBlank { "Treino" },
                        subtitle = uiState.exercises
                            .mapNotNull { it.muscleGroup }
                            .distinct()
                            .take(2)
                            .joinToString(" & ") {
                                muscleLabel(it).lowercase().replaceFirstChar { c -> c.uppercase() }
                            },
                        completedAt = if (uiState.completionDate != null && uiState.completionTime != null)
                            uiState.completionDate.atTime(uiState.completionTime)
                        else
                            LocalDateTime.now(),
                        durationMs = stats.durationMs
                    )
                }
                item {
                    StatGrid(stats = stats, deltas = WorkoutReportDeltas(), isFirst = true)
                }
                item {
                    CaloriesBlock(caloriesBurned = uiState.caloriesBurned)
                }
                if (stats.top != null) {
                    item { TopLiftCard(top = stats.top) }
                }
                if (stats.muscleDist.isNotEmpty()) {
                    item { MuscleDistribution(slices = stats.muscleDist) }
                }
                if (uiState.sessionPRs.isNotEmpty()) {
                    item { SessionPRBlock(prs = uiState.sessionPRs) }
                }
                if (uiState.exercises.isNotEmpty()) {
                    item { ExerciseRollup(exercises = uiState.exercises) }
                }
                item {
                    FeelingSection(value = feeling, onChange = { feeling = it })
                }
                item {
                    Text(
                        text = "\u201CCada série que você termina compõe a versão futura de você.\u201D",
                        color = RepSty.TextDim,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp, vertical = 24.dp)
                    )
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}

private fun WorkoutReportDeltas.hasAny() =
    volumePct != null || setsPct != null || exercisesPct != null || repsPct != null

// =============================================================================
//  Top bar
// =============================================================================
@Composable
private fun ReportTopBar(onClose: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconCircle(icon = Icons.AutoMirrored.Filled.ArrowBack, onClick = onClose)
        Spacer(Modifier.weight(1f))
        Text(
            "Relatório do treino",
            color = RepSty.TextDim,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 0.4.sp
        )
        Spacer(Modifier.weight(1f))
        IconCircle(icon = Icons.Filled.IosShare, onClick = onShare)
    }
}

@Composable
private fun IconCircle(icon: ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(RepSty.Surface)
            .border(BorderStroke(1.dp, RepSty.Border), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = RepSty.TextMain, modifier = Modifier.size(18.dp))
    }
}

// =============================================================================
//  Hero
// =============================================================================
@Composable
private fun HeroBlock(
    workoutName: String,
    subtitle: String,
    completedAt: LocalDateTime,
    durationMs: Long,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(top = 6.dp, bottom = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Seal with rings
        Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
            val transition = rememberInfiniteTransition(label = "rings")
            val r1 by transition.animateFloat(
                initialValue = 0.92f, targetValue = 1.20f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "r1"
            )
            val r1Alpha by transition.animateFloat(
                initialValue = 0.55f, targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2400, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "r1a"
            )
            Box(
                modifier = Modifier
                    .size((120.dp.value * r1).dp)
                    .alpha(r1Alpha)
                    .border(1.dp, AppTheme.accent.light, CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .shadow(elevation = 20.dp, shape = CircleShape, spotColor = AppTheme.accent.light)
                    .clip(CircleShape)
                    .background(AppTheme.accent.gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = RepSty.OnAccent,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Text(
            "TREINO CONCLUÍDO",
            color = AppTheme.accent.light,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.4.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            workoutName,
            color = RepSty.TextMain,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            val dateLabel = remember(completedAt) {
                val months = listOf("JAN","FEV","MAR","ABR","MAI","JUN","JUL","AGO","SET","OUT","NOV","DEZ")
                "%d %s · %02d:%02d".format(
                    completedAt.dayOfMonth,
                    months[completedAt.monthValue - 1],
                    completedAt.hour,
                    completedAt.minute
                )
            }
            Text(
                "$subtitle · $dateLabel",
                color = RepSty.TextDim,
                fontSize = 12.sp,
                letterSpacing = 0.2.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                fmtClock(durationMs),
                color = RepSty.TextMain,
                fontSize = 56.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2).sp
            )
            Text(
                "TOTAL",
                color = RepSty.TextFaint,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.6.sp,
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }
    }
}

// =============================================================================
//  Stat grid
// =============================================================================
@Composable
private fun StatGrid(
    stats: DerivedStats,
    deltas: WorkoutReportDeltas,
    isFirst: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCell(
                modifier = Modifier.weight(1f),
                label = "VOLUME",
                value = fmtInt(stats.totalVolume),
                unit = "kg",
                deltaPct = deltas.volumePct,
                isFirst = isFirst,
                accent = true
            )
            StatCell(
                modifier = Modifier.weight(1f),
                label = "SÉRIES",
                value = stats.completedSets.toString(),
                unit = null,
                deltaPct = deltas.setsPct,
                isFirst = isFirst,
                accent = false
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCell(
                modifier = Modifier.weight(1f),
                label = "EXERCÍCIOS",
                value = stats.exerciseCount.toString(),
                unit = null,
                deltaPct = deltas.exercisesPct,
                isFirst = isFirst
            )
            StatCell(
                modifier = Modifier.weight(1f),
                label = "REPS TOTAIS",
                value = stats.totalReps.toString(),
                unit = null,
                deltaPct = deltas.repsPct,
                isFirst = isFirst,
                accent = false
            )
        }
    }
}

@Composable
private fun StatCell(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    unit: String? = null,
    deltaPct: Int?,
    isFirst: Boolean,
    accent: Boolean = false,
) {
    val shape = RoundedCornerShape(18.dp)
    val accentBg = if (accent) {
        Brush.radialGradient(
            colors = listOf(AppTheme.accent.light.copy(alpha = 0.16f), Color.Transparent),
            radius = 240f
        )
    } else {
        Brush.linearGradient(listOf(RepSty.Surface, RepSty.Surface))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(RepSty.Surface)
            .background(accentBg)
            .border(
                BorderStroke(1.dp,
                    if (accent) AppTheme.accent.light.copy(alpha = 0.22f) else RepSty.Border
                ),
                shape
            )
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Column {
            Text(
                label,
                color = RepSty.TextDim,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.2.sp
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    value,
                    color = RepSty.TextMain,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.7).sp
                )
                if (unit != null) {
                    Spacer(Modifier.width(4.dp))
                    Text(
                        unit,
                        color = RepSty.TextDim,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (!isFirst && deltaPct != null) {
                DeltaPill(pct = deltaPct, isFirst = false)
            }
        }
    }
}

@Composable
private fun DeltaPill(pct: Int?, isFirst: Boolean) {
    val (bg, fg, border, icon, label) = when {
        isFirst || pct == null -> Quint(
            Color.White.copy(alpha = 0.04f), RepSty.TextFaint, RepSty.Border, null,
            if (isFirst) "primeiro treino" else "—"
        )
        pct > 0 -> Quint(
            RepSty.Green.copy(alpha = 0.10f), RepSty.Green,
            RepSty.Green.copy(alpha = 0.22f), Icons.Filled.TrendingUp,
            "+$pct% vs último"
        )
        pct < 0 -> Quint(
            RepSty.Coral.copy(alpha = 0.10f), RepSty.Coral,
            RepSty.Coral.copy(alpha = 0.22f), Icons.Filled.TrendingDown,
            "$pct% vs último"
        )
        else -> Quint(
            Color.White.copy(alpha = 0.04f), RepSty.TextDim, RepSty.Border, null,
            "igual ao último"
        )
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(bg)
            .border(BorderStroke(1.dp, border), RoundedCornerShape(100.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(3.dp))
        }
        Text(label, color = fg, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
    }
}

private data class Quint(
    val a: Color, val b: Color, val c: Color, val d: ImageVector?, val e: String
)

// =============================================================================
//  Top lift
// =============================================================================
@Composable
private fun TopLiftCard(top: TopSet) {
    SectionFrame(label = "PICO DO TREINO", icon = Icons.Filled.Bolt, accent = true) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(listOf(RepSty.Surface, RepSty.Surface))
                )
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AppTheme.accent.light.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        radius = 360f
                    )
                )
                .border(
                    BorderStroke(1.dp, AppTheme.accent.light.copy(alpha = 0.22f)),
                    RoundedCornerShape(22.dp)
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "SUA SÉRIE MAIS PESADA",
                    color = AppTheme.accent.light,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.4.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    top.exerciseName,
                    color = RepSty.TextMain,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.2).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    muscleLabel(top.muscleGroup),
                    color = RepSty.TextFaint,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.6.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        fmtKg(top.weightKg),
                        color = RepSty.TextMain,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                    Text(" kg ", color = RepSty.TextDim, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                    Text("× ", color = RepSty.TextDim, fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 6.dp))
                    Text(
                        top.reps.toString(),
                        color = RepSty.TextMain,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(" reps", color = RepSty.TextDim, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
                }
                Text(
                    "na ${top.setNumber}ª série",
                    color = RepSty.TextFaint,
                    fontSize = 11.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(12.dp, CircleShape, spotColor = AppTheme.accent.light)
                    .clip(CircleShape)
                    .background(AppTheme.accent.gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = RepSty.OnAccent,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }
}

// =============================================================================
//  PRs
// =============================================================================
@Composable
private fun PRBlock(prs: List<WorkoutReportPR>) {
    SectionFrame(
        label = "NOVOS RECORDES",
        icon = Icons.Filled.EmojiEvents,
        gold = true,
        countBadge = prs.size
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            RepSty.Gold.copy(alpha = 0.10f),
                            RepSty.Gold.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
                .background(RepSty.Surface.copy(alpha = 0.6f))
                .border(
                    BorderStroke(1.dp, RepSty.Gold.copy(alpha = 0.28f)),
                    RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            prs.forEachIndexed { idx, pr ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .then(
                            if (idx != prs.lastIndex)
                                Modifier.drawBehind {
                                    drawBehindRowDivider()
                                }
                            else Modifier
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(6.dp, CircleShape, spotColor = RepSty.Gold)
                            .clip(CircleShape)
                            .background(RepSty.GoldGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFF2D1F06),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            pr.exerciseName,
                            color = RepSty.TextMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${fmtKg(pr.previousKg)} kg",
                                color = RepSty.TextFaint,
                                fontSize = 11.5.sp,
                                modifier = Modifier
                                    .drawBehind {
                                        val y = size.height / 2f + 1.dp.toPx()
                                        drawLine(
                                            color = RepSty.TextFaint,
                                            start = androidx.compose.ui.geometry.Offset(0f, y),
                                            end = androidx.compose.ui.geometry.Offset(size.width, y),
                                            strokeWidth = 1.dp.toPx()
                                        )
                                    }
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = RepSty.Gold.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                "${fmtKg(pr.newKg)} kg",
                                color = RepSty.Gold,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RepSty.Gold.copy(alpha = 0.10f))
                            .border(
                                BorderStroke(1.dp, RepSty.Gold.copy(alpha = 0.30f)),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        val delta = pr.newKg - pr.previousKg
                        Text(
                            "+${fmtKg(delta)} kg",
                            color = RepSty.Gold1,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.1).sp
                        )
                    }
                }
            }
        }
    }
}

// =============================================================================
//  Calories block
// =============================================================================
@Composable
private fun CaloriesBlock(caloriesBurned: Int?) {
    SectionFrame(label = "CALORIAS", icon = Icons.Filled.Bolt) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(RepSty.Surface)
                .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(18.dp))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (caloriesBurned != null) {
                // Calorias calculadas — exibe o valor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFFFF6B35),
                                        Color(0xFFFF3D00)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "$caloriesBurned kcal",
                            color = RepSty.TextMain,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            "queimadas neste treino",
                            color = RepSty.TextFaint,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                // Sem peso/idade — exibe CTA para o perfil
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(RepSty.Surface2)
                            .border(BorderStroke(1.dp, RepSty.Border), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = RepSty.TextFaint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Calorias indisponíveis",
                            color = RepSty.TextMain,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Cadastre seu peso e idade no perfil para ver as calorias queimadas nos seus treinos.",
                            color = RepSty.TextDim,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionPRBlock(prs: Map<String, Double>) {
    val entries = remember(prs) { prs.entries.sortedByDescending { it.value } }
    SectionFrame(
        label = "RECORDES BATIDOS",
        icon = Icons.Filled.EmojiEvents,
        gold = true,
        countBadge = entries.size
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            RepSty.Gold.copy(alpha = 0.10f),
                            RepSty.Gold.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
                .background(RepSty.Surface.copy(alpha = 0.6f))
                .border(
                    BorderStroke(1.dp, RepSty.Gold.copy(alpha = 0.28f)),
                    RoundedCornerShape(22.dp)
                )
                .padding(horizontal = 14.dp, vertical = 2.dp)
        ) {
            entries.forEachIndexed { idx, (exerciseName, weightKg) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .then(
                            if (idx != entries.lastIndex)
                                Modifier.drawBehind { drawBehindRowDivider() }
                            else Modifier
                        )
                ) {
                    // Ícone troféu
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .shadow(6.dp, CircleShape, spotColor = RepSty.Gold)
                            .clip(CircleShape)
                            .background(RepSty.GoldGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = Color(0xFF2D1F06),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    // Nome do exercício
                    Text(
                        text = exerciseName,
                        color = RepSty.TextMain,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Badge com o novo recorde
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(RepSty.Gold.copy(alpha = 0.10f))
                            .border(
                                BorderStroke(1.dp, RepSty.Gold.copy(alpha = 0.30f)),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${fmtKg(weightKg)} kg",
                            color = RepSty.Gold1,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.1).sp
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBehindRowDivider() {
    drawLine(
        color = RepSty.Gold.copy(alpha = 0.12f),
        start = androidx.compose.ui.geometry.Offset(0f, size.height),
        end = androidx.compose.ui.geometry.Offset(size.width, size.height),
        strokeWidth = 1f
    )
}

// =============================================================================
//  Muscle distribution
// =============================================================================
@Composable
private fun MuscleDistribution(slices: List<MuscleSlice>) {
    SectionFrame(label = "FOCO MUSCULAR", icon = Icons.Filled.Equalizer) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(RepSty.Surface)
                .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            slices.forEach { slice ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Text(
                        muscleLabel(slice.group),
                        color = RepSty.TextDim,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.4.sp,
                        modifier = Modifier.width(86.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(RepSty.Surface2)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(slice.pct / 100f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(AppTheme.accent.gradient)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${slice.pct}%",
                        color = RepSty.TextMain,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

// =============================================================================
//  Exercises rollup
// =============================================================================
@Composable
private fun ExerciseRollup(exercises: List<Exercise>) {
    SectionFrame(
        label = "EXERCÍCIOS",
        icon = Icons.AutoMirrored.Filled.List,
        countBadge = exercises.size
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(RepSty.Surface)
                .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(18.dp))
        ) {
            exercises.forEachIndexed { idx, ex ->
                ExerciseRow(ex = ex, lastInList = idx == exercises.lastIndex)
            }
        }
    }
}

@Composable
private fun ExerciseRow(ex: Exercise, lastInList: Boolean) {
    val savedSets = ex.exerciseSets.filter { it.isCompleted }
    val isTimeBase = ex.type == ExerciseType.CARDIO || ex.type == ExerciseType.STRETCHING

    // Canto direito: kg máximo (força) ou tempo total formatado (cardio/alongamento)
    val topKg: Double = if (!isTimeBase)
        savedSets.maxOfOrNull { it.weight.toDoubleOrNull() ?: 0.0 } ?: 0.0
    else 0.0

    // Soma dos tempos em segundos para cardio/alongamento
    val totalSeconds: Int = if (isTimeBase)
        savedSets.sumOf { parseTimeToSeconds(it.time) }
    else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(RepSty.Surface2)
        ) {
            Image(
                painter = painterResource(id = muscleImage(ex.muscleGroup)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                        )
                    )
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                ex.name,
                color = RepSty.TextMain,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                muscleLabel(ex.muscleGroup),
                color = RepSty.TextFaint,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.8.sp
            )
            Spacer(Modifier.height(6.dp))
            // pills — conteúdo varia por tipo
            if (savedSets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(8.dp))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        "NÃO REALIZADO",
                        color = RepSty.TextFaint,
                        fontSize = 10.sp,
                        letterSpacing = 0.6.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (isTimeBase) {
                // Cardio / Alongamento — uma pill por série com o tempo
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    savedSets.take(6).forEach { s ->
                        // Cardio: HH:MM → "h" se horas > 0, senão "min"
                        // Alongamento: valor em segundos → sempre "seg"
                        val timeUnit = if (ex.type == ExerciseType.CARDIO) {
                            val parts = s.time.orEmpty().trim().split(":")
                            val firstPart = parts.getOrNull(0)?.toIntOrNull() ?: 0
                            if (firstPart > 0) "h" else "min"
                        } else {
                            "seg"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RepSty.Surface2)
                                .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    s.time.orEmpty().ifBlank { "—" },
                                    color = RepSty.TextMain,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                if (s.time.orEmpty().isNotBlank()) {
                                    Spacer(Modifier.width(3.dp))
                                    Text(
                                        timeUnit,
                                        color = RepSty.TextFaint,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    if (savedSets.size > 6) {
                        Text(
                            "+${savedSets.size - 6}",
                            color = RepSty.TextFaint,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 2.dp, top = 3.dp)
                        )
                    }
                }
            } else {
                // Força — weight × reps
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    savedSets.take(6).forEach { s ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(RepSty.Surface2)
                                .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(8.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    s.weight.ifBlank { "—" },
                                    color = RepSty.TextMain,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "×",
                                    color = RepSty.TextFaint,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                                Text(
                                    s.reps.ifBlank { "—" },
                                    color = RepSty.TextMain,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (savedSets.size > 6) {
                        Text(
                            "+${savedSets.size - 6}",
                            color = RepSty.TextFaint,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 2.dp, top = 3.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        // Canto direito — kg top (força) ou tempo total (cardio/alongamento)
        Column(horizontalAlignment = Alignment.End) {
            if (isTimeBase) {
                val isStretching = ex.type == ExerciseType.STRETCHING
                Text(
                    if (isStretching) "$totalSeconds" else fmtSeconds(totalSeconds),
                    color = AppTheme.accent.light,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    if (isStretching) "seg" else "tempo",
                    color = RepSty.TextFaint,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp
                )
            } else {
                Text(
                    if (topKg > 0) fmtKg(topKg) else "—",
                    color = AppTheme.accent.light,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.4).sp
                )
                Text(
                    "kg top",
                    color = RepSty.TextFaint,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.4.sp
                )
            }
        }
    }
    if (!lastInList) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(RepSty.Border)
        )
    }
}

// =============================================================================
//  Sentiment row
// =============================================================================
private data class FeelingOpt(val id: String, val emoji: String, val label: String)
private val feelingOpts = listOf(
    FeelingOpt("destroyed", "😵", "Destruído"),
    FeelingOpt("hard",      "😤", "Pesado"),
    FeelingOpt("good",      "💪", "Forte"),
    FeelingOpt("light",     "😎", "Tranquilo"),
    FeelingOpt("easy",      "🥱", "Leve"),
)

@Composable
private fun FeelingSection(value: String?, onChange: (String) -> Unit) {
    SectionFrame(label = "COMO FOI?", icon = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            feelingOpts.forEach { opt ->
                val selected = value == opt.id
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (selected) RepSty.Surface else RepSty.Surface
                        )
                        .background(
                            if (selected) Brush.radialGradient(
                                colors = listOf(
                                    AppTheme.accent.light.copy(alpha = 0.18f),
                                    Color.Transparent
                                )
                            ) else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (selected) AppTheme.accent.light else RepSty.Border
                            ),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { onChange(opt.id) }
                        .padding(vertical = 10.dp, horizontal = 4.dp)
                ) {
                    Text(
                        opt.emoji,
                        fontSize = 22.sp,
                        modifier = Modifier
                            .alpha(if (selected) 1f else 0.55f)
                            .graphicsLayer(
                                scaleX = if (selected) 1.12f else 1f,
                                scaleY = if (selected) 1.12f else 1f
                            )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        opt.label.uppercase(),
                        color = if (selected) AppTheme.accent.light else RepSty.TextFaint,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// =============================================================================
//  Section frame helper (header + content slot)
// =============================================================================
@Composable
private fun SectionFrame(
    label: String,
    icon: ImageVector?,
    accent: Boolean = false,
    gold: Boolean = false,
    countBadge: Int? = null,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 10.dp)
        ) {
            val tint = when {
                gold -> RepSty.Gold
                accent -> AppTheme.accent.light
                else -> RepSty.TextDim
            }
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
            }
            Text(
                label,
                color = tint,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.2.sp
            )
            if (countBadge != null) {
                Spacer(Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(RepSty.Surface2)
                        .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(100.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        countBadge.toString(),
                        color = RepSty.TextMain,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )
                }
            }
        }
        content()
    }
}

// =============================================================================
//  Footer
// =============================================================================
@Composable
private fun ReportFooter(onClose: () -> Unit, onShare: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RepSty.BgEnd.copy(alpha = 0.92f))
            .border(BorderStroke(1.dp, RepSty.Border), RoundedCornerShape(0.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ghost share
        Row(
            modifier = Modifier
                .height(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(RepSty.Surface)
                .border(BorderStroke(1.dp, RepSty.BorderStrong), RoundedCornerShape(16.dp))
                .clickable { onShare() }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                Icons.Filled.IosShare,
                contentDescription = null,
                tint = RepSty.TextMain,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Compartilhar",
                color = RepSty.TextMain,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp
            )
        }
        // primary done
        Row(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .shadow(elevation = 14.dp, shape = RoundedCornerShape(16.dp),
                    spotColor = AppTheme.accent.light)
                .clip(RoundedCornerShape(16.dp))
                .background(AppTheme.accent.gradient)
                .clickable { onClose() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "CONCLUIR",
                color = RepSty.OnAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = RepSty.OnAccent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
