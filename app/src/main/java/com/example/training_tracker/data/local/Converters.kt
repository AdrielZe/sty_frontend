package com.example.training_tracker.data.local

import androidx.room.TypeConverter
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.ExerciseType
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.Technique
import com.example.training_tracker.data.models.WorkoutHistory
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

class Converters {
    private val gson = com.google.gson.GsonBuilder()
        .registerTypeAdapterFactory(SafeEnumTypeAdapterFactory())
        .create()

    @TypeConverter
    fun fromExerciseType(value: ExerciseType) = value.name

    @TypeConverter
    fun toExerciseType(value: String) = runCatching { ExerciseType.valueOf(value) }.getOrDefault(ExerciseType.STRENGTH)

    // Converte o Mapa com histórico de listas para String (JSON)
    @TypeConverter
    fun fromRecordsMap(map: MutableMap<String, MutableList<Double>>?): String {
        return gson.toJson(map ?: mutableMapOf<String, MutableList<Double>>())
    }

    // Converte a String (JSON) de volta para o Mapa com as listas
    @TypeConverter
    fun toRecordsMap(jsonString: String?): MutableMap<String, MutableList<Double>> {
        if (jsonString.isNullOrEmpty()) return mutableMapOf()
        val mapType = object : TypeToken<MutableMap<String, MutableList<Double>>>() {}.type
        return gson.fromJson(jsonString, mapType) ?: mutableMapOf()
    }

    @TypeConverter
    fun fromDoubleList(value: MutableList<Double>?): String {
        return gson.toJson(value ?: mutableListOf<Double>())
    }

    @TypeConverter
    fun toDoubleList(value: String?): MutableList<Double> {
        if (value.isNullOrEmpty()) return mutableListOf()
        val listType = object : TypeToken<MutableList<Double>>() {}.type
        return gson.fromJson(value, listType) ?: mutableListOf()
    }

    @TypeConverter
    fun fromIntList(value: MutableList<Int>?): String {
        return gson.toJson(value ?: mutableListOf<Int>())
    }

    @TypeConverter
    fun toIntList(value: String?): MutableList<Int> {
        if (value.isNullOrEmpty()) return mutableListOf()
        val listType = object : TypeToken<MutableList<Int>>() {}.type
        return gson.fromJson(value, listType) ?: mutableListOf()
    }

    @TypeConverter
    fun fromExerciseList(value: List<Exercise>?): String = gson.toJson(value ?: emptyList<Exercise>())

    @TypeConverter
    fun toExerciseList(value: String?): List<Exercise> {
        // 1. Proteção contra string nula do Room
        if (value.isNullOrBlank()) return emptyList()

        val listType = object : com.google.gson.reflect.TypeToken<List<Exercise>>() {}.type
        val exercises: List<Exercise> = gson.fromJson(value, listType) ?: return emptyList()

        return exercises.map {
            it.copy(
                type = it.type ?: ExerciseType.STRENGTH,
                // 2. A CORREÇÃO DE OURO: Proteção contra Gson injetando null na lista
                exerciseSets = it.exerciseSets?.map { set -> sanitizeExerciseSet(set, it.id) } ?: emptyList()
            )
        }
    }

    // exerciseId e um UUID nao-nulo no Kotlin, mas o Gson usa reflexao e pode
    // injetar null nesse campo ao desserializar dados antigos, ignorando a
    // garantia de null-safety. Sem esse fallback, um exerciseId nulo persiste
    // ate o sync e quebra a constraint NOT NULL no backend.
    private fun sanitizeExerciseSet(it: ExerciseSet, ownerExerciseId: String? = null) = it.copy(
        reps = it.reps ?: "",
        weight = it.weight ?: "",
        previousReps = it.previousReps ?: "",
        previousWeight = it.previousWeight ?: "",
        time = it.time ?: "",
        distance = it.distance ?: "",
        previousTime = it.previousTime ?: "",
        previousDistance = it.previousDistance ?: "",
        technique = it.technique ?: Technique.NORMAL,
        targetReps = it.targetReps ?: "",
        targetWeight = it.targetWeight ?: "",
        exerciseId = it.exerciseId ?: ownerExerciseId?.let { id -> UUID.fromString(id) } ?: it.exerciseId
    )

    @TypeConverter
    fun fromRecords(records: Records?): String? {
        return records?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toRecords(recordsString: String?): Records? {
        return recordsString?.let {
            gson.fromJson(it, Records::class.java)
        }
    }
    @TypeConverter
    fun fromString(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun dateToString(date: LocalDate?): String? {
        return date?.toString() // Salva como "2026-04-08"
    }

    @TypeConverter
    fun fromLocalTime(time: LocalTime?): String? {
        return time?.toString()
    }

    @TypeConverter
    fun toLocalTime(timeString: String?): LocalTime? {
        return timeString?.let { LocalTime.parse(it) }
    }

    @TypeConverter
    fun fromExerciseSet(value: List<ExerciseSet>?): String = gson.toJson(value ?: emptyList<ExerciseSet>())

    @TypeConverter
    fun toExerciseSet(value: String?): List<ExerciseSet> {
        // Proteção contra string nula do Room
        if (value.isNullOrBlank()) return emptyList()

        val listType = object: com.google.gson.reflect.TypeToken<List<ExerciseSet>>() {}.type
        val sets: List<ExerciseSet> = gson.fromJson(value, listType) ?: return emptyList()
        return sets.map { sanitizeExerciseSet(it) }
    }
    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek?): String? = value?.name

    @TypeConverter
    fun toDayOfWeek(value: String?): DayOfWeek? = value?.let { DayOfWeek.valueOf(it) }

    // Ensina o Room a Salvar (Map -> String)
    @TypeConverter
    fun fromMapToString(map: MutableMap<String, Int>): String {
        return gson.toJson(map)
    }

    // Ensina o Room a Ler (String -> Map)
    @TypeConverter
    fun fromStringToMap(jsonString: String): MutableMap<String, Int> {
        val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
        return gson.fromJson(jsonString, mapType) ?: mutableMapOf()
    }
}

// Returns null for any unknown enum value instead of throwing JsonSyntaxException
class SafeEnumTypeAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType
        if (!rawType.isEnum) return null
        val delegate = gson.getDelegateAdapter(this, type)
        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) = delegate.write(out, value)
            override fun read(input: JsonReader): T? {
                if (input.peek() == JsonToken.NULL) { input.nextNull(); return null }
                return runCatching { delegate.read(input) }.getOrElse { input.skipValue(); null }
            }
        }
    }
}