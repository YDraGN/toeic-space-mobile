package com.toeicspace.android.core.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import com.toeicspace.android.R
import kotlinx.coroutines.launch

/**
 * The action for buttons whose feature is not built yet: shows "Tính năng đang được phát triển",
 * replacing any snackbar already on screen so repeated taps do not queue up.
 */
@Composable
fun rememberComingSoonAction(snackbarHostState: SnackbarHostState): () -> Unit {
    val scope = rememberCoroutineScope()
    val message = stringResource(id = R.string.common_msg_coming_soon)
    return {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message)
        }
    }
}
