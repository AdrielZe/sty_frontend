package com.example.training_tracker.ui.utils

import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseType

object CalorieCalculator {

    fun calculate(
        exercises: List<Exercise>,
        durationMs: Long,
        weightKg: Float,
        ageYears: Int? = null,
        gender: String? = null
    ): Int {
        if (exercises.isEmpty() || durationMs <= 0 || weightKg <= 0f) return 0

        val durationMinutes = durationMs / 60_000.0
        val ageFactor = if ((ageYears ?: 0) > 50) 0.9 else 1.0
        val genderFactor = if (gender == "FEMALE") 0.9 else 1.0

        val total = exercises.size.coerceAtLeast(1)
        val strengthExercises = exercises.filter { it.type == ExerciseType.STRENGTH }
        val cardioExercises = exercises.filter { it.type == ExerciseType.CARDIO }
        val stretchingExercises = exercises.filter { it.type == ExerciseType.STRETCHING }

        val strengthFrac = strengthExercises.size.toDouble() / total
        val cardioFrac = cardioExercises.size.toDouble() / total
        val stretchFrac = stretchingExercises.size.toDouble() / total

        var totalCalories = 0.0

        // === MUSCULAÇÃO: MET baseado na taxa de volume (kg·reps/min) ===
        // Quanto mais volume por minuto, maior a intensidade → MET maior
        if (strengthExercises.isNotEmpty()) {
            val totalVolume = strengthExercises.sumOf { exercise ->
                exercise.exerciseSets.sumOf { set ->
                    val w = set.weight.toDoubleOrNull() ?: 0.0
                    val r = set.reps.toIntOrNull() ?: 0
                    w * r
                }
            }
            val strengthMinutes = durationMinutes * strengthFrac
            val volumeRate = if (strengthMinutes > 0) totalVolume / strengthMinutes else 0.0

            val met = when {
                volumeRate < 50 -> 3.5   // leve (ex: exercícios isoladores, pouca carga)
                volumeRate < 150 -> 5.0  // moderado (treino típico)
                volumeRate < 300 -> 6.5  // pesado (agachamento, terra, volume alto)
                else -> 8.0              // muito pesado / circuit training
            }
            totalCalories += met * weightKg * (strengthMinutes / 60.0)
        }

        // === CARDIO: MET baseado na velocidade real (km/h) se disponível ===
        // Se o usuário não registrou distância, usa MET padrão de 8.0
        if (cardioExercises.isNotEmpty()) {
            val cardioMinutes = durationMinutes * cardioFrac

            val totalDistanceKm = cardioExercises.sumOf { exercise ->
                exercise.exerciseSets.sumOf { set ->
                    set.distance?.toDoubleOrNull() ?: 0.0
                }
            }
            val totalCardioSeconds = cardioExercises.sumOf { exercise ->
                exercise.exerciseSets.sumOf { set -> parseTimeToSeconds(set.time) }
            }

            val cardioMet = if (totalDistanceKm > 0 && totalCardioSeconds > 0) {
                val speedKmH = totalDistanceKm / (totalCardioSeconds / 3600.0)
                speedToMet(speedKmH)
            } else {
                8.0 // padrão para cardio sem dados de velocidade
            }

            totalCalories += cardioMet * weightKg * (cardioMinutes / 60.0)
        }

        // === ALONGAMENTO: MET fixo de 2.5 (atividade leve) ===
        if (stretchingExercises.isNotEmpty()) {
            val stretchMinutes = durationMinutes * stretchFrac
            totalCalories += 2.5 * weightKg * (stretchMinutes / 60.0)
        }

        return (totalCalories * ageFactor * genderFactor).toInt()
    }

    // Converte "MM:SS" ou "HH:MM:SS" ou segundos puros para segundos
    private fun parseTimeToSeconds(timeStr: String?): Double {
        if (timeStr.isNullOrBlank()) return 0.0
        val parts = timeStr.split(":")
        return when (parts.size) {
            3 -> (parts[0].toIntOrNull() ?: 0) * 3600.0 +
                 (parts[1].toIntOrNull() ?: 0) * 60.0 +
                 (parts[2].toDoubleOrNull() ?: 0.0)
            2 -> (parts[0].toIntOrNull() ?: 0) * 60.0 +
                 (parts[1].toDoubleOrNull() ?: 0.0)
            else -> timeStr.toDoubleOrNull() ?: 0.0
        }
    }

    // MET baseado na velocidade — valores do Compendium of Physical Activities (Ainsworth et al.)
    private fun speedToMet(speedKmH: Double): Double = when {
        speedKmH < 3.5 -> 2.5   // caminhada muito lenta
        speedKmH < 5.0 -> 3.5   // caminhada normal
        speedKmH < 6.5 -> 5.0   // caminhada rápida
        speedKmH < 8.0 -> 7.0   // corrida leve (trote)
        speedKmH < 10.0 -> 9.0  // corrida moderada
        speedKmH < 13.0 -> 11.0 // corrida rápida
        else -> 13.0             // sprint
    }
}
