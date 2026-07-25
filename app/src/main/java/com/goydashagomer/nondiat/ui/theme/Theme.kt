package com.goydashagomer.nondiat.ui.theme

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
import com.goydashagomer.nondiat.data.AppThemeSetting

private val LightColorScheme = lightColorScheme(
    primary = MdPrimaryLight,
    onPrimary = MdOnPrimaryLight,
    primaryContainer = MdPrimaryContainerLight,
    onPrimaryContainer = MdOnPrimaryContainerLight,
    secondary = MdSecondaryLight,
    onSecondary = MdOnSecondaryLight,
    secondaryContainer = MdSecondaryContainerLight,
    onSecondaryContainer = MdOnSecondaryContainerLight,
    tertiary = MdTertiaryLight,
    onTertiary = MdOnTertiaryLight,
    tertiaryContainer = MdTertiaryContainerLight,
    onTertiaryContainer = MdOnTertiaryContainerLight,
    background = MdBackgroundLight,
    surface = MdSurfaceLight,
    surfaceVariant = MdSurfaceVariantLight
)

private val DarkColorScheme = darkColorScheme(
    primary = MdPrimaryDark,
    onPrimary = MdOnPrimaryDark,
    primaryContainer = MdPrimaryContainerDark,
    onPrimaryContainer = MdOnPrimaryContainerDark,
    secondary = MdSecondaryDark,
    onSecondary = MdOnSecondaryDark,
    secondaryContainer = MdSecondaryContainerDark,
    onSecondaryContainer = MdOnSecondaryContainerDark,
    background = MdBackgroundDark,
    surface = MdSurfaceDark,
    surfaceVariant = MdSurfaceVariantDark
)

private val AmoledColorScheme = darkColorScheme(
    primary = MdPrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = MdPrimaryContainerDark,
    onPrimaryContainer = Color.White,
    secondary = MdSecondaryDark,
    onSecondary = Color.Black,
    background = AmoledBackground,
    surface = AmoledSurface,
    surfaceVariant = AmoledSurfaceVariant
)

@Composable
fun GoydaShagomerTheme(
    appTheme: AppThemeSetting = AppThemeSetting.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val colorScheme = when (appTheme) {
        AppThemeSetting.AMOLED -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val baseDynamic = dynamicDarkColorScheme(context)
                baseDynamic.copy(
                    background = Color.Black,
                    surface = Color.Black,
                    surfaceDim = Color.Black,
                    surfaceBright = Color(0xFF141414),
                    surfaceContainer = Color.Black,
                    surfaceContainerLowest = Color.Black,
                    surfaceContainerLow = Color(0xFF0D0D0D),
                    surfaceContainerHigh = Color(0xFF181818),
                    surfaceContainerHighest = Color(0xFF222222),
                    surfaceVariant = Color(0xFF161616),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            } else {
                AmoledColorScheme
            }
        }
        AppThemeSetting.LIGHT -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicLightColorScheme(context)
            } else {
                LightColorScheme
            }
        }
        AppThemeSetting.DARK -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context)
            } else {
                DarkColorScheme
            }
        }
        AppThemeSetting.SYSTEM -> {
            if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (systemInDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (systemInDark) DarkColorScheme else LightColorScheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
