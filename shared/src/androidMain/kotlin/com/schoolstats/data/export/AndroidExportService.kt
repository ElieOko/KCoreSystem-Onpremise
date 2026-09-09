package com.schoolstats.data.export

import com.schoolstats.data.local.AndroidContextHolder
import com.schoolstats.domain.model.ReportRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AndroidExportService : ExportService {
    override suspend fun exportExcel(report: ReportRequest, path: String): Result<String> = writeReport(report, "rapport.csv")

    override suspend fun exportPdf(report: ReportRequest, path: String): Result<String> = writeReport(report, "rapport.txt")

    private suspend fun writeReport(report: ReportRequest, filename: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val app = AndroidContextHolder.require()
            val dir = app.getExternalFilesDir(null) ?: app.filesDir
            val file = File(dir, filename)
            file.writeText(buildString {
                appendLine(report.title)
                appendLine("Année scolaire: ${report.schoolYearName}")
                report.subdivisionName?.let { appendLine("Sous-Division: $it") }
                appendLine()
                appendLine("Élèves: ${report.stats.totalStudents} (G ${report.stats.totalBoys} / F ${report.stats.totalGirls})")
                appendLine("Enseignants: ${report.stats.totalTeachers}")
                appendLine("Ouvriers: ${report.stats.totalWorkers}")
                appendLine("Administratif: ${report.stats.totalAdminStaff}")
                appendLine()
                appendLine("Classe;Garçons;Filles;Total")
                report.stats.byClass.forEach {
                    appendLine("${it.className};${it.boysCount};${it.girlsCount};${it.totalCount}")
                }
            })
            file.absolutePath
        }
    }
}
