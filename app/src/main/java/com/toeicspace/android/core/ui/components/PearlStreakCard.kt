package com.toeicspace.android.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HeroGradientBrush
import com.toeicspace.android.core.ui.theme.HeroWeekTint
import com.toeicspace.android.core.ui.theme.OnHero
import com.toeicspace.android.core.ui.theme.Pearl
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SunlitOnHero
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow
import java.time.DayOfWeek
import java.time.LocalDate

private val HeroShape = RoundedCornerShape(28.dp)
private val WeekShape = RoundedCornerShape(18.dp)
private val DaySlotSize = 32.dp
private val TodayRingSize = 24.dp
private val UpcomingDotSize = 8.dp
private val StreakMascotSize = 104.dp

private const val TITLE_ALPHA = 0.9f
private const val LABEL_ALPHA = 0.85f
private const val WEEK_BORDER_ALPHA = 0.25f
private const val TODAY_FILL_ALPHA = 0.15f
private const val UPCOMING_DOT_ALPHA = 0.45f

enum class StreakDay {
    /** Studied that day: Oysteic sits in the day's circle. */
    DONE,

    /** Today, not studied yet: a gold ring waiting to be filled. */
    TODAY,

    /** Later this week, or a missed day: a faint dot. */
    UPCOMING,
}

/**
 * Sample streak shown to members until the streak API exists, so the mascot day marks can be seen.
 * Guests always see 0.
 */
const val SAMPLE_STREAK_DAYS = 2

/**
 * Monday to Sunday for a streak of [streakDays] that ended yesterday: those days before [today] are
 * done, today waits to be filled. Days of the streak that fall in last week are not drawn.
 */
fun streakWeek(
    today: LocalDate,
    streakDays: Int = 0,
): List<StreakDay> {
    val todayIndex = today.dayOfWeek.value - 1
    return List(DayOfWeek.entries.size) { index ->
        when {
            index == todayIndex -> StreakDay.TODAY
            index in (todayIndex - streakDays) until todayIndex -> StreakDay.DONE
            else -> StreakDay.UPCOMING
        }
    }
}

/**
 * Streak hero: the day count in sunlit gold beside Oysteic, then the week, where every studied day
 * shows the mascot. Dark at the top-left (see [HeroGradientBrush]) so the white text stays readable.
 */
@Composable
fun PearlStreakCard(
    days: Int,
    week: List<StreakDay>,
    body: String,
    modifier: Modifier = Modifier,
    footer: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .softShadow(HeroShape)
                .clip(HeroShape)
                .background(HeroGradientBrush)
                .padding(
                    horizontal = Spacing.md + Spacing.xs,
                    vertical =
                        Spacing.md + Spacing.space2,
                ),
        verticalArrangement = Arrangement.spacedBy(Spacing.smd),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.space2),
            ) {
                Text(
                    text = stringResource(id = R.string.home_title_streak),
                    style = MaterialTheme.typography.titleSmall,
                    color = OnHero.copy(alpha = TITLE_ALPHA),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.space2),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = days.toString(),
                        style = AppTextStyles.StreakCount,
                        color = SunlitOnHero,
                    )
                    Text(
                        text = stringResource(id = R.string.home_label_days),
                        style = MaterialTheme.typography.headlineMedium,
                        color = SunlitOnHero,
                        modifier = Modifier.padding(bottom = Spacing.xs + Spacing.space2),
                    )
                }
                Text(text = body, style = AppTextStyles.Caption, color = OnHero)
            }
            MascotView(
                mascotResId = R.drawable.oy2_streak,
                size = StreakMascotSize,
                isFloating = false,
            )
        }
        StreakWeek(week = week)
        footer()
    }
}

@Composable
private fun StreakWeek(
    week: List<StreakDay>,
    modifier: Modifier = Modifier,
) {
    val labels = stringArrayResource(id = R.array.explore_week_days)
    val summary =
        stringResource(id = R.string.home_cd_streak_week, week.count { it == StreakDay.DONE })

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(WeekShape)
                // Navy film, not white: the card turns light toward the bottom-right, and the day
                // labels there still need contrast.
                .background(HeroWeekTint)
                .border(1.dp, OnHero.copy(alpha = WEEK_BORDER_ALPHA), WeekShape)
                .padding(Spacing.smd)
                // Seven marks mean nothing to a screen reader; one sentence does.
                .clearAndSetSemantics { contentDescription = summary },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        week.forEachIndexed { index, day ->
            val isToday = day == StreakDay.TODAY
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.space2),
            ) {
                Box(modifier = Modifier.size(DaySlotSize), contentAlignment = Alignment.Center) {
                    DayMark(day = day)
                }
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isToday) SunlitOnHero else OnHero.copy(alpha = LABEL_ALPHA),
                )
            }
        }
    }
}

@Composable
private fun DayMark(day: StreakDay) {
    when (day) {
        StreakDay.DONE ->
            Image(
                painter = painterResource(id = R.drawable.oy2_joy),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier =
                    Modifier
                        .size(DaySlotSize)
                        .clip(CircleShape)
                        .background(Pearl)
                        .border(1.5.dp, SunlitOnHero, CircleShape),
            )
        StreakDay.TODAY ->
            Box(
                modifier =
                    Modifier
                        .size(TodayRingSize)
                        .background(OnHero.copy(alpha = TODAY_FILL_ALPHA), CircleShape)
                        .border(2.5.dp, SunlitOnHero, CircleShape),
            )
        StreakDay.UPCOMING ->
            Box(
                modifier =
                    Modifier
                        .size(UpcomingDotSize)
                        .background(OnHero.copy(alpha = UPCOMING_DOT_ALPHA), CircleShape),
            )
    }
}

@Preview
@Composable
private fun PearlStreakCardPreview() {
    ToeicSpaceTheme {
        PearlStreakCard(
            days = SAMPLE_STREAK_DAYS,
            week = streakWeek(LocalDate.of(2026, 11, 4), SAMPLE_STREAK_DAYS),
            body = "Mỗi buổi học thêm một lớp xà cừ.",
            modifier = Modifier.padding(Spacing.md),
        )
    }
}
