package com.schoolstats.domain.census

import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.Gender
import com.schoolstats.domain.model.StudentRecord
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.WorkerCategory
import com.schoolstats.domain.model.WorkerRecord
import com.schoolstats.domain.validation.StatValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegistryAggregatorTest {
    @Test
    fun `students are counted by class and gender`() {
        val students = listOf(
            StudentRecord(schoolId = "sch-1", fullName = "Jean", gender = Gender.MALE, className = "1ère année", age = 6),
            StudentRecord(schoolId = "sch-1", fullName = "Marie", gender = Gender.FEMALE, className = "1ère année", age = 7),
            StudentRecord(schoolId = "sch-1", fullName = "Paul", gender = Gender.MALE, className = "2ème année", age = 8),
        )
        val primary = RegistryAggregator.primaryFromStudents(students)
        val first = primary.first { it.className == "1ère année" }
        val second = primary.first { it.className == "2ème année" }
        assertEquals(1, first.boysCount)
        assertEquals(1, first.girlsCount)
        assertEquals(1, second.boysCount)
        assertEquals(0, second.girlsCount)

        val summary = RegistryAggregator.summary("sch-1", "École Test", students, emptyList())
        assertEquals(3, summary.studentCount)
        assertEquals(2, summary.studentBoys)
        assertEquals(1, summary.studentGirls)
        assertEquals(0, summary.workerCount)
    }

    @Test
    fun `workers are split by category and feed census rows`() {
        val workers = listOf(
            WorkerRecord(
                schoolId = "sch-1",
                fullName = "Sarah",
                gender = Gender.FEMALE,
                category = WorkerCategory.ENSEIGNANT,
                educationLevel = EducationLevel.GRADUE,
                teacherBranch = TeacherBranch.PRIMAIRE,
            ),
            WorkerRecord(
                schoolId = "sch-1",
                fullName = "Joseph",
                gender = Gender.MALE,
                category = WorkerCategory.ADMINISTRATIF,
                educationLevel = EducationLevel.LICENCE,
                adminFunction = AdminStaffFunction.DIRECTEUR,
            ),
            WorkerRecord(
                schoolId = "sch-1",
                fullName = "Pierre",
                gender = Gender.MALE,
                category = WorkerCategory.OUVRIER,
                educationLevel = EducationLevel.D4,
            ),
            WorkerRecord(
                schoolId = "sch-1",
                fullName = "Jeanne",
                gender = Gender.FEMALE,
                category = WorkerCategory.OUVRIER,
                educationLevel = EducationLevel.D4,
            ),
        )
        val summary = RegistryAggregator.summary("sch-1", "École Test", emptyList(), workers)
        assertEquals(4, summary.workerCount)
        assertEquals(1, summary.teacherCount)
        assertEquals(1, summary.adminCount)
        assertEquals(2, summary.ouvrierCount)

        val teachers = RegistryAggregator.teachersFromWorkers(workers)
        val teacherRow = teachers.first { it.educationLevel == EducationLevel.GRADUE && it.branch == TeacherBranch.PRIMAIRE }
        assertEquals(0, teacherRow.menCount)
        assertEquals(1, teacherRow.womenCount)

        val ouvriers = RegistryAggregator.ouvriersFromWorkers(workers)
        val d4 = ouvriers.first { it.educationLevel == EducationLevel.D4 }
        assertEquals(1, d4.menCount)
        assertEquals(1, d4.womenCount)
    }

    @Test
    fun `student and worker records require a name`() {
        val studentErrors = StatValidator.validateStudentRecord(
            StudentRecord(schoolId = "sch-1", fullName = "", gender = Gender.MALE, className = "1ère année", age = 6),
        )
        assertTrue(studentErrors.any { it.field == "fullName" })
        val workerErrors = StatValidator.validateWorkerRecord(
            WorkerRecord(schoolId = "sch-1", fullName = "", gender = Gender.MALE, category = WorkerCategory.OUVRIER),
        )
        assertTrue(workerErrors.any { it.field == "fullName" })
    }

    @Test
    fun `school demo login credentials are recognized`() {
        assertTrue(com.schoolstats.data.demo.DemoDataSeeder.isLocalDemoLogin("ecole@local", "demo"))
        assertTrue(com.schoolstats.data.demo.DemoDataSeeder.isSchoolDemo("ECOLE@LOCAL"))
        assertEquals(false, com.schoolstats.data.demo.DemoDataSeeder.isSchoolDemo("demo@local"))
    }
}
