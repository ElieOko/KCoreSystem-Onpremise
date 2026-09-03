package com.schoolstats.desktop.screens.validation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun ValidationScreen(viewModel: ValidationViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Déclarations à valider", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.submissions) { submission ->
                    SubmissionItem(submission, selected = submission.id == state.selected?.id, onClick = { viewModel.select(submission) })
                }
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
