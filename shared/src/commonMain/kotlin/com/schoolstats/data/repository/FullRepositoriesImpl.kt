package com.schoolstats.data.repository

import com.schoolstats.data.demo.DemoDataSeeder
import com.schoolstats.data.local.datasource.ExtendedStatisticsLocalDataSource
import com.schoolstats.data.local.datasource.LocalSchoolDataSource
import com.schoolstats.data.local.datasource.LocalStatisticsDataSource
import com.schoolstats.data.local.datasource.LocalSubmissionDataSource
import com.schoolstats.data.mapper.toDomain
import com.schoolstats.data.remote.SupabaseClientProvider
import com.schoolstats.data.remote.datasource.RemoteAuthDataSource
import com.schoolstats.data.remote.datasource.RemoteDashboardDataSource
import com.schoolstats.data.remote.datasource.RemoteProfileDataSource
import com.schoolstats.data.remote.datasource.RemoteSchoolYearDataSource
import com.schoolstats.data.remote.datasource.RemoteStatisticsDataSource
import com.schoolstats.data.remote.datasource.RemoteSubmissionDataSource
import com.schoolstats.data.remote.dto.PrimaryClassStatDto
import com.schoolstats.domain.model.AppNotification
import com.schoolstats.domain.model.CentralizationStats
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.EnrollmentComparison
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.ManagedUser
import com.schoolstats.domain.model.NotificationType
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.ReportRequest
import com.schoolstats.domain.model.SchoolYear
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.SubmissionStatus
import com.schoolstats.domain.model.SyncStatus
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.UserProfile
import com.schoolstats.domain.model.UserRole
import com.schoolstats.domain.repository.AuthRepository
import com.schoolstats.domain.repository.CentralizationRepository
import com.schoolstats.domain.repository.DashboardRepository
import com.schoolstats.domain.repository.NotificationRepository
import com.schoolstats.domain.repository.ReportRepository
import com.schoolstats.domain.repository.SchoolYearRepository
import com.schoolstats.domain.repository.StatisticsRepository
import com.schoolstats.domain.repository.SubmissionRepository
import com.schoolstats.domain.repository.UserManagementRepository
import com.schoolstats.domain.validation.StatValidator
import com.schoolstats.util.currentTimeMillis
import com.schoolstats.util.randomUuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class AuthRepositoryImpl(
    private val clientProvider: SupabaseClientProvider,
    private val remoteAuth: RemoteAuthDataSource,
    private val remoteProfile: RemoteProfileDataSource,
    private val schools: LocalSchoolDataSource,
    private val submissions: LocalSubmissionDataSource,
    private val statistics: LocalStatisticsDataSource,
    private val extended: ExtendedStatisticsLocalDataSource,
) : AuthRepository {
    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    override val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    override fun isConfigured(): Boolean = clientProvider.getOrCreate() != null

    override suspend fun login(email: String, password: String): Result<UserProfile> = runCatching {
        if (!isConfigured()) {
            if (email.equals("demo@local", ignoreCase = true) && password == "demo") {
                val demo = UserProfile(
                    id = "demo-admin",
                    fullName = "Admin Démo",
                    email = email,
                    role = UserRole.ADMIN_SOUS_DIVISION,
                    subdivisionId = DemoDataSeeder.DEMO_SUBDIVISION,
                )
                _currentProfile.value = demo
                seedDemoDataIfNeeded()
                return@runCatching demo
            }
            error("Supabase non configuré. Utilisez demo@local / demo.")
        }
        remoteAuth.login(email, password)
        refreshProfile().getOrThrow()
    }

    override suspend fun seedDemoDataIfNeeded() {
        val profile = _currentProfile.value ?: return
        DemoDataSeeder.seedIfEmpty(schools, submissions, statistics, extended, profile.id)
    }

    override suspend fun logout() {
        runCatching { remoteAuth.logout() }
        clientProvider.clear()
        _currentProfile.value = null
    }

    override suspend fun refreshProfile(): Result<UserProfile> = runCatching {
        val userId = remoteAuth.currentUserId() ?: error("Session invalide")
        val profile = remoteProfile.fetchCurrentProfile(userId).toDomain()
        _currentProfile.value = profile
        profile
    }
}

