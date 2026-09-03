package com.schoolstats.domain.usecase.auth

import com.schoolstats.domain.model.UserProfile
import com.schoolstats.domain.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(email: String, password: String): Result<UserProfile> {
        if (email.isBlank()) return Result.failure(IllegalArgumentException("L'email est obligatoire."))
        if (password.isBlank()) return Result.failure(IllegalArgumentException("Le mot de passe est obligatoire."))
        if (!authRepository.isConfigured()) {
            if (email.equals("demo@local", ignoreCase = true) && password == "demo") {
                return authRepository.login(email, password)
            }
            return Result.failure(IllegalStateException("Supabase non configuré. Utilisez demo@local / demo."))
        }
        return authRepository.login(email.trim(), password)
    }
}

class LogoutUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke() = authRepository.logout()
}

class ObserveCurrentProfileUseCase(
    private val authRepository: AuthRepository,
) {
    operator fun invoke() = authRepository.currentProfile
}
