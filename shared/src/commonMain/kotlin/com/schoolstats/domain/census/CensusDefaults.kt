package com.schoolstats.domain.census

import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AgeSexStat
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.StudentAges
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.WorkerStat

object CensusDefaults {
    val primaryClasses: List<String> = listOf(
        "1ère année", "2ème année", "3ème année", "4ème année", "5ème année", "6ème année",
    )

    val secondaryRows: List<Triple<String, String?, String>> = listOf(
        Triple("Enseignement Général", "Latin-Philo", "1ère"),
        Triple("Enseignement Général", "Latin-Philo", "2ème"),
        Triple("Enseignement Général", "Latin-Philo", "3ème"),
        Triple("Enseignement Général", "Latin-Philo", "4ème"),
        Triple("Enseignement Général", "Latin-Philo", "5ème"),
        Triple("Enseignement Général", "Latin-Philo", "6ème"),
        Triple("Enseignement Technique", "Électricité", "3ème"),
        Triple("Enseignement Technique", "Électricité", "4ème"),
    )

    fun emptyPrimary(): List<PrimaryClassStat> =
        primaryClasses.mapIndexed { index, name ->
            PrimaryClassStat(className = name, classOrder = index + 1, boysCount = 0, girlsCount = 0)
        }

    fun emptySecondary(): List<SecondaryStudentStat> =
        secondaryRows.map { (section, option, clazz) ->
            SecondaryStudentStat(sectionName = section, optionName = option, className = clazz, boysCount = 0, girlsCount = 0)
        }

    fun emptyTeachers(): List<TeacherStatDetail> =
        EducationLevel.entries.flatMap { level ->
            listOf(TeacherBranch.PRIMAIRE, TeacherBranch.SECONDAIRE_GENERAL).map { branch ->
                TeacherStatDetail(educationLevel = level, branch = branch, menCount = 0, womenCount = 0)
            }
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

    fun emptyAgeSex(classNames: List<String> = primaryClasses): List<AgeSexStat> =
        classNames.flatMap { className ->
            StudentAges.values.map { age ->
                AgeSexStat(className = className, age = age, boysCount = 0, girlsCount = 0)
            }
        }

    fun emptyEnrollments(): List<EnrollmentStat> =
        primaryClasses.flatMap { className ->
            listOf(
                EnrollmentStat(className = className, boysCount = 0, girlsCount = 0, isBeginning = true),
                EnrollmentStat(className = className, boysCount = 0, girlsCount = 0, isBeginning = false),
            )
        }

    fun distributeByAge(className: String, classOrder: Int, boys: Int, girls: Int): List<AgeSexStat> {
        val typical = (5 + classOrder).coerceIn(6, 18)
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
                age = (typical + offset).coerceIn(6, StudentAges.PLUS_AGE),
                boysCount = boyParts[index],
                girlsCount = girlParts[index],
            )
        }
    }
}
