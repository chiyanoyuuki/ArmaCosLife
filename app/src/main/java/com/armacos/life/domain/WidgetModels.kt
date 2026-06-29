package com.armacos.life.domain

import com.armacos.life.data.entity.StatType

/** Une tuile du widget (stat épinglée + sa valeur du jour). */
data class WidgetItem(
    val statId: Long,
    val emoji: String,
    val name: String,
    val type: StatType,
    val value: Double,
    val display: String,
    val colorArgb: Int,
    /** true = un tap sur la tuile incrémente directement (sans ouvrir l'app). */
    val isCounter: Boolean,
)

/** Tout ce que le widget doit afficher pour aujourd'hui. */
data class WidgetSnapshot(
    val dayKey: String,
    val dateLabel: String,
    val headline: String,
    val items: List<WidgetItem>,
)
