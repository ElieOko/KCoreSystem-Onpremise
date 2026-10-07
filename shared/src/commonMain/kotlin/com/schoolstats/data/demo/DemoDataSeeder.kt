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
            School("sch-4", DEMO_SUBDIVISION, "École Maternelle Ngiri-Ngiri", "EM-NGI-04", schoolType = SchoolType.MATERNELLE, subdivisionName = "Limete"),
        )
        schoolList.forEach { schools.upsertSchool(it, SyncStatus.SYNCED) }

        val submissionList = listOf(
            Submission(DEMO_SUBMISSION_ID, "sch-1", DEMO_YEAR_ID, SubmissionStatus.SOUMIS, "École Primaire Limete", "EP-LIM-01", "2026-03-01"),
            Submission(DEMO_SUBMISSION_2, "sch-2", DEMO_YEAR_ID, SubmissionStatus.EN_VERIFICATION, "Lycée Technique Bandal", "LT-BAN-02", "2026-03-04"),
            Submission(DEMO_SUBMISSION_3, "sch-3", DEMO_YEAR_ID, SubmissionStatus.VALIDE, "Institut Mbandaka", "IM-MBA-03", "2026-02-20"),
            Submission("demo-submission-4", "sch-1", DEMO_YEAR_ID, SubmissionStatus.BROUILLON, "École Primaire Limete", "EP-LIM-01"),
            Submission("demo-submission-5", "sch-4", DEMO_YEAR_ID, SubmissionStatus.VALIDE, "École Maternelle Ngiri-Ngiri", "EM-NGI-04", "2026-02-18"),
        )
        submissionList.forEach { submissions.upsertSubmission(it) }

        seedPrimaryCensus(statistics, extended)
        seedSecondaryCensus(extended)
        seedMixedCensus(statistics, extended)
        seedPreschoolCensus(statistics, extended)

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
        if (statistics.observePrimaryStats("demo-submission-5").first().isEmpty()) {
            seedPreschoolCensus(statistics, extended)
        }
    }

    private suspend fun seedPreschoolCensus(
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
    ) {
        val preschool = listOf(
            PrimaryClassStat(className = "1ère maternelle", classOrder = 1, boysCount = 22, girlsCount = 24),
            PrimaryClassStat(className = "2ème maternelle", classOrder = 2, boysCount = 20, girlsCount = 21),
            PrimaryClassStat(className = "3ème maternelle", classOrder = 3, boysCount = 18, girlsCount = 19),
        ).map { it.copy(submissionId = "demo-submission-5") }
        statistics.savePrimaryStats(preschool, "sch-4", SyncStatus.SYNCED)
        extended.saveTeachers(
            "demo-submission-5",
            listOf(
                TeacherStatDetail(educationLevel = EducationLevel.D6, branch = TeacherBranch.MATERNELLE, menCount = 0, womenCount = 2),
                TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.MATERNELLE, menCount = 1, womenCount = 4),
            ),
        )
        extended.saveAgeSex(
            "demo-submission-5",
            preschool.flatMap { CensusDefaults.distributeByAge(it.className, it.classOrder, it.boysCount, it.girlsCount) },
        )
        extended.saveEnrollments(
            "demo-submission-5",
            listOf(
                EnrollmentStat(className = "1ère maternelle", boysCount = 24, girlsCount = 25, isBeginning = true),
                EnrollmentStat(className = "3ème maternelle", boysCount = 19, girlsCount = 20, isBeginning = true),
                EnrollmentStat(className = "1ère maternelle", boysCount = 22, girlsCount = 24, isBeginning = false),
                EnrollmentStat(className = "3ème maternelle", boysCount = 18, girlsCount = 19, isBeginning = false),
            ),
        )
        extended.saveWorkers("demo-submission-5", listOf(WorkerStat(educationLevel = EducationLevel.D4, menCount = 1, womenCount = 1)))
        extended.saveAdminStaff(
            "demo-submission-5",
            listOf(
                AdminStaffStat(function = AdminStaffFunction.DIRECTEUR, educationLevel = EducationLevel.LICENCE, menCount = 0, womenCount = 1),
                AdminStaffStat(function = AdminStaffFunction.SECRETAIRE, educationLevel = EducationLevel.D6, menCount = 0, womenCount = 1),
            ),
        )
    }

    private suspend fun seedPrimaryCensus(
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
    ) {
        val primary = listOf(
            PrimaryClassStat(className = "1ère année primaire", classOrder = 4, boysCount = 45, girlsCount = 42),
            PrimaryClassStat(className = "2ème année primaire", classOrder = 5, boysCount = 38, girlsCount = 40),
            PrimaryClassStat(className = "3ème année primaire", classOrder = 6, boysCount = 35, girlsCount = 33),
            PrimaryClassStat(className = "4ème année primaire", classOrder = 7, boysCount = 30, girlsCount = 28),
            PrimaryClassStat(className = "5ème année primaire", classOrder = 8, boysCount = 25, girlsCount = 27),
            PrimaryClassStat(className = "6ème année primaire", classOrder = 9, boysCount = 22, girlsCount = 24),
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
                EnrollmentStat(className = "1ère année primaire", boysCount = 48, girlsCount = 44, isBeginning = true),
                EnrollmentStat(className = "6ème année primaire", boysCount = 24, girlsCount = 26, isBeginning = true),
                EnrollmentStat(className = "1ère année primaire", boysCount = 45, girlsCount = 42, isBeginning = false),
                EnrollmentStat(className = "6ème année primaire", boysCount = 22, girlsCount = 24, isBeginning = false),
            ),
        )
        extended.saveCertifications(
            DEMO_SUBMISSION_ID,
            listOf(
                CertificationResult(examName = "TENAFEP", className = "6ème année primaire", registeredCount = 50, participantsCount = 48, successesCount = 40, boysSucceeded = 18, girlsSucceeded = 22),
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
            SecondaryStudentStat(sectionName = "Tronc commun", optionName = null, className = "7ème année (1ère secondaire)", boysCount = 28, girlsCount = 26),
            SecondaryStudentStat(sectionName = "Tronc commun", optionName = null, className = "8ème année (2ème secondaire)", boysCount = 24, girlsCount = 22),
            SecondaryStudentStat(sectionName = "Humanités Générales", optionName = "Latin-Philosophie", className = "1ère des Humanités", boysCount = 20, girlsCount = 18),
            SecondaryStudentStat(sectionName = "Humanités Générales", optionName = "Latin-Philosophie", className = "2ème des Humanités", boysCount = 18, girlsCount = 16),
            SecondaryStudentStat(sectionName = "Humanités Générales", optionName = "Latin-Philosophie", className = "3ème des Humanités", boysCount = 16, girlsCount = 14),
            SecondaryStudentStat(sectionName = "Humanités Générales", optionName = "Latin-Philosophie", className = "4ème des Humanités", boysCount = 14, girlsCount = 15),
            SecondaryStudentStat(sectionName = "Humanités Techniques", optionName = "Électricité", className = "1ère des Humanités", boysCount = 15, girlsCount = 8),
            SecondaryStudentStat(sectionName = "Humanités Techniques", optionName = "Électricité", className = "2ème des Humanités", boysCount = 12, girlsCount = 6),
            SecondaryStudentStat(sectionName = "Humanités Techniques", optionName = "Électricité", className = "3ème des Humanités", boysCount = 11, girlsCount = 5),
            SecondaryStudentStat(sectionName = "Humanités Techniques", optionName = "Électricité", className = "4ème des Humanités", boysCount = 10, girlsCount = 4),
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
                CertificationResult(examName = "TENASOSP", className = "8ème année (2ème secondaire)", registeredCount = 48, participantsCount = 46, successesCount = 38, boysSucceeded = 20, girlsSucceeded = 18),
                CertificationResult(examName = "EXETAT", className = "4ème des Humanités", registeredCount = 30, participantsCount = 28, successesCount = 20, boysSucceeded = 9, girlsSucceeded = 11),
            ),
        )
        extended.saveEnrollments(
            DEMO_SUBMISSION_2,
            listOf(
                EnrollmentStat(className = "7ème année (1ère secondaire)", boysCount = 30, girlsCount = 28, isBeginning = true),
                EnrollmentStat(className = "4ème des Humanités", boysCount = 26, girlsCount = 20, isBeginning = true),
                EnrollmentStat(className = "7ème année (1ère secondaire)", boysCount = 28, girlsCount = 26, isBeginning = false),
                EnrollmentStat(className = "4ème des Humanités", boysCount = 24, girlsCount = 19, isBeginning = false),
            ),
        )
    }

    private suspend fun seedMixedCensus(
        statistics: LocalStatisticsDataSource,
        extended: ExtendedStatisticsLocalDataSource,
    ) {
        val primary = listOf(
            PrimaryClassStat(className = "1ère année primaire", classOrder = 4, boysCount = 28, girlsCount = 30),
            PrimaryClassStat(className = "6ème année primaire", classOrder = 9, boysCount = 18, girlsCount = 20),
        ).map { it.copy(submissionId = DEMO_SUBMISSION_3) }
        statistics.savePrimaryStats(primary, "sch-3", SyncStatus.SYNCED)
        extended.saveSecondary(
            DEMO_SUBMISSION_3,
            listOf(
                SecondaryStudentStat(sectionName = "Tronc commun", optionName = null, className = "7ème année (1ère secondaire)", boysCount = 16, girlsCount = 15),
                SecondaryStudentStat(sectionName = "Humanités Générales", optionName = "Latin-Philosophie", className = "3ème des Humanités", boysCount = 12, girlsCount = 14),
                SecondaryStudentStat(sectionName = "Humanités Générales", optionName = "Latin-Philosophie", className = "4ème des Humanités", boysCount = 10, girlsCount = 11),
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
