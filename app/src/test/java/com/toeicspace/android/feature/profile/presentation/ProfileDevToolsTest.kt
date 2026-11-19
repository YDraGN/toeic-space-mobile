package com.toeicspace.android.feature.profile.presentation

import com.toeicspace.android.testing.FakeCrashReporter
import com.toeicspace.android.testing.FakeProfileRepository
import com.toeicspace.android.testing.FakePushTokenProvider
import com.toeicspace.android.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProfileDevToolsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val crash = FakeCrashReporter()
    private val tokens = FakePushTokenProvider()

    private fun viewModel() =
        ProfileViewModel(
            profileRepository = FakeProfileRepository(initial = null),
            crashReporter = crash,
            pushTokens = tokens,
        )

    @Test
    fun `test crash turns reporting on, then crashes with a recognisable exception`() {
        val viewModel = viewModel()

        val thrown = runCatching { viewModel.onTestCrash() }.exceptionOrNull()

        assertTrue(thrown is TestCrashException)
        assertTrue(crash.isCollectionEnabled)
    }

    @Test
    fun `test error is recorded as a non-fatal and says so`() {
        val viewModel = viewModel()

        viewModel.onTestNonFatal()

        assertTrue(crash.recorded.single() is TestCrashException)
        assertTrue(crash.isCollectionEnabled)
        assertEquals(ProfileMessage.TestErrorRecorded, viewModel.message.value)
    }

    @Test
    fun `the push token is handed to the screen to copy, then confirmed`() =
        runTest {
            val viewModel = viewModel()

            viewModel.onCopyPushToken()
            assertEquals("fcm-token", viewModel.tokenToCopy.value)

            viewModel.onPushTokenCopied()
            assertNull(viewModel.tokenToCopy.value)
            assertEquals(ProfileMessage.PushTokenCopied, viewModel.message.value)
        }

    @Test
    fun `without a token it says it could not get one`() =
        runTest {
            tokens.token = null
            val viewModel = viewModel()

            viewModel.onCopyPushToken()

            assertNull(viewModel.tokenToCopy.value)
            assertEquals(ProfileMessage.PushTokenUnavailable, viewModel.message.value)
        }
}
