package io.github.nwma_fywf.mineword.ui.theme

import android.app.Activity
import android.graphics.Typeface
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.nwma_fywf.mineword.data.local.FontStyle
import io.github.nwma_fywf.mineword.data.local.ThemeMode

val LocalCornerScale: ProvidableCompositionLocal<Float> = staticCompositionLocalOf { 1.0f }

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun MineWordTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    useDynamicColor: Boolean = true,
    fontStyle: FontStyle = FontStyle.DEFAULT,
    customFontPath: String? = null,
    customPrimaryColor: Int? = null,
    customSecondaryColor: Int? = null,
    customTertiaryColor: Int? = null,
    fontScale: Float = 1.0f,
    cornerScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        customPrimaryColor != null -> {
            val primary = Color(customPrimaryColor)
            val secondary = Color(customSecondaryColor ?: 0xFF625b71.toInt())
            val tertiary = Color(customTertiaryColor ?: 0xFF7D5260.toInt())
            if (darkTheme) {
                darkColorScheme(
                    primary = primary.copy(alpha = 0.8f),
                    secondary = secondary.copy(alpha = 0.8f),
                    tertiary = tertiary.copy(alpha = 0.8f)
                )
            } else {
                lightColorScheme(
                    primary = primary,
                    secondary = secondary,
                    tertiary = tertiary
                )
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = (when (fontStyle) {
        FontStyle.DEFAULT -> defaultTypography
        FontStyle.SERIF -> serifTypography
        FontStyle.SANS_SERIF -> sansSerifTypography
        FontStyle.MONOSPACE -> monospaceTypography
        FontStyle.CUSTOM -> {
            if (customFontPath != null) {
                createCustomTypography(customFontPath)
            } else {
                defaultTypography
            }
        }
    }).let { baseTypography ->
        androidx.compose.material3.Typography(
            displayLarge = baseTypography.displayLarge.copy(fontSize = baseTypography.displayLarge.fontSize * fontScale),
            displayMedium = baseTypography.displayMedium.copy(fontSize = baseTypography.displayMedium.fontSize * fontScale),
            displaySmall = baseTypography.displaySmall.copy(fontSize = baseTypography.displaySmall.fontSize * fontScale),
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = baseTypography.headlineLarge.fontSize * fontScale),
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = baseTypography.headlineMedium.fontSize * fontScale),
            headlineSmall = baseTypography.headlineSmall.copy(fontSize = baseTypography.headlineSmall.fontSize * fontScale),
            titleLarge = baseTypography.titleLarge.copy(fontSize = baseTypography.titleLarge.fontSize * fontScale),
            titleMedium = baseTypography.titleMedium.copy(fontSize = baseTypography.titleMedium.fontSize * fontScale),
            titleSmall = baseTypography.titleSmall.copy(fontSize = baseTypography.titleSmall.fontSize * fontScale),
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = baseTypography.bodyLarge.fontSize * fontScale),
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = baseTypography.bodyMedium.fontSize * fontScale),
            bodySmall = baseTypography.bodySmall.copy(fontSize = baseTypography.bodySmall.fontSize * fontScale),
            labelLarge = baseTypography.labelLarge.copy(fontSize = baseTypography.labelLarge.fontSize * fontScale),
            labelMedium = baseTypography.labelMedium.copy(fontSize = baseTypography.labelMedium.fontSize * fontScale),
            labelSmall = baseTypography.labelSmall.copy(fontSize = baseTypography.labelSmall.fontSize * fontScale)
        )
    }

    val shapes = Shapes(
        extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp * cornerScale),
        small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp * cornerScale),
        medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp * cornerScale),
        large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp * cornerScale),
        extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp * cornerScale)
    )

    CompositionLocalProvider(
        LocalCornerScale provides cornerScale
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content
        )
    }
}

private fun createCustomTypography(fontPath: String): androidx.compose.material3.Typography {
    val customFont = Typeface.createFromFile(fontPath)
    val customFontFamily = FontFamily(customFont)

    return androidx.compose.material3.Typography(
        displayLarge = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
        displayMedium = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
        displaySmall = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
        headlineLarge = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
        headlineMedium = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
        headlineSmall = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
        titleLarge = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
        titleMedium = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
        titleSmall = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        bodyLarge = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
        bodyMedium = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
        bodySmall = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
        labelLarge = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
        labelMedium = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
        labelSmall = TextStyle(fontFamily = customFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
    )
}