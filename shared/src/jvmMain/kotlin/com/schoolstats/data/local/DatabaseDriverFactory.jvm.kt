package com.schoolstats.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.schoolstats.data.config.AppConfig
import com.schoolstats.data.local.database.SchoolStatsDatabase

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): JdbcSqliteDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${AppConfig.databasePath}")
        runCatching { SchoolStatsDatabase.Schema.create(driver) }
        ensureCensusTables(driver)
        return driver
    }
}

private fun ensureCensusTables(driver: SqlDriver) {
    driver.execute(
        null,
        """
        CREATE TABLE IF NOT EXISTS local_age_sex_stat (
            id TEXT NOT NULL PRIMARY KEY,
            submission_id TEXT NOT NULL,
            class_name TEXT NOT NULL,
            age INTEGER NOT NULL,
            boys_count INTEGER NOT NULL DEFAULT 0,
            girls_count INTEGER NOT NULL DEFAULT 0,
            updated_at INTEGER NOT NULL
        )
        """.trimIndent(),
        0,
    )
    driver.execute(
        null,
        """
        CREATE TABLE IF NOT EXISTS local_worker_stat (
            id TEXT NOT NULL PRIMARY KEY,
            submission_id TEXT NOT NULL,
            education_level TEXT NOT NULL,
            men_count INTEGER NOT NULL DEFAULT 0,
            women_count INTEGER NOT NULL DEFAULT 0,
            updated_at INTEGER NOT NULL
        )
        """.trimIndent(),
        0,
    )
    driver.execute(
        null,
        """
        CREATE TABLE IF NOT EXISTS local_admin_staff_stat (
            id TEXT NOT NULL PRIMARY KEY,
            submission_id TEXT NOT NULL,
            function_name TEXT NOT NULL,
            education_level TEXT NOT NULL,
            men_count INTEGER NOT NULL DEFAULT 0,
            women_count INTEGER NOT NULL DEFAULT 0,
            updated_at INTEGER NOT NULL
        )
        """.trimIndent(),
        0,
    )
}

actual fun createDatabaseDriverFactory(): DatabaseDriverFactory = JvmDatabaseDriverFactory()
