package com.example.training_tracker.ui.screens.workout_report

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.Technique
import com.example.training_tracker.ui.screens.home.MomentumCard
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutReportScreen(
    workoutReportScreenViewModel: WorkoutReportViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by workoutReportScreenViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val targetMinHeight = screenWidth * (16f / 9f)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.relatorio_do_treino),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent,
                            letterSpacing = 2.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.voltar),
                            tint = CyanAccent
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
            )
        },
    ) { paddingValues ->
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Seção que será capturada no print (do Topo até Recordes)
            // Usamos um Box com tamanho mínimo para manter o formato 9:16
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawContent()
                    }
                    .background(MaterialTheme.colorScheme.background),
                //   .defaultMinSize(minHeight = targetMinHeight),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {

                    Column() {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(id = R.string.home_app_title),
                                style = TextStyle(
                                    brush = CyanGradient
                                ),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    Text(
                        text = uiState.workoutName.ifEmpty { stringResource(R.string.relatorio_do_treino) },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    // 1. Gamification Card
                    GamificationCard(uiState = uiState)

                    // 2. Métricas Gerais
                    MetricsGrid(uiState = uiState)

                    // 3. Novos Recordes
                    if (uiState.records?.exercisesRecordMap?.isNotEmpty() == true ||
                        uiState.records?.volumeRecords?.isNotEmpty() == true
                    ) {
                        RecordsSection(uiState.records)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                MuscleIntensitySection(uiState.exercises)

                ExercisesSummarySection(uiState = uiState)

                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                                shareWorkoutBitmap(context, bitmap)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        // 👇 1. Troca do height para defaultMinSize no Button
                        .defaultMinSize(minHeight = 64.dp)
                        .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = CyanAccent),
                    shape = RoundedCornerShape(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues() // Zera o padding padrão, o Box assume o controle
                ) {
                    Box(
                        modifier = Modifier
                            // 👇 2. Troca do fillMaxSize para fillMaxWidth + defaultMinSize
                            // Isso garante que o fundo com gradiente estique para baixo junto com o botão
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 64.dp)
                            .background(CyanGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            // 👇 3. Padding interno para garantir que o texto não cole nas bordas arredondadas
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(R.string.compartilhe_seu_progresso),
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                ),
                                // 👇 4. A mágica de sempre: alinhamento central, maxLines e weight(1f, fill=false)
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

private fun shareWorkoutBitmap(context: Context, bitmap: Bitmap) {
    try {
        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()
        val file = File(cachePath, "workout_report.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        if (contentUri != null) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)

                // Configuração para exibir o preview no Share Sheet (Android 10+)
                putExtra(Intent.EXTRA_TITLE, "Relatório do Treino")

                // O ClipData é EXTREMAMENTE importante para:
                // 1. Mostrar o preview da imagem na tela de compartilhamento nativa
                // 2. Dar permissão correta para apps de terceiros (Instagram, Facebook) lerem a imagem
                clipData = ClipData.newUri(
                    context.contentResolver,
                    "Relatório do Treino",
                    contentUri
                )

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartilhar Treino")
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun GamificationCard(uiState: WorkoutReportUiState) {
    val isDark = isSystemInDarkTheme()
    val weightValue = uiState.totalWeightLiftedInfo?.value ?: 0.0

    val image = when {
        weightValue >= 13000.0 -> R.drawable.weight1
        weightValue >= 8000.0 -> R.drawable.weight2
        else -> R.drawable.weight3
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // 👇 1. defaultMinSize permite que os 230dp sejam uma base, não um teto inflexível
            .defaultMinSize(minHeight = 230.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        // Removi o fillMaxSize() da Box
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(image)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                // 👇 2. matchParentSize nas imagens de fundo
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isDark) 0.5f else 0.7f
            )

            Box(
                modifier = Modifier
                    // 👇 2. matchParentSize no gradiente tbm
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
                    // 👇 3. defaultMinSize aqui garante que o Arrangement.Bottom mande tudo pra baixo
                    .defaultMinSize(minHeight = 230.dp)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // --- LINHA 1: Título ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.AutoMirrored.Filled.TrendingUp,
                        contentDescription = null,
                        tint = CyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.total_de_peso_levantado),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent,
                            letterSpacing = 1.5.sp
                        ),
                        // Proteção contra traduções longas / telas finas
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- LINHA 2: Valor grandão + KG ---
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = NumberFormat.getInstance(Locale.getDefault()).format(weightValue),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        ),
                        // 👇 4. Proteção do número: Impede que um número gigantesco empurre o "kg" pra fora da tela
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "kg",
                        modifier = Modifier.padding(bottom = 8.dp),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // --- LINHA 3: Texto de comparação ---
                Text(
                    text = uiState.totalWeightLiftedInfo?.comparisonText
                        ?: stringResource(R.string.belo_trabalho_hoje),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium
                    )
                    // Não tem maxLines aqui propositalmente!
                    // Como liberamos o defaultMinSize do Card, se essa frase for gigante,
                    // ela pode quebrar em 3 ou 4 linhas e o Card vai crescer graciosamente para acomodar.
                )
            }
        }
    }
}

