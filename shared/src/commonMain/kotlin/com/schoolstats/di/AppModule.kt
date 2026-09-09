package com.schoolstats.di

import com.schoolstats.data.export.ExportService
import com.schoolstats.data.local.createDatabaseDriverFactory
import com.schoolstats.data.local.database.SchoolStatsDatabase
import com.schoolstats.data.local.datasource.ExtendedStatisticsLocalDataSource
import com.schoolstats.data.local.datasource.LocalSchoolDataSource
import com.schoolstats.data.local.datasource.LocalStatisticsDataSource
import com.schoolstats.data.local.datasource.LocalSubmissionDataSource
import com.schoolstats.data.remote.SupabaseClientProvider
import com.schoolstats.data.remote.datasource.RemoteAuthDataSource
import com.schoolstats.data.remote.datasource.RemoteDashboardDataSource
import com.schoolstats.data.remote.datasource.RemoteProfileDataSource
import com.schoolstats.data.remote.datasource.RemoteSchoolDataSource
import com.schoolstats.data.remote.datasource.RemoteSchoolYearDataSource
import com.schoolstats.data.remote.datasource.RemoteStatisticsDataSource
import com.schoolstats.data.remote.datasource.RemoteSubmissionDataSource
import com.schoolstats.data.repository.AuthRepositoryImpl
import com.schoolstats.data.repository.CentralizationRepositoryImpl
import com.schoolstats.data.repository.DashboardRepositoryImpl
import com.schoolstats.data.repository.NotificationRepositoryImpl
import com.schoolstats.data.repository.ReportRepositoryImpl
import com.schoolstats.data.repository.SchoolRepositoryImpl
import com.schoolstats.data.repository.SchoolYearRepositoryImpl
import com.schoolstats.data.repository.StatisticsRepositoryImpl
import com.schoolstats.data.repository.SubmissionRepositoryImpl
import com.schoolstats.data.repository.UserManagementRepositoryImpl
import com.schoolstats.data.sync.NetworkMonitor
import com.schoolstats.data.sync.SyncManager
import com.schoolstats.domain.repository.AuthRepository
import com.schoolstats.domain.repository.CentralizationRepository
import com.schoolstats.domain.repository.DashboardRepository
import com.schoolstats.domain.repository.NotificationRepository
import com.schoolstats.domain.repository.ReportRepository
import com.schoolstats.domain.repository.SchoolRepository
import com.schoolstats.domain.repository.SchoolYearRepository
import com.schoolstats.domain.repository.StatisticsRepository
import com.schoolstats.domain.repository.SubmissionRepository
import com.schoolstats.domain.repository.UserManagementRepository
import com.schoolstats.domain.usecase.auth.LoginUseCase
import com.schoolstats.domain.usecase.auth.LogoutUseCase
import com.schoolstats.domain.usecase.auth.ObserveCurrentProfileUseCase
import com.schoolstats.domain.usecase.school.ObserveSchoolsUseCase
import com.schoolstats.domain.usecase.school.SaveSchoolUseCase
import com.schoolstats.domain.usecase.school.SyncSchoolsUseCase
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
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val dataModule = module {
    single { SupabaseClientProvider() }
    single { SchoolStatsDatabase(createDatabaseDriverFactory().createDriver()) }
    single { LocalSchoolDataSource(get()) }
    single { LocalSubmissionDataSource(get()) }
    single { LocalStatisticsDataSource(get()) }
    single { ExtendedStatisticsLocalDataSource(get()) }
    single { RemoteAuthDataSource(get()) }
    single { RemoteProfileDataSource(get()) }
    single { RemoteSchoolDataSource(get()) }
    single { RemoteSchoolYearDataSource(get()) }
    single { RemoteSubmissionDataSource(get()) }
    single { RemoteDashboardDataSource(get()) }
    single { RemoteStatisticsDataSource(get()) }
    single { NetworkMonitor() }
    single {
        SyncManager(get()) {
            get<SchoolRepository>().syncSchools()
        }
    }
    single<AuthRepository> {
        AuthRepositoryImpl(get(), get(), get(), get(), get(), get(), get())
    }
    single<SchoolRepository> { SchoolRepositoryImpl(get(), get(), get()) }
    single<SchoolYearRepository> { SchoolYearRepositoryImpl(get()) }
    single<SubmissionRepository> { SubmissionRepositoryImpl(get(), get(), get(), get()) }
    single<StatisticsRepository> { StatisticsRepositoryImpl(get(), get(), get()) }
    single<DashboardRepository> { DashboardRepositoryImpl(get(), get(), get()) }
    single<CentralizationRepository> { CentralizationRepositoryImpl(get(), get(), get()) }
    single<NotificationRepository> { NotificationRepositoryImpl(get()) }
    single<UserManagementRepository> { UserManagementRepositoryImpl() }
    single<ReportRepository> { ReportRepositoryImpl(get(), get()) }
}

val domainModule = module {
    factory { LoginUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { ObserveCurrentProfileUseCase(get()) }
    factory { ObserveSchoolsUseCase(get()) }
    factory { SaveSchoolUseCase(get()) }
    factory { SyncSchoolsUseCase(get()) }
}

val viewModelModule = module {
    viewModel { AuthViewModel(get(), get(), get()) }
    viewModel { DashboardViewModel(get(), get()) }
    viewModel { SchoolsViewModel(get(), get(), get()) }
    viewModel { SubmissionsViewModel(get()) }
    viewModel { StatisticsViewModel(get(), get()) }
    viewModel { ValidationViewModel(get()) }
    viewModel { CentralizationViewModel(get(), get()) }
    viewModel { ReportsViewModel(get(), get()) }
    viewModel { UsersViewModel(get()) }
    viewModel { SettingsViewModel() }
}

val appModules = listOf(dataModule, domainModule, viewModelModule)
