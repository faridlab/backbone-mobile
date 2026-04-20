package id.startapp.pheromone.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Material 3 theme for the application
 *
 * Generated from Backbone schema
 */
private val LightColors = lightColorScheme(
    primary = AppColors.Primary,
    onPrimary = AppColors.OnPrimary,
    primaryContainer = AppColors.PrimaryContainer,
    onPrimaryContainer = AppColors.OnPrimaryContainer,
    secondary = AppColors.Secondary,
    onSecondary = AppColors.OnSecondary,
    secondaryContainer = AppColors.SecondaryContainer,
    onSecondaryContainer = AppColors.OnSecondaryContainer,
    tertiary = AppColors.Tertiary,
    onTertiary = AppColors.OnTertiary,
    tertiaryContainer = AppColors.TertiaryContainer,
    onTertiaryContainer = AppColors.OnTertiaryContainer,
    error = AppColors.Error,
    onError = AppColors.OnError,
    errorContainer = AppColors.ErrorContainer,
    onErrorContainer = AppColors.OnErrorContainer,
    background = AppColors.Background,
    onBackground = AppColors.OnBackground,
    surface = AppColors.Surface,
    onSurface = AppColors.OnSurface,
    surfaceVariant = AppColors.SurfaceVariant,
    onSurfaceVariant = AppColors.OnSurfaceVariant,
    outline = AppColors.Outline,
    outlineVariant = AppColors.OutlineVariant,
)

private val DarkColors = darkColorScheme(
    primary = DarkAppColors.Primary,
    onPrimary = DarkAppColors.OnPrimary,
    primaryContainer = DarkAppColors.PrimaryContainer,
    onPrimaryContainer = DarkAppColors.OnPrimaryContainer,
    secondary = DarkAppColors.Secondary,
    onSecondary = DarkAppColors.OnSecondary,
    secondaryContainer = DarkAppColors.SecondaryContainer,
    onSecondaryContainer = DarkAppColors.OnSecondaryContainer,
    tertiary = DarkAppColors.Tertiary,
    onTertiary = DarkAppColors.OnTertiary,
    tertiaryContainer = DarkAppColors.TertiaryContainer,
    onTertiaryContainer = DarkAppColors.OnTertiaryContainer,
    error = DarkAppColors.Error,
    onError = DarkAppColors.OnError,
    errorContainer = DarkAppColors.ErrorContainer,
    onErrorContainer = DarkAppColors.OnErrorContainer,
    background = DarkAppColors.Background,
    onBackground = DarkAppColors.OnBackground,
    surface = DarkAppColors.Surface,
    onSurface = DarkAppColors.OnSurface,
    surfaceVariant = DarkAppColors.SurfaceVariant,
    onSurfaceVariant = DarkAppColors.OnSurfaceVariant,
    outline = DarkAppColors.Outline,
    outlineVariant = DarkAppColors.OutlineVariant,
)

/**
 * App theme composable
 *
 * @param darkTheme Whether to use dark theme
 * @param content Content to render
 */
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
