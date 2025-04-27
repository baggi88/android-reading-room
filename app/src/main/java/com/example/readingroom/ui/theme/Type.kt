package com.example.readingroom.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.readingroom.R

// Добавляем семейство шрифтов Philosopher
val Philosopher = FontFamily(
    Font(R.font.philosopher_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.philosopher_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.philosopher_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.philosopher_bold_italic, FontWeight.Bold, FontStyle.Italic)
)

// Заменяем определение Typography, применяя Philosopher ко всем стилям
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Bold, // Сделаем заголовки жирными
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = Philosopher,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp
    )
) 