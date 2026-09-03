package com.schoolstats.domain.validation

import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.PrimaryClassStat
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StatValidatorTest {
    @Test
    fun `validate primary stat rejects negative counts`() {
        val errors = StatValidator.validatePrimaryClassStat(
            PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = -1, girlsCount = 2),
        )
        assertTrue(errors.any { it.field == "boys" })
    }

    @Test
    fun `retention rate handles zero beginning`() {
        assertNull(StatValidator.retentionRate(beginning = 0, end = 10))
    }

    @Test
    fun `retention rate calculates correctly`() {
        assertEquals(80.0, StatValidator.retentionRate(beginning = 100, end = 80))
    }

    @Test
    fun `certification rejects participants above registered`() {
        val errors = StatValidator.validateCertification(
            CertificationResult(
                examName = "TENAFEP",
                className = "6ème",
                registeredCount = 10,
                participantsCount = 12,
                successesCount = 8,
                boysSucceeded = 4,
                girlsSucceeded = 4,
            ),
        )
        assertTrue(errors.isNotEmpty())
    }
}
