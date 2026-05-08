package com.example.training_tracker.domain.repository

import com.example.training_tracker.data.models.Records
import kotlinx.coroutines.flow.Flow

interface RecordsRepository{
    val records: Flow<Records?>
    suspend fun getRecord() : Records?
    suspend fun addRecord(record: Records)
    suspend fun updateRecord(record: Records)
    suspend fun deleteRecord(record: Records)
}