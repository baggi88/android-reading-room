package com.example.readingroom.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Enum для представления доступных тем
enum class ThemeType {
    LIGHT, DARK, PURPLE
}

private val LightColorScheme = lightColorScheme(
    primary = md_theme_primary,
    onPrimary = md_theme_onPrimary,
    primaryContainer = md_theme_primaryContainer,
    onPrimaryContainer = md_theme_onPrimaryContainer,
    secondary = md_theme_secondary,
    onSecondary = md_theme_onSecondary,
    secondaryContainer = md_theme_secondaryContainer,
    onSecondaryContainer = md_theme_onSecondaryContainer,
    tertiary = md_theme_tertiary,
    onTertiary = md_theme_onTertiary,
    tertiaryContainer = md_theme_tertiaryContainer,
    onTertiaryContainer = md_theme_onTertiaryContainer,
    error = md_theme_error,
    errorContainer = md_theme_errorContainer,
    onError = md_theme_onError,
    onErrorContainer = md_theme_onErrorContainer,
    background = md_theme_background,
    onBackground = md_theme_onBackground,
    surface = md_theme_surface,
    onSurface = md_theme_onSurface,
    surfaceVariant = md_theme_surfaceVariant,
    onSurfaceVariant = md_theme_onSurfaceVariant,
    outline = md_theme_outline,
    inverseOnSurface = md_theme_inverseOnSurface,
    inverseSurface = md_theme_inverseSurface,
    inversePrimary = md_theme_inversePrimary,
    surfaceTint = md_theme_surfaceTint,
    scrim = Color.Black,
)

// Новая "классическая" темная тема
private val ClassicDarkColorScheme = darkColorScheme(
    primary = Color.LightGray, // Основной акцентный цвет (для кнопок, индикаторов)
    onPrimary = Color.Black, // Текст/иконки на primary
    primaryContainer = Color.DarkGray, // Контейнеры с primary акцентом
    onPrimaryContainer = Color.White,
    secondary = Color.Gray, // Вторичный акцент
    onSecondary = Color.Black,
    secondaryContainer = Color.DarkGray,
    onSecondaryContainer = Color.White,
    tertiary = Color.Gray, // Третичный акцент
    onTertiary = Color.Black,
    tertiaryContainer = Color.DarkGray,
    onTertiaryContainer = Color.White,
    error = Color(0xFFCF6679), // Стандартный красный для ошибок в темных темах
    onError = Color.Black,
    errorContainer = Color.DarkGray,
    onErrorContainer = Color.White,
    background = Color.Black, // Черный фон
    onBackground = Color.White, // Белый текст на фоне
    surface = Color.Black, // Поверхности (карточки и т.д.) тоже черные
    onSurface = Color.White, // Белый текст на поверхностях
    surfaceVariant = Color(0xFF1E1E1E), // Чуть более светлый вариант поверхности (для BottomNav)
    onSurfaceVariant = Color.White, // Текст/иконки на surfaceVariant - БЕЛЫЙ
    outline = md_theme_outline,
    inverseOnSurface = md_theme_inverseOnSurface,
    inverseSurface = md_theme_inverseSurface,
    inversePrimary = md_theme_inversePrimary,
    surfaceTint = md_theme_surfaceTint,
    scrim = md_theme_scrim,
)

// Новая фиолетовая тема
private val PurpleColorScheme = darkColorScheme(
    primary = Color(0xFF7a6284),           // Светлый приглушенный фиолетовый
    onPrimary = Color(0xFF0c080d),           // Почти черный текст на primary
    primaryContainer = Color(0xFF52425c),   // Средний приглушенный
    onPrimaryContainer = Color(0xFFE2D2C8),  // Светлый текст на контейнере
    secondary = Color(0xFF52425c),         // Средний приглушенный фиолетовый
    onSecondary = Color(0xFFE2D2C8),        // Светлый текст на secondary
    secondaryContainer = Color(0xFF382b3f), // Темно-фиолетовый
    onSecondaryContainer = Color(0xFFE2D2C8),
    tertiary = Color(0xFF8E7692),           // Оставим старый
    onTertiary = Color(0xFFE2D2C8),
    tertiaryContainer = Color(0xFF572E54),   // Оставим старый
    onTertiaryContainer = Color(0xFFE2D2C8),
    error = Color(0xFFFF6699),           // Яркий для ошибок
    onError = Color(0xFF000000),           // Черный текст на ошибке
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0c080d),         // Почти черный фон
    onBackground = Color(0xFFE2D2C8),        // Светлый текст на фоне
    // Карточки совпадают с фоном
    surface = Color(0xFF0c080d),           // Фон карточек = фон экрана
    onSurface = Color(0xFFE2D2C8),        // Светлый текст на карточках
    surfaceVariant = Color(0xFF1f1823),     // Очень темный фон под обложкой
    onSurfaceVariant = Color(0xFFE2D2C8),    // Светлый текст/иконки на нем
    outline = Color(0xFF7a6284),           // Светлый приглушенный для обводки
    inverseOnSurface = Color(0xFF1f1823),     // Темный текст для инверсных поверхностей
    inverseSurface = Color(0xFFE2D2C8),      // Светлый фон для инверсных поверхностей
    inversePrimary = Color(0xFF52425c),     // Инверсный primary
    surfaceTint = Color(0xFF7a6284),         // Оттенок Elevation
    scrim = Color(0xFF000000),             // Черный scrim
)

@Composable
fun ReadingRoomTheme(
    // Используем ThemeType вместо Boolean
    selectedTheme: ThemeType = ThemeType.LIGHT, 
    dynamicColor: Boolean = false, // Оставляем возможность динамических цветов
    content: @Composable () -> Unit
) {
    // Определяем, является ли системная тема темной (для динамических цветов)
    val isSystemDark = isSystemInDarkTheme()
    
    val colorScheme = when {
        // Логика динамических цветов (если включены и поддерживаются)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Выбор нашей схемы на основе selectedTheme
        selectedTheme == ThemeType.DARK -> ClassicDarkColorScheme
        selectedTheme == ThemeType.PURPLE -> PurpleColorScheme
        else -> LightColorScheme // По умолчанию LIGHT
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Устанавливаем цвет статус-бара в соответствии с фоном темы
            window.statusBarColor = colorScheme.background.toArgb() 
            // Определяем цвет иконок статус-бара (светлые для темных тем, темные для светлых)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = (selectedTheme == ThemeType.LIGHT && !dynamicColor) || (dynamicColor && !isSystemDark)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 