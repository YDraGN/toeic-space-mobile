package com.toeicspace.android.feature.explore.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.PearlStreakCard
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SAMPLE_STREAK_DAYS
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicChip
import com.toeicspace.android.core.ui.components.ToeicChipStyle
import com.toeicspace.android.core.ui.components.ToeicFilterChip
import com.toeicspace.android.core.ui.components.ToeicIconButton
import com.toeicspace.android.core.ui.components.streakWeek
import com.toeicspace.android.core.ui.currentWindowWidth
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.feature.explore.domain.LearningCenter
import com.toeicspace.android.feature.explore.domain.StudyMode
import com.toeicspace.android.feature.main.presentation.Viewer
import java.time.LocalDate

private val CenterCardWidth = 272.dp
private const val COVER_ASPECT_RATIO = 16f / 9f

/**
 * Open to everyone and the guest start tab. Streak and plans are product info, so nothing here is a
 * made-up number; the centers are sample data until the backend can list partner centers.
 */
@Composable
internal fun ExploreTab(
    viewer: Viewer,
    onSignUpClick: () -> Unit,
    onComingSoon: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val centers by viewModel.featuredCenters.collectAsStateWithLifecycle()
    val studyModeFilter by viewModel.studyModeFilter.collectAsStateWithLifecycle()

    ExploreContent(
        viewer = viewer,
        centers = centers,
        studyModeFilter = studyModeFilter,
        onStudyModeFilterChange = viewModel::onStudyModeFilterChange,
        onMoreFiltersClick = onComingSoon,
        onSignUpClick = onSignUpClick,
        onCenterClick = { onComingSoon() },
        modifier = modifier,
        centerRows = if (currentWindowWidth().isAtLeastMedium) 2 else 1,
    )
}

@Composable
internal fun ExploreContent(
    viewer: Viewer,
    centers: List<LearningCenter>,
    studyModeFilter: StudyMode?,
    onStudyModeFilterChange: (StudyMode?) -> Unit,
    onMoreFiltersClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onCenterClick: (LearningCenter) -> Unit,
    modifier: Modifier = Modifier,
    centerRows: Int = 1,
) {
    val isGuest = viewer is Viewer.Guest
    // Side padding goes on each item instead of the list, so the center row can scroll edge to edge.
    val itemModifier = Modifier.padding(horizontal = Spacing.md)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.smd),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        item(key = "streak") {
            StreakCard(isGuest = isGuest, onSignUpClick = onSignUpClick, modifier = itemModifier)
        }
        item(key = "centers-header") {
            SectionHeader(
                title = stringResource(id = R.string.explore_title_centers),
                badge = stringResource(id = R.string.common_label_sample_data),
                modifier = itemModifier,
            )
        }
        item(key = "centers-filter") {
            CenterFilterRow(
                selected = studyModeFilter,
                onSelect = onStudyModeFilterChange,
                onMoreFiltersClick = onMoreFiltersClick,
            )
        }
        item(key = "centers") {
            // Wide windows stack the cards two high and still scroll sideways, so the section keeps
            // its carousel feel without leaving half the screen empty.
            LazyRow(
                contentPadding = PaddingValues(horizontal = Spacing.md),
                horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
            ) {
                items(items = centers.chunked(centerRows), key = { column ->
                    column.first().id
                }) { column ->
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.smd)) {
                        column.forEach { center ->
                            CenterCard(center = center, onClick = { onCenterClick(center) })
                        }
                    }
                }
            }
        }
        item(key = "plans-header") {
            SectionHeader(
                title = stringResource(id = R.string.explore_title_plans),
                modifier = itemModifier,
            )
        }
        item(key = "plan-free") {
            PlanCard(
                emoji = "🌱",
                title = stringResource(id = R.string.explore_title_plan_free),
                features = stringArrayResource(id = R.array.explore_plan_free_features).toList(),
                modifier = itemModifier,
            ) {
                if (isGuest) {
                    PrimaryButton(
                        text = stringResource(id = R.string.common_action_sign_up_free),
                        onClick = onSignUpClick,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    ToeicChip(
                        text = stringResource(id = R.string.explore_label_current_plan),
                        emoji = "✅",
                    )
                }
            }
        }
        item(key = "plan-center") {
            PlanCard(
                emoji = "🏫",
                title = stringResource(id = R.string.explore_title_plan_center),
                features = stringArrayResource(id = R.array.explore_plan_center_features).toList(),
                modifier = itemModifier,
            ) {
                ToeicChip(
                    text = stringResource(id = R.string.common_label_coming_soon),
                    style = ToeicChipStyle.NOTICE,
                )
            }
        }
    }
}

