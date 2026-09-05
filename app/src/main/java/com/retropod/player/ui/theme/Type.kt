package com.retropod.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.retropod.player.R

/**
 * Outfit is a geometric sans with even metal-signage strokes.
 * Downloaded at runtime via Play Services; falls back to the system sans.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val RetroFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Outfit"), fontProvider = provider, weight = FontWeight.Bold)
)

val RetroTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = RetroFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp,
        letterSpacing = 0.4.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RetroFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp,
        letterSpacing = 0.3.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 17.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RetroFontFamily, fontWeight = FontWeight.Normal, fontSize = 15.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp,
        letterSpacing = 0.2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RetroFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp,
        letterSpacing = 0.4.sp
    )
)
