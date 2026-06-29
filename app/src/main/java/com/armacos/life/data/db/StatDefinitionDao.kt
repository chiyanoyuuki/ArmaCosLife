package com.armacos.life.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatSource
import kotlinx.coroutines.flow.Flow

@Dao
interface StatDefinitionDao {

    @Query("SELECT * FROM stat_definitions WHERE archived = 0 ORDER BY sortOrder, id")
    fun observeActive(): Flow<List<StatDefinition>>

    @Query("SELECT * FROM stat_definitions ORDER BY archived, sortOrder, id")
    fun observeAll(): Flow<List<StatDefinition>>

    @Query("SELECT * FROM stat_definitions WHERE archived = 0 AND pinnedToWidget = 1 ORDER BY sortOrder, id")
    suspend fun pinned(): List<StatDefinition>

    @Query("SELECT * FROM stat_definitions WHERE id = :id")
    suspend fun byId(id: Long): StatDefinition?

    @Query("SELECT * FROM stat_definitions WHERE id = :id")
    fun observeById(id: Long): Flow<StatDefinition?>

    @Query("SELECT * FROM stat_definitions ORDER BY sortOrder, id")
    suspend fun all(): List<StatDefinition>

    @Query("SELECT COUNT(*) FROM stat_definitions")
    suspend fun count(): Int

    @Query("SELECT * FROM stat_definitions WHERE source = :source AND archived = 0 LIMIT 1")
    suspend fun firstBySource(source: StatSource): StatDefinition?

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM stat_definitions")
    suspend fun maxSortOrder(): Int

    @Insert
    suspend fun insert(stat: StatDefinition): Long

    @Update
    suspend fun update(stat: StatDefinition)

    @Delete
    suspend fun delete(stat: StatDefinition)

    @Query("DELETE FROM stat_definitions")
    suspend fun clear()
}
