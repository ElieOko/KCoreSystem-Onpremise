package com.schoolstats.data.remote

import com.schoolstats.data.config.AppConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

class SupabaseClientProvider {
  private var client: SupabaseClient? = null

  fun getOrCreate(): SupabaseClient? {
    if (!AppConfig.supabase.isValid) return null
    return client ?: createSupabaseClient(
      supabaseUrl = AppConfig.supabase.url,
      supabaseKey = AppConfig.supabase.anonKey,
    ) {
      install(Auth)
      install(Postgrest)
      install(Realtime)
      install(Storage)
      httpEngine = createHttpEngine()
    }.also { client = it }
  }

  fun clear() {
    client = null
  }
}
