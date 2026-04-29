package com.example.training_tracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.training_tracker.data.routes.Routes
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.ui.screens.home.HomeUiState
import com.example.training_tracker.ui.screens.home.HomeScreen
import com.example.training_tracker.ui.screens.home.HomeViewModel
import com.example.training_tracker.ui.screens.records.RecordsScreen
import com.example.training_tracker.ui.screens.records.RecordsViewModel
import com.example.training_tracker.ui.screens.registered_workouts.RegisteredWorkoutsScreen
import com.example.training_tracker.ui.screens.user_profile.UserProfileScreen
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryScreen
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainTabsScreen(
    rootNavController: androidx.navigation.NavController,
    homeViewModel: HomeViewModel
) {
    // Este é o NavController FILHO, exclusivo para as abas
    val tabsNavController = rememberNavController()
    val navBackStackEntry by tabsNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home.name
    val navigateToTab = { routeName: String ->
        tabsNavController.navigate(routeName) {
            popUpTo(tabsNavController.graph.findStartDestination().route ?: Routes.Home.name) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            GroffitBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { tab -> navigateToTab(tab) }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = tabsNavController,
            startDestination = Routes.Home.name,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(route = Routes.Home.name) {
                val homeUiState by homeViewModel.uiState.collectAsState()
                HomeScreen(
                    homeUiState = homeUiState,
                    onClickWorkoutCard = { workoutId ->
                        val successState = homeUiState as? HomeUiState.Success
                        val clickedWorkout = successState?.todayWorkouts?.find { it.id == workoutId }
                        
                        if (clickedWorkout != null) {
                            if (clickedWorkout.isCompleted == false && !clickedWorkout.isOnGoing) {
                                rootNavController.navigate("${Routes.WorkoutDetails.name}/$workoutId/true")
                            } else if(clickedWorkout.isOnGoing == true) {
                                // Redireciona corretamente se for o treino freestyle
                                if (workoutId == "freestyle_workout_id" || clickedWorkout.id == "freestyle_workout_id") {
                                    rootNavController.navigate(Routes.FreestyleWorkout.name)
                                } else {
                                    rootNavController.navigate("${Routes.Workout.name}/$workoutId")
                                }
                            }
                            else {
                                rootNavController.navigate("${Routes.WorkoutReport.name}/${clickedWorkout.historyId}")
                            }
                        }
                    },
                    onNavigateToCreateWorkout = {
                        rootNavController.navigate(Routes.CreateWorkout.name)
                    },
                    onNavigateToRegisteredWorkouts = {
                        navigateToTab(Routes.RegisteredWorkouts.name)
                    },
                    onNavigateToWorkoutsHistory = {
                        navigateToTab(Routes.WorkoutHistory.name)
                    },
                    onClickBrowseWorkouts = {
                        navigateToTab(Routes.RegisteredWorkouts.name)
                    },
                    onNavigateToFreestyleWorkout = {
                        rootNavController.navigate(Routes.FreestyleWorkout.name)
                    },
                )
            }

            composable(route = Routes.RegisteredWorkouts.name) {
                RegisteredWorkoutsScreen(
                    onNavigateBack = { tabsNavController.popBackStack() },
                    // Clicar no treino abre tela cheia, então usa o ROOT
                    onWorkoutClick = { workoutId ->
                        rootNavController.navigate("${Routes.WorkoutDetails.name}/$workoutId/false")
                    },
                    // Criar treino abre tela cheia, então usa o ROOT
                    onCreateWorkoutClick = { dayOfWeek ->
                        if (dayOfWeek != null) {
                            rootNavController.navigate("${Routes.CreateWorkout.name}?dayOfWeek=${dayOfWeek.name}")
                        } else {
                            rootNavController.navigate(Routes.CreateWorkout.name)
                        }
                    }
                )
            }

            composable(route = Routes.WorkoutHistory.name) {
                val viewModel: WorkoutHistoryViewModel = viewModel(factory = WorkoutHistoryViewModel.Factory)
                val uiState by viewModel.uiState.collectAsState()

                WorkoutHistoryScreen(
                    uiState = uiState,
                    onDateSelected = viewModel::onDateSelected,
                    onMuscleGroupSelected = viewModel::onMuscleGroupSelected,
                    onMoveMonth = viewModel::onMoveMonth,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onSortOrderChange = viewModel::onSortOrderChange,
                    onNavigateBack = { tabsNavController.popBackStack() },
                    onClickHistory = { id -> 
                        rootNavController.navigate("${Routes.WorkoutReport.name}/$id") 
                    }
                )
            }

            composable(route = Routes.Records.name) {
                val viewModel: RecordsViewModel = viewModel(factory = RecordsViewModel.Factory)
                val uiState by viewModel.uiState.collectAsState()

                RecordsScreen(
                    uiState = uiState,
                    onNavigateBack = { tabsNavController.popBackStack() },
                    onMuscleGroupSelected = viewModel::onMuscleGroupSelected,
                    onExerciseClick = viewModel::onExerciseSelected,
                    onDismissHistory = viewModel::onDismissHistory
                )
            }

            composable(route = Routes.Profile.name) {
                UserProfileScreen(
                    onBackClick = { tabsNavController.popBackStack() }
                )
            }
        }
    }
}
