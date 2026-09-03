package com.schoolstats.data.remote.dto

import com.schoolstats.domain.model.SchoolOwnership
import com.schoolstats.domain.model.SchoolType
import com.schoolstats.domain.model.SubmissionStatus
import com.schoolstats.domain.model.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    @SerialName("school_id") val schoolId: String? = null,
    @SerialName("subdivision_id") val subdivisionId: String? = null,
    @SerialName("province_id") val provinceId: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
)

@Serializable
data class SchoolDto(
    val id: String,
    @SerialName("subdivision_id") val subdivisionId: String,
    val name: String,
    @SerialName("school_code") val schoolCode: String,
    @SerialName("dinacope_id") val dinacopeId: String? = null,
    val address: String? = null,
    val city: String? = null,
    @SerialName("school_type") val schoolType: String = "PRIMAIRE",
    val ownership: String = "PUBLIQUE",
    val environment: String? = null,
    @SerialName("is_active") val isActive: Boolean = true,
    val subdivisions: SubdivisionRefDto? = null,
    val submissions: List<SubmissionRefDto>? = null,
)

@Serializable
data class SubdivisionRefDto(
    val name: String? = null,
)

@Serializable
data class SubmissionRefDto(
    val status: String,
)

@Serializable
data class SchoolYearDto(
    val id: String,
    val name: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("is_active") val isActive: Boolean,
)

@Serializable
data class SubmissionDto(
    val id: String,
    @SerialName("school_id") val schoolId: String,
    @SerialName("school_year_id") val schoolYearId: String,
    val status: String,
    @SerialName("submitted_at") val submittedAt: String? = null,
    @SerialName("rejection_reason") val rejectionReason: String? = null,
    val schools: SchoolRefDto? = null,
)

@Serializable
data class SchoolRefDto(
    val name: String? = null,
    @SerialName("school_code") val schoolCode: String? = null,
)

@Serializable
data class DashboardDto(
    @SerialName("total_schools") val totalSchools: Int = 0,
    @SerialName("schools_submitted") val schoolsSubmitted: Int = 0,
    @SerialName("schools_validated") val schoolsValidated: Int = 0,
    @SerialName("schools_pending") val schoolsPending: Int = 0,
    @SerialName("schools_rejected") val schoolsRejected: Int = 0,
    @SerialName("schools_not_submitted") val schoolsNotSubmitted: Int = 0,
    @SerialName("total_students") val totalStudents: Int = 0,
    @SerialName("total_teachers") val totalTeachers: Int = 0,
)

@Serializable
data class PrimaryClassStatDto(
    val id: String,
    @SerialName("submission_id") val submissionId: String,
    @SerialName("school_id") val schoolId: String,
    @SerialName("class_name") val className: String,
    @SerialName("class_order") val classOrder: Int,
    @SerialName("boys_count") val boysCount: Int,
    @SerialName("girls_count") val girlsCount: Int,
)

fun String.toUserRole(): UserRole = runCatching { UserRole.valueOf(this) }.getOrDefault(UserRole.ECOLE)
fun String.toSchoolType(): SchoolType = runCatching { SchoolType.valueOf(this) }.getOrDefault(SchoolType.PRIMAIRE)
fun String.toSchoolOwnership(): SchoolOwnership = runCatching { SchoolOwnership.valueOf(this) }.getOrDefault(SchoolOwnership.PUBLIQUE)
fun String.toSubmissionStatus(): SubmissionStatus = runCatching { SubmissionStatus.valueOf(this) }.getOrDefault(SubmissionStatus.BROUILLON)
