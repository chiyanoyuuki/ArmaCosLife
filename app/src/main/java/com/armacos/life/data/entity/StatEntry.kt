package com.armacos.life.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Une saisie : un point de donnée daté pour une stat.
 * `dayKey` (yyyy-MM-dd, fuseau local) sert au système de « jour qui se réinitialise
 * à minuit » et rend les rétrospectives jour/mois/année triviales. Rien n'est jamais
 * supprimé : changer de jour ne fait que filtrer sur un nouveau dayKey.
 */
@Entity(
    tableName = "stat_entries",
    indices = [
        Index("statId"),
        Index("dayKey"),
        Index(value = ["statId", "dayKey"]),
    ],
)
@Serializable
data class StatEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val statId: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val dayKey: String,
    val numericValue: Double? = null,
    val textValue: String? = null,
    val choiceValue: String? = null,
    val personIds: List<Long> = emptyList(),
    val placeId: Long? = null,
    val note: String? = null,
)

/** Valeur numérique d'une saisie pour l'agrégation (par défaut 1 = « une fois »). */
fun StatEntry.numericOrCount(): Double = numericValue ?: 1.0
