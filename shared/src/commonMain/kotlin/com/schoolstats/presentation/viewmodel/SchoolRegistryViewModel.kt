package com.schoolstats.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.schoolstats.data.demo.DemoDataSeeder
import com.schoolstats.domain.census.RegistryAggregator
import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.Gender
import com.schoolstats.domain.model.School
import com.schoolstats.domain.model.SchoolRegistrySummary
import com.schoolstats.domain.model.StudentRecord
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.UserRole
import com.schoolstats.domain.model.WorkerCategory
import com.schoolstats.domain.model.WorkerRecord
import com.schoolstats.domain.repository.AuthRepository
import com.schoolstats.domain.repository.SchoolRegistryRepository
import com.schoolstats.domain.repository.SchoolRepository
import com.schoolstats.domain.repository.SchoolYearRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SchoolRegistryUiState(
    val tab: Int = 0,
    val isLoading: Boolean = true,
    val isSchoolAccount: Boolean = false,
    val schoolName: String = "",
    val selectedSchoolId: String = "",
    val schools: List<School> = emptyList(),
    val students: List<StudentRecord> = emptyList(),
    val workers: List<WorkerRecord> = emptyList(),
    val summary: SchoolRegistrySummary = SchoolRegistrySummary(),
    val studentQuery: String = "",
    val workerQuery: String = "",
    val editingStudent: StudentRecord? = null,
    val editingWorker: WorkerRecord? = null,
    val message: String? = null,
    val error: String? = null,
) {
    val visibleStudents: List<StudentRecord>
        get() {
            val q = studentQuery.trim()
            if (q.isEmpty()) return students
            return students.filter {
                it.fullName.contains(q, ignoreCase = true) || it.className.contains(q, ignoreCase = true)
            }
        }
    val visibleWorkers: List<WorkerRecord>
        get() {
            val q = workerQuery.trim()
            if (q.isEmpty()) return workers
            return workers.filter {
                it.fullName.contains(q, ignoreCase = true) || it.category.name.contains(q, ignoreCase = true)
            }
        }
}

