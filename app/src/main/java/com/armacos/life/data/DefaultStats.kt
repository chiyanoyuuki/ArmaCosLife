package com.armacos.life.data

import com.armacos.life.data.entity.Aggregation
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatSource
import com.armacos.life.data.entity.StatType

/**
 * Stats de démarrage : insérées une seule fois au premier lancement pour que l'app
 * soit utile immédiatement. Toutes 100 % modifiables ou supprimables par l'utilisateur.
 */
object DefaultStats {

    private fun color(hex: Long) = hex.toInt()

    val list: List<StatDefinition> = listOf(
        StatDefinition(
            name = "Argent dépensé", emoji = "💸", type = StatType.MONEY, unit = "€",
            aggregation = Aggregation.SUM, quickAddPresets = listOf(5.0, 10.0, 20.0, 50.0),
            colorArgb = color(0xFFE5484D), sortOrder = 1,
        ),
        StatDefinition(
            name = "Verres bus", emoji = "🍺", type = StatType.COUNTER, step = 1.0,
            aggregation = Aggregation.SUM, dailyGoal = 4.0,
            colorArgb = color(0xFFF5A623), sortOrder = 2,
        ),
        StatDefinition(
            name = "Verres d'eau", emoji = "💧", type = StatType.COUNTER, step = 1.0,
            aggregation = Aggregation.SUM, dailyGoal = 8.0,
            colorArgb = color(0xFF1FA2FF), sortOrder = 3,
        ),
        StatDefinition(
            name = "Cafés", emoji = "☕", type = StatType.COUNTER, step = 1.0,
            aggregation = Aggregation.SUM, colorArgb = color(0xFF8B5A2B), sortOrder = 4,
        ),
        StatDefinition(
            name = "Cigarettes", emoji = "🚬", type = StatType.COUNTER, step = 1.0,
            aggregation = Aggregation.SUM, colorArgb = color(0xFF6B7280), sortOrder = 5,
        ),
        StatDefinition(
            name = "Pas", emoji = "🏃", type = StatType.NUMBER, unit = "pas",
            aggregation = Aggregation.SUM, dailyGoal = 10000.0, source = StatSource.STEP_SENSOR,
            pinnedToWidget = true, colorArgb = color(0xFF22C55E), sortOrder = 6,
        ),
        StatDefinition(
            name = "Humeur", emoji = "😀", type = StatType.RATING, unit = "/5",
            aggregation = Aggregation.AVG, colorArgb = color(0xFFEAB308), sortOrder = 7,
        ),
        StatDefinition(
            name = "Activité", emoji = "🎯", type = StatType.CHOICE,
            aggregation = Aggregation.COUNT,
            choiceOptions = listOf("Travail", "Sport", "Sortie", "Ciné", "Courses", "Repos", "Voyage"),
            colorArgb = color(0xFF8B5CF6), sortOrder = 8,
        ),
        StatDefinition(
            name = "Personnes vues", emoji = "👥", type = StatType.PEOPLE,
            aggregation = Aggregation.COUNT, colorArgb = color(0xFFEC4899), sortOrder = 9,
        ),
        StatDefinition(
            name = "Lieux visités", emoji = "📍", type = StatType.PLACE,
            aggregation = Aggregation.COUNT, colorArgb = color(0xFF06B6D4), sortOrder = 10,
        ),
        StatDefinition(
            name = "Sport", emoji = "💪", type = StatType.DURATION, unit = "min",
            aggregation = Aggregation.SUM, quickAddPresets = listOf(15.0, 30.0, 45.0, 60.0),
            colorArgb = color(0xFFEF4444), sortOrder = 11,
        ),
        StatDefinition(
            name = "Sommeil", emoji = "🛏️", type = StatType.DURATION, unit = "min",
            aggregation = Aggregation.LAST, quickAddPresets = listOf(360.0, 420.0, 480.0, 540.0),
            pinnedToWidget = false, colorArgb = color(0xFF6366F1), sortOrder = 12,
        ),
        StatDefinition(
            name = "Note du jour", emoji = "📝", type = StatType.TEXT,
            aggregation = Aggregation.COUNT, pinnedToWidget = false,
            colorArgb = color(0xFF14B8A6), sortOrder = 13,
        ),
        StatDefinition(
            name = "Sport fait ?", emoji = "✅", type = StatType.BOOLEAN,
            aggregation = Aggregation.COUNT, pinnedToWidget = false,
            colorArgb = color(0xFF84CC16), sortOrder = 14,
        ),
    )
}
