package com.example.training_tracker.ui.screens.records

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.repository.ExerciseRepository
import com.example.training_tracker.data.repository.RecordsRepository
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
    private val _selectedExerciseHistory = MutableStateFlow<Pair<String, List<Int>>?>(null)
    private val _exercises = exerciseRepository.exercises

    val uiState: StateFlow<RecordsUiState> = combine(
        recordsRepository.records,
        _selectedMuscleGroup,
        _exercises,
        _selectedExerciseHistory
    ) { records, selectedGroup, allExercises, selectedHistory ->
        
        val filteredRecords = if (selectedGroup == null) {
            records
        } else {
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

        RecordsUiState(
            records = filteredRecords,
            isLoading = false,
            selectedMuscleGroup = selectedGroup,
            selectedExerciseHistory = selectedHistory
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecordsUiState(isLoading = true)
    )

    fun onMuscleGroupSelected(muscleGroup: MuscleGroups?) {
        _selectedMuscleGroup.value = muscleGroup
    }

    fun onExerciseSelected(exerciseName: String, history: List<Int>) {
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