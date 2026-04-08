package com.example.training_tracker.data

import android.content.Context
import com.example.training_tracker.data.local.AppDatabase
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.repository.ExerciseRepository
import com.example.training_tracker.data.repository.UserRepository
import com.example.training_tracker.data.repository.WorkoutHistoryRepository
import com.example.training_tracker.data.repository.WorkoutRepository
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

    init {
        scope.launch {
            // 1. Lemos a primeira emissão (first) que vier do banco de dados
            val currentExercises = exerciseRepository.exercises.first()

            // 2. Se a lista estiver vazia, é porque o usuário acabou de instalar o app
            if (currentExercises.isEmpty()) {
                val defaultExercises = listOf(
// PEITO
                    Exercise(name = "Supino Reto (Barra)", isDefault = true),
                    Exercise(name = "Supino Reto (Halteres)", isDefault = true),
                    Exercise(name = "Supino Inclinado (Barra)", isDefault = true),
                    Exercise(name = "Supino Inclinado (Halteres)", isDefault = true),
                    Exercise(name = "Crucifixo Reto", isDefault = true),
                    Exercise(name = "Peck Deck (Voador)", isDefault = true),
                    Exercise(name = "Crossover (Polia Alta)", isDefault = true),
                    Exercise(name = "Flexão de Braços", isDefault = true),

                    // COSTAS
                    Exercise(name = "Puxada Frontal (Aberta)", isDefault = true),
                    Exercise(name = "Remada Curvada (Barra)", isDefault = true),
                    Exercise(name = "Remada Baixa (Triângulo)", isDefault = true),
                    Exercise(name = "Barra Fixa (Pronada)", isDefault = true),
                    Exercise(name = "Puxada com Triângulo", isDefault = true),
                    Exercise(name = "Remada Cavalinho", isDefault = true),
                    Exercise(name = "Pulldown (Corda)", isDefault = true),
                    Exercise(name = "Levantamento Terra", isDefault = true),

                    // PERNAS
                    Exercise(name = "Agachamento Livre (Barra)", isDefault = true),
                    Exercise(name = "Leg Press 45°", isDefault = true),
                    Exercise(name = "Cadeira Extensora", isDefault = true),
                    Exercise(name = "Mesa Flexora", isDefault = true),
                    Exercise(name = "Cadeira Flexora", isDefault = true),
                    Exercise(name = "Afundo / Passada", isDefault = true),
                    Exercise(name = "Stiff (Halteres)", isDefault = true),
                    Exercise(name = "Panturrilha em Pé (Máquina)", isDefault = true),
                    Exercise(name = "Panturrilha Sentado (Sólio)", isDefault = true),

                    // OMBROS
                    Exercise(name = "Desenvolvimento (Halteres)", isDefault = true),
                    Exercise(name = "Desenvolvimento (Barra)", isDefault = true),
                    Exercise(name = "Elevação Lateral (Halteres)", isDefault = true),
                    Exercise(name = "Elevação Frontal (Halteres)", isDefault = true),
                    Exercise(name = "Crucifixo Inverso (Halteres)", isDefault = true),
                    Exercise(name = "Encolhimento (Halteres)", isDefault = true),

                    // BÍCEPS
                    Exercise(name = "Rosca Direta (Barra W)", isDefault = true),
                    Exercise(name = "Rosca Alternada (Halteres)", isDefault = true),
                    Exercise(name = "Rosca Martelo (Halteres)", isDefault = true),
                    Exercise(name = "Rosca Scott (Máquina)", isDefault = true),
                    Exercise(name = "Rosca Concentrada", isDefault = true),

                    // TRÍCEPS
                    Exercise(name = "Tríceps Pulley (Barra Reta)", isDefault = true),
                    Exercise(name = "Tríceps Corda", isDefault = true),
                    Exercise(name = "Tríceps Testa (Barra W)", isDefault = true),
                    Exercise(name = "Tríceps Francês (Halter)", isDefault = true),
                    Exercise(name = "Mergulho em Paralelas", isDefault = true),
                    Exercise(name = "Tríceps Coice (Polia)", isDefault = true),

                    // ABDÔMEN E CORE
                    Exercise(name = "Abdominal Supra (Solo)", isDefault = true),
                    Exercise(name = "Abdominal Infra (Elevação de Pernas)", isDefault = true),
                    Exercise(name = "Prancha Abdominal", isDefault = true),
                    Exercise(name = "Abdominal Oblíquo", isDefault = true),
                    Exercise(name = "Hiperextensão Lombar", isDefault = true)
                )

                // 3. Inserimos um a um usando o seu próprio repositório
                defaultExercises.forEach { exercise ->
                    exerciseRepository.addExercise(exercise)
                }
            }
        }
    }
}