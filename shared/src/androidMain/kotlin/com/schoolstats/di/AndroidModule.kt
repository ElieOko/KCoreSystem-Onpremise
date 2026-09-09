package com.schoolstats.di

import com.schoolstats.data.export.AndroidExportService
import com.schoolstats.data.export.ExportService
import org.koin.dsl.module

val androidModule = module {
    single<ExportService> { AndroidExportService() }
}
