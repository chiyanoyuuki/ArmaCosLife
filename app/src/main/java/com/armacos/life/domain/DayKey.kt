package com.armacos.life.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Gère le « jour » de l'app, qui se réinitialise à minuit (fuseau local).
 * Un dayKey est une chaîne `yyyy-MM-dd` : triable, groupable, et stockée sur
 * chaque saisie. Rien n'est supprimé à minuit — on change juste de clé.
 */
object DayKey {
    private val fmt: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun of(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate().format(fmt)

    fun today(zone: ZoneId = ZoneId.systemDefault()): String =
        LocalDate.now(zone).format(fmt)

    fun toLocalDate(key: String): LocalDate = LocalDate.parse(key, fmt)

    fun fromLocalDate(date: LocalDate): String = date.format(fmt)

    /** Millisecondes restantes avant le prochain minuit (pour planifier le reset). */
    fun millisUntilNextMidnight(zone: ZoneId = ZoneId.systemDefault()): Long {
        val now = ZonedDateTime.now(zone)
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(zone)
        return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(0)
    }
}
