// ui/theme/Theme.kt
package com.beutystore.pearl.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Цветовая палитра
val PearlLightBlue = Color(0xFF87CEEB)
val PearlLavender = Color(0xFFE6E6FA)
val PearlPink = Color(0xFFFFB6C1)
val PearlWhite = Color(0xFFFFFFFF)
val PearlDarkGray = Color(0xFF333333)
val PearlLightGray = Color(0xFFF5F5F5)

// Добавленные цвета для исправления ошибок
val PearlLightLavender = Color(0xFFF8F8FF)
val PearlBeige = Color(0xFFF5F5DC)
val PearlGray = Color(0xFF808080)

// Акцентные цвета
val PearlPrimary = PearlLightBlue    // Голубой
val PearlSecondary = PearlLavender  // Сиреневый
val PearlAccent = PearlPink         // Розовый

private val PearlColorScheme = lightColorScheme(
    primary = PearlPrimary,
    secondary = PearlSecondary,
    tertiary = PearlAccent,
    background = PearlWhite,  // Белый фон вместо бежевого
    surface = PearlWhite,
    onPrimary = PearlWhite,
    onSecondary = PearlDarkGray,
    onBackground = PearlDarkGray,
    onSurface = PearlDarkGray,
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
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PearlColorScheme,
        typography = PearlTypography,
        content = content
    )
}