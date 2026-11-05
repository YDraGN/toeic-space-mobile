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
                        modes = setOf(StudyMode.ONLINE, StudyMode.OFFLINE),
                        description =
                            "Lớp nhỏ 8 bạn, luyện đề sát đề thật mỗi tuần, có lớp tối cho người đi làm.",
                    ),
                    LearningCenter(
                        id = "sample-2",
                        name = "Ngọc Trai English",
                        location = "Cầu Giấy, Hà Nội",
                        modes = setOf(StudyMode.OFFLINE),
                        description =
                            "Lộ trình từ mất gốc, giáo viên kèm phát âm 1–1 trong từng buổi học.",
                    ),
                    LearningCenter(
                        id = "sample-3",
                        name = "Sóng Mới TOEIC",
                        location = "Học trực tuyến",
                        modes = setOf(StudyMode.ONLINE),
                        description =
                            "Học trực tuyến theo nhóm nhỏ, chữa đề Listening và Reading sau mỗi buổi.",
                    ),
                    LearningCenter(
                        id = "sample-4",
                        name = "Hải Đăng Academy",
                        location = "Ninh Kiều, Cần Thơ",
                        modes = setOf(StudyMode.OFFLINE),
                        description =
                            "Lớp cuối tuần cho sinh viên, thi thử định kỳ mỗi tháng tại trung tâm.",
                    ),
                    LearningCenter(
                        id = "sample-5",
                        name = "Sao Biển Learning",
                        location = "Học trực tuyến",
                        modes = setOf(StudyMode.ONLINE),
                        description =
                            "Lớp 1–1 linh hoạt giờ học, chữa từng câu sai qua video call.",
                    ),
                    LearningCenter(
                        id = "sample-6",
                        name = "Rạn San Hô English",
                        location = "Ngô Quyền, Hải Phòng",
                        modes = setOf(StudyMode.ONLINE, StudyMode.OFFLINE),
                        description =
                            "Học ở lớp hoặc tại nhà, có bài tập Part 5–7 mỗi tối kèm giải thích.",
                    ),
                ),
            )
    }
