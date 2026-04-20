package com.example.training_tracker.ui.screens.workout_report

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.training_tracker.R
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.ui.theme.CyanAccent
import com.example.training_tracker.ui.theme.CyanGradient

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutReportScreen(
    workoutReportScreenViewModel: WorkoutReportViewModel,
    onNavigateBack: () -> Unit,
) {
    val uiState by workoutReportScreenViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "WORKOUT SUMMARY",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = CyanAccent,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = CyanAccent)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Section
            item {
                HeroSection(uiState = uiState)
            }

            // 2. Gamification Card
            item {
                GamificationCard(
                    uiState = uiState
                )
            }

            // 3. Métricas Gerais
            item {
                MetricsGrid(uiState = uiState)
            }

            // 4. Recordes
            item {
                RecordsSection()
            }

            // 5. Tabela de Exercícios (Novo)
            item {
                ExercisesSummarySection(uiState = uiState)
            }

            // Botão de Compartilhar
            item {
                Button(
                    onClick = { /* Compartilhar */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(CyanGradient, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "COMPARTILHAR EVOLUÇÃO",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeroSection(uiState: WorkoutReportUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyanGradient)
                .padding(24.dp)
        ) {
            Column {
                Badge(
                    containerColor = Color.Black.copy(alpha = 0.3f),
                    contentColor = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(4.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("DIFICULDADE: ${uiState.workoutDifficulty}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "${uiState.heroSectionTitle}",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 36.sp
                    )
                )
                Text(
                    "Finalizado em ${uiState.completionDate}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.8f))
                )
            }
        }
    }
}

@Composable
fun GamificationCard(uiState: WorkoutReportUiState) {
    val image = when {
        uiState.totalWeightLiftedInfo?.value == null -> R.drawable.confused_0kg
        uiState.totalWeightLiftedInfo.value >= 10000.0 -> R.drawable.strong_5k
        uiState.totalWeightLiftedInfo.value >= 6000.0 -> R.drawable.strong_4k
        uiState.totalWeightLiftedInfo.value >= 4200.0 -> R.drawable.strong_3k
        uiState.totalWeightLiftedInfo.value >= 3000.0 -> R.drawable.lifting_2kg
        uiState.totalWeightLiftedInfo.value >= 1500.0 -> R.drawable.feather_1kg
        else -> R.drawable.confused_0kg
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = image),
                    contentDescription = "Achievement Image",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    uiState.totalWeightLiftedInfo?.title ?: "Treino Concluído",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                )
                Text(
                    buildString {
                        append("You lifted ")
                        append(uiState.totalWeightLiftedInfo?.value)
                        append(", ")
                        append(uiState.totalWeightLiftedInfo?.comparisonText)
                    },
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray, lineHeight = 18.sp)
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
        MetricItem(Modifier.weight(1f), uiState.totalSets.toString(), "SÉRIES", Icons.Default.FitnessCenter)
        MetricItem(Modifier.weight(1f), uiState.totalReps.toString(), "REPS", Icons.Default.Repeat)
        MetricItem(Modifier.weight(1f), uiState.totalMinutes.toString(), "MINUTOS TUT", Icons.Default.Timer)
    }
}

@Composable
fun MetricItem(modifier: Modifier, value: String, label: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecordsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "NOVOS RECORDES",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.Gray,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
            )
        )
        RecordCard("CARGA MÁXIMA", "140kg (Agachamento)", "🏆")
        RecordCard("VOLUME TOTAL", "Volume Recorde: 12.400kg", "📈")
        RecordCard("ESTIMATIVA DE FORÇA", "1RM Estimado: 155kg", "⚡")
    }
}

@Composable
fun RecordCard(label: String, value: String, emoji: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(CyanAccent.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExercisesSummarySection(uiState: WorkoutReportUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "RESUMO DO TREINO",
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.Gray,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold
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
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                exercise.name.uppercase(),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = CyanAccent,
                    letterSpacing = 0.5.sp
                )
            )
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("SET", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("CARGA", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text("REPS", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.1f))
            
            exercise.exerciseSets.forEachIndexed { index, set ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${index + 1}",
                        modifier = Modifier.weight(0.5f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        "${set.weight} kg",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        set.reps,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }
        }
    }
}