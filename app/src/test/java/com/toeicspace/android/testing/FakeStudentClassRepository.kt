package com.toeicspace.android.testing

import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassRepository
import com.toeicspace.android.feature.classes.domain.StudentClassStatus

class FakeStudentClassRepository : StudentClassRepository {
    var list: AppResult<List<StudentClass>> = AppResult.Failure(AppError.Unknown)
    var detail: AppResult<StudentClass> = AppResult.Failure(AppError.Unknown)
    var cachedList: List<StudentClass>? = null
    var cachedDetail: StudentClass? = null

    var listCalls = 0
        private set
    var detailCalls = 0
        private set
    var lastDetailId: String? = null
        private set

    override suspend fun getMyClasses(): AppResult<List<StudentClass>> {
        listCalls++
        return list
    }

    override suspend fun getMyClass(id: String): AppResult<StudentClass> {
        detailCalls++
        lastDetailId = id
        return detail
    }

    override suspend fun cachedClasses(): List<StudentClass>? = cachedList

    override suspend fun cachedClass(id: String): StudentClass? =
        cachedDetail?.takeIf { it.id == id }
}

fun testClass(
    id: String,
    status: StudentClassStatus? = StudentClassStatus.ACTIVE,
) = StudentClass(
    id = id,
    code = "CODE-$id",
    name = "Class $id",
    description = null,
    status = status,
    startDate = null,
    endDate = null,
    teacherName = "Teacher",
    hasTeacher = true,
)
