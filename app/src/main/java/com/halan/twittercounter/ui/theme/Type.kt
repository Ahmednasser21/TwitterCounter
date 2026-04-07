package com.halan.twittercounter.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.halan.twittercounter.R

val DinNextArabic = FontFamily(Font(R.font.din_next_lt_arabic, FontWeight.W500))
val DinNextArabicBold = FontFamily(Font(R.font.din_next_lt_arabic, FontWeight.W700))
val DinNextArabicRegular = FontFamily(Font(R.font.din_next_lt_arabic_regular, FontWeight.W400))
val DinNextArabicMedium = FontFamily(Font(R.font.din_next_lt_arabic_medium, FontWeight.W500))

val Typography = Typography(

    titleMedium = TextStyle(
        fontFamily = DinNextArabic,
        fontWeight = FontWeight.W500,
        fontSize = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DinNextArabicMedium,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
    ),

    headlineMedium = TextStyle(
        fontFamily = DinNextArabicMedium,
        fontWeight = FontWeight.W500,
        fontSize = 26.sp,
    ),

    bodyMedium = TextStyle(
        fontFamily = DinNextArabicRegular,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
    ),

    labelLarge = TextStyle(
        fontFamily = DinNextArabicBold,
        fontWeight = FontWeight.W700,
        fontSize = 14.sp,
        lineHeight = 19.6.sp,
    ),

    titleLarge = TextStyle(
        fontFamily = DinNextArabicBold,
        fontWeight = FontWeight.W700,
        fontSize = 18.sp,
    ),
)