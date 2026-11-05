package com.toeicspace.android.feature.explore.domain

import kotlinx.coroutines.flow.Flow

/** An English center that joined TOEICSpace. */
data class LearningCenter(
    val id: String,
    val name: String,
    val location: String,
    val modes: Set<StudyMode>,
    /** One or two sentences that help a learner decide; shown under the name. */
    val description: String,
)

enum class StudyMode {
    ONLINE,
    OFFLINE,
}

interface ExploreRepository {
    val featuredCenters: Flow<List<LearningCenter>>
}
