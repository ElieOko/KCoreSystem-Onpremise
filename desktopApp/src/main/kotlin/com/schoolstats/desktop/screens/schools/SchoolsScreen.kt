package com.schoolstats.desktop.screens.schools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.domain.model.School
import com.schoolstats.presentation.viewmodel.SchoolsViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SchoolsScreen(
    viewModel: SchoolsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Gestion des écoles", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Button(onClick = viewModel::openCreateForm) { Text("Ajouter une école") }
        }
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearch,
            label = { Text("Rechercher par nom ou code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.schools) { school ->
                    SchoolRow(school = school, onEdit = { viewModel.openEditForm(school) })
                }
            }
        }
    }

    uiState.editingSchool?.let { school ->
        SchoolFormDialog(
            school = school,
            onDismiss = viewModel::closeForm,
            onSave = viewModel::saveSchool,
        )
    }
}

@Composable
private fun SchoolRow(school: School, onEdit: () -> Unit) {
    Surface(shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(school.name, fontWeight = FontWeight.SemiBold)
                Text("Code: ${school.schoolCode} | Type: ${school.schoolType.name}")
                school.subdivisionName?.let { Text("Sous-Division: $it", style = MaterialTheme.typography.bodySmall) }
                school.submissionStatus?.let { Text("Déclaration: ${it.name}", style = MaterialTheme.typography.bodySmall) }
            }
            TextButton(onClick = onEdit) { Text("Modifier") }
        }
    }
}

@Composable
private fun SchoolFormDialog(
    school: School,
    onDismiss: () -> Unit,
    onSave: (School) -> Unit,
) {
    var name by remember(school.id) { mutableStateOf(school.name) }
    var code by remember(school.id) { mutableStateOf(school.schoolCode) }
    var subdivisionId by remember(school.id) { mutableStateOf(school.subdivisionId) }
    var city by remember(school.id) { mutableStateOf(school.city.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (school.id.isBlank()) "Nouvelle école" else "Modifier l'école") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(code, { code = it }, label = { Text("Code école") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(subdivisionId, { subdivisionId = it }, label = { Text("ID Sous-Division") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(city, { city = it }, label = { Text("Ville") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    school.copy(
                        name = name,
                        schoolCode = code,
                        subdivisionId = subdivisionId,
                        city = city.ifBlank { null },
                    ),
                )
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
