package com.example.training_tracker.data.local

import androidx.room.TypeConverter
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.data.models.WorkoutHistory
import com.google.gson.reflect.TypeToken
import java.time.DayOfWeek
import java.time.LocalDate

class Converters {
    private val gson = com.google.gson.Gson()

    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String = gson.toJson(value)

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        val listType = object : com.google.gson.reflect.TypeToken<List<Exercise>>() {}.type
        return gson.fromJson(value, listType)
    }

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
    fun fromExerciseSet(value: List<ExerciseSet>): String = gson.toJson(value)

    @TypeConverter
    fun toExerciseSet(value: String): List<ExerciseSet> {
        val listType = object: com.google.gson.reflect.TypeToken<List<ExerciseSet>>() {}.type
        return gson.fromJson(value, listType)
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
        // O TypeToken é necessário para o Gson entender os tipos dentro do Map
        val mapType = object : TypeToken<MutableMap<String, Int>>() {}.type
        return gson.fromJson(jsonString, mapType) ?: mutableMapOf()
    }
}