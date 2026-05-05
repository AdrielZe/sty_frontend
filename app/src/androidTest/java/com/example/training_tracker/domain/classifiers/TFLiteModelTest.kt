package com.example.training_tracker.domain.classifiers

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TFLiteModelTest {

    private lateinit var classifier: TFLiteExerciseClassifier

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        classifier = TFLiteExerciseClassifier(appContext)
    }

    @Test
    fun testModelPredictions() {
        val testCases = mapOf(
            "Supino Reto" to "peito",
            "Rosca Direta" to "biceps",
            "Agachamento" to "perna",
            "Triceps Corda" to "triceps",
            "Remada Curvada" to "costas",
            "Desenvolvimento" to "ombro",
            "Abdominal Supra" to "abdomen",
            "Crucifixo Invertido" to "costas"
        )

        println("\n--- Iniciando Testes da IA TFLite ---")
        
        var successCount = 0
        testCases.forEach { (exercise, expectedMuscle) ->
            val result = classifier.classify(exercise)
            val isCorrect = result.label == expectedMuscle
            
            if (isCorrect) successCount++

            println("""
                Exercício: $exercise
                Esperado: $expectedMuscle
                Resultado: ${result.label ?: "Desconhecido"}
                Certeza: ${(result.confidence * 100).format(2)}%
                Status: ${if (isCorrect) "✅ ACERTOU" else "❌ ERROU"}
                -----------------------------------
            """.trimIndent())
        }

        val accuracy = (successCount.toFloat() / testCases.size) * 100
        println("Resultado Final: $successCount/${testCases.size} acertos (${accuracy.format(2)}% de precisão)")
        
        assertTrue("A precisão da IA está muito baixa: $accuracy%", accuracy > 50)
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)
}
