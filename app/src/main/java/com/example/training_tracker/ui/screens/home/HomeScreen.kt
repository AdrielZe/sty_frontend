package com.example.training_tracker.ui.screens.home

import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.example.training_tracker.R
import com.example.training_tracker.ui.components.StyLogo
import com.example.training_tracker.ui.components.StyLogoLayout
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.models.User
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.models.extensions.isValidToComplete
import com.example.training_tracker.ui.theme.AppTheme

import com.example.training_tracker.ui.theme.GreenGradient
import com.example.training_tracker.ui.theme.Typography
import kotlin.math.abs

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onClickWorkoutCard: (String?) -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    onNavigateToRegisteredWorkouts: () -> Unit,
    onNavigateToWorkoutsHistory: () -> Unit,
    onClickBrowseWorkouts: () -> Unit,
    onNavigateToFreestyleWorkout: () -> Unit = {},
    homeUiState: HomeUiState,
    homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
) {
    when (homeUiState) {
        is HomeUiState.Loading -> {
            HomeLoadingScreen()
        }

        is HomeUiState.Error -> {
            HomeErrorScreen(message = homeUiState.message)
        }

        is HomeUiState.Success -> {
            HomeContent(
                modifier = modifier,
                onClickWorkoutCard = onClickWorkoutCard,
                onNavigateToCreateWorkout = onNavigateToCreateWorkout,
                onNavigateToRegisteredWorkouts = onNavigateToRegisteredWorkouts,
                onNavigateToWorkoutsHistory = onNavigateToWorkoutsHistory,
                onClickBrowseWorkouts = onClickBrowseWorkouts,
                onNavigateToFreestyleWorkout = onNavigateToFreestyleWorkout,
                homeUiState = homeUiState,
                homeViewModel = homeViewModel,
            )
        }
    }
}

@Composable
fun HomeLoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AppTheme.accent.light)
    }
}

