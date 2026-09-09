package com.schoolstats.domain.census

import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AgeSexStat
import com.schoolstats.domain.model.CentralizationStats
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.EnrollmentComparison
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.WorkerStat

object CensusAggregator {
    fun from(
        primary: List<PrimaryClassStat>,
        secondary: List<SecondaryStudentStat>,
        teachers: List<TeacherStatDetail>,
        enrollments: List<EnrollmentStat>,
        certifications: List<CertificationResult>,
        ageSex: List<AgeSexStat>,
        workers: List<WorkerStat>,
        adminStaff: List<AdminStaffStat>,
        contributingSchools: Int = 1,
    ): CentralizationStats {
        val boys = primary.sumOf { it.boysCount } + secondary.sumOf { it.boysCount }
        val girls = primary.sumOf { it.girlsCount } + secondary.sumOf { it.girlsCount }
        return CentralizationStats(
            totalBoys = boys,
            totalGirls = girls,
            totalStudents = boys + girls,
            totalTeachers = teachers.sumOf { it.totalCount },
            totalTeacherMen = teachers.sumOf { it.menCount },
            totalTeacherWomen = teachers.sumOf { it.womenCount },
            totalWorkers = workers.sumOf { it.totalCount },
            totalWorkerMen = workers.sumOf { it.menCount },
            totalWorkerWomen = workers.sumOf { it.womenCount },
            totalAdminStaff = adminStaff.sumOf { it.totalCount },
            totalAdminMen = adminStaff.sumOf { it.menCount },
            totalAdminWomen = adminStaff.sumOf { it.womenCount },
            contributingSchools = contributingSchools,
            byClass = primary.sortedBy { it.classOrder },
            bySection = secondary,
            teachers = teachers,
            enrollments = compareEnrollments(enrollments),
            certifications = certifications,
            byAge = ageSex.sortedWith(compareBy({ it.className }, { it.age })),
            workers = workers,
            adminStaff = adminStaff,
        )
    }

    fun merge(parts: List<CentralizationStats>): CentralizationStats {
        if (parts.isEmpty()) return CentralizationStats()
        if (parts.size == 1) return parts.first()
        return CentralizationStats(
            totalBoys = parts.sumOf { it.totalBoys },
            totalGirls = parts.sumOf { it.totalGirls },
            totalStudents = parts.sumOf { it.totalStudents },
            totalTeachers = parts.sumOf { it.totalTeachers },
            totalTeacherMen = parts.sumOf { it.totalTeacherMen },
            totalTeacherWomen = parts.sumOf { it.totalTeacherWomen },
            totalWorkers = parts.sumOf { it.totalWorkers },
            totalWorkerMen = parts.sumOf { it.totalWorkerMen },
            totalWorkerWomen = parts.sumOf { it.totalWorkerWomen },
            totalAdminStaff = parts.sumOf { it.totalAdminStaff },
            totalAdminMen = parts.sumOf { it.totalAdminMen },
            totalAdminWomen = parts.sumOf { it.totalAdminWomen },
            contributingSchools = parts.sumOf { it.contributingSchools },
            byClass = parts.flatMap { it.byClass }
                .groupBy { it.className }
                .map { (name, rows) ->
                    PrimaryClassStat(
                        className = name,
                        classOrder = rows.minOf { it.classOrder },
                        boysCount = rows.sumOf { it.boysCount },
                        girlsCount = rows.sumOf { it.girlsCount },
                    )
                }
                .sortedBy { it.classOrder },
            bySection = parts.flatMap { it.bySection }
                .groupBy { Triple(it.sectionName, it.optionName, it.className) }
                .map { (key, rows) ->
                    SecondaryStudentStat(
                        sectionName = key.first,
                        optionName = key.second,
                        className = key.third,
                        boysCount = rows.sumOf { it.boysCount },
                        girlsCount = rows.sumOf { it.girlsCount },
                    )
                },
            teachers = parts.flatMap { it.teachers }
                .groupBy { it.educationLevel to it.branch }
                .map { (key, rows) ->
                    TeacherStatDetail(
                        educationLevel = key.first,
                        branch = key.second,
                        menCount = rows.sumOf { it.menCount },
                        womenCount = rows.sumOf { it.womenCount },
                    )
                },
            enrollments = parts.flatMap { it.enrollments }
                .groupBy { it.className }
                .map { (name, rows) ->
                    EnrollmentComparison(
                        className = name,
                        beginningTotal = rows.sumOf { it.beginningTotal },
                        endTotal = rows.sumOf { it.endTotal },
                    )
                }
                .sortedBy { it.className },
            certifications = parts.flatMap { it.certifications }
                .groupBy { it.examName to it.className }
                .map { (key, rows) ->
                    CertificationResult(
                        examName = key.first,
                        className = key.second,
                        registeredCount = rows.sumOf { it.registeredCount },
                        participantsCount = rows.sumOf { it.participantsCount },
                        successesCount = rows.sumOf { it.successesCount },
                        boysSucceeded = rows.sumOf { it.boysSucceeded },
                        girlsSucceeded = rows.sumOf { it.girlsSucceeded },
                    )
                },
            byAge = parts.flatMap { it.byAge }
                .groupBy { it.className to it.age }
                .map { (key, rows) ->
                    AgeSexStat(
                        className = key.first,
                        age = key.second,
                        boysCount = rows.sumOf { it.boysCount },
                        girlsCount = rows.sumOf { it.girlsCount },
                    )
                }
                .sortedWith(compareBy({ it.className }, { it.age })),
            workers = parts.flatMap { it.workers }
                .groupBy { it.educationLevel }
                .map { (level, rows) ->
                    WorkerStat(
                        educationLevel = level,
                        menCount = rows.sumOf { it.menCount },
                        womenCount = rows.sumOf { it.womenCount },
                    )
                }
                .sortedBy { it.educationLevel.ordinal },
            adminStaff = parts.flatMap { it.adminStaff }
                .groupBy { it.function to it.educationLevel }
                .map { (key, rows) ->
                    AdminStaffStat(
                        function = key.first,
                        educationLevel = key.second,
                        menCount = rows.sumOf { it.menCount },
                        womenCount = rows.sumOf { it.womenCount },
                    )
                }
                .sortedBy { it.function.ordinal },
        )
    }

    fun compareEnrollments(stats: List<EnrollmentStat>): List<EnrollmentComparison> {
        val beginning = stats.filter { it.isBeginning }.associateBy { it.className }
        val end = stats.filter { !it.isBeginning }.associateBy { it.className }
        return (beginning.keys + end.keys).distinct().sorted().map { className ->
            EnrollmentComparison(
                className = className,
                beginningTotal = beginning[className]?.totalCount ?: 0,
                endTotal = end[className]?.totalCount ?: 0,
            )
        }
    }

    fun totalsByAge(stats: List<AgeSexStat>): List<AgeSexStat> =
        stats.groupBy { it.age }
            .map { (age, rows) ->
                AgeSexStat(
                    className = "Ensemble",
                    age = age,
                    boysCount = rows.sumOf { it.boysCount },
                    girlsCount = rows.sumOf { it.girlsCount },
                )
            }
            .sortedBy { it.age }

    fun teachersByEducation(stats: List<TeacherStatDetail>): List<WorkerStat> =
        stats.groupBy { it.educationLevel }
            .map { (level, rows) ->
                WorkerStat(
                    educationLevel = level,
                    menCount = rows.sumOf { it.menCount },
                    womenCount = rows.sumOf { it.womenCount },
                )
            }
            .sortedBy { it.educationLevel.ordinal }
}
