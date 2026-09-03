package com.schoolstats.domain.repository

import com.schoolstats.domain.model.AppNotification
import com.schoolstats.domain.model.CentralizationStats
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.EnrollmentComparison
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.ManagedUser
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.ReportRequest
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SchoolYear
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.SubmissionStatus
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentProfile: StateFlow<UserProfile?>
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun logout()
    suspend fun refreshProfile(): Result<UserProfile>
    fun isConfigured(): Boolean
    suspend fun seedDemoDataIfNeeded()
}

interface SchoolRepository {
    fun observeSchools(query: String = ""): Flow<List<School>>
    suspend fun getSchool(id: String): School?
    suspend fun saveSchool(school: School): Result<School>
    suspend fun syncSchools(): Result<Unit>
}

interface SchoolYearRepository {
    fun observeActiveYears(): Flow<List<SchoolYear>>
    fun getDefaultYear(): SchoolYear
}

interface SubmissionRepository {
    fun observeSubmissions(statusFilter: String? = null): Flow<List<Submission>>
    suspend fun getOrCreateSubmission(schoolId: String, schoolYearId: String): Submission
    suspend fun submitDeclaration(submissionId: String): Result<Unit>
    suspend fun validateSubmission(submissionId: String, comment: String?): Result<Unit>
    suspend fun rejectSubmission(submissionId: String, reason: String): Result<Unit>
    suspend fun requestCorrection(submissionId: String, comment: String): Result<Unit>
}

interface StatisticsRepository {
    fun observePrimaryStats(submissionId: String): Flow<List<PrimaryClassStat>>
    fun observeSecondaryStats(submissionId: String): Flow<List<SecondaryStudentStat>>
    fun observeTeacherStats(submissionId: String): Flow<List<TeacherStatDetail>>
    fun observeEnrollments(submissionId: String): Flow<List<EnrollmentStat>>
    fun observeCertifications(submissionId: String): Flow<List<CertificationResult>>
    suspend fun savePrimaryStats(submissionId: String, schoolId: String, stats: List<PrimaryClassStat>): Result<Unit>
    suspend fun saveSecondaryStats(submissionId: String, stats: List<SecondaryStudentStat>): Result<Unit>
    suspend fun saveTeacherStats(submissionId: String, stats: List<TeacherStatDetail>): Result<Unit>
    suspend fun saveEnrollments(submissionId: String, stats: List<EnrollmentStat>): Result<Unit>
    suspend fun saveCertifications(submissionId: String, results: List<CertificationResult>): Result<Unit>
    fun compareEnrollments(stats: List<EnrollmentStat>): List<EnrollmentComparison>
}

interface DashboardRepository {
    fun observeDashboard(schoolYearId: String?): Flow<DashboardStats>
}

interface CentralizationRepository {
    fun observeCentralization(submissionIds: List<String>): Flow<CentralizationStats>
}

interface NotificationRepository {
    fun observeNotifications(userId: String): Flow<List<AppNotification>>
    suspend fun markAsRead(id: String)
}

interface UserManagementRepository {
    fun observeUsers(): Flow<List<ManagedUser>>
}

interface ReportRepository {
    suspend fun buildReport(schoolYearName: String, subdivisionName: String?): ReportRequest
}

interface ExportRepository {
    suspend fun exportExcel(report: ReportRequest, path: String): Result<String>
    suspend fun exportPdf(report: ReportRequest, path: String): Result<String>
}
