package com.armacos.life.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class ArmaDatabase : RoomDatabase() {
    abstract fun statDefinitionDao(): StatDefinitionDao
    abstract fun statEntryDao(): StatEntryDao
    abstract fun personDao(): PersonDao
    abstract fun placeDao(): PlaceDao
}
