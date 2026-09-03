package com.schoolstats.desktop.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.domain.model.SubmissionStatus
import com.schoolstats.presentation.viewmodel.SubmissionsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SubmissionsScreen(viewModel: SubmissionsViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsState()
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Déclarations reçues", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.statusFilter == null, onClick = { viewModel.filterByStatus(null) }, label = { Text("Toutes") })
            SubmissionStatus.entries.forEach { status ->
                FilterChip(
                    selected = state.statusFilter == status.name,
                    onClick = { viewModel.filterByStatus(status.name) },
                    label = { Text(status.name) },
                )
            }
        }
        if (state.submissions.isEmpty()) {
            Text("Aucune déclaration.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.submissions) { submission ->
                    Surface(shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(submission.schoolName ?: submission.schoolId, fontWeight = FontWeight.SemiBold)
                            Text("Statut: ${submission.status.name}")
                            submission.schoolCode?.let { Text("Code: $it") }
                            submission.submittedAt?.let { Text("Soumis: $it", style = MaterialTheme.typography.bodySmall) }
                            submission.rejectionReason?.let {
                                Text("Rejet: $it", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
