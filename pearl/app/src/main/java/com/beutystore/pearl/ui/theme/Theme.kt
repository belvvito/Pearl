// ui/theme/Theme.kt
package com.beutystore.pearl.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Цветовая палитра в красно-персиковых тонах

// Красные оттенки
val PearlRed = Color(0xFFE63946)           // Красный (менее яркий)
val PearlDarkRed = Color(0xFFC1121F)       // Темный красный
val PearlLightRed = Color(0xFFFFE5E5)      // Очень светлый красный
val PearlCrimson = Color(0xFFDC143C)       // Малиновый (менее яркий)
val PearlCherryRed = Color(0xFFDE3163)     // Вишневый красный (менее яркий)
val PearlScarlet = Color(0xFFE63946)       // Красный (менее яркий)

// Персиковые оттенки
val PearlPeach = Color(0xFFFFB380)         // Яркий персиковый
val PearlLightPeach = Color(0xFFFFF8F3)    // Светлый персиковый
val PearlSoftPeach = Color(0xFFFFEBD5)     // Мягкий персиковый
val PearlDarkPeach = Color(0xFFFF9966)     // Яркий темный персиковый
val PearlCoralPeach = Color(0xFFFF9966)    // Яркий кораллово-персиковый
val PearlApricot = Color(0xFFFFAA80)       // Яркий абрикосовый
val PearlMelon = Color(0xFFFFCC99)         // Яркий дынный

// Базовые цвета
val PearlWhite = Color(0xFFFFFFFF)
val PearlDarkGray = Color(0xFF333333)
val PearlLightGray = Color(0xFFF5F5F5)
val PearlBlack = Color(0xFF000000)

// Темные цвета
val PearlDarkBackground = Color(0xFF121212)
val PearlDarkSurface = Color(0xFF1E1E1E)
val PearlDarkOnSurface = Color(0xFFE0E0E0)

// Вспомогательные цвета
val PearlLightPeachBg = Color(0xFFFFF8F3)  // Светло-персиковый фон
val PearlLightRedBg = Color(0xFFFFF5F5)    // Светло-красный фон
val PearlRedPeachBg = Color(0xFFFFF5F0)    // Смешанный красно-персиковый фон
val PearlBeige = Color(0xFFF5F5DC)
val PearlGray = Color(0xFF808080)

// Основные акцентные цвета для современного интерфейса
val PearlPrimary = PearlRed                // Основной цвет - яркий красный
val PearlSecondary = PearlPeach            // Вторичный цвет - персиковый
val PearlAccent = PearlCrimson             // Акцентный цвет - малиновый
val PearlPrimaryContainer = PearlLightRed  // Контейнер для primary - светлый красный
val PearlSecondaryContainer = PearlSoftPeach // Контейнер для secondary - мягкий персиковый

private val PearlLightColorScheme = lightColorScheme(
    primary = PearlPrimary,                    // Основной красный
    onPrimary = PearlWhite,
    primaryContainer = PearlPrimaryContainer,   // Светлый красный контейнер
    onPrimaryContainer = PearlRed,
    
    secondary = PearlSecondary,                 // Персиковый
    onSecondary = PearlDarkGray,
    secondaryContainer = PearlSecondaryContainer, // Мягкий персиковый контейнер
    onSecondaryContainer = PearlDarkPeach,
    
    tertiary = PearlAccent,                    // Акцентный малиновый
    onTertiary = PearlWhite,
    
    background = PearlRedPeachBg,              // Смешанный красно-персиковый фон
    onBackground = PearlDarkGray,
    
    surface = PearlWhite,
    onSurface = PearlDarkGray,
    
    surfaceVariant = PearlLightPeach,          // Светло-персиковый для вариантов поверхностей
    onSurfaceVariant = PearlDarkGray,
    
    outline = PearlCoralPeach,                 // Кораллово-персиковый
    outlineVariant = PearlSoftPeach,           // Мягкий персиковый
)

private val PearlDarkColorScheme = darkColorScheme(
    primary = PearlRed,                         // Красный для темной темы
    onPrimary = PearlWhite,
    primaryContainer = Color(0xFF8B1A1A),       // Темный красный контейнер
    onPrimaryContainer = PearlLightRed,
    
    secondary = PearlPeach,                     // Персиковый для темной темы
    onSecondary = PearlDarkGray,
    secondaryContainer = Color(0xFFB8946F),     // Темный персиковый контейнер
    onSecondaryContainer = PearlSoftPeach,
    
    tertiary = PearlCrimson,                    // Малиновый для темной темы
    onTertiary = PearlWhite,
    
    background = PearlDarkBackground,
    onBackground = PearlWhite,  // Белый текст в темной теме
    
    surface = PearlDarkSurface,
    onSurface = PearlWhite,  // Белый текст в темной теме
    
    surfaceVariant = Color(0xFF2D1F1F),        // Темный с красноватым оттенком
    onSurfaceVariant = PearlWhite,  // Белый текст в темной теме
    
    outline = Color(0xFF8B6B5D),                // Красно-персиково-серый
    outlineVariant = Color(0xFF3D2A2A),        // Темный красно-серый
)

val PearlTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    )
)

@Composable
fun PearlTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        PearlDarkColorScheme
    } else {
        PearlLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PearlTypography,
        content = content
    )
}