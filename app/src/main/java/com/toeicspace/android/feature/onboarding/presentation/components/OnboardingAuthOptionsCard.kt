package com.toeicspace.android.feature.onboarding.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.theme.OceanicInk
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.SeaTurquoise
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceShapes
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme

/** Last onboarding page: sign up, sign in or continue as a guest. */
@Composable
internal fun OnboardingAuthOptionsCard(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onContinueAsGuestClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        shape = ToeicSpaceShapes.large,
        color = Color.White,
        shadowElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_subtitle_choose_method),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = SeaTurquoise,
                        lineHeight = 22.sp,
                    ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(20.dp))
            PrimaryButton(
                text = stringResource(id = R.string.onboarding_action_sign_up),
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_sign_up),
                        contentDescription = null,
                    )
                },
            )
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(
                text = stringResource(id = R.string.onboarding_action_sign_in),
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_log_in),
                        contentDescription = null,
                    )
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            GuestLink(onClick = onContinueAsGuestClick, enabled = enabled)
        }
    }
}

@Composable
private fun GuestLink(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val contentColor = OceanicInk.copy(alpha = 0.75f)
    Row(
        modifier =
            modifier
                .clip(PillShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = Spacing.sm, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.onboarding_action_continue_as_guest),
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                ),
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFDCEBFF)
@Composable
private fun OnboardingAuthOptionsCardPreview() {
    ToeicSpaceTheme {
        OnboardingAuthOptionsCard(
            onSignUpClick = {},
            onSignInClick = {},
            onContinueAsGuestClick = {},
            modifier = Modifier.padding(Spacing.md),
        )
    }
}
