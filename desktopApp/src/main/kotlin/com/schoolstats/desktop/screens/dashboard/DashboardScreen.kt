package com.schoolstats.desktop.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.schoolstats.desktop.components.ChartPalette
import com.schoolstats.desktop.components.KpiCard
import com.schoolstats.desktop.components.PageHeader
import com.schoolstats.desktop.components.charts.ChartDatum
import com.schoolstats.desktop.components.charts.DonutChartCard
import com.schoolstats.desktop.components.charts.GroupedBarChartCard
import com.schoolstats.desktop.components.charts.HorizontalBarChartCard
import com.schoolstats.desktop.components.formatInt
import com.schoolstats.domain.census.CensusAggregator
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.labelFr
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
    val census = stats.census
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageHeader(
            "Tableau de bord",
            "Vue d'ensemble de la sous-division — effectifs, personnel et avancement des déclarations.",
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Écoles", formatInt(stats.totalSchools), "${stats.schoolsValidated} validées", ChartPalette.navy, Modifier.weight(1f))
            KpiCard("Élèves", formatInt(stats.totalStudents), "G ${formatInt(stats.totalBoys)} · F ${formatInt(stats.totalGirls)}", ChartPalette.boys, Modifier.weight(1f))
            KpiCard("Enseignants", formatInt(stats.totalTeachers), "H ${formatInt(stats.teacherMen)} · F ${formatInt(stats.teacherWomen)}", ChartPalette.teal, Modifier.weight(1f))
            KpiCard("Ouvriers", formatInt(stats.totalWorkers), "H ${formatInt(stats.workerMen)} · F ${formatInt(stats.workerWomen)}", ChartPalette.gold, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Soumises", formatInt(stats.schoolsSubmitted), modifier = Modifier.weight(1f), accent = ChartPalette.teal)
            KpiCard("En attente", formatInt(stats.schoolsPending), modifier = Modifier.weight(1f), accent = ChartPalette.orange)
            KpiCard("Rejetées", formatInt(stats.schoolsRejected), modifier = Modifier.weight(1f), accent = ChartPalette.girls)
            KpiCard("Non soumises", formatInt(stats.schoolsNotSubmitted), modifier = Modifier.weight(1f), accent = ChartPalette.slate)
            KpiCard("Administratif", formatInt(stats.totalAdminStaff), modifier = Modifier.weight(1f), accent = ChartPalette.women)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DonutChartCard(
                title = "Élèves inscrits",
                centerLabel = formatInt(stats.totalStudents),
                data = listOf(
                    ChartDatum("Garçons", stats.totalBoys.toFloat(), ChartPalette.boys),
                    ChartDatum("Filles", stats.totalGirls.toFloat(), ChartPalette.girls),
                ),
                modifier = Modifier.weight(1f),
            )
            DonutChartCard(
                title = "Suivi des déclarations",
                centerLabel = formatInt(stats.totalSchools),
                data = listOf(
                    ChartDatum("Validées", stats.schoolsValidated.toFloat(), ChartPalette.teal),
                    ChartDatum("En attente", stats.schoolsPending.toFloat(), ChartPalette.gold),
                    ChartDatum("Rejetées", stats.schoolsRejected.toFloat(), ChartPalette.girls),
                    ChartDatum("Non soumises", stats.schoolsNotSubmitted.toFloat(), ChartPalette.slate),
                ),
                modifier = Modifier.weight(1f),
            )
        }
        GroupedBarChartCard(
            title = "Effectifs par classe",
            data = census.byClass.map {
                ChartDatum(it.className.replace(" année", ""), it.boysCount.toFloat(), secondary = it.girlsCount.toFloat())
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalBarChartCard(
                title = "Niveaux d'études enseignants",
                data = CensusAggregator.teachersByEducation(census.teachers).mapIndexed { i, row ->
                    ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                },
                modifier = Modifier.weight(1f),
            )
            HorizontalBarChartCard(
                title = "Niveaux d'études ouvriers",
                data = census.workers.filter { it.totalCount > 0 }.mapIndexed { i, row ->
                    ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
