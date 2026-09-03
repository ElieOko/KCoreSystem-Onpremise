package com.schoolstats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SchoolOwnership
import com.schoolstats.domain.model.SchoolType
import com.schoolstats.domain.usecase.school.ObserveSchoolsUseCase
import com.schoolstats.domain.usecase.school.SaveSchoolUseCase
import com.schoolstats.domain.usecase.school.SyncSchoolsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SchoolsUiState(
    val isLoading: Boolean = true,
    val schools: List<School> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null,
    val showForm: Boolean = false,
    val editingSchool: School? = null,
)

class SchoolsViewModel(
    private val observeSchoolsUseCase: ObserveSchoolsUseCase,
    private val saveSchoolUseCase: SaveSchoolUseCase,
    private val syncSchoolsUseCase: SyncSchoolsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SchoolsUiState())
    val uiState: StateFlow<SchoolsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            syncSchoolsUseCase()
            observeSchoolsUseCase(_uiState.value.searchQuery).collect { schools ->
                _uiState.update { it.copy(isLoading = false, schools = schools) }
            }
        }
    }

    fun onSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            observeSchoolsUseCase(query).collect { schools ->
                _uiState.update { it.copy(schools = schools) }
            }
        }
    }

    fun openCreateForm() {
        _uiState.update {
            it.copy(
                showForm = true,
                editingSchool = School(
                    id = "",
                    subdivisionId = "",
                    name = "",
                    schoolCode = "",
                    schoolType = SchoolType.PRIMAIRE,
                    ownership = SchoolOwnership.PUBLIQUE,
                ),
            )
        }
    }

    fun openEditForm(school: School) {
        _uiState.update { it.copy(showForm = true, editingSchool = school) }
    }

    fun closeForm() {
        _uiState.update { it.copy(showForm = false, editingSchool = null) }
    }

    fun saveSchool(school: School) {
        viewModelScope.launch {
            saveSchoolUseCase(school)
                .onSuccess {
                    closeForm()
                    load()
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
        }
    }
}
