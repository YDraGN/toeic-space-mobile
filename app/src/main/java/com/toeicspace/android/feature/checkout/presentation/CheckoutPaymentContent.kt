package com.toeicspace.android.feature.checkout.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.BlushTone
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.PriceGreen
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.feature.checkout.domain.BankTransfer
import com.toeicspace.android.feature.checkout.domain.CheckoutOrder
import com.toeicspace.android.feature.checkout.presentation.components.CopyableField
import com.toeicspace.android.feature.checkout.presentation.components.InfoNote
import com.toeicspace.android.feature.checkout.presentation.components.StatusPill
import com.toeicspace.android.feature.course.presentation.components.amountText
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.util.Locale

private val QrMaxSize = 300.dp
private const val SECONDS_PER_MINUTE = 60
private const val TICK_MS = 1_000L

/** What a copy button puts on the clipboard, and the snackbar it shows afterwards. */
internal data class CopyRequest(
    val label: String,
    val value: String,
    val messageRes: Int,
)

/**
 * Step two: a pending order. Phones stack the QR above the bank details; wider windows put them side
 * by side so both fit without scrolling.
 */
@Composable
internal fun PaymentContent(
    order: CheckoutOrder,
    transfer: BankTransfer,
    state: CheckoutUiState,
    actions: CheckoutActions,
    sideBySide: Boolean,
    onCopy: (CopyRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        PaymentStatusHeader(order = order)
        if (sideBySide) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                QrCard(
                    order = order,
                    transfer = transfer,
                    state = state,
                    actions = actions,
                    modifier = Modifier.weight(1f),
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    TransferDetailsCard(
                        transfer = transfer,
                        currency = order.amounts.currency,
                        onCopy = onCopy,
                    )
                    CancelOrderButton(state = state, actions = actions)
                }
            }
        } else {
            QrCard(order = order, transfer = transfer, state = state, actions = actions)
            TransferDetailsCard(
                transfer = transfer,
                currency = order.amounts.currency,
                onCopy = onCopy,
            )
            CancelOrderButton(state = state, actions = actions)
        }
    }
}

