package com.schoolstats.data.config

import java.io.File
import java.util.Properties

actual object AppConfig {
    private val properties: Properties by lazy { loadProperties() }

    actual val supabase: SupabaseConfig
        get() = SupabaseConfig(
            url = resolve("supabase.url", "SUPABASE_URL"),
            anonKey = resolve("supabase.anon.key", "SUPABASE_ANON_KEY"),
        )

    actual val databasePath: String
        get() {
            val appDir = File(System.getProperty("user.home"), ".kcoresystem")
            appDir.mkdirs()
            return File(appDir, "schoolstats.db").absolutePath
        }

    private fun resolve(propertyKey: String, envKey: String): String {
        return System.getenv(envKey)?.takeIf { it.isNotBlank() }
            ?: properties.getProperty(propertyKey)?.takeIf { it.isNotBlank() }
            ?: ""
    }

    private fun loadProperties(): Properties {
        val props = Properties()
        val candidates = listOf(
            File("local.properties"),
            File(System.getProperty("user.dir"), "local.properties"),
            File(System.getProperty("user.home"), ".kcoresystem/local.properties"),
        )
        candidates.firstOrNull { it.exists() }?.inputStream()?.use { props.load(it) }
        return props
    }
}
