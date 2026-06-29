package com.armacos.life.data

import android.content.Context

/** Petites préférences locales (SharedPreferences). */
class AppPrefs(context: Context) {

    private val sp = context.getSharedPreferences("arma_prefs", Context.MODE_PRIVATE)

    /** Le suivi automatique du trajet GPS est-il activé ? (off par défaut, opt-in). */
    var gpsTrackingEnabled: Boolean
        get() = sp.getBoolean(KEY_GPS, false)
        set(value) = sp.edit().putBoolean(KEY_GPS, value).apply()

    companion object {
        private const val KEY_GPS = "gps_tracking_enabled"
    }
}