/** The order code, the countdown to `expiresAt` and a reminder that the status updates by itself. */
@Composable
private fun PaymentStatusHeader(order: CheckoutOrder) {
    val remaining = rememberRemainingSeconds(order.expiresAt)
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = stringResource(R.string.checkout_label_order_code, order.orderCode),
            style = AppTextStyles.HelperText,
            color = HintText,
        )
        if (remaining > 0) {
            StatusPill(
                text =
                    stringResource(
                        R.string.checkout_label_countdown,
                        formatCountdown(remaining),
                    ),
                iconRes = R.drawable.ic_clock,
                tone = SkyTone,
                modifier = Modifier.testTag(CheckoutTestTags.COUNTDOWN),
            )
        } else {
            StatusPill(
                text = stringResource(R.string.checkout_label_countdown_over),
                iconRes = R.drawable.ic_clock,
                tone = BlushTone,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
            Text(
                text = stringResource(R.string.checkout_label_waiting),
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QrCard(
    order: CheckoutOrder,
    transfer: BankTransfer,
    state: CheckoutUiState,
    actions: CheckoutActions,
    modifier: Modifier = Modifier,
) {
    ToeicCard(modifier = modifier.fillMaxWidth(), itemSpacing = Spacing.smd) {
        Text(
            text = stringResource(R.string.checkout_section_transfer),
            style = AppTextStyles.SectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.checkout_hint_scan),
            style = AppTextStyles.ReadingBody,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            modifier =
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .widthIn(max = QrMaxSize)
                    .fillMaxWidth()
                    .aspectRatio(1f),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, CardBorder),
        ) {
            // The image exactly as Payment built it (vietqr.app, SePay's documented format).
            SubcomposeAsyncImage(
                model = transfer.qrUrl,
                contentDescription = stringResource(R.string.checkout_cd_qr, order.orderCode),
                modifier = Modifier.padding(Spacing.sm).testTag(CheckoutTestTags.QR),
                loading = {
                    QrPlaceholder(
                        text = stringResource(R.string.checkout_label_qr_loading),
                        loading = true,
                    )
                },
                error = {
                    QrPlaceholder(
                        text = stringResource(R.string.checkout_error_qr_load),
                        loading = false,
                    )
                },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SecondaryButton(
                text = stringResource(R.string.checkout_action_save_qr),
                onClick = actions.onSaveQr,
                isLoading = state.isSavingQr,
                modifier = Modifier.weight(1f),
                leadingIcon = { ButtonIcon(R.drawable.ic_download) },
            )
            SecondaryButton(
                text = stringResource(R.string.checkout_action_share_qr),
                onClick = actions.onShareQr,
                isLoading = state.isPreparingShare,
                modifier = Modifier.weight(1f),
                leadingIcon = { ButtonIcon(R.drawable.ic_share) },
            )
        }
    }
}

@Composable
private fun ButtonIcon(iconRes: Int) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun QrPlaceholder(
    text: String,
    loading: Boolean,
) {
    Box(modifier = Modifier.padding(Spacing.md), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (loading) CircularProgressIndicator()
            Text(
                text = text,
                style = AppTextStyles.Caption,
                color = HintText,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Bank, account, amount and memo, read-only, each with a copy button except the bank name. */
@Composable
private fun TransferDetailsCard(
    transfer: BankTransfer,
    currency: String,
    onCopy: (CopyRequest) -> Unit,
) {
    val accountLabel = stringResource(R.string.checkout_label_account)
    val amountLabel = stringResource(R.string.checkout_label_amount)
    val memoLabel = stringResource(R.string.checkout_label_memo)
    ToeicCard(modifier = Modifier.fillMaxWidth(), itemSpacing = Spacing.sm) {
        Text(
            text = stringResource(R.string.checkout_section_bank),
            style = AppTextStyles.SectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        CopyableField(label = stringResource(R.string.checkout_label_bank), value = transfer.bank)
        CopyableField(
            label = accountLabel,
            value = transfer.accountNumber,
            copyDescription = stringResource(R.string.checkout_cd_copy_account),
            onCopy = {
                onCopy(
                    CopyRequest(
                        accountLabel,
                        transfer.accountNumber,
                        R.string.checkout_msg_copied_account,
                    ),
                )
            },
        )
        CopyableField(
            label = amountLabel,
            value = amountText(transfer.amount, currency),
            valueStyle = AppTextStyles.PriceLarge,
            valueColor = PriceGreen,
            copyDescription = stringResource(R.string.checkout_cd_copy_amount),
            // Digits only: banking apps reject "499.000đ" in the amount field.
            onCopy = {
                onCopy(
                    CopyRequest(
                        amountLabel,
                        transfer.amount.toString(),
                        R.string.checkout_msg_copied_amount,
                    ),
                )
            },
        )
        CopyableField(
            label = memoLabel,
            value = transfer.memo,
            valueStyle = AppTextStyles.CourseTitle,
            copyDescription = stringResource(R.string.checkout_cd_copy_memo),
            onCopy = {
                onCopy(
                    CopyRequest(memoLabel, transfer.memo, R.string.checkout_msg_copied_memo),
                )
            },
        )
        InfoNote(text = stringResource(R.string.checkout_note_exact))
    }
}

@Composable
private fun CancelOrderButton(
    state: CheckoutUiState,
    actions: CheckoutActions,
) {
    SecondaryButton(
        text =
            stringResource(
                if (state.isCancelling) {
                    R.string.checkout_label_cancelling
                } else {
                    R.string.checkout_action_cancel_order
                },
            ),
        onClick = actions.onCancelClick,
        isLoading = state.isCancelling,
        modifier = Modifier.fillMaxWidth().testTag(CheckoutTestTags.CANCEL_ORDER),
    )
}

/** Seconds left until [expiresAt], ticking once a second and stopping at zero. */
@Composable
private fun rememberRemainingSeconds(expiresAt: Instant): Long {
    val remaining by produceState(initialValue = secondsUntil(expiresAt), expiresAt) {
        while (value > 0) {
            delay(TICK_MS)
            value = secondsUntil(expiresAt)
        }
    }
    return remaining
}

private fun secondsUntil(instant: Instant): Long =
    Duration.between(Instant.now(), instant).seconds.coerceAtLeast(0)

/** 754 to "12:34". */
internal fun formatCountdown(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val minutes = safe / SECONDS_PER_MINUTE
    val rest = safe % SECONDS_PER_MINUTE
    return String.format(Locale.ROOT, "%02d:%02d", minutes, rest)
}
