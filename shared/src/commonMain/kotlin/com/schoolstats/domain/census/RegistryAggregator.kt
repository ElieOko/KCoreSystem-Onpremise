package com.schoolstats.domain.census

import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AgeSexStat
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.Gender
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SchoolRegistrySummary
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.StudentAges
import com.schoolstats.domain.model.StudentRecord
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.WorkerCategory
import com.schoolstats.domain.model.WorkerRecord
import com.schoolstats.domain.model.WorkerStat

object RegistryAggregator {
    fun summary(
        schoolId: String,
        schoolName: String,
        students: List<StudentRecord>,
        workers: List<WorkerRecord>,
    ): SchoolRegistrySummary {
        val teachers = workers.filter { it.category == WorkerCategory.ENSEIGNANT }
        val admin = workers.filter { it.category == WorkerCategory.ADMINISTRATIF }
        val ouvriers = workers.filter { it.category == WorkerCategory.OUVRIER }
        return SchoolRegistrySummary(
            schoolId = schoolId,
            schoolName = schoolName,
            studentCount = students.size,
            studentBoys = students.count { it.gender == Gender.MALE },
            studentGirls = students.count { it.gender == Gender.FEMALE },
            workerCount = workers.size,
            workerMen = workers.count { it.gender == Gender.MALE },
            workerWomen = workers.count { it.gender == Gender.FEMALE },
            teacherCount = teachers.size,
            adminCount = admin.size,
            ouvrierCount = ouvriers.size,
        )
    }

    fun primaryFromStudents(students: List<StudentRecord>): List<PrimaryClassStat> {
        val primary = students.filter { !it.isSecondary }
        val order = CensusDefaults.primaryClasses.withIndex().associate { it.value to it.index + 1 }
        val classNames = (CensusDefaults.primaryClasses + primary.map { it.className }).distinct()
        return classNames.map { className ->
            val inClass = primary.filter { it.className == className }
            PrimaryClassStat(
                className = className,
                classOrder = order[className] ?: 99,
                boysCount = inClass.count { it.gender == Gender.MALE },
                girlsCount = inClass.count { it.gender == Gender.FEMALE },
            )
        }
    }

    fun secondaryFromStudents(students: List<StudentRecord>): List<SecondaryStudentStat> {
        val secondary = students.filter { it.isSecondary }
        if (secondary.isEmpty()) return CensusDefaults.emptySecondary()
        val defaults = CensusDefaults.emptySecondary()
        val grouped = secondary.groupBy { Triple(it.sectionName.orEmpty(), it.optionName, it.className) }
        val fromRegistry = grouped.map { (key, rows) ->
            SecondaryStudentStat(
                sectionName = key.first,
                optionName = key.second,
                className = key.third,
                boysCount = rows.count { it.gender == Gender.MALE },
                girlsCount = rows.count { it.gender == Gender.FEMALE },
            )
        }
        val merged = defaults.map { row ->
            fromRegistry.firstOrNull {
                it.sectionName == row.sectionName && it.optionName == row.optionName && it.className == row.className
            } ?: row
        }
        val extras = fromRegistry.filter { saved ->
            defaults.none {
                it.sectionName == saved.sectionName && it.optionName == saved.optionName && it.className == saved.className
            }
        }
        return merged + extras
    }

    fun ageSexFromStudents(students: List<StudentRecord>): List<AgeSexStat> {
        val classNames = (CensusDefaults.primaryClasses + students.map { it.className }).distinct()
        return classNames.flatMap { className ->
            StudentAges.values.map { age ->
                val inCell = students.filter { it.className == className && normalizeAge(it.age) == age }
                AgeSexStat(
                    className = className,
                    age = age,
                    boysCount = inCell.count { it.gender == Gender.MALE },
                    girlsCount = inCell.count { it.gender == Gender.FEMALE },
                )
            }
        }
    }

    fun teachersFromWorkers(workers: List<WorkerRecord>): List<TeacherStatDetail> {
        val teachers = workers.filter { it.category == WorkerCategory.ENSEIGNANT }
        val defaults = CensusDefaults.emptyTeachers()
        val grouped = teachers.groupBy {
            (it.educationLevel) to (it.teacherBranch ?: TeacherBranch.PRIMAIRE)
        }
        val fromRegistry = grouped.map { (key, rows) ->
            TeacherStatDetail(
                educationLevel = key.first,
                branch = key.second,
                menCount = rows.count { it.gender == Gender.MALE },
                womenCount = rows.count { it.gender == Gender.FEMALE },
            )
        }
        val merged = defaults.map { row ->
            fromRegistry.firstOrNull { it.educationLevel == row.educationLevel && it.branch == row.branch } ?: row
        }
        val extras = fromRegistry.filter { saved ->
            defaults.none { it.educationLevel == saved.educationLevel && it.branch == saved.branch }
        }
        return merged + extras
    }

    fun adminFromWorkers(workers: List<WorkerRecord>): List<AdminStaffStat> {
        val admin = workers.filter { it.category == WorkerCategory.ADMINISTRATIF }
        val defaults = CensusDefaults.emptyAdminStaff()
        val byFunction = admin.groupBy { it.adminFunction ?: AdminStaffFunction.SURNUMERAIRE }
        return defaults.map { row ->
            val rows = byFunction[row.function].orEmpty()
            if (rows.isEmpty()) {
                row
            } else {
                row.copy(
                    educationLevel = rows.first().educationLevel,
                    menCount = rows.count { it.gender == Gender.MALE },
                    womenCount = rows.count { it.gender == Gender.FEMALE },
                )
            }
        }
    }

    fun ouvriersFromWorkers(workers: List<WorkerRecord>): List<WorkerStat> {
        val ouvriers = workers.filter { it.category == WorkerCategory.OUVRIER }
        val defaults = CensusDefaults.emptyWorkers()
        val byLevel = ouvriers.groupBy { it.educationLevel }
        return defaults.map { row ->
            val rows = byLevel[row.educationLevel].orEmpty()
            row.copy(
                menCount = rows.count { it.gender == Gender.MALE },
                womenCount = rows.count { it.gender == Gender.FEMALE },
            )
        }
    }

    fun enrollmentsFromStudents(students: List<StudentRecord>): List<com.schoolstats.domain.model.EnrollmentStat> {
        val primary = primaryFromStudents(students)
        return primary.flatMap { row ->
            listOf(
                com.schoolstats.domain.model.EnrollmentStat(
                    className = row.className,
                    boysCount = row.boysCount,
                    girlsCount = row.girlsCount,
                    isBeginning = true,
                ),
                com.schoolstats.domain.model.EnrollmentStat(
                    className = row.className,
                    boysCount = row.boysCount,
                    girlsCount = row.girlsCount,
                    isBeginning = false,
                ),
            )
        }
    }

    private fun normalizeAge(age: Int): Int = when {
        age >= StudentAges.PLUS_AGE -> StudentAges.PLUS_AGE
        age < 6 -> 6
        else -> age
    }
}
