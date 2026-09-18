package com.schoolstats.data.repository

import com.schoolstats.data.demo.DemoDataSeeder
import com.schoolstats.data.local.datasource.LocalRegistryDataSource
import com.schoolstats.domain.census.RegistryAggregator
import com.schoolstats.domain.model.StudentRecord
import com.schoolstats.domain.model.WorkerRecord
import com.schoolstats.domain.repository.SchoolRegistryRepository
import com.schoolstats.domain.repository.SchoolYearRepository
import com.schoolstats.domain.repository.StatisticsRepository
import com.schoolstats.domain.repository.SubmissionRepository
import com.schoolstats.domain.validation.StatValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SchoolRegistryRepositoryImpl(
    private val local: LocalRegistryDataSource,
    private val submissionRepository: SubmissionRepository,
    private val statisticsRepository: StatisticsRepository,
    private val schoolYearRepository: SchoolYearRepository,
) : SchoolRegistryRepository {
    override fun observeStudents(schoolId: String, schoolYearId: String): Flow<List<StudentRecord>> =
        local.observeStudents(schoolId, schoolYearId)

    override fun observeWorkers(schoolId: String, schoolYearId: String): Flow<List<WorkerRecord>> =
        local.observeWorkers(schoolId, schoolYearId)

    override suspend fun saveStudent(student: StudentRecord): Result<StudentRecord> = runCatching {
        val errors = StatValidator.validateStudentRecord(student)
        if (errors.isNotEmpty()) error(errors.first().message)
        val yearId = student.schoolYearId.ifBlank { schoolYearRepository.getDefaultYear().id }
        val saved = local.upsertStudent(student.copy(schoolYearId = yearId))
        syncCensus(saved.schoolId, yearId)
        saved
    }

    override suspend fun deleteStudent(id: String, schoolId: String, schoolYearId: String): Result<Unit> = runCatching {
        local.deleteStudent(id)
        syncCensus(schoolId, schoolYearId)
    }

    override suspend fun saveWorker(worker: WorkerRecord): Result<WorkerRecord> = runCatching {
        val errors = StatValidator.validateWorkerRecord(worker)
        if (errors.isNotEmpty()) error(errors.first().message)
        val yearId = worker.schoolYearId.ifBlank { schoolYearRepository.getDefaultYear().id }
        val saved = local.upsertWorker(worker.copy(schoolYearId = yearId))
        syncCensus(saved.schoolId, yearId)
        saved
    }

    override suspend fun deleteWorker(id: String, schoolId: String, schoolYearId: String): Result<Unit> = runCatching {
        local.deleteWorker(id)
        syncCensus(schoolId, schoolYearId)
    }

    override suspend fun seedDemoIfEmpty(schoolId: String, schoolYearId: String) {
        if (local.countStudents(schoolId, schoolYearId) > 0L) return
        DemoDataSeeder.schoolStudents(schoolId, schoolYearId).forEach { local.upsertStudent(it) }
        DemoDataSeeder.schoolWorkers(schoolId, schoolYearId).forEach { local.upsertWorker(it) }
        syncCensus(schoolId, schoolYearId)
    }

    private suspend fun syncCensus(schoolId: String, schoolYearId: String) {
        val yearId = schoolYearId.ifBlank { schoolYearRepository.getDefaultYear().id }
        val submission = submissionRepository.getOrCreateSubmission(schoolId, yearId)
        val students = local.observeStudents(schoolId, yearId).first()
        val workers = local.observeWorkers(schoolId, yearId).first()
        statisticsRepository.savePrimaryStats(submission.id, schoolId, RegistryAggregator.primaryFromStudents(students)).getOrThrow()
        statisticsRepository.saveSecondaryStats(submission.id, RegistryAggregator.secondaryFromStudents(students)).getOrThrow()
        statisticsRepository.saveAgeSexStats(submission.id, RegistryAggregator.ageSexFromStudents(students)).getOrThrow()
        statisticsRepository.saveTeacherStats(submission.id, RegistryAggregator.teachersFromWorkers(workers)).getOrThrow()
        statisticsRepository.saveAdminStaffStats(submission.id, RegistryAggregator.adminFromWorkers(workers)).getOrThrow()
        statisticsRepository.saveWorkerStats(submission.id, RegistryAggregator.ouvriersFromWorkers(workers)).getOrThrow()
        statisticsRepository.saveEnrollments(submission.id, RegistryAggregator.enrollmentsFromStudents(students)).getOrThrow()
    }
}
