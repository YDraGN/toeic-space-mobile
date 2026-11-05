package com.toeicspace.android.feature.auth.presentation.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.AppTopBar
import com.toeicspace.android.core.ui.components.BrandLogo
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PrimaryBorder
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow

// Forms stay readable on tablets and in landscape.
private val MaxContentWidth = 480.dp

/** Shared layout of the auth screens: brand top bar, mascot, title and a white form card. */
private val FormVerticalPadding = Spacing.md + Spacing.xs

@Composable
internal fun AuthScaffold(
    title: String,
    @DrawableRes mascotRes: Int,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    footer: @Composable ColumnScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_splash_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .imePadding(),
        ) {
            AppTopBar(
                leadingContent = {
                    if (onBackClick != null) {
                        ToeicIconButton(
                            onClick = onBackClick,
                            contentDescription = stringResource(id = R.string.common_action_back),
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_chevron_left),
                                contentDescription = null,
                                tint = OceanicInk,
                            )
                        }
                    } else {
                        BrandLogo()
                    }
                },
                trailingContent = { if (onBackClick != null) BrandLogo() },
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(
                    modifier = Modifier.widthIn(max = MaxContentWidth),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    MascotView(mascotResId = mascotRes, size = 120.dp)
                    Spacer(modifier = Modifier.height(Spacing.smd))
                    Text(
                        text = title,
                        style =
                            MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = OceanicInk,
                        textAlign = TextAlign.Center,
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OceanicInk.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    Surface(
                        modifier = Modifier.fillMaxWidth().softShadow(ToeicSpaceShapes.large),
                        shape = ToeicSpaceShapes.large,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, CardBorder),
                    ) {
                        // 16dp sides and 12dp between fields, close to Google and Material sign-in
                        // forms; a helper line adds its own 4dp on top.
                        Column(
                            modifier =
                                Modifier.padding(
                                    horizontal = Spacing.md,
                                    vertical = FormVerticalPadding,
                                ),
                            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
                            content = content,
                        )
                    }
                    Spacer(modifier = Modifier.height(Spacing.md))
                    footer()
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
            }
        }
    }
}

/** Inline message at the top of a form; [isError] switches between error and success colors. */
@Composable
internal fun AuthMessageBanner(
    text: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val containerColor = if (isError) colors.errorContainer else colors.secondaryContainer
    val contentColor = if (isError) colors.onErrorContainer else colors.onSecondaryContainer
    Text(
        text = text,
        style = AppTextStyles.Caption,
        color = contentColor,
        modifier =
            modifier
                .fillMaxWidth()
                .clip(ToeicSpaceShapes.small)
                .background(containerColor)
                .padding(horizontal = Spacing.smd, vertical = Spacing.sm),
    )
}

/** "Prompt? Action" row used under the form, e.g. "Chưa có tài khoản? Đăng ký ngay". */
@Composable
internal fun AuthPromptLink(
    prompt: String,
    action: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyMedium,
            color = OceanicInk.copy(alpha = 0.75f),
        )
        AuthTextButton(text = action, onClick = onClick, enabled = enabled)
    }
}

@Composable
internal fun AuthTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = if (enabled) PrimaryBorder else OceanicInk.copy(alpha = 0.4f),
        modifier =
            modifier
                .clip(PillShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
    )
}

@Composable
internal fun GuestEntryLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val contentColor = OceanicInk.copy(alpha = 0.75f)
    Row(
        modifier =
            modifier
                .clip(PillShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = Spacing.sm, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_action_continue_as_guest),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f, fill = false),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = contentColor,
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScaffoldPreview() {
    ToeicSpaceTheme {
        AuthScaffold(
            title = "Đăng nhập",
            subtitle = "Chào mừng bạn quay lại không gian học!",
            mascotRes = R.drawable.oy2_hello,
            onBackClick = {},
        ) {
            AuthMessageBanner(text = "Email đã được xác thực.", isError = false)
            AuthMessageBanner(text = "Email hoặc mật khẩu chưa đúng", isError = true)
        }
    }
}
