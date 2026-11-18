package com.toeicspace.android.feature.lesson.presentation

/*
 * Teachers write lesson text in Markdown (the web shows it with a Markdown preview). The app only
 * needs to read it, so this covers what the editor produces: headings, paragraphs, bullet and
 * numbered lists, quotes, and bold / italic / code / links inside a line. Anything else is shown
 * as plain text rather than lost.
 */

sealed interface MarkdownBlock {
    data class Heading(
        val level: Int,
        val text: String,
    ) : MarkdownBlock

    data class Paragraph(
        val text: String,
    ) : MarkdownBlock

    /** [marker] is "•" for bullets or "3." for numbered items. */
    data class ListItem(
        val marker: String,
        val text: String,
    ) : MarkdownBlock

    data class Quote(
        val text: String,
    ) : MarkdownBlock
}

/** A run of text inside a block, with the styles that apply to it. */
data class MarkdownSpan(
    val text: String,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val code: Boolean = false,
    val link: Boolean = false,
)

private val HeadingPattern = Regex("^(#{1,6})\\s+(.*)$")
private val BulletPattern = Regex("^[-*+]\\s+(.*)$")
private val NumberedPattern = Regex("^(\\d+)[.)]\\s+(.*)$")
private val QuotePattern = Regex("^>\\s?(.*)$")
private val RulePattern = Regex("^([-*_])(\\s*\\1){2,}$")
private const val MAX_HEADING_LEVEL = 3

fun parseMarkdown(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraph = mutableListOf<String>()

    fun flushParagraph() {
        if (paragraph.isNotEmpty()) {
            blocks += MarkdownBlock.Paragraph(paragraph.joinToString(" "))
            paragraph.clear()
        }
    }

    markdown.lineSequence().map { it.trim() }.forEach { line ->
        val heading = HeadingPattern.matchEntire(line)
        val bullet = BulletPattern.matchEntire(line)
        val numbered = NumberedPattern.matchEntire(line)
        val quote = QuotePattern.matchEntire(line)
        when {
            line.isEmpty() ||
                RulePattern.matches(
                    line,
                ) ||
                line.startsWith("```") -> flushParagraph()
            heading != null -> {
                flushParagraph()
                val (hashes, text) = heading.destructured
                blocks +=
                    MarkdownBlock.Heading(
                        hashes.length.coerceAtMost(MAX_HEADING_LEVEL),
                        text.trimEnd('#', ' '),
                    )
            }
            bullet != null -> {
                flushParagraph()
                blocks += MarkdownBlock.ListItem("•", bullet.groupValues[1])
            }
            numbered != null -> {
                flushParagraph()
                blocks +=
                    MarkdownBlock.ListItem("${numbered.groupValues[1]}.", numbered.groupValues[2])
            }
            quote != null -> {
                flushParagraph()
                blocks += MarkdownBlock.Quote(quote.groupValues[1])
            }
            else -> paragraph += line
        }
    }
    flushParagraph()
    return blocks
}

/** Splits one line into styled runs: `code`, **bold**, *italic* / _italic_, [link](url), ![image](url). */
fun parseInline(text: String): List<MarkdownSpan> {
    val spans = mutableListOf<MarkdownSpan>()
    val plain = StringBuilder()
    var bold = false
    var italic = false
    var index = 0

    fun flush() {
        if (plain.isNotEmpty()) {
            spans += MarkdownSpan(plain.toString(), bold = bold, italic = italic)
            plain.clear()
        }
    }

    while (index < text.length) {
        val char = text[index]
        when {
            char == '\\' && index + 1 < text.length -> {
                plain.append(text[index + 1])
                index += 2
            }
            char == '`' && text.indexOf('`', index + 1) > index -> {
                flush()
                val end = text.indexOf('`', index + 1)
                spans += MarkdownSpan(text.substring(index + 1, end), code = true)
                index = end + 1
            }
            text.startsWith("**", index) || text.startsWith("__", index) -> {
                flush()
                bold = !bold
                index += 2
            }
            (char == '*' || char == '_') && isEmphasisMarker(text, index) -> {
                flush()
                italic = !italic
                index += 1
            }
            (char == '[' || text.startsWith("![", index)) && linkAt(text, index) != null -> {
                flush()
                val (label, end) = checkNotNull(linkAt(text, index))
                if (char ==
                    '['
                ) {
                    spans += MarkdownSpan(label, bold = bold, italic = italic, link = true)
                }
                index = end
            }
            else -> {
                plain.append(char)
                index += 1
            }
        }
    }
    flush()
    return spans
}

/** A lone `*` or `_` touching a word, so "2 * 3" and snake_case stay as they are. */
private fun isEmphasisMarker(
    text: String,
    index: Int,
): Boolean {
    val before = text.getOrNull(index - 1)
    val after = text.getOrNull(index + 1)
    val opens =
        after != null && !after.isWhitespace() && (before == null || !before.isLetterOrDigit())
    val closes =
        before != null && !before.isWhitespace() && (after == null || !after.isLetterOrDigit())
    return opens || closes
}

/** `[label](url)` or `![label](url)` at [index]: the label and the index just past it. */
private fun linkAt(
    text: String,
    index: Int,
): Pair<String, Int>? {
    val open = if (text[index] == '!') index + 1 else index
    val closeLabel = text.indexOf("](", open)
    if (closeLabel < 0) return null
    val closeUrl = text.indexOf(')', closeLabel + 2)
    if (closeUrl < 0) return null
    return text.substring(open + 1, closeLabel) to closeUrl + 1
}