class SchoolYearRepositoryImpl(
    private val remote: RemoteSchoolYearDataSource,
) : SchoolYearRepository {
    override fun observeActiveYears(): Flow<List<SchoolYear>> = flow {
        val remoteYears = remote.fetchActiveYears().map { it.toDomain() }
        emit(
            remoteYears.ifEmpty {
                listOf(
                    SchoolYear(DemoDataSeeder.DEMO_YEAR_ID, DemoDataSeeder.DEMO_YEAR, "2025-09-01", "2026-06-30", true),
                )
            },
        )
    }

    override fun getDefaultYear(): SchoolYear =
        SchoolYear(DemoDataSeeder.DEMO_YEAR_ID, DemoDataSeeder.DEMO_YEAR, "2025-09-01", "2026-06-30", true)
}

class SubmissionRepositoryImpl(
    private val local: LocalSubmissionDataSource,
    private val remote: RemoteSubmissionDataSource,
    private val notifications: ExtendedStatisticsLocalDataSource,
    private val authRepository: AuthRepository,
) : SubmissionRepository {
    override fun observeSubmissions(statusFilter: String?): Flow<List<Submission>> =
        local.observeSubmissions(statusFilter)

    override suspend fun getOrCreateSubmission(schoolId: String, schoolYearId: String): Submission {
        val existing = local.observeSubmissions(null).first()
            .firstOrNull { it.schoolId == schoolId && it.schoolYearId == schoolYearId }
        if (existing != null) return existing
        val created = Submission(
            id = randomUuid(),
            schoolId = schoolId,
            schoolYearId = schoolYearId,
            status = SubmissionStatus.BROUILLON,
        )
        local.upsertSubmission(created)
        return created
    }

    override suspend fun submitDeclaration(submissionId: String): Result<Unit> = runCatching {
        updateStatus(submissionId, SubmissionStatus.SOUMIS, submittedAt = currentTimeMillis().toString())
        runCatching { remote.submit(submissionId) }
        notifyAdmin(NotificationType.NEW_SUBMISSION, "Nouvelle déclaration soumise.")
    }

    override suspend fun validateSubmission(submissionId: String, comment: String?): Result<Unit> = runCatching {
        updateStatus(submissionId, SubmissionStatus.VALIDE, comment = comment)
        notifyAdmin(NotificationType.DATA_VALIDATED, "Déclaration validée.")
    }

    override suspend fun rejectSubmission(submissionId: String, reason: String): Result<Unit> = runCatching {
        if (reason.isBlank()) error("Le motif de rejet est obligatoire.")
        updateStatus(submissionId, SubmissionStatus.REJETE, rejectionReason = reason)
        notifyAdmin(NotificationType.DATA_REJECTED, "Déclaration rejetée: $reason")
    }

    override suspend fun requestCorrection(submissionId: String, comment: String): Result<Unit> = runCatching {
        if (comment.isBlank()) error("Le commentaire est obligatoire.")
        updateStatus(submissionId, SubmissionStatus.CORRECTION_DEMANDEE, comment = comment)
        notifyAdmin(NotificationType.CORRECTION_REQUESTED, comment)
    }

    private suspend fun updateStatus(
        submissionId: String,
        status: SubmissionStatus,
        submittedAt: String? = null,
        rejectionReason: String? = null,
        comment: String? = null,
    ) {
        val current = local.observeSubmissions(null).first().first { it.id == submissionId }
        local.upsertSubmission(
            current.copy(
                status = status,
                submittedAt = submittedAt ?: current.submittedAt,
                rejectionReason = rejectionReason ?: current.rejectionReason,
                comment = comment ?: current.comment,
            ),
        )
    }

    private suspend fun notifyAdmin(type: NotificationType, message: String) {
        val adminId = authRepository.currentProfile.value?.id ?: return
        notifications.addNotificationForUser(
            adminId,
            AppNotification(
                id = randomUuid(),
                title = type.name,
                message = message,
                type = type,
                createdAt = currentTimeMillis().toString(),
            ),
        )
    }
}

