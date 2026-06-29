package com.armacos.life.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.armacos.life.ArmaApp
import com.armacos.life.domain.DayKey
import com.armacos.life.sensor.StepCounter

/**
 * Échantillonne le capteur de pas et enregistre le total du jour pour la stat « Pas ».
 * Comme le capteur compte depuis le démarrage, on garde une « base » prise au début
 * de chaque journée : pas du jour = total actuel − base.
 */
class StepWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ArmaApp
        val repo = app.container.repository
        val stat = repo.stepStat() ?: return Result.success()
        val current = StepCounter.readCumulative(applicationContext) ?: return Result.success()

        val prefs = applicationContext.getSharedPreferences("steps", Context.MODE_PRIVATE)
        val today = DayKey.today()
        val baselineDay = prefs.getString("baseline_day", null)
        var baseline = prefs.getFloat("baseline", -1f)

        // Nouveau jour, première lecture, ou redémarrage du téléphone -> on réinitialise la base.
        if (baselineDay != today || baseline < 0f || current < baseline) {
            baseline = current
            prefs.edit()
                .putString("baseline_day", today)
                .putFloat("baseline", baseline)
                .apply()
        }

        val todaySteps = (current - baseline).coerceAtLeast(0f).toDouble()
        repo.setDailyAbsolute(stat.id, today, todaySteps)
        app.container.refreshWidget()
        return Result.success()
    }
}
