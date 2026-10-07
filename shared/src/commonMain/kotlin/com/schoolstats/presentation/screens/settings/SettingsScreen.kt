package com.schoolstats.presentation.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.domain.education.EducationCycle
import com.schoolstats.domain.education.RdcEducationSystem
import com.schoolstats.domain.education.descriptionFr
import com.schoolstats.domain.education.labelFr as cycleLabelFr
import com.schoolstats.domain.model.SchoolType
import com.schoolstats.domain.model.labelFr as schoolTypeLabelFr
import com.schoolstats.presentation.components.SectionCard
import com.schoolstats.presentation.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreen(
    databaseHint: String = "~/.kcoresystem/schoolstats.db",
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    Column(
        Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Paramètres", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Mode sombre")
            Switch(checked = state.darkTheme, onCheckedChange = { viewModel.toggleTheme() })
        }
        Text("Base locale: $databaseHint", style = MaterialTheme.typography.bodySmall)
        Text("Configurez Supabase via local.properties", style = MaterialTheme.typography.bodySmall)
        Button(onClick = viewModel::toggleTheme) { Text("Basculer le thème") }

        SectionCard(
            "Système éducatif officiel — ${RdcEducationSystem.COUNTRY}",
            "${RdcEducationSystem.AUTHORITY}. ${RdcEducationSystem.LEGAL_FRAME}.",
        ) {
            EducationCycle.entries.forEach { cycle ->
                val classes = when (cycle) {
                    EducationCycle.MATERNELLE -> RdcEducationSystem.preschoolClasses
                    EducationCycle.PRIMAIRE -> RdcEducationSystem.primaryClasses
                    EducationCycle.TRONC_COMMUN -> RdcEducationSystem.troncCommunClasses
                    EducationCycle.HUMANITES -> RdcEducationSystem.humanitesClasses
                }
                Text(cycle.cycleLabelFr(), fontWeight = FontWeight.SemiBold)
                Text(cycle.descriptionFr(), style = MaterialTheme.typography.bodySmall)
                Text(classes.joinToString(" · ") { it.name }, style = MaterialTheme.typography.bodyMedium)
            }
        }

        SectionCard("Types d'établissements officiels", "Correspondance EPST pour le recensement") {
            SchoolType.entries.forEach { type ->
                Text("• ${type.schoolTypeLabelFr()}")
            }
        }

        SectionCard("Sections et options des humanités", "Référentiel officiel utilisé à la saisie") {
            RdcEducationSystem.sections.forEach { section ->
                val options = RdcEducationSystem.options.filter { it.sectionCode == section.code }
                Text(section.name, fontWeight = FontWeight.SemiBold)
                if (options.isEmpty()) {
                    Text("Sans option (tronc commun)", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(options.joinToString(" · ") { it.name }, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        SectionCard("Épreuves certificatives officielles") {
            RdcEducationSystem.exams.forEach { exam ->
                Text("${exam.name} — ${exam.className}", fontWeight = FontWeight.Medium)
                Text(exam.description, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