class StatisticsRepositoryImpl(
    private val local: LocalStatisticsDataSource,
    private val extended: ExtendedStatisticsLocalDataSource,
    private val remote: RemoteStatisticsDataSource,
) : StatisticsRepository {
    override fun observePrimaryStats(submissionId: String) = local.observePrimaryStats(submissionId)
    override fun observeSecondaryStats(submissionId: String) = extended.observeSecondary(submissionId)
    override fun observeTeacherStats(submissionId: String) = extended.observeTeachers(submissionId)
    override fun observeEnrollments(submissionId: String) = extended.observeEnrollments(submissionId)
    override fun observeCertifications(submissionId: String) = extended.observeCertifications(submissionId)

    override suspend fun savePrimaryStats(submissionId: String, schoolId: String, stats: List<PrimaryClassStat>): Result<Unit> =
        save(stats, StatValidator::validatePrimaryClassStat) {
            val normalized = it.map { s -> s.copy(id = s.id.ifBlank { randomUuid() }, submissionId = submissionId) }
            local.savePrimaryStats(normalized, schoolId, SyncStatus.PENDING_SYNC)
            runCatching {
                remote.upsertPrimaryStats(
                    normalized.map { s ->
                        PrimaryClassStatDto(s.id, submissionId, schoolId, s.className, s.classOrder, s.boysCount, s.girlsCount)
                    },
                )
            }
        }

    override suspend fun saveSecondaryStats(submissionId: String, stats: List<SecondaryStudentStat>): Result<Unit> =
        save(stats, StatValidator::validateSecondaryStat) { extended.saveSecondary(submissionId, it) }

    override suspend fun saveTeacherStats(submissionId: String, stats: List<TeacherStatDetail>): Result<Unit> =
        save(stats, StatValidator::validateTeacherStat) { extended.saveTeachers(submissionId, it) }

    override suspend fun saveEnrollments(submissionId: String, stats: List<EnrollmentStat>): Result<Unit> =
        save(stats, StatValidator::validateEnrollment) { extended.saveEnrollments(submissionId, it) }

    override suspend fun saveCertifications(submissionId: String, results: List<CertificationResult>): Result<Unit> =
        save(results, StatValidator::validateCertification) { extended.saveCertifications(submissionId, it) }

    override fun compareEnrollments(stats: List<EnrollmentStat>): List<EnrollmentComparison> {
        val beginning = stats.filter { it.isBeginning }.associateBy { it.className }
        val end = stats.filter { !it.isBeginning }.associateBy { it.className }
        return (beginning.keys + end.keys).distinct().sorted().map { className ->
            EnrollmentComparison(
                className = className,
                beginningTotal = beginning[className]?.totalCount ?: 0,
                endTotal = end[className]?.totalCount ?: 0,
            )
        }
    }

    private suspend fun <T> save(
        items: List<T>,
        validator: (T) -> List<com.schoolstats.domain.validation.ValidationError>,
        block: suspend (List<T>) -> Unit,
    ): Result<Unit> = runCatching {
        val errors = items.flatMap(validator)
        if (errors.isNotEmpty()) error(errors.first().message)
        block(items)
    }
}

class DashboardRepositoryImpl(
    private val localSchool: LocalSchoolDataSource,
    private val localSubmission: LocalSubmissionDataSource,
    private val statistics: LocalStatisticsDataSource,
    private val extended: ExtendedStatisticsLocalDataSource,
    private val remote: RemoteDashboardDataSource,
    private val authRepository: AuthRepository,
) : DashboardRepository {
    override fun observeDashboard(schoolYearId: String?): Flow<DashboardStats> = flow {
        val profile = authRepository.currentProfile.value
        remote.fetchDashboard(profile?.subdivisionId, schoolYearId)?.toDomain()?.let {
            emit(it)
            return@flow
        }
        val totalSchools = localSchool.count().toInt()
        val validated = localSubmission.countByStatus("VALIDE").toInt()
        val submitted = localSubmission.countByStatus("SOUMIS").toInt()
        val rejected = localSubmission.countByStatus("REJETE").toInt()
        val pending = localSubmission.countByStatus("EN_VERIFICATION").toInt()
        val primary = statistics.observePrimaryStats(DemoDataSeeder.DEMO_SUBMISSION_ID).first()
        val teachers = extended.observeTeachers(DemoDataSeeder.DEMO_SUBMISSION_ID).first()
        val totalBoys = primary.sumOf { it.boysCount }
        val totalGirls = primary.sumOf { it.girlsCount }
        emit(
            DashboardStats(
                totalSchools = totalSchools,
                schoolsSubmitted = submitted + validated + pending + rejected,
                schoolsValidated = validated,
                schoolsPending = pending,
                schoolsRejected = rejected,
                schoolsNotSubmitted = (totalSchools - (submitted + validated + pending + rejected)).coerceAtLeast(0),
                totalStudents = totalBoys + totalGirls,
                totalTeachers = teachers.sumOf { it.totalCount },
                totalBoys = totalBoys,
                totalGirls = totalGirls,
            ),
        )
    }
}

