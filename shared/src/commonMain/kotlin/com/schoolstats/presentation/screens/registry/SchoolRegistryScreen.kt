package com.schoolstats.presentation.screens.registry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.schoolstats.domain.census.CensusDefaults
import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.Gender
import com.schoolstats.domain.model.StudentRecord
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.WorkerCategory
import com.schoolstats.domain.model.WorkerRecord
import com.schoolstats.domain.model.labelFr
import com.schoolstats.domain.model.labelStudentFr
import com.schoolstats.domain.model.labelWorkerFr
import com.schoolstats.presentation.components.AdaptiveKpiGrid
import com.schoolstats.presentation.components.KpiCard
import com.schoolstats.presentation.components.PageHeader
import com.schoolstats.presentation.components.formatInt
import com.schoolstats.presentation.viewmodel.SchoolRegistryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SchoolRegistryScreen(
    initialTab: Int = 0,
    compact: Boolean = false,
    viewModel: SchoolRegistryViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(initialTab) { viewModel.setTab(initialTab) }

    Column(Modifier.fillMaxSize().padding(4.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PageHeader(
            title = if (state.isSchoolAccount) "Registre de l'école" else "Registre élèves et travailleurs",
            subtitle = if (state.schoolName.isBlank()) {
                "Enregistrez les élèves et les travailleurs de l'établissement."
            } else {
                "${state.schoolName} — ajoutez, modifiez ou retirez les élèves et les travailleurs."
            },
        )
        if (!state.isSchoolAccount && state.schools.size > 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("École", fontWeight = FontWeight.Medium)
                state.schools.take(6).forEach { school ->
                    FilterChip(
                        selected = school.id == state.selectedSchoolId,
                        onClick = { viewModel.selectSchool(school.id) },
                        label = { Text(school.name) },
                    )
                }
            }
        }
        AdaptiveKpiGrid(
            compact,
            { KpiCard("Élèves", formatInt(state.summary.studentCount), "G ${formatInt(state.summary.studentBoys)} · F ${formatInt(state.summary.studentGirls)}", modifier = it) },
            { KpiCard("Travailleurs", formatInt(state.summary.workerCount), "H ${formatInt(state.summary.workerMen)} · F ${formatInt(state.summary.workerWomen)}", modifier = it) },
            { KpiCard("Enseignants", formatInt(state.summary.teacherCount), modifier = it) },
            { KpiCard("Ouvriers", formatInt(state.summary.ouvrierCount), "Admin ${formatInt(state.summary.adminCount)}", modifier = it) },
        )
        PrimaryScrollableTabRow(selectedTabIndex = state.tab, edgePadding = 0.dp) {
            Tab(selected = state.tab == 0, onClick = { viewModel.setTab(0) }, text = { Text("Élèves") })
            Tab(selected = state.tab == 1, onClick = { viewModel.setTab(1) }, text = { Text("Travailleurs") })
        }
        if (state.isLoading) {
            CircularProgressIndicator()
            return
        }
        when (state.tab) {
            0 -> StudentsPane(
                students = state.visibleStudents,
                query = state.studentQuery,
                onQuery = viewModel::onStudentSearch,
                onAdd = viewModel::openNewStudent,
                onEdit = viewModel::openEditStudent,
                onDelete = viewModel::deleteStudent,
            )
            else -> WorkersPane(
                workers = state.visibleWorkers,
                query = state.workerQuery,
                onQuery = viewModel::onWorkerSearch,
                onAdd = viewModel::openNewWorker,
                onEdit = viewModel::openEditWorker,
                onDelete = viewModel::deleteWorker,
            )
        }
        state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }

    state.editingStudent?.let { student ->
        StudentFormDialog(
            student = student,
            onDismiss = viewModel::closeStudentForm,
            onSave = viewModel::saveStudent,
        )
    }
    state.editingWorker?.let { worker ->
        WorkerFormDialog(
            worker = worker,
            onDismiss = viewModel::closeWorkerForm,
            onSave = viewModel::saveWorker,
        )
    }
}

