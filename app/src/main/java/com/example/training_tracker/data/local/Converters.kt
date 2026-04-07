package com.example.training_tracker.data.local

import androidx.room.TypeConverter
import com.example.training_tracker.data.models.Exercise
import java.time.DayOfWeek

class Converters {
    private val gson = com.google.gson.Gson() // Você precisará da lib Gson ou Kotlinx Serialization

    @TypeConverter
    fun fromExerciseList(value: List<Exercise>): String = gson.toJson(value)

    @TypeConverter
    fun toExerciseList(value: String): List<Exercise> {
        val listType = object : com.google.gson.reflect.TypeToken<List<Exercise>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromDayOfWeek(value: DayOfWeek?): String? = value?.name

    @TypeConverter
    fun toDayOfWeek(value: String?): DayOfWeek? = value?.let { DayOfWeek.valueOf(it) }
}