package com.armacos.life.data.db

import androidx.room.TypeConverter
import com.armacos.life.data.entity.Aggregation
import com.armacos.life.data.entity.StatSource
import com.armacos.life.data.entity.StatType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter fun fromStatType(v: StatType): String = v.name
    @TypeConverter fun toStatType(v: String): StatType = StatType.valueOf(v)

    @TypeConverter fun fromAggregation(v: Aggregation): String = v.name
    @TypeConverter fun toAggregation(v: String): Aggregation = Aggregation.valueOf(v)

    @TypeConverter fun fromSource(v: StatSource): String = v.name
    @TypeConverter fun toSource(v: String): StatSource = StatSource.valueOf(v)

    @TypeConverter fun fromLongList(v: List<Long>): String = v.joinToString(",")
    @TypeConverter fun toLongList(v: String): List<Long> =
        if (v.isBlank()) emptyList() else v.split(",").mapNotNull { it.trim().toLongOrNull() }

    @TypeConverter fun fromDoubleList(v: List<Double>): String = v.joinToString(",")
    @TypeConverter fun toDoubleList(v: String): List<Double> =
        if (v.isBlank()) emptyList() else v.split(",").mapNotNull { it.trim().toDoubleOrNull() }

    @TypeConverter fun fromStringList(v: List<String>): String = Json.encodeToString(v)
    @TypeConverter fun toStringList(v: String): List<String> =
        if (v.isBlank()) emptyList() else runCatching { Json.decodeFromString<List<String>>(v) }.getOrDefault(emptyList())
}
