package com.schoolstats.desktop.screens.centralization

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.desktop.components.ChartPalette
import com.schoolstats.desktop.components.KpiCard
import com.schoolstats.desktop.components.PageHeader
import com.schoolstats.desktop.components.SectionCard
import com.schoolstats.desktop.components.TableHeader
import com.schoolstats.desktop.components.charts.ChartDatum
import com.schoolstats.desktop.components.charts.DonutChartCard
import com.schoolstats.desktop.components.charts.GroupedBarChartCard
import com.schoolstats.desktop.components.charts.HorizontalBarChartCard
import com.schoolstats.desktop.components.charts.StackedSexBarCard
import com.schoolstats.desktop.components.formatInt
import com.schoolstats.domain.census.CensusAggregator
import com.schoolstats.domain.model.SubmissionStatus
import com.schoolstats.domain.model.labelFr
import com.schoolstats.presentation.viewmodel.CentralizationViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CentralizationScreen(viewModel: CentralizationViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val stats = state.stats
    val ageTotals = CensusAggregator.totalsByAge(stats.byAge)
    val teacherLevels = CensusAggregator.teachersByEducation(stats.teachers)

    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageHeader(
            title = "Fichier central — Sous-division ${state.subdivisionName}",
            subtitle = "Agrégation automatique des déclarations scolaires pour l'année ${state.schoolYear}. Seules les données soumises, en vérification ou validées alimentent ce fichier.",
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Élèves", formatInt(stats.totalStudents), "G ${formatInt(stats.totalBoys)} · F ${formatInt(stats.totalGirls)}", ChartPalette.boys, Modifier.weight(1f))
            KpiCard("Enseignants", formatInt(stats.totalTeachers), "H ${formatInt(stats.totalTeacherMen)} · F ${formatInt(stats.totalTeacherWomen)}", ChartPalette.teal, Modifier.weight(1f))
            KpiCard("Administratif", formatInt(stats.totalAdminStaff), "H ${formatInt(stats.totalAdminMen)} · F ${formatInt(stats.totalAdminWomen)}", ChartPalette.women, Modifier.weight(1f))
            KpiCard("Ouvriers", formatInt(stats.totalWorkers), "H ${formatInt(stats.totalWorkerMen)} · F ${formatInt(stats.totalWorkerWomen)}", ChartPalette.gold, Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DonutChartCard(
                title = "Répartition des élèves",
                centerLabel = formatInt(stats.totalStudents),
                data = listOf(
                    ChartDatum("Garçons", stats.totalBoys.toFloat(), ChartPalette.boys),
                    ChartDatum("Filles", stats.totalGirls.toFloat(), ChartPalette.girls),
                ),
                modifier = Modifier.weight(1f),
            )
            DonutChartCard(
                title = "Personnel enseignant",
                centerLabel = formatInt(stats.totalTeachers),
                data = listOf(
                    ChartDatum("Hommes", stats.totalTeacherMen.toFloat(), ChartPalette.men),
                    ChartDatum("Femmes", stats.totalTeacherWomen.toFloat(), ChartPalette.women),
                ),
                modifier = Modifier.weight(1f),
            )
        }
        GroupedBarChartCard(
            title = "Effectifs par classe (primaire)",
            subtitle = "Garçons et filles agrégés au niveau de la sous-division",
            data = stats.byClass.map {
                ChartDatum(it.className.replace(" année", ""), it.boysCount.toFloat(), secondary = it.girlsCount.toFloat())
            },
        )
        GroupedBarChartCard(
            title = "Élèves par âge",
            data = ageTotals.map {
                ChartDatum(it.ageLabel, it.boysCount.toFloat(), secondary = it.girlsCount.toFloat())
            },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            HorizontalBarChartCard(
                title = "Niveau d'études — enseignants",
                data = teacherLevels.mapIndexed { i, row ->
                    ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                },
                modifier = Modifier.weight(1f),
            )
            HorizontalBarChartCard(
                title = "Niveau d'études — ouvriers",
                data = stats.workers.filter { it.totalCount > 0 }.mapIndexed { i, row ->
                    ChartDatum(row.educationLevel.labelFr(), row.totalCount.toFloat(), ChartPalette.series[i % ChartPalette.series.size])
                },
                modifier = Modifier.weight(1f),
            )
        }
        StackedSexBarCard(
            title = "Personnel administratif par fonction",
            data = stats.adminStaff.filter { it.totalCount > 0 }.map {
                ChartDatum(it.function.labelFr(), it.menCount.toFloat(), secondary = it.womenCount.toFloat())
            },
        )

        SectionCard("Écoles contributrices") {
            if (state.submissions.isEmpty()) {
                Text("Aucune déclaration.")
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("${stats.contributingSchools} dossiers agrégés") })
                    AssistChip(onClick = {}, label = { Text("${state.submissions.count { it.status == SubmissionStatus.VALIDE }} validés") })
                }
                state.submissions.forEach { submission ->
                    Text(
                        "${submission.schoolName ?: submission.schoolId} — ${submission.status.name}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        SectionCard("A. Effectifs inscrits par classe") {
            TableHeader(listOf("Classe" to 2f, "Garçons" to 1f, "Filles" to 1f, "Total" to 1f))
            stats.byClass.forEach {
                Row(Modifier.fillMaxWidth()) {
                    Text(it.className, Modifier.weight(2f))
                    Text(it.boysCount.toString(), Modifier.weight(1f))
                    Text(it.girlsCount.toString(), Modifier.weight(1f))
                    Text(it.totalCount.toString(), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        SectionCard("B. Effectifs secondaires") {
            TableHeader(listOf("Section" to 1.4f, "Option" to 1.2f, "Classe" to 1f, "G" to 0.6f, "F" to 0.6f, "T" to 0.6f))
            stats.bySection.forEach {
                Row(Modifier.fillMaxWidth()) {
                    Text(it.sectionName, Modifier.weight(1.4f))
                    Text(it.optionName.orEmpty(), Modifier.weight(1.2f))
                    Text(it.className, Modifier.weight(1f))
                    Text(it.boysCount.toString(), Modifier.weight(0.6f))
                    Text(it.girlsCount.toString(), Modifier.weight(0.6f))
                    Text(it.totalCount.toString(), Modifier.weight(0.6f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        SectionCard("C. Enseignants par niveau d'études") {
            TableHeader(listOf("Branche" to 1.6f, "Niveau" to 1.2f, "H" to 0.7f, "F" to 0.7f, "T" to 0.7f))
            stats.teachers.filter { it.totalCount > 0 }.forEach {
                Row(Modifier.fillMaxWidth()) {
                    Text(it.branch.labelFr(), Modifier.weight(1.6f))
                    Text(it.educationLevel.labelFr(), Modifier.weight(1.2f))
                    Text(it.menCount.toString(), Modifier.weight(0.7f))
                    Text(it.womenCount.toString(), Modifier.weight(0.7f))
                    Text(it.totalCount.toString(), Modifier.weight(0.7f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        SectionCard("D. Personnel administratif") {
            TableHeader(listOf("Fonction" to 1.8f, "Niveau" to 1.2f, "H" to 0.7f, "F" to 0.7f, "T" to 0.7f))
            stats.adminStaff.filter { it.totalCount > 0 }.forEach {
                Row(Modifier.fillMaxWidth()) {
                    Text(it.function.labelFr(), Modifier.weight(1.8f))
                    Text(it.educationLevel.labelFr(), Modifier.weight(1.2f))
                    Text(it.menCount.toString(), Modifier.weight(0.7f))
                    Text(it.womenCount.toString(), Modifier.weight(0.7f))
                    Text(it.totalCount.toString(), Modifier.weight(0.7f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        SectionCard("E. Ouvriers par niveau d'études") {
            TableHeader(listOf("Niveau" to 2f, "Hommes" to 1f, "Femmes" to 1f, "Total" to 1f))
            stats.workers.filter { it.totalCount > 0 }.forEach {
                Row(Modifier.fillMaxWidth()) {
                    Text(it.educationLevel.labelFr(), Modifier.weight(2f))
                    Text(it.menCount.toString(), Modifier.weight(1f))
                    Text(it.womenCount.toString(), Modifier.weight(1f))
                    Text(it.totalCount.toString(), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
        SectionCard("F. Élèves par âge et sexe") {
            TableHeader(listOf("Classe" to 1.6f, "Âge" to 0.8f, "Garçons" to 1f, "Filles" to 1f, "Total" to 1f))
            stats.byAge.filter { it.totalCount > 0 }.forEach {
                Row(Modifier.fillMaxWidth()) {
                    Text(it.className, Modifier.weight(1.6f))
                    Text(it.ageLabel, Modifier.weight(0.8f))
                    Text(it.boysCount.toString(), Modifier.weight(1f))
                    Text(it.girlsCount.toString(), Modifier.weight(1f))
                    Text(it.totalCount.toString(), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
