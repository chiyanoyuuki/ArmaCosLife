package com.armacos.life.domain

import com.armacos.life.data.entity.LocationPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Calculs géographiques simples (distance entre points GPS). */
object GeoUtils {

    private const val EARTH_RADIUS_M = 6_371_000.0

    /** Distance en mètres entre deux points (formule de haversine). */
    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLng / 2) * sin(dLng / 2)
        return EARTH_RADIUS_M * (2 * atan2(sqrt(a), sqrt(1 - a)))
    }

    /** Longueur totale d'un trajet (somme des segments), en mètres. */
    fun pathDistanceMeters(points: List<LocationPoint>): Double {
        var total = 0.0
        for (i in 1 until points.size) {
            total += distanceMeters(
                points[i - 1].lat, points[i - 1].lng,
                points[i].lat, points[i].lng,
            )
        }
        return total
    }

    /** "1,2 km" / "650 m". */
    fun formatDistance(meters: Double): String =
        if (meters >= 1000) "${Format.number(meters / 1000)} km" else "${meters.toInt()} m"
}
