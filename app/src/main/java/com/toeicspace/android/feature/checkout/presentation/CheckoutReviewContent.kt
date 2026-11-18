package com.toeicspace.android.feature.checkout.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicTextField
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.ExamSpeakingWritingContent
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PriceGreen
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.feature.checkout.domain.DiscountSource
import com.toeicspace.android.feature.checkout.domain.OrderAmounts
import com.toeicspace.android.feature.checkout.domain.PricePreview
import com.toeicspace.android.feature.checkout.presentation.components.StatusPill
import com.toeicspace.android.feature.checkout.presentation.components.SummaryRow
import com.toeicspace.android.feature.course.presentation.components.amountText
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val PromotionDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM")

/** Step one: what the order will cost, an optional coupon, and the button that creates it. */
@Composable
internal fun ReviewContent(
    preview: PricePreview,
    state: CheckoutUiState,
    actions: CheckoutActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        OrderSummaryCard(
            courseTitle = state.courseTitle,
            amounts = preview.amounts,
            promotionExpiresAt = preview.promotionExpiresAt,
            appliedCoupon = state.appliedCoupon,
        )
        CouponCard(state = state, preview = preview, actions = actions)
        val free = preview.amounts.finalAmount <= 0
        PrimaryButton(
            text =
                stringResource(
                    if (free) {
                        R.string.checkout_action_create_order_free
                    } else {
                        R.string.checkout_action_create_order
                    },
                ),
            onClick = actions.onCreateOrder,
            isLoading = state.isCreatingOrder,
            loadingText = stringResource(R.string.checkout_label_creating_order),
            enabled = !state.isApplyingCoupon,
            shape = PillShape,
            modifier = Modifier.fillMaxWidth().testTag(CheckoutTestTags.CREATE_ORDER),
        )
        Text(
            text = stringResource(R.string.checkout_note_review),
            style = AppTextStyles.HelperText,
            color = HintText,
        )
    }
}

/** Original price, the discount Payment chose, and the total in large green, as on the web. */
@Composable
internal fun OrderSummaryCard(
    courseTitle: String?,
    amounts: OrderAmounts,
    modifier: Modifier = Modifier,
    promotionExpiresAt: Instant? = null,
    appliedCoupon: String? = null,
) {
    ToeicCard(modifier = modifier.fillMaxWidth(), itemSpacing = Spacing.smd) {
        Text(
            text = stringResource(R.string.checkout_section_summary),
            style = AppTextStyles.SectionTitle,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (courseTitle != null) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.space2)) {
                Text(
                    text = stringResource(R.string.checkout_label_course),
                    style = AppTextStyles.HelperText,
                    color = HintText,
                )
                Text(
                    text = courseTitle,
                    style = AppTextStyles.CourseTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        SummaryRow(
            label = stringResource(R.string.checkout_label_original),
            value = amountText(amounts.originalAmount, amounts.currency),
        )
        if (amounts.hasDiscount) {
            SummaryRow(
                label = amounts.discountLabel(appliedCoupon),
                value =
                    stringResource(
                        R.string.checkout_label_discount_amount,
                        amountText(amounts.discountAmount, amounts.currency),
                    ),
                valueColor = ExamSpeakingWritingContent,
            )
            if (amounts.discountSource == DiscountSource.PROMOTION && promotionExpiresAt != null) {
                Text(
                    text =
                        stringResource(
                            R.string.checkout_label_promotion_until,
                            PromotionDateFormat
                                .withZone(
                                    ZoneId.systemDefault(),
                                ).format(promotionExpiresAt),
                        ),
                    style = AppTextStyles.HelperText,
                    color = HintText,
                )
            }
        }
        HorizontalDivider(color = SubtleBorder)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = stringResource(R.string.checkout_label_total),
                style = AppTextStyles.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            Column(horizontalAlignment = Alignment.End) {
                if (amounts.hasDiscount) {
                    Text(
                        text = amountText(amounts.originalAmount, amounts.currency),
                        style = AppTextStyles.HelperText,
                        color = HintText,
                        textDecoration = TextDecoration.LineThrough,
                    )
                }
                Text(
                    text = amountText(amounts.finalAmount, amounts.currency),
                    style = AppTextStyles.PriceLarge,
                    color = PriceGreen,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun OrderAmounts.discountLabel(appliedCoupon: String?): String =
    when (discountSource) {
        DiscountSource.PROMOTION ->
            promotionName?.let { stringResource(R.string.checkout_label_discount_promotion, it) }
                ?: stringResource(R.string.checkout_label_discount_promotion_plain)
        DiscountSource.COUPON, DiscountSource.NONE ->
            stringResource(
                R.string.checkout_label_discount_coupon,
                couponCode ?: appliedCoupon.orEmpty(),
            )
    }

@Composable
private fun CouponCard(
    state: CheckoutUiState,
    preview: PricePreview,
    actions: CheckoutActions,
) {
    ToeicCard(modifier = Modifier.fillMaxWidth(), itemSpacing = Spacing.smd) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_ticket_percent),
                contentDescription = null,
                tint = ExamSpeakingWritingContent,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = stringResource(R.string.checkout_section_coupon),
                style = AppTextStyles.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        val applied = state.appliedCoupon
        if (applied != null) {
            val promotionWon = preview.amounts.discountSource == DiscountSource.PROMOTION
            StatusPill(
                text =
                    stringResource(
                        if (promotionWon) {
                            R.string.checkout_msg_promotion_better
                        } else {
                            R.string.checkout_msg_coupon_applied
                        },
                        applied,
                    ),
                iconRes = R.drawable.ic_check,
                tone = MintTone,
            )
            SecondaryButton(
                text = stringResource(R.string.checkout_action_remove_coupon),
                onClick = actions.onRemoveCoupon,
                isLoading = state.isApplyingCoupon,
                enabled = !state.isCreatingOrder,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            ToeicTextField(
                value = state.couponInput,
                onValueChange = actions.onCouponInputChange,
                label = stringResource(R.string.checkout_label_coupon),
                enabled = !state.isApplyingCoupon && !state.isCreatingOrder,
                errorText = state.couponErrorRes?.let { stringResource(it) },
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { actions.onApplyCoupon() }),
                testTag = CheckoutTestTags.COUPON,
            )
            SecondaryButton(
                text = stringResource(R.string.checkout_action_apply_coupon),
                onClick = actions.onApplyCoupon,
                isLoading = state.isApplyingCoupon,
                enabled = state.couponInput.isNotBlank() && !state.isCreatingOrder,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
