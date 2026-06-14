package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val SovereignColorScheme = darkColorScheme(
  primary = BrassAccent,
  onPrimary = DeepNavy,
  primaryContainer = NavySurface,
  onPrimaryContainer = BrassAlert,
  secondary = BrassAlert,
  onSecondary = DeepNavy,
  background = DeepNavy,
  onBackground = WhiteTech,
  surface = NavyMedium,
  onSurface = WhiteTech,
  surfaceVariant = NavySurface,
  onSurfaceVariant = GrayText,
  outline = BrassAccent,
  error = RedAlert,
  onError = WhiteTech
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force Dark Navy/Sovereign look by default
  dynamicColor: Boolean = false, // Disable dynamic styling to preserve design identity
  content: @Composable () -> Unit,
) {
  val colorScheme = SovereignColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
