package com.example.training_tracker.data

import android.app.Application
import android.content.Context
import com.example.training_tracker.data.local.AppDatabase
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.MuscleGroups
import com.example.training_tracker.data.remote.RetrofitClient
import com.example.training_tracker.data.remote.auth.AuthApi
import com.example.training_tracker.data.remote.user.UserApi
import com.example.training_tracker.domain.repository.ExerciseRepository
import com.example.training_tracker.domain.repository.RecordsRepository
import com.example.training_tracker.domain.repository.UserRepository
import com.example.training_tracker.domain.repository.WorkoutHistoryRepository
import com.example.training_tracker.domain.repository.WorkoutRepository
import com.example.training_tracker.domain.classifiers.ExerciseClassifier
import com.example.training_tracker.domain.classifiers.TFLiteExerciseClassifier
import com.example.training_tracker.data.repository.ExerciseRepositoryImpl
import com.example.training_tracker.data.repository.RecordsRepositoryImpl
import com.example.training_tracker.data.repository.UserRepositoryImpl
import com.example.training_tracker.data.repository.WorkoutHistoryImpl
import com.example.training_tracker.data.repository.WorkoutRepositoryImpl
import com.example.training_tracker.session_manager.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

interface AppContainer {
    val userRepository: UserRepository
    val workoutRepository: WorkoutRepository
    val exerciseRepository: ExerciseRepository
    val recordsRepository: RecordsRepository
    val exerciseClassifier: ExerciseClassifier
    val workoutHistoryRepository: WorkoutHistoryRepository
    val sessionManager: SessionManager
    val userApi: UserApi
    val authApi: AuthApi
}

