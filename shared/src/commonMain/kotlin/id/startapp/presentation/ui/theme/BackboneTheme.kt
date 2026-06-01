package id.startapp.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Backbone Provider Mobile App Theme
 *
 * Material 3 theme configuration using Backbone brand colors.
 * Supports both light and dark themes.
 *
 * Design specification:
 * https://github.com/backbone/backbone/docs/blueprint/development/provider-mobile/design/00-design-system.md
 */

// ============================================================================
// Light Color Scheme
// ============================================================================

/**
 * Light theme color scheme using Backbone brand colors.
 */
private val LightColorScheme = lightColorScheme(
    // Primary colors - Brand Blue
    primary = BackboneBlue,
    onPrimary = Color.White,
    primaryContainer = BackboneBlueTint,
    onPrimaryContainer = BackboneBlueDark,

    // Secondary colors
    secondary = BackboneBlueLight,
    onSecondary = Color.White,
    secondaryContainer = BackboneBlueTint,
    onSecondaryContainer = BackboneBlueDark,

    // Tertiary colors - Express Purple
    tertiary = ExpressPurple,
    onTertiary = Color.White,
    tertiaryContainer = ExpressPurpleBg,
    onTertiaryContainer = ExpressPurpleLight,

    // Error colors
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorRedBg,
    onErrorContainer = ErrorRed,

    // Background colors
    background = BackgroundPrimary,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,

    // Surface variants
    surfaceVariant = SurfaceGray100,
    onSurfaceVariant = TextSecondary,

    // Outline colors
    outline = SurfaceGray300,
    outlineVariant = SurfaceGray200,

    // Inverse colors
    inverseSurface = DarkBackgroundPrimary,
    inverseOnSurface = SurfaceGray50,
    inversePrimary = BackboneBlueLight,

    // Success status (custom)
    // Material 3 doesn't have success color, using tertiary as workaround
    // Use SuccessGreen directly in components when needed
)

// ============================================================================
// Dark Color Scheme
// ============================================================================

/**
 * Dark theme color scheme using Backbone brand colors.
 */
private val DarkColorScheme = darkColorScheme(
    // Primary colors - Brand Blue (adjusted for dark mode)
    primary = BackboneBlueLight,
    onPrimary = BackboneBlueDark,
    primaryContainer = BackboneBlueDark,
    onPrimaryContainer = BackboneBlueTint,

    // Secondary colors
    secondary = BackboneBlue,
    onSecondary = Color.White,
    secondaryContainer = BackboneBlueDark,
    onSecondaryContainer = BackboneBlueTint,

    // Tertiary colors - Express Purple (adjusted for dark mode)
    tertiary = ExpressPurpleLight,
    onTertiary = Color.White,
    tertiaryContainer = ExpressPurple,
    onTertiaryContainer = ExpressPurpleBg,

    // Error colors
    error = ErrorRedLight,
    onError = Color.White,
    errorContainer = ErrorRed,
    onErrorContainer = ErrorRedBg,

    // Background colors
    background = DarkBackgroundPrimary,
    onBackground = SurfaceGray50,
    surface = DarkBackgroundSecondary,
    onSurface = SurfaceGray50,

    // Surface variants
    surfaceVariant = DarkBackgroundTertiary,
    onSurfaceVariant = SurfaceGray200,

    // Outline colors
    outline = SurfaceGray400,
    outlineVariant = DarkBackgroundTertiary,

    // Inverse colors
    inverseSurface = BackgroundPrimary,
    inverseOnSurface = TextPrimary,
    inversePrimary = BackboneBlueDark
)

// ============================================================================
// Theme Composable
// ============================================================================

/**
 * Backbone App Theme
 *
 * Main theme composable for the Backbone Provider Mobile App.
 * Applies Material 3 theme with Backbone brand colors and typography.
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param content Content to be themed
 */
@Composable
fun BackboneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = BackboneTypography,
        content = content
    )
}

// ============================================================================
// Shape Constants
// ============================================================================

/**
 * Backbone shape constants for consistent border radius across the app.
 */
object BackboneShapes {
    /**
     * Small border radius - 4dp
     * Used for: Small UI elements
     */
    const val Small = 4

    /**
     * Medium border radius - 8dp
     * Used for: Buttons, chips
     */
    const val Medium = 8

