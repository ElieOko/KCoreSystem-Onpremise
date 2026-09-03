package com.schoolstats.domain.usecase.school

import com.schoolstats.domain.model.School
import com.schoolstats.domain.repository.SchoolRepository

class ObserveSchoolsUseCase(
    private val schoolRepository: SchoolRepository,
) {
    operator fun invoke(query: String = "") = schoolRepository.observeSchools(query)
}

class SaveSchoolUseCase(
    private val schoolRepository: SchoolRepository,
) {
    suspend operator fun invoke(school: School): Result<School> {
        if (school.name.isBlank()) return Result.failure(IllegalArgumentException("Le nom de l'école est obligatoire."))
        if (school.schoolCode.isBlank()) return Result.failure(IllegalArgumentException("Le code école est obligatoire."))
        return schoolRepository.saveSchool(school)
    }
}

class SyncSchoolsUseCase(
    private val schoolRepository: SchoolRepository,
) {
    suspend operator fun invoke() = schoolRepository.syncSchools()
}
