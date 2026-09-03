package com.schoolstats.data.repository

import com.schoolstats.data.local.datasource.LocalStatisticsDataSource
import com.schoolstats.data.local.datasource.LocalSubmissionDataSource
import com.schoolstats.data.local.datasource.LocalSchoolDataSource
import com.schoolstats.data.mapper.toDomain
import com.schoolstats.data.remote.datasource.RemoteDashboardDataSource
import com.schoolstats.data.remote.datasource.RemoteSchoolYearDataSource
import com.schoolstats.data.remote.datasource.RemoteStatisticsDataSource
import com.schoolstats.data.remote.datasource.RemoteSubmissionDataSource
import com.schoolstats.data.remote.dto.PrimaryClassStatDto
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SchoolYear
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.SyncStatus
import com.schoolstats.domain.repository.AuthRepository
import com.schoolstats.domain.repository.DashboardRepository
import com.schoolstats.domain.repository.SchoolYearRepository
import com.schoolstats.domain.repository.StatisticsRepository
import com.schoolstats.domain.repository.SubmissionRepository
import com.schoolstats.domain.validation.StatValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SchoolYearRepositoryImpl(
    private val remote: RemoteSchoolYearDataSource,
) : SchoolYearRepository {
    override fun observeActiveYears(): Flow<List<SchoolYear>> = flow {
        emit(remote.fetchActiveYears().map { it.toDomain() })
    }
}

class SubmissionRepositoryImpl(
    private val local: LocalSubmissionDataSource,
    private val remote: RemoteSubmissionDataSource,
) : SubmissionRepository {
    override fun observeSubmissions(statusFilter: String?): Flow<List<Submission>> =
        local.observeSubmissions(statusFilter)

    override suspend fun submitDeclaration(submissionId: String): Result<Unit> = runCatching {
        remote.submit(submissionId)
    }
}

class StatisticsRepositoryImpl(
    private val local: LocalStatisticsDataSource,
    private val remote: RemoteStatisticsDataSource,
) : StatisticsRepository {
    override fun observePrimaryStats(submissionId: String): Flow<List<PrimaryClassStat>> =
        local.observePrimaryStats(submissionId)

    override suspend fun savePrimaryStats(
        submissionId: String,
        schoolId: String,
        stats: List<PrimaryClassStat>,
    ): Result<Unit> = runCatching {
        val errors = StatValidator.validatePrimaryStats(stats)
        if (errors.isNotEmpty()) error(errors.first().message)
        val normalized = stats.map {
            it.copy(
                id = it.id.ifBlank { com.schoolstats.util.randomUuid() },
                submissionId = submissionId,
            )
        }
        local.savePrimaryStats(normalized, schoolId, SyncStatus.PENDING_SYNC)
        val dtos = normalized.map {
            PrimaryClassStatDto(
                id = it.id,
                submissionId = submissionId,
                schoolId = schoolId,
                className = it.className,
                classOrder = it.classOrder,
                boysCount = it.boysCount,
                girlsCount = it.girlsCount,
            )
        }
        runCatching { remote.upsertPrimaryStats(dtos) }
    }
}

class DashboardRepositoryImpl(
    private val localSchool: LocalSchoolDataSource,
    private val localSubmission: LocalSubmissionDataSource,
    private val remote: RemoteDashboardDataSource,
    private val authRepository: AuthRepository,
) : DashboardRepository {
    override fun observeDashboard(schoolYearId: String?): Flow<DashboardStats> = flow {
        val profile = authRepository.currentProfile.value
        val remoteStats = remote.fetchDashboard(profile?.subdivisionId, schoolYearId)?.toDomain()
        if (remoteStats != null) {
            emit(remoteStats)
        } else {
            val totalSchools = localSchool.count().toInt()
            val validated = localSubmission.countByStatus("VALIDE").toInt()
            val submitted = localSubmission.countByStatus("SOUMIS").toInt()
            val rejected = localSubmission.countByStatus("REJETE").toInt()
            val pending = localSubmission.countByStatus("EN_VERIFICATION").toInt()
            emit(
                DashboardStats(
                    totalSchools = totalSchools,
                    schoolsSubmitted = submitted + validated + pending + rejected,
                    schoolsValidated = validated,
                    schoolsPending = pending,
                    schoolsRejected = rejected,
                    schoolsNotSubmitted = (totalSchools - (submitted + validated + pending + rejected)).coerceAtLeast(0),
                ),
            )
        }
    }
}
