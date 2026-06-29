package com.armacos.life.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Un lieu, pour la stat « Lieux visités » (coordonnées optionnelles, GPS en V2). */
@Serializable
@Entity(tableName = "places")
data class Place(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "📍",
    val colorArgb: Int = 0xFF006A6A.toInt(),
    val lat: Double? = null,
    val lng: Double? = null,
)
