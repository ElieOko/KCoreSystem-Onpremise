package com.schoolstats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.repository.StatisticsRepository
import com.schoolstats.domain.repository.SubmissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubmissionsUiState(
    val submissions: List<Submission> = emptyList(),
    val statusFilter: String? = null,
)

class SubmissionsViewModel(
    private val submissionRepository: SubmissionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubmissionsUiState())
    val uiState: StateFlow<SubmissionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            submissionRepository.observeSubmissions(null).collect { submissions ->
                _uiState.update { it.copy(submissions = submissions) }
            }
        }
    }

    fun filterByStatus(status: String?) {
        _uiState.update { it.copy(statusFilter = status) }
        viewModelScope.launch {
            submissionRepository.observeSubmissions(status).collect { submissions ->
                _uiState.update { it.copy(submissions = submissions) }
            }
        }
    }
}

data class PrimaryStatsUiState(
    val submissionId: String = "",
    val schoolId: String = "",
    val stats: List<PrimaryClassStat> = defaultPrimaryClasses(),
    val error: String? = null,
    val saved: Boolean = false,
)

private fun defaultPrimaryClasses(): List<PrimaryClassStat> = listOf(
    PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "2ème année", classOrder = 2, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "3ème année", classOrder = 3, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "4ème année", classOrder = 4, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "5ème année", classOrder = 5, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "6ème année", classOrder = 6, boysCount = 0, girlsCount = 0),
)

class PrimaryStatsViewModel(
    private val statisticsRepository: StatisticsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PrimaryStatsUiState())
    val uiState: StateFlow<PrimaryStatsUiState> = _uiState.asStateFlow()

    fun init(submissionId: String, schoolId: String) {
        _uiState.update { it.copy(submissionId = submissionId, schoolId = schoolId) }
        viewModelScope.launch {
            statisticsRepository.observePrimaryStats(submissionId).collect { stats ->
                if (stats.isNotEmpty()) {
                    _uiState.update { it.copy(stats = stats) }
                }
            }
        }
    }

    fun updateStat(index: Int, boys: Int? = null, girls: Int? = null) {
        _uiState.update { state ->
            val updated = state.stats.toMutableList()
            val current = updated[index]
            updated[index] = current.copy(
                boysCount = boys ?: current.boysCount,
                girlsCount = girls ?: current.girlsCount,
            )
            state.copy(stats = updated, saved = false)
        }
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            statisticsRepository.savePrimaryStats(state.submissionId, state.schoolId, state.stats)
                .onSuccess { _uiState.update { it.copy(saved = true, error = null) } }
                .onFailure { error -> _uiState.update { it.copy(error = error.message) } }
        }
    }

    val totalBoys: Int get() = _uiState.value.stats.sumOf { it.boysCount }
    val totalGirls: Int get() = _uiState.value.stats.sumOf { it.girlsCount }
    val totalStudents: Int get() = totalBoys + totalGirls
}
