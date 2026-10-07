package com.schoolstats.domain.education

import com.schoolstats.domain.census.CensusDefaults
import com.schoolstats.domain.model.SchoolType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RdcEducationSystemTest {
    @Test
    fun `official catalog has three preschool six primary and six secondary classes`() {
        assertEquals(3, RdcEducationSystem.preschoolClasses.size)
        assertEquals(6, RdcEducationSystem.primaryClasses.size)
        assertEquals(2, RdcEducationSystem.troncCommunClasses.size)
        assertEquals(4, RdcEducationSystem.humanitesClasses.size)
        assertEquals(15, RdcEducationSystem.allOfficialClasses.size)
    }

    @Test
    fun `class names are official and ordered`() {
        assertEquals(
            listOf("1ère maternelle", "2ème maternelle", "3ème maternelle"),
            RdcEducationSystem.preschoolClassNames,
        )
        assertEquals("1ère année primaire", RdcEducationSystem.primaryClassNames.first())
        assertEquals("6ème année primaire", RdcEducationSystem.primaryClassNames.last())
        assertEquals("7ème année (1ère secondaire)", RdcEducationSystem.troncCommunClasses.first().name)
        assertEquals("4ème des Humanités", RdcEducationSystem.humanitesClasses.last().name)
    }

    @Test
    fun `official exams map to terminal classes`() {
        val byCode = RdcEducationSystem.exams.associateBy { it.code }
        assertEquals("6ème année primaire", byCode.getValue("TENAFEP").className)
        assertEquals("8ème année (2ème secondaire)", byCode.getValue("TENASOSP").className)
        assertEquals("4ème des Humanités", byCode.getValue("EXETAT").className)
        assertEquals("TENAFEP", RdcEducationSystem.classByName("6ème année primaire")?.certificationExamCode)
        assertEquals("EXETAT", RdcEducationSystem.classByName("4ème des Humanités")?.certificationExamCode)
    }

    @Test
    fun `school types expose official cycles`() {
        assertEquals(setOf(EducationCycle.MATERNELLE), RdcEducationSystem.schoolTypesOffered(SchoolType.MATERNELLE))
        assertTrue(EducationCycle.HUMANITES in RdcEducationSystem.schoolTypesOffered(SchoolType.SECONDAIRE))
        assertEquals(EducationCycle.entries.toSet(), RdcEducationSystem.schoolTypesOffered(SchoolType.COMPLET))
    }

    @Test
    fun `default forms use official class names`() {
        val primary = CensusDefaults.emptyPrimary()
        assertTrue(primary.any { it.className == "1ère maternelle" })
        assertTrue(primary.any { it.className == "6ème année primaire" })
        assertTrue(CensusDefaults.emptySecondary().any { it.className.startsWith("7ème") })
        assertTrue(CensusDefaults.emptySecondary().any { it.optionName == "Latin-Philosophie" })
        assertEquals(3, CensusDefaults.emptyCertifications().size)
        assertNotNull(CensusDefaults.emptyCertifications().single { it.examName == "EXETAT" })
    }
}
