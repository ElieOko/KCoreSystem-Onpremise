package com.schoolstats.data.export

import com.schoolstats.domain.model.ReportRequest

interface ExportService {
    suspend fun exportExcel(report: ReportRequest, path: String): Result<String>
    suspend fun exportPdf(report: ReportRequest, path: String): Result<String>
}
