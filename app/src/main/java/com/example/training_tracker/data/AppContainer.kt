package com.example.training_tracker.data

import android.content.Context
import com.example.training_tracker.data.local.AppDatabase
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.repository.ExerciseRepository
import com.example.training_tracker.data.repository.UserRepository
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import com.example.training_tracker.domain.classifiers.TFLiteExerciseClassifier
import com.example.training_tracker.domain.repository.ExerciseRepositoryImpl
import com.example.training_tracker.domain.repository.WorkoutHistoryImpl
import com.example.training_tracker.domain.repository.WorkoutRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Esta interface define o que o container deve fornecer
interface AppContainer {
    val userRepository: UserRepository
    val workoutRepository: WorkoutRepository
    val exerciseRepository: ExerciseRepository

    val exerciseClassifier: ExerciseClassifier
    val workoutHistoryRepository: WorkoutHistoryRepository
}

// Esta é a implementação real do container
class DefaultAppContainer(
    private val context: Context,
    private val scope: CoroutineScope
) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }
    // O 'by lazy' garante que o UserRepository só será instanciado
    // na primeira vez que for chamado, e depois a mesma instância será reutilizada.
    override val userRepository: UserRepository by lazy {
        UserRepository()
    }

    override val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(database.exerciseDao())
    }

    override val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(database.workoutDao())
    }

    override val workoutHistoryRepository: WorkoutHistoryRepository by lazy {
        WorkoutHistoryImpl(database.workoutHistoryDao())
    }

    override val exerciseClassifier: ExerciseClassifier by lazy {
        TFLiteExerciseClassifier(context)
    }


    init {
        scope.launch {
            // 1. Lemos a primeira emissão (first) que vier do banco de dados
            val currentExercises = exerciseRepository.exercises.first()

            // 2. Se a lista estiver vazia, é porque o usuário acabou de instalar o app
            if (currentExercises.isEmpty()) {
                val defaultExercises = listOf(
// PEITO
                    Exercise(name = "Supino Reto (Barra)", muscleGroup = MuscleGroups.CHEST, isDefault = true),
                    Exercise(name = "Supino Reto (Halteres)", muscleGroup = MuscleGroups.CHEST, isDefault = true),
                    Exercise(name = "Supino Inclinado (Barra)", muscleGroup = MuscleGroups.CHEST, isDefault = true),
                    Exercise(name = "Supino Inclinado (Halteres)", muscleGroup = MuscleGroups.CHEST, isDefault = true),
                    Exercise(name = "Crucifixo Reto", muscleGroup = MuscleGroups.CHEST, isDefault = true),
                    Exercise(name = "Peck Deck (Voador)", muscleGroup = MuscleGroups.CHEST, isDefault = true),
                    Exercise(name = "Crossover (Polia Alta)", muscleGroup = MuscleGroups.CHEST, isDefault = true),
                    Exercise(name = "Flexão de Braços", muscleGroup = MuscleGroups.CHEST, isDefault = true),

                    // COSTAS
                    Exercise(name = "Puxada Frontal (Aberta)", muscleGroup = MuscleGroups.BACK, isDefault = true),
                    Exercise(name = "Remada Curvada (Barra)", muscleGroup = MuscleGroups.BACK, isDefault = true),
                    Exercise(name = "Remada Baixa (Triângulo)", muscleGroup = MuscleGroups.BACK, isDefault = true),
                    Exercise(name = "Barra Fixa (Pronada)", muscleGroup = MuscleGroups.BACK, isDefault = true),
                    Exercise(name = "Puxada com Triângulo", muscleGroup = MuscleGroups.BACK, isDefault = true),
                    Exercise(name = "Remada Cavalinho", muscleGroup = MuscleGroups.BACK, isDefault = true),
                    Exercise(name = "Pulldown (Corda)", muscleGroup = MuscleGroups.BACK, isDefault = true),
                    Exercise(name = "Levantamento Terra", muscleGroup = MuscleGroups.BACK, isDefault = true),

                    // PERNAS
                    Exercise(name = "Agachamento Livre (Barra)", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Leg Press 45°", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Cadeira Extensora", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Mesa Flexora", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Cadeira Flexora", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Afundo / Passada", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Stiff (Halteres)", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Panturrilha em Pé (Máquina)", muscleGroup = MuscleGroups.LEGS, isDefault = true),
                    Exercise(name = "Panturrilha Sentado (Sólio)", muscleGroup = MuscleGroups.LEGS, isDefault = true),

                    // OMBROS
                    Exercise(name = "Desenvolvimento (Halteres)", muscleGroup = MuscleGroups.SHOULDERS, isDefault = true),
                    Exercise(name = "Desenvolvimento (Barra)", muscleGroup = MuscleGroups.SHOULDERS, isDefault = true),
                    Exercise(name = "Elevação Lateral (Halteres)", muscleGroup = MuscleGroups.SHOULDERS, isDefault = true),
                    Exercise(name = "Elevação Frontal (Halteres)", muscleGroup = MuscleGroups.SHOULDERS, isDefault = true),
                    Exercise(name = "Crucifixo Inverso (Halteres)", muscleGroup = MuscleGroups.SHOULDERS, isDefault = true),
                    Exercise(name = "Encolhimento (Halteres)", muscleGroup = MuscleGroups.SHOULDERS, isDefault = true),

                    // BÍCEPS
                    Exercise(name = "Rosca Direta (Barra W)", muscleGroup = MuscleGroups.BICEPS, isDefault = true),
                    Exercise(name = "Rosca Alternada (Halteres)", muscleGroup = MuscleGroups.BICEPS, isDefault = true),
                    Exercise(name = "Rosca Martelo (Halteres)", muscleGroup = MuscleGroups.BICEPS, isDefault = true),
                    Exercise(name = "Rosca Scott (Máquina)", muscleGroup = MuscleGroups.BICEPS, isDefault = true),
                    Exercise(name = "Rosca Concentrada", muscleGroup = MuscleGroups.BICEPS, isDefault = true),

                    // TRÍCEPS
                    Exercise(name = "Tríceps Pulley (Barra Reta)", muscleGroup = MuscleGroups.TRICEPS, isDefault = true),
                    Exercise(name = "Tríceps Corda", muscleGroup = MuscleGroups.TRICEPS, isDefault = true),
                    Exercise(name = "Tríceps Testa (Barra W)", muscleGroup = MuscleGroups.TRICEPS, isDefault = true),
                    Exercise(name = "Tríceps Francês (Halter)", muscleGroup = MuscleGroups.TRICEPS, isDefault = true),
                    Exercise(name = "Mergulho em Paralelas", muscleGroup = MuscleGroups.TRICEPS, isDefault = true),
                    Exercise(name = "Tríceps Coice (Polia)", muscleGroup = MuscleGroups.TRICEPS, isDefault = true),

                    // ABDÔMEN E CORE
                    Exercise(name = "Abdominal Supra (Solo)", muscleGroup = MuscleGroups.ABS, isDefault = true),
                    Exercise(name = "Abdominal Infra (Elevação de Pernas)", muscleGroup = MuscleGroups.ABS, isDefault = true),
                    Exercise(name = "Prancha Abdominal", muscleGroup = MuscleGroups.ABS, isDefault = true),
                    Exercise(name = "Abdominal Oblíquo", muscleGroup = MuscleGroups.ABS, isDefault = true),
                    Exercise(name = "Hiperextensão Lombar", muscleGroup = MuscleGroups.ABS, isDefault = true)
                )

                // 3. Inserimos um a um usando o seu próprio repositório
                defaultExercises.forEach { exercise ->
                    exerciseRepository.addExercise(exercise)
                }
            }
        }
    }
}