class DefaultAppContainer(
    private val context: Context,
    private val scope: CoroutineScope
) : AppContainer {

    private val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    val exerciseApi = RetrofitClient.exerciseApi

    override val userApi = RetrofitClient.userApi

    override val authApi = RetrofitClient.authApi

    // O 'by lazy' garante que o UserRepository só será instanciado
    // na primeira vez que for chamado, e depois a mesma instância será reutilizada.
    override val userRepository: UserRepository by lazy {
        UserRepositoryImpl(database.userDao(), userApi)
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    override val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepositoryImpl(database.exerciseDao(), exerciseApi)
    }

    override val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepositoryImpl(database.workoutDao())
    }

    override val recordsRepository: RecordsRepository by lazy {
        RecordsRepositoryImpl(database.recordsDao())
    }

    override val workoutHistoryRepository: WorkoutHistoryRepository by lazy {
        WorkoutHistoryImpl(database.workoutHistoryDao())
    }

    override val exerciseClassifier: ExerciseClassifier by lazy {
        TFLiteExerciseClassifier(context)
    }


    init {
        scope.launch {
            val defaultExercises = listOf(
                // PEITO
                Exercise(
                    name = "Supino Reto (Barra)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Reto (Halteres)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Inclinado (Barra)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Inclinado (Halteres)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Declinado (Barra)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Declinado (Halteres)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Crucifixo Reto",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Crucifixo Inclinado",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Peck Deck (Voador)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Crossover (Polia Alta)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Crossover (Polia Baixa)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Crossover (Polia Média)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Flexão de Braços",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Flexão Inclinada",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Flexão Declinada",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Flexão com Palmas Fechadas (Diamante)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Pullover (Halter)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Pullover (Polia)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Smith (Reto)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Smith (Inclinado)",
                    muscleGroup = MuscleGroups.CHEST,
                    isDefault = true
                ),

                // COSTAS
                Exercise(
                    name = "Puxada Frontal (Aberta)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Puxada Frontal (Fechada)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Puxada Supinada (Fechada)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Curvada (Barra)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Curvada (Halteres)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Baixa (Triângulo)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Baixa (Aberta)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Barra Fixa (Pronada)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Barra Fixa (Supinada)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Puxada com Triângulo",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Cavalinho",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Unilateral (Halter)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(name = "Remada Smith", muscleGroup = MuscleGroups.BACK, isDefault = true),
                Exercise(
                    name = "Pulldown (Corda)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Crucifixo inverso",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Levantamento Terra",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Levantamento Terra Romeno",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Levantamento Terra Sumo",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Sentado (Máquina)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Puxada Frontal (Máquina)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Inclinada (Polia)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),
                Exercise(
                    name = "Good Morning (Barra)",
                    muscleGroup = MuscleGroups.BACK,
                    isDefault = true
                ),

                // QUADRÍCEPS
                Exercise(
                    name = "Agachamento Livre (Barra)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Sumô",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Búlgaro",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Goblet (Haltere)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Frontal (Barra)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento no Smith",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Leg Press 45°",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Leg Press Horizontal",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Cadeira Extensora",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Cadeira Extensora Unilateral",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Hack Squat (Máquina)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Afundo / Passada (Halteres)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Afundo / Passada (Barra)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Afundo Reverso",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Step Up (Banco)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Pistol",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Wall Sit (Cadeira na Parede)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Extensão de Joelho (Polia Baixa)",
                    muscleGroup = MuscleGroups.QUADRICEPS,
                    isDefault = true
                ),

// POSTERIORES
                Exercise(
                    name = "Mesa Flexora",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Mesa Flexora Unilateral",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Cadeira Flexora",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Cadeira Flexora Unilateral",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Stiff (Barra)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Stiff (Halteres)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Stiff Unilateral (Haltere)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Levantamento Terra Romeno",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Leg Curl Deitado (Máquina)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Leg Curl em Pé (Máquina)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Flexão de Joelho (Polia Baixa)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Nordic Curl",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Good Morning (Barra)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Sumô (Foco Posterior)",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Ponte de Glúteo com Foco Posterior",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),
                Exercise(
                    name = "Curl com Fitball",
                    muscleGroup = MuscleGroups.HAMSTRINGS,
                    isDefault = true
                ),

// PANTURRILHA
                Exercise(
                    name = "Panturrilha em Pé (Máquina)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha Sentado (Sólio)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha no Leg Press",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha Unilateral (Haltere)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha Unilateral (Sem Peso)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha em Pé (Smith)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha no Hack Squat",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação de Ponta de Pé (Step)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha Burro (Donkey Calf)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Flexão Plantar (Polia Baixa)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Panturrilha Bilateral (Sem Peso)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),
                Exercise(
                    name = "Salto de Panturrilha (Pliométrico)",
                    muscleGroup = MuscleGroups.CALF,
                    isDefault = true
                ),

// GLÚTEO
                Exercise(
                    name = "Hip Thrust (Barra)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Hip Thrust (Haltere)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Hip Thrust Unilateral",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação Pélvica (Solo)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Glúteo no Cabo (Polia Baixa)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Extensão de Quadril (Polia Baixa)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Abdução de Quadril (Máquina)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Abdução de Quadril (Polia Baixa)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Passada com Ênfase no Glúteo",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Sumô (Foco Glúteo)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Coice de Glúteo (Polia Baixa)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Coice de Glúteo (Solo)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Coice de Glúteo (Máquina)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Lateral (Polia)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Step Up com Ênfase no Glúteo",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Afundo Reverso (Foco Glúteo)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Clamshell (Abdução Lateral no Solo)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Agachamento Búlgaro (Foco Glúteo)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Levantamento Terra Sumo (Foco Glúteo)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),
                Exercise(
                    name = "Monster Walk (Elástico)",
                    muscleGroup = MuscleGroups.GLUTE,
                    isDefault = true
                ),

                // OMBROS
                Exercise(
                    name = "Desenvolvimento (Halteres)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Desenvolvimento (Barra)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Desenvolvimento (Máquina)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Desenvolvimento Arnold",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Desenvolvimento Smith",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação Lateral (Halteres)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação Lateral (Polia)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação Lateral (Máquina)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação Frontal (Halteres)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação Frontal (Barra)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação Frontal (Polia)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Crucifixo Inverso (Halteres)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Crucifixo Inverso (Máquina)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Crucifixo Inverso (Polia)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Encolhimento (Halteres)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Encolhimento (Barra)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Encolhimento (Máquina)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Alta (Barra)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Remada Alta (Halteres)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),
                Exercise(
                    name = "Face Pull (Corda)",
                    muscleGroup = MuscleGroups.SHOULDERS,
                    isDefault = true
                ),

                // BÍCEPS
                Exercise(
                    name = "Rosca Direta (Barra W)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Direta (Barra Reta)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Alternada (Halteres)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Simultânea (Halteres)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Martelo (Halteres)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Martelo (Corda)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Scott (Máquina)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Scott (Barra W)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Concentrada",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Inclinada (Halteres)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Inversa (Barra)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Polia Baixa (Barra)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Polia Baixa (Corda)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca 21 (Barra)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Rosca Spyder (Banco Inclinado)",
                    muscleGroup = MuscleGroups.BICEPS,
                    isDefault = true
                ),

                // TRÍCEPS
                Exercise(
                    name = "Tríceps Pulley (Barra Reta)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Pulley (Barra V)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Corda",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Testa (Barra W)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Testa (Halteres)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Francês (Halter)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Francês (Barra W)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Mergulho em Paralelas",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Coice (Polia)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Coice (Halter)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Supino Fechado (Barra)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Banco (Bench Dip)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Polia Alta (Unilateral)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Overhead (Polia)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),
                Exercise(
                    name = "Tríceps Overhead (Halter)",
                    muscleGroup = MuscleGroups.TRICEPS,
                    isDefault = true
                ),

                // ABDÔMEN E CORE
                Exercise(
                    name = "Abdominal Supra (Solo)",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Abdominal Infra (Elevação de Pernas)",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Prancha Abdominal",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Prancha Lateral",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Prancha com Alternância de Braços",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Abdominal Oblíquo",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Abdominal Oblíquo (Bicicleta)",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Hiperextensão Lombar",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Crunch na Máquina",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Crunch com Polia Alta",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Abdominal Canivete",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação de Pernas (Barra Fixa)",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Elevação de Pernas (Paralelas)",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(name = "Russian Twist", muscleGroup = MuscleGroups.ABS, isDefault = true),
                Exercise(name = "Dead Bug", muscleGroup = MuscleGroups.ABS, isDefault = true),
                Exercise(
                    name = "Rollout (Roda Abdominal)",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Mountain Climber",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Vacuum Abdominal",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),
                Exercise(
                    name = "Abdominal Oblíquo (Polia)",
                    muscleGroup = MuscleGroups.ABS,
                    isDefault = true
                ),

                //Cardio
                Exercise(
                    name = "Corrida",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Caminhada",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Bicicleta",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Spinning (Bike Indoor)",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Elíptico",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Esteira (Caminhada Inclinada)",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Escada (Stairmaster)",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Remo Ergométrico",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Pular Corda",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Circuito HIIT",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Burpee",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Jumping Jack",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Step Aeróbico",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Corrida Estacionária",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Air Bike (Assault Bike)",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),
                Exercise(
                    name = "Ski Erg",
                    muscleGroup = MuscleGroups.CARDIO,
                    type = ExerciseType.CARDIO,
                    isDefault = true
                ),

                // ALONGAMENTO
                Exercise(
                    name = "Alongamento de Quadríceps",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Isquiotibiais",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Glúteo (Pombo)",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Peito",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Costas (Gato-Vaca)",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Ombro",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Tríceps",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Bíceps",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Panturrilha",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Flexor de Quadril",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de Adutores (Borboleta)",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento Lombar (Joelhos ao Peito)",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento Cervical",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Rotação de Coluna",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Alongamento de IT Band",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Cobra (Abertura de Peito)",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Downward Dog",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
                Exercise(
                    name = "Pigeon Pose",
                    muscleGroup = MuscleGroups.STRETCHING,
                    type = ExerciseType.STRETCHING,
                    isDefault = true
                ),
            )

            val currentExercises = exerciseRepository.exercises.first()
            val currentExercisesByName = currentExercises.associateBy { it.name }

            defaultExercises.forEach { defaultExercise ->
                val existing = currentExercisesByName[defaultExercise.name]

                when {
                    // Não existe -> insere
                    existing == null -> {
                        exerciseRepository.addExercise(defaultExercise)
                    }
                    // Existe mas algum atributo mudou -> atualiza
                    hasChanges(existing, defaultExercise) -> {
                        exerciseRepository.updateExercise(
                            existing.copy(
                                muscleGroup = defaultExercise.muscleGroup,
                                type = defaultExercise.type,
                                isDefault = defaultExercise.isDefault
                                // adicione outros atributos aqui se necessário
                            )
                        )
                    }
                }
            }
        }
    }
}
private fun hasChanges(existing: Exercise, default: Exercise): Boolean {
    return existing.muscleGroup != default.muscleGroup ||
            existing.type != default.type ||
            existing.isDefault != default.isDefault
}