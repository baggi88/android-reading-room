package com.example.readingroom.ui.theme

import androidx.compose.ui.graphics.Color

// Цвета из файла values/colors.xml (Светлая тема)
val md_theme_primary = Color(0xFF6750A4)
val md_theme_onPrimary = Color(0xFFFFFFFF)
val md_theme_primaryContainer = Color(0xFFEADDFF)
val md_theme_onPrimaryContainer = Color(0xFF21005D)
val md_theme_secondary = Color(0xFF625B71)
val md_theme_onSecondary = Color(0xFFFFFFFF)
val md_theme_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_onSecondaryContainer = Color(0xFF1D192B)
val md_theme_tertiary = Color(0xFF7D5260)
val md_theme_onTertiary = Color(0xFFFFFFFF)
val md_theme_tertiaryContainer = Color(0xFFFFD8E4)
val md_theme_onTertiaryContainer = Color(0xFF31111D)
val md_theme_error = Color(0xFFB3261E)
val md_theme_onError = Color(0xFFFFFFFF)
val md_theme_errorContainer = Color(0xFFF9DEDC)
val md_theme_onErrorContainer = Color(0xFF410E0B)
val md_theme_background = Color(0xFFFFFBFE)
val md_theme_onBackground = Color(0xFF1C1B1F)
val md_theme_surface = Color(0xFFFFFBFE)
val md_theme_onSurface = Color(0xFF1C1B1F)
val md_theme_surfaceVariant = Color(0xFFE7E0EC)
val md_theme_onSurfaceVariant = Color(0xFF49454F)
val md_theme_outline = Color(0xFF79747E)
val md_theme_inverseSurface = Color(0xFF313033)
val md_theme_inverseOnSurface = Color(0xFFF4EFF4)
val md_theme_inversePrimary = Color(0xFFD0BCFF)
val md_theme_scrim = Color(0xFF000000)
val md_theme_surfaceTint = Color(0xFF6750A4)

// Здесь можно было бы определить цвета для темной темы (md_theme_dark_...), 
// но так как они используются в Theme.kt через darkColorScheme(), который
// ссылается на те же имена (md_theme_primary и т.д.), а система сама
// подставляет значения из values-night/colors.xml, то отдельные константы
// для темной темы в этом файле не обязательны.
// Оставляем этот файл только с базовыми (светлыми) цветами, но с именами без _light. 