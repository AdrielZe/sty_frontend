package com.example.training_tracker.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.domain.repository.ExerciseRepository
import com.example.training_tracker.domain.repository.RecordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class RecordsViewModel(
    private val recordsRepository: RecordsRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _selectedMuscleGroup = MutableStateFlow<MuscleGroups?>(null)
    private val _selectedExerciseHistory = MutableStateFlow<Pair<String, List<Double>>?>(null)
    private val _exercises = exerciseRepository.exercises

    val uiState: StateFlow<RecordsUiState> = combine(
        recordsRepository.records,
        _selectedMuscleGroup,
        _exercises,
        _selectedExerciseHistory
    ) { records, selectedGroup, allExercises, selectedHistory ->

        val cardioExerciseNames = allExercises
            .filter { it.type == ExerciseType.CARDIO }
            .map { it.name.uppercase() }
            .toSet()

        val filteredStrengthRecords = when {
            selectedGroup == null -> records
            selectedGroup == MuscleGroups.CARDIO -> records?.copy(exercisesRecordMap = mutableMapOf())
            else -> {
                val muscleGroupExercises = allExercises
                    .filter { it.muscleGroup == selectedGroup }
                    .map { it.name.uppercase() }
                    .toSet()
                records?.copy(
                    exercisesRecordMap = records.exercisesRecordMap.filterKeys {
                        muscleGroupExercises.contains(it.uppercase())
                    }.toMutableMap()
                )
            }
        }

        val filteredCardioRecords: Map<String, List<Double>> = when {
            selectedGroup == null || selectedGroup == MuscleGroups.CARDIO ->
                records?.cardioRecordsMap ?: emptyMap()
            else -> emptyMap()
        }

        val selectedIsCardio = selectedHistory != null &&
            cardioExerciseNames.contains(selectedHistory.first.uppercase())

        RecordsUiState(
            records = filteredStrengthRecords,
            isLoading = false,
            selectedMuscleGroup = selectedGroup,
            selectedExerciseHistory = selectedHistory,
            selectedExerciseIsCardio = selectedIsCardio,
            cardioRecords = filteredCardioRecords,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecordsUiState(isLoading = true)
    )

    fun onMuscleGroupSelected(muscleGroup: MuscleGroups?) {
        _selectedMuscleGroup.value = muscleGroup
    }

    fun onExerciseSelected(exerciseName: String, history: List<Double>) {
        _selectedExerciseHistory.value = exerciseName to history
    }

    fun onDismissHistory() {
        _selectedExerciseHistory.value = null
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GymTrackerApplication)
                val recordsRepository = application.container.recordsRepository
                val exerciseRepository = application.container.exerciseRepository
                RecordsViewModel(
                    recordsRepository = recordsRepository,
                    exerciseRepository = exerciseRepository
                )
            }
        }
    }
}