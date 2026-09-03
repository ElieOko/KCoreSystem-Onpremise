package com.schoolstats.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.schoolstats.data.local.database.SchoolStatsDatabase
import com.schoolstats.data.mapper.toDomain
import com.schoolstats.data.mapper.toLocal
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalSubmissionDataSource(
    private val database: SchoolStatsDatabase,
) {
    private val queries = database.submissionsQueries

    fun observeSubmissions(statusFilter: String?): Flow<List<Submission>> {
        val source = if (statusFilter.isNullOrBlank()) {
            queries.selectAllSubmissions()
        } else {
            queries.selectSubmissionsByStatus(statusFilter)
        }
        return source.asFlow().mapToList(Dispatchers.IO).map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun upsertSubmission(submission: Submission, syncStatus: SyncStatus = SyncStatus.SYNCED) {
        withContext(Dispatchers.IO) {
            queries.upsertSubmission(
                id = submission.id,
                remote_id = submission.id,
                school_id = submission.schoolId,
                school_year_id = submission.schoolYearId,
                school_name = submission.schoolName,
                school_code = submission.schoolCode,
                status = submission.status.name,
                submitted_at = submission.submittedAt,
                rejection_reason = submission.rejectionReason,
                admin_comment = submission.comment,
                sync_status = syncStatus.name,
                updated_at = com.schoolstats.util.currentTimeMillis(),
            )
        }
    }

    suspend fun countByStatus(status: String): Long = withContext(Dispatchers.IO) {
        queries.countSubmissionsByStatus(status).executeAsOne()
    }
}

class LocalStatisticsDataSource(
    private val database: SchoolStatsDatabase,
) {
    private val queries = database.statisticsQueries

    fun observePrimaryStats(submissionId: String): Flow<List<PrimaryClassStat>> {
        return queries.selectPrimaryStatsBySubmission(submissionId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun savePrimaryStats(stats: List<PrimaryClassStat>, schoolId: String, syncStatus: SyncStatus) {
        withContext(Dispatchers.IO) {
            if (stats.isNotEmpty()) {
                queries.deletePrimaryStatsForSubmission(stats.first().submissionId)
            }
            stats.forEach { stat ->
                val local = stat.toLocal(schoolId = schoolId, syncStatus = syncStatus)
                queries.upsertPrimaryStat(
                    id = local.id,
                    submission_id = local.submission_id,
                    school_id = local.school_id,
                    class_name = local.class_name,
                    class_order = local.class_order,
                    boys_count = local.boys_count,
                    girls_count = local.girls_count,
                    sync_status = local.sync_status,
                    updated_at = local.updated_at,
                )
            }
        }
    }
}
