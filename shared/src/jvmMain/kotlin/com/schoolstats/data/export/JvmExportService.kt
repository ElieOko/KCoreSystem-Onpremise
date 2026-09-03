package com.schoolstats.data.export

import com.lowagie.text.Document
import com.lowagie.text.FontFactory
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import com.schoolstats.domain.model.ReportRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream

class JvmExportService : ExportService {
    override suspend fun exportExcel(report: ReportRequest, path: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Statistiques")
            var rowIdx = 0
            fun row(cells: List<String>) {
                val row = sheet.createRow(rowIdx++)
                cells.forEachIndexed { i, v -> row.createCell(i).setCellValue(v) }
            }
            row(listOf(report.title))
            row(listOf("Année scolaire", report.schoolYearName))
            report.subdivisionName?.let { row(listOf("Sous-Division", it)) }
            row(emptyList())
            row(listOf("Total garçons", report.stats.totalBoys.toString()))
            row(listOf("Total filles", report.stats.totalGirls.toString()))
            row(listOf("Total élèves", report.stats.totalStudents.toString()))
            row(listOf("Total enseignants", report.stats.totalTeachers.toString()))
            row(emptyList())
            row(listOf("Classe", "Garçons", "Filles", "Total"))
            report.stats.byClass.forEach { row(listOf(it.className, it.boysCount.toString(), it.girlsCount.toString(), it.totalCount.toString())) }
            row(emptyList())
            row(listOf("Section", "Option", "Classe", "Garçons", "Filles", "Total"))
            report.stats.bySection.forEach {
                row(listOf(it.sectionName, it.optionName.orEmpty(), it.className, it.boysCount.toString(), it.girlsCount.toString(), it.totalCount.toString()))
            }
            row(emptyList())
            row(listOf("Classe", "Début", "Fin", "Différence", "Rétention %"))
            report.stats.enrollments.forEach {
                row(listOf(it.className, it.beginningTotal.toString(), it.endTotal.toString(), it.difference.toString(), it.retentionRate?.let { r -> "%.1f".format(r) }.orEmpty()))
            }
            File(path).parentFile?.mkdirs()
            FileOutputStream(path).use { workbook.write(it) }
            workbook.close()
            path
        }
    }

    override suspend fun exportPdf(report: ReportRequest, path: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            File(path).parentFile?.mkdirs()
            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(path))
            document.open()
            val titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16f)
            val bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11f)
            document.add(Paragraph(report.title, titleFont))
            document.add(Paragraph("Année scolaire: ${report.schoolYearName}", bodyFont))
            report.subdivisionName?.let { document.add(Paragraph("Sous-Division: $it", bodyFont)) }
            document.add(Paragraph(" "))
            document.add(Paragraph("Total élèves: ${report.stats.totalStudents} (G: ${report.stats.totalBoys} / F: ${report.stats.totalGirls})", bodyFont))
            document.add(Paragraph("Total enseignants: ${report.stats.totalTeachers}", bodyFont))
            document.add(Paragraph(" "))
            document.add(Paragraph("Effectifs par classe", titleFont))
            report.stats.byClass.forEach {
                document.add(Paragraph("${it.className}: G=${it.boysCount} F=${it.girlsCount} T=${it.totalCount}", bodyFont))
            }
            document.add(Paragraph(" "))
            document.add(Paragraph("Résultats certificatifs", titleFont))
            report.stats.certifications.forEach {
                document.add(Paragraph("${it.examName} ${it.className}: ${it.successesCount}/${it.participantsCount} (${it.successRate?.let { r -> "%.1f%%".format(r) }.orEmpty()})", bodyFont))
            }
            document.close()
            path
        }
    }
}
