package com.armacos.life.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Palette de couleurs proposée pour les stats / personnes / lieux. */
val StatPalette: List<Int> = listOf(
    0xFFE5484D, 0xFFF5A623, 0xFFEAB308, 0xFF22C55E, 0xFF14B8A6,
    0xFF06B6D4, 0xFF1FA2FF, 0xFF6366F1, 0xFF8B5CF6, 0xFFEC4899,
    0xFF8B5A2B, 0xFF6B7280, 0xFF6750A4, 0xFF006A6A, 0xFF84CC16,
).map { it.toInt() }

/** Émojis fréquents pour la création rapide d'une stat. */
val EmojiChoices: List<String> = listOf(
    "📊", "💸", "🍺", "🍷", "💧", "☕", "🚬", "🏃", "🚶", "😀",
    "😴", "🎯", "👥", "📍", "💪", "🛏️", "📝", "✅", "🎮", "📚",
    "🍔", "🥗", "🚗", "🎵", "❤️", "💊", "🧘", "🚿", "💼", "📞",
    "🛒", "🎬", "✈️", "💻", "🎨", "⚽", "🏋️", "🧠", "💤", "🌙",
)

/** Couleur de texte lisible (noir/blanc) sur un fond donné. */
fun onColorFor(background: Color): Color =
    if (background.luminance() > 0.5f) Color(0xFF1C1B1F) else Color.White