@Composable
fun MetricsGrid(uiState: WorkoutReportUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MetricCard(
            Modifier.weight(1f),
            uiState.totalSets.toString(),
            stringResource(R.string.serie_upper),
            Icons.Default.FitnessCenter,
            CyanAccent
        )
        MetricCard(
            Modifier.weight(1f),
            uiState.totalReps.toString(),
            stringResource(R.string.reps),
            Icons.Default.Repeat,
            Color(0xFF4CAF50)
        )
        MetricCard(
            Modifier.weight(1f),
            uiState.totalMinutes.toString(),
            stringResource(R.string.mins),
            Icons.Default.Timer,
            Color(0xFFFF9800)
        )
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        // 👇 1. defaultMinSize libera o card para crescer se a fonte do celular for gigante
        modifier = modifier.defaultMinSize(minHeight = 130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                // 👇 2. fillMaxWidth() e defaultMinSize() no lugar de fillMaxSize()
                // Isso garante que ele centralize verticalmente, mas permita o crescimento
                .fillMaxWidth()
                .defaultMinSize(minHeight = 130.dp)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f))
                    .border(BorderStroke(1.dp, color.copy(alpha = 0.4f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                // 👇 3. Proteção do Valor (garante que não passe de 1 linha e centraliza)
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                // 👇 4. Proteção da Legenda (se for longa, quebra em 2 linhas centralizadas)
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MuscleIntensitySection(exercises: List<Exercise>) {
    val muscleGroupsData = remember(exercises) {
        exercises.groupBy { it.muscleGroup }
            .mapValues { it.value.sumOf { ex -> ex.exerciseSets.size } }
            .toList()
            .sortedByDescending { it.second }
    }

    if (muscleGroupsData.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = stringResource(R.string.intensidade_muscular),
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    MuscleLineChart(
                        data = muscleGroupsData,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Detailed Legend below
                muscleGroupsData.forEachIndexed { index, (muscle, count) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(8.dp),
                                shape = CircleShape,
                                color = CyanAccent.copy(
                                    alpha = (1f - (index * 0.15f)).coerceIn(
                                        0.4f,
                                        1f
                                    )
                                )
                            ) {}
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = stringResource(muscle?.resId ?: R.string.nenhum_registro),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "$count ${
                                if (count == 1) stringResource(R.string.serie) else stringResource(
                                    R.string.series_min
                                )
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent
                        )
                    }
                    if (index < muscleGroupsData.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.1f
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MuscleLineChart(
    data: List<Pair<com.example.training_tracker.data.models.MuscleGroups?, Int>>,
    modifier: Modifier = Modifier
) {
    val maxSets = remember(data) { data.maxOf { it.second }.toFloat().coerceAtLeast(1f) }
    val accentColor = CyanAccent
    val textMeasurer = rememberTextMeasurer()
    val onSurface = MaterialTheme.colorScheme.onSurface

    val resources = LocalContext.current.resources

    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = onSurface.copy(alpha = 0.9f),
        fontWeight = FontWeight.ExtraBold,
        fontSize = 9.sp
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Espaçamento para as labels no topo
        val topPadding = 30.dp.toPx()
        val bottomPadding = 10.dp.toPx()
        val chartHeight = height - topPadding - bottomPadding

        val spacing = if (data.size > 1) width / (data.size - 1) else width

        val points = data.mapIndexed { index, pair ->
            val x = if (data.size > 1) index * spacing else width / 2
            val y = topPadding + (chartHeight - (pair.second / maxSets) * chartHeight)
            Offset(x, y)
        }

        // Draw background area gradient
        if (points.size > 1) {
            val fillPath = Path().apply {
                moveTo(points.first().x, height)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.2f), Color.Transparent),
                    startY = topPadding,
                    endY = height
                )
            )
        }

        // Draw line
        val linePath = Path().apply {
            points.forEachIndexed { index, offset ->
                if (index == 0) moveTo(offset.x, offset.y)
                else lineTo(offset.x, offset.y)
            }
        }
        drawPath(
            path = linePath,
            color = accentColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points and labels
        points.forEachIndexed { index, offset ->
            // Circle
            drawCircle(
                color = accentColor,
                radius = 5.dp.toPx(),
                center = offset
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = offset
            )

            // Muscle Name Label above the dot
            val muscle = data[index].first
            val translatedName = if (muscle != null) {
                resources.getString(muscle.resId)
            } else {
                resources.getString(R.string.nenhum_registro)
            }

            val textLayoutResult = textMeasurer.measure(
                text = translatedName,
                style = labelStyle
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = offset.x - textLayoutResult.size.width / 2f,
                    y = offset.y - textLayoutResult.size.height - 6.dp.toPx()
                )
            )
        }
    }
}

@Composable
fun RecordsSection(records: com.example.training_tracker.data.models.Records?) {
    if (records == null || records.exercisesRecordMap.isEmpty()) return

    // Ordenar os recordes por peso decrescente
    val sortedRecords = remember(records) {
        records.exercisesRecordMap.entries
            .map { it.key to (it.value.firstOrNull() ?: 0.0) }
            .sortedByDescending { it.second }
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            // 👇 1. fillMaxWidth() garante que a Row ocupe tudo
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.new_achievements),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                // 👇 2. weight(1f) impede que o título empurre o contador para fora da tela
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.width(8.dp))

            // O selo do contador está protegido agora!
            Surface(
                color = CyanAccent.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Text(
                    text = sortedRecords.size.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = CyanAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        // Dica: Se o Formatador do Java for usado, criamos um que remove zeros sozinho
        val numberFormat = remember {
            java.text.NumberFormat.getNumberInstance(Locale.getDefault()).apply {
                maximumFractionDigits = 1 // Máximo de 1 casa decimal
                minimumFractionDigits = 0 // Se for redondo (ex: 20), não mostra casas decimais
            }
        }

        sortedRecords.forEach { (exerciseName, maxWeight) ->
            NewRecordCard(
                title = stringResource(R.string.recorde_pessoal),
                // 👇 3. Bug do ".0kg" resolvido! Agora "20.0" vira "20kg" e "20.5" vira "20,5kg" no Brasil
                value = "${numberFormat.format(maxWeight)}kg",
                subValue = exerciseName,
                icon = Icons.Default.EmojiEvents,
                color = CyanAccent
            )
        }
    }
}

@Composable
fun NewRecordCard(title: String, value: String, subValue: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            Brush.linearGradient(listOf(color.copy(alpha = 0.6f), Color.Transparent))
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Reduzi levemente o padding de 20 para 16 para ganhar espaço
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ÍCONE (Esquerda) - Mantemos fixo, mas com Surface menor se necessário
            Surface(
                modifier = Modifier.size(48.dp), // Reduzi de 56 para 48 para priorizar o texto
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.padding(12.dp)
                )
            }

            // Espaçador menor para ganhar largura vital
            Spacer(Modifier.width(12.dp))

            // TEXTOS (Meio)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    color = color,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp, // Reduzi um pouco o espaçamento de letra
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Nome do exercício (SubValue)
                Text(
                    text = subValue.uppercase(),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    // 👇 AQUI ESTÁ O SEGREDO: Permitir que o nome do exercício
                    // use até 2 ou 3 linhas se a tela for estreita demais.
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Spacer de segurança
            Spacer(Modifier.width(8.dp))

            // VALOR (Direita)
            Column(
                horizontalAlignment = Alignment.End,
                // Impedimos que esta coluna seja "esmagada"
                modifier = Modifier.wrapContentWidth()
            ) {
                Text(
                    text = value,
                    fontSize = 20.sp, // Reduzi de 24 para 20 para evitar invasão de espaço
                    color = color,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    style = TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color.copy(alpha = 0.3f),
                            blurRadius = 8f
                        )
                    )
                )
                Text(
                    text = stringResource(R.string.novo_recorde),
                    fontSize = 8.sp,
                    color = color.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun ExercisesSummarySection(uiState: WorkoutReportUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            stringResource(R.string.resumo_dos_exercicios),
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.ExtraBold
            )
        )
        uiState.exercises.forEach { exercise ->
            ExerciseSummaryCard(exercise)
        }
    }
}

