package com.example.training_tracker.ui.screens.create_workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.training_tracker.GymTrackerApplication
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import com.example.training_tracker.data.repository.ExerciseRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

class CreateWorkoutViewModel(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _draftState = MutableStateFlow(CreateWorkoutUiState())

    val uiState: StateFlow<CreateWorkoutUiState> = combine(
        _draftState,
        exerciseRepository.exercises
    ) { draft, available ->
        draft.copy(availableExercises = available)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CreateWorkoutUiState()
    )

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()


    fun updateWorkoutName(name: String) {
        _draftState.update {
            it.copy(workoutName = name) 
        }
    }

    fun updateSelectedDay(day: DayOfWeek) {
        _draftState.update {
            it.copy(selectedDay = day) 
        }
    }

    fun addExercise(exerciseName: String) {
        if (exerciseName.isBlank()) return
        val nameFormatted = exerciseName
            .trim()
            .split("\\s+".toRegex())
            .joinToString(" ") { word ->
                word.lowercase().replaceFirstChar { it.uppercase() }
            }

        // 1. Verifica se esse exercício já existe na lista do banco de dados
        val existingExercise = uiState.value.availableExercises.find {
            it.name.equals(nameFormatted, ignoreCase = true)
        }

        // 2. Se for novo, salva permanentemente usando o repositório
        if (existingExercise == null) {
            viewModelScope.launch {
                try {
                    val newExerciseToDB = Exercise(name = nameFormatted)
                    exerciseRepository.addExercise(newExerciseToDB)
                } catch (e: Exception) {
                    _uiEvent.send("Erro ao salvar novo exercício: ${e.message}")
                }
            }
        }

        // 3. Adiciona ao treino atual (rascunho)
        val exerciseToAdd = existingExercise ?: Exercise(name = nameFormatted)
        _draftState.update {
            it.copy(exercises = it.exercises + exerciseToAdd)
        }
    }

    fun removeExercise(exercise: Exercise) {
        // 1. Tira da tela imediatamente para não atrapalhar a criação do treino atual
        _draftState.update {
            it.copy(exercises = it.exercises - exercise)
        }

        // 2. Vai no banco de dados e verifica se pode apagar
        viewModelScope.launch {
            try {
                val exerciseInDb = uiState.value.availableExercises.find {
                    it.name.equals(exercise.name, ignoreCase = true)
                }

                exerciseInDb?.let {
                    // A TRAVA AQUI: Só deleta se NÃO for um exercício padrão
                    if (!it.isDefault) {
                        exerciseRepository.deleteExercise(it)
                    }
                }
            } catch (e: Exception) {
                _uiEvent.send("Erro ao excluir do banco: ${e.message}")
            }
        }
    }

    fun saveWorkout() {
        if (_draftState.value.canSave) {
            val currentState = _draftState.value
            val newWorkout = Workout(
                name = currentState.workoutName,
                exercises = currentState.exercises,
                dayOfWeek = currentState.selectedDay
            )
            viewModelScope.launch {
                try {
                    workoutRepository.addWorkout(newWorkout)
                } catch (e: Exception) {
                    _uiEvent.send("Erro ao salvar o treino: ${e.message}")
                }
            }
            _draftState.update { it.copy(isWorkoutSaved = true) }
        } else {
            _draftState.update { it.copy(showErrors = true) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as GymTrackerApplication)
                val workoutRepository = application.container.workoutRepository
                val exerciseRepository = application.container.exerciseRepository
                CreateWorkoutViewModel(workoutRepository = workoutRepository, exerciseRepository = exerciseRepository)
            }
        }
    }
}