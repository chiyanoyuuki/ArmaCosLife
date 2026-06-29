package com.armacos.life.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.armacos.life.ArmaApp

/** Clé d'extra : l'id de la stat ciblée par un tap sur une tuile. */
val StatIdKey = ActionParameters.Key<Long>("stat_id")

/** Tap sur une tuile « compteur » : +1 (ou +step) directement, sans ouvrir l'app. */
class IncrementAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val statId = parameters[StatIdKey] ?: return
        val app = context.applicationContext as ArmaApp
        app.container.repository.increment(statId)
        ArmaWidget().updateAll(context)
    }
}
