package com.itsmesujan.jlpt.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

// Brand palette: torii vermillion + warm paper + indigo night (dark theme uses M3 surface tones, never pure black).
private val Vermillion = Color(0xFFC62828)
private val VermillionDark = Color(0xFF8E1414)
private val Amber = Color(0xFFEF6C00)
private val Paper = Color(0xFFFFF8EE)
private val Indigo = Color(0xFF283593)

private val LightColors = lightColorScheme(
    primary = Vermillion, onPrimary = Color.White,
    secondary = Amber, tertiary = Indigo,
    background = Paper, surface = Color.White
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF8A65), onPrimary = Color(0xFF3E2723),
    secondary = Color(0xFFFFB74D), tertiary = Color(0xFF9FA8DA),
    background = Color(0xFF14100E), surface = Color(0xFF1E1917)
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.Default, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.Default, fontSize = 17.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.Default, fontSize = 15.sp, lineHeight = 21.sp)
)

@Composable
fun JlptQuestTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, typography = AppTypography, content = content)
}