/** The same pearl card as the home tab, so the streak looks alike wherever it shows. */
@Composable
private fun StreakCard(
    isGuest: Boolean,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now() }
    // No streak API yet: members see the same sample streak as the home tab, guests start at 0.
    val days = if (isGuest) 0 else SAMPLE_STREAK_DAYS
    PearlStreakCard(
        days = days,
        week = remember(today, days) { streakWeek(today, days) },
        body =
            if (isGuest) {
                stringResource(id = R.string.explore_body_streak_guest)
            } else {
                stringResource(id = R.string.home_body_streak)
            },
        modifier = modifier,
    ) {
        if (isGuest) {
            PrimaryButton(
                text = stringResource(id = R.string.explore_action_start_streak),
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(top = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (badge != null) {
            ToeicChip(text = badge, style = ToeicChipStyle.NOTICE)
        }
    }
}

/** The filter button and the study-mode chips share one row, scrolling sideways if it gets long. */
@Composable
private fun CenterFilterRow(
    selected: StudyMode?,
    onSelect: (StudyMode?) -> Unit,
    onMoreFiltersClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToeicIconButton(
            onClick = onMoreFiltersClick,
            contentDescription = stringResource(id = R.string.explore_cd_filter),
        ) {
            Icon(painter = painterResource(id = R.drawable.ic_filter), contentDescription = null)
        }
        ToeicFilterChip(
            text = stringResource(id = R.string.explore_label_all),
            selected = selected == null,
            onClick = { onSelect(null) },
        )
        StudyMode.entries.forEach { mode ->
            ToeicFilterChip(
                text = stringResource(id = mode.labelRes),
                selected = selected == mode,
                onClick = { onSelect(mode) },
            )
        }
    }
}

/**
 * Cover, name, place, a two-line description and one row of chips. Every line has a fixed line
 * count, so all cards in the carousel come out the same height.
 */
@Composable
private fun CenterCard(
    center: LearningCenter,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToeicCard(
        modifier = modifier.width(CenterCardWidth),
        contentPadding = PaddingValues(0.dp),
        onClick = onClick,
    ) {
        // Partners have no photos yet; the brand painting stands in so the layout can be judged
        // with a real image. Swap for the center's cover once the API returns one.
        Image(
            painter = painterResource(id = R.drawable.img_splash_background),
            contentDescription = stringResource(id = R.string.explore_cd_center_cover, center.name),
            contentScale = ContentScale.Crop,
            // The top of the painting is pale sky; the horizon and sea give the cover some color.
            alignment = Alignment.BottomCenter,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(COVER_ASPECT_RATIO),
        )
        Column(
            modifier = Modifier.padding(start = Spacing.md, end = Spacing.md, bottom = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        ) {
            Text(
                text = center.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = center.location,
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = center.description,
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                modifier = Modifier.padding(top = Spacing.xs),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                StudyMode.entries.filter { it in center.modes }.forEach { mode ->
                    ToeicChip(text = stringResource(id = mode.labelRes), style = mode.chipStyle)
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    emoji: String,
    title: String,
    features: List<String>,
    modifier: Modifier = Modifier,
    footer: @Composable () -> Unit,
) {
    ToeicCard(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "$emoji  $title",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            features.forEach { feature ->
                Text(
                    text = stringResource(id = R.string.explore_plan_feature_item, feature),
                    style = AppTextStyles.Caption,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        footer()
    }
}

/** One color per study mode, the same on every card. */
private val StudyMode.chipStyle: ToeicChipStyle
    get() =
        when (this) {
            StudyMode.ONLINE -> ToeicChipStyle.SKY
            StudyMode.OFFLINE -> ToeicChipStyle.BLUSH
        }

private val StudyMode.labelRes: Int
    get() =
        when (this) {
            StudyMode.ONLINE -> R.string.explore_label_online
            StudyMode.OFFLINE -> R.string.explore_label_offline
        }

private val PreviewCenters =
    listOf(
        LearningCenter(
            "1",
            "Anh ngữ Biển Xanh",
            "Hải Châu, Đà Nẵng",
            setOf(StudyMode.ONLINE, StudyMode.OFFLINE),
            "Lớp nhỏ 8 bạn, luyện đề sát đề thật mỗi tuần.",
        ),
        LearningCenter(
            "2",
            "Ngọc Trai English",
            "Cầu Giấy, Hà Nội",
            setOf(StudyMode.OFFLINE),
            "Lộ trình từ mất gốc, kèm phát âm 1–1.",
        ),
    )

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF)
@Composable
private fun ExploreContentGuestPreview() {
    ToeicSpaceTheme {
        ExploreContent(
            viewer = Viewer.Guest,
            centers = PreviewCenters,
            studyModeFilter = null,
            onStudyModeFilterChange = {},
            onMoreFiltersClick = {},
            onSignUpClick = {},
            onCenterClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF)
@Composable
private fun ExploreContentMemberPreview() {
    ToeicSpaceTheme {
        ExploreContent(
            viewer = Viewer.Member(fullName = "Minh Anh", email = "minhanh@example.com"),
            centers = PreviewCenters,
            studyModeFilter = null,
            onStudyModeFilterChange = {},
            onMoreFiltersClick = {},
            onSignUpClick = {},
            onCenterClick = {},
        )
    }
}
