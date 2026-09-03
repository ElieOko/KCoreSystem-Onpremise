package com.schoolstats.data.config

data class SupabaseConfig(
    val url: String,
    val anonKey: String,
) {
    val isValid: Boolean get() = url.isNotBlank() && anonKey.isNotBlank()
}

expect object AppConfig {
    val supabase: SupabaseConfig
    val databasePath: String
}
