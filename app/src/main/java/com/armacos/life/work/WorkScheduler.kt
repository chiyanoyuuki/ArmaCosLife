package com.armacos.life.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.armacos.life.data.AppPrefs
import com.armacos.life.domain.DayKey
import java.util.concurrent.TimeUnit

/** Planifie les tâches de fond : échantillonnage des pas + reset de minuit. */
object WorkScheduler {

    private const val STEP_PERIODIC = "arma_step_sync"
    private const val STEP_NOW = "arma_step_now"
    private const val MIDNIGHT = "arma_midnight"
    private const val LOCATION_PERIODIC = "arma_location_sync"
    private const val LOCATION_NOW = "arma_location_now"

    fun ensureScheduled(context: Context) {
        val wm = WorkManager.getInstance(context)

        val periodic = PeriodicWorkRequestBuilder<StepWorker>(1, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork(STEP_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic)

        wm.enqueueUniqueWork(
            STEP_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<StepWorker>().build(),
        )

        scheduleMidnight(context)

        if (AppPrefs(context).gpsTrackingEnabled) scheduleLocation(context)
    }

    /** Active l'échantillonnage GPS périodique (~15 min) + une capture immédiate. */
    fun scheduleLocation(context: Context) {
        val wm = WorkManager.getInstance(context)
        val periodic = PeriodicWorkRequestBuilder<LocationWorker>(15, TimeUnit.MINUTES).build()
        wm.enqueueUniquePeriodicWork(LOCATION_PERIODIC, ExistingPeriodicWorkPolicy.KEEP, periodic)
        wm.enqueueUniqueWork(
            LOCATION_NOW,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<LocationWorker>().build(),
        )
    }

    fun cancelLocation(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(LOCATION_PERIODIC)
    }

    fun scheduleMidnight(context: Context) {
        val request = OneTimeWorkRequestBuilder<MidnightWorker>()
            .setInitialDelay(DayKey.millisUntilNextMidnight() + 2_000L, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(MIDNIGHT, ExistingWorkPolicy.REPLACE, request)
    }
}
