package com.hrithikvish.curler.ui.screens.newrequest

import com.hrithikvish.curler.data.history.HistoryDao
import com.hrithikvish.curler.data.history.HistoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FakeHistoryDao : HistoryDao {
    private val entities = MutableStateFlow<List<HistoryEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun insert(entity: HistoryEntity): Long {
        val withId = entity.copy(id = nextId++)
        entities.update { it + withId }
        return withId.id
    }

    override suspend fun delete(entity: HistoryEntity) {
        entities.update { list -> list.filterNot { it.id == entity.id } }
    }

    override suspend fun getById(id: Long): HistoryEntity? = entities.value.firstOrNull { it.id == id }

    override fun observeAll(): Flow<List<HistoryEntity>> = entities.asStateFlow()

    fun currentEntities(): List<HistoryEntity> = entities.value
}
