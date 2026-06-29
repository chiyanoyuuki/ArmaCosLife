package com.armacos.life.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.armacos.life.data.entity.LocationPoint
import com.armacos.life.data.entity.Person
import com.armacos.life.data.entity.Place
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatEntry

@Database(
    entities = [
        StatDefinition::class,
        StatEntry::class,
        Person::class,
        Place::class,
        LocationPoint::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ArmaDatabase : RoomDatabase() {
    abstract fun statDefinitionDao(): StatDefinitionDao
    abstract fun statEntryDao(): StatEntryDao
    abstract fun personDao(): PersonDao
    abstract fun placeDao(): PlaceDao
    abstract fun locationDao(): LocationDao
}

/** v1 -> v2 : ajoute la table des points GPS sans toucher aux données existantes. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `location_points` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`timestamp` INTEGER NOT NULL, " +
                "`dayKey` TEXT NOT NULL, " +
                "`lat` REAL NOT NULL, " +
                "`lng` REAL NOT NULL, " +
                "`accuracy` REAL NOT NULL)",
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_location_points_dayKey` " +
                "ON `location_points` (`dayKey`)",
        )
    }
}
