package com.schoolstats.data.local

import android.app.Application
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.schoolstats.data.local.database.SchoolStatsDatabase

object AndroidContextHolder {
    var application: Application? = null

    fun require(): Application =
        application ?: error("AndroidContextHolder n'est pas initialisé.")
}

class AndroidDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(
            schema = SchoolStatsDatabase.Schema,
            context = AndroidContextHolder.require(),
            name = "schoolstats.db",
        )
        driver.ensureCensusTables()
        return driver
    }
}

actual fun createDatabaseDriverFactory(): DatabaseDriverFactory = AndroidDatabaseDriverFactory()
