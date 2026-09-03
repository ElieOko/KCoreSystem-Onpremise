package com.schoolstats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolstats.domain.model.UserProfile
import com.schoolstats.domain.usecase.auth.LoginUseCase
import com.schoolstats.domain.usecase.auth.LogoutUseCase
import com.schoolstats.domain.usecase.auth.ObserveCurrentProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val profile: UserProfile? = null,
    val error: String? = null,
    val isConfigured: Boolean = true,
)

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    observeCurrentProfileUseCase: ObserveCurrentProfileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeCurrentProfileUseCase().collect { profile ->
                _uiState.update {
                    it.copy(
                        profile = profile,
                        isAuthenticated = profile != null,
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            loginUseCase(email, password)
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true, profile = profile) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.update { AuthUiState() }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
