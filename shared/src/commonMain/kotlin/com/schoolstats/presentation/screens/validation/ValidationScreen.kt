package com.schoolstats.presentation.screens.validation

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.domain.model.Submission
import com.schoolstats.presentation.viewmodel.ValidationViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ValidationScreen(
    compact: Boolean = false,
    viewModel: ValidationViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val list: @Composable () -> Unit = {
        Text("Déclarations à valider", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        state.submissions.forEach { submission ->
            SubmissionItem(submission, selected = submission.id == state.selected?.id, onClick = { viewModel.select(submission) })
        }
    }
    val actions: @Composable () -> Unit = {
        Text("Action administrative", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        state.selected?.let { selected ->
            Text("${selected.schoolName} (${selected.schoolCode})")
            Text("Statut: ${selected.status.name}")
            OutlinedTextField(
                value = state.comment,
                onValueChange = viewModel::setComment,
                label = { Text("Commentaire / motif") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = viewModel::validate) { Text("Valider") }
                Button(onClick = viewModel::reject) { Text("Rejeter") }
                Button(onClick = viewModel::requestCorrection) { Text("Correction") }
            }
        } ?: Text("Sélectionnez une déclaration.")
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
    if (compact) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            list()
            actions()
        }
    } else {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { list() }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) { actions() }
        }
    }
}

@Composable
private fun SubmissionItem(submission: Submission, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shadowElevation = if (selected) 4.dp else 1.dp,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(submission.schoolName ?: submission.schoolId, fontWeight = FontWeight.SemiBold)
            Text(submission.status.name, style = MaterialTheme.typography.bodySmall)
        }
    }
}
