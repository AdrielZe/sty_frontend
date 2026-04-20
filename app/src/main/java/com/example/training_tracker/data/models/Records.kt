package com.example.training_tracker.data.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "records")
data class Records(
    @PrimaryKey @ColumnInfo("id") val id: String = UUID.randomUUID().toString(),
    @ColumnInfo("exercisesRecord") val exercisesRecordMap: MutableMap<String, Int> = mutableMapOf(),
    @ColumnInfo("volumeRecord") val volumeRecord: Int = 0,
    @ColumnInfo("strengthRecord") val strengthRecord: Int = 0,
)