package com.schoolstats.desktop.screens.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.presentation.viewmodel.StatisticsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.padding(8.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Statistiques scolaires", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        ScrollableTabRow(selectedTabIndex = state.tab) {
            listOf("Primaire", "Secondaire", "Enseignants", "Inscriptions", "Certificatives").forEachIndexed { i, label ->
                Tab(selected = state.tab == i, onClick = { viewModel.setTab(i) }, text = { Text(label) })
            }
        }
        when (state.tab) {
            0 -> PrimaryTab(state.primary, viewModel::updatePrimary)
            1 -> SecondaryTab(state.secondary)
            2 -> TeachersTab(state.teachers)
            3 -> EnrollmentTab(viewModel.enrollmentComparison)
            4 -> CertificationTab(state.certifications)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::saveAll) { Text("Enregistrer brouillon") }
            Button(onClick = viewModel::submit) { Text("Soumettre") }
        }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun PrimaryTab(stats: List<com.schoolstats.domain.model.PrimaryClassStat>, onUpdate: (Int, Int?, Int?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Text("Classe", Modifier.weight(2f), fontWeight = FontWeight.SemiBold)
            Text("Garçons", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Text("Filles", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Text("Total", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        }
        stats.forEachIndexed { index, stat ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stat.className, Modifier.weight(2f))
                OutlinedTextField(
                    value = stat.boysCount.toString(),
                    onValueChange = { onUpdate(index, it.toIntOrNull() ?: 0, null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                OutlinedTextField(
                    value = stat.girlsCount.toString(),
                    onValueChange = { onUpdate(index, null, it.toIntOrNull() ?: 0) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                )
                Text(stat.totalCount.toString(), Modifier.weight(1f).padding(top = 12.dp))
            }
        }
        Text("Total: G=${stats.sumOf { it.boysCount }} F=${stats.sumOf { it.girlsCount }} T=${stats.sumOf { it.totalCount }}")
    }
}

@Composable
private fun SecondaryTab(stats: List<com.schoolstats.domain.model.SecondaryStudentStat>) {
    stats.forEach {
        Text("${it.sectionName} | ${it.optionName.orEmpty()} | ${it.className}: G=${it.boysCount} F=${it.girlsCount} T=${it.totalCount}")
    }
}

@Composable
private fun TeachersTab(stats: List<com.schoolstats.domain.model.TeacherStatDetail>) {
    stats.forEach {
        Text("${it.branch.name} / ${it.educationLevel.name}: H=${it.menCount} F=${it.womenCount} T=${it.totalCount}")
    }
}

@Composable
private fun EnrollmentTab(stats: List<com.schoolstats.domain.model.EnrollmentComparison>) {
    Row(Modifier.fillMaxWidth()) {
        Text("Classe", Modifier.weight(2f), fontWeight = FontWeight.SemiBold)
        Text("Début", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text("Fin", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text("Diff.", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        Text("Rétention", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
    }
    stats.forEach {
        Row(Modifier.fillMaxWidth()) {
            Text(it.className, Modifier.weight(2f))
            Text(it.beginningTotal.toString(), Modifier.weight(1f))
            Text(it.endTotal.toString(), Modifier.weight(1f))
            Text(it.difference.toString(), Modifier.weight(1f))
            Text(it.retentionRate?.let { r -> "%.1f%%".format(r) }.orEmpty(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun CertificationTab(stats: List<com.schoolstats.domain.model.CertificationResult>) {
    stats.forEach {
        Text("${it.examName} ${it.className}: ${it.successesCount}/${it.participantsCount} (${it.successRate?.let { r -> "%.1f%%".format(r) }.orEmpty()})")
    }
}
