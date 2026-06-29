package com.armacos.life.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Un point GPS horodaté du trajet de la journée. */
@Serializable
@Entity(tableName = "location_points", indices = [Index("dayKey")])
data class LocationPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dayKey: String,
    val lat: Double,
    val lng: Double,
    val accuracy: Float = 0f,
)
