package com.toeicspace.android.feature.profile.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.components.DetailScaffold
import com.toeicspace.android.core.ui.components.EditableAvatar
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicFilterChip
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.components.ToeicListDivider
import com.toeicspace.android.core.ui.components.ToeicListRow
import com.toeicspace.android.core.ui.components.ToeicListSection
import com.toeicspace.android.core.ui.components.ToeicTextField
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Pearl
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.auth.domain.FieldError
import com.toeicspace.android.feature.auth.presentation.common.messageRes
import com.toeicspace.android.feature.profile.domain.EnglishLevel
import com.toeicspace.android.feature.profile.domain.Gender
import com.toeicspace.android.feature.profile.domain.ProfileFormValidator
import com.toeicspace.android.feature.profile.domain.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
internal fun AccountInfoRoute(
    onBackClick: () -> Unit,
    viewModel: AccountInfoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Only members reach this screen; if the session ends while it is open, leave.
    LaunchedEffect(uiState.isLoading, uiState.profile) {
        if (!uiState.isLoading && uiState.profile == null) onBackClick()
    }
    val profile = uiState.profile ?: return

    AccountInfoScreen(
        profile = profile,
        form = uiState.form,
        isSaving = uiState.isSaving,
        message = uiState.message,
        isUploadingAvatar = uiState.isUploadingAvatar,
        onBackClick = onBackClick,
        actions =
            AccountInfoActions(
                onEditClick = viewModel::onEditClick,
                onCancelEdit = viewModel::onCancelEdit,
                onFullNameChange = viewModel::onFullNameChange,
                onPhoneChange = viewModel::onPhoneChange,
                onDateOfBirthChange = viewModel::onDateOfBirthChange,
                onGenderChange = viewModel::onGenderChange,
                onBiographyChange = viewModel::onBiographyChange,
                onTargetScoreChange = viewModel::onTargetScoreChange,
                onLevelChange = viewModel::onLevelChange,
                onSaveClick = viewModel::onSaveClick,
                onMessageShown = viewModel::onMessageShown,
                onAvatarPicked = viewModel::onAvatarPicked,
            ),
    )
}

/** Everything the screen can ask the view model to do, so previews can pass no-ops in one go. */
internal data class AccountInfoActions(
    val onEditClick: () -> Unit = {},
    val onCancelEdit: () -> Unit = {},
    val onFullNameChange: (String) -> Unit = {},
    val onPhoneChange: (String) -> Unit = {},
    val onDateOfBirthChange: (LocalDate) -> Unit = {},
    val onGenderChange: (Gender) -> Unit = {},
    val onBiographyChange: (String) -> Unit = {},
    val onTargetScoreChange: (Int) -> Unit = {},
    val onLevelChange: (EnglishLevel) -> Unit = {},
    val onSaveClick: () -> Unit = {},
    val onMessageShown: () -> Unit = {},
    val onAvatarPicked: (String) -> Unit = {},
)

/**
 * Account details. Viewing shows every field of the profile; the pencil opens one form for all of
 * them, sent together because `PUT /users/me` replaces the whole profile. The avatar has its own
 * flow: picking a photo uploads it at once and saves the new URL, independent of the form.
 */
@Composable
internal fun AccountInfoScreen(
    profile: UserProfile,
    form: ProfileForm?,
    isSaving: Boolean,
    message: ProfileMessage?,
    onBackClick: () -> Unit,
    actions: AccountInfoActions,
    modifier: Modifier = Modifier,
    isUploadingAvatar: Boolean = false,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isEditing = form != null

    val messageText = message?.let { stringResource(id = it.messageRes()) }
    LaunchedEffect(message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            actions.onMessageShown()
        }
    }
    BackHandler(enabled = isEditing, onBack = actions.onCancelEdit)

    DetailScaffold(
        title = stringResource(id = R.string.profile_action_account_info),
        onBackClick = if (isEditing) actions.onCancelEdit else onBackClick,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        actions = {
            if (!isEditing) {
                ToeicIconButton(
                    onClick = actions.onEditClick,
                    contentDescription = stringResource(id = R.string.account_info_cd_edit),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_pencil),
                        contentDescription = null,
                    )
                }
            }
        },
    ) {
        AvatarHeader(
            imageUrl = profile.avatarUrl,
            isUploading = isUploadingAvatar,
            onPhotoPicked = actions.onAvatarPicked,
        )
        if (form == null) {
            ProfileDetails(profile = profile)
        } else {
            ProfileEditForm(
                email = profile.email,
                form = form,
                isSaving = isSaving,
                actions = actions,
            )
        }
    }
}

