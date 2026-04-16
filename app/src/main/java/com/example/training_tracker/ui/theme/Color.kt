package com.example.training_tracker.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


// Cores Principais
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
