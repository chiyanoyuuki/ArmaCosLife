package com.armacos.life.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.armacos.life.ArmaApp

/** À minuit : rafraîchit le widget (nouveau jour) et reprogramme le prochain reset. */
class MidnightWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        (applicationContext as ArmaApp).container.refreshWidget()
        WorkScheduler.scheduleMidnight(applicationContext)
        return Result.success()
    }
}