@Composable
fun ExerciseSummaryCard(exercise: Exercise) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = exercise.name.uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = CyanAccent,
                    letterSpacing = 1.sp
                ),
                // 👇 Blindagem contra nomes de exercícios gigantes
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))

            exercise.exerciseSets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // BLOCO DA ESQUERDA (Bolinha + Texto)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f) // Protege o alinhamento se o texto crescer
                    ) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        ) {
                            // 👇 MUDANÇA: Usamos Box com fillMaxSize para garantir
                            // que o alinhamento Center seja absoluto.
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    // 👇 IMPORTANTE: Forçamos a altura da linha para 1
                                    // para o texto não ser empurrado por paddings invisíveis
                                    lineHeight = 10.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth()
                        ) {
                            if (exercise.type == ExerciseType.CARDIO) {
                                Text(
                                    text = "${set.distance ?: set.weight} km",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = stringResource(R.string.em),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = set.time ?: set.reps,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = CyanAccent,
                                    maxLines = 1
                                )
                            } else if (exercise.type == ExerciseType.STRETCHING) {
                                Text(
                                    text = set.time ?: "--:--",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = CyanAccent,
                                    maxLines = 1
                                )
                            } else {
                            Text(
                                text = "${set.weight} kg",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = "  ×  ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${set.reps} reps",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = CyanAccent,
                                maxLines = 1
                            )
                            }

                            if (set.technique != Technique.NORMAL) {
                                Text(
                                    text = "  -  ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = stringResource(set.technique.label),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = CyanAccent,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                }

                if (index < exercise.exerciseSets.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f)
                    )
                }
            }

        }
    }
}