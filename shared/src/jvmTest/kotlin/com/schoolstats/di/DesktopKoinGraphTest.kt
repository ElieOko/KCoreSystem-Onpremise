package com.schoolstats.di

import com.schoolstats.data.export.ExportService
import com.schoolstats.data.local.JvmDatabaseDriverFactory
import com.schoolstats.data.local.database.SchoolStatsDatabase
import com.schoolstats.domain.repository.AuthRepository
import com.schoolstats.presentation.viewmodel.AuthViewModel
import com.schoolstats.presentation.viewmodel.CentralizationViewModel
import com.schoolstats.presentation.viewmodel.DashboardViewModel
import com.schoolstats.presentation.viewmodel.ReportsViewModel
import com.schoolstats.presentation.viewmodel.SchoolsViewModel
import com.schoolstats.presentation.viewmodel.SettingsViewModel
import com.schoolstats.presentation.viewmodel.StatisticsViewModel
import com.schoolstats.presentation.viewmodel.SubmissionsViewModel
import com.schoolstats.presentation.viewmodel.UsersViewModel
import com.schoolstats.presentation.viewmodel.ValidationViewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform.getKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class DesktopKoinGraphTest {
    @BeforeTest
    fun setUp() {
        System.setProperty(JvmDatabaseDriverFactory.MEMORY_PROPERTY, "true")
        JvmDatabaseDriverFactory.prepareNativeLibraries()
        startKoin { modules(appModules + jvmModule) }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        System.clearProperty(JvmDatabaseDriverFactory.MEMORY_PROPERTY)
    }

    @Test
    fun `auth viewmodel and all desktop dependencies resolve`() {
        val koin = getKoin()
        assertNotNull(koin.get<SchoolStatsDatabase>())
        assertNotNull(koin.get<AuthRepository>())
        assertNotNull(koin.get<ExportService>())
        assertNotNull(koin.get<AuthViewModel>())
        assertNotNull(koin.get<SettingsViewModel>())
        assertNotNull(koin.get<DashboardViewModel>())
        assertNotNull(koin.get<SchoolsViewModel>())
        assertNotNull(koin.get<SubmissionsViewModel>())
        assertNotNull(koin.get<StatisticsViewModel>())
        assertNotNull(koin.get<ValidationViewModel>())
        assertNotNull(koin.get<CentralizationViewModel>())
        assertNotNull(koin.get<ReportsViewModel>())
        assertNotNull(koin.get<UsersViewModel>())
    }
}
