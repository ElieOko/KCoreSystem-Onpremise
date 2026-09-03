package com.schoolstats.desktop.screens.centralization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.presentation.viewmodel.CentralizationViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CentralizationScreen(viewModel: CentralizationViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val stats = state.stats
    Column(Modifier.padding(8.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Centralisation des statistiques", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Année scolaire: ${state.schoolYear}")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard("Total garçons", stats.totalBoys.toString(), Modifier.weight(1f))
            SummaryCard("Total filles", stats.totalGirls.toString(), Modifier.weight(1f))
            SummaryCard("Total élèves", stats.totalStudents.toString(), Modifier.weight(1f))
            SummaryCard("Enseignants", stats.totalTeachers.toString(), Modifier.weight(1f))
        }
        SectionTable("A. Effectif par classe", listOf("Classe", "Garçons", "Filles", "Total")) {
            stats.byClass.forEach { Text("${it.className} | ${it.boysCount} | ${it.girlsCount} | ${it.totalCount}") }
        }
        SectionTable("B. Effectif par section", listOf("Section", "Option", "Classe", "G", "F", "T")) {
            stats.bySection.forEach { Text("${it.sectionName} | ${it.optionName.orEmpty()} | ${it.className} | ${it.boysCount} | ${it.girlsCount} | ${it.totalCount}") }
        }
        SectionTable("C. Enseignants", listOf("Branche", "Niveau", "H", "F", "T")) {
            stats.teachers.forEach { Text("${it.branch.name} | ${it.educationLevel.name} | ${it.menCount} | ${it.womenCount} | ${it.totalCount}") }
        }
        SectionTable("D. Début / Fin d'année", listOf("Classe", "Début", "Fin", "Diff.", "Rétention")) {
            stats.enrollments.forEach {
                Text("${it.className} | ${it.beginningTotal} | ${it.endTotal} | ${it.difference} | ${it.retentionRate?.let { r -> "%.1f%%".format(r) }.orEmpty()}")
            }
        }
        SectionTable("E. Résultats certificatifs", listOf("Épreuve", "Classe", "Réussites", "Participants", "%")) {
            stats.certifications.forEach {
                Text("${it.examName} | ${it.className} | ${it.successesCount} | ${it.participantsCount} | ${it.successRate?.let { r -> "%.1f%%".format(r) }.orEmpty()}")
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SectionTable(title: String, headers: List<String>, rows: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        Text(headers.joinToString(" | "), style = MaterialTheme.typography.labelMedium)
        rows()
    }
}
