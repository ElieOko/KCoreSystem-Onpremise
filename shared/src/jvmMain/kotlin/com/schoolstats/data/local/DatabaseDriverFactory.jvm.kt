package com.schoolstats.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.schoolstats.data.config.AppConfig
import com.schoolstats.data.local.database.SchoolStatsDatabase
import java.io.File
import java.util.Properties

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): JdbcSqliteDriver {
        Class.forName(SQLITE_JDBC_CLASS)
        val dbFile = File(AppConfig.databasePath)
        dbFile.parentFile?.mkdirs()
        val driver = JdbcSqliteDriver(
            url = sqliteUrl(dbFile),
            properties = Properties(),
            schema = SchoolStatsDatabase.Schema,
        )
        driver.ensureCensusTables()
        return driver
    }

    companion object {
        const val SQLITE_JDBC_CLASS = "org.sqlite.JDBC"

        fun sqliteUrl(path: String): String = "jdbc:sqlite:${path.replace('\\', '/')}"

        fun sqliteUrl(dbFile: File): String = sqliteUrl(dbFile.absoluteFile.invariantSeparatorsPath)
    }
}

actual fun createDatabaseDriverFactory(): DatabaseDriverFactory = JvmDatabaseDriverFactory()
