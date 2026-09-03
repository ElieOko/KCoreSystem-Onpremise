package com.schoolstats.domain.model

enum class UserRole {
    SUPER_ADMIN,
    ADMIN_PROVINCIAL,
    ADMIN_SOUS_DIVISION,
    ECOLE,
}

enum class SubmissionStatus {
    BROUILLON,
    SOUMIS,
    EN_VERIFICATION,
    VALIDE,
    REJETE,
    CORRECTION_DEMANDEE,
}

enum class SyncStatus {
    SYNCED,
    PENDING_SYNC,
    SYNCING,
    SYNC_FAILED,
    CONFLICT,
}

enum class SchoolType {
    PRIMAIRE,
    SECONDAIRE,
    PRIMAIRE_ET_SECONDAIRE,
}

enum class SchoolOwnership {
    PUBLIQUE,
    PRIVEE,
}

enum class SchoolEnvironment {
    URBAIN,
    RURAL,
}

enum class EducationLevel {
    D4, D6, GRADUE, LICENCE, MASTER, DOCTORAT, AUTRES,
}

enum class TeacherBranch {
    PRIMAIRE,
    SECONDAIRE_GENERAL,
    SECONDAIRE_TECHNIQUE,
    SECONDAIRE_PROFESSIONNEL,
    AUTRES,
}

enum class NotificationType {
    DATA_SUBMITTED,
    NEW_SUBMISSION,
    DATA_VALIDATED,
    DATA_REJECTED,
    CORRECTION_REQUESTED,
    SYNC_FAILED,
    SYNC_SUCCESS,
}

data class UserProfile(
    val id: String,
    val fullName: String,
    val email: String,
    val phone: String? = null,
    val role: UserRole,
    val schoolId: String? = null,
    val subdivisionId: String? = null,
    val provinceId: String? = null,
    val isActive: Boolean = true,
)

data class School(
    val id: String,
    val subdivisionId: String,
    val name: String,
    val schoolCode: String,
    val dinacopeId: String? = null,
    val address: String? = null,
    val city: String? = null,
    val communeTerritory: String? = null,
    val sectorQuarter: String? = null,
    val principalName: String? = null,
    val principalPhone: String? = null,
    val schoolType: SchoolType = SchoolType.PRIMAIRE,
    val ownership: SchoolOwnership = SchoolOwnership.PUBLIQUE,
    val environment: SchoolEnvironment? = null,
    val isActive: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val subdivisionName: String? = null,
    val submissionStatus: SubmissionStatus? = null,
)

data class SchoolYear(
    val id: String,
    val name: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean,
)

data class Submission(
    val id: String,
    val schoolId: String,
    val schoolYearId: String,
    val status: SubmissionStatus,
    val schoolName: String? = null,
    val schoolCode: String? = null,
    val submittedAt: String? = null,
    val rejectionReason: String? = null,
    val comment: String? = null,
)

data class PrimaryClassStat(
    val id: String = "",
    val submissionId: String = "",
    val className: String,
    val classOrder: Int,
    val boysCount: Int,
    val girlsCount: Int,
) {
    val totalCount: Int get() = boysCount + girlsCount
}

data class SecondaryStudentStat(
    val id: String = "",
    val submissionId: String = "",
    val sectionName: String,
    val optionName: String? = null,
    val className: String,
    val boysCount: Int,
    val girlsCount: Int,
) {
    val totalCount: Int get() = boysCount + girlsCount
}

data class TeacherStatDetail(
    val id: String = "",
    val submissionId: String = "",
    val educationLevel: EducationLevel,
    val branch: TeacherBranch,
    val menCount: Int,
    val womenCount: Int,
) {
    val totalCount: Int get() = menCount + womenCount
}

data class EnrollmentStat(
    val id: String = "",
    val submissionId: String = "",
    val className: String,
    val boysCount: Int,
    val girlsCount: Int,
    val isBeginning: Boolean,
) {
    val totalCount: Int get() = boysCount + girlsCount
}

data class EnrollmentComparison(
    val className: String,
    val beginningTotal: Int,
    val endTotal: Int,
) {
    val difference: Int get() = beginningTotal - endTotal
    val retentionRate: Double? get() = if (beginningTotal <= 0) null else (endTotal.toDouble() / beginningTotal) * 100
    val dropoutRate: Double? get() = retentionRate?.let { 100 - it }
}

data class CertificationResult(
    val id: String = "",
    val submissionId: String = "",
    val examName: String,
    val className: String,
    val registeredCount: Int,
    val participantsCount: Int,
    val successesCount: Int,
    val boysSucceeded: Int,
    val girlsSucceeded: Int,
) {
    val failuresCount: Int get() = (participantsCount - successesCount).coerceAtLeast(0)
    val successRate: Double? get() = if (participantsCount <= 0) null else (successesCount.toDouble() / participantsCount) * 100
    val participationRate: Double? get() = if (registeredCount <= 0) null else (participantsCount.toDouble() / registeredCount) * 100
}

data class DashboardStats(
    val totalSchools: Int = 0,
    val schoolsSubmitted: Int = 0,
    val schoolsValidated: Int = 0,
    val schoolsPending: Int = 0,
    val schoolsRejected: Int = 0,
    val schoolsNotSubmitted: Int = 0,
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val totalBoys: Int = 0,
    val totalGirls: Int = 0,
)

data class CentralizationStats(
    val totalBoys: Int = 0,
    val totalGirls: Int = 0,
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val byClass: List<PrimaryClassStat> = emptyList(),
    val bySection: List<SecondaryStudentStat> = emptyList(),
    val teachers: List<TeacherStatDetail> = emptyList(),
    val enrollments: List<EnrollmentComparison> = emptyList(),
    val certifications: List<CertificationResult> = emptyList(),
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val createdAt: String,
)

data class ManagedUser(
    val id: String,
    val fullName: String,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
)

data class ReportRequest(
    val title: String,
    val schoolYearName: String,
    val subdivisionName: String?,
    val stats: CentralizationStats,
)
