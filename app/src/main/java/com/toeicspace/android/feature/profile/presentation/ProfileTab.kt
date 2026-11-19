package com.toeicspace.android.feature.profile.presentation

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.BuildConfig
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.components.EditableAvatar
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.SignInRequiredState
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicChip
import com.toeicspace.android.core.ui.components.ToeicChipStyle
import com.toeicspace.android.core.ui.components.ToeicListDivider
import com.toeicspace.android.core.ui.components.ToeicListRow
import com.toeicspace.android.core.ui.components.ToeicListSection
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTint
import com.toeicspace.android.core.ui.theme.Pearl
import com.toeicspace.android.core.ui.theme.PearlPaper
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.main.presentation.Viewer
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.UserProfile

private val ButtonIconSize = 20.dp

/**
 * The learner's own space, and for now also the settings: account, app and support links, sign out
 * and delete account. When settings grow (theme, language) they move back to a screen of their own.
 */
@Composable
internal fun ProfileTab(
    viewer: Viewer,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onAccountInfoClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onComingSoon: () -> Unit,
    onSignOutClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onMyCoursesClick: () -> Unit = {},
    onMyClassesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val isUploadingAvatar by viewModel.isUploadingAvatar.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val tokenToCopy by viewModel.tokenToCopy.collectAsStateWithLifecycle()
    val notifications = rememberNotificationToggle()
    val context = LocalContext.current

    val messageText = message?.let { stringResource(id = it.messageRes()) }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(messageText)
            viewModel.onMessageShown()
        }
    }
    val tokenLabel = stringResource(id = R.string.dev_label_push_token)
    LaunchedEffect(tokenToCopy) {
        val token = tokenToCopy ?: return@LaunchedEffect
        context
            .getSystemService(ClipboardManager::class.java)
            ?.setPrimaryClip(ClipData.newPlainText(tokenLabel, token))
        viewModel.onPushTokenCopied()
    }

    ProfileContent(
        viewer = viewer,
        profile = profile,
        versionName = BuildConfig.VERSION_NAME,
        onSignUpClick = onSignUpClick,
        onSignInClick = onSignInClick,
        onAccountInfoClick = onAccountInfoClick,
        onChangePasswordClick = onChangePasswordClick,
        onComingSoon = onComingSoon,
        onSignOutClick = onSignOutClick,
        modifier = modifier,
        isUploadingAvatar = isUploadingAvatar,
        onAvatarPicked = viewModel::onAvatarPicked,
        onMyCoursesClick = onMyCoursesClick,
        onMyClassesClick = onMyClassesClick,
        notificationsEnabled = notifications.enabled,
        onNotificationsToggle = notifications.onToggle,
        devTools =
            if (BuildConfig.DEBUG) {
                DevTools(
                    onTestCrash = viewModel::onTestCrash,
                    onTestNonFatal = viewModel::onTestNonFatal,
                    onCopyPushToken = viewModel::onCopyPushToken,
                )
            } else {
                null
            },
    )
}

/** Debug builds only (PBL-110): try Crashlytics and FCM without waiting for a real crash. */
internal data class DevTools(
    val onTestCrash: () -> Unit,
    val onTestNonFatal: () -> Unit,
    val onCopyPushToken: () -> Unit,
)

@Composable
internal fun ProfileContent(
    viewer: Viewer,
    profile: UserProfile?,
    versionName: String,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onAccountInfoClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onComingSoon: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUploadingAvatar: Boolean = false,
    onAvatarPicked: (String) -> Unit = {},
    onMyCoursesClick: () -> Unit = {},
    onMyClassesClick: () -> Unit = {},
    notificationsEnabled: Boolean = false,
    onNotificationsToggle: (Boolean) -> Unit = {},
    devTools: DevTools? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
                .padding(top = Spacing.sm, bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        when (viewer) {
            Viewer.Guest -> GuestCard(onSignUpClick = onSignUpClick, onSignInClick = onSignInClick)
            is Viewer.Member -> {
                MemberHeader(
                    member = viewer,
                    profile = profile,
                    isUploadingAvatar = isUploadingAvatar,
                    onAvatarPicked = onAvatarPicked,
                )
                ToeicListSection(
                    title = stringResource(id = R.string.my_courses_section_learning),
                ) {
                    ToeicListRow(
                        iconRes = R.drawable.ic_graduation_cap,
                        label = stringResource(id = R.string.my_courses_title),
                        supportingText = stringResource(id = R.string.my_courses_body_entry),
                        onClick = onMyCoursesClick,
                        modifier = Modifier.testTag(TestTags.MY_COURSES_ENTRY),
                    )
                    ToeicListDivider()
                    ToeicListRow(
                        iconRes = R.drawable.ic_users,
                        label = stringResource(id = R.string.my_classes_title),
                        supportingText = stringResource(id = R.string.my_classes_body_entry),
                        onClick = onMyClassesClick,
                    )
                }
                ToeicListSection(title = stringResource(id = R.string.profile_section_account)) {
                    ToeicListRow(
                        iconRes = R.drawable.ic_account,
                        label = stringResource(id = R.string.profile_action_account_info),
                        onClick = onAccountInfoClick,
                    )
                    ToeicListDivider()
                    ToeicListRow(
                        iconRes = R.drawable.ic_key,
                        label = stringResource(id = R.string.profile_action_change_password),
                        iconBackground = Pearl,
                        onClick = onChangePasswordClick,
                    )
                }
            }
        }

        ToeicListSection(title = stringResource(id = R.string.profile_section_app)) {
            ToeicListRow(
                iconRes = R.drawable.ic_bell,
                label = stringResource(id = R.string.profile_label_notifications),
                supportingText = stringResource(id = R.string.profile_body_notifications),
                iconBackground = BlushTint,
                trailing = {
                    Switch(checked = notificationsEnabled, onCheckedChange = onNotificationsToggle)
                },
            )
        }

        if (devTools != null) DevToolsSection(devTools)

        ToeicListSection(title = stringResource(id = R.string.profile_section_support)) {
            ToeicListRow(
                iconRes = R.drawable.ic_file_text,
                label = stringResource(id = R.string.profile_label_terms),
                onClick = onComingSoon,
            )
            ToeicListDivider()
            ToeicListRow(
                iconRes = R.drawable.ic_shield,
                label = stringResource(id = R.string.profile_label_privacy),
                iconBackground = Pearl,
                onClick = onComingSoon,
            )
            ToeicListDivider()
            ToeicListRow(
                iconRes = R.drawable.ic_info,
                label = stringResource(id = R.string.profile_label_version),
                value = versionName,
            )
        }

        if (viewer is Viewer.Member) {
            SecondaryButton(
                text = stringResource(id = R.string.profile_action_sign_out),
                onClick = onSignOutClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_log_out),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonIconSize),
                    )
                },
            )
            // Google Play requires an in-app way to delete the account; the API is not ready yet.
            ToeicListSection {
                ToeicListRow(
                    iconRes = R.drawable.ic_trash,
                    label = stringResource(id = R.string.profile_action_delete_account),
                    iconBackground = BlushTint,
                    iconTint = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.error,
                    onClick = onComingSoon,
                )
            }
        }
    }
}

