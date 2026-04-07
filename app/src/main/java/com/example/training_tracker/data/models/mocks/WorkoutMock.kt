package com.example.training_tracker.data.models.mocks

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.Workout
import java.time.DayOfWeek

val initialWorkouts =
    listOf(
    Workout(
        id = "mock_monday",
        name = "Treino de Peito",
        dayOfWeek = DayOfWeek.MONDAY,
        exercises = listOf(
            Exercise(name = "Supino Reto"),
            Exercise(name = "Supino Inclinado"),
            Exercise(name = "Crucifixo"),
            Exercise(name = "Voador")
        )
    ),
    Workout(
        id = "mock_tuesday",
        name = "Treino de Costas",
        dayOfWeek = DayOfWeek.TUESDAY,
        exercises = listOf(
            Exercise(name = "Puxada Pulley"),
            Exercise(name = "Remada Baixa"),
            Exercise(name = "Serrote"),
            Exercise(name = "Levantamento Terra")
        )
    ),
    Workout(
        id = "mock_wednesday",
        name = "Treino de Pernas",
        dayOfWeek = DayOfWeek.WEDNESDAY,
        exercises = listOf(
            Exercise(name = "Agachamento Livre"),
            Exercise(name = "Leg Press 45°"),
            Exercise(name = "Cadeira Extensora"),
            Exercise(name = "Mesa Flexora")
        )
    ),
    Workout(
        id = "mock_thursday",
        name = "Treino de Ombros",
        dayOfWeek = DayOfWeek.THURSDAY,
        exercises = listOf(
            Exercise(name = "Desenvolvimento"),
            Exercise(name = "Elevação Lateral"),
            Exercise(name = "Elevação Frontal"),
            Exercise(name = "Encolhimento")
        )
    ),
    Workout(
        id = "mock_friday",
        name = "Treino de Braços",
        dayOfWeek = DayOfWeek.FRIDAY,
        exercises = listOf(
            Exercise(name = "Rosca Direta"),
            Exercise(name = "Tríceps Corda"),
            Exercise(name = "Rosca Martelo"),
            Exercise(name = "Tríceps Testa")
        )
    ),
    Workout(
        id = "mock_saturday",
        name = "Core e Cardio",
        dayOfWeek = DayOfWeek.SATURDAY,
        exercises = listOf(
            Exercise(name = "Abdominal Supra"),
            Exercise(name = "Prancha"),
            Exercise(name = "Caminhada"),
            Exercise(name = "Bike")
        )
    ),
    Workout(
        id = "mock_sunday",
        name = "Descanso Ativo",
        dayOfWeek = DayOfWeek.SUNDAY,
        exercises = listOf(
            Exercise(name = "Alongamento"),
            Exercise(name = "Caminhada Leve")
        )
    )
)