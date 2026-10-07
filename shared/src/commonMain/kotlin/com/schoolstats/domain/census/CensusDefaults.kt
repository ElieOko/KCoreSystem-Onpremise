package com.schoolstats.domain.census

import com.schoolstats.domain.education.EducationCycle
import com.schoolstats.domain.education.OfficialClass
import com.schoolstats.domain.education.RdcEducationSystem
import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AgeSexStat
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.StudentAges
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.WorkerStat

object CensusDefaults {
    val primaryClasses: List<String> = RdcEducationSystem.preschoolAndPrimaryClassNames

    val secondaryRows: List<Triple<String, String?, String>> =
        RdcEducationSystem.defaultSecondaryRows().map { Triple(it.sectionName, it.optionName, it.className) }

    fun emptyPrimary(): List<PrimaryClassStat> =
        RdcEducationSystem.preschoolAndPrimaryClasses.map { clazz ->
            PrimaryClassStat(className = clazz.name, classOrder = clazz.order, boysCount = 0, girlsCount = 0)
        }

    fun emptySecondary(): List<SecondaryStudentStat> =
        RdcEducationSystem.defaultSecondaryRows().map { row ->
            SecondaryStudentStat(
                sectionName = row.sectionName,
                optionName = row.optionName,
                className = row.className,
                boysCount = 0,
                girlsCount = 0,
            )
        }

    fun emptyTeachers(): List<TeacherStatDetail> =
        EducationLevel.entries.flatMap { level ->
            listOf(
                TeacherBranch.MATERNELLE,
                TeacherBranch.PRIMAIRE,
                TeacherBranch.SECONDAIRE_GENERAL,
                TeacherBranch.SECONDAIRE_TECHNIQUE,
            ).map { branch ->
                TeacherStatDetail(educationLevel = level, branch = branch, menCount = 0, womenCount = 0)
            }
        }

    fun emptyCertifications(): List<CertificationResult> =
        RdcEducationSystem.exams.map { exam ->
            CertificationResult(
                examName = exam.name,
                className = exam.className,
                registeredCount = 0,
                participantsCount = 0,
                successesCount = 0,
                boysSucceeded = 0,
                girlsSucceeded = 0,
            )
        }

    fun officialClassesFor(cycle: EducationCycle): List<OfficialClass> = when (cycle) {
        EducationCycle.MATERNELLE -> RdcEducationSystem.preschoolClasses
        EducationCycle.PRIMAIRE -> RdcEducationSystem.primaryClasses
        EducationCycle.TRONC_COMMUN -> RdcEducationSystem.troncCommunClasses
        EducationCycle.HUMANITES -> RdcEducationSystem.humanitesClasses
    }

    fun emptyWorkers(): List<WorkerStat> =
        EducationLevel.entries.map { WorkerStat(educationLevel = it, menCount = 0, womenCount = 0) }

    fun emptyAdminStaff(): List<AdminStaffStat> =
        AdminStaffFunction.entries.map { function ->
            val level = when (function) {
                AdminStaffFunction.DIRECTEUR, AdminStaffFunction.DIRECTEUR_DES_ETUDES -> EducationLevel.LICENCE
                AdminStaffFunction.DIRECTEUR_ADJOINT, AdminStaffFunction.PREFET -> EducationLevel.GRADUE
                AdminStaffFunction.SECRETAIRE -> EducationLevel.D6
                else -> EducationLevel.GRADUE
            }
            AdminStaffStat(function = function, educationLevel = level, menCount = 0, womenCount = 0)
        }

    fun emptyAgeSex(classNames: List<String> = RdcEducationSystem.allOfficialClassNames): List<AgeSexStat> =
        classNames.flatMap { className ->
            StudentAges.values.map { age ->
                AgeSexStat(className = className, age = age, boysCount = 0, girlsCount = 0)
            }
        }

    fun emptyEnrollments(): List<EnrollmentStat> =
        RdcEducationSystem.allOfficialClassNames.flatMap { className ->
            listOf(
                EnrollmentStat(className = className, boysCount = 0, girlsCount = 0, isBeginning = true),
                EnrollmentStat(className = className, boysCount = 0, girlsCount = 0, isBeginning = false),
            )
        }

    fun distributeByAge(className: String, classOrder: Int, boys: Int, girls: Int): List<AgeSexStat> {
        val typical = RdcEducationSystem.typicalAge(className, classOrder).coerceIn(3, 18)
        val offsets = listOf(-1, 0, 1, 2)
        val weights = listOf(0.15, 0.50, 0.25, 0.10)
        fun split(total: Int): List<Int> {
            if (total <= 0) return List(4) { 0 }
            val parts = weights.map { (total * it).toInt() }.toMutableList()
            var remainder = total - parts.sum()
            var i = 1
            while (remainder > 0) {
                parts[i % parts.size] += 1
                remainder--
                i++
            }
            return parts
        }
        val boyParts = split(boys)
        val girlParts = split(girls)
        return offsets.mapIndexed { index, offset ->
            AgeSexStat(
                className = className,
                age = (typical + offset).coerceIn(3, StudentAges.PLUS_AGE),
                boysCount = boyParts[index],
                girlsCount = girlParts[index],
            )
        }
    }
}
