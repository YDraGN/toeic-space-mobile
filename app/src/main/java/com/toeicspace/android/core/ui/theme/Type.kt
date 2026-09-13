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
}