    /**
     * Large border radius - 12dp
     * Used for: Cards, dialogs, input fields
     */
    const val Large = 12

    /**
     * Input field border radius - 12dp
     * Used for: Text input fields, OTP boxes
     */
    const val InputRadius = 12

    /**
     * Extra large border radius - 16dp
     * Used for: Bottom sheets, FAB
     */
    const val XLarge = 16

    /**
     * Full radius - 28dp
     * Used for: Full-width dialogs, rounded modals
     */
    const val Full = 28
}

// ============================================================================
// Spacing Constants
// ============================================================================

/**
 * Backbone spacing constants based on 8pt grid system.
 */
object BackboneSpacing {
    /** XXSmall gaps - 4dp */
    const val XS = 4

    /** XSmall - 8dp */
    const val SM = 8

    /** Medium (default padding) - 16dp */
    const val MD = 16

    /** Large - 24dp */
    const val LG = 24

    /** XLarge - 32dp */
    const val XL = 32

    /** XXLarge - 40dp */
    const val XXL = 40

    /** XXXLarge - 48dp */
    const val XXXL = 48

    /** Massive - 64dp */
    const val Massive = 64
}

// ============================================================================
// Icon Sizes
// ============================================================================

/**
 * Backbone icon size constants.
 */
object BackboneIconSizes {
    /** Small icon - 18dp */
    const val Small = 18

    /** Default icon - 24dp */
    const val Medium = 24

    /** Large icon - 32dp */
    const val Large = 32

    /** Extra large icon - 48dp */
    const val XLarge = 48

    // ============================================================================
    // Logo Sizes (for BackboneLogo component)
    // ============================================================================

    /** Small logo - 80dp */
    const val LogoSmall = 80

    /** Medium logo - 120dp (default) */
    const val LogoMedium = 120

    /** Large logo - 160dp */
    const val LogoLarge = 160

    /** Extra large logo - 200dp */
    const val LogoXLarge = 200
}

// ============================================================================
// Component Dimensions
// ============================================================================

/**
 * Backbone component dimension constants.
 */
object BackboneDimensions {
    /** Button height - 48dp */
    const val ButtonHeight = 48

    /** FAB size - 56dp */
    const val FabSize = 56

    /** Input field height - 56dp */
    const val InputHeight = 56

    /** App bar height - 56dp */
    const val AppBarHeight = 56

    /** Bottom nav height - 80dp (56dp + safe area) */
    const val BottomNavHeight = 80

    /** Status badge height - 24dp */
    const val StatusBadgeHeight = 24

    /** Avatar size - small 40dp */
    const val AvatarSmall = 40

    /** Avatar size - medium 80dp */
    const val AvatarMedium = 80

    /** Avatar size - large 100dp */
    const val AvatarLarge = 100

    /** Card minimum touch target - 48dp */
    const val TouchTarget = 48

    /** OTP box size - 48dp x 48dp */
    const val OtpBoxSize = 48

    /** Filter chip height - 32dp */
    const val ChipHeight = 32

    // ============================================================================
    // Spacing (from BackboneSpacing, duplicated for convenience)
    // ============================================================================

    /** XXSmall gaps - 4dp */
    const val XS = 4

    /** XSmall - 8dp */
    const val SM = 8

    /** Medium (default padding) - 16dp */
    const val MD = 16

    /** Large - 24dp */
    const val LG = 24

    /** XLarge - 32dp */
    const val XL = 32

    /** XXLarge - 40dp */
    const val XXL = 40

    /** XXXLarge - 48dp */
    const val XXXL = 48

    // ============================================================================
    // Onboarding Constants
    // ============================================================================

    /** Number of onboarding slides */
    const val OnboardingSlideCount = 3

    /** Onboarding illustration size */
    const val OnboardingIllustrationSize = 280

    // ============================================================================
    // Timer Constants
    // ============================================================================

    /** Splash screen delay in milliseconds */
    const val SplashDelay = 2000L

    /** Default OTP countdown duration in seconds */
    const val OtpCountdownSeconds = 60

    /** OTP length */
    const val OtpLength = 6

    // ============================================================================
    // Pagination Constants
    // ============================================================================

    /** Active dot width */
    const val PaginationDotActiveWidth = 24

    /** Inactive dot size */
    const val PaginationDotSize = 8

    /** Pagination dot corner radius */
    const val PaginationDotCornerRadius = 4
}
