package com.toeicspace.android.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.toeicspace.android.R

val Quicksand = FontFamily(Font(R.font.quicksand_bold, FontWeight.Bold))
val PlayfairDisplay = FontFamily(Font(R.font.playfair_display_semibold, FontWeight.SemiBold))

val DancingScript = FontFamily(Font(R.font.dancing_script_semibold, FontWeight.SemiBold))
val BeVietnamPro =
    FontFamily(
        Font(R.font.be_vietnam_pro, FontWeight.Normal),
        Font(R.font.be_vietnam_pro_medium, FontWeight.Medium),
        Font(R.font.be_vietnam_pro_bold, FontWeight.Bold),
    )

val ToeicSpaceTypography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = Quicksand,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                lineHeight = 44.sp,
                letterSpacing = (-0.01).em,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = Quicksand,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                lineHeight = 34.sp,
                letterSpacing = (-0.01).em,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = Quicksand,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = Quicksand,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                lineHeight = 26.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = Quicksand,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = Quicksand,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        bodyLarge =
            TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 18.sp,
                lineHeight = 28.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 26.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                lineHeight = 24.sp,
            ),
        labelLarge =
            TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                lineHeight = 24.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = BeVietnamPro,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 16.sp,
            ),
    )

// Style riêng ngoài 6 slot chuẩn M3 — dùng trực tiếp qua AppTextStyles.xxx khi cần
object AppTextStyles {
    val Editorial =
        TextStyle(
            fontFamily = PlayfairDisplay,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.01.em,
        )
    val KickerLabel =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.24.em,
        )
    val Caption =
        TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.01.em,
        )

    /** Hint or error under a form field: a step below the 13sp floating label, as in Material. */
    val HelperText =
        TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.01.em,
        )

    /** Brand tagline in script; only for short lines, never body copy. */
    val HeroTagline =
        TextStyle(
            fontFamily = DancingScript,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
        )

    /** A price on a course card: large and bold, the first number a learner looks for. */
    val Price =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            lineHeight = 26.sp,
        )

    /** The price on the course detail enroll bar. */
    val PriceLarge =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 32.sp,
        )

    /*
     * Reading pages (course detail) run a step smaller than the tabs, so long text and the colored
     * badges do not crowd each other.
     */

    /** Course name at the top of its detail page; [CourseTitleLarge] on tablets. */
    val CourseTitle =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )
    val CourseTitleLarge =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
        )

    /** Card headings on a reading page ("Giới thiệu", "Giáo trình"). */
    val SectionTitle =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            lineHeight = 22.sp,
        )

    /** Paragraphs and list items on a reading page. */
    val ReadingBody =
        TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 22.sp,
        )

    /** One line of a list the learner scans, such as a lesson title. */
    val ListItemTitle =
        TextStyle(
            fontFamily = BeVietnamPro,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )

    /** The price on the phone's enroll bar: one step below [PriceLarge] so the button still fits. */
    val PriceBar =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            lineHeight = 30.sp,
        )

    /** The big day count on streak cards. */
    val StreakCount =
        TextStyle(
            fontFamily = Quicksand,
            fontWeight = FontWeight.Bold,
            fontSize = 46.sp,
            lineHeight = 50.sp,
        )
}
