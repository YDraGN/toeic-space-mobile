package com.toeicspace.android.feature.explore.data

import com.toeicspace.android.feature.explore.domain.ExploreRepository
import com.toeicspace.android.feature.explore.domain.LearningCenter
import com.toeicspace.android.feature.explore.domain.StudyMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Made-up centers until the backend can list partner centers. The Explore tab labels them as
 * sample data; replace this binding with a network repository once the API exists.
 */
class SampleExploreRepository
    @Inject
    constructor() : ExploreRepository {
        override val featuredCenters: Flow<List<LearningCenter>> =
            flowOf(
                listOf(
                    LearningCenter(
                        id = "sample-1",
                        name = "Anh ngữ Biển Xanh",
                        location = "Hải Châu, Đà Nẵng",
                        targetScore = 650,
                        modes = setOf(StudyMode.ONLINE, StudyMode.OFFLINE),
                        description =
                            "Lớp nhỏ 8 bạn, luyện đề sát đề thật mỗi tuần, có lớp tối cho người đi làm.",
                    ),
                    LearningCenter(
                        id = "sample-2",
                        name = "Ngọc Trai English",
                        location = "Cầu Giấy, Hà Nội",
                        targetScore = 750,
                        modes = setOf(StudyMode.OFFLINE),
                        description =
                            "Lộ trình từ mất gốc, giáo viên kèm phát âm 1–1 trong từng buổi học.",
                    ),
                    LearningCenter(
                        id = "sample-3",
                        name = "Sóng Mới TOEIC",
                        location = "Học trực tuyến",
                        targetScore = 500,
                        modes = setOf(StudyMode.ONLINE),
                        description =
                            "Học trực tuyến theo nhóm nhỏ, chữa đề Listening và Reading sau mỗi buổi.",
                    ),
                ),
            )
    }
