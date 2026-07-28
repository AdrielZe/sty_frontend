package com.example.training_tracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import com.example.training_tracker.data.models.isFreestyleWorkout
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.training_tracker.ui.screens.home.HomeUiState
import com.example.training_tracker.ui.screens.home.HomeScreen
import com.example.training_tracker.ui.screens.home.HomeViewModel
import android.net.Uri
import com.example.training_tracker.session_manager.MainViewModel
import com.example.training_tracker.session_manager.SessionManager
import com.example.training_tracker.ui.screens.conquistas.AchievementsScreen
import com.example.training_tracker.ui.screens.conquistas.AchievementsViewModel
import com.example.training_tracker.ui.screens.exercise_data.ExerciseDataScreen
import com.example.training_tracker.ui.screens.exercise_data.ExerciseDataViewModel
import com.example.training_tracker.ui.screens.exercise_data.EXERCISE_NAME_ARG
import com.example.training_tracker.ui.screens.records.RecordsScreen
import com.example.training_tracker.ui.screens.records.RecordsViewModel
import com.example.training_tracker.ui.screens.register_screen.RegisterScreen
import com.example.training_tracker.ui.screens.register_screen.RegisterViewModel
import com.example.training_tracker.ui.screens.registered_workouts.RegisteredWorkoutsScreen
import com.example.training_tracker.ui.screens.user_profile.UserProfileScreen
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryScreen
import com.example.training_tracker.ui.screens.workout_history.WorkoutHistoryViewModel
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainTabsScreen(
    rootNavController: androidx.navigation.NavController,
    homeViewModel: HomeViewModel,
    mainViewModel: MainViewModel
) {
    // Este é o NavController FILHO, exclusivo para as abas
    val tabsNavController = rememberNavController()
    val navBackStackEntry by tabsNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Home.name
    val coroutineScope = rememberCoroutineScope()
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
            StyBottomNavBar(
                currentRoute = currentRoute,
                onNavigate = { tab -> navigateToTab(tab) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { innerPadding ->
        NavHost(
            navController = tabsNavController,
            startDestination = Routes.Home.name,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
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
                                if (clickedWorkout.isFreestyleWorkout()) {
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
                    mainViewModel = mainViewModel,
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
                    onClickGoToLogin = {
                        rootNavController.navigate(Routes.Login.name)
                    },
                    onLogoutClick = {
                        coroutineScope.launch {
                            mainViewModel.logout()
                        }
                    },
                    onRegisterClick = {
                        rootNavController.navigate(Routes.Register.name)
                    }
                )
            }

            composable(route = Routes.ExerciseData.name) {
                val viewModel: ExerciseDataViewModel = viewModel(factory = ExerciseDataViewModel.Factory)
                val uiState by viewModel.uiState.collectAsState()
                ExerciseDataScreen(
                    uiState = uiState,
                    onNavigateBack = { tabsNavController.popBackStack() },
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onMuscleGroupSelected = viewModel::onMuscleGroupSelected,
                    onExerciseClick = { exerciseName ->
                        rootNavController.navigate(
                            "${Routes.ExerciseDetail.name}/${Uri.encode(exerciseName)}"
                        )
                    }
                )
            }

            composable(route = Routes.Achievements.name) {
                val viewModel: AchievementsViewModel = viewModel(factory = AchievementsViewModel.Factory)
                val uiState by viewModel.uiState.collectAsState()
                AchievementsScreen(
                    uiState = uiState,
                    onNavigateBack = { tabsNavController.popBackStack() },
                    onYearSelected = viewModel::onYearSelected,
                    onDaySelected = viewModel::onDaySelected
                )
            }

            composable(route = Routes.RegisteredWorkouts.name) {
                RegisteredWorkoutsScreen(
                    onNavigateBack = { tabsNavController.popBackStack() },
                    onWorkoutClick = { workoutId ->
                        rootNavController.navigate("${Routes.WorkoutDetails.name}/$workoutId/false")
                    },
                    // criar treino abre tela cheia, então usar o ROOT
                    onCreateWorkoutClick = { dayOfWeek ->
                        if (dayOfWeek != null) {
                            rootNavController.navigate("${Routes.CreateWorkout.name}?dayOfWeek=${dayOfWeek.name}")
                        } else {
                            rootNavController.navigate(Routes.CreateWorkout.name)
                        }
                    },
                    onEditWorkout = { workoutId ->
                        rootNavController.navigate("${Routes.WorkoutDetails.name}/$workoutId/false")
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
                    },
                    onDuplicateFromHistory = viewModel::duplicateFromHistory,
                    onPageChange = viewModel::onPageChange
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
                    onDismissHistory = viewModel::onDismissHistory,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onVolumeCardClick = viewModel::onVolumeCardClick,
                    onDismissVolumeHistory = viewModel::onDismissVolumeHistory,
                    onNavigateToExerciseData = {
                        rootNavController.navigate(Routes.ExerciseData.name)
                    },
                )
            }

            composable(route = Routes.Profile.name) {
                UserProfileScreen(
                    onBackClick = { tabsNavController.popBackStack() },
                    onNavigateToAchievements = {
                        navigateToTab(Routes.Achievements.name)
                    }
                )
            }
        }
    }
}
