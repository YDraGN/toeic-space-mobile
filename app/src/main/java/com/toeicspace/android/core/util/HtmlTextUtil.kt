package com.toeicspace.android.core.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.fromHtml

/**
 * Parses simple HTML (e.g. `<b>`) from a string resource and tints bold ranges with
 * [highlightColor].
 */
fun String.htmlToAnnotatedString(highlightColor: Color): AnnotatedString {
    val parsed = AnnotatedString.fromHtml(this)
    return buildAnnotatedString {
        append(parsed)
        parsed.spanStyles
            .filter { it.item.fontWeight == FontWeight.Bold }
            .forEach { range ->
                addStyle(SpanStyle(color = highlightColor), range.start, range.end)
            }
    }
}
