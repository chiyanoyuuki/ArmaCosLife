package com.armacos.life.ui.nav

/** Toutes les destinations de navigation, au même endroit. */
object Routes {
    const val TODAY = "today"
    const val HISTORY = "history"
    const val TRAJET = "trajet"
    const val MANAGE = "manage"
    const val SETTINGS = "settings"
    const val PEOPLE = "people"

    const val ENTRY = "entry" // entry/{statId}
    fun entry(statId: Long) = "entry/$statId"

    const val EDITOR = "editor" // editor?statId={statId}
    fun editor(statId: Long? = null) = "editor?statId=${statId ?: -1L}"

    const val RETRO = "retro" // retro/{statId}
    fun retro(statId: Long) = "retro/$statId"

    const val DAY = "day" // day/{dayKey}
    fun day(dayKey: String) = "day/$dayKey"
}
