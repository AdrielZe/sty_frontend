package com.example.training_tracker.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


// Cores Principais (Cyan - padrão)
val CyanAccent = Color(0xFF61B2F3)
val CyanDark = Color(0xFF1B92B6)

// Degradê de tons de azul
val CyanGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF61B2F3), // Tom original
        Color(0xFF4A90E2), // Tom intermediário
        Color(0xFF1B92B6)  // Tom mais escuro (CyanDark)
    )
)

val GreenGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
)

val LightBrushes = CustomBrushes(
    primaryGradient = Brush.linearGradient(listOf(CyanDark, CyanAccent)),
    backgroundGradient = Brush.verticalGradient(
        listOf(
            Color(0xFFFDFDFD), // Off-white
            Color(0xFFF2F2F2)  // O cinza claro que você usou, mas na base
        )
    )
)

val DarkBrushes = CustomBrushes(
    primaryGradient = Brush.linearGradient(listOf(CyanAccent, Color.Blue)),
    backgroundGradient = Brush.verticalGradient(listOf(Color(0xFF2D2D35),Color(0xFF1E1E24)))
)
val ExerciseCardBackgroundLight = Color(0XFFFFFFFF)
val ExerciseCardBackgroundDark = Color(0XFF000000)

// Cores para quando clicar nos treino
val ClickBlue = Color(0XFF52d6ff)

// Modo Claro
val LightBackground = Color(0xFFFDFDFD)
val LightSurface = Color(0xFFF0F4F8)
val LightText = Color(0xFF1A1A1A)

// Modo Escuro
val DarkBackground = Color(0xFF131319)
val DarkSurface = Color(0xFF1E1E24)
val DarkText = Color(0xFFE0E0E0)

val GrayWhiteText = Color(0xFFcfd1cf)

val GrayDarkText = Color(0xFF323332)

val LightOutlineText = Color(0xFF94A3B8)

val DarkOutlineText = Color(0xFF94A3B8 )


val FadeCardBackgroundLight = Color(0xFFF0F4F8)

// ─── Accent theme system ───────────────────────────────────────────────────

data class AccentTheme(
    val name: String,
    val light: Color,
    val dark: Color,
    val gradient: Brush
)

object AccentThemes {
    val Cyan = AccentTheme(
        name = "CYAN",
        light = Color(0xFF61B2F3),
        dark = Color(0xFF1B92B6),
        gradient = Brush.linearGradient(listOf(Color(0xFF61B2F3), Color(0xFF4A90E2), Color(0xFF1B92B6)))
    )
    val Purple = AccentTheme(
        name = "PURPLE",
        light = Color(0xFFB39DDB),
        dark = Color(0xFF7E57C2),
        gradient = Brush.linearGradient(listOf(Color(0xFFB39DDB), Color(0xFF9575CD), Color(0xFF7E57C2)))
    )
    val Orange = AccentTheme(
        name = "ORANGE",
        light = Color(0xFFFFB74D),
        dark = Color(0xFFF57C00),
        gradient = Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFFF9800), Color(0xFFF57C00)))
    )
    val Green = AccentTheme(
        name = "GREEN",
        light = Color(0xFF81C784),
        dark = Color(0xFF388E3C),
        gradient = Brush.linearGradient(listOf(Color(0xFF81C784), Color(0xFF4CAF50), Color(0xFF388E3C)))
    )
    val Pink = AccentTheme(
        name = "PINK",
        light = Color(0xFFF48FB1),
        dark = Color(0xFFC2185B),
        gradient = Brush.linearGradient(listOf(Color(0xFFF48FB1), Color(0xFFE91E63), Color(0xFFC2185B)))
    )
    val Red = AccentTheme(
        name = "RED",
        light = Color(0xFFEF9A9A),
        dark = Color(0xFFC62828),
        gradient = Brush.linearGradient(listOf(Color(0xFFEF9A9A), Color(0xFFF44336), Color(0xFFC62828)))
    )

    val all = listOf(Cyan, Purple, Orange, Green, Pink, Red)

    fun fromName(name: String?): AccentTheme = all.find { it.name == name } ?: Cyan
}
