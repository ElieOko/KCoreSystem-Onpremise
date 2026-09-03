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

data class DashboardStats(
    val totalSchools: Int = 0,
    val schoolsSubmitted: Int = 0,
    val schoolsValidated: Int = 0,
    val schoolsPending: Int = 0,
    val schoolsRejected: Int = 0,
    val schoolsNotSubmitted: Int = 0,
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
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
)
