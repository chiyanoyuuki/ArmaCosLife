package com.armacos.life.data.entity

import kotlinx.serialization.Serializable

/**
 * Façon dont on agrège les saisies d'une stat pour une période donnée
 * (jour / semaine / mois / année). C'est ce qui pilote les rétrospectives.
 */
@Serializable
enum class Aggregation(val label: String) {
    SUM("Total"),
    COUNT("Nombre de fois"),
    AVG("Moyenne"),
    MAX("Max"),
    MIN("Min"),
    LAST("Dernière valeur"),
}

/** D'où viennent les saisies : à la main, ou un capteur du téléphone. */
@Serializable
enum class StatSource {
    MANUAL,
    STEP_SENSOR,
}

/**
 * Le type d'une stat définit comment on la saisit et comment on la résume.
 * C'est le cœur du système « générique » : on ne code aucune stat en dur,
 * l'utilisateur en crée autant qu'il veut en choisissant un type.
 */
@Serializable
enum class StatType(
    val label: String,
    val emojiHint: String,
    val defaultAggregation: Aggregation,
    val defaultUnit: String,
    /** true = la saisie produit une valeur numérique (montant, durée, note…). */
    val numeric: Boolean,
) {
    COUNTER("Compteur", "🔢", Aggregation.SUM, "", true),
    NUMBER("Nombre", "📏", Aggregation.SUM, "", true),
    MONEY("Argent", "💸", Aggregation.SUM, "€", true),
    DURATION("Durée", "⏱️", Aggregation.SUM, "min", true),
    RATING("Note sur 5", "⭐", Aggregation.AVG, "/5", true),
    BOOLEAN("Oui / Non", "✅", Aggregation.COUNT, "", true),
    CHOICE("Choix dans une liste", "🏷️", Aggregation.COUNT, "", false),
    TEXT("Note libre", "📝", Aggregation.COUNT, "", false),
    PEOPLE("Personnes vues", "👥", Aggregation.COUNT, "", false),
    PLACE("Lieu", "📍", Aggregation.COUNT, "", false);

    companion object {
        /** Types proposés sur l'écran « Nouvelle stat », dans un ordre logique. */
        val pickerOrder: List<StatType> = listOf(
            COUNTER, MONEY, NUMBER, DURATION, RATING, BOOLEAN, CHOICE, PEOPLE, PLACE, TEXT,
        )
    }
}
