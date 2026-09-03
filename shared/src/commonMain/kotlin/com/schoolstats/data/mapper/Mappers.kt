package com.schoolstats.data.mapper

import com.schoolstats.database.Local_primary_stat
import com.schoolstats.database.Local_school
import com.schoolstats.database.Local_submission
import com.schoolstats.data.remote.dto.DashboardDto
import com.schoolstats.data.remote.dto.PrimaryClassStatDto
import com.schoolstats.data.remote.dto.ProfileDto
import com.schoolstats.data.remote.dto.SchoolDto
import com.schoolstats.data.remote.dto.SchoolYearDto
import com.schoolstats.data.remote.dto.SubmissionDto
import com.schoolstats.data.remote.dto.toSchoolOwnership
import com.schoolstats.data.remote.dto.toSchoolType
import com.schoolstats.data.remote.dto.toSubmissionStatus
import com.schoolstats.data.remote.dto.toUserRole
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SchoolEnvironment
import com.schoolstats.domain.model.SchoolYear
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.SyncStatus
import com.schoolstats.domain.model.UserProfile
import com.schoolstats.util.currentTimeMillis
import com.schoolstats.util.randomUuid

fun ProfileDto.toDomain(): UserProfile = UserProfile(
    id = id,
    fullName = fullName,
    email = email,
    phone = phone,
    role = role.toUserRole(),
    schoolId = schoolId,
    subdivisionId = subdivisionId,
    provinceId = provinceId,
    isActive = isActive,
)

fun SchoolDto.toDomain(): School = School(
    id = id,
    subdivisionId = subdivisionId,
    name = name,
    schoolCode = schoolCode,
    dinacopeId = dinacopeId,
    address = address,
    city = city,
    schoolType = schoolType.toSchoolType(),
    ownership = ownership.toSchoolOwnership(),
    environment = environment?.let { runCatching { SchoolEnvironment.valueOf(it) }.getOrNull() },
    isActive = isActive,
    syncStatus = SyncStatus.SYNCED,
    subdivisionName = subdivisions?.name,
    submissionStatus = submissions?.firstOrNull()?.status?.toSubmissionStatus(),
)

fun School.toLocal(syncStatus: SyncStatus = SyncStatus.PENDING_SYNC): Local_school = Local_school(
    id = id.ifBlank { randomUuid() },
    remote_id = if (syncStatus == SyncStatus.SYNCED) id else null,
    subdivision_id = subdivisionId,
    subdivision_name = subdivisionName,
    name = name,
    school_code = schoolCode,
    dinacope_id = dinacopeId,
    address = address,
    city = city,
    school_type = schoolType.name,
    ownership = ownership.name,
    environment = environment?.name,
    is_active = if (isActive) 1L else 0L,
    submission_status = submissionStatus?.name,
    sync_status = syncStatus.name,
    updated_at = currentTimeMillis(),
    is_deleted = 0L,
)

fun Local_school.toDomain(): School = School(
    id = remote_id ?: id,
    subdivisionId = subdivision_id,
    name = name,
    schoolCode = school_code,
    dinacopeId = dinacope_id,
    address = address,
    city = city,
    schoolType = school_type.toSchoolType(),
    ownership = ownership.toSchoolOwnership(),
    environment = environment?.let { runCatching { SchoolEnvironment.valueOf(it) }.getOrNull() },
    isActive = is_active == 1L,
    syncStatus = runCatching { SyncStatus.valueOf(sync_status) }.getOrDefault(SyncStatus.SYNCED),
    subdivisionName = subdivision_name,
    submissionStatus = submission_status?.toSubmissionStatus(),
)

fun SchoolYearDto.toDomain(): SchoolYear = SchoolYear(
    id = id,
    name = name,
    startDate = startDate,
    endDate = endDate,
    isActive = isActive,
)

fun SubmissionDto.toDomain(): Submission = Submission(
    id = id,
    schoolId = schoolId,
    schoolYearId = schoolYearId,
    status = status.toSubmissionStatus(),
    schoolName = schools?.name,
    schoolCode = schools?.schoolCode,
    submittedAt = submittedAt,
    rejectionReason = rejectionReason,
)

fun Local_submission.toDomain(): Submission = Submission(
    id = remote_id ?: id,
    schoolId = school_id,
    schoolYearId = school_year_id,
    status = status.toSubmissionStatus(),
    schoolName = school_name,
    schoolCode = school_code,
    submittedAt = submitted_at,
    rejectionReason = rejection_reason,
    comment = admin_comment,
)

fun DashboardDto.toDomain(): DashboardStats = DashboardStats(
    totalSchools = totalSchools,
    schoolsSubmitted = schoolsSubmitted,
    schoolsValidated = schoolsValidated,
    schoolsPending = schoolsPending,
    schoolsRejected = schoolsRejected,
    schoolsNotSubmitted = schoolsNotSubmitted,
    totalStudents = totalStudents,
    totalTeachers = totalTeachers,
)

fun PrimaryClassStatDto.toDomain(): PrimaryClassStat = PrimaryClassStat(
    id = id,
    submissionId = submissionId,
    className = className,
    classOrder = classOrder,
    boysCount = boysCount,
    girlsCount = girlsCount,
)

fun Local_primary_stat.toDomain(): PrimaryClassStat = PrimaryClassStat(
    id = id,
    submissionId = submission_id,
    className = class_name,
    classOrder = class_order.toInt(),
    boysCount = boys_count.toInt(),
    girlsCount = girls_count.toInt(),
)

fun PrimaryClassStat.toLocal(
    schoolId: String = "",
    syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
): Local_primary_stat = Local_primary_stat(
    id = id.ifBlank { randomUuid() },
    submission_id = submissionId,
    school_id = schoolId,
    class_name = className,
    class_order = classOrder.toLong(),
    boys_count = boysCount.toLong(),
    girls_count = girlsCount.toLong(),
    sync_status = syncStatus.name,
    updated_at = currentTimeMillis(),
)
