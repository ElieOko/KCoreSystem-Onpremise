package com.schoolstats.domain.validation

import com.schoolstats.domain.model.PrimaryClassStat

data class ValidationError(
    val field: String,
    val message: String,
)

object StatValidator {
    fun validatePrimaryClassStat(stat: PrimaryClassStat): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        if (stat.boysCount < 0) errors += ValidationError("boys", "Le nombre de garçons ne peut pas être négatif.")
        if (stat.girlsCount < 0) errors += ValidationError("girls", "Le nombre de filles ne peut pas être négatif.")
        if (stat.className.isBlank()) errors += ValidationError("className", "Le nom de la classe est obligatoire.")
        return errors
    }

    fun validatePrimaryStats(stats: List<PrimaryClassStat>): List<ValidationError> {
        return stats.flatMap { validatePrimaryClassStat(it) }
    }

    fun retentionRate(beginning: Int, end: Int): Double? {
        if (beginning <= 0) return null
        return (end.toDouble() / beginning.toDouble()) * 100.0
    }

    fun dropoutRate(beginning: Int, end: Int): Double? {
        val retention = retentionRate(beginning, end) ?: return null
        return 100.0 - retention
    }
}
