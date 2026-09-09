package com.schoolstats.desktop.screens.statistics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.desktop.components.CountField
import com.schoolstats.desktop.components.KpiCard
import com.schoolstats.desktop.components.PageHeader
import com.schoolstats.desktop.components.SectionCard
import com.schoolstats.desktop.components.TableHeader
import com.schoolstats.desktop.components.formatInt
import com.schoolstats.domain.census.CensusAggregator
import com.schoolstats.domain.model.AdminStaffStat
import com.schoolstats.domain.model.AgeSexStat
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.StudentAges
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.WorkerStat
import com.schoolstats.domain.model.isSecondarySpecific
import com.schoolstats.domain.model.labelFr
import com.schoolstats.presentation.viewmodel.StatisticsViewModel
import org.koin.compose.viewmodel.koinViewModel

private val tabs = listOf(
    "Effectifs inscrits",
    "Âge et sexe",
    "Enseignants",
    "Administratif",
    "Ouvriers",
    "Inscriptions",
    "Certificatives",
)

@Composable
fun StatisticsScreen(
    initialTab: Int = 0,
    viewModel: StatisticsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(initialTab) { viewModel.setTab(initialTab) }

    Column(
        Modifier.padding(4.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PageHeader(
            title = "Saisie des statistiques scolaires",
            subtitle = "Renseignez les effectifs, le personnel et les niveaux d'études. Les totaux se calculent automatiquement. La sous-division agrège ensuite le fichier central.",
        )
        SummaryStrip(state.primary, state.secondary, state.teachers, state.workers, state.adminStaff)
        PrimaryScrollableTabRow(selectedTabIndex = state.tab, edgePadding = 0.dp) {
            tabs.forEachIndexed { i, label ->
                Tab(selected = state.tab == i, onClick = { viewModel.setTab(i) }, text = { Text(label) })
            }
        }
        when (state.tab) {
            0 -> EffectifsTab(state.primary, state.secondary, viewModel::updatePrimary, viewModel::updateSecondary)
            1 -> AgeSexTab(state.ageSex, viewModel::updateAgeSex)
            2 -> TeachersTab(state.teachers, viewModel::updateTeacher)
            3 -> AdminTab(state.adminStaff, viewModel::updateAdmin)
            4 -> WorkersTab(state.workers, viewModel::updateWorker)
            5 -> EnrollmentTab(state.enrollments, viewModel.enrollmentComparison, viewModel::updateEnrollment)
            6 -> CertificationTab(state.certifications)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::saveAll) { Text("Enregistrer brouillon") }
            OutlinedButton(onClick = viewModel::submit) { Text("Soumettre à la sous-division") }
        }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}

@Composable
private fun SummaryStrip(
    primary: List<PrimaryClassStat>,
    secondary: List<SecondaryStudentStat>,
    teachers: List<TeacherStatDetail>,
    workers: List<WorkerStat>,
    admin: List<AdminStaffStat>,
) {
    val boys = primary.sumOf { it.boysCount } + secondary.sumOf { it.boysCount }
    val girls = primary.sumOf { it.girlsCount } + secondary.sumOf { it.girlsCount }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        KpiCard("Élèves inscrits", formatInt(boys + girls), "G ${formatInt(boys)} · F ${formatInt(girls)}", modifier = Modifier.weight(1f))
        KpiCard("Enseignants", formatInt(teachers.sumOf { it.totalCount }), "H ${teachers.sumOf { it.menCount }} · F ${teachers.sumOf { it.womenCount }}", modifier = Modifier.weight(1f))
        KpiCard("Administratif", formatInt(admin.sumOf { it.totalCount }), "Direction, préfet, secrétariat…", modifier = Modifier.weight(1f))
        KpiCard("Ouvriers", formatInt(workers.sumOf { it.totalCount }), "H ${workers.sumOf { it.menCount }} · F ${workers.sumOf { it.womenCount }}", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EffectifsTab(
    primary: List<PrimaryClassStat>,
    secondary: List<SecondaryStudentStat>,
    onPrimary: (Int, Int?, Int?) -> Unit,
    onSecondary: (Int, Int?, Int?) -> Unit,
) {
    SectionCard("Primaire — effectifs inscrits", "Garçons, filles et total par classe") {
        TableHeader(listOf("Classe" to 2f, "Garçons" to 1f, "Filles" to 1f, "Total" to 1f))
        primary.forEachIndexed { index, stat ->
            SexCountRow(stat.className, stat.boysCount, stat.girlsCount, { onPrimary(index, it, null) }, { onPrimary(index, null, it) })
        }
        TotalsRow(primary.sumOf { it.boysCount }, primary.sumOf { it.girlsCount })
    }
    SectionCard("Secondaire — effectifs inscrits", "Par section, option, classe et sexe") {
        TableHeader(listOf("Section / option / classe" to 2.4f, "Garçons" to 1f, "Filles" to 1f, "Total" to 1f))
        secondary.forEachIndexed { index, stat ->
            val label = listOfNotNull(stat.sectionName, stat.optionName, stat.className).joinToString(" · ")
            SexCountRow(label, stat.boysCount, stat.girlsCount, { onSecondary(index, it, null) }, { onSecondary(index, null, it) })
        }
        TotalsRow(secondary.sumOf { it.boysCount }, secondary.sumOf { it.girlsCount })
    }
}

@Composable
private fun AgeSexTab(
    stats: List<AgeSexStat>,
    onUpdate: (String, Int, Int?, Int?) -> Unit,
) {
    val classes = stats.map { it.className }.distinct()
    SectionCard(
        "Élèves par classe, âge et sexe",
        "Saisissez les effectifs pour chaque âge. 19+ regroupe les élèves de 19 ans et plus.",
    ) {
        val scroll = rememberScrollState()
        Column(Modifier.horizontalScroll(scroll), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Classe", Modifier.width(140.dp), fontWeight = FontWeight.SemiBold)
                StudentAges.values.forEach { age ->
                    Text(StudentAges.label(age), Modifier.width(92.dp), fontWeight = FontWeight.SemiBold)
                }
                Text("Total", Modifier.width(72.dp), fontWeight = FontWeight.SemiBold)
            }
            classes.forEach { className ->
                val rows = stats.filter { it.className == className }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(className, Modifier.width(140.dp), fontWeight = FontWeight.Medium)
                    StudentAges.values.forEach { age ->
                        val cell = rows.firstOrNull { it.age == age }
                        Column(Modifier.width(92.dp).padding(end = 4.dp)) {
                            CountField(cell?.boysCount ?: 0, { onUpdate(className, age, it, null) })
                            CountField(cell?.girlsCount ?: 0, { onUpdate(className, age, null, it) })
                        }
                    }
                    val total = rows.sumOf { it.totalCount }
                    Text(total.toString(), Modifier.width(72.dp), fontWeight = FontWeight.SemiBold)
                }
            }
            Text("Ligne du haut = garçons, ligne du bas = filles.", style = MaterialTheme.typography.bodySmall)
        }
        val totals = CensusAggregator.totalsByAge(stats)
        Text(
            "Ensemble : G ${totals.sumOf { it.boysCount }} · F ${totals.sumOf { it.girlsCount }} · T ${totals.sumOf { it.totalCount }}",
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun TeachersTab(
    stats: List<TeacherStatDetail>,
    onUpdate: (Int, Int?, Int?) -> Unit,
) {
    SectionCard("Enseignants — sexe et niveau d'études", "Hommes et femmes par branche et diplôme") {
        TableHeader(listOf("Branche" to 1.6f, "Niveau d'études" to 1.4f, "Hommes" to 1f, "Femmes" to 1f, "Total" to 0.8f))
        stats.forEachIndexed { index, stat ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stat.branch.labelFr(), Modifier.weight(1.6f))
                Text(stat.educationLevel.labelFr(), Modifier.weight(1.4f))
                CountField(stat.menCount, { onUpdate(index, it, null) }, Modifier.weight(1f))
                CountField(stat.womenCount, { onUpdate(index, null, it) }, Modifier.weight(1f))
                Text(stat.totalCount.toString(), Modifier.weight(0.8f), fontWeight = FontWeight.SemiBold)
            }
        }
        TotalsRow(stats.sumOf { it.menCount }, stats.sumOf { it.womenCount }, menLabel = true)
    }
}

@Composable
private fun AdminTab(
    stats: List<AdminStaffStat>,
    onUpdate: (Int, Int?, Int?, EducationLevel?) -> Unit,
) {
    SectionCard(
        "Personnel administratif — niveau d'études",
        "Directeur, adjoint, surnuméraire, et pour le secondaire : préfet, secrétaire, directeur des études, conseiller pédagogique, directeur de discipline, conseiller d'orientation.",
    ) {
        val direction = stats.filter { !it.function.isSecondarySpecific() }
        val secondary = stats.filter { it.function.isSecondarySpecific() }
        AdminGroup("Direction de l'établissement", direction, stats, onUpdate)
        AdminGroup("Encadrement secondaire", secondary, stats, onUpdate)
        TotalsRow(stats.sumOf { it.menCount }, stats.sumOf { it.womenCount }, menLabel = true)
    }
}

@Composable
private fun AdminGroup(
    title: String,
    rows: List<AdminStaffStat>,
    all: List<AdminStaffStat>,
    onUpdate: (Int, Int?, Int?, EducationLevel?) -> Unit,
) {
    Text(title, fontWeight = FontWeight.SemiBold)
    TableHeader(listOf("Fonction" to 1.8f, "Niveau d'études" to 1.4f, "Hommes" to 1f, "Femmes" to 1f, "Total" to 0.8f))
    rows.forEach { stat ->
        val index = all.indexOf(stat)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stat.function.labelFr(), Modifier.weight(1.8f))
            OutlinedButton(
                onClick = {
                    val levels = EducationLevel.entries
                    val next = levels[(levels.indexOf(stat.educationLevel) + 1) % levels.size]
                    onUpdate(index, null, null, next)
                },
                modifier = Modifier.weight(1.4f),
            ) { Text(stat.educationLevel.labelFr()) }
            CountField(stat.menCount, { onUpdate(index, it, null, null) }, Modifier.weight(1f))
            CountField(stat.womenCount, { onUpdate(index, null, it, null) }, Modifier.weight(1f))
            Text(stat.totalCount.toString(), Modifier.weight(0.8f), fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun WorkersTab(
    stats: List<WorkerStat>,
    onUpdate: (Int, Int?, Int?) -> Unit,
) {
    SectionCard("Ouvriers — sexe et niveau d'études", "Personnel ouvrier de l'établissement") {
        TableHeader(listOf("Niveau d'études" to 2f, "Hommes" to 1f, "Femmes" to 1f, "Total" to 1f))
        stats.forEachIndexed { index, stat ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stat.educationLevel.labelFr(), Modifier.weight(2f))
                CountField(stat.menCount, { onUpdate(index, it, null) }, Modifier.weight(1f))
                CountField(stat.womenCount, { onUpdate(index, null, it) }, Modifier.weight(1f))
                Text(stat.totalCount.toString(), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            }
        }
        TotalsRow(stats.sumOf { it.menCount }, stats.sumOf { it.womenCount }, menLabel = true)
    }
}

@Composable
private fun EnrollmentTab(
    stats: List<EnrollmentStat>,
    comparison: List<com.schoolstats.domain.model.EnrollmentComparison>,
    onUpdate: (Int, Int?, Int?) -> Unit,
) {
    SectionCard("Inscriptions début / fin d'année") {
        TableHeader(listOf("Classe" to 1.6f, "Période" to 1.2f, "Garçons" to 1f, "Filles" to 1f, "Total" to 0.8f))
        stats.forEachIndexed { index, stat ->
            SexCountRow(
                "${stat.className} · ${if (stat.isBeginning) "Début" else "Fin"}",
                stat.boysCount,
                stat.girlsCount,
                { onUpdate(index, it, null) },
                { onUpdate(index, null, it) },
            )
        }
    }
    SectionCard("Rétention") {
        TableHeader(listOf("Classe" to 2f, "Début" to 1f, "Fin" to 1f, "Diff." to 1f, "Rétention" to 1f))
        comparison.forEach {
            Row(Modifier.fillMaxWidth()) {
                Text(it.className, Modifier.weight(2f))
                Text(it.beginningTotal.toString(), Modifier.weight(1f))
                Text(it.endTotal.toString(), Modifier.weight(1f))
                Text(it.difference.toString(), Modifier.weight(1f))
                Text(it.retentionRate?.let { r -> "%.1f%%".format(r) }.orEmpty(), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CertificationTab(stats: List<com.schoolstats.domain.model.CertificationResult>) {
    SectionCard("Épreuves certificatives") {
        stats.forEach {
            Text(
                "${it.examName} ${it.className}: ${it.successesCount}/${it.participantsCount} (${it.successRate?.let { r -> "%.1f%%".format(r) }.orEmpty()})",
            )
        }
    }
}

@Composable
private fun SexCountRow(
    label: String,
    boys: Int,
    girls: Int,
    onBoys: (Int) -> Unit,
    onGirls: (Int) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, Modifier.weight(2f))
        CountField(boys, onBoys, Modifier.weight(1f))
        CountField(girls, onGirls, Modifier.weight(1f))
        Text((boys + girls).toString(), Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun TotalsRow(first: Int, second: Int, menLabel: Boolean = false) {
    val left = if (menLabel) "H" else "G"
    val right = if (menLabel) "F" else "F"
    Text(
        "Total  $left=${formatInt(first)}  $right=${formatInt(second)}  T=${formatInt(first + second)}",
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
}
