package com.armacos.life.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Une « stat » telle que définie par l'utilisateur (ex : Argent dépensé, Verres bus,
 * Humeur…). Aucune stat n'est codée en dur : tout part d'une ligne de cette table.
 */
@Serializable
@Entity(tableName = "stat_definitions")
data class StatDefinition(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "📊",
    val colorArgb: Int = 0xFF6750A4.toInt(),
    val type: StatType = StatType.COUNTER,
    val unit: String = "",
    val aggregation: Aggregation = Aggregation.SUM,
    /** Incrément d'un tap pour un COUNTER (ex : +1 verre, +0.5…). */
    val step: Double = 1.0,
    /** Objectif quotidien optionnel (ex : 10000 pas, max 5 verres). */
    val dailyGoal: Double? = null,
    /** Valeurs proposées en accès rapide (ex : 5/10/20/50 €). */
    val quickAddPresets: List<Double> = emptyList(),
    /** Options pour un type CHOICE (ex : Sport / Travail / Sortie). */
    val choiceOptions: List<String> = emptyList(),
    val source: StatSource = StatSource.MANUAL,
    /** Épinglée au widget d'écran d'accueil. */
    val pinnedToWidget: Boolean = true,
    val sortOrder: Int = 0,
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)
