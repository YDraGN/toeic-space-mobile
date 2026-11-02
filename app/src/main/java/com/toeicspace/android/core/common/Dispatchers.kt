package com.toeicspace.android.core.common

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Dispatcher(
    val dispatcher: ToeicDispatchers,
)

enum class ToeicDispatchers {
    Default,
    IO,
}
