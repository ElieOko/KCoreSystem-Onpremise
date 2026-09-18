package com.schoolstats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolstats.data.demo.DemoDataSeeder
import com.schoolstats.domain.census.RegistryAggregator
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.SchoolRegistrySummary
import com.schoolstats.domain.model.UserRole
import com.schoolstats.domain.repository.AuthRepository
import com.schoolstats.domain.repository.DashboardRepository
import com.schoolstats.domain.repository.SchoolRegistryRepository
import com.schoolstats.domain.repository.SchoolRepository
import com.schoolstats.domain.repository.SchoolYearRepository
import com.schoolstats.domain.usecase.school.SyncSchoolsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isSchoolAccount: Boolean = false,
    val stats: DashboardStats = DashboardStats(),
    val registry: SchoolRegistrySummary = SchoolRegistrySummary(),
    val error: String? = null,
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val syncSchoolsUseCase: SyncSchoolsUseCase,
    private val authRepository: AuthRepository,
    private val registryRepository: SchoolRegistryRepository,
    private val schoolRepository: SchoolRepository,
    private val schoolYearRepository: SchoolYearRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val profile = authRepository.currentProfile.value
        val isSchool = profile?.role == UserRole.ECOLE
        val schoolId = profile?.schoolId
        _uiState.update { it.copy(isLoading = true, error = null, isSchoolAccount = isSchool) }
        viewModelScope.launch {
            syncSchoolsUseCase().onFailure { /* offline fallback */ }
            if (isSchool && !schoolId.isNullOrBlank()) {
                val yearId = schoolYearRepository.getDefaultYear().id
                val schoolName = schoolRepository.getSchool(schoolId)?.name ?: DemoDataSeeder.DEMO_SCHOOL_NAME
                registryRepository.seedDemoIfEmpty(schoolId, yearId)
                combine(
                    registryRepository.observeStudents(schoolId, yearId),
                    registryRepository.observeWorkers(schoolId, yearId),
                ) { students, workers ->
                    RegistryAggregator.summary(schoolId, schoolName, students, workers)
                }.collect { summary ->
                    _uiState.update { it.copy(isLoading = false, registry = summary) }
                }
            } else {
                dashboardRepository.observeDashboard(null).collect { stats ->
                    _uiState.update { it.copy(isLoading = false, stats = stats) }
                }
            }
        }
    }
}
