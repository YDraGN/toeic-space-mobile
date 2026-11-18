package com.toeicspace.android.feature.lesson.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.LilacTone
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.PeachTone
import com.toeicspace.android.core.ui.theme.QuietFill
import com.toeicspace.android.core.ui.theme.SecondaryButtonBackground
import com.toeicspace.android.core.ui.theme.SecondaryButtonContent
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.feature.course.presentation.detail.DetailSection
import com.toeicspace.android.feature.course.presentation.detail.formatLessonDuration
import com.toeicspace.android.feature.lesson.domain.LessonAttachment
import java.util.Locale

private val QuoteBarWidth = 3.dp
private val RowIconSize = 18.dp
private val MarkerWidth = 22.dp
private const val BYTES_PER_KB = 1024.0
private const val KB_PER_MB = 1024.0

/** The lesson text, read from Markdown, in a card like the course page sections. */
@Composable
internal fun LessonTextSection(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    val blocks = remember(markdown) { parseMarkdown(markdown) }
    DetailSection(
        title = stringResource(R.string.lesson_section_content),
        iconRes = R.drawable.ic_file_text,
        tone = SkyTone,
        modifier = modifier,
    ) {
        blocks.forEach { block -> MarkdownBlockView(block) }
    }
}

@Composable
private fun MarkdownBlockView(block: MarkdownBlock) {
    when (block) {
        is MarkdownBlock.Heading ->
            Text(
                text = block.text.toStyledText(),
                style =
                    when (block.level) {
                        1 -> MaterialTheme.typography.titleLarge
                        2 -> MaterialTheme.typography.titleMedium
                        else -> MaterialTheme.typography.titleSmall
                    },
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.xs).semantics { heading() },
            )
        is MarkdownBlock.Paragraph -> ReadingText(block.text)
        is MarkdownBlock.ListItem ->
            Row {
                Text(
                    text = block.marker,
                    style = AppTextStyles.ReadingBody,
                    color = SecondaryButtonContent,
                    modifier = Modifier.width(MarkerWidth),
                )
                ReadingText(block.text, modifier = Modifier.weight(1f))
            }
        is MarkdownBlock.Quote ->
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(QuietFill),
            ) {
                // Intrinsic height lets the bar follow the quote, however many lines it takes.
                Box(
                    modifier =
                        Modifier
                            .width(QuoteBarWidth)
                            .fillMaxHeight()
                            .background(LilacTone.content),
                )
                ReadingText(
                    block.text,
                    modifier = Modifier.padding(horizontal = Spacing.smd, vertical = Spacing.sm),
                )
            }
    }
}

@Composable
private fun ReadingText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.toStyledText(),
        style = AppTextStyles.ReadingBody,
        color = InkSoft,
        modifier = modifier,
    )
}

@Composable
private fun String.toStyledText(): AnnotatedString {
    val linkColor = SecondaryButtonContent
    val codeBackground = QuietFill
    // Unset fields of the default style, so the Text's own color shows through.
    val plain = TextStyle.Default
    return remember(this) {
        buildAnnotatedString {
            parseInline(this@toStyledText).forEach { span ->
                val style =
                    SpanStyle(
                        fontWeight = if (span.bold) FontWeight.Bold else null,
                        fontStyle = if (span.italic) FontStyle.Italic else null,
                        fontFamily = if (span.code) FontFamily.Monospace else null,
                        background = if (span.code) codeBackground else plain.background,
                        color = if (span.link) linkColor else plain.color,
                        textDecoration = if (span.link) TextDecoration.Underline else null,
                    )
                withStyle(style) { append(span.text) }
            }
        }
    }
}

