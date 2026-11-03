package com.toeicspace.android.feature.home.presentation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R
import com.toeicspace.android.core.ui.components.MascotView
import com.toeicspace.android.core.ui.components.PearlStreakCard
import com.toeicspace.android.core.ui.components.PrimaryButton
import com.toeicspace.android.core.ui.components.SecondaryButton
import com.toeicspace.android.core.ui.components.ToeicCard
import com.toeicspace.android.core.ui.components.ToeicChip
import com.toeicspace.android.core.ui.components.ToeicChipStyle
import com.toeicspace.android.core.ui.components.weekWithOnlyToday
import com.toeicspace.android.core.ui.theme.AppTextStyles
import com.toeicspace.android.core.ui.theme.CardBorder
import com.toeicspace.android.core.ui.theme.DancingScript
import com.toeicspace.android.core.ui.theme.HeroGradientBrush
import com.toeicspace.android.core.ui.theme.Pearl
import com.toeicspace.android.core.ui.theme.PearlPaper
import com.toeicspace.android.core.ui.theme.PillShape
import com.toeicspace.android.core.ui.theme.PrimaryGradientStart
import com.toeicspace.android.core.ui.theme.Spacing
import com.toeicspace.android.core.ui.theme.ToeicSpaceTheme
import com.toeicspace.android.core.ui.theme.softShadow
import com.toeicspace.android.feature.main.presentation.Viewer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HeroShape = RoundedCornerShape(28.dp)
private val HeroMascotSize = 116.dp
private val SignUpMascotSize = 104.dp
private val BenefitCardWidth = 156.dp
private val BenefitIconSize = 40.dp
private val TodayRowShape = RoundedCornerShape(14.dp)
private val CheckboxSize = 22.dp
private val WashiWidth = 96.dp
private val WashiHeight = 19.dp
private val SectionGap = Spacing.md + Spacing.xs

/** Cream on the dark corner of the hero; the brand script is too thin for white at 20sp. */
private val TaglineCream = Color(0xFFFFF1C2)
private val BlushTint = Color(0xFFFFD3D6)
private val SkyTint = Color(0xFFBBE2FF)

private val VietnameseDate = DateTimeFormatter.ofPattern("EEEE, dd/MM", Locale.forLanguageTag("vi"))

/**
 * Guest: a welcome hero, what the app offers and the sign-up card. Member: the "Registered" variant
 * from screens-flow §5.2, where sections without a backend yet are marked "coming soon" instead of
 * showing made-up numbers.
 */
@Composable
internal fun HomeTab(
    viewer: Viewer,
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (viewer) {
        Viewer.Guest ->
            GuestHome(
                onSignUpClick = onSignUpClick,
                onSignInClick = onSignInClick,
                onExploreClick = onExploreClick,
                modifier = modifier,
            )
        is Viewer.Member -> RegisteredHome(modifier = modifier)
    }
}

@Composable
private fun GuestHome(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = Spacing.sm, bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        WelcomeHero(
            onStartClick = onSignUpClick,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        BenefitsSection()
        SignUpCard(
            onSignUpClick = onSignUpClick,
            onSignInClick = onSignInClick,
            onExploreClick = onExploreClick,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
    }
}

@Composable
private fun WelcomeHero(
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .softShadow(HeroShape)
                .clip(HeroShape)
                .background(HeroGradientBrush)
                .padding(
                    start = Spacing.md + Spacing.xs,
                    end = Spacing.sm,
                    top = Spacing.md,
                    bottom = Spacing.md,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            Text(
                text = stringResource(id = R.string.home_hero_tagline),
                fontFamily = DancingScript,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                color = TaglineCream,
            )
            Text(
                text = stringResource(id = R.string.home_hero_title),
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
            )
            Surface(
                onClick = onStartClick,
                shape = PillShape,
                color = Color.White,
                contentColor = PrimaryGradientStart,
                modifier = Modifier.padding(top = Spacing.xs),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.smd),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.home_action_start),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_chevron_right),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
        MascotView(mascotResId = R.drawable.oy2_hello, size = HeroMascotSize)
    }
}

private data class Benefit(
    @param:DrawableRes val iconRes: Int,
    val iconTint: Color?,
    val iconBackground: Color,
    @param:StringRes val titleRes: Int,
    @param:StringRes val bodyRes: Int,
)

private val Benefits =
    listOf(
        Benefit(
            R.drawable.ic_practice,
            PrimaryGradientStart,
            SkyTint,
            R.string.home_benefit_practice,
            R.string.home_benefit_practice_body,
        ),
        // The pearl icon carries its own colors.
        Benefit(
            R.drawable.ic_glowing_pearl,
            null,
            BlushTint,
            R.string.home_benefit_streak,
            R.string.home_benefit_streak_body,
        ),
        Benefit(
            R.drawable.ic_explore,
            PrimaryGradientStart,
            Pearl,
            R.string.home_benefit_roadmap,
            R.string.home_benefit_roadmap_body,
        ),
        Benefit(
            R.drawable.ic_vocabulary,
            PrimaryGradientStart,
            SkyTint,
            R.string.home_benefit_vocabulary,
            R.string.home_benefit_vocabulary_body,
        ),
    )

