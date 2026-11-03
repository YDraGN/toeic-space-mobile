package com.toeicspace.android.core.ui.components

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.HeroGradientBrush
import com.toeicspace.android.core.ui.theme.NightSea
import com.toeicspace.android.core.ui.theme.Pearl
import com.toeicspace.android.core.ui.theme.Quicksand
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.SunlitGold
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow
import java.time.DayOfWeek
import java.time.LocalDate

private val HeroShape = RoundedCornerShape(28.dp)
private val WeekShape = RoundedCornerShape(18.dp)
private val PearlSize = 22.dp
private val UpcomingDotSize = 8.dp
private val StreakMascotSize = 104.dp

/** Brighter than [SunlitGold] so the count and today's label pass contrast on the dark corner. */
private val SunlitOnDark = Color(0xFFF5D85A)

enum class StreakDay {
    /** Studied that day: a glowing pearl. */
    DONE,

    /** Today, not studied yet: a gold ring. */
    TODAY,

    /** Later this week, or a missed day: a faint dot. */
    UPCOMING,
}

/** Monday to Sunday with only [today] marked: what every streak card shows until the streak API exists. */
fun weekWithOnlyToday(today: LocalDate): List<StreakDay> =
    List(DayOfWeek.entries.size) { index ->
        if (index == today.dayOfWeek.value - 1) StreakDay.TODAY else StreakDay.UPCOMING
    }

/**
 * Streak hero: the day count in sunlit gold beside Oysteic, then the week as a string of pearls.
 * Dark at the top-left (see [HeroGradientBrush]) so the white text there stays readable.
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
                    color = Color.White.copy(alpha = 0.9f),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.space2),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = days.toString(),
                        fontFamily = Quicksand,
                        fontWeight = FontWeight.Bold,
                        fontSize = 46.sp,
                        lineHeight = 50.sp,
                        color = SunlitOnDark,
                    )
                    Text(
                        text = stringResource(id = R.string.home_label_days),
                        style = MaterialTheme.typography.headlineMedium,
                        color = SunlitOnDark,
                        modifier = Modifier.padding(bottom = Spacing.xs + Spacing.space2),
                    )
                }
                Text(
                    text = body,
                    style = AppTextStyles.Caption,
                    color = Color.White,
                )
            }
            MascotView(
                mascotResId = R.drawable.oy2_streak,
                size = StreakMascotSize,
                isFloating = false,
            )
        }
        PearlWeek(week = week)
        footer()
    }
}

@Composable
private fun PearlWeek(
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
                // Navy tint, not white: the card turns light toward the bottom-right, and the day
                // labels there still need contrast.
                .background(NightSea.copy(alpha = 0.45f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), WeekShape)
                .padding(horizontal = Spacing.smd, vertical = Spacing.smd)
                // Seven dots mean nothing to a screen reader; one sentence does.
                .clearAndSetSemantics { contentDescription = summary },
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        week.forEachIndexed { index, day ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs + Spacing.space2),
            ) {
                Box(
                    modifier = Modifier.size(PearlSize + Spacing.xs),
                    contentAlignment = Alignment.Center,
                ) {
                    DayPearl(day = day)
                }
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color =
                        if (day ==
                            StreakDay.TODAY
                        ) {
                            SunlitOnDark
                        } else {
                            Color.White.copy(alpha = 0.85f)
                        },
                )
            }
        }
    }
}

@Composable
private fun DayPearl(day: StreakDay) {
    when (day) {
        StreakDay.DONE ->
            Box(
                modifier =
                    Modifier
                        .size(PearlSize)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            ambientColor = SunlitGold,
                            spotColor = SunlitGold,
                        ).background(
                            Brush.radialGradient(listOf(Color.White, Pearl, SunlitGold)),
                            CircleShape,
                        ),
            )
        StreakDay.TODAY ->
            Box(
                modifier =
                    Modifier
                        .size(PearlSize)
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .border(2.5.dp, SunlitOnDark, CircleShape),
            )
        StreakDay.UPCOMING ->
            Box(
                modifier =
                    Modifier
                        .size(UpcomingDotSize)
                        .background(Color.White.copy(alpha = 0.45f), CircleShape),
            )
    }
}

@Preview
@Composable
private fun PearlStreakCardPreview() {
    ToeicSpaceTheme {
        PearlStreakCard(
            days = 3,
            week =
                listOf(StreakDay.DONE, StreakDay.DONE, StreakDay.DONE, StreakDay.TODAY) +
                    List(3) { StreakDay.UPCOMING },
            body = "Viên ngọc của bạn đã có 3 lớp xà cừ.",
            modifier = Modifier.padding(Spacing.md),
        )
    }
}
