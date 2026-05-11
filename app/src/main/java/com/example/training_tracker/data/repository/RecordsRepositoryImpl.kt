package com.example.training_tracker.data.repository

import com.example.training_tracker.data.local.dao.RecordsDao
import com.example.training_tracker.data.models.Records
import com.example.training_tracker.domain.repository.RecordsRepository
import kotlinx.coroutines.flow.Flow

class RecordsRepositoryImpl(
    private val recordsDao: RecordsDao
) : RecordsRepository {
    override val records: Flow<Records?> = recordsDao.getRecordFlow()

    override suspend fun getRecord(): Records? {
        return recordsDao.getRecord()
    }

    override suspend fun addRecord(record: Records) {
        recordsDao.insert(record)
    }

    override suspend fun updateRecord(record: Records) {
        recordsDao.update(record)
    }

    override suspend fun deleteRecord(record: Records) {
        recordsDao.delete(record)
    }
}