class SchoolRegistryViewModel(
    private val registryRepository: SchoolRegistryRepository,
    private val authRepository: AuthRepository,
    private val schoolRepository: SchoolRepository,
    private val schoolYearRepository: SchoolYearRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SchoolRegistryUiState())
    val uiState: StateFlow<SchoolRegistryUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            val profile = authRepository.currentProfile.value
            val yearId = schoolYearRepository.getDefaultYear().id
            val isSchool = profile?.role == UserRole.ECOLE
            val schools = schoolRepository.observeSchools("").first()
            val schoolId = profile?.schoolId
                ?: DemoDataSeeder.DEMO_SCHOOL_ID.takeIf { id -> schools.any { it.id == id } }
                ?: schools.firstOrNull()?.id.orEmpty()
            val schoolName = schools.firstOrNull { it.id == schoolId }?.name
                ?: schoolRepository.getSchool(schoolId)?.name
                ?: DemoDataSeeder.DEMO_SCHOOL_NAME
            _uiState.update {
                it.copy(
                    isSchoolAccount = isSchool,
                    selectedSchoolId = schoolId,
                    schoolName = schoolName,
                    schools = if (isSchool) schools.filter { school -> school.id == schoolId } else schools,
                )
            }
            if (schoolId.isNotBlank()) {
                registryRepository.seedDemoIfEmpty(schoolId, yearId)
                observe(schoolId, yearId)
            } else {
                _uiState.update { it.copy(isLoading = false, error = "Aucune école associée au compte.") }
            }
        }
    }

    fun setTab(tab: Int) = _uiState.update { it.copy(tab = tab, message = null, error = null) }

    fun onStudentSearch(query: String) = _uiState.update { it.copy(studentQuery = query) }

    fun onWorkerSearch(query: String) = _uiState.update { it.copy(workerQuery = query) }

    fun selectSchool(schoolId: String) {
        val yearId = schoolYearRepository.getDefaultYear().id
        val name = _uiState.value.schools.firstOrNull { it.id == schoolId }?.name.orEmpty()
        _uiState.update { it.copy(selectedSchoolId = schoolId, schoolName = name, isLoading = true) }
        viewModelScope.launch {
            registryRepository.seedDemoIfEmpty(schoolId, yearId)
            observe(schoolId, yearId)
        }
    }

    fun openNewStudent() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                editingStudent = StudentRecord(
                    schoolId = state.selectedSchoolId,
                    schoolYearId = schoolYearRepository.getDefaultYear().id,
                    fullName = "",
                    gender = Gender.MALE,
                    className = "1ère année",
                    age = 6,
                ),
                message = null,
                error = null,
            )
        }
    }

    fun openEditStudent(student: StudentRecord) = _uiState.update { it.copy(editingStudent = student, error = null) }

    fun closeStudentForm() = _uiState.update { it.copy(editingStudent = null) }

    fun openNewWorker() {
        val state = _uiState.value
        _uiState.update {
            it.copy(
                editingWorker = WorkerRecord(
                    schoolId = state.selectedSchoolId,
                    schoolYearId = schoolYearRepository.getDefaultYear().id,
                    fullName = "",
                    gender = Gender.MALE,
                    category = WorkerCategory.ENSEIGNANT,
                    educationLevel = EducationLevel.GRADUE,
                    teacherBranch = TeacherBranch.PRIMAIRE,
                ),
                message = null,
                error = null,
            )
        }
    }

    fun openEditWorker(worker: WorkerRecord) = _uiState.update { it.copy(editingWorker = worker, error = null) }

    fun closeWorkerForm() = _uiState.update { it.copy(editingWorker = null) }

    fun saveStudent(student: StudentRecord) {
        viewModelScope.launch {
            registryRepository.saveStudent(student.copy(schoolId = _uiState.value.selectedSchoolId))
                .onSuccess {
                    _uiState.update { s -> s.copy(editingStudent = null, message = "Élève enregistré.", error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteStudent(student: StudentRecord) {
        viewModelScope.launch {
            val yearId = schoolYearRepository.getDefaultYear().id
            registryRepository.deleteStudent(student.id, student.schoolId, yearId)
                .onSuccess { _uiState.update { it.copy(message = "Élève retiré du registre.", error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun saveWorker(worker: WorkerRecord) {
        viewModelScope.launch {
            val normalized = when (worker.category) {
                WorkerCategory.ENSEIGNANT -> worker.copy(
                    teacherBranch = worker.teacherBranch ?: TeacherBranch.PRIMAIRE,
                    adminFunction = null,
                )
                WorkerCategory.ADMINISTRATIF -> worker.copy(
                    adminFunction = worker.adminFunction ?: AdminStaffFunction.SECRETAIRE,
                    teacherBranch = null,
                )
                WorkerCategory.OUVRIER -> worker.copy(teacherBranch = null, adminFunction = null)
            }
            registryRepository.saveWorker(normalized.copy(schoolId = _uiState.value.selectedSchoolId))
                .onSuccess {
                    _uiState.update { s -> s.copy(editingWorker = null, message = "Travailleur enregistré.", error = null) }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
        }
    }

    fun deleteWorker(worker: WorkerRecord) {
        viewModelScope.launch {
            val yearId = schoolYearRepository.getDefaultYear().id
            registryRepository.deleteWorker(worker.id, worker.schoolId, yearId)
                .onSuccess { _uiState.update { it.copy(message = "Travailleur retiré du registre.", error = null) } }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    private fun observe(schoolId: String, yearId: String) {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            combine(
                registryRepository.observeStudents(schoolId, yearId),
                registryRepository.observeWorkers(schoolId, yearId),
            ) { students, workers -> students to workers }
                .collect { (students, workers) ->
                    val name = _uiState.value.schoolName
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            students = students,
                            workers = workers,
                            summary = RegistryAggregator.summary(schoolId, name, students, workers),
                        )
                    }
                }
        }
    }
}