@Composable
fun HomeErrorScreen(message: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message ?: stringResource(R.string.error),
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    onClickWorkoutCard: (String?) -> Unit,
    onNavigateToCreateWorkout: () -> Unit,
    onNavigateToRegisteredWorkouts: () -> Unit,
    onNavigateToWorkoutsHistory: () -> Unit,
    onClickBrowseWorkouts: () -> Unit,
    onNavigateToFreestyleWorkout: () -> Unit,
    homeUiState: HomeUiState.Success,
    homeViewModel: HomeViewModel
) {
    var isProfileExpanded by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showFreestyleNameDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CustomTopBar(
                onProfileClick = { isProfileExpanded = true },
                user = homeUiState.user
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateWorkout,
                containerColor = AppTheme.accent.light,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(elevation = 8.dp, shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.home_create_new_workout_button),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // Greeting block
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(
                            id = R.string.home_greeting,
                            homeUiState.user?.name
                                ?: stringResource(id = R.string.home_default_user_name)
                        ),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    homeUiState.currentDate?.let { date ->
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HomeSectionHeader(
                title = stringResource(id = R.string.home_scheduled_workout_today),
                actionLabel = stringResource(R.string.home_browse_workouts),
                onActionClick = onClickBrowseWorkouts
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (homeUiState.todayWorkouts.isNotEmpty()) {
                homeUiState.todayWorkouts.forEach { workout ->
                    WorkoutCard(
                        workout = workout,
                        onClick = { onClickWorkoutCard(workout.id) }
                    )
                }
            } else {
                EmptyWorkoutCard()
            }

            Spacer(modifier = Modifier.height(24.dp))

            HomeSectionHeader(title = stringResource(R.string.treino_livre))

            Spacer(modifier = Modifier.height(12.dp))

            FreestyleWorkoutCard(
                activeWorkout = homeUiState.activeFreestyleWorkout,
                onClick = { showFreestyleNameDialog = true },
                viewModel = homeViewModel
            )

            Spacer(modifier = Modifier.height(24.dp))

            HomeSectionHeader(title = stringResource(R.string.progresso))

            Spacer(modifier = Modifier.height(10.dp))

            if (homeUiState.user?.weightKg != null && homeUiState.weeklyCalories > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                WeeklyCaloriesCard(calories = homeUiState.weeklyCalories)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (homeUiState.totalWorkoutsCompleted > 0) MomentumCard(count = homeUiState.totalWorkoutsCompleted) else EmptyMomentumCard()

            Spacer(modifier = Modifier.height(12.dp))

            val weeklyGoal = homeUiState.user?.weeklyGoal

            if (weeklyGoal != null && weeklyGoal > 0) {
                WeeklyProgressCard(
                    currentWorkouts = homeUiState.workoutsCompletedThisWeek,
                    goalWorkouts = weeklyGoal,
                    onEditGoal = { showGoalDialog = true }
                )
            } else {
                SetWeeklyGoalCard(onClick = { showGoalDialog = true })
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (isProfileExpanded) {
        Dialog(onDismissRequest = { isProfileExpanded = false }) {
            Box(
                modifier = Modifier
                    .size(300.dp) // Tamanho da imagem expandida
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(BorderStroke(4.dp, AppTheme.accent.gradient), CircleShape)
                    .clickable { isProfileExpanded = false },
                contentAlignment = Alignment.Center
            ) {
                if (!homeUiState.user?.profilePicture.isNullOrEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(homeUiState.user?.profilePicture)
                            .size(Size.ORIGINAL)
                            .build(),
                        contentDescription = stringResource(R.string.foto_de_perfil_expandida),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.gym),
                        placeholder = painterResource(id = R.drawable.gym)
                    )
                } else {
                    // Imagem padrão
                    Image(
                        painter = painterResource(id = R.drawable.gym),
                        contentDescription = stringResource(R.string.foto_de_perfil_padrao),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    if (showGoalDialog) {
        WeeklyGoalPickerDialog(
            currentGoal = homeUiState.user?.weeklyGoal ?: 3,
            onDismiss = { showGoalDialog = false },
            onConfirm = { goal ->
                homeViewModel.updateWeeklyGoal(goal)
                showGoalDialog = false
            }
        )
    }

    if (showFreestyleNameDialog) {
        FreestyleWorkoutNameDialog(
            onDismiss = { showFreestyleNameDialog = false },
            onConfirm = { name ->
                homeViewModel.startFreestyleWorkout(name) {
                    onNavigateToFreestyleWorkout()
                }
                showFreestyleNameDialog = false
            }
        )
    }
}

@Composable
fun FreestyleWorkoutNameDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var workoutName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                // Adicionando uma borda sutil em degradê ou cor de destaque
                .border(1.dp, AppTheme.accent.light.copy(alpha = 0.2f), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícone moderno no topo
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(AppTheme.accent.light.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit, // Certifique-se de importar Icons.Default.Edit
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.nome_do_treino),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold, // Mais peso para modernidade
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = stringResource(R.string.como_quer_chamar_o_seu_treino),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.ex_treino_de_sexta),
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    textStyle = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.accent.light,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                        cursorColor = AppTheme.accent.light,
                        focusedContainerColor = AppTheme.accent.light.copy(alpha = 0.02f)
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cancelar),
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { if (workoutName.isNotBlank()) onConfirm(workoutName) },
                        enabled = workoutName.isNotBlank(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.accent.light,
                            contentColor = Color.Black,
                            disabledContainerColor = AppTheme.accent.light.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.iniciar),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FreestyleWorkoutCard(
    activeWorkout: Workout? = null,
    viewModel: HomeViewModel,
    onClick: () -> Unit
) {
    val isLocked = activeWorkout != null
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.cancelar_treino),
                    style = Typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.tem_certeza_que_deseja_cancelar_e_remover_o_treino,
                        activeWorkout?.name ?: "this"
                    ),
                    style = Typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeFreestyleWorkout()
                        showCancelDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.confirmar),
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCancelDialog = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancelar),
                        color = Color.Gray
                    )
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 100.dp)
            .clickable(enabled = !isLocked) { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLocked)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (isLocked) Color.Gray.copy(alpha = 0.3f) else AppTheme.accent.light.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isLocked) Color.Gray.copy(alpha = 0.1f) else AppTheme.accent.light.copy(
                            alpha = 0.1f
                        ), CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (isLocked) Color.Gray else AppTheme.accent.light,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.treino_livre),
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) Color.Gray else AppTheme.accent.light
                )
                Text(
                    text = if (isLocked)
                        stringResource(
                            R.string.treino_livre_em_andamento_finalize_ou_cancele_esse_treino_antes_de_iniciar_outro,
                            activeWorkout.name
                        )
                    else
                        stringResource(R.string.nao_planejou_seu_treino_inicie_um_treino_livre_e_adicione_os_exercicios_na_hora),
                    style = Typography.labelSmall,
                    color = if (isLocked) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (isLocked) {
                    Text(
                        text = stringResource(R.string.cancelar_treino),
                        style = Typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 8.dp) // Um pequeno respiro entre a descrição e o botão
                            .clip(RoundedCornerShape(4.dp)) // Deixa o efeito de clique arredondado
                            .clickable { showCancelDialog = true }
                            .padding(4.dp) // Padding interno para a área de clique ficar confortável
                    )
                }
            }
            if (!isLocked) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = AppTheme.accent.light,
                )
            }
        }
    }
}

