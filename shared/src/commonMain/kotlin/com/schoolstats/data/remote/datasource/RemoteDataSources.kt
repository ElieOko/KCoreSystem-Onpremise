package com.schoolstats.data.remote.datasource

import com.schoolstats.data.remote.SupabaseClientProvider
import com.schoolstats.data.remote.dto.DashboardDto
import com.schoolstats.data.remote.dto.PrimaryClassStatDto
import com.schoolstats.data.remote.dto.ProfileDto
import com.schoolstats.data.remote.dto.SchoolDto
import com.schoolstats.data.remote.dto.SchoolYearDto
import com.schoolstats.data.remote.dto.SubmissionDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest

class RemoteAuthDataSource(
    private val clientProvider: SupabaseClientProvider,
) {
    suspend fun login(email: String, password: String) {
        val client = clientProvider.getOrCreate() ?: error("Supabase non configuré")
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun logout() {
        clientProvider.getOrCreate()?.auth?.signOut()
    }

    suspend fun currentUserId(): String? {
        return clientProvider.getOrCreate()?.auth?.currentUserOrNull()?.id
    }
}

class RemoteProfileDataSource(
    private val clientProvider: SupabaseClientProvider,
) {
    suspend fun fetchCurrentProfile(userId: String): ProfileDto {
        val client = clientProvider.getOrCreate() ?: error("Supabase non configuré")
        return client.postgrest["profiles"].select {
            filter { eq("id", userId) }
            limit(1)
        }.decodeSingle<ProfileDto>()
    }
}

class RemoteSchoolDataSource(
    private val clientProvider: SupabaseClientProvider,
) {
    suspend fun fetchSchools(): List<SchoolDto> {
        val client = clientProvider.getOrCreate() ?: return emptyList()
        return client.postgrest["schools"].select {
            filter { eq("is_active", true) }
        }.decodeList<SchoolDto>()
    }
}

class RemoteSchoolYearDataSource(
    private val clientProvider: SupabaseClientProvider,
) {
    suspend fun fetchActiveYears(): List<SchoolYearDto> {
        val client = clientProvider.getOrCreate() ?: return emptyList()
        return client.postgrest["school_years"].select {
            filter { eq("is_active", true) }
        }.decodeList<SchoolYearDto>()
    }
}

class RemoteSubmissionDataSource(
    private val clientProvider: SupabaseClientProvider,
) {
    suspend fun fetchSubmissions(): List<SubmissionDto> {
        val client = clientProvider.getOrCreate() ?: return emptyList()
        return client.postgrest["submissions"].select().decodeList<SubmissionDto>()
    }

    suspend fun submit(submissionId: String) {
        val client = clientProvider.getOrCreate() ?: error("Supabase non configuré")
        client.postgrest["submissions"].update(
            {
                set("status", "SOUMIS")
            },
        ) {
            filter { eq("id", submissionId) }
        }
    }
}

class RemoteDashboardDataSource(
    private val clientProvider: SupabaseClientProvider,
) {
    suspend fun fetchDashboard(subdivisionId: String?, schoolYearId: String?): DashboardDto? {
        val client = clientProvider.getOrCreate() ?: return null
        return client.postgrest["vw_subdivision_dashboard"].select {
            subdivisionId?.let { filter { eq("subdivision_id", it) } }
            schoolYearId?.let { filter { eq("school_year_id", it) } }
            limit(1)
        }.decodeList<DashboardDto>().firstOrNull()
    }
}

class RemoteStatisticsDataSource(
    private val clientProvider: SupabaseClientProvider,
) {
    suspend fun fetchPrimaryStats(submissionId: String): List<PrimaryClassStatDto> {
        val client = clientProvider.getOrCreate() ?: return emptyList()
        return client.postgrest["primary_class_statistics"].select {
            filter { eq("submission_id", submissionId) }
        }.decodeList<PrimaryClassStatDto>()
    }

    suspend fun upsertPrimaryStats(stats: List<PrimaryClassStatDto>) {
        val client = clientProvider.getOrCreate() ?: error("Supabase non configuré")
        client.postgrest["primary_class_statistics"].upsert(stats)
    }
}
