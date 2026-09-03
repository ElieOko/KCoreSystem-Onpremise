package com.schoolstats.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.schoolstats.data.local.database.SchoolStatsDatabase
import com.schoolstats.domain.model.AppNotification
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.NotificationType
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.util.currentTimeMillis
import com.schoolstats.util.randomUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ExtendedStatisticsLocalDataSource(
    private val database: SchoolStatsDatabase,
) {
    private val queries = database.extendedStatisticsQueries

    fun observeSecondary(submissionId: String): Flow<List<SecondaryStudentStat>> =
        queries.selectSecondaryStats(submissionId).asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map {
                SecondaryStudentStat(
                    id = it.id,
                    submissionId = it.submission_id,
                    sectionName = it.section_name,
                    optionName = it.option_name,
                    className = it.class_name,
                    boysCount = it.boys_count.toInt(),
                    girlsCount = it.girls_count.toInt(),
                )
            }
        }

    fun observeTeachers(submissionId: String): Flow<List<TeacherStatDetail>> =
        queries.selectTeacherStats(submissionId).asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map {
                TeacherStatDetail(
                    id = it.id,
                    submissionId = it.submission_id,
                    educationLevel = EducationLevel.valueOf(it.education_level),
                    branch = TeacherBranch.valueOf(it.branch),
                    menCount = it.men_count.toInt(),
                    womenCount = it.women_count.toInt(),
                )
            }
        }

    fun observeEnrollments(submissionId: String): Flow<List<EnrollmentStat>> =
        queries.selectEnrollmentStats(submissionId).asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map {
                EnrollmentStat(
                    id = it.id,
                    submissionId = it.submission_id,
                    className = it.class_name,
                    boysCount = it.boys_count.toInt(),
                    girlsCount = it.girls_count.toInt(),
                    isBeginning = it.is_beginning == 1L,
                )
            }
        }

    fun observeCertifications(submissionId: String): Flow<List<CertificationResult>> =
        queries.selectCertificationResults(submissionId).asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map {
                CertificationResult(
                    id = it.id,
                    submissionId = it.submission_id,
                    examName = it.exam_name,
                    className = it.class_name,
                    registeredCount = it.registered_count.toInt(),
                    participantsCount = it.participants_count.toInt(),
                    successesCount = it.successes_count.toInt(),
                    boysSucceeded = it.boys_succeeded.toInt(),
                    girlsSucceeded = it.girls_succeeded.toInt(),
                )
            }
        }

    suspend fun saveSecondary(submissionId: String, stats: List<SecondaryStudentStat>) = withContext(Dispatchers.IO) {
        queries.deleteSecondaryStats(submissionId)
        stats.forEach { s ->
            queries.upsertSecondaryStat(
                id = s.id.ifBlank { randomUuid() },
                submission_id = submissionId,
                section_name = s.sectionName,
                option_name = s.optionName,
                class_name = s.className,
                boys_count = s.boysCount.toLong(),
                girls_count = s.girlsCount.toLong(),
                updated_at = currentTimeMillis(),
            )
        }
    }

    suspend fun saveTeachers(submissionId: String, stats: List<TeacherStatDetail>) = withContext(Dispatchers.IO) {
        queries.deleteTeacherStats(submissionId)
        stats.forEach { s ->
            queries.upsertTeacherStat(
                id = s.id.ifBlank { randomUuid() },
                submission_id = submissionId,
                education_level = s.educationLevel.name,
                branch = s.branch.name,
                men_count = s.menCount.toLong(),
                women_count = s.womenCount.toLong(),
                updated_at = currentTimeMillis(),
            )
        }
    }

    suspend fun saveEnrollments(submissionId: String, stats: List<EnrollmentStat>) = withContext(Dispatchers.IO) {
        queries.deleteEnrollmentStats(submissionId)
        stats.forEach { s ->
            queries.upsertEnrollmentStat(
                id = s.id.ifBlank { randomUuid() },
                submission_id = submissionId,
                class_name = s.className,
                boys_count = s.boysCount.toLong(),
                girls_count = s.girlsCount.toLong(),
                is_beginning = if (s.isBeginning) 1L else 0L,
                updated_at = currentTimeMillis(),
            )
        }
    }

    suspend fun saveCertifications(submissionId: String, results: List<CertificationResult>) = withContext(Dispatchers.IO) {
        queries.deleteCertificationResults(submissionId)
        results.forEach { r ->
            queries.upsertCertificationResult(
                id = r.id.ifBlank { randomUuid() },
                submission_id = submissionId,
                exam_name = r.examName,
                class_name = r.className,
                registered_count = r.registeredCount.toLong(),
                participants_count = r.participantsCount.toLong(),
                successes_count = r.successesCount.toLong(),
                boys_succeeded = r.boysSucceeded.toLong(),
                girls_succeeded = r.girlsSucceeded.toLong(),
                updated_at = currentTimeMillis(),
            )
        }
    }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> =
        queries.selectNotifications(userId).asFlow().mapToList(Dispatchers.IO).map { rows ->
            rows.map {
                AppNotification(
                    id = it.id,
                    title = it.title,
                    message = it.message,
                    type = NotificationType.valueOf(it.type),
                    isRead = it.is_read == 1L,
                    createdAt = it.created_at,
                )
            }
        }

    suspend fun addNotification(notification: AppNotification) = withContext(Dispatchers.IO) {
        queries.insertNotification(
            id = notification.id,
            user_id = notification.id.substringBefore("-"),
            title = notification.title,
            message = notification.message,
            type = notification.type.name,
            is_read = if (notification.isRead) 1L else 0L,
            created_at = notification.createdAt,
        )
    }

    suspend fun addNotificationForUser(userId: String, notification: AppNotification) = withContext(Dispatchers.IO) {
        queries.insertNotification(
            id = notification.id,
            user_id = userId,
            title = notification.title,
            message = notification.message,
            type = notification.type.name,
            is_read = if (notification.isRead) 1L else 0L,
            created_at = notification.createdAt,
        )
    }
}
