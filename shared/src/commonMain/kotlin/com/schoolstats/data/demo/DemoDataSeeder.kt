package com.schoolstats.data.demo

import com.schoolstats.data.local.datasource.ExtendedStatisticsLocalDataSource
import com.schoolstats.data.local.datasource.LocalSchoolDataSource
import com.schoolstats.data.local.datasource.LocalStatisticsDataSource
import com.schoolstats.data.local.datasource.LocalSubmissionDataSource
import com.schoolstats.domain.census.CensusDefaults
import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AppNotification
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
import com.schoolstats.domain.model.WorkerStat
import com.schoolstats.util.currentTimeMillis
import com.schoolstats.util.randomUuid
import kotlinx.coroutines.flow.first

object DemoDataSeeder {
    const val DEMO_SUBDIVISION = "demo-subdivision"
    const val DEMO_YEAR = "2025-2026"
    const val DEMO_YEAR_ID = "year-2025-2026"
    const val DEMO_SUBMISSION_ID = "demo-submission-1"
    const val DEMO_SUBMISSION_2 = "demo-submission-2"
    const val DEMO_SUBMISSION_3 = "demo-submission-3"

    suspend fun seedIfEmpty(
        schools: LocalSchoolDataSource,
        submissions: LocalSubmissionDataSource,
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
        adminUserId: String,
    ) {
        if (schools.count() == 0L) {
            seedCore(schools, submissions, statistics, extended, adminUserId)
        }
        seedCensusIfMissing(statistics, extended)
    }

