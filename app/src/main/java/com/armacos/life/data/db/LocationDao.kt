package com.armacos.life.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.armacos.life.data.entity.LocationPoint
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insert(point: LocationPoint)

    @Query("SELECT * FROM location_points WHERE dayKey = :dayKey ORDER BY timestamp ASC")
    fun observeForDay(dayKey: String): Flow<List<LocationPoint>>

    @Query("SELECT * FROM location_points WHERE dayKey = :dayKey ORDER BY timestamp ASC")
    suspend fun forDayOnce(dayKey: String): List<LocationPoint>

    @Query("SELECT DISTINCT dayKey FROM location_points ORDER BY dayKey DESC")
    fun observeDaysWithTrack(): Flow<List<String>>

    @Query("SELECT * FROM location_points ORDER BY timestamp ASC")
    suspend fun all(): List<LocationPoint>

    @Query("DELETE FROM location_points WHERE dayKey = :dayKey")
    suspend fun clearDay(dayKey: String)

    @Query("DELETE FROM location_points")
    suspend fun clear()
}
