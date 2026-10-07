package com.schoolstats.data.local

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.sql.DriverManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class JvmSqliteDriverTest {
    @Test
    fun `sqlite jdbc driver is registered for DriverManager`() {
        Class.forName(JvmDatabaseDriverFactory.SQLITE_JDBC_CLASS)
        val driver = DriverManager.getDriver("jdbc:sqlite:")
        assertNotNull(driver)
        assertEquals("org.sqlite.JDBC", driver.javaClass.name)
    }

    @Test
    fun `windows style path is normalized for jdbc sqlite`() {
        val url = JvmDatabaseDriverFactory.sqliteUrl("C:\\Users\\demo\\.kcoresystem\\schoolstats.db")
        assertEquals("jdbc:sqlite:C:/Users/demo/.kcoresystem/schoolstats.db", url)
    }

    @Test
    fun `in memory sqlite driver opens`() {
        Class.forName(JvmDatabaseDriverFactory.SQLITE_JDBC_CLASS)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, "SELECT 1", 0)
        driver.close()
    }
}
