package com.armacos.life.domain

import com.armacos.life.data.entity.StatType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToLong

/** Formatage centralisé des valeurs et des dates (utilisé par l'UI ET le widget). */
object Format {

    private val FR = Locale.FRENCH

    /** Nombre « propre » : 12 plutôt que 12.0, 12,5 si décimale. */
    fun number(value: Double): String {
        val rounded = (value * 100).roundToLong() / 100.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            String.format(FR, "%.1f", rounded)
        }
    }

    /** Valeur affichable d'une stat, selon son type (durée en h/min, argent en €, etc.). */
    fun value(type: StatType, value: Double, unit: String): String = when (type) {
        StatType.MONEY -> "${number(value)} ${unit.ifBlank { "€" }}"
        StatType.DURATION -> duration(value)
        StatType.RATING -> "${number(value)}/5"
        StatType.BOOLEAN -> if (value > 0) "Oui" else "Non"
        else -> if (unit.isBlank()) number(value) else "${number(value)} $unit"
    }

    /** Minutes -> "1 h 30" / "45 min". */
    fun duration(minutes: Double): String {
        val total = minutes.roundToLong()
        val h = total / 60
        val m = total % 60
        return when {
            h > 0 && m > 0 -> "${h} h ${m}"
            h > 0 -> "${h} h"
            else -> "$m min"
        }
    }

    /** "lun. 29 juin 2026" depuis un dayKey. */
    fun longDate(dayKey: String): String {
        val d = LocalDate.parse(dayKey)
        return d.format(DateTimeFormatter.ofPattern("EEE d MMMM yyyy", FR))
            .replaceFirstChar { it.uppercase(FR) }
    }

    /** "Aujourd'hui" / "Hier" / "lun. 29 juin". */
    fun smartDate(dayKey: String, today: LocalDate = LocalDate.now()): String {
        val d = LocalDate.parse(dayKey)
        return when (d) {
            today -> "Aujourd'hui"
            today.minusDays(1) -> "Hier"
            else -> d.format(DateTimeFormatter.ofPattern("EEE d MMM", FR))
        }
    }

    fun monthName(month: Int): String =
        java.time.Month.of(month).getDisplayName(TextStyle.FULL, FR)
            .replaceFirstChar { it.uppercase(FR) }
}
