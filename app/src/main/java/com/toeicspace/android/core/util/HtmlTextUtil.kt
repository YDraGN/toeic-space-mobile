package com.toeicspace.android.core.util

import android.graphics.Typeface
import android.text.Html
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

fun CharSequence.parseHtmlToAnnotatedString(highlightColor: Color): AnnotatedString {
    val spanned: Spanned =
        this as? Spanned ?: Html.fromHtml(this.toString(), Html.FROM_HTML_MODE_COMPACT)
    return buildAnnotatedString {
        append(spanned.toString())
        val styleSpans = spanned.getSpans(0, spanned.length, StyleSpan::class.java)
        styleSpans.forEach { span ->
            val start = spanned.getSpanStart(span)
            val end = spanned.getSpanEnd(span)
            if (span.style == Typeface.BOLD) {
                addStyle(
                    style =
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = highlightColor,
                        ),
                    start = start,
                    end = end,
                )
            }
        }
    }
}
