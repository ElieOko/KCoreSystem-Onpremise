package com.schoolstats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolstats.data.demo.DemoDataSeeder
import com.schoolstats.domain.model.CertificationResult
import com.schoolstats.domain.model.EnrollmentStat
import com.schoolstats.domain.model.PrimaryClassStat
import com.schoolstats.domain.model.SecondaryStudentStat
import com.schoolstats.domain.model.Submission
import com.schoolstats.domain.model.SubmissionStatus
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.TeacherStatDetail
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.repository.StatisticsRepository
import com.schoolstats.domain.repository.SubmissionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SubmissionsUiState(
    val submissions: List<Submission> = emptyList(),
    val statusFilter: String? = null,
)

class SubmissionsViewModel(
    private val submissionRepository: SubmissionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubmissionsUiState())
    val uiState: StateFlow<SubmissionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            submissionRepository.observeSubmissions(null).collect { submissions ->
                _uiState.update { it.copy(submissions = submissions) }
            }
        }
    }

    fun filterByStatus(status: String?) {
        _uiState.update { it.copy(statusFilter = status) }
        viewModelScope.launch {
            submissionRepository.observeSubmissions(status).collect { submissions ->
                _uiState.update { it.copy(submissions = submissions) }
            }
        }
    }
}

data class StatisticsUiState(
    val submissionId: String = DemoDataSeeder.DEMO_SUBMISSION_ID,
    val schoolId: String = "sch-1",
    val tab: Int = 0,
    val primary: List<PrimaryClassStat> = defaultPrimary(),
    val secondary: List<SecondaryStudentStat> = defaultSecondary(),
    val teachers: List<TeacherStatDetail> = defaultTeachers(),
    val enrollments: List<EnrollmentStat> = defaultEnrollments(),
    val certifications: List<CertificationResult> = defaultCerts(),
    val message: String? = null,
    val error: String? = null,
)

private fun defaultPrimary() = listOf(
    PrimaryClassStat(className = "1ère année", classOrder = 1, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "2ème année", classOrder = 2, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "3ème année", classOrder = 3, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "4ème année", classOrder = 4, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "5ème année", classOrder = 5, boysCount = 0, girlsCount = 0),
    PrimaryClassStat(className = "6ème année", classOrder = 6, boysCount = 0, girlsCount = 0),
)

private fun defaultSecondary() = listOf(
    SecondaryStudentStat(sectionName = "Enseignement Général", optionName = "Latin-Philo", className = "3ème", boysCount = 0, girlsCount = 0),
)

private fun defaultTeachers() = listOf(
    TeacherStatDetail(educationLevel = EducationLevel.GRADUE, branch = TeacherBranch.PRIMAIRE, menCount = 0, womenCount = 0),
)

private fun defaultEnrollments() = listOf(
    EnrollmentStat(className = "1ère année", boysCount = 0, girlsCount = 0, isBeginning = true),
    EnrollmentStat(className = "1ère année", boysCount = 0, girlsCount = 0, isBeginning = false),
)

private fun defaultCerts() = listOf(
    CertificationResult(examName = "TENAFEP", className = "6ème année", registeredCount = 0, participantsCount = 0, successesCount = 0, boysSucceeded = 0, girlsSucceeded = 0),
)

