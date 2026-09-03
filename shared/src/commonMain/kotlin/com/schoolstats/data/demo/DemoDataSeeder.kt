package com.schoolstats.data.demo

import com.schoolstats.data.local.datasource.ExtendedStatisticsLocalDataSource
import com.schoolstats.data.local.datasource.LocalSchoolDataSource
import com.schoolstats.data.local.datasource.LocalStatisticsDataSource
import com.schoolstats.data.local.datasource.LocalSubmissionDataSource
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.NotificationType
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SchoolOwnership
import com.schoolstats.domain.model.SchoolType
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.SubmissionStatus
import com.schoolstats.domain.model.SyncStatus
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.AppNotification
import com.schoolstats.util.currentTimeMillis
import com.schoolstats.util.randomUuid

object DemoDataSeeder {
    const val DEMO_SUBDIVISION = "demo-subdivision"
    const val DEMO_YEAR = "2025-2026"
    const val DEMO_YEAR_ID = "year-2025-2026"
    const val DEMO_SUBMISSION_ID = "demo-submission-1"

    suspend fun seedIfEmpty(
        schools: LocalSchoolDataSource,
        submissions: LocalSubmissionDataSource,
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
        adminUserId: String,
    ) {
        if (schools.count() > 0) return

        val schoolList = listOf(
            School("sch-1", DEMO_SUBDIVISION, "École Primaire Limete", "EP-LIM-01", schoolType = SchoolType.PRIMAIRE, subdivisionName = "Limete"),
            School("sch-2", DEMO_SUBDIVISION, "Lycée Technique Bandal", "LT-BAN-02", schoolType = SchoolType.SECONDAIRE, subdivisionName = "Limete"),
            School("sch-3", DEMO_SUBDIVISION, "Institut Mbandaka", "IM-MBA-03", schoolType = SchoolType.PRIMAIRE_ET_SECONDAIRE, ownership = SchoolOwnership.PRIVEE, subdivisionName = "Limete"),
        )
        schoolList.forEach { schools.upsertSchool(it, SyncStatus.SYNCED) }

        val submissionList = listOf(
            Submission(DEMO_SUBMISSION_ID, "sch-1", DEMO_YEAR_ID, SubmissionStatus.SOUMIS, "École Primaire Limete", "EP-LIM-01", "2026-03-01"),
            Submission("demo-submission-2", "sch-2", DEMO_YEAR_ID, SubmissionStatus.EN_VERIFICATION, "Lycée Technique Bandal", "LT-BAN-02"),
            Submission("demo-submission-3", "sch-3", DEMO_YEAR_ID, SubmissionStatus.VALIDE, "Institut Mbandaka", "IM-MBA-03"),
            Submission("demo-submission-4", "sch-1", DEMO_YEAR_ID, SubmissionStatus.BROUILLON, "École Primaire Limete", "EP-LIM-01"),
        )
        submissionList.forEach { submissions.upsertSubmission(it) }

        val primary = listOf(
            PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = 45, girlsCount = 42),
            PrimaryClassStat(className = "2ème année", classOrder = 2, boysCount = 38, girlsCount = 40),
            PrimaryClassStat(className = "3ème année", classOrder = 3, boysCount = 35, girlsCount = 33),
            PrimaryClassStat(className = "4ème année", classOrder = 4, boysCount = 30, girlsCount = 28),
            PrimaryClassStat(className = "5ème année", classOrder = 5, boysCount = 25, girlsCount = 27),
            PrimaryClassStat(className = "6ème année", classOrder = 6, boysCount = 22, girlsCount = 24),
        ).map { it.copy(submissionId = DEMO_SUBMISSION_ID) }
        statistics.savePrimaryStats(primary, "sch-1", SyncStatus.SYNCED)

        extended.saveSecondary(
            DEMO_SUBMISSION_ID,
            listOf(
                SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "3ème", boysCount = 20, girlsCount = 18),
                SecondaryStudentStat(sectionName = "Enseignement Technique", optionName = "Électricité", className = "4ème", boysCount = 15, girlsCount = 8),
            ),
        )
        extended.saveTeachers(
            DEMO_SUBMISSION_ID,
            listOf(
                TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.PRIMAIRE, menCount = 8, womenCount = 12),
                TeacherStatDetail(educationLevel = EducationLevel.LICENCE, branch = TeacherBranch.SECONDAIRE_GENERAL, menCount = 5, womenCount = 7),
            ),
        )
        extended.saveEnrollments(
            DEMO_SUBMISSION_ID,
            listOf(
                EnrollmentStat(className = "1ère année", boysCount = 48, girlsCount = 44, isBeginning = true),
                EnrollmentStat(className = "6ème année", boysCount = 46, girlsCount = 42, isBeginning = true),
                EnrollmentStat(className = "1ère année", boysCount = 45, girlsCount = 42, isBeginning = false),
                EnrollmentStat(className = "6ème année", boysCount = 22, girlsCount = 24, isBeginning = false),
            ),
        )
        extended.saveCertifications(
            DEMO_SUBMISSION_ID,
            listOf(
                CertificationResult(examName = "TENAFEP", className = "6ème année", registeredCount = 50, participantsCount = 48, successesCount = 40, boysSucceeded = 18, girlsSucceeded = 22),
                CertificationResult(examName = "Examen d'État", className = "6ème Humanités", registeredCount = 30, participantsCount = 28, successesCount = 20, boysSucceeded = 9, girlsSucceeded = 11),
            ),
        )

        extended.addNotificationForUser(
            adminUserId,
            AppNotification(
                id = randomUuid(),
                title = "Nouvelle déclaration",
                message = "L'École Primaire Limete a soumis ses statistiques.",
                type = NotificationType.NEW_SUBMISSION,
                createdAt = currentTimeMillis().toString(),
            ),
        )
    }
}
