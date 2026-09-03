package com.schoolstats.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.presentation.viewmodel.PrimaryStatsViewModel
import com.schoolstats.presentation.viewmodel.SubmissionsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlaceholderScreen(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
        Text("Module en cours d'implémentation.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SubmissionsScreen(
    viewModel: SubmissionsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Déclarations reçues", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        if (uiState.submissions.isEmpty()) {
            Text("Aucune déclaration pour le moment.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.submissions) { submission ->
                    Surface(shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(submission.schoolName ?: submission.schoolId, fontWeight = FontWeight.SemiBold)
                            Text("Statut: ${submission.status.name}")
                            submission.schoolCode?.let { Text("Code: $it") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrimaryStatisticsScreen(
    viewModel: PrimaryStatsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Statistiques primaire", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        uiState.stats.forEachIndexed { index, stat ->
            Surface(shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) {
                    Text(stat.className, fontWeight = FontWeight.SemiBold)
                    Text("Garçons: ${stat.boysCount} | Filles: ${stat.girlsCount} | Total: ${stat.totalCount}")
                }
            }
        }
        Text("Total garçons: ${viewModel.totalBoys} | Total filles: ${viewModel.totalGirls} | Total: ${viewModel.totalStudents}")
        if (uiState.saved) Text("Données sauvegardées localement.", color = MaterialTheme.colorScheme.primary)
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
