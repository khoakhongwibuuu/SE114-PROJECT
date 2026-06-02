package com.example.carenest.core.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.carenest.R

@OptIn(ExperimentalTextApi::class)
private val InterFontFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.inter_regular, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.inter_regular, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.inter_regular, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.inter_regular, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)

@OptIn(ExperimentalTextApi::class)
private val ManropeFontFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(R.font.manrope_regular, FontWeight.Medium, variationSettings = FontVariation.Settings(FontVariation.weight(500))),
    Font(R.font.manrope_regular, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))),
    Font(R.font.manrope_regular, FontWeight.Bold, variationSettings = FontVariation.Settings(FontVariation.weight(700))),
    Font(R.font.manrope_regular, FontWeight.ExtraBold, variationSettings = FontVariation.Settings(FontVariation.weight(800)))
)

object CareNestTextStyles {
    val headlineXl = TextStyle(
        fontFamily = ManropeFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp
    )
    val headlineLg = TextStyle(
        fontFamily = ManropeFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp
    )
    val headlineMd = TextStyle(
        fontFamily = ManropeFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp
    )
    val titleXl = TextStyle(
        fontFamily = ManropeFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
    val titleLg = TextStyle(
        fontFamily = ManropeFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp
    )
    val titleMd = TextStyle(
        fontFamily = ManropeFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
    val bodyLg = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val bodyMd = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    )
    val bodySm = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp
    )
    val labelLg = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
    val labelMd = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp
    )
    val labelSm = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    val navLabel = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.SemiBold,
        fontSize = 9.sp,
        lineHeight = 12.sp
    )
    val overline = TextStyle(
        fontFamily = InterFontFamily,
        fontSynthesis = FontSynthesis.None,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        letterSpacing = 1.2.sp
    )
}

val Typography = Typography(
    displayLarge = CareNestTextStyles.headlineXl,
    displayMedium = CareNestTextStyles.headlineLg,
    displaySmall = CareNestTextStyles.headlineMd,
    headlineLarge = CareNestTextStyles.headlineLg,
    headlineMedium = CareNestTextStyles.headlineMd,
    headlineSmall = CareNestTextStyles.titleXl,
    titleLarge = CareNestTextStyles.titleXl,
    titleMedium = CareNestTextStyles.titleLg,
    titleSmall = CareNestTextStyles.titleMd,
    bodyLarge = CareNestTextStyles.bodyLg,
    bodyMedium = CareNestTextStyles.bodyMd,
    bodySmall = CareNestTextStyles.bodySm,
    labelLarge = CareNestTextStyles.labelLg,
    labelMedium = CareNestTextStyles.labelMd,
    labelSmall = CareNestTextStyles.labelSm
)
