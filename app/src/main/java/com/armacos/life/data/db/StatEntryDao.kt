package com.armacos.life.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.armacos.life.data.entity.StatEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface StatEntryDao {

    @Insert
    suspend fun insert(entry: StatEntry): Long

    @Update
    suspend fun update(entry: StatEntry)

    @Delete
    suspend fun delete(entry: StatEntry)

    @Query("SELECT * FROM stat_entries WHERE dayKey = :dayKey ORDER BY timestamp DESC")
    fun observeForDay(dayKey: String): Flow<List<StatEntry>>

    @Query("SELECT * FROM stat_entries WHERE statId = :statId AND dayKey = :dayKey ORDER BY timestamp DESC")
    fun observeForStatDay(statId: Long, dayKey: String): Flow<List<StatEntry>>

    @Query("SELECT * FROM stat_entries WHERE statId = :statId AND dayKey = :dayKey ORDER BY timestamp DESC")
    suspend fun forStatDayOnce(statId: Long, dayKey: String): List<StatEntry>

    @Query("SELECT * FROM stat_entries WHERE statId = :statId ORDER BY timestamp ASC")
    fun observeAllForStat(statId: Long): Flow<List<StatEntry>>

    @Query("SELECT * FROM stat_entries WHERE statId = :statId ORDER BY timestamp ASC")
    suspend fun allForStat(statId: Long): List<StatEntry>

    @Query("SELECT * FROM stat_entries WHERE statId = :statId AND dayKey BETWEEN :from AND :to ORDER BY timestamp ASC")
    suspend fun rangeForStat(statId: Long, from: String, to: String): List<StatEntry>

    @Query("DELETE FROM stat_entries WHERE statId = :statId")
    suspend fun deleteForStat(statId: Long)

    @Query("DELETE FROM stat_entries")
    suspend fun clear()

    @Query("SELECT * FROM stat_entries ORDER BY timestamp ASC")
    suspend fun all(): List<StatEntry>

    @Query("SELECT MIN(dayKey) FROM stat_entries")
    suspend fun firstDayKey(): String?
}
