package com.schoolstats.data.repository

import com.schoolstats.data.local.datasource.LocalSchoolDataSource
import com.schoolstats.data.mapper.toDomain
import com.schoolstats.data.remote.datasource.RemoteSchoolDataSource
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SyncStatus
import com.schoolstats.domain.repository.SchoolRepository
import com.schoolstats.util.randomUuid
import kotlinx.coroutines.flow.Flow

class SchoolRepositoryImpl(
    private val local: LocalSchoolDataSource,
    private val remote: RemoteSchoolDataSource,
    private val syncManager: SyncManager,
) : SchoolRepository {
    override fun observeSchools(query: String): Flow<List<School>> = local.observeSchools(query)

    override suspend fun getSchool(id: String): School? {
        return local.observeSchools("").let { flow ->
            var result: School? = null
            flow.collect { schools -> result = schools.find { it.id == id }; return@collect }
            result
        }
    }

    override suspend fun saveSchool(school: School): Result<School> = runCatching {
        val toSave = if (school.id.isBlank()) school.copy(id = randomUuid()) else school
        local.upsertSchool(toSave, SyncStatus.PENDING_SYNC)
        syncManager.scheduleSync()
        toSave.copy(syncStatus = SyncStatus.PENDING_SYNC)
    }

    override suspend fun syncSchools(): Result<Unit> = runCatching {
        val remoteSchools = remote.fetchSchools().map { it.toDomain() }
        if (remoteSchools.isNotEmpty()) {
            local.replaceAll(remoteSchools)
        }
    }
}
