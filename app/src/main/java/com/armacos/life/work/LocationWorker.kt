package com.armacos.life.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.armacos.life.ArmaApp
import com.armacos.life.location.LocationSampler

/** Capture périodiquement (~15 min) un point GPS si le suivi est activé. */
class LocationWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as ArmaApp
        if (!app.container.prefs.gpsTrackingEnabled) return Result.success()
        val location = LocationSampler.current(applicationContext) ?: return Result.success()
        app.container.repository.addLocation(
            lat = location.latitude,
            lng = location.longitude,
            accuracy = location.accuracy,
        )
        return Result.success()
    }
}
