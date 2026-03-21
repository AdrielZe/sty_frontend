package com.example.training_tracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.training_tracker.data.routes.Routes
import com.example.training_tracker.ui.screens.home.HomeScreen
import com.example.training_tracker.ui.screens.home.HomeViewModel
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
        startDestination = Routes.Home.name
    ) {
        composable(route = Routes.Home.name) {
            HomeScreen(
                homeUiState = homeUiState,
                onClickWorkoutCard = { workoutId ->
                    navController.navigate("${Routes.Workout.name}/$workoutId")
                }
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
                workoutUiState,
                    onRepsChange = {id, setNumber, newReps -> workoutViewModel.updateExercise(exerciseId = id, setNumber = setNumber, newReps = newReps)},
                    onWeightChange = {id, setNumber, newWeight -> workoutViewModel.updateExercise(exerciseId = id, setNumber = setNumber, newWeight = newWeight)},
                    onAddSetClick = {exerciseId -> workoutViewModel.addNewSetLine(exerciseId = exerciseId)},
                    onRemoveSet = {exerciseId, setNumber -> workoutViewModel.removeSetLine(exerciseId, setNumber)},
                    onCompleteSet = {exerciseId, setNumber -> workoutViewModel.completeSet(exerciseId, setNumber)}
            )
        }
    }
}

