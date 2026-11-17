package com.toeicspace.android.feature.course.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.PriceGreen
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.domain.CoursePrice
import com.toeicspace.android.feature.course.domain.CoursePromotion
import kotlin.math.abs

private const val VND = "VND"

/** "499000" to "499.000": Vietnamese groups thousands with dots, whatever the device locale. */
internal fun formatCourseAmount(amount: Long): String {
    val digits = abs(amount).toString()
    val grouped =
        digits
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    return if (amount < 0) "-$grouped" else grouped
}

@Composable
internal fun amountText(
    amount: Long,
    currency: String,
): String =
    if (currency.equals(VND, ignoreCase = true)) {
        stringResource(R.string.course_price_vnd, formatCourseAmount(amount))
    } else {
        stringResource(R.string.course_price_other, formatCourseAmount(amount), currency)
    }

/**
 * What the learner pays, large and green as on the web catalog. With a promotion the original
 * price is struck through beside it.
 */
@Composable
fun CoursePriceText(
    price: CoursePrice,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTextStyles.Price,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                if (price.isFree) {
                    stringResource(R.string.course_price_free)
                } else {
                    amountText(price.payable, price.currency)
                },
            style = style,
            color = PriceGreen,
            maxLines = 1,
        )
        val promotion = price.promotion
        if (promotion != null && promotion.originalPrice > price.payable) {
            val original = amountText(promotion.originalPrice, price.currency)
            val description = stringResource(R.string.course_cd_original_price, original)
            Text(
                text = original,
                style = MaterialTheme.typography.bodySmall,
                color = InkSoft,
                textDecoration = TextDecoration.LineThrough,
                maxLines = 1,
                modifier = Modifier.semantics { contentDescription = description },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoursePriceTextPreview() {
    ToeicSpaceTheme {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            CoursePriceText(price = CoursePrice(amount = 0, currency = VND))
            CoursePriceText(price = CoursePrice(amount = 499_000, currency = VND))
            CoursePriceText(
                price =
                    CoursePrice(
                        amount = 499_000,
                        currency = VND,
                        promotion =
                            CoursePromotion(
                                "Black Friday",
                                499_000,
                                399_000,
                                expiresAt = null,
                            ),
                    ),
                style = AppTextStyles.PriceLarge,
            )
        }
    }
}
