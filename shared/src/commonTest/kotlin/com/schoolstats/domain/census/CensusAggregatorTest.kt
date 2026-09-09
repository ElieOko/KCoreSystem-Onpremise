package com.schoolstats.domain.census

import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AgeSexStat
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.WorkerStat
import com.schoolstats.domain.validation.StatValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CensusAggregatorTest {
    @Test
    fun `merge aggregates students teachers workers and admin by key`() {
        val a = CensusAggregator.from(
            primary = listOf(PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = 10, girlsCount = 8)),
            secondary = emptyList(),
            teachers = listOf(
                TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.PRIMAIRE, menCount = 2, womenCount = 3),
            ),
            enrollments = emptyList(),
            certifications = emptyList(),
            ageSex = listOf(AgeSexStat(className = "1ère année", age = 6, boysCount = 10, girlsCount = 8)),
            workers = listOf(WorkerStat(educationLevel = EducationLevel.D4, menCount = 1, womenCount = 1)),
            adminStaff = listOf(
                AdminStaffStat(function = AdminStaffFunction.DIRECTEUR, educationLevel = EducationLevel.LICENCE, menCount = 1, womenCount = 0),
            ),
        )
        val b = CensusAggregator.from(
            primary = listOf(PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = 5, girlsCount = 7)),
            secondary = emptyList(),
            teachers = listOf(
                TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.PRIMAIRE, menCount = 1, womenCount = 1),
            ),
            enrollments = emptyList(),
            certifications = emptyList(),
            ageSex = listOf(AgeSexStat(className = "1ère année", age = 6, boysCount = 5, girlsCount = 7)),
            workers = listOf(WorkerStat(educationLevel = EducationLevel.D4, menCount = 2, womenCount = 0)),
            adminStaff = listOf(
                AdminStaffStat(function = AdminStaffFunction.DIRECTEUR, educationLevel = EducationLevel.LICENCE, menCount = 0, womenCount = 1),
            ),
        )
        val merged = CensusAggregator.merge(listOf(a, b))
        assertEquals(15, merged.totalBoys)
        assertEquals(15, merged.totalGirls)
        assertEquals(30, merged.totalStudents)
        assertEquals(7, merged.totalTeachers)
        assertEquals(4, merged.totalWorkers)
        assertEquals(2, merged.totalAdminStaff)
        assertEquals(15, merged.byClass.single().boysCount)
        assertEquals(15, merged.byAge.single().girlsCount)
    }

    @Test
    fun `age sex validator rejects negatives`() {
        val errors = StatValidator.validateAgeSexStat(
            AgeSexStat(className = "1ère année", age = 7, boysCount = -1, girlsCount = 2),
        )
        assertTrue(errors.any { it.field == "counts" })
    }

    @Test
    fun `distribute by age preserves totals`() {
        val rows = CensusDefaults.distributeByAge("2ème année", 2, boys = 20, girls = 10)
        assertEquals(20, rows.sumOf { it.boysCount })
        assertEquals(10, rows.sumOf { it.girlsCount })
    }
}
