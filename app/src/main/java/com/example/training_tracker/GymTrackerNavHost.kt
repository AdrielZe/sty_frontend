package com.example.training_tracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.net.Uri
import com.example.training_tracker.data.routes.Routes
import com.example.training_tracker.ui.screens.create_workout.CreateWorkoutScreen
import com.example.training_tracker.ui.screens.exercise_data.EXERCISE_NAME_ARG
import com.example.training_tracker.ui.screens.exercise_data.ExerciseDetailScreen
import com.example.training_tracker.ui.screens.exercise_data.ExerciseDetailViewModel
import com.example.training_tracker.ui.screens.home.HomeViewModel
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.screens.workout_details.WorkoutDetailsScreen
import com.example.training_tracker.ui.screens.workout_details.WorkoutEditViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.training_tracker.ui.screens.workout_report.WorkoutReportScreen
import com.example.training_tracker.ui.screens.workout_report.WorkoutReportViewModel
import com.example.training_tracker.ui.screens.workout_screen.WorkoutScreen
import com.example.training_tracker.ui.screens.workout_screen.WorkoutViewModel
import com.example.training_tracker.ui.screens.conquistas.AchievementsScreen
import com.example.training_tracker.ui.screens.conquistas.AchievementsViewModel
import com.example.training_tracker.ui.screens.freestyle_workout.FreestyleWorkoutScreen
import com.example.training_tracker.ui.theme.AppTheme
import java.time.DayOfWeek

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GymTrackerNavHost() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = "MAIN_TABS",
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        enterTransition = {
            slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + 
            fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + 
            fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + 
            fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + 
            fadeOut(animationSpec = tween(400))
        }
    ) {
        // Main Tabs (Home, Workouts, History, Records)
        composable(route = "MAIN_TABS") {
            MainTabsScreen(
                rootNavController = navController,
                homeViewModel = homeViewModel
            )
        }

        // Freestyle Workout
        composable(route = Routes.FreestyleWorkout.name) {
            FreestyleWorkoutScreen(
                onBackClick = { navController.popBackStack() },
                onNavigateToReport = { historyId ->
                    navController.navigate("${Routes.WorkoutReport.name}/$historyId?fromWorkout=true") {
                        popUpTo("MAIN_TABS") { inclusive = false }
                    }
                },
            )
        }

        // Full Screen: Create Workout
        composable(
            route = "${Routes.CreateWorkout.name}?dayOfWeek={dayOfWeek}",
            arguments = listOf(
                navArgument("dayOfWeek") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val dayOfWeekString = backStackEntry.arguments?.getString("dayOfWeek")
            val dayOfWeek = dayOfWeekString?.let { DayOfWeek.valueOf(it) }

            CreateWorkoutScreen(
                onNavigateBack = { navController.popBackStack() },
                initialDayOfWeek = dayOfWeek
            )
        }

        // Full Screen: Workout Details
        composable(
            route = "${Routes.WorkoutDetails.name}/{workoutId}/{canStart}",
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType },
                navArgument("canStart") { type = NavType.StringType }
            )
        ) {
            val workoutEditViewModel: WorkoutEditViewModel =
                viewModel(factory = WorkoutEditViewModel.Factory)
            WorkoutDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartWorkout = { workoutId ->
                    navController.navigate("${Routes.Workout.name}/$workoutId") {
                        popUpTo("MAIN_TABS") // Pop back to tabs when starting
                    }
                },
                onEditWorkoutName = { name ->
                    workoutEditViewModel.updateWorkoutName(name = name)
                }
            )
        }

        // Full Screen: Active Workout Session
        composable(
            route = "${Routes.Workout.name}/{workoutId}",
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType }
            )
        ) {
            val workoutViewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModel.Factory)
            val workoutUiState by workoutViewModel.uiState.collectAsState()
            val navigateToId by workoutViewModel.navigateToReport.collectAsState()

            LaunchedEffect(navigateToId) {
                navigateToId?.let { historyId ->
                    navController.navigate("${Routes.WorkoutReport.name}/$historyId?fromWorkout=true") {
                        popUpTo("MAIN_TABS") { inclusive = false }
                    }
                    workoutViewModel.onNavigatedToReport()
                }
            }

            WorkoutScreen(
                workoutUiState = workoutUiState,
                onRepsChange = { id, setNumber, newReps ->
                    workoutViewModel.updateExercise(id, setNumber, newReps = newReps)
                },
                onWeightChange = { id, setNumber, newWeight ->
                    workoutViewModel.updateExercise(id, setNumber, newWeight = newWeight)
                },
                onAddSetClick = { exerciseId -> workoutViewModel.addNewSetLine(exerciseId) },
                onRemoveSet = { exerciseId, setNumber -> workoutViewModel.removeSetLine(exerciseId, setNumber) },
                onCompleteSet = { exerciseId, setNumber -> workoutViewModel.completeSet(exerciseId, setNumber) },
                onCompleteExercise = { exerciseId -> workoutViewModel.completeExercise(exerciseId) },
                onReopenExercise = { exerciseId -> workoutViewModel.reopenExercise(exerciseId) },
                onBackClick = { navController.popBackStack() },
                onCompleteWorkout = { workoutViewModel.completeWorkout() },
                onTogglePause = { workoutViewModel.togglePauseWorkout() },
                onTechniqueChange = { exerciseId, setNumber, technique ->
                    workoutViewModel.updateSetTechnique(exerciseId, setNumber, technique)
                }
            )
        }

        // Full Screen: Exercise Detail
        composable(
            route = "${Routes.ExerciseDetail.name}/{$EXERCISE_NAME_ARG}",
            arguments = listOf(navArgument(EXERCISE_NAME_ARG) { type = NavType.StringType })
        ) {
            val exerciseDetailViewModel: ExerciseDetailViewModel =
                viewModel(factory = ExerciseDetailViewModel.Factory)
            val exerciseDetailUiState by exerciseDetailViewModel.uiState.collectAsState()

            ExerciseDetailScreen(
                uiState = exerciseDetailUiState,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Full Screen: Workout Report
        composable(
            route = "${Routes.WorkoutReport.name}/{workoutId}?fromWorkout={fromWorkout}",
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType },
                navArgument("fromWorkout") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            val workoutReportViewModel: WorkoutReportViewModel =
                viewModel(factory = WorkoutReportViewModel.Factory)
            val workoutReportUiState by workoutReportViewModel.uiState.collectAsStateWithLifecycle()

            WorkoutReportScreen(
                uiState = workoutReportUiState,
                onClose = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun StyBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        val items = listOf(
            Triple(Routes.Home.name, Icons.Default.Home, stringResource(R.string.home)),
            Triple(Routes.Achievements.name, Icons.Default.EmojiEvents, stringResource(R.string.conquistas_min)),
            Triple(Routes.WorkoutHistory.name, Icons.Default.History, stringResource(R.string.historico_min)),
            Triple(Routes.ExerciseData.name, Icons.Default.FitnessCenter, stringResource(R.string.exercicios_min)),
            Triple(Routes.Profile.name, Icons.Default.Person, stringResource(R.string.perfil))
        )

        items.forEach { (route, icon, label) ->
            val isSelected = currentRoute == route

            NavigationBarItem(
                selected = isSelected,
                onClick = { if (!isSelected) onNavigate(route) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        // BLINDAGEM DE TEXTO AQUI 👇
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppTheme.accent.light,
                    selectedTextColor = AppTheme.accent.light,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}