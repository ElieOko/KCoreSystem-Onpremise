package com.schoolstats.presentation.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.schoolstats.domain.census.CensusAggregator
import com.schoolstats.domain.model.DashboardStats
import com.schoolstats.domain.model.labelFr
import com.schoolstats.presentation.components.AdaptiveChartStack
import com.schoolstats.presentation.components.AdaptiveKpiGrid
import com.schoolstats.presentation.components.ChartPalette
import com.schoolstats.presentation.components.KpiCard
import com.schoolstats.presentation.components.PageHeader
import com.schoolstats.presentation.components.charts.ChartDatum
import com.schoolstats.presentation.components.charts.DonutChartCard
import com.schoolstats.presentation.components.charts.GroupedBarChartCard
import com.schoolstats.presentation.components.charts.HorizontalBarChartCard
import com.schoolstats.presentation.components.formatInt
import com.schoolstats.presentation.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun DashboardScreen(
    compact: Boolean = false,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    if (uiState.isLoading) {
        CircularProgressIndicator()
        return
    }
    DashboardContent(stats = uiState.stats, compact = compact)
}

@Composable
fun DashboardContent(stats: DashboardStats, compact: Boolean = false) {
    val census = stats.census
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageHeader(
            "Tableau de bord",
            "Vue d'ensemble de la sous-division — effectifs, personnel et avancement des déclarations.",
        )
        AdaptiveKpiGrid(
            compact,
            { KpiCard("Écoles", formatInt(stats.totalSchools), "${stats.schoolsValidated} validées", ChartPalette.navy, it) },
            { KpiCard("Élèves", formatInt(stats.totalStudents), "G ${formatInt(stats.totalBoys)} · F ${formatInt(stats.totalGirls)}", ChartPalette.boys, it) },
            { KpiCard("Enseignants", formatInt(stats.totalTeachers), "H ${formatInt(stats.teacherMen)} · F ${formatInt(stats.teacherWomen)}", ChartPalette.teal, it) },
            { KpiCard("Ouvriers", formatInt(stats.totalWorkers), "H ${formatInt(stats.workerMen)} · F ${formatInt(stats.workerWomen)}", ChartPalette.gold, it) },
        )
        AdaptiveKpiGrid(
            compact,
            { KpiCard("Soumises", formatInt(stats.schoolsSubmitted), accent = ChartPalette.teal, modifier = it) },
            { KpiCard("En attente", formatInt(stats.schoolsPending), accent = ChartPalette.orange, modifier = it) },
            { KpiCard("Rejetées", formatInt(stats.schoolsRejected), accent = ChartPalette.girls, modifier = it) },
            { KpiCard("Non soumises", formatInt(stats.schoolsNotSubmitted), accent = ChartPalette.slate, modifier = it) },
            { KpiCard("Administratif", formatInt(stats.totalAdminStaff), accent = ChartPalette.women, modifier = it) },
        )
        AdaptiveChartStack(
            compact,
            { mod ->
                DonutChartCard(
                    title = "Élèves inscrits",
                    centerLabel = formatInt(stats.totalStudents),
                    data = listOf(
                        ChartDatum("Garçons", stats.totalBoys.toFloat(), ChartPalette.boys),
                        ChartDatum("Filles", stats.totalGirls.toFloat(), ChartPalette.girls),
                    ),
                    modifier = mod,
                    stacked = compact,
                )
            },
            { mod ->
                DonutChartCard(
                    title = "Suivi des déclarations",
                    centerLabel = formatInt(stats.totalSchools),
                    data = listOf(
                        ChartDatum("Validées", stats.schoolsValidated.toFloat(), ChartPalette.teal),
                        ChartDatum("En attente", stats.schoolsPending.toFloat(), ChartPalette.gold),
                        ChartDatum("Rejetées", stats.schoolsRejected.toFloat(), ChartPalette.girls),
                        ChartDatum("Non soumises", stats.schoolsNotSubmitted.toFloat(), ChartPalette.slate),
                    ),
                    modifier = mod,
                    stacked = compact,
                )
            },
        )
        GroupedBarChartCard(
            title = "Effectifs par classe",
            data = census.byClass.map {
                ChartDatum(it.className.replace(" année", ""), it.boysCount.toFloat(), secondary = it.girlsCount.toFloat())
            },
        )
        AdaptiveChartStack(
            compact,
            { mod ->
                HorizontalBarChartCard(
                    title = "Niveaux d'études enseignants",
                    data = CensusAggregator.teachersByEducation(census.teachers).mapIndexed { i, row ->
                        ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                    },
                    modifier = mod,
                )
            },
            { mod ->
                HorizontalBarChartCard(
                    title = "Niveaux d'études ouvriers",
                    data = census.workers.filter { it.totalCount > 0 }.mapIndexed { i, row ->
                        ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                    },
                    modifier = mod,
                )
            },
        )
    }
}
