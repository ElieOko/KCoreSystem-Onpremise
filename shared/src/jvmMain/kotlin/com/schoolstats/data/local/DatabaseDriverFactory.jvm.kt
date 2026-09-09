package com.schoolstats.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.schoolstats.data.config.AppConfig
import com.schoolstats.data.local.database.SchoolStatsDatabase

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): JdbcSqliteDriver {
        val driver = JdbcSqliteDriver("jdbc:sqlite:${AppConfig.databasePath}")
        runCatching { SchoolStatsDatabase.Schema.create(driver) }
        driver.ensureCensusTables()
        return driver
    }
}

actual fun createDatabaseDriverFactory(): DatabaseDriverFactory = JvmDatabaseDriverFactory()
