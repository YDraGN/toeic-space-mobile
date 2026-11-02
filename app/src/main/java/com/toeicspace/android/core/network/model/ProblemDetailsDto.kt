package com.toeicspace.android.core.network.model

import kotlinx.serialization.Serializable

/** RFC 7807 error body returned by every backend service, plus the `code` and `errors` extensions. */
@Serializable
data class ProblemDetailsDto(
    val status: Int? = null,
    val code: String? = null,
    val errors: Map<String, List<String>>? = null,
)
