package com.example.training_tracker.fakes

import com.example.training_tracker.data.models.Records
import com.example.training_tracker.domain.repository.RecordsRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeRecordsRepository(initial: Records? = null) : RecordsRepository {
    val recordsFlow = MutableStateFlow(initial)
    override val records = recordsFlow

    override suspend fun getRecord(): Records? = recordsFlow.value
    override suspend fun addRecord(record: Records) { recordsFlow.value = record }
    override suspend fun updateRecord(record: Records) { recordsFlow.value = record }
    override suspend fun deleteRecord(record: Records) { recordsFlow.value = null }
}
