package com.armacos.life.domain

import com.armacos.life.data.entity.Aggregation
import com.armacos.life.data.entity.StatEntry
import com.armacos.life.data.entity.numericOrCount
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

/** Granularité d'une rétrospective. */
enum class Granularity(val label: String, val defaultWindow: Int) {
    DAY("Jour", 14),
    WEEK("Semaine", 12),
    MONTH("Mois", 12),
    YEAR("Année", 6),
}

/** Une barre du graphe : une période agrégée. */
data class Bucket(
    val key: String,
    val label: String,
    val value: Double,
    val count: Int,
)

/** Résultat complet d'une rétrospective, prêt à afficher. */
data class Retrospective(
    val granularity: Granularity,
    val buckets: List<Bucket>,
    /** Agrégat appliqué sur TOUT l'historique (le « grand chiffre »). */
    val overall: Double,
    val perPeriodAverage: Double,
    val best: Bucket?,
    val activeDays: Int,
    val currentStreak: Int,
    val totalEntries: Int,
)

/**
 * Moteur des rétrospectives : regroupe les saisies par jour/semaine/mois/année
 * et applique l'agrégation choisie pour la stat.
 */
object Aggregator {

    private val FR = Locale.FRENCH

    private fun entryValue(e: StatEntry): Double = e.numericOrCount()

    fun aggregate(values: List<Double>, agg: Aggregation): Double {
        if (values.isEmpty()) return 0.0
        return when (agg) {
            Aggregation.SUM -> values.sum()
            Aggregation.COUNT -> values.size.toDouble()
            Aggregation.AVG -> values.average()
            Aggregation.MAX -> values.maxOrNull() ?: 0.0
            Aggregation.MIN -> values.minOrNull() ?: 0.0
            Aggregation.LAST -> values.last()
        }
    }

    /** Valeur agrégée d'une seule journée (liste de saisies d'un même jour). */
    fun dayValue(entries: List<StatEntry>, agg: Aggregation): Double =
        aggregate(entries.sortedBy { it.timestamp }.map { entryValue(it) }, agg)

    private fun periodStart(date: LocalDate, g: Granularity): LocalDate = when (g) {
        Granularity.DAY -> date
        Granularity.WEEK -> date.with(WeekFields.ISO.dayOfWeek(), 1L)
        Granularity.MONTH -> date.withDayOfMonth(1)
        Granularity.YEAR -> date.withDayOfYear(1)
    }

    private fun previousPeriod(date: LocalDate, g: Granularity): LocalDate = when (g) {
        Granularity.DAY -> date.minusDays(1)
        Granularity.WEEK -> date.minusWeeks(1)
        Granularity.MONTH -> date.minusMonths(1)
        Granularity.YEAR -> date.minusYears(1)
    }

    private fun label(date: LocalDate, g: Granularity): String = when (g) {
        Granularity.DAY -> date.format(DateTimeFormatter.ofPattern("d/M", FR))
        Granularity.WEEK -> "S" + date.get(WeekFields.ISO.weekOfWeekBasedYear())
        Granularity.MONTH -> date.format(DateTimeFormatter.ofPattern("MMM", FR))
        Granularity.YEAR -> date.year.toString()
    }

    fun retrospective(
        entries: List<StatEntry>,
        agg: Aggregation,
        g: Granularity,
        window: Int = g.defaultWindow,
        today: LocalDate = LocalDate.now(),
    ): Retrospective {
        val byPeriod = entries.groupBy { periodStart(DayKey.toLocalDate(it.dayKey), g) }

        // Les `window` dernières périodes jusqu'à aujourd'hui (incluant les vides).
        val periods = ArrayList<LocalDate>(window)
        var cursor = periodStart(today, g)
        repeat(window) {
            periods.add(cursor)
            cursor = previousPeriod(cursor, g)
        }
        periods.reverse()

        val buckets = periods.map { p ->
            val es = byPeriod[p].orEmpty().sortedBy { it.timestamp }
            Bucket(
                key = p.toString(),
                label = label(p, g),
                value = aggregate(es.map { entryValue(it) }, agg),
                count = es.size,
            )
        }

        val allValues = entries.sortedBy { it.timestamp }.map { entryValue(it) }
        val dayKeys = entries.mapTo(sortedSetOf()) { it.dayKey }
        val nonEmpty = buckets.filter { it.count > 0 }

        return Retrospective(
            granularity = g,
            buckets = buckets,
            overall = aggregate(allValues, agg),
            perPeriodAverage = if (nonEmpty.isEmpty()) 0.0 else nonEmpty.map { it.value }.average(),
            best = nonEmpty.maxByOrNull { it.value },
            activeDays = dayKeys.size,
            currentStreak = currentStreak(dayKeys, today),
            totalEntries = entries.size,
        )
    }

    private fun currentStreak(dayKeys: Set<String>, today: LocalDate): Int {
        var streak = 0
        var d = today
        while (DayKey.fromLocalDate(d) in dayKeys) {
            streak++
            d = d.minusDays(1)
        }
        return streak
    }
}
