package com.schoolstats.data.local

import app.cash.sqldelight.db.SqlDriver

interface DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

expect fun createDatabaseDriverFactory(): DatabaseDriverFactory
