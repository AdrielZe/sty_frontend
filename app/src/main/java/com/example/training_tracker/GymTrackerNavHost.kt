package com.example.training_tracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
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
import com.example.training_tracker.ui.screens.registered_workouts.RegisteredWorkoutsScreen
import com.example.training_tracker.ui.screens.workout_details.WorkoutDetailsScreen
import com.example.training_tracker.ui.screens.workout_details.WorkoutDetailsViewModel
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryScreen
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryViewModel
import com.example.training_tracker.ui.screens.workout_screen.WorkoutScreen
import com.example.training_tracker.ui.screens.workout_screen.WorkoutViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun GymTrackerNavHost() {
    val navController = rememberNavController()
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)
    val homeUiState by homeViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.Home.name,
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
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
                        navController.navigate("${Routes.Workout.name}/$workoutId")
                    } else {
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
                }
            )
        }

        composable(route = Routes.CreateWorkout.name) {
            CreateWorkoutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Routes.RegisteredWorkouts.name) {
            RegisteredWorkoutsScreen(
                onNavigateBack = { navController.popBackStack() },
                onWorkoutClick = { workoutId ->
                    navController.navigate("${Routes.WorkoutDetails.name}/$workoutId/false")
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
            val workoutDetailsViewModel: WorkoutDetailsViewModel = viewModel(factory = WorkoutDetailsViewModel.Factory)
            WorkoutDetailsScreen(
                onNavigateBack = { navController.popBackStack() },
                onStartWorkout = { workoutId ->
                    navController.navigate("${Routes.Workout.name}/$workoutId") {
                        popUpTo(Routes.Home.name)
                    }
                },
                onEditWorkoutName = {
                   name -> workoutDetailsViewModel.updateWorkoutName(name = name)
                }
            )
        }

        composable(route = Routes.WorkoutHistory.name) {
            val historyViewModel: WorkoutHistoryViewModel = viewModel(factory = WorkoutHistoryViewModel.Factory)
            val historyUiState by historyViewModel.uiState.collectAsState()

            WorkoutHistoryScreen(
                uiState = historyUiState,
                onSearchQueryChange = { historyViewModel.onSearchQueryChange(it) },
                onSortOrderChange = { historyViewModel.onSortOrderChange(it) },
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
            val workoutViewModel: WorkoutViewModel = viewModel(factory = WorkoutViewModel.Factory)
            val workoutUiState by workoutViewModel.uiState.collectAsState()

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
                onCompleteWorkout = { workoutViewModel.completeWorkout()}
            )
        }
    }
}
