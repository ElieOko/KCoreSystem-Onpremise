package com.schoolstats.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.schoolstats.data.config.AppConfig
import com.schoolstats.data.local.database.SchoolStatsDatabase
import java.io.File
import java.util.Properties

class JvmDatabaseDriverFactory : DatabaseDriverFactory {
    override fun createDriver(): JdbcSqliteDriver {
        prepareNativeLibraries()
        Class.forName(SQLITE_JDBC_CLASS)
        if (System.getProperty(MEMORY_PROPERTY) == "true") {
            return openDriver(JdbcSqliteDriver.IN_MEMORY)
        }
        return runCatching { openFileDriver() }
            .getOrElse { fileError ->
                runCatching { openDriver(JdbcSqliteDriver.IN_MEMORY) }
                    .getOrElse { memoryError ->
                        throw IllegalStateException(
                            buildString {
                                appendLine("Impossible d'ouvrir la base SQLite locale.")
                                appendLine("Fichier: ${fileError.message}")
                                append("Mémoire: ${memoryError.message}")
                            },
                            fileError,
                        )
                    }
            }
    }

    private fun openFileDriver(): JdbcSqliteDriver {
        val dbFile = File(AppConfig.databasePath)
        dbFile.parentFile?.mkdirs()
        return openDriver(sqliteUrl(dbFile))
    }

    private fun openDriver(url: String): JdbcSqliteDriver {
        val driver = JdbcSqliteDriver(url = url, properties = Properties())
        // create() échoue si les tables existent déjà : on ignore pour rester compatible
        // avec une base déjà créée par une version précédente.
        runCatching { SchoolStatsDatabase.Schema.create(driver) }
        driver.ensureCensusTables()
        return driver
    }

    companion object {
        const val SQLITE_JDBC_CLASS = "org.sqlite.JDBC"
        const val MEMORY_PROPERTY = "kcoresystem.db.memory"

        fun prepareNativeLibraries() {
            val nativeDir = File(System.getProperty("user.home"), ".kcoresystem/native")
            nativeDir.mkdirs()
            System.setProperty("org.sqlite.tmpdir", nativeDir.absolutePath)
        }

        fun sqliteUrl(path: String): String = "jdbc:sqlite:${path.replace('\\', '/')}"

        fun sqliteUrl(dbFile: File): String = sqliteUrl(dbFile.absoluteFile.invariantSeparatorsPath)
    }
}

actual fun createDatabaseDriverFactory(): DatabaseDriverFactory = JvmDatabaseDriverFactory()
