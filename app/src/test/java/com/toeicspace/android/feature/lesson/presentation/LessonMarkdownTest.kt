package com.toeicspace.android.feature.lesson.presentation

import org.junit.Assert.assertEquals
import org.junit.Test

class LessonMarkdownTest {
    @Test
    fun `reads headings, paragraphs, lists and quotes`() {
        val blocks =
            parseMarkdown(
                """
                # Mục tiêu ##
                Dòng một
                dòng hai

                - Who
                * When
                2. Where
                > Mẹo nhỏ
                ---
                #### Sâu
                """.trimIndent(),
            )

        assertEquals(
            listOf(
                MarkdownBlock.Heading(1, "Mục tiêu"),
                MarkdownBlock.Paragraph("Dòng một dòng hai"),
                MarkdownBlock.ListItem("•", "Who"),
                MarkdownBlock.ListItem("•", "When"),
                MarkdownBlock.ListItem("2.", "Where"),
                MarkdownBlock.Quote("Mẹo nhỏ"),
                MarkdownBlock.Heading(3, "Sâu"),
            ),
            blocks,
        )
    }

    @Test
    fun `styles bold, italic, code and links`() {
        assertEquals(
            listOf(
                MarkdownSpan("Nghe "),
                MarkdownSpan("kỹ", bold = true),
                MarkdownSpan(" và "),
                MarkdownSpan("chậm", italic = true),
                MarkdownSpan(", gõ "),
                MarkdownSpan("who", code = true),
                MarkdownSpan(", xem "),
                MarkdownSpan("đề", link = true),
            ),
            parseInline("Nghe **kỹ** và _chậm_, gõ `who`, xem [đề](https://x.y)"),
        )
    }

    @Test
    fun `keeps lone markers, snake case and escapes as text`() {
        assertEquals(
            listOf(MarkdownSpan("2 * 3 = 6 và part_two *x")),
            parseInline("2 * 3 = 6 và part_two \\*x"),
        )
    }

    @Test
    fun `drops images but keeps their place`() {
        assertEquals(
            listOf(MarkdownSpan("Xem "), MarkdownSpan(" nhé")),
            parseInline("Xem ![hình](a.png) nhé"),
        )
    }
}
