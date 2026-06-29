package com.armacos.life.data.export

import com.armacos.life.data.entity.LocationPoint
import com.armacos.life.data.entity.Person
import com.armacos.life.data.entity.Place
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatEntry
import com.armacos.life.data.repo.TrackerRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Contenu complet d'une sauvegarde, sérialisable en JSON. */
@Serializable
data class BackupData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val stats: List<StatDefinition> = emptyList(),
    val entries: List<StatEntry> = emptyList(),
    val people: List<Person> = emptyList(),
    val places: List<Place> = emptyList(),
    val locations: List<LocationPoint> = emptyList(),
)

/** Export / import de toutes les données en JSON (sauvegarde locale, hors-ligne). */
class BackupManager(private val repo: TrackerRepository) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun exportJson(): String = json.encodeToString(repo.snapshotForBackup())

    suspend fun importJson(text: String) {
        val data = json.decodeFromString<BackupData>(text)
        repo.replaceAll(data)
    }
}