class StatisticsViewModel(
    private val statisticsRepository: StatisticsRepository,
    private val submissionRepository: SubmissionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        load(DemoDataSeeder.DEMO_SUBMISSION_ID)
    }

    fun load(submissionId: String) {
        viewModelScope.launch {
            statisticsRepository.observePrimaryStats(submissionId).collect { if (it.isNotEmpty()) _uiState.update { s -> s.copy(primary = it) } }
        }
        viewModelScope.launch {
            statisticsRepository.observeSecondaryStats(submissionId).collect { if (it.isNotEmpty()) _uiState.update { s -> s.copy(secondary = it) } }
        }
        viewModelScope.launch {
            statisticsRepository.observeTeacherStats(submissionId).collect { if (it.isNotEmpty()) _uiState.update { s -> s.copy(teachers = it) } }
        }
        viewModelScope.launch {
            statisticsRepository.observeEnrollments(submissionId).collect { if (it.isNotEmpty()) _uiState.update { s -> s.copy(enrollments = it) } }
        }
        viewModelScope.launch {
            statisticsRepository.observeCertifications(submissionId).collect { if (it.isNotEmpty()) _uiState.update { s -> s.copy(certifications = it) } }
        }
        _uiState.update { it.copy(submissionId = submissionId) }
    }

    fun setTab(tab: Int) = _uiState.update { it.copy(tab = tab) }

    fun updatePrimary(index: Int, boys: Int? = null, girls: Int? = null) = _uiState.update { state ->
        val list = state.primary.toMutableList()
        val c = list[index]
        list[index] = c.copy(boysCount = boys ?: c.boysCount, girlsCount = girls ?: c.girlsCount)
        state.copy(primary = list, message = null)
    }

    fun saveAll() {
        val state = _uiState.value
        viewModelScope.launch {
            runCatching {
                statisticsRepository.savePrimaryStats(state.submissionId, state.schoolId, state.primary).getOrThrow()
                statisticsRepository.saveSecondaryStats(state.submissionId, state.secondary).getOrThrow()
                statisticsRepository.saveTeacherStats(state.submissionId, state.teachers).getOrThrow()
                statisticsRepository.saveEnrollments(state.submissionId, state.enrollments).getOrThrow()
                statisticsRepository.saveCertifications(state.submissionId, state.certifications).getOrThrow()
            }.onSuccess {
                _uiState.update { it.copy(message = "Statistiques enregistrées.", error = null) }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun submit() {
        viewModelScope.launch {
            submissionRepository.submitDeclaration(_uiState.value.submissionId)
                .onSuccess { _uiState.update { it.copy(message = "Déclaration soumise.") } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    val enrollmentComparison get() = statisticsRepository.compareEnrollments(_uiState.value.enrollments)
}

data class ValidationUiState(
    val submissions: List<Submission> = emptyList(),
    val selected: Submission? = null,
    val comment: String = "",
    val message: String? = null,
    val error: String? = null,
)

class ValidationViewModel(
    private val submissionRepository: SubmissionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ValidationUiState())
    val uiState: StateFlow<ValidationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            submissionRepository.observeSubmissions(null).collect { list ->
                val pending = list.filter { it.status in setOf(SubmissionStatus.SOUMIS, SubmissionStatus.EN_VERIFICATION) }
                _uiState.update { it.copy(submissions = pending, selected = pending.firstOrNull()) }
            }
        }
    }

    fun select(submission: Submission) = _uiState.update { it.copy(selected = submission, comment = "") }
    fun setComment(c: String) = _uiState.update { it.copy(comment = c) }

    fun validate() = act { submissionRepository.validateSubmission(it, _uiState.value.comment.ifBlank { null }) }
    fun reject() = act { submissionRepository.rejectSubmission(it, _uiState.value.comment) }
    fun requestCorrection() = act { submissionRepository.requestCorrection(it, _uiState.value.comment) }

    private fun act(block: suspend (String) -> Result<Unit>) {
        val id = _uiState.value.selected?.id ?: return
        viewModelScope.launch {
            block(id)
                .onSuccess { _uiState.update { it.copy(message = "Action effectuée.", error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
}

data class CentralizationUiState(
    val stats: com.schoolstats.domain.model.CentralizationStats = com.schoolstats.domain.model.CentralizationStats(),
    val schoolYear: String = DemoDataSeeder.DEMO_YEAR,
)

class CentralizationViewModel(
    private val centralizationRepository: com.schoolstats.domain.repository.CentralizationRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CentralizationUiState())
    val uiState: StateFlow<CentralizationUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            centralizationRepository.observeCentralization(emptyList()).collect { stats ->
                _uiState.update { it.copy(stats = stats) }
            }
        }
    }
}

data class ReportsUiState(
    val exportPath: String = "",
    val message: String? = null,
    val error: String? = null,
)

class ReportsViewModel(
    private val reportRepository: com.schoolstats.domain.repository.ReportRepository,
    private val exportService: com.schoolstats.data.export.ExportService,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    fun setPath(path: String) = _uiState.update { it.copy(exportPath = path) }

    fun exportExcel() = export { report, path -> exportService.exportExcel(report, "$path/rapport.xlsx") }
    fun exportPdf() = export { report, path -> exportService.exportPdf(report, "$path/rapport.pdf") }

    private fun export(block: suspend (com.schoolstats.domain.model.ReportRequest, String) -> Result<String>) {
        val base = _uiState.value.exportPath.ifBlank { "exports" }
        viewModelScope.launch {
            val report = reportRepository.buildReport(DemoDataSeeder.DEMO_YEAR, "Limete")
            block(report, base)
                .onSuccess { _uiState.update { it.copy(message = "Export réussi: $it", error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }
}

class UsersViewModel(
    private val userRepository: com.schoolstats.domain.repository.UserManagementRepository,
) : ViewModel() {
    private val _users = MutableStateFlow<List<com.schoolstats.domain.model.ManagedUser>>(emptyList())
    val users = _users.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.observeUsers().collect { _users.value = it }
        }
    }
}

data class SettingsUiState(val darkTheme: Boolean = false, val message: String? = null)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    fun toggleTheme() = _uiState.update { it.copy(darkTheme = !it.darkTheme) }
}
