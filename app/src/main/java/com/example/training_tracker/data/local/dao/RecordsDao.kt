package com.example.training_tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.training_tracker.data.models.Records
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {
    @Query("SELECT * FROM records LIMIT 1")
    fun getRecordFlow(): Flow<Records?>

    // 2. Usado para a nossa lógica de updateRecords() no repositório
    @Query("SELECT * FROM records LIMIT 1")
    fun getRecord(): Records?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: Records)

    @Update
    suspend fun update(record: Records)

    @Delete
    suspend fun delete(record: Records)

    @Query("DELETE FROM records")
    suspend fun deleteAll()
}