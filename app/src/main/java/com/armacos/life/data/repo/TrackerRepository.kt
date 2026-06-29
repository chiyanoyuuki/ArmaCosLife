package com.armacos.life.data.repo

import com.armacos.life.data.db.PersonDao
import com.armacos.life.data.db.PlaceDao
import com.armacos.life.data.db.StatDefinitionDao
import com.armacos.life.data.db.StatEntryDao
import com.armacos.life.data.entity.Person
import com.armacos.life.data.entity.Place
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatEntry
import com.armacos.life.data.entity.StatSource
import com.armacos.life.data.entity.StatType
import com.armacos.life.data.export.BackupData
import com.armacos.life.domain.Aggregator
import com.armacos.life.domain.DayKey
import com.armacos.life.domain.Format
import com.armacos.life.domain.WidgetItem
import com.armacos.life.domain.WidgetSnapshot
import kotlinx.coroutines.flow.Flow

/** Point d'accès unique aux données pour toute l'app (UI, widget, capteurs). */
class TrackerRepository(
    private val defs: StatDefinitionDao,
    private val entries: StatEntryDao,
    private val people: PersonDao,
    private val places: PlaceDao,
) {

    // ----- Stats -----
    fun activeStats(): Flow<List<StatDefinition>> = defs.observeActive()
    fun allStats(): Flow<List<StatDefinition>> = defs.observeAll()
    fun statFlow(id: Long): Flow<StatDefinition?> = defs.observeById(id)
    suspend fun stat(id: Long): StatDefinition? = defs.byId(id)
    suspend fun statsOnce(): List<StatDefinition> = defs.all()

    suspend fun addStat(stat: StatDefinition): Long {
        val order = if (stat.sortOrder == 0) defs.maxSortOrder() + 1 else stat.sortOrder
        return defs.insert(stat.copy(sortOrder = order))
    }

    suspend fun updateStat(stat: StatDefinition) = defs.update(stat)

    suspend fun deleteStat(stat: StatDefinition) {
        entries.deleteForStat(stat.id)
        defs.delete(stat)
    }

    suspend fun setArchived(stat: StatDefinition, archived: Boolean) =
        defs.update(stat.copy(archived = archived))

    suspend fun setPinned(stat: StatDefinition, pinned: Boolean) =
        defs.update(stat.copy(pinnedToWidget = pinned))

    // ----- Saisies -----
    fun todayEntries(): Flow<List<StatEntry>> = entries.observeForDay(DayKey.today())
    fun entriesForDay(dayKey: String): Flow<List<StatEntry>> = entries.observeForDay(dayKey)
    fun entriesForStatDay(statId: Long, dayKey: String): Flow<List<StatEntry>> =
        entries.observeForStatDay(statId, dayKey)
    fun allEntriesForStat(statId: Long): Flow<List<StatEntry>> = entries.observeAllForStat(statId)
    suspend fun entriesForStatOnce(statId: Long): List<StatEntry> = entries.allForStat(statId)

    suspend fun log(
        statId: Long,
        numericValue: Double? = null,
        text: String? = null,
        choice: String? = null,
        personIds: List<Long> = emptyList(),
        placeId: Long? = null,
        note: String? = null,
        timestamp: Long = System.currentTimeMillis(),
    ): Long = entries.insert(
        StatEntry(
            statId = statId,
            timestamp = timestamp,
            dayKey = DayKey.of(timestamp),
            numericValue = numericValue,
            textValue = text,
            choiceValue = choice,
            personIds = personIds,
            placeId = placeId,
            note = note,
        ),
    )

    /** Tap rapide « +1 » (ou +step) pour un compteur. */
    suspend fun increment(statId: Long, amount: Double? = null) {
        val def = defs.byId(statId) ?: return
        log(statId, numericValue = amount ?: def.step)
    }

    suspend fun deleteEntry(entry: StatEntry) = entries.delete(entry)

    /** Stat alimentée par le capteur de pas (s'il y en a une). */
    suspend fun stepStat(): StatDefinition? = defs.firstBySource(StatSource.STEP_SENSOR)

    /** Remplace la valeur d'un jour par une valeur absolue (capteurs : pas, etc.). */
    suspend fun setDailyAbsolute(statId: Long, dayKey: String, value: Double) {
        entries.forStatDayOnce(statId, dayKey).forEach { entries.delete(it) }
        entries.insert(StatEntry(statId = statId, dayKey = dayKey, numericValue = value))
    }

    /** Valeur agrégée du jour pour une stat (utilisée par les cartes « Aujourd'hui »). */
    suspend fun todayValue(stat: StatDefinition): Double {
        val es = entries.forStatDayOnce(stat.id, DayKey.today())
        return Aggregator.dayValue(es, stat.aggregation)
    }

    // ----- Personnes & lieux -----
    fun peopleFlow(): Flow<List<Person>> = people.observeAll()
    suspend fun peopleOnce(): List<Person> = people.all()
    suspend fun addPerson(person: Person): Long = people.insert(person)
    suspend fun updatePerson(person: Person) = people.update(person)
    suspend fun deletePerson(person: Person) = people.delete(person)

    fun placesFlow(): Flow<List<Place>> = places.observeAll()
    suspend fun placesOnce(): List<Place> = places.all()
    suspend fun addPlace(place: Place): Long = places.insert(place)
    suspend fun updatePlace(place: Place) = places.update(place)
    suspend fun deletePlace(place: Place) = places.delete(place)

    // ----- Widget -----
    suspend fun widgetSnapshot(): WidgetSnapshot {
        val today = DayKey.today()
        val pinned = defs.pinned()
        var moneyTotal = 0.0
        val items = pinned.map { def ->
            val es = entries.forStatDayOnce(def.id, today)
            val value = Aggregator.dayValue(es, def.aggregation)
            if (def.type == StatType.MONEY) moneyTotal += value
            WidgetItem(
                statId = def.id,
                emoji = def.emoji,
                name = def.name,
                type = def.type,
                value = value,
                display = Format.value(def.type, value, def.unit),
                colorArgb = def.colorArgb,
                isCounter = def.type == StatType.COUNTER,
            )
        }
        val headline = if (moneyTotal > 0) {
            "${Format.number(moneyTotal)} € dépensés aujourd'hui"
        } else {
            "${items.size} stats épinglées"
        }
        return WidgetSnapshot(today, Format.smartDate(today), headline, items)
    }

    // ----- Sauvegarde JSON (lecture brute) -----
    suspend fun snapshotForBackup(): BackupData = BackupData(
        stats = defs.all(),
        entries = entries.all(),
        people = people.all(),
        places = places.all(),
    )

    suspend fun replaceAll(data: BackupData) {
        // Remplace tout le contenu par celui d'une sauvegarde importée.
        entries.clear()
        defs.clear()
        people.clear()
        places.clear()
        data.people.forEach { people.insert(it) }
        data.places.forEach { places.insert(it) }
        data.stats.forEach { defs.insert(it) }
        data.entries.forEach { entries.insert(it) }
    }
}
