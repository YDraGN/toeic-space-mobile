package com.toeicspace.android.feature.course.presentation.catalog

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.TestTags
import com.toeicspace.android.core.ui.components.ToeicFilterChip
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.InkSoft
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.course.domain.CourseExamType
import com.toeicspace.android.feature.course.domain.CourseFilter
import com.toeicspace.android.feature.course.domain.CourseLevel
import com.toeicspace.android.feature.course.presentation.components.labelRes

private val SearchFieldHeight = 48.dp
private val SearchIconSize = 20.dp

/** Pill search box; the keyboard's search key searches at once instead of waiting for the debounce. */
@Composable
internal fun CourseSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.colorScheme
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.onSurface),
        cursorBrush = SolidColor(colors.primary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(onSearch = {
                onSearch()
                focusManager.clearFocus()
            }),
        modifier = modifier.testTag(TestTags.COURSE_SEARCH),
        decorationBox = { innerTextField ->
            Surface(
                shape = PillShape,
                color = colors.surface,
                modifier = Modifier.border(1.dp, CardBorder, PillShape),
            ) {
                Row(
                    modifier =
                        Modifier
                            .heightIn(
                                min = SearchFieldHeight,
                            ).padding(start = Spacing.md, end = Spacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_search),
                        contentDescription = null,
                        tint = InkSoft,
                        modifier = Modifier.size(SearchIconSize),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(R.string.course_hint_search),
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                        innerTextField()
                    }
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_x),
                                contentDescription =
                                    stringResource(
                                        R.string.course_cd_clear_search,
                                    ),
                                tint = InkSoft,
                                modifier = Modifier.size(SearchIconSize),
                            )
                        }
                    }
                }
            }
        },
    )
}

/** Opens the level and score sheet; the badge counts the filters chosen there. */
@Composable
internal fun CourseFilterButton(
    activeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToeicIconButton(
        onClick = onClick,
        modifier = modifier.testTag(TestTags.COURSE_FILTER),
        badgeCount = activeCount.takeIf { it > 0 },
        isSelected = activeCount > 0,
        contentDescription = stringResource(R.string.course_cd_filter),
    ) {
        Icon(painter = painterResource(R.drawable.ic_filter), contentDescription = null)
    }
}

/** "All" and one chip per exam type, scrolling sideways on narrow screens. */
@Composable
internal fun ExamTypeChipRow(
    selected: CourseExamType?,
    onSelect: (CourseExamType?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        ExamTypeChips(selected = selected, onSelect = onSelect)
    }
}

@Composable
private fun ExamTypeChips(
    selected: CourseExamType?,
    onSelect: (CourseExamType?) -> Unit,
) {
    ToeicFilterChip(
        text = stringResource(R.string.course_label_all),
        selected = selected == null,
        onClick = { onSelect(null) },
    )
    CourseExamType.entries.forEach { type ->
        ToeicFilterChip(
            text = stringResource(type.labelRes),
            selected = selected == type,
            onClick = { onSelect(type) },
        )
    }
}

/**
 * Filter groups as wrapping chip rows: level and target score, plus exam type where the exam row is
 * not already on screen (the tablet side panel).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun CourseFilterOptions(
    filter: CourseFilter,
    onExamTypeSelect: (CourseExamType?) -> Unit,
    onLevelSelect: (CourseLevel?) -> Unit,
    onTargetScoreSelect: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    showExamType: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        if (showExamType) {
            FilterGroup(title = stringResource(R.string.course_label_exam_type)) {
                ExamTypeChips(selected = filter.examType, onSelect = onExamTypeSelect)
            }
        }
        FilterGroup(title = stringResource(R.string.course_label_level)) {
            ToeicFilterChip(
                text = stringResource(R.string.course_label_all),
                selected = filter.level == null,
                onClick = { onLevelSelect(null) },
            )
            CourseLevel.entries.forEach { level ->
                ToeicFilterChip(
                    text = stringResource(level.labelRes),
                    selected = filter.level == level,
                    onClick = { onLevelSelect(level) },
                )
            }
        }
        FilterGroup(title = stringResource(R.string.course_label_target_score)) {
            ToeicFilterChip(
                text = stringResource(R.string.course_label_all),
                selected = filter.targetScore == null,
                onClick = { onTargetScoreSelect(null) },
            )
            TargetScoreOptions.forEach { score ->
                ToeicFilterChip(
                    text = stringResource(R.string.course_label_target_option, score),
                    selected = filter.targetScore == score,
                    onClick = { onTargetScoreSelect(score) },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            content()
        }
    }
}

/** Title row of the sheet and the side panel: "Bộ lọc" and, when something is set, "Xoá bộ lọc". */
@Composable
internal fun FilterHeader(
    showClear: Boolean,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.course_title_filter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (showClear) {
            TextButton(onClick = onClear) {
                Text(text = stringResource(R.string.course_action_clear_filters))
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CourseFiltersPreview() {
    ToeicSpaceTheme {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            CourseSearchField(query = "", onQueryChange = {}, onSearch = {})
            ExamTypeChipRow(selected = null, onSelect = {})
            FilterHeader(showClear = true, onClear = {})
            CourseFilterOptions(
                filter = CourseFilter(level = CourseLevel.INTERMEDIATE),
                onExamTypeSelect = {},
                onLevelSelect = {},
                onTargetScoreSelect = {},
                showExamType = true,
            )
        }
    }
}
