package com.example.training_tracker.data.local

import androidx.room.TypeConverter
import com.example.training_tracker.data.models.Exercise
import com.example.training_tracker.data.models.ExerciseSet
import com.example.training_tracker.data.models.WorkoutHistory
import java.time.DayOfWeek

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
    fun fromWorkoutHistoryList(value: List<WorkoutHistory>): String = gson.toJson(value)

    @TypeConverter
    fun toWorkoutHistoryList(value: String): List<WorkoutHistory> {
        val listType = object : com.google.gson.reflect.TypeToken<List<WorkoutHistory>>() {}.type
        return gson.fromJson(value, listType)
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
}