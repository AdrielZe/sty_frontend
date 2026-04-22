package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "records")
data class Records(
    @PrimaryKey @ColumnInfo("id") val id: String = UUID.randomUUID().toString(),
    @ColumnInfo("exercisesRecord") val exercisesRecordMap: MutableMap<String, MutableList<Int>> = mutableMapOf(),
    @ColumnInfo("volumeRecordS") var volumeRecords: MutableList<Int>? = mutableListOf(),
    @ColumnInfo("strengthRecord") val strengthRecord: Int = 0,
)