@Composable
private fun AvatarHeader(
    imageUrl: String?,
    isUploading: Boolean,
    onPhotoPicked: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        EditableAvatar(
            onPhotoPicked = { uri -> if (uri != null) onPhotoPicked(uri.toString()) },
            imageUrl = imageUrl,
            isUploading = isUploading,
        )
        Text(
            text = stringResource(id = R.string.account_info_hint_avatar),
            style = AppTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileDetails(profile: UserProfile) {
    val empty = stringResource(id = R.string.account_info_value_empty)

    ToeicListSection(title = stringResource(id = R.string.account_info_section_personal)) {
        InfoRow(R.drawable.ic_account, R.string.auth_label_full_name, profile.fullName)
        ToeicListDivider()
        InfoRow(R.drawable.ic_mail, R.string.auth_label_email, profile.email)
        ToeicListDivider()
        InfoRow(R.drawable.ic_phone, R.string.auth_label_phone, profile.phone ?: empty)
        ToeicListDivider()
        InfoRow(
            R.drawable.ic_calendar,
            R.string.account_info_label_date_of_birth,
            profile.dateOfBirth?.toDisplayDate() ?: empty,
        )
        ToeicListDivider()
        InfoRow(
            R.drawable.ic_users,
            R.string.account_info_label_gender,
            profile.gender?.let { stringResource(id = it.labelRes) } ?: empty,
        )
        ToeicListDivider()
        InfoRow(
            R.drawable.ic_message,
            R.string.account_info_label_biography,
            profile.biography ?: empty,
        )
    }
    ToeicListSection(title = stringResource(id = R.string.account_info_section_goal)) {
        InfoRow(
            R.drawable.ic_target,
            R.string.account_info_label_target_score,
            profile.targetScore?.let {
                stringResource(
                    id = R.string.account_info_value_target_score,
                    it,
                )
            }
                ?: empty,
        )
        ToeicListDivider()
        InfoRow(
            R.drawable.ic_bar_chart,
            R.string.account_info_label_level,
            profile.currentLevel?.let { stringResource(id = it.labelRes) } ?: empty,
        )
    }
}

@Composable
private fun InfoRow(
    iconRes: Int,
    labelRes: Int,
    value: String,
) {
    ToeicListRow(iconRes = iconRes, overline = stringResource(id = labelRes), label = value)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileEditForm(
    email: String,
    form: ProfileForm,
    isSaving: Boolean,
    actions: AccountInfoActions,
) {
    var isPickingDate by rememberSaveable { mutableStateOf(false) }

    ToeicCard(modifier = Modifier.fillMaxWidth()) {
        ToeicTextField(
            value = form.fullName,
            onValueChange = actions.onFullNameChange,
            label = stringResource(id = R.string.auth_label_full_name),
            isRequired = true,
            errorText = form.fullNameError?.let { stringResource(id = it.messageRes()) },
            testTag = TestTags.PROFILE_FULL_NAME,
        )
        ToeicTextField(
            value = email,
            onValueChange = {},
            label = stringResource(id = R.string.auth_label_email),
            enabled = false,
            supportingText = stringResource(id = R.string.account_info_hint_email),
        )
        ToeicTextField(
            value = form.phone,
            onValueChange = actions.onPhoneChange,
            label = stringResource(id = R.string.auth_label_phone),
            errorText = form.phoneError?.let { stringResource(id = it.messageRes()) },
            keyboardType = KeyboardType.Phone,
            testTag = TestTags.PROFILE_PHONE,
        )
        ToeicTextField(
            value = form.biography,
            onValueChange = actions.onBiographyChange,
            label = stringResource(id = R.string.account_info_label_biography),
            errorText = form.biographyError?.let { stringResource(id = it.messageRes()) },
            supportingText = stringResource(id = R.string.account_info_hint_biography),
            singleLine = false,
            imeAction = ImeAction.Default,
            keyboardActions = KeyboardActions.Default,
            testTag = TestTags.PROFILE_BIOGRAPHY,
        )
    }

    ToeicListSection {
        ToeicListRow(
            iconRes = R.drawable.ic_calendar,
            overline = stringResource(id = R.string.account_info_label_date_of_birth),
            label =
                form.dateOfBirth?.toDisplayDate()
                    ?: stringResource(id = R.string.account_info_value_empty),
            iconBackground = Pearl,
            onClick = { isPickingDate = true },
        )
    }

    ToeicCard(modifier = Modifier.fillMaxWidth()) {
        ChoiceGroup(label = stringResource(id = R.string.account_info_label_gender)) {
            Gender.entries.forEach { gender ->
                ToeicFilterChip(
                    text = stringResource(id = gender.labelRes),
                    selected = form.gender == gender,
                    onClick = { actions.onGenderChange(gender) },
                )
            }
        }
        ChoiceGroup(label = stringResource(id = R.string.account_info_label_target_score)) {
            ProfileFormValidator.TargetScoreOptions.forEach { score ->
                ToeicFilterChip(
                    text = stringResource(id = R.string.account_info_value_target_score, score),
                    selected = form.targetScore == score,
                    onClick = { actions.onTargetScoreChange(score) },
                )
            }
        }
        ChoiceGroup(label = stringResource(id = R.string.account_info_label_level)) {
            EnglishLevel.entries.forEach { level ->
                ToeicFilterChip(
                    text = stringResource(id = level.labelRes),
                    selected = form.currentLevel == level,
                    onClick = { actions.onLevelChange(level) },
                )
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.smd)) {
        PrimaryButton(
            text = stringResource(id = R.string.account_info_action_save),
            onClick = actions.onSaveClick,
            modifier = Modifier.fillMaxWidth(),
            isLoading = isSaving,
            loadingText = stringResource(id = R.string.account_info_action_saving),
        )
        SecondaryButton(
            text = stringResource(id = R.string.account_info_action_cancel),
            onClick = actions.onCancelEdit,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving,
        )
    }

    if (isPickingDate) {
        DateOfBirthDialog(
            initial = form.dateOfBirth,
            onPicked = { date ->
                actions.onDateOfBirthChange(date)
                isPickingDate = false
            },
            onDismiss = { isPickingDate = false },
        )
    }
}

/** A labelled row of chips for a field with a few fixed answers. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChoiceGroup(
    label: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) { content() }
    }
}

/** Only past dates can be picked, matching the backend rule that a birthday is in the past. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateOfBirthDialog(
    initial: LocalDate?,
    onPicked: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val todayMillis =
        remember {
            LocalDate
                .now()
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli()
        }
    val state =
        rememberDatePickerState(
            initialSelectedDateMillis =
                initial
                    ?.atStartOfDay()
                    ?.toInstant(
                        ZoneOffset.UTC,
                    )?.toEpochMilli(),
            selectableDates =
                object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis < todayMillis
                },
        )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onPicked(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    } ?: onDismiss()
                },
            ) { Text(text = stringResource(id = R.string.account_info_action_pick_date)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.account_info_action_cancel))
            }
        },
    ) { DatePicker(state = state) }
}

private val PreviewProfile =
    UserProfile(
        fullName = "Nguyễn Minh Anh",
        email = "minhanh@example.com",
        phone = "0905123456",
        dateOfBirth = LocalDate.of(2004, 5, 12),
        biography = "Muốn đạt 650+ trước kỳ thực tập mùa hè.",
        targetScore = 650,
        currentLevel = EnglishLevel.INTERMEDIATE,
    )

@Preview(heightDp = 1000)
@Composable
private fun AccountInfoViewPreview() {
    ToeicSpaceTheme {
        AccountInfoScreen(
            profile = PreviewProfile,
            form = null,
            isSaving = false,
            message = null,
            onBackClick = {},
            actions = AccountInfoActions(),
        )
    }
}

@Preview(heightDp = 1300)
@Composable
private fun AccountInfoEditPreview() {
    ToeicSpaceTheme {
        AccountInfoScreen(
            profile = PreviewProfile,
            form =
                ProfileForm(
                    fullName = "Nguyễn Minh Anh",
                    phone = "0905",
                    dateOfBirth = PreviewProfile.dateOfBirth,
                    gender = null,
                    biography = "",
                    targetScore = 650,
                    currentLevel = EnglishLevel.INTERMEDIATE,
                    phoneError = FieldError.InvalidPhone,
                ),
            isSaving = false,
            message = null,
            onBackClick = {},
            actions = AccountInfoActions(),
        )
    }
}