fun getWorkoutImageRes(workout: Workout?): Int {
    if (workout == null || workout.exercises.isEmpty()) return R.drawable.workout

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
        MuscleGroups.CARDIO -> R.drawable.cardio_workout
        MuscleGroups.STRETCHING -> R.drawable.stretching_workout
        else -> R.drawable.biceps_workout
    }
}

@Composable
fun SetWeeklyGoalCard(onClick: () -> Unit) {
    val isSystemDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.strong_3k)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isSystemDark) 0.2f else 0.4f
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = AppTheme.accent.light.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, AppTheme.accent.light.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = stringResource(R.string.defina_sua_meta_semanal),
                        style = Typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(R.string.quantos_treinos_voce_fara_esta_semana),
                        style = Typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyGoalPickerDialog(
    currentGoal: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val items = (1..7).toList()
    val listState = rememberLazyListState()

    val selectedGoal by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) currentGoal
            else {
                val viewportCenter =
                    (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2f
                visibleItemsInfo.minByOrNull { item ->
                    val itemCenter = item.offset + (item.size / 2f)
                    abs(itemCenter - viewportCenter)
                }?.index?.plus(1) ?: currentGoal
            }
        }
    }

    LaunchedEffect(Unit) {
        listState.scrollToItem(currentGoal - 1)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.meta_semanal),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.accent.light
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.escolha_seu_objetivo_de_treinos_por_semana),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        color = AppTheme.accent.light.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {}

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(vertical = 65.dp)
                    ) {
                        items(items) { num ->
                            val isSelected = num == selectedGoal
                            Box(
                                modifier = Modifier
                                    .height(50.dp)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$num ${
                                        if (num == 1) stringResource(R.string.treino) else stringResource(
                                            R.string.treinos
                                        )
                                    }",
                                    style = if (isSelected) {
                                        MaterialTheme.typography.headlineMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.accent.light
                                        )
                                    } else {
                                        MaterialTheme.typography.bodyLarge.copy(
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = stringResource(R.string.cancelar),
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(12.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(selectedGoal) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppTheme.accent.light),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("CONFIRMAR", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyProgressCard(
    currentWorkouts: Int,
    goalWorkouts: Int,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress = if (goalWorkouts > 0) {
        (currentWorkouts.toFloat() / goalWorkouts.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            // 👇 1. defaultMinSize para permitir o card esticar verticalmente se precisar
            .defaultMinSize(minHeight = 160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        // Removi o fillMaxSize() daqui para a Box acompanhar o tamanho da Row interna
        Box {

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.count_backround)
                    .build(),
                contentDescription = stringResource(R.string.fundo_do_card),
                contentScale = ContentScale.Crop,
                // 👇 2. matchParentSize() garante que a imagem acompanhe o crescimento do card
                modifier = Modifier.matchParentSize()
            )

            Box(
                modifier = Modifier
                    // 👇 2. matchParentSize() no overlay escuro tbm
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    // Adicionei defaultMinSize na Column tbm para o SpaceBetween vertical funcionar certinho
                    .defaultMinSize(minHeight = 160.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                // --- LINHA DO TOPO ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    // Removi o Arrangement.SpaceBetween daqui, pois o weight() vai fazer o trabalho!
                    verticalAlignment = Alignment.Top
                ) {

                    // LADO ESQUERDO (Textos + Lápis)
                    Column(
                        // 👇 3. A SALVAÇÃO HORIZONTAL: weight(1f)
                        // Impede que os textos empurrem o botão "X / Y" para fora da tela.
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.progresso_semanal),
                                color = Color.White,
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                // 👇 4. fill = false permite que o texto quebre de linha
                                // sem empurrar o ícone de lápis para longe se sobrar espaço.
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.editar_meta),
                                modifier = Modifier
                                    .size(16.dp) // Aumentei um tiquinho (14.dp é bem ruim de clicar)
                                    .clickable { onEditGoal() },
                                tint = AppTheme.accent.light
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = stringResource(R.string.consistencia_e_a_chave),
                            color = Color.LightGray,
                            style = Typography.labelSmall
                        )
                    }

                    // Um espaço seguro entre os textos e o contador para eles nunca se colarem
                    Spacer(modifier = Modifier.width(16.dp))

                    // LADO DIREITO (Contador X / Y)
                    Surface(
                        color = AppTheme.accent.light.copy(alpha = 0.1f),
                        shape = CircleShape,
                        border = BorderStroke(1.dp, AppTheme.accent.light.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "$currentWorkouts / $goalWorkouts",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = AppTheme.accent.light,
                            style = Typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BLOCO DA BARRA DE PROGRESSO ---
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction = progress)
                                .fillMaxHeight() // fillMaxHeight aqui está seguro, pois o pai tem altura fixa de 8.dp
                                .background(AppTheme.accent.gradient)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (progress >= 1f) stringResource(R.string.objetivo_atingido) else stringResource(
                            R.string.completo, (progress * 100).toInt()
                        ),
                        color = if (progress >= 1f) Color.White else Color.LightGray,
                        style = Typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


@Composable
fun CustomTopBar(
    onProfileClick: () -> Unit,
    user: User?
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StyLogo(
            titleSize = 22.sp,
            layout = StyLogoLayout.VERTICAL
        )

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            AppTheme.accent.dark.copy(alpha = 0.6f),
                            com.example.training_tracker.ui.theme.AppTheme.accent.light.copy(alpha = 0.3f)
                        )
                    )
                )
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            if (user?.profilePicture != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(user.profilePicture)
                        .size(Size.ORIGINAL)
                        .build(),
                    contentDescription = stringResource(id = R.string.content_description_profile),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.gym),
                    contentDescription = stringResource(R.string.foto_de_perfil_padr_o),
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun HomeSectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = Typography.titleMedium.copy(brush = AppTheme.accent.gradient),
            fontWeight = FontWeight.Bold
        )
        if (actionLabel != null && onActionClick != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppTheme.accent.light.copy(alpha = 0.12f),
                modifier = Modifier.clickable { onActionClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = actionLabel.trim(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.accent.light
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MainGradientButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 👇 1. defaultMinSize para manter os 64.dp de design, mas permitindo expansão
            .defaultMinSize(minHeight = 64.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = AppTheme.accent.light.copy(alpha = 0.5f)
            )
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = AppTheme.accent.gradient
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            // 👇 2. Um padding de segurança para o conteúdo nunca colar nas bordas arredondadas do botão
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center, // Mantém o texto alinhado se quebrar a linha
                // 👇 4. weight(1f, fill = false) mantém tudo centralizado, mas permite que o texto encolha sem vazar do botão!
                modifier = Modifier.weight(1f, fill = false)
            )
        }
    }
}
@Composable
fun WorkoutCard(modifier: Modifier = Modifier, workout: Workout?, onClick: () -> Unit) {
    val isCompleted = workout?.isCompleted == true
    val isSystemDark = isSystemInDarkTheme()

    val muscleGroups = remember(workout) {
        workout?.exercises?.mapNotNull { it.muscleGroup }?.distinct() ?: emptyList()
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemDark) MaterialTheme.colorScheme.surface else Color(0XFF0D0D0D)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getWorkoutImageRes(workout))
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isCompleted) 0.2f else if (isSystemDark) 0.5f else 0.7f
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                if (isCompleted) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(
                                    alpha = if (isSystemDark) 0.8f else 0.9f
                                )
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 200.dp)
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                brush = if (isCompleted) GreenGradient else AppTheme.accent.gradient,
                                alpha = 0.9f
                            )
                    ) {
                        Text(
                            text = if (isCompleted) stringResource(R.string.completed) else stringResource(R.string.today),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }

                    if (workout?.id == "freestyle_workout_id") {
                        Spacer(Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    brush = if (isCompleted) GreenGradient else AppTheme.accent.gradient,
                                    alpha = 0.9f
                                )
                        ) {
                            Text(
                                text = stringResource(R.string.livre),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                // --- COLUNA DE BAIXO (Nome, Status, Infos e Músculos) ---
                Column(
                    // 👇 3. PROTEÇÃO DO ÍCONE: Se não estiver completo, adiciona um padding na direita
                    // para garantir que o título e as infos nunca encostem na seta branca gigante!
                    modifier = Modifier.padding(end = if (!isCompleted) 48.dp else 0.dp)
                ) {
                    Text(
                        text = workout?.name ?: stringResource(id = R.string.home_default_workout_name),
                        style = Typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (workout?.isOnGoing == true && !isCompleted) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(workout.progress)
                                        .fillMaxHeight()
                                        .background(AppTheme.accent.gradient)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(workout.progress * 100).toInt()}% ${stringResource(R.string.completed)}",
                                color = Color.White.copy(alpha = 0.7f),
                                style = Typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                id = R.string.home_workout_exercise_count,
                                workout?.exercises?.size ?: 0
                            ),
                            color = Color.White.copy(alpha = 0.7f),
                            style = Typography.bodySmall,
                            maxLines = 1 // Bom garantir aqui tbm
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${workout?.estimatedTime ?: 0} min",
                            color = Color.White.copy(alpha = 0.7f),
                            style = Typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    if (muscleGroups.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(muscleGroups) { muscle ->
                                MuscleBadge(muscle = muscle)
                            }
                        }
                    }
                }
            }

            // --- BOTÃO DE SETA ABSOLUTO ---
            if (!isCompleted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp)
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MuscleBadge(muscle: MuscleGroups) {
    Surface(
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(id = muscle.resId),
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    ),
                    lineHeight = 18.sp
                )
            )
        }
    }
}

