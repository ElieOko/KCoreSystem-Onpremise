package com.schoolstats.desktop.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.presentation.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isLoading) {
        CircularProgressIndicator()
        return
    }
  DashboardContent(stats = uiState.stats)
}

@Composable
fun DashboardContent(stats: DashboardStats) {
    val cards = listOf(
        "Total écoles" to stats.totalSchools,
        "Données soumises" to stats.schoolsSubmitted,
        "Données validées" to stats.schoolsValidated,
        "En attente" to stats.schoolsPending,
        "Données rejetées" to stats.schoolsRejected,
        "Écoles non soumises" to stats.schoolsNotSubmitted,
        "Total élèves" to stats.totalStudents,
        "Total enseignants" to stats.totalTeachers,
        "Garçons" to stats.totalBoys,
        "Filles" to stats.totalGirls,
    )
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tableau de bord", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(220.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(cards) { (label, value) ->
                StatCard(label = label, value = value)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: Int) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.7f))
            Text(value.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        }
    }
}
