package com.toeicspace.android.feature.classes.data.repository

import com.toeicspace.android.core.common.AppResult
import com.toeicspace.android.core.data.repository.SessionRepository
import com.toeicspace.android.core.model.SessionState
import com.toeicspace.android.core.network.safeApiCall
import com.toeicspace.android.feature.classes.data.remote.ClassroomApiService
import com.toeicspace.android.feature.classes.data.remote.toDomain
import com.toeicspace.android.feature.classes.domain.StudentClass
import com.toeicspace.android.feature.classes.domain.StudentClassRepository
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/** What was loaded last and the account it belongs to, so it never shows after switching accounts. */
private data class ClassCache(
    val userId: String,
    val list: List<StudentClass>? = null,
    val details: Map<String, StudentClass> = emptyMap(),
)

/**
 * Reads the student's classes from Classroom and keeps the last answer in memory. A student has a
 * handful of classes, so the list is read page by page (100 a page, the API's maximum) until all
 * are in; [MAX_PAGES] only guards against a misbehaving server.
 */
@Singleton
internal class StudentClassRepositoryImpl
    @Inject
    constructor(
        private val classroomApiService: ClassroomApiService,
        private val sessionRepository: SessionRepository,
        private val json: Json,
    ) : StudentClassRepository {
        private val mutex = Mutex()
        private var cache: ClassCache? = null

        override suspend fun getMyClasses(): AppResult<List<StudentClass>> {
            val classes = mutableListOf<StudentClass>()
            var page = 1
            var done = false
            while (!done) {
                val result =
                    safeApiCall(json) { classroomApiService.getMyClasses(page, PAGE_SIZE) }
                if (result is AppResult.Failure) return result
                val dto = (result as AppResult.Success).data
                classes += dto.items.map { it.toDomain() }
                done = dto.items.isEmpty() ||
                    page * dto.pageSize >= dto.totalCount ||
                    page >= MAX_PAGES
                page++
            }
            val list = classes.distinctBy { it.id }
            updateCache { it.copy(list = list) }
            return AppResult.Success(list)
        }

        override suspend fun getMyClass(id: String): AppResult<StudentClass> =
            when (val result = safeApiCall(json) { classroomApiService.getMyClass(id) }) {
                is AppResult.Failure -> result
                is AppResult.Success -> {
                    val detail = result.data.classInfo.toDomain()
                    updateCache { cached ->
                        cached.copy(
                            details = cached.details + (id to detail),
                            list = cached.list?.map { if (it.id == id) detail else it },
                        )
                    }
                    AppResult.Success(detail)
                }
            }

        override suspend fun cachedClasses(): List<StudentClass>? = currentCache()?.list

        override suspend fun cachedClass(id: String): StudentClass? {
            val cached = currentCache() ?: return null
            return cached.details[id] ?: cached.list?.firstOrNull { it.id == id }
        }

        private suspend fun currentUserId(): String? =
            sessionRepository.session
                .filterIsInstance<SessionState.Ready>()
                .first()
                .user
                ?.id

        private suspend fun currentCache(): ClassCache? {
            val userId = currentUserId() ?: return null
            return mutex.withLock { cache?.takeIf { it.userId == userId } }
        }

        private suspend fun updateCache(transform: (ClassCache) -> ClassCache) {
            val userId = currentUserId() ?: return
            mutex.withLock {
                val current = cache?.takeIf { it.userId == userId } ?: ClassCache(userId)
                cache = transform(current)
            }
        }

        private companion object {
            const val PAGE_SIZE = 100
            const val MAX_PAGES = 10
        }
    }