@Composable
fun EmptyWorkoutCard(modifier: Modifier = Modifier) {
    val isSystemDark = isSystemInDarkTheme()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemDark) MaterialTheme.colorScheme.surfaceVariant else Color.Black
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.relax_no_workout)
                    .build(),
                contentDescription = null,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isSystemDark) 0.3f else 0.55f
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isSystemDark) 0.7f else 0.85f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .defaultMinSize(minHeight = 200.dp)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = stringResource(id = R.string.home_no_workout_scheduled_today),
                    color = Color.White,
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessibilityNew,
                        contentDescription = null,
                        tint = AppTheme.accent.light,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.dica_faca_um_leve_agachamento_ou_uma_caminhada_de_15_minutos),
                        color = Color.White,
                        style = Typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun MomentumCard(count: Int) {
    val isSystemDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 👇 1. defaultMinSize para garantir o visual padrão, mas permitir o crescimento
            .defaultMinSize(minHeight = 150.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSystemDark) MaterialTheme.colorScheme.surface else Color(0XFF262525))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.gym)
                .build(),
            contentDescription = null,
            // 👇 2. matchParentSize() para a imagem preencher o fundo independentemente da altura que o Box assumir
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        Column(
            modifier = Modifier
                .fillMaxWidth() // Adicionado para a Row interna se comportar bem
                .padding(24.dp)
                .align(Alignment.CenterStart)
        ) {
            Text(
                stringResource(R.string.mantenha_o_foco),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = AppTheme.accent.light,
                    lineHeight = 24.sp
                )
            )
            Spacer(Modifier.height(8.dp))
            Row(
                // 👇 3. fillMaxWidth() no lugar de fillMaxSize() para acompanhar a altura dinâmica da Column
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.treinos_totais),
                    style = Typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$count",
                        style = Typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTheme.accent.light,
                    )
                }
            }
        }
    }
}
@Composable
fun EmptyMomentumCard() {
    val isSystemDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSystemDark) MaterialTheme.colorScheme.surface else Color.Black),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(R.drawable.start_today)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )

        Column(
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.CenterStart)
        ) {
            Text(
                stringResource(R.string.comece_hoje),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = AppTheme.accent.light,
                    lineHeight = 24.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                stringResource(R.string.voce_nao_completou_nenhum_treino_ainda),
                fontSize = 14.sp,
                color = if (isSystemDark) MaterialTheme.colorScheme.onSurface else Color.White
            )
            Text(
                stringResource(R.string.comece_hoje_para_acompanhar_seu_progresso),
                fontSize = 14.sp,
                color = if (isSystemDark) MaterialTheme.colorScheme.onSurface else Color.White
            )
        }
    }
}

@Composable
fun WeeklyCaloriesCard(calories: Int) {
    val fireColor = Color(0xFFFF5722)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            fireColor.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(fireColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = fireColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.calorias_semana),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                    Text(
                        text = stringResource(R.string.calorias_queimadas, calories),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = fireColor
                        )
                    )
                }
            }
        }
    }
}