@Composable
private fun BenefitsSection(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacing.smd)) {
        Text(
            text = stringResource(id = R.string.home_title_benefits),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = Spacing.md),
        )
        // A plain scrolling row rather than a LazyRow: with four items, IntrinsicSize.Max can give
        // every card the height of the tallest one, so they line up without a fixed height. The
        // padding sits inside the scroll so the first card lines up and the last one can peek.
        Row(
            modifier =
                Modifier
                    .horizontalScroll(rememberScrollState())
                    .height(IntrinsicSize.Max)
                    .padding(start = Spacing.md, end = Spacing.md, bottom = Spacing.smd),
            horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        ) {
            Benefits.forEach { benefit ->
                BenefitCard(benefit = benefit, modifier = Modifier.fillMaxHeight())
            }
        }
    }
}

@Composable
private fun BenefitCard(
    benefit: Benefit,
    modifier: Modifier = Modifier,
) {
    ToeicCard(modifier = modifier.width(BenefitCardWidth)) {
        Box(
            modifier =
                Modifier
                    .size(BenefitIconSize)
                    .background(benefit.iconBackground, RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center,
        ) {
            if (benefit.iconTint != null) {
                Icon(
                    painter = painterResource(id = benefit.iconRes),
                    contentDescription = null,
                    tint = benefit.iconTint,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Image(
                    painter = painterResource(id = benefit.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Text(
            text = stringResource(id = benefit.titleRes),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(id = benefit.bodyRes),
            style = AppTextStyles.Caption,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun SignUpCard(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ToeicCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                horizontal = Spacing.md + Spacing.xs,
                vertical =
                    Spacing.md + Spacing.xs,
            ),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.smd),
        ) {
            MascotView(
                mascotResId = R.drawable.oy2_cheer,
                size = SignUpMascotSize,
                isFloating = false,
            )
            Text(
                text = stringResource(id = R.string.home_title_guest),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(id = R.string.home_body_guest),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(
                text = stringResource(id = R.string.common_action_sign_up_free),
                onClick = onSignUpClick,
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
            )
            SecondaryButton(
                text = stringResource(id = R.string.common_action_have_account),
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = onExploreClick) {
                Text(
                    text = stringResource(id = R.string.home_action_explore_first),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun RegisteredHome(modifier: Modifier = Modifier) {
    val today = remember { LocalDate.now() }
    // No streak API yet: nothing is filled, only today is marked.
    val week = remember(today) { weekWithOnlyToday(today) }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.md)
                .padding(top = Spacing.sm, bottom = Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SectionGap),
    ) {
        PearlStreakCard(
            days = 0,
            week = week,
            body = stringResource(id = R.string.home_body_streak),
        )
        TodayJournalCard(date = today)
    }
}

/** A journal page listing what the home tab will offer, each marked "coming soon" for now. */
@Composable
private fun TodayJournalCard(
    date: LocalDate,
    modifier: Modifier = Modifier,
) {
    val dateLabel =
        remember(date) {
            VietnameseDate
                .format(
                    date,
                ).replaceFirstChar { it.titlecase(Locale.forLanguageTag("vi")) }
        }

    // The tape overlaps the card's top edge, so it sits outside the card's clip.
    Box(modifier = modifier.fillMaxWidth()) {
        ToeicCard(
            modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
            containerColor = PearlPaper,
            contentPadding =
                PaddingValues(
                    start = Spacing.md,
                    end = Spacing.md,
                    top = Spacing.lg,
                    bottom = Spacing.md,
                ),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dateLabel,
                        style = AppTextStyles.Caption,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(id = R.string.home_title_today),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                ToeicChip(
                    text = stringResource(id = R.string.common_label_coming_soon),
                    style = ToeicChipStyle.NOTICE,
                )
            }
            TodayRow(
                title = stringResource(id = R.string.home_title_placement),
                body = stringResource(id = R.string.home_body_placement),
            )
            TodayRow(
                title = stringResource(id = R.string.home_title_classes),
                body = stringResource(id = R.string.home_body_classes),
            )
            TodayRow(
                title = stringResource(id = R.string.home_title_free_resources),
                body = stringResource(id = R.string.home_body_free_resources),
            )
        }
        Image(
            painter = painterResource(id = R.drawable.img_washi_tape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .offset(x = Spacing.lg)
                    .size(width = WashiWidth, height = WashiHeight)
                    .rotate(-4f),
        )
    }
}

@Composable
private fun TodayRow(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(TodayRowShape)
                .background(Color.White.copy(alpha = 0.9f))
                .border(1.dp, CardBorder, TodayRowShape)
                .padding(horizontal = Spacing.smd, vertical = Spacing.smd),
        horizontalArrangement = Arrangement.spacedBy(Spacing.smd),
        verticalAlignment = Alignment.Top,
    ) {
        // An empty box: these become real tasks once the backend can tell what is done.
        Box(
            modifier =
                Modifier
                    .padding(top = Spacing.space2)
                    .size(CheckboxSize)
                    .border(
                        2.dp,
                        PrimaryGradientStart.copy(alpha = 0.55f),
                        RoundedCornerShape(7.dp),
                    ),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.space2),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = body,
                style = AppTextStyles.Caption,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, heightDp = 900)
@Composable
private fun HomeTabMemberPreview() {
    ToeicSpaceTheme {
        HomeTab(
            viewer = Viewer.Member(fullName = "Minh Anh", email = "minhanh@example.com"),
            onSignUpClick = {},
            onSignInClick = {},
            onExploreClick = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFE7FAFF, heightDp = 1100)
@Composable
private fun HomeTabGuestPreview() {
    ToeicSpaceTheme {
        HomeTab(
            viewer = Viewer.Guest,
            onSignUpClick = {},
            onSignInClick = {},
            onExploreClick = {},
        )
    }
}
