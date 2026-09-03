package com.schoolstats.data.repository

import com.schoolstats.data.mapper.toDomain
import com.schoolstats.data.remote.SupabaseClientProvider
import com.schoolstats.data.remote.datasource.RemoteAuthDataSource
import com.schoolstats.data.remote.datasource.RemoteProfileDataSource
import com.schoolstats.domain.model.UserProfile
import com.schoolstats.domain.model.UserRole
import com.schoolstats.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthRepositoryImpl(
    private val clientProvider: SupabaseClientProvider,
    private val remoteAuth: RemoteAuthDataSource,
    private val remoteProfile: RemoteProfileDataSource,
) : AuthRepository {
    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    override val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()

    override fun isConfigured(): Boolean = clientProvider.getOrCreate() != null

    override suspend fun login(email: String, password: String): Result<UserProfile> = runCatching {
        if (!isConfigured()) {
            if (email.equals("demo@local", ignoreCase = true) && password == "demo") {
                val demo = UserProfile(
                    id = "demo-admin",
                    fullName = "Admin Démo",
                    email = email,
                    role = UserRole.ADMIN_SOUS_DIVISION,
                    subdivisionId = "demo-subdivision",
                )
                _currentProfile.value = demo
                return@runCatching demo
            }
            error("Supabase non configuré. Utilisez demo@local / demo pour le mode démo.")
        }
        remoteAuth.login(email, password)
        refreshProfile().getOrThrow()
    }

    override suspend fun logout() {
        remoteAuth.logout()
        clientProvider.clear()
        _currentProfile.value = null
    }

    override suspend fun refreshProfile(): Result<UserProfile> = runCatching {
        val userId = remoteAuth.currentUserId() ?: error("Session invalide")
        val profile = remoteProfile.fetchCurrentProfile(userId).toDomain()
        _currentProfile.value = profile
        profile
    }
}
