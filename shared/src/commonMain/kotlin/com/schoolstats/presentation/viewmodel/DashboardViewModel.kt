package com.schoolstats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.repository.DashboardRepository
import com.schoolstats.domain.usecase.school.SyncSchoolsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val stats: DashboardStats = DashboardStats(),
    val error: String? = null,
)

class DashboardViewModel(
    private val dashboardRepository: DashboardRepository,
    private val syncSchoolsUseCase: SyncSchoolsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            syncSchoolsUseCase().onFailure { /* offline fallback */ }
            dashboardRepository.observeDashboard(null).collect { stats ->
                _uiState.update { it.copy(isLoading = false, stats = stats) }
            }
        }
    }
}
