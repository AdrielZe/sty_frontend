package com.example.training_tracker.domain.classifiers

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class TFLiteExerciseClassifier(private val context: Context) : ExerciseClassifier {

    private var interpreter: Interpreter? = null
    private val vocab = mutableMapOf<String, Int>()
    private val labels = mutableListOf<String>()

    // Tem que ser o mesmo tamanho configurado no Python (MAX_LEN = 10)
    private val MAX_LEN = 10

    init {
// 1. Carrega o modelo puro na memória
        interpreter = Interpreter(loadModelFile("model_v3.tflite"))

        // 2. Carrega o dicionário (Forçando UTF-8 e ignorando linhas vazias)
        context.assets.open("vocab.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
            var realIndex = 0
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    vocab[line.trim()] = realIndex
                    realIndex++
                }
            }
        }

        // 3. Carrega a lista de músculos (Forçando UTF-8 e ignorando linhas vazias)
        context.assets.open("labels.txt").bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.forEach { line ->
                if (line.isNotBlank()) {
                    labels.add(line.trim())
                }
            }
        }
    }

     override fun classify(exerciseName: String): ClassificationResult {
        if (interpreter == null || labels.isEmpty()) return ClassificationResult(null, 0f)

        // 1. Prepara o input
        val words = exerciseName.lowercase().trim().split("\\s+".toRegex())
        val inputArray = Array(1) { FloatArray(MAX_LEN) } // Inicia tudo com Zeros (PAD)

        // 2. Coleta APENAS os IDs que a IA conhece
        var validIndex = 0
        for (word in words) {
            val wordId = vocab[word]
            if (wordId != null && validIndex < MAX_LEN) {
                inputArray[0][validIndex] = wordId.toFloat()
                validIndex++
            }
        }

        val outputArray = Array(1) { FloatArray(labels.size) }

         for (word in words) {
             val wordId = vocab[word]
             Log.d("IA_DEBUG", "Palavra: '$word' -> ID no Vocab: $wordId") // LOG 1

             if (wordId != null && validIndex < MAX_LEN) {
                 inputArray[0][validIndex] = wordId.toFloat()
                 validIndex++
             }
         }


        // 3. Roda a inferência
        interpreter?.run(inputArray, outputArray)



        // 4. Analisa o resultado
        val probabilities = outputArray[0]

         Log.d("IA_DEBUG", "Probabilidades brutas: ${probabilities.joinToString()}")

        var maxIndex = -1
        var maxConfidence = -1f

        for (i in probabilities.indices) {
            if (probabilities[i] > maxConfidence) {
                maxConfidence = probabilities[i]
                maxIndex = i
            }
        }

        val label = if (maxIndex != -1) labels[maxIndex].trim() else null

         Log.d("IA_DEBUG", "Índice vencedor: $maxIndex")
// LOG 3: Qual foi a resposta final que o Kotlin montou
         Log.d("IA_DEBUG", "Resultado final: Músculo = '$label', Confiança = $maxConfidence")
        
        return ClassificationResult(label, maxConfidence)
    }

    // Função auxiliar para converter o arquivo .tflite do disco para a memória RAM (ByteBuffer)
    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }
}