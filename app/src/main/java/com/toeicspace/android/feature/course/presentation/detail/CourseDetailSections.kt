package com.toeicspace.android.feature.course.presentation.detail

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HintText
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.LilacTone
import com.toeicspace.android.core.ui.theme.MintTone
import com.toeicspace.android.core.ui.theme.ObjectiveCheckContainer
import com.toeicspace.android.core.ui.theme.ObjectiveCheckContent
import com.toeicspace.android.core.ui.theme.PeachTone
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.QuietFill
import com.toeicspace.android.core.ui.theme.SecondaryButtonBackground
import com.toeicspace.android.core.ui.theme.SecondaryButtonContent
import com.toeicspace.android.core.ui.theme.SkyTone
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SubtleBorder
import com.toeicspace.android.core.ui.theme.TagTone
import com.toeicspace.android.core.ui.theme.TagTones
import com.toeicspace.android.feature.course.domain.CourseDetail
import com.toeicspace.android.feature.course.domain.CourseLesson
import com.toeicspace.android.feature.course.domain.CourseModule
import com.toeicspace.android.feature.course.presentation.components.CourseCover
import com.toeicspace.android.feature.course.presentation.components.CourseLevelBadge
import com.toeicspace.android.feature.course.presentation.components.ExamTypeBadge

private val CoverSize = 96.dp
private val CoverSizeLarge = 132.dp
private val StatIconSize = 18.dp
private val CheckSize = 22.dp
private val CheckIconSize = 13.dp
private val BulletSize = 6.dp

/** Puts a bullet's center on the first line of [AppTextStyles.ReadingBody] (22sp line height). */
private val BulletTopInset = 8.dp
private val LessonIndexSize = 28.dp
private val LessonIconSize = 13.dp
private val LockIconSize = 18.dp
private val PillIconSize = 12.dp
private val ChevronSize = 20.dp
private val SectionIconBoxSize = 32.dp
private val SectionIconSize = 18.dp
private val SectionMascotSize = 72.dp
private const val CHEVRON_OPEN_DEGREES = 180f

/**
 * A white card with a heading, the one pattern every section of the page shares. The heading is led
 * by [iconRes] on a tile of [tone], so each section has its own color and the page is easy to scan.
 */
@Composable
internal fun DetailSection(
    title: String,
    @DrawableRes iconRes: Int,
    tone: TagTone,
    modifier: Modifier = Modifier,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ToeicCard(modifier = modifier.fillMaxWidth(), itemSpacing = Spacing.smd) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionIcon(iconRes = iconRes, tone = tone)
            Text(
                text = title,
                style = AppTextStyles.SectionTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f).semantics { heading() },
            )
            trailing?.let {
                Text(text = it, style = AppTextStyles.HelperText, color = HintText, maxLines = 1)
            }
        }
        content()
    }
}

/** Cover, badges and name, then a strip of the numbers a learner weighs first. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CourseHeroCard(
    course: CourseDetail,
    large: Boolean,
    modifier: Modifier = Modifier,
) {
    val summary = course.summary
    val titleStyle = if (large) AppTextStyles.CourseTitleLarge else AppTextStyles.CourseTitle
    ToeicCard(modifier = modifier.fillMaxWidth(), itemSpacing = Spacing.smd) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CourseCover(
                imageUrl = summary.thumbnailUrl,
                courseTitle = summary.title,
                seed = summary.id,
                modifier = Modifier.size(if (large) CoverSizeLarge else CoverSize),
                shape = if (large) MaterialTheme.shapes.medium else MaterialTheme.shapes.small,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                if (summary.examType != null || summary.level != null) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        summary.examType?.let { ExamTypeBadge(examType = it) }
                        summary.level?.let { CourseLevelBadge(level = it) }
                    }
                }
                Text(
                    text = summary.title,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
                summary.shortDescription?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = AppTextStyles.HelperText,
                        color = HintText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        CourseStatStrip(course = course)
    }
}

@Composable
private fun CourseStatStrip(course: CourseDetail) {
    val summary = course.summary
    val capacity = summary.capacity
    val remaining = summary.remainingSeats
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .background(QuietFill)
                .padding(horizontal = Spacing.xs, vertical = Spacing.smd),
    ) {
        StatItem(
            iconRes = R.drawable.ic_book_open,
            tone = SkyTone,
            value = stringResource(R.string.course_stat_lessons_value, course.lessonCount),
            label = stringResource(R.string.course_stat_lessons),
        )
        if (course.totalDurationSeconds > 0) {
            StatItem(
                iconRes = R.drawable.ic_clock,
                tone = LilacTone,
                value = totalDurationText(course.totalDurationSeconds, short = true),
                label = stringResource(R.string.course_stat_duration),
            )
        }
        summary.targetScore?.let {
            StatItem(
                iconRes = R.drawable.ic_target,
                tone = PeachTone,
                value = stringResource(R.string.course_label_target_option, it),
                label = stringResource(R.string.course_stat_target),
            )
        }
        if (capacity != null && remaining != null) {
            StatItem(
                iconRes = R.drawable.ic_users,
                tone = MintTone,
                value =
                    stringResource(
                        R.string.course_stat_seats_value,
                        remaining.coerceAtLeast(0),
                        capacity,
                    ),
                label = stringResource(R.string.course_stat_seats),
            )
        }
    }
}

/** A section's icon on a rounded tile of its tone. */
@Composable
internal fun SectionIcon(
    @DrawableRes iconRes: Int,
    tone: TagTone,
) {
    Box(
        modifier =
            Modifier
                .size(SectionIconBoxSize)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(tone.container),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(SectionIconSize),
        )
    }
}

@Composable
private fun RowScope.StatItem(
    @DrawableRes iconRes: Int,
    tone: TagTone,
    value: String,
    label: String,
) {
    Column(
        modifier = Modifier.weight(1f).semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.space2),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(StatIconSize),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = HintText,
            maxLines = 1,
        )
    }
}

@Composable
internal fun IntroSection(
    description: String,
    modifier: Modifier = Modifier,
) {
    DetailSection(
        title = stringResource(R.string.course_section_intro),
        iconRes = R.drawable.ic_info,
        tone = SkyTone,
        modifier = modifier,
    ) {
        // The mascot reads along beside the text, so the longest card is not a wall of words.
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = description,
                style = AppTextStyles.ReadingBody,
                color = InkSoft,
                modifier = Modifier.weight(1f),
            )
            MascotView(
                mascotResId = R.drawable.oy2_reading,
                size = SectionMascotSize,
                isFloating = false,
            )
        }
    }
}

@Composable
internal fun ObjectivesSection(
    objectives: List<String>,
    modifier: Modifier = Modifier,
) {
    DetailSection(
        title = stringResource(R.string.course_section_objectives),
        iconRes = R.drawable.ic_target,
        tone = PeachTone,
        modifier = modifier,
    ) {
        objectives.forEach { objective ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smd)) {
                Box(
                    modifier =
                        Modifier
                            .size(CheckSize)
                            .clip(CircleShape)
                            .background(ObjectiveCheckContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        tint = ObjectiveCheckContent,
                        modifier = Modifier.size(CheckIconSize),
                    )
                }
                Text(
                    text = objective,
                    style = AppTextStyles.ReadingBody,
                    color = InkSoft,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Skills as quiet tags, then the prerequisites, in one card. Either list may be empty; with both
 * empty nothing is drawn.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun SkillsSection(
    skills: List<String>,
    prerequisites: List<String>,
    modifier: Modifier = Modifier,
) {
    if (skills.isEmpty() && prerequisites.isEmpty()) return
    val prerequisitesTitle = stringResource(R.string.course_section_prerequisites)
    DetailSection(
        title =
            if (skills.isNotEmpty()) {
                stringResource(R.string.course_section_skills)
            } else {
                prerequisitesTitle
            },
        iconRes = R.drawable.ic_bar_chart,
        tone = LilacTone,
        modifier = modifier,
    ) {
        if (skills.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                // Each skill takes the next tone, so neighbouring tags never share a color.
                skills.forEachIndexed { index, skill ->
                    val tone = TagTones[index % TagTones.size]
                    Text(
                        text = skill,
                        style = AppTextStyles.HelperText,
                        fontWeight = FontWeight.Bold,
                        color = tone.content,
                        modifier =
                            Modifier
                                .clip(PillShape)
                                .background(tone.container)
                                .padding(horizontal = Spacing.smd, vertical = Spacing.xs),
                    )
                }
            }
        }
        if (prerequisites.isNotEmpty()) {
            if (skills.isNotEmpty()) {
                HorizontalDivider(color = SubtleBorder)
                Text(
                    text = prerequisitesTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.semantics { heading() },
                )
            }
            prerequisites.forEach { prerequisite ->
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.smd)) {
                    Box(
                        modifier =
                            Modifier
                                .padding(top = BulletTopInset)
                                .size(BulletSize)
                                .clip(CircleShape)
                                .background(HintText),
                    )
                    Text(
                        text = prerequisite,
                        style = AppTextStyles.ReadingBody,
                        color = InkSoft,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Chapters that open to show their lessons. Before enrolling, free previews are marked and the rest
 * locked; once [unlocked] (enrolled), every lesson opens.
 */
@Composable
internal fun CurriculumSection(
    modules: List<CourseModule>,
    expandedModuleIds: Set<String>,
    onModuleToggle: (moduleId: String) -> Unit,
    onLessonClick: (lessonId: String) -> Unit,
    modifier: Modifier = Modifier,
    unlocked: Boolean = false,
) {
    DetailSection(
        title = stringResource(R.string.course_section_curriculum),
        iconRes = R.drawable.ic_book_open,
        tone = MintTone,
        trailing =
            stringResource(
                R.string.course_label_curriculum_summary,
                modules.size,
                modules.sumOf { it.lessons.size },
            ),
        modifier = modifier,
    ) {
        modules.forEach { module ->
            ModuleBlock(
                module = module,
                expanded = module.id in expandedModuleIds,
                onToggle = { onModuleToggle(module.id) },
                onLessonClick = onLessonClick,
                unlocked = unlocked,
            )
        }
    }
}

@Composable
private fun ModuleBlock(
    module: CourseModule,
    expanded: Boolean,
    onToggle: () -> Unit,
    onLessonClick: (lessonId: String) -> Unit,
    unlocked: Boolean,
) {
    val shape = MaterialTheme.shapes.small
    val durationSeconds = module.lessons.sumOf { it.durationSeconds ?: 0 }
    val meta =
        if (durationSeconds > 0) {
            stringResource(
                R.string.course_label_module_summary,
                module.lessons.size,
                totalDurationText(durationSeconds),
            )
        } else {
            stringResource(R.string.course_label_module_lessons, module.lessons.size)
        }
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .then(
                    if (expanded) {
                        Modifier.background(QuietFill)
                    } else {
                        Modifier.border(1.dp, SubtleBorder, shape)
                    },
                ).animateContentSize(),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel =
                            stringResource(
                                if (expanded) {
                                    R.string.course_cd_collapse_module
                                } else {
                                    R.string.course_cd_expand_module
                                },
                            ),
                        role = Role.Button,
                        onClick = onToggle,
                    ).padding(Spacing.smd),
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.space2),
            ) {
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(text = meta, style = AppTextStyles.HelperText, color = HintText)
            }
            Icon(
                painter = painterResource(R.drawable.ic_chevron_down),
                contentDescription = null,
                tint = HintText,
                modifier =
                    Modifier
                        .size(
                            ChevronSize,
                        ).rotate(if (expanded) CHEVRON_OPEN_DEGREES else 0f),
            )
        }
        if (expanded) {
            Column(
                modifier =
                    Modifier.padding(
                        start = Spacing.smd,
                        end = Spacing.smd,
                        bottom = Spacing.sm,
                    ),
            ) {
                module.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = AppTextStyles.HelperText,
                        color = InkSoft,
                        modifier = Modifier.padding(bottom = Spacing.xs),
                    )
                }
                module.lessons.forEachIndexed { index, lesson ->
                    LessonRow(
                        number = index + 1,
                        lesson = lesson,
                        unlocked = unlocked,
                        onClick = { onLessonClick(lesson.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonRow(
    number: Int,
    lesson: CourseLesson,
    unlocked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(
                    if (unlocked) {
                        Modifier.clip(MaterialTheme.shapes.extraSmall).clickable(
                            onClickLabel =
                                stringResource(
                                    R.string.course_cd_open_lesson,
                                    lesson.title,
                                ),
                            role = Role.Button,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ).padding(vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier =
                Modifier
                    .size(LessonIndexSize)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, SubtleBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$number",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = HintText,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            Text(
                text = lesson.title,
                style = AppTextStyles.ListItemTitle,
                color = MaterialTheme.colorScheme.onSurface,
            )
            lesson.durationSeconds?.let {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_clock),
                        contentDescription = null,
                        tint = HintText,
                        modifier = Modifier.size(LessonIconSize),
                    )
                    Text(
                        text = formatLessonDuration(it),
                        style = AppTextStyles.HelperText,
                        color = HintText,
                    )
                }
            }
        }
        if (unlocked) {
            Icon(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = null,
                tint = SecondaryButtonContent,
                modifier = Modifier.size(LockIconSize),
            )
        } else if (lesson.isFreePreview) {
            PreviewPill(lessonTitle = lesson.title, onClick = onClick)
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_lock),
                contentDescription = stringResource(R.string.course_cd_locked_lesson),
                tint = HintText,
                modifier = Modifier.size(LockIconSize),
            )
        }
    }
}

/** "Học thử" in the secondary button colors: tappable, but quieter than the enroll button. */
@Composable
private fun PreviewPill(
    lessonTitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .clip(PillShape)
                .background(SecondaryButtonBackground)
                .clickable(
                    onClickLabel = stringResource(R.string.course_cd_free_preview, lessonTitle),
                    role = Role.Button,
                    onClick = onClick,
                ).padding(horizontal = Spacing.smd, vertical = Spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_play),
            contentDescription = null,
            tint = SecondaryButtonContent,
            modifier = Modifier.size(PillIconSize),
        )
        Text(
            text = stringResource(R.string.course_label_free_preview),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = SecondaryButtonContent,
        )
    }
}

/** What the course contains, listed under the price on tablets. */
@Composable
internal fun ColumnScope.CourseIncludes(course: CourseDetail) {
    val summary = course.summary
    val capacity = summary.capacity
    val remaining = summary.remainingSeats
    val previewCount = course.modules.sumOf { module -> module.lessons.count { it.isFreePreview } }
    Text(
        text = stringResource(R.string.course_section_includes),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() },
    )
    IncludeRow(
        R.drawable.ic_book_open,
        SkyTone,
        stringResource(R.string.course_include_lessons, course.lessonCount, course.modules.size),
    )
    if (course.totalDurationSeconds > 0) {
        IncludeRow(
            R.drawable.ic_clock,
            LilacTone,
            stringResource(
                R.string.course_include_duration,
                totalDurationText(course.totalDurationSeconds),
            ),
        )
    }
    if (previewCount > 0) {
        IncludeRow(
            R.drawable.ic_play,
            PeachTone,
            stringResource(R.string.course_include_previews, previewCount),
        )
    }
    if (capacity != null && remaining != null) {
        IncludeRow(
            R.drawable.ic_users,
            MintTone,
            stringResource(R.string.course_include_seats, remaining.coerceAtLeast(0), capacity),
        )
    }
}

@Composable
private fun IncludeRow(
    @DrawableRes iconRes: Int,
    tone: TagTone,
    text: String,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tone.content,
            modifier = Modifier.size(StatIconSize),
        )
        Text(text = text, style = AppTextStyles.Caption, color = InkSoft)
    }
}