@Composable
private fun StudentsPane(
    students: List<StudentRecord>,
    query: String,
    onQuery: (String) -> Unit,
    onAdd: () -> Unit,
    onEdit: (StudentRecord) -> Unit,
    onDelete: (StudentRecord) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                label = { Text("Rechercher un élève") },
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                singleLine = true,
            )
            Button(onClick = onAdd) { Text("Enregistrer un élève") }
        }
        if (students.isEmpty()) {
            Text("Aucun élève enregistré pour cette école.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                items(students, key = { it.id }) { student ->
                    Surface(Modifier.fillMaxWidth(), tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(student.fullName, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${student.className} · ${student.gender.labelStudentFr()} · ${student.age} ans",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Row {
                                TextButton(onClick = { onEdit(student) }) { Text("Modifier") }
                                TextButton(onClick = { onDelete(student) }) { Text("Retirer") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkersPane(
    workers: List<WorkerRecord>,
    query: String,
    onQuery: (String) -> Unit,
    onAdd: () -> Unit,
    onEdit: (WorkerRecord) -> Unit,
    onDelete: (WorkerRecord) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                label = { Text("Rechercher un travailleur") },
                modifier = Modifier.weight(1f).padding(end = 12.dp),
                singleLine = true,
            )
            Button(onClick = onAdd) { Text("Enregistrer un travailleur") }
        }
        if (workers.isEmpty()) {
            Text("Aucun travailleur enregistré pour cette école.", style = MaterialTheme.typography.bodyMedium)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.heightIn(max = 520.dp)) {
                items(workers, key = { it.id }) { worker ->
                    Surface(Modifier.fillMaxWidth(), tonalElevation = 1.dp, shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(worker.fullName, fontWeight = FontWeight.SemiBold)
                                Text(
                                    listOfNotNull(
                                        worker.category.labelFr(),
                                        worker.gender.labelWorkerFr(),
                                        worker.educationLevel.labelFr(),
                                        worker.teacherBranch?.labelFr(),
                                        worker.adminFunction?.labelFr(),
                                    ).joinToString(" · "),
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                            Row {
                                TextButton(onClick = { onEdit(worker) }) { Text("Modifier") }
                                TextButton(onClick = { onDelete(worker) }) { Text("Retirer") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentFormDialog(
    student: StudentRecord,
    onDismiss: () -> Unit,
    onSave: (StudentRecord) -> Unit,
) {
    var name by remember(student.id) { mutableStateOf(student.fullName) }
    var gender by remember(student.id) { mutableStateOf(student.gender) }
    var className by remember(student.id) { mutableStateOf(student.className) }
    var age by remember(student.id) { mutableStateOf(student.age.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (student.id.isBlank()) "Nouvel élève" else "Modifier l'élève") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth())
                Text("Sexe", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Gender.entries.forEach {
                        FilterChip(selected = gender == it, onClick = { gender = it }, label = { Text(it.labelStudentFr()) })
                    }
                }
                Text("Classe", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    CensusDefaults.primaryClasses.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { clazz ->
                                FilterChip(selected = className == clazz, onClick = { className = clazz }, label = { Text(clazz.removeSuffix(" année")) })
                            }
                        }
                    }
                }
                OutlinedTextField(age, { age = it.filter(Char::isDigit) }, label = { Text("Âge") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        student.copy(
                            fullName = name.trim(),
                            gender = gender,
                            className = className,
                            age = age.toIntOrNull() ?: student.age,
                        ),
                    )
                },
                enabled = name.isNotBlank(),
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}

@Composable
private fun WorkerFormDialog(
    worker: WorkerRecord,
    onDismiss: () -> Unit,
    onSave: (WorkerRecord) -> Unit,
) {
    var name by remember(worker.id) { mutableStateOf(worker.fullName) }
    var gender by remember(worker.id) { mutableStateOf(worker.gender) }
    var category by remember(worker.id) { mutableStateOf(worker.category) }
    var level by remember(worker.id) { mutableStateOf(worker.educationLevel) }
    var branch by remember(worker.id) { mutableStateOf(worker.teacherBranch ?: TeacherBranch.PRIMAIRE) }
    var function by remember(worker.id) { mutableStateOf(worker.adminFunction ?: AdminStaffFunction.SECRETAIRE) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (worker.id.isBlank()) "Nouveau travailleur" else "Modifier le travailleur") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nom complet") }, modifier = Modifier.fillMaxWidth())
                Text("Sexe", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Gender.entries.forEach {
                        FilterChip(selected = gender == it, onClick = { gender = it }, label = { Text(it.labelWorkerFr()) })
                    }
                }
                Text("Catégorie", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WorkerCategory.entries.forEach {
                        FilterChip(selected = category == it, onClick = { category = it }, label = { Text(it.labelFr()) })
                    }
                }
                Text("Niveau d'études", style = MaterialTheme.typography.labelMedium)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    EducationLevel.entries.chunked(4).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach {
                                FilterChip(selected = level == it, onClick = { level = it }, label = { Text(it.labelFr()) })
                            }
                        }
                    }
                }
                if (category == WorkerCategory.ENSEIGNANT) {
                    Text("Branche", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        TeacherBranch.entries.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach {
                                    FilterChip(selected = branch == it, onClick = { branch = it }, label = { Text(it.labelFr()) })
                                }
                            }
                        }
                    }
                }
                if (category == WorkerCategory.ADMINISTRATIF) {
                    Text("Fonction", style = MaterialTheme.typography.labelMedium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        AdminStaffFunction.entries.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach {
                                    FilterChip(selected = function == it, onClick = { function = it }, label = { Text(it.labelFr()) })
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        worker.copy(
                            fullName = name.trim(),
                            gender = gender,
                            category = category,
                            educationLevel = level,
                            teacherBranch = branch.takeIf { category == WorkerCategory.ENSEIGNANT },
                            adminFunction = function.takeIf { category == WorkerCategory.ADMINISTRATIF },
                        ),
                    )
                },
                enabled = name.isNotBlank(),
            ) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } },
    )
}
