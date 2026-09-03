package com.schoolstats.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.schoolstats.data.local.database.SchoolStatsDatabase
import com.schoolstats.data.mapper.toDomain
import com.schoolstats.data.mapper.toLocal
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalSchoolDataSource(
    private val database: SchoolStatsDatabase,
) {
    private val queries = database.organizationQueries

    fun observeSchools(query: String): Flow<List<School>> {
        val source = if (query.isBlank()) {
            queries.selectAllSchools()
        } else {
            queries.searchSchools(query)
        }
        return source.asFlow().mapToList(Dispatchers.IO).map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun upsertSchool(school: School, syncStatus: SyncStatus = SyncStatus.PENDING_SYNC) {
        withContext(Dispatchers.IO) {
            val local = school.toLocal(syncStatus)
            queries.upsertSchool(
                id = local.id,
                remote_id = local.remote_id,
                subdivision_id = local.subdivision_id,
                subdivision_name = local.subdivision_name,
                name = local.name,
                school_code = local.school_code,
                dinacope_id = local.dinacope_id,
                address = local.address,
                city = local.city,
                school_type = local.school_type,
                ownership = local.ownership,
                environment = local.environment,
                is_active = local.is_active,
                submission_status = local.submission_status,
                sync_status = local.sync_status,
                updated_at = local.updated_at,
                is_deleted = local.is_deleted,
            )
        }
    }

    suspend fun replaceAll(schools: List<School>) {
        withContext(Dispatchers.IO) {
            schools.forEach { upsertSchool(it, SyncStatus.SYNCED) }
        }
    }

    suspend fun count(): Long = withContext(Dispatchers.IO) {
        queries.countSchools().executeAsOne()
    }
}
