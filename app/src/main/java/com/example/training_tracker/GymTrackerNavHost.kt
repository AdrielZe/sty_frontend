package com.example.training_tracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.training_tracker.data.routes.Routes
import com.example.training_tracker.ui.screens.create_workout.CreateWorkoutScreen
import com.example.training_tracker.ui.screens.home.HomeViewModel
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.screens.workout_details.WorkoutDetailsScreen
import com.example.training_tracker.ui.screens.workout_details.WorkoutDetailsViewModel
import com.example.training_tracker.ui.screens.workout_report.WorkoutReportScreen
import com.example.training_tracker.ui.screens.workout_report.WorkoutReportViewModel
import com.example.training_tracker.ui.screens.workout_screen.WorkoutScreen
import com.example.training_tracker.ui.screens.workout_screen.WorkoutViewModel
import com.example.training_tracker.ui.screens.freestyle_workout.FreestyleWorkoutScreen
import com.example.training_tracker.ui.theme.CyanAccent
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
                    navController.navigate("${Routes.WorkoutReport.name}/$historyId") {
                        popUpTo("MAIN_TABS") { inclusive = false }
                    }
                }
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
            val workoutDetailsViewModel: WorkoutDetailsViewModel =
                viewModel(factory = WorkoutDetailsViewModel.Factory)
            WorkoutDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartWorkout = { workoutId ->
                    navController.navigate("${Routes.Workout.name}/$workoutId") {
                        popUpTo("MAIN_TABS") // Pop back to tabs when starting
                    }
                },
                onEditWorkoutName = { name ->
                    workoutDetailsViewModel.updateWorkoutName(name = name)
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
                    navController.navigate("${Routes.WorkoutReport.name}/$historyId") {
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
            )
        }

        // Full Screen: Workout Report
        composable(
            route = "${Routes.WorkoutReport.name}/{workoutId}",
            arguments = listOf(
                navArgument("workoutId") { type = NavType.StringType }
            )
        ) {
            val workoutReportViewModel: WorkoutReportViewModel = 
                viewModel(factory = WorkoutReportViewModel.Factory)

            WorkoutReportScreen(
                workoutReportScreenViewModel = workoutReportViewModel,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}

@Composable
fun GroffitBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.height(85.dp)
    ) {
        val items = listOf(
            Triple(Routes.Home.name, Icons.Default.Home, "Home"),
            Triple(Routes.RegisteredWorkouts.name, Icons.Default.FitnessCenter, "Treinos"),
            Triple(Routes.WorkoutHistory.name, Icons.Default.History, "Histórico"),
            Triple(Routes.Records.name, Icons.Default.EmojiEvents, "Recordes"),
            Triple(Routes.Profile.name, Icons.Default.Person, "Perfil")
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
                        style = MaterialTheme.typography.labelSmall
                    ) 
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyanAccent,
                    selectedTextColor = CyanAccent,
                    unselectedIconColor = TextGray,
                    unselectedTextColor = TextGray,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
