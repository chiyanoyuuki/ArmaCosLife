package com.armacos.life.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/** Une personne, pour la stat « Personnes vues ». */
@Serializable
@Entity(tableName = "people")
data class Person(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val emoji: String = "🙂",
    val colorArgb: Int = 0xFF7D5260.toInt(),
)
