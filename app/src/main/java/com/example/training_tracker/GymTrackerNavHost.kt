package com.example.training_tracker


import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.training_tracker.data.routes.Routes
import com.example.training_tracker.ui.screens.create_workout.CreateWorkoutScreen
import com.example.training_tracker.ui.screens.home.HomeScreen
import com.example.training_tracker.ui.screens.home.HomeViewModel
import com.example.training_tracker.ui.screens.records.RecordsScreen
import com.example.training_tracker.ui.screens.records.RecordsViewModel
import com.example.training_tracker.ui.screens.registered_workouts.RegisteredWorkoutsScreen
import com.example.training_tracker.ui.screens.registered_workouts.TextGray
import com.example.training_tracker.ui.screens.workout_details.WorkoutDetailsScreen
import com.example.training_tracker.ui.screens.workout_details.WorkoutDetailsViewModel
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryScreen
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryViewModel
import com.example.training_tracker.ui.screens.workout_report.WorkoutReportScreen
import com.example.training_tracker.ui.screens.workout_report.WorkoutReportViewModel
import com.example.training_tracker.ui.screens.workout_screen.WorkoutScreen
import com.example.training_tracker.ui.screens.workout_screen.WorkoutViewModel
import com.example.training_tracker.ui.theme.CyanAccent
import java.time.DayOfWeek

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GymTrackerNavHost() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val homeUiState by homeViewModel.uiState.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute= navBackStackEntry?.destination?.route

    val routesWithBottomBar = listOf(
        Routes.Home.name,
        Routes.RegisteredWorkouts.name,
        Routes.WorkoutHistory.name,
        Routes.Records.name
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in routesWithBottomBar) {
                GroffitBottomNavBar(
                    currentRoute = currentRoute ?: Routes.Home.name,
                    onNavigate = { routeName ->
                        navController.navigate(routeName) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home.name,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            enterTransition = {
                slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(
                    animationSpec = tween(400)
                )
            },
            exitTransition = {
                slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(
                    animationSpec = tween(400)
                )
            },
            popEnterTransition = {
                slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn(
                    animationSpec = tween(400)
                )
            },
            popExitTransition = {
                slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(
                    animationSpec = tween(400)
                )
            }
        ) {
            composable(route = Routes.Home.name) {
                HomeScreen(
                    homeUiState = homeUiState,
                    onClickWorkoutCard = { workoutId ->
                        val workout = homeUiState.todayWorkouts.find { it.id == workoutId }
                        if (workout?.isOnGoing == true) {
                            println(workout)

                            navController.navigate("${Routes.Workout.name}/$workoutId")
                        } else if (workout?.isCompleted == true) {
                            navController.navigate("${Routes.WorkoutReport.name}/${workout.historyId}")
                        }
                        else {
                            println(workout)

                            navController.navigate("${Routes.WorkoutDetails.name}/$workoutId/true")
                        }
                    },
                    onNavigateToCreateWorkout = {
                        navController.navigate(Routes.CreateWorkout.name)
                    },
                    onNavigateToRegisteredWorkouts = {
                        navController.navigate(Routes.RegisteredWorkouts.name)
                    },
                    onNavigateToWorkoutsHistory = {
                        navController.navigate(Routes.WorkoutHistory.name)
                    },
                    onClickBrowseWorkouts = {
                        navController.navigate(Routes.RegisteredWorkouts.name)
                    },
                )
            }

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

            composable(route = Routes.RegisteredWorkouts.name) {
                RegisteredWorkoutsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onWorkoutClick = { workoutId ->
                        navController.navigate("${Routes.WorkoutDetails.name}/$workoutId/false")
                    },
                    onCreateWorkoutClick = { dayOfWeek ->
                        if (dayOfWeek != null) {
                            navController.navigate("${Routes.CreateWorkout.name}?dayOfWeek=${dayOfWeek.name}")
                        } else {
                            navController.navigate(Routes.CreateWorkout.name)
                        }
                    }
                )
            }

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
                            popUpTo(Routes.Home.name)
                        }
                    },
                    onEditWorkoutName = { name ->
                        workoutDetailsViewModel.updateWorkoutName(name = name)
                    }
                )
            }

            composable(route = Routes.WorkoutHistory.name) {
                val historyViewModel: WorkoutHistoryViewModel =
                    viewModel(factory = WorkoutHistoryViewModel.Factory)
                val historyUiState by historyViewModel.uiState.collectAsState()

                WorkoutHistoryScreen(
                    uiState = historyUiState,
                    onSearchQueryChange = { historyViewModel.onSearchQueryChange(it) },
                    onSortOrderChange = { historyViewModel.onSortOrderChange(it) },
                    onNavigateBack = { navController.popBackStack() },
                    onClickHistory = { id -> navController.navigate("${Routes.WorkoutReport.name}/$id") },
                    onDateSelected = { historyViewModel.onDateSelected(it) },
                    onMoveMonth = { historyViewModel.onMoveMonth(it) }
                )
            }

            composable(route = Routes.Records.name) {
                val recordsViewModel: RecordsViewModel = viewModel(factory = RecordsViewModel.Factory)
                val recordsUiState by recordsViewModel.uiState.collectAsState()

                RecordsScreen(
                    uiState = recordsUiState,
                    onNavigateBack = { navController.popBackStack() }
                )
            }


            composable(
                route = "${Routes.Workout.name}/{workoutId}",
                arguments = listOf(
                    navArgument("workoutId") {
                        type = NavType.StringType
                    }
                )
            ) {
                val workoutViewModel: WorkoutViewModel =
                    viewModel(factory = WorkoutViewModel.Factory)
                val workoutUiState by workoutViewModel.uiState.collectAsState()

                val navigateToId by workoutViewModel.navigateToReport.collectAsState()

                LaunchedEffect(navigateToId) {
                    navigateToId?.let { historyId ->
                        navController.navigate("${Routes.WorkoutReport.name}/$historyId") {
                            popUpTo(Routes.Home.name) { inclusive = false }
                        }
                        workoutViewModel.onNavigatedToReport()
                    }
                }

                WorkoutScreen(
                    workoutUiState = workoutUiState,
                    onRepsChange = { id, setNumber, newReps ->
                        workoutViewModel.updateExercise(
                            exerciseId = id,
                            setNumber = setNumber,
                            newReps = newReps
                        )
                    },
                    onWeightChange = { id, setNumber, newWeight ->
                        workoutViewModel.updateExercise(
                            exerciseId = id,
                            setNumber = setNumber,
                            newWeight = newWeight
                        )
                    },
                    onAddSetClick = { exerciseId -> workoutViewModel.addNewSetLine(exerciseId = exerciseId) },
                    onRemoveSet = { exerciseId, setNumber ->
                        workoutViewModel.removeSetLine(
                            exerciseId,
                            setNumber
                        )
                    },
                    onCompleteSet = { exerciseId, setNumber ->
                        workoutViewModel.completeSet(
                            exerciseId,
                            setNumber
                        )
                    },
                    onCompleteExercise = { exerciseId ->
                        workoutViewModel.completeExercise(
                            exerciseId
                        )
                    },
                    onReopenExercise = { exerciseId ->
                        workoutViewModel.reopenExercise(exerciseId)
                    },
                    onBackClick = { navController.popBackStack() },
                    onCompleteWorkout = { workoutViewModel.completeWorkout() },
                    onTogglePause = { workoutViewModel.togglePauseWorkout() }
                )
            }

            composable(
                route = "${Routes.WorkoutReport.name}/{workoutId}",
                arguments = listOf(
                    navArgument("workoutId") {
                        type = NavType.StringType
                    }
                )
            ) {
                val workoutReportScreenViewModel: WorkoutReportViewModel =
                    viewModel(factory = WorkoutReportViewModel.Factory)

                WorkoutReportScreen(
                    workoutReportScreenViewModel = workoutReportScreenViewModel,
                    onNavigateBack = { navController.popBackStack() },
                )
            }
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
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == Routes.Home.name,
            onClick = { onNavigate(Routes.Home.name) },
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = null) },
            label = { Text("HOME") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.RegisteredWorkouts.name,
            onClick = { onNavigate(Routes.RegisteredWorkouts.name) },
            icon = { Icon(Icons.Default.ListAlt, contentDescription = null) },
            label = { Text("WORKOUTS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.WorkoutHistory.name,
            onClick = { onNavigate(Routes.WorkoutHistory.name) },
            icon = { Icon(Icons.Default.History, contentDescription = null) },
            label = { Text("HISTORY") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )

        NavigationBarItem(
            selected = currentRoute == Routes.Records.name,
            onClick = { onNavigate(Routes.Records.name) },
            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
            label = { Text("RECORDS") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )

        NavigationBarItem(
            selected = currentRoute == "PROFILE_ROUTE",
            onClick = { /* onNavigate(Routes.Profile.name) */ },
            icon = { Icon(Icons.Default.Person, contentDescription = null) },
            label = { Text("PROFILE") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = CyanAccent,
                selectedTextColor = CyanAccent,
                unselectedIconColor = TextGray
            )
        )
    }
}
