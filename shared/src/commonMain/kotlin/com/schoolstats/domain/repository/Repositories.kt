package com.schoolstats.domain.repository

import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SchoolYear
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val currentProfile: StateFlow<UserProfile?>
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun logout()
    suspend fun refreshProfile(): Result<UserProfile>
    fun isConfigured(): Boolean
}

interface SchoolRepository {
    fun observeSchools(query: String = ""): Flow<List<School>>
    suspend fun getSchool(id: String): School?
    suspend fun saveSchool(school: School): Result<School>
    suspend fun syncSchools(): Result<Unit>
}

interface SchoolYearRepository {
    fun observeActiveYears(): Flow<List<SchoolYear>>
}

interface SubmissionRepository {
    fun observeSubmissions(statusFilter: String? = null): Flow<List<Submission>>
    suspend fun submitDeclaration(submissionId: String): Result<Unit>
}

interface StatisticsRepository {
    fun observePrimaryStats(submissionId: String): Flow<List<PrimaryClassStat>>
    suspend fun savePrimaryStats(submissionId: String, schoolId: String, stats: List<PrimaryClassStat>): Result<Unit>
}

interface DashboardRepository {
    fun observeDashboard(schoolYearId: String?): Flow<DashboardStats>
}

interface SessionRepository {
    suspend fun saveSession(accessToken: String, refreshToken: String)
    suspend fun clearSession()
    suspend fun getAccessToken(): String?
}
