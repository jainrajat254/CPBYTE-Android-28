package com.example.projecthub.ui.theme

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
import com.example.projecthub.viewModel.ThemeViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40

private val DarkColorScheme = darkColorScheme(
    primary = DeepBlueGray,
    onPrimary = OffWhite,
    primaryContainer = CharcoalBlue,
    onPrimaryContainer = OffWhite,
    secondary = CadetBlue,
    onSecondary = OffWhite,
    secondaryContainer = CadetBlue.copy(alpha = 0.3f),
    onSecondaryContainer = OffWhite,
    tertiary = SlateGrayBlue,
    onTertiary = Color.White,
    tertiaryContainer = SlateGrayBlue.copy(alpha = 0.2f),
    onTertiaryContainer = OffWhite,
    background = CharcoalBlue.copy(alpha = 0.9f),
    onBackground = OffWhite,
    surface = CharcoalBlue.copy(alpha = 0.8f),
    onSurface = OffWhite,
    surfaceVariant = CharcoalBlue.copy(alpha = 0.4f),
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    error = Color(0xFFCF6679),
    onError = Color.White,
    errorContainer = Color(0xFF8B1D2C),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = lightColorScheme(
    primary = ClassicSlateGray,
    onPrimary = Color.White,
    primaryContainer = CadetBlue.copy(alpha = 0.15f),
    onPrimaryContainer = CharcoalBlue,
    secondary = CadetBlue,
    onSecondary = Color.White,
    secondaryContainer = CadetBlue.copy(alpha = 0.1f),
    onSecondaryContainer = CharcoalBlue,
    tertiary = SlateGrayBlue,
    onTertiary = Color.White,
    tertiaryContainer = SlateGrayBlue.copy(alpha = 0.1f),
    onTertiaryContainer = CharcoalBlue,
    background = OffWhite,
    onBackground = CharcoalBlue,
    surface = LightGray,
    onSurface = CharcoalBlue,
    surfaceVariant = LightGray,
    onSurfaceVariant = CharcoalBlue.copy(alpha = 0.7f),
    outline = MediumGray,
    error = Color(0xFFB00020),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)


    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */

@Composable
fun ProjectHUBTheme(
    themeViewModel: ThemeViewModel,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDarkTheme by themeViewModel.isDarkMode.collectAsState()

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}