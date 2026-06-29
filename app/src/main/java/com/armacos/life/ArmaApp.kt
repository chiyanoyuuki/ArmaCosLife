package com.armacos.life

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.armacos.life.data.DefaultStats
import com.armacos.life.data.db.ArmaDatabase
import com.armacos.life.data.export.BackupManager
import com.armacos.life.data.repo.TrackerRepository
import com.armacos.life.widget.ArmaWidget
import com.armacos.life.work.WorkScheduler
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ArmaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.seedIfNeeded()
        WorkScheduler.ensureScheduled(this)
    }
}

/** Conteneur d'injection de dépendances « fait main » (pas de Hilt). */
class AppContainer(private val appContext: Context) {

    private val database: ArmaDatabase = Room.databaseBuilder(
        appContext,
        ArmaDatabase::class.java,
        "arma.db",
    ).fallbackToDestructiveMigration().build()

    val repository = TrackerRepository(
        database.statDefinitionDao(),
        database.statEntryDao(),
        database.personDao(),
        database.placeDao(),
    )

    val backupManager = BackupManager(repository)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /** Insère les stats de démarrage au tout premier lancement. */
    fun seedIfNeeded() {
        scope.launch {
            if (database.statDefinitionDao().count() == 0) {
                DefaultStats.list.forEach { database.statDefinitionDao().insert(it) }
            }
            refreshWidget()
        }
    }

    /** Rafraîchit toutes les instances du widget (appelé après chaque saisie). */
    suspend fun refreshWidget() {
        runCatching { ArmaWidget().updateAll(appContext) }
    }
}