@Composable
private fun MemberHeader(
    member: Viewer.Member,
    profile: UserProfile?,
    isUploadingAvatar: Boolean,
    onAvatarPicked: (String) -> Unit,
) {
    ToeicCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = PearlPaper,
        contentPadding = PaddingValues(Spacing.lg),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            EditableAvatar(
                onPhotoPicked = { uri -> if (uri != null) onAvatarPicked(uri.toString()) },
                imageUrl = profile?.avatarUrl,
                isUploading = isUploadingAvatar,
            )
            Text(
                text = member.fullName,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.sm),
            )
            Text(
                text = member.email,
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                profile?.targetScore?.let { score ->
                    ToeicChip(
                        text = stringResource(id = R.string.account_info_value_target_score, score),
                        style = ToeicChipStyle.SKY,
                    )
                }
                profile?.currentLevel?.let { level ->
                    ToeicChip(
                        text = stringResource(id = level.labelRes),
                        style = ToeicChipStyle.NOTICE,
                    )
                }
            }
        }
    }
}

@Composable
private fun DevToolsSection(devTools: DevTools) {
    ToeicListSection(title = stringResource(id = R.string.dev_section_title)) {
        ToeicListRow(
            iconRes = R.drawable.ic_bug,
            label = stringResource(id = R.string.dev_action_test_crash),
            supportingText = stringResource(id = R.string.dev_body_test_crash),
            iconBackground = BlushTint,
            onClick = devTools.onTestCrash,
        )
        ToeicListDivider()
        ToeicListRow(
            iconRes = R.drawable.ic_info,
            label = stringResource(id = R.string.dev_action_test_non_fatal),
            supportingText = stringResource(id = R.string.dev_body_test_non_fatal),
            onClick = devTools.onTestNonFatal,
        )
        ToeicListDivider()
        ToeicListRow(
            iconRes = R.drawable.ic_copy,
            label = stringResource(id = R.string.dev_action_copy_push_token),
            supportingText = stringResource(id = R.string.dev_body_copy_push_token),
            iconBackground = Pearl,
            onClick = devTools.onCopyPushToken,
        )
    }
}

/** Guests have no account yet, so the first card invites them to make one. */
@Composable
private fun GuestCard(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
) {
    ToeicCard(modifier = Modifier.fillMaxWidth()) {
        SignInRequiredState(
            onSignUpClick = onSignUpClick,
            onSignInClick = onSignInClick,
            title = stringResource(id = R.string.profile_title_guest),
            body = stringResource(id = R.string.profile_body_guest),
            fullScreen = false,
            mascotResId = R.drawable.oy2_warm,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, heightDp = 1200)
@Composable
private fun ProfileContentMemberPreview() {
    ToeicSpaceTheme {
        ProfileContent(
            viewer = Viewer.Member(fullName = "Nguyễn Minh Anh", email = "minhanh@example.com"),
            profile =
                UserProfile(
                    fullName = "Nguyễn Minh Anh",
                    email = "minhanh@example.com",
                    targetScore = 650,
                    currentLevel = EnglishLevel.INTERMEDIATE,
                ),
            versionName = "1.0",
            onSignUpClick = {},
            onSignInClick = {},
            onAccountInfoClick = {},
            onChangePasswordClick = {},
            onComingSoon = {},
            onSignOutClick = {},
            notificationsEnabled = true,
            devTools = DevTools(onTestCrash = {}, onTestNonFatal = {}, onCopyPushToken = {}),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, heightDp = 900)
@Composable
private fun ProfileContentGuestPreview() {
    ToeicSpaceTheme {
        ProfileContent(
            viewer = Viewer.Guest,
            profile = null,
            versionName = "1.0",
            onSignUpClick = {},
            onSignInClick = {},
            onAccountInfoClick = {},
            onChangePasswordClick = {},
            onComingSoon = {},
            onSignOutClick = {},
        )
    }
}
