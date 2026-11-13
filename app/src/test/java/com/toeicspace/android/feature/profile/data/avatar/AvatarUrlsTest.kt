package com.toeicspace.android.feature.profile.data.avatar

import org.junit.Assert.assertEquals
import org.junit.Test

class AvatarUrlsTest {
    @Test
    fun `the bucket prefix is dropped and the cdn serves the key`() {
        val url =
            publicAvatarUrl(
                uploadUrl =
                    "https://acc.r2.cloudflarestorage.com/toeic-space-media/avatars/user-7?X-Amz-Signature=x",
                cdnBaseUrl = "https://pub-1.r2.dev/",
                version = 99,
            )

        assertEquals("https://pub-1.r2.dev/avatars/user-7?v=99", url)
    }

    @Test
    fun `without a cdn the upload origin is used`() {
        val url =
            publicAvatarUrl(
                uploadUrl = "https://storage.test:8443/bucket/avatars/user-7?sig=1",
                cdnBaseUrl = "",
                version = 1,
            )

        assertEquals("https://storage.test:8443/avatars/user-7?v=1", url)
    }
}
