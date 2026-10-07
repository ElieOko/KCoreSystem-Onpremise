package com.schoolstats.domain.education

import com.schoolstats.domain.model.SchoolType

/**
 * Référentiel officiel du système éducatif de la RDC
 * (Loi-cadre n° 14/004 du 11 février 2014 — EPST).
 *
 * Cycles :
 * - Préscolaire / maternelle : 3 ans
 * - Primaire : 6 ans (éducation de base)
 * - Premier cycle du secondaire (tronc commun) : 2 ans (7ème, 8ème)
 * - Deuxième cycle du secondaire (humanités) : 4 ans
 */
enum class EducationCycle {
    MATERNELLE,
    PRIMAIRE,
    TRONC_COMMUN,
    HUMANITES,
}

fun EducationCycle.labelFr(): String = when (this) {
    EducationCycle.MATERNELLE -> "Maternelle"
    EducationCycle.PRIMAIRE -> "Primaire"
    EducationCycle.TRONC_COMMUN -> "Premier cycle — Tronc commun"
    EducationCycle.HUMANITES -> "Deuxième cycle — Humanités"
}

fun EducationCycle.descriptionFr(): String = when (this) {
    EducationCycle.MATERNELLE -> "Éducation préscolaire, 3 années officielles"
    EducationCycle.PRIMAIRE -> "Enseignement primaire, 6 années officielles (TENAFEP en 6ème)"
    EducationCycle.TRONC_COMMUN -> "7ème et 8ème années (1ère et 2ème secondaire)"
    EducationCycle.HUMANITES -> "1ère à 4ème des Humanités (EXETAT en 4ème)"
}

data class OfficialClass(
    val name: String,
    val shortName: String,
    val cycle: EducationCycle,
    val order: Int,
    val typicalAge: Int,
    val certificationExamCode: String? = null,
)

data class OfficialSection(
    val name: String,
    val code: String,
    val order: Int,
    val cycle: EducationCycle,
)

data class OfficialOption(
    val sectionCode: String,
    val name: String,
    val code: String,
    val order: Int,
)

data class OfficialExam(
    val name: String,
    val code: String,
    val className: String,
    val description: String,
)

data class OfficialSecondaryRow(
    val sectionName: String,
    val optionName: String?,
    val className: String,
)

object RdcEducationSystem {
    const val COUNTRY = "République Démocratique du Congo"
    const val AUTHORITY = "EPST — Enseignement Primaire, Secondaire et Technique"
    const val LEGAL_FRAME = "Loi-cadre n° 14/004 du 11 février 2014 de l'enseignement national"

    val preschoolClasses: List<OfficialClass> = listOf(
        OfficialClass("1ère maternelle", "1ère Mat.", EducationCycle.MATERNELLE, 1, 3),
        OfficialClass("2ème maternelle", "2ème Mat.", EducationCycle.MATERNELLE, 2, 4),
        OfficialClass("3ème maternelle", "3ème Mat.", EducationCycle.MATERNELLE, 3, 5),
    )

    val primaryClasses: List<OfficialClass> = listOf(
        OfficialClass("1ère année primaire", "1ère P", EducationCycle.PRIMAIRE, 4, 6),
        OfficialClass("2ème année primaire", "2ème P", EducationCycle.PRIMAIRE, 5, 7),
        OfficialClass("3ème année primaire", "3ème P", EducationCycle.PRIMAIRE, 6, 8),
        OfficialClass("4ème année primaire", "4ème P", EducationCycle.PRIMAIRE, 7, 9),
        OfficialClass("5ème année primaire", "5ème P", EducationCycle.PRIMAIRE, 8, 10),
        OfficialClass("6ème année primaire", "6ème P", EducationCycle.PRIMAIRE, 9, 11, "TENAFEP"),
    )

    val troncCommunClasses: List<OfficialClass> = listOf(
        OfficialClass("7ème année (1ère secondaire)", "7ème", EducationCycle.TRONC_COMMUN, 10, 12),
        OfficialClass("8ème année (2ème secondaire)", "8ème", EducationCycle.TRONC_COMMUN, 11, 13, "TENASOSP"),
    )

    val humanitesClasses: List<OfficialClass> = listOf(
        OfficialClass("1ère des Humanités", "1ère H", EducationCycle.HUMANITES, 12, 14),
        OfficialClass("2ème des Humanités", "2ème H", EducationCycle.HUMANITES, 13, 15),
        OfficialClass("3ème des Humanités", "3ème H", EducationCycle.HUMANITES, 14, 16),
        OfficialClass("4ème des Humanités", "4ème H", EducationCycle.HUMANITES, 15, 17, "EXETAT"),
    )

    val preschoolAndPrimaryClasses: List<OfficialClass> = preschoolClasses + primaryClasses
    val secondaryClasses: List<OfficialClass> = troncCommunClasses + humanitesClasses
    val allOfficialClasses: List<OfficialClass> = preschoolAndPrimaryClasses + secondaryClasses

