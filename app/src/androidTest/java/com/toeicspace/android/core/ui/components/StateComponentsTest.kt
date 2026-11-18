package com.toeicspace.android.core.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.toeicspace.android.R
import com.toeicspace.android.core.common.AppError
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StateComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun string(id: Int): String =
        InstrumentationRegistry.getInstrumentation().targetContext.getString(id)

    @Test
    fun networkError_showsOfflineCopyAndRetries() {
        var retries = 0
        composeRule.setContent {
            ToeicSpaceTheme { ErrorState(error = AppError.Network, onRetry = { retries++ }) }
        }

        composeRule.onNodeWithTag(TestTags.STATE_ERROR).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.state_error_network_title)).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.STATE_ACTION_RETRY).performClick()
        assertEquals(1, retries)
    }

    @Test
    fun notFound_hidesRetry() {
        composeRule.setContent {
            ToeicSpaceTheme {
                ErrorState(error = AppError.Server(code = null, status = 404), onRetry = {})
            }
        }

        composeRule.onNodeWithTag(TestTags.STATE_ACTION_RETRY).assertDoesNotExist()
    }

    @Test
    fun cachedContent_showsDataWithStaleFlagWhenOffline() {
        composeRule.setContent {
            ToeicSpaceTheme {
                CachedContent(
                    data = "cached",
                    isLoading = false,
                    error = AppError.Network,
                    onRetry = {},
                ) { _, isStale -> if (isStale) OfflineBanner() }
            }
        }

        composeRule.onNodeWithTag(TestTags.STATE_OFFLINE_BANNER).assertIsDisplayed()
        composeRule.onNodeWithTag(TestTags.STATE_ERROR).assertDoesNotExist()
    }

    @Test
    fun signInRequired_callsBothActions() {
        var signUps = 0
        var signIns = 0
        composeRule.setContent {
            ToeicSpaceTheme {
                SignInRequiredState(onSignUpClick = { signUps++ }, onSignInClick = { signIns++ })
            }
        }

        composeRule.onNodeWithTag(TestTags.STATE_ACTION_SIGN_UP).performClick()
        composeRule.onNodeWithTag(TestTags.STATE_ACTION_SIGN_IN).performClick()
        assertEquals(1, signUps)
        assertEquals(1, signIns)
    }
}