/** Files to download; each opens in a Custom Tab with a fresh link. */
@Composable
internal fun AttachmentsSection(
    attachments: List<LessonAttachment>,
    openingAttachmentId: String?,
    onAttachmentClick: (attachmentId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailSection(
        title = stringResource(R.string.lesson_section_attachments),
        iconRes = R.drawable.ic_paperclip,
        tone = PeachTone,
        trailing = stringResource(R.string.lesson_label_attachment_count, attachments.size),
        modifier = modifier,
    ) {
        attachments.forEach { attachment ->
            val opening = attachment.id == openingAttachmentId
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(QuietFill)
                        .clickable(
                            enabled = openingAttachmentId == null,
                            role = Role.Button,
                            onClickLabel =
                                stringResource(
                                    R.string.lesson_cd_open_attachment,
                                    attachment.fileName,
                                ),
                            onClick = { onAttachmentClick(attachment.id) },
                        ).padding(Spacing.smd),
                horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_file_text),
                    contentDescription = null,
                    tint = PeachTone.content,
                    modifier = Modifier.size(RowIconSize),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attachment.fileName,
                        style = AppTextStyles.ListItemTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (attachment.fileSize > 0) {
                        Text(
                            text = attachment.fileSize.toSizeText(),
                            style = AppTextStyles.HelperText,
                            color = HintText,
                        )
                    }
                }
                if (opening) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(RowIconSize),
                        strokeWidth = Spacing.space2,
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_link),
                        contentDescription = null,
                        tint = SecondaryButtonContent,
                        modifier = Modifier.size(RowIconSize),
                    )
                }
            }
        }
    }
}

@Composable
private fun Long.toSizeText(): String {
    val kb = this / BYTES_PER_KB
    return if (kb < KB_PER_MB) {
        stringResource(
            R.string.lesson_size_kb,
            String.format(Locale.ROOT, "%.0f", kb.coerceAtLeast(1.0)),
        )
    } else {
        stringResource(R.string.lesson_size_mb, String.format(Locale.ROOT, "%.1f", kb / KB_PER_MB))
    }
}

/**
 * Every lesson of the course in order. The current one is highlighted; lessons this viewer cannot
 * open (before enrolling, all but the free previews) show a lock and do nothing.
 */
@Composable
internal fun LessonOutlineSection(
    outline: List<OutlineLesson>,
    currentLessonId: String,
    accessibleIds: Set<String>,
    onLessonClick: (lessonId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DetailSection(
        title = stringResource(R.string.lesson_section_outline),
        iconRes = R.drawable.ic_book_open,
        tone = MintTone,
        trailing = stringResource(R.string.lesson_label_outline_count, outline.size),
        modifier = modifier,
    ) {
        var lastModule: String? = null
        outline.forEachIndexed { index, lesson ->
            if (lesson.moduleTitle != lastModule) {
                lastModule = lesson.moduleTitle
                Text(
                    text = lesson.moduleTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = HintText,
                    modifier = Modifier.padding(top = if (index == 0) 0.dp else Spacing.xs),
                )
            }
            OutlineRow(
                lesson = lesson,
                current = lesson.id == currentLessonId,
                accessible = lesson.id in accessibleIds,
                onClick = { onLessonClick(lesson.id) },
            )
        }
    }
}

@Composable
private fun OutlineRow(
    lesson: OutlineLesson,
    current: Boolean,
    accessible: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .then(if (current) Modifier.background(SecondaryButtonBackground) else Modifier)
                .clickable(
                    enabled = accessible && !current,
                    role = Role.Button,
                    onClickLabel = stringResource(R.string.course_cd_open_lesson, lesson.title),
                    onClick = onClick,
                ).padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(if (accessible) R.drawable.ic_play else R.drawable.ic_lock),
            contentDescription =
                if (accessible) null else stringResource(R.string.course_cd_locked_lesson),
            tint = if (accessible) SecondaryButtonContent else HintText,
            modifier = Modifier.size(RowIconSize),
        )
        Text(
            text = lesson.title,
            style = AppTextStyles.ListItemTitle,
            fontWeight = if (current) FontWeight.Bold else null,
            color = if (accessible) MaterialTheme.colorScheme.onSurface else HintText,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        lesson.durationSeconds?.let {
            Text(
                text = formatLessonDuration(it),
                style = AppTextStyles.HelperText,
                color = HintText,
            )
        }
    }
}