    val sections: List<OfficialSection> = listOf(
        OfficialSection("Tronc commun", "TC", 1, EducationCycle.TRONC_COMMUN),
        OfficialSection("Humanités Générales", "HG", 2, EducationCycle.HUMANITES),
        OfficialSection("Humanités Pédagogiques", "HP", 3, EducationCycle.HUMANITES),
        OfficialSection("Humanités Techniques", "HT", 4, EducationCycle.HUMANITES),
        OfficialSection("Humanités Professionnelles", "HPR", 5, EducationCycle.HUMANITES),
    )

    val options: List<OfficialOption> = listOf(
        OfficialOption("HG", "Latin-Philosophie", "LP", 1),
        OfficialOption("HG", "Mathématiques-Physique", "MP", 2),
        OfficialOption("HG", "Chimie-Biologie", "CB", 3),
        OfficialOption("HG", "Latin-Mathématiques", "LM", 4),
        OfficialOption("HP", "Pédagogie Générale", "PG", 1),
        OfficialOption("HP", "Pédagogie Maternelle", "PM", 2),
        OfficialOption("HT", "Commercial et Gestion", "CG", 1),
        OfficialOption("HT", "Sociale", "SOC", 2),
        OfficialOption("HT", "Électricité", "EL", 3),
        OfficialOption("HT", "Mécanique Générale", "MG", 4),
        OfficialOption("HT", "Construction", "CONS", 5),
        OfficialOption("HT", "Coupe et Couture", "CC", 6),
        OfficialOption("HT", "Agriculture", "AG", 7),
        OfficialOption("HT", "Hôtellerie et Restauration", "HR", 8),
        OfficialOption("HPR", "Menuiserie", "MEN", 1),
        OfficialOption("HPR", "Maçonnerie", "MAC", 2),
        OfficialOption("HPR", "Coupe et Couture professionnelle", "CCP", 3),
    )

    val defaultHumanitesOptions: List<OfficialOption> = options.filter { option ->
        option.code in setOf("LP", "MP", "CB", "PG", "CG", "EL")
    }

    val exams: List<OfficialExam> = listOf(
        OfficialExam(
            name = "TENAFEP",
            code = "TENAFEP",
            className = "6ème année primaire",
            description = "Test National de Fin d'Études Primaires",
        ),
        OfficialExam(
            name = "TENASOSP",
            code = "TENASOSP",
            className = "8ème année (2ème secondaire)",
            description = "Test National de fin du premier cycle du secondaire",
        ),
        OfficialExam(
            name = "EXETAT",
            code = "EXETAT",
            className = "4ème des Humanités",
            description = "Examen d'État (fin des humanités)",
        ),
    )

    private val classesByName: Map<String, OfficialClass> = allOfficialClasses.associateBy { it.name }

    val preschoolClassNames: List<String> = preschoolClasses.map { it.name }
    val primaryClassNames: List<String> = primaryClasses.map { it.name }
    val preschoolAndPrimaryClassNames: List<String> = preschoolAndPrimaryClasses.map { it.name }
    val secondaryClassNames: List<String> = secondaryClasses.map { it.name }
    val allOfficialClassNames: List<String> = allOfficialClasses.map { it.name }

    fun classByName(name: String): OfficialClass? = classesByName[name]

    fun shortName(className: String): String = classesByName[className]?.shortName ?: className

    fun typicalAge(className: String, fallbackOrder: Int): Int =
        classesByName[className]?.typicalAge ?: (5 + fallbackOrder).coerceIn(3, 18)

    fun cycleOf(className: String): EducationCycle? = classesByName[className]?.cycle

    fun defaultSecondaryRows(): List<OfficialSecondaryRow> {
        val tronc = troncCommunClasses.map { clazz ->
            OfficialSecondaryRow(sectionName = "Tronc commun", optionName = null, className = clazz.name)
        }
        val humanites = defaultHumanitesOptions.flatMap { option ->
            val section = sections.first { it.code == option.sectionCode }
            humanitesClasses.map { clazz ->
                OfficialSecondaryRow(
                    sectionName = section.name,
                    optionName = option.name,
                    className = clazz.name,
                )
            }
        }
        return tronc + humanites
    }

    fun schoolTypesOffered(type: SchoolType): Set<EducationCycle> = when (type) {
        SchoolType.MATERNELLE -> setOf(EducationCycle.MATERNELLE)
        SchoolType.PRIMAIRE -> setOf(EducationCycle.PRIMAIRE)
        SchoolType.SECONDAIRE -> setOf(EducationCycle.TRONC_COMMUN, EducationCycle.HUMANITES)
        SchoolType.MATERNELLE_ET_PRIMAIRE -> setOf(EducationCycle.MATERNELLE, EducationCycle.PRIMAIRE)
        SchoolType.PRIMAIRE_ET_SECONDAIRE -> setOf(
            EducationCycle.PRIMAIRE,
            EducationCycle.TRONC_COMMUN,
            EducationCycle.HUMANITES,
        )
        SchoolType.COMPLET -> EducationCycle.entries.toSet()
    }
}