class CentralizationRepositoryImpl(
    private val submissions: LocalSubmissionDataSource,
    private val statistics: LocalStatisticsDataSource,
    private val extended: ExtendedStatisticsLocalDataSource,
) : CentralizationRepository {
    override fun observeCentralization(submissionIds: List<String>): Flow<CentralizationStats> {
        val ids = submissionIds.ifEmpty { listOf(DemoDataSeeder.DEMO_SUBMISSION_ID) }
        val flows = ids.map { id ->
            combine(
                statistics.observePrimaryStats(id),
                extended.observeSecondary(id),
                extended.observeTeachers(id),
                extended.observeEnrollments(id),
                extended.observeCertifications(id),
            ) { primary, secondary, teachers, enrollments, certs ->
                CentralizationStats(
                    totalBoys = primary.sumOf { it.boysCount } + secondary.sumOf { it.boysCount },
                    totalGirls = primary.sumOf { it.girlsCount } + secondary.sumOf { it.girlsCount },
                    totalStudents = primary.sumOf { it.totalCount } + secondary.sumOf { it.totalCount },
                    totalTeachers = teachers.sumOf { it.totalCount },
                    byClass = primary,
                    bySection = secondary,
                    teachers = teachers,
                    enrollments = compareEnrollments(enrollments),
                    certifications = certs,
                )
            }
        }
        return if (flows.size == 1) flows.first() else combine(flows) { list ->
            CentralizationStats(
                totalBoys = list.sumOf { it.totalBoys },
                totalGirls = list.sumOf { it.totalGirls },
                totalStudents = list.sumOf { it.totalStudents },
                totalTeachers = list.sumOf { it.totalTeachers },
                byClass = list.flatMap { it.byClass },
                bySection = list.flatMap { it.bySection },
                teachers = list.flatMap { it.teachers },
                enrollments = list.flatMap { it.enrollments },
                certifications = list.flatMap { it.certifications },
            )
        }
    }

    private fun compareEnrollments(stats: List<EnrollmentStat>): List<EnrollmentComparison> {
        val beginning = stats.filter { it.isBeginning }.associateBy { it.className }
        val end = stats.filter { !it.isBeginning }.associateBy { it.className }
        return (beginning.keys + end.keys).distinct().sorted().map { className ->
            EnrollmentComparison(
                className = className,
                beginningTotal = beginning[className]?.totalCount ?: 0,
                endTotal = end[className]?.totalCount ?: 0,
            )
        }
    }
}

class NotificationRepositoryImpl(
    private val extended: ExtendedStatisticsLocalDataSource,
) : NotificationRepository {
    override fun observeNotifications(userId: String) = extended.observeNotifications(userId)
    override suspend fun markAsRead(id: String) { /* local update optional */ }
}

class UserManagementRepositoryImpl : UserManagementRepository {
    override fun observeUsers(): Flow<List<ManagedUser>> = flow {
        emit(
            listOf(
                ManagedUser("demo-admin", "Admin Démo", "demo@local", UserRole.ADMIN_SOUS_DIVISION, true),
                ManagedUser("u-2", "Directeur École", "ecole@local", UserRole.ECOLE, true),
                ManagedUser("u-3", "Admin Provincial", "prov@local", UserRole.ADMIN_PROVINCIAL, true),
            ),
        )
    }
}

class ReportRepositoryImpl(
    private val centralization: CentralizationRepository,
    private val schoolYearRepository: SchoolYearRepository,
) : ReportRepository {
    override suspend fun buildReport(schoolYearName: String, subdivisionName: String?): ReportRequest {
        val stats = centralization.observeCentralization(emptyList()).first()
        return ReportRequest(
            title = "Rapport statistiques scolaires",
            schoolYearName = schoolYearName,
            subdivisionName = subdivisionName,
            stats = stats,
        )
    }
}
