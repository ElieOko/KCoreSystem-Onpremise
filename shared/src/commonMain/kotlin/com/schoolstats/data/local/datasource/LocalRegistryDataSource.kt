package com.schoolstats.data.local.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.schoolstats.data.local.database.SchoolStatsDatabase
import com.schoolstats.domain.model.AdminStaffFunction
import com.schoolstats.domain.model.EducationLevel
import com.schoolstats.domain.model.Gender
import com.schoolstats.domain.model.StudentRecord
import com.schoolstats.domain.model.TeacherBranch
import com.schoolstats.domain.model.WorkerCategory
import com.schoolstats.domain.model.WorkerRecord
import com.schoolstats.util.currentTimeMillis
import com.schoolstats.util.randomUuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class LocalRegistryDataSource(
    private val database: SchoolStatsDatabase,
) {
    private val queries = database.registryQueries

    fun observeStudents(schoolId: String, schoolYearId: String): Flow<List<StudentRecord>> =
        queries.selectStudentsBySchool(schoolId, schoolYearId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toStudent() } }

    fun observeWorkers(schoolId: String, schoolYearId: String): Flow<List<WorkerRecord>> =
        queries.selectWorkersBySchool(schoolId, schoolYearId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { rows -> rows.map { it.toWorker() } }

    suspend fun countStudents(schoolId: String, schoolYearId: String): Long = withContext(Dispatchers.IO) {
        queries.countStudentsBySchool(schoolId, schoolYearId).executeAsOne()
    }

    suspend fun upsertStudent(student: StudentRecord): StudentRecord = withContext(Dispatchers.IO) {
        val saved = student.copy(id = student.id.ifBlank { randomUuid() })
        queries.upsertStudentRecord(
            id = saved.id,
            school_id = saved.schoolId,
            school_year_id = saved.schoolYearId,
            full_name = saved.fullName.trim(),
            gender = saved.gender.name,
            class_name = saved.className,
            age = saved.age.toLong(),
            section_name = saved.sectionName,
            option_name = saved.optionName,
            updated_at = currentTimeMillis(),
        )
        saved
    }

    suspend fun deleteStudent(id: String) = withContext(Dispatchers.IO) {
        queries.deleteStudentRecord(id)
    }

    suspend fun upsertWorker(worker: WorkerRecord): WorkerRecord = withContext(Dispatchers.IO) {
        val saved = worker.copy(id = worker.id.ifBlank { randomUuid() })
        queries.upsertWorkerRecord(
            id = saved.id,
            school_id = saved.schoolId,
            school_year_id = saved.schoolYearId,
            full_name = saved.fullName.trim(),
            gender = saved.gender.name,
            category = saved.category.name,
            education_level = saved.educationLevel.name,
            teacher_branch = saved.teacherBranch?.name,
            admin_function = saved.adminFunction?.name,
            updated_at = currentTimeMillis(),
        )
        saved
    }

    suspend fun deleteWorker(id: String) = withContext(Dispatchers.IO) {
        queries.deleteWorkerRecord(id)
    }

    private fun com.schoolstats.database.Local_student_record.toStudent() = StudentRecord(
        id = id,
        schoolId = school_id,
        schoolYearId = school_year_id,
        fullName = full_name,
        gender = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.MALE),
        className = class_name,
        age = age.toInt(),
        sectionName = section_name,
        optionName = option_name,
    )

    private fun com.schoolstats.database.Local_worker_record.toWorker() = WorkerRecord(
        id = id,
        schoolId = school_id,
        schoolYearId = school_year_id,
        fullName = full_name,
        gender = runCatching { Gender.valueOf(gender) }.getOrDefault(Gender.MALE),
        category = runCatching { WorkerCategory.valueOf(category) }.getOrDefault(WorkerCategory.OUVRIER),
        educationLevel = runCatching { EducationLevel.valueOf(education_level) }.getOrDefault(EducationLevel.AUTRES),
        teacherBranch = teacher_branch?.let { runCatching { TeacherBranch.valueOf(it) }.getOrNull() },
        adminFunction = admin_function?.let { runCatching { AdminStaffFunction.valueOf(it) }.getOrNull() },
    )
}
