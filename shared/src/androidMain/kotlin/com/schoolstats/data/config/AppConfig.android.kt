package com.schoolstats.data.config

import com.schoolstats.data.local.AndroidContextHolder
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
        get() = "schoolstats.db"

    private fun resolve(propertyKey: String, envKey: String): String {
        return System.getenv(envKey)?.takeIf { it.isNotBlank() }
            ?: properties.getProperty(propertyKey)?.takeIf { it.isNotBlank() }
            ?: ""
    }

    private fun loadProperties(): Properties {
        val props = Properties()
        val context = AndroidContextHolder.application ?: return props
        runCatching { context.assets.open("local.properties").use { props.load(it) } }
        val file = File(context.filesDir, "local.properties")
        if (file.exists()) file.inputStream().use { props.load(it) }
        return props
    }
}