    private suspend fun seedCore(
        schools: LocalSchoolDataSource,
        submissions: LocalSubmissionDataSource,
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
        adminUserId: String,
    ) {
        val schoolList = listOf(
            School("sch-1", DEMO_SUBDIVISION, "École Primaire Limete", "EP-LIM-01", schoolType = SchoolType.PRIMAIRE, subdivisionName = "Limete"),
            School("sch-2", DEMO_SUBDIVISION, "Lycée Technique Bandal", "LT-BAN-02", schoolType = SchoolType.SECONDAIRE, subdivisionName = "Limete"),
            School("sch-3", DEMO_SUBDIVISION, "Institut Mbandaka", "IM-MBA-03", schoolType = SchoolType.PRIMAIRE_ET_SECONDAIRE, ownership = SchoolOwnership.PRIVEE, subdivisionName = "Limete"),
        )
        schoolList.forEach { schools.upsertSchool(it, SyncStatus.SYNCED) }

        val submissionList = listOf(
            Submission(DEMO_SUBMISSION_ID, "sch-1", DEMO_YEAR_ID, SubmissionStatus.SOUMIS, "École Primaire Limete", "EP-LIM-01", "2026-03-01"),
            Submission(DEMO_SUBMISSION_2, "sch-2", DEMO_YEAR_ID, SubmissionStatus.EN_VERIFICATION, "Lycée Technique Bandal", "LT-BAN-02", "2026-03-04"),
            Submission(DEMO_SUBMISSION_3, "sch-3", DEMO_YEAR_ID, SubmissionStatus.VALIDE, "Institut Mbandaka", "IM-MBA-03", "2026-02-20"),
            Submission("demo-submission-4", "sch-1", DEMO_YEAR_ID, SubmissionStatus.BROUILLON, "École Primaire Limete", "EP-LIM-01"),
        )
        submissionList.forEach { submissions.upsertSubmission(it) }

        seedPrimaryCensus(statistics, extended)
        seedSecondaryCensus(extended)
        seedMixedCensus(statistics, extended)

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

    private suspend fun seedCensusIfMissing(
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
    ) {
        if (extended.observeAgeSex(DEMO_SUBMISSION_ID).first().isEmpty()) {
            if (statistics.observePrimaryStats(DEMO_SUBMISSION_ID).first().isEmpty()) {
                seedPrimaryCensus(statistics, extended)
            } else {
                val primary = statistics.observePrimaryStats(DEMO_SUBMISSION_ID).first()
                extended.saveAgeSex(
                    DEMO_SUBMISSION_ID,
                    primary.flatMap { CensusDefaults.distributeByAge(it.className, it.classOrder, it.boysCount, it.girlsCount) },
                )
                if (extended.observeWorkers(DEMO_SUBMISSION_ID).first().isEmpty()) {
                    extended.saveWorkers(DEMO_SUBMISSION_ID, primaryWorkers())
                }
                if (extended.observeAdminStaff(DEMO_SUBMISSION_ID).first().isEmpty()) {
                    extended.saveAdminStaff(DEMO_SUBMISSION_ID, primaryAdmin())
                }
            }
        }
        if (extended.observeAgeSex(DEMO_SUBMISSION_2).first().isEmpty()) {
            seedSecondaryCensus(extended)
        }
        if (extended.observeAgeSex(DEMO_SUBMISSION_3).first().isEmpty()) {
            seedMixedCensus(statistics, extended)
        }
    }

    private suspend fun seedPrimaryCensus(
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
    ) {
        val primary = listOf(
            PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = 45, girlsCount = 42),
            PrimaryClassStat(className = "2ème année", classOrder = 2, boysCount = 38, girlsCount = 40),
            PrimaryClassStat(className = "3ème année", classOrder = 3, boysCount = 35, girlsCount = 33),
            PrimaryClassStat(className = "4ème année", classOrder = 4, boysCount = 30, girlsCount = 28),
            PrimaryClassStat(className = "5ème année", classOrder = 5, boysCount = 25, girlsCount = 27),
            PrimaryClassStat(className = "6ème année", classOrder = 6, boysCount = 22, girlsCount = 24),
        ).map { it.copy(submissionId = DEMO_SUBMISSION_ID) }
        statistics.savePrimaryStats(primary, "sch-1", SyncStatus.SYNCED)
        extended.saveTeachers(
            DEMO_SUBMISSION_ID,
            listOf(
                TeacherStatDetail(educationLevel = EducationLevel.D6, branch = TeacherBranch.PRIMAIRE, menCount = 3, womenCount = 5),
                TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.PRIMAIRE, menCount = 8, womenCount = 12),
                TeacherStatDetail(educationLevel = EducationLevel.LICENCE, branch = TeacherBranch.PRIMAIRE, menCount = 2, womenCount = 4),
            ),
        )
        extended.saveEnrollments(
            DEMO_SUBMISSION_ID,
            listOf(
                EnrollmentStat(className = "1ère année", boysCount = 48, girlsCount = 44, isBeginning = true),
                EnrollmentStat(className = "6ème année", boysCount = 24, girlsCount = 26, isBeginning = true),
                EnrollmentStat(className = "1ère année", boysCount = 45, girlsCount = 42, isBeginning = false),
                EnrollmentStat(className = "6ème année", boysCount = 22, girlsCount = 24, isBeginning = false),
            ),
        )
        extended.saveCertifications(
            DEMO_SUBMISSION_ID,
            listOf(
                CertificationResult(examName = "TENAFEP", className = "6ème année", registeredCount = 50, participantsCount = 48, successesCount = 40, boysSucceeded = 18, girlsSucceeded = 22),
            ),
        )
        extended.saveAgeSex(
            DEMO_SUBMISSION_ID,
            primary.flatMap { CensusDefaults.distributeByAge(it.className, it.classOrder, it.boysCount, it.girlsCount) },
        )
        extended.saveWorkers(DEMO_SUBMISSION_ID, primaryWorkers())
        extended.saveAdminStaff(DEMO_SUBMISSION_ID, primaryAdmin())
    }

    private suspend fun seedSecondaryCensus(extended: ExtendedStatisticsLocalDataSource) {
        val secondary = listOf(
            SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "3ème", boysCount = 20, girlsCount = 18),
            SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "4ème", boysCount = 18, girlsCount = 16),
            SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "5ème", boysCount = 16, girlsCount = 14),
            SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "6ème", boysCount = 14, girlsCount = 15),
            SecondaryStudentStat(sectionName = "Enseignement Technique", optionName = "Électricité", className = "3ème", boysCount = 15, girlsCount = 8),
            SecondaryStudentStat(sectionName = "Enseignement Technique", optionName = "Électricité", className = "4ème", boysCount = 12, girlsCount = 6),
        )
        extended.saveSecondary(DEMO_SUBMISSION_2, secondary)
        extended.saveTeachers(
            DEMO_SUBMISSION_2,
            listOf(
                TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.SECONDAIRE_GENERAL, menCount = 6, womenCount = 5),
                TeacherStatDetail(educationLevel = EducationLevel.LICENCE, branch = TeacherBranch.SECONDAIRE_GENERAL, menCount = 5, womenCount = 7),
                TeacherStatDetail(educationLevel = EducationLevel.LICENCE, branch = TeacherBranch.SECONDAIRE_TECHNIQUE, menCount = 4, womenCount = 2),
                TeacherStatDetail(educationLevel = EducationLevel.MASTER, branch = TeacherBranch.SECONDAIRE_GENERAL, menCount = 2, womenCount = 1),
            ),
        )
        extended.saveAgeSex(
            DEMO_SUBMISSION_2,
            secondary.flatMapIndexed { index, stat ->
                CensusDefaults.distributeByAge(stat.className, index + 8, stat.boysCount, stat.girlsCount)
                    .map { it.copy(className = "${stat.optionName} ${stat.className}") }
            },
        )
        extended.saveWorkers(
            DEMO_SUBMISSION_2,
            listOf(
                WorkerStat(educationLevel = EducationLevel.D4, menCount = 5, womenCount = 2),
                WorkerStat(educationLevel = EducationLevel.D6, menCount = 3, womenCount = 1),
                WorkerStat(educationLevel = EducationLevel.AUTRES, menCount = 2, womenCount = 1),
            ),
        )
        extended.saveAdminStaff(DEMO_SUBMISSION_2, secondaryAdmin())
        extended.saveCertifications(
            DEMO_SUBMISSION_2,
            listOf(
                CertificationResult(examName = "Examen d'État", className = "6ème Humanités", registeredCount = 30, participantsCount = 28, successesCount = 20, boysSucceeded = 9, girlsSucceeded = 11),
            ),
        )
        extended.saveEnrollments(
            DEMO_SUBMISSION_2,
            listOf(
                EnrollmentStat(className = "3ème", boysCount = 38, girlsCount = 28, isBeginning = true),
                EnrollmentStat(className = "6ème", boysCount = 16, girlsCount = 16, isBeginning = true),
                EnrollmentStat(className = "3ème", boysCount = 35, girlsCount = 26, isBeginning = false),
                EnrollmentStat(className = "6ème", boysCount = 14, girlsCount = 15, isBeginning = false),
            ),
        )
    }

    private suspend fun seedMixedCensus(
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
    ) {
        val primary = listOf(
            PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = 28, girlsCount = 30),
            PrimaryClassStat(className = "6ème année", classOrder = 6, boysCount = 18, girlsCount = 20),
        ).map { it.copy(submissionId = DEMO_SUBMISSION_3) }
        statistics.savePrimaryStats(primary, "sch-3", SyncStatus.SYNCED)
        extended.saveSecondary(
            DEMO_SUBMISSION_3,
            listOf(
                SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "5ème", boysCount = 12, girlsCount = 14),
                SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "6ème", boysCount = 10, girlsCount = 11),
            ),
        )
        extended.saveTeachers(
            DEMO_SUBMISSION_3,
            listOf(
                TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.PRIMAIRE, menCount = 4, womenCount = 6),
                TeacherStatDetail(educationLevel = EducationLevel.LICENCE, branch = TeacherBranch.SECONDAIRE_GENERAL, menCount = 3, womenCount = 3),
            ),
        )
        extended.saveAgeSex(
            DEMO_SUBMISSION_3,
            primary.flatMap { CensusDefaults.distributeByAge(it.className, it.classOrder, it.boysCount, it.girlsCount) },
        )
        extended.saveWorkers(
            DEMO_SUBMISSION_3,
            listOf(
                WorkerStat(educationLevel = EducationLevel.D4, menCount = 2, womenCount = 1),
                WorkerStat(educationLevel = EducationLevel.D6, menCount = 1, womenCount = 1),
            ),
        )
        extended.saveAdminStaff(
            DEMO_SUBMISSION_3,
            listOf(
                AdminStaffStat(function = AdminStaffFunction.DIRECTEUR, educationLevel = EducationLevel.LICENCE, menCount = 1, womenCount = 0),
                AdminStaffStat(function = AdminStaffFunction.DIRECTEUR_ADJOINT, educationLevel = EducationLevel.GRADUE, menCount = 0, womenCount = 1),
                AdminStaffStat(function = AdminStaffFunction.PREFET, educationLevel = EducationLevel.LICENCE, menCount = 1, womenCount = 0),
                AdminStaffStat(function = AdminStaffFunction.SECRETAIRE, educationLevel = EducationLevel.D6, menCount = 0, womenCount = 1),
            ),
        )
    }

    private fun primaryWorkers() = listOf(
        WorkerStat(educationLevel = EducationLevel.D4, menCount = 4, womenCount = 2),
        WorkerStat(educationLevel = EducationLevel.D6, menCount = 3, womenCount = 1),
        WorkerStat(educationLevel = EducationLevel.AUTRES, menCount = 2, womenCount = 0),
    )

    private fun primaryAdmin() = listOf(
        AdminStaffStat(function = AdminStaffFunction.DIRECTEUR, educationLevel = EducationLevel.LICENCE, menCount = 1, womenCount = 0),
        AdminStaffStat(function = AdminStaffFunction.DIRECTEUR_ADJOINT, educationLevel = EducationLevel.GRADUE, menCount = 0, womenCount = 1),
        AdminStaffStat(function = AdminStaffFunction.SURNUMERAIRE, educationLevel = EducationLevel.D6, menCount = 1, womenCount = 0),
        AdminStaffStat(function = AdminStaffFunction.SECRETAIRE, educationLevel = EducationLevel.D6, menCount = 0, womenCount = 1),
    )

    private fun secondaryAdmin() = listOf(
        AdminStaffStat(function = AdminStaffFunction.DIRECTEUR, educationLevel = EducationLevel.MASTER, menCount = 1, womenCount = 0),
        AdminStaffStat(function = AdminStaffFunction.DIRECTEUR_ADJOINT, educationLevel = EducationLevel.LICENCE, menCount = 0, womenCount = 1),
        AdminStaffStat(function = AdminStaffFunction.PREFET, educationLevel = EducationLevel.LICENCE, menCount = 1, womenCount = 0),
        AdminStaffStat(function = AdminStaffFunction.SECRETAIRE, educationLevel = EducationLevel.D6, menCount = 0, womenCount = 1),
        AdminStaffStat(function = AdminStaffFunction.DIRECTEUR_DES_ETUDES, educationLevel = EducationLevel.LICENCE, menCount = 1, womenCount = 0),
        AdminStaffStat(function = AdminStaffFunction.CONSEILLER_PEDAGOGIQUE, educationLevel = EducationLevel.GRADUE, menCount = 0, womenCount = 1),
        AdminStaffStat(function = AdminStaffFunction.DIRECTEUR_DE_DISCIPLINE, educationLevel = EducationLevel.GRADUE, menCount = 1, womenCount = 0),
        AdminStaffStat(function = AdminStaffFunction.CONSEILLER_ORIENTATION, educationLevel = EducationLevel.LICENCE, menCount = 0, womenCount = 1),
        AdminStaffStat(function = AdminStaffFunction.SURNUMERAIRE, educationLevel = EducationLevel.D6, menCount = 1, womenCount = 1),
    )
}
