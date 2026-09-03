package com.schoolstats.di

import com.schoolstats.data.export.ExportService
import com.schoolstats.data.export.JvmExportService
import org.koin.dsl.module

val jvmModule = module {
    single<ExportService> { JvmExportService() }
}
