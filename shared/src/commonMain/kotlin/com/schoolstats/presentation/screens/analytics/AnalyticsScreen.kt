package com.schoolstats.presentation.screens.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.schoolstats.presentation.components.AdaptiveChartStack
import com.schoolstats.presentation.components.ChartPalette
import com.schoolstats.presentation.components.PageHeader
import com.schoolstats.presentation.components.charts.ChartDatum
import com.schoolstats.presentation.components.charts.DonutChartCard
import com.schoolstats.presentation.components.charts.GroupedBarChartCard
import com.schoolstats.presentation.components.charts.HorizontalBarChartCard
import com.schoolstats.presentation.components.charts.StackedSexBarCard
import com.schoolstats.presentation.components.formatInt
import com.schoolstats.domain.census.CensusAggregator
import com.schoolstats.domain.model.labelFr
import com.schoolstats.presentation.viewmodel.DashboardViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AnalyticsScreen(
    compact: Boolean = false,
    viewModel: DashboardViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val stats = state.stats
    val census = stats.census
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageHeader(
            "Analyses",
            "Graphiques du fichier central : effectifs, âges, enseignants, administratif et ouvriers.",
        )
        AdaptiveChartStack(
            compact,
            { mod ->
                DonutChartCard(
                    title = "Garçons / Filles",
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
                    title = "Personnel",
                    centerLabel = formatInt(stats.totalTeachers + stats.totalWorkers + stats.totalAdminStaff),
                    data = listOf(
                        ChartDatum("Enseignants", stats.totalTeachers.toFloat(), ChartPalette.teal),
                        ChartDatum("Administratif", stats.totalAdminStaff.toFloat(), ChartPalette.women),
                        ChartDatum("Ouvriers", stats.totalWorkers.toFloat(), ChartPalette.gold),
                    ),
                    modifier = mod,
                    stacked = compact,
                )
            },
        )
        GroupedBarChartCard(
            title = "Élèves par classe",
            data = census.byClass.map {
                ChartDatum(it.className.replace(" année", ""), it.boysCount.toFloat(), secondary = it.girlsCount.toFloat())
            },
        )
        GroupedBarChartCard(
            title = "Élèves par âge",
            data = CensusAggregator.totalsByAge(census.byAge).map {
                ChartDatum(it.ageLabel, it.boysCount.toFloat(), secondary = it.girlsCount.toFloat())
            },
        )
        StackedSexBarCard(
            title = "Administratif par fonction",
            data = census.adminStaff.filter { it.totalCount > 0 }.map {
                ChartDatum(it.function.labelFr(), it.menCount.toFloat(), secondary = it.womenCount.toFloat())
            },
        )
        AdaptiveChartStack(
            compact,
            { mod ->
                HorizontalBarChartCard(
                    title = "Enseignants — niveau d'études",
                    data = CensusAggregator.teachersByEducation(census.teachers).mapIndexed { i, row ->
                        ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                    },
                    modifier = mod,
                )
            },
            { mod ->
                HorizontalBarChartCard(
                    title = "Ouvriers — niveau d'études",
                    data = census.workers.filter { it.totalCount > 0 }.mapIndexed { i, row ->
                        ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                    },
                    modifier = mod,
                )
            },
        )
    }
}
