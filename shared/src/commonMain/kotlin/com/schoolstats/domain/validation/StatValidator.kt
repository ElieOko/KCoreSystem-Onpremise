package com.schoolstats.domain.validation

import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AgeSexStat
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.WorkerStat

data class ValidationError(val field: String, val message: String)

object StatValidator {
    fun validatePrimaryClassStat(stat: PrimaryClassStat): List<ValidationError> = buildList {
        if (stat.boysCount < 0) add(ValidationError("boys", "Garçons négatif interdit (${stat.className})."))
        if (stat.girlsCount < 0) add(ValidationError("girls", "Filles négatif interdit (${stat.className})."))
        if (stat.className.isBlank()) add(ValidationError("className", "Nom de classe obligatoire."))
    }

    fun validatePrimaryStats(stats: List<PrimaryClassStat>) = stats.flatMap(::validatePrimaryClassStat)

    fun validateSecondaryStat(stat: SecondaryStudentStat): List<ValidationError> = buildList {
        if (stat.boysCount < 0 || stat.girlsCount < 0) add(ValidationError("counts", "Effectifs négatifs interdits."))
        if (stat.sectionName.isBlank()) add(ValidationError("section", "Section obligatoire."))
    }

    fun validateTeacherStat(stat: TeacherStatDetail): List<ValidationError> = buildList {
        if (stat.menCount < 0 || stat.womenCount < 0) add(ValidationError("counts", "Effectifs enseignants négatifs interdits."))
    }

    fun validateAgeSexStat(stat: AgeSexStat): List<ValidationError> = buildList {
        if (stat.className.isBlank()) add(ValidationError("className", "Classe obligatoire pour l'âge et le sexe."))
        if (stat.age < 5) add(ValidationError("age", "Âge invalide (${stat.className})."))
        if (stat.boysCount < 0 || stat.girlsCount < 0) {
            add(ValidationError("counts", "Effectifs par âge négatifs interdits (${stat.className})."))
        }
    }

    fun validateWorkerStat(stat: WorkerStat): List<ValidationError> = buildList {
        if (stat.menCount < 0 || stat.womenCount < 0) {
            add(ValidationError("counts", "Effectifs ouvriers négatifs interdits."))
        }
    }

    fun validateAdminStaffStat(stat: AdminStaffStat): List<ValidationError> = buildList {
        if (stat.menCount < 0 || stat.womenCount < 0) {
            add(ValidationError("counts", "Effectifs administratifs négatifs interdits (${stat.function.name})."))
        }
    }

    fun validateEnrollment(stat: EnrollmentStat): List<ValidationError> = buildList {
        if (stat.boysCount < 0 || stat.girlsCount < 0) add(ValidationError("counts", "Inscriptions négatives interdites."))
    }

    fun validateCertification(result: CertificationResult): List<ValidationError> = buildList {
        if (result.participantsCount > result.registeredCount) {
            add(ValidationError("participants", "Participants > inscrits (${result.className})."))
        }
        if (result.successesCount > result.participantsCount) {
            add(ValidationError("successes", "Réussites > participants (${result.className})."))
        }
        if (result.boysSucceeded + result.girlsSucceeded > result.successesCount) {
            add(ValidationError("gender", "Répartition sexe incohérente (${result.className})."))
        }
    }

    fun retentionRate(beginning: Int, end: Int): Double? =
        if (beginning <= 0) null else (end.toDouble() / beginning) * 100

    fun dropoutRate(beginning: Int, end: Int): Double? =
        retentionRate(beginning, end)?.let { 100 - it }
}
