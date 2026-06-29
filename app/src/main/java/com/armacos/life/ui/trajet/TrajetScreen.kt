package com.armacos.life.ui.trajet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.armacos.life.domain.DayKey
import com.armacos.life.domain.Format
import com.armacos.life.domain.GeoUtils
import com.armacos.life.location.LocationSampler
import com.armacos.life.ui.LocalAppContainer
import com.armacos.life.work.WorkScheduler
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrajetScreen() {
    val container = LocalAppContainer.current
    val repo = container.repository
    val context = LocalContext.current
    val brand = MaterialTheme.colorScheme.primary

    var enabled by remember { mutableStateOf(container.prefs.gpsTrackingEnabled) }
    var dayKey by remember { mutableStateOf(DayKey.today()) }
    val pointsFlow = remember(dayKey) { repo.locationsForDay(dayKey) }
    val points by pointsFlow.collectAsState(initial = emptyList())
    val distance = remember(points) { GeoUtils.pathDistanceMeters(points) }

    fun setTracking(on: Boolean) {
        container.prefs.gpsTrackingEnabled = on
        enabled = on
        if (on) WorkScheduler.scheduleLocation(context) else WorkScheduler.cancelLocation(context)
    }

    val bgLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* arrière-plan : au mieux ; sinon réglages */ }

    val fineLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            setTracking(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        }
    }

    fun onToggle(want: Boolean) {
        if (!want) {
            setTracking(false)
            return
        }
        if (LocationSampler.hasPermission(context)) {
            setTracking(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                bgLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
        } else {
            fineLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }

    val today = remember { DayKey.today() }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Trajet") }) },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Card(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Suivre mon trajet", fontWeight = FontWeight.Bold)
                            Text(
                                "Un point GPS toutes les ~15 min, même app fermée.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = enabled, onCheckedChange = { onToggle(it) })
                    }
                    if (enabled) {
                        Text(
                            "Pour que ça marche app fermée, mets la localisation sur « Toujours autoriser ».",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        TextButton(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null),
                                    ),
                                )
                            },
                        ) { Text("Ouvrir les réglages de l'app") }
                    }
                }
            }

            // Navigateur de jour + distance
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = {
                    dayKey = DayKey.fromLocalDate(LocalDate.parse(dayKey).minusDays(1))
                }) { Icon(Icons.Filled.ChevronLeft, contentDescription = "Jour précédent") }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(Format.smartDate(dayKey), fontWeight = FontWeight.Bold)
                    Text(
                        "${GeoUtils.formatDistance(distance)} · ${points.size} points",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    enabled = dayKey < today,
                    onClick = { dayKey = DayKey.fromLocalDate(LocalDate.parse(dayKey).plusDays(1)) },
                ) { Icon(Icons.Filled.ChevronRight, contentDescription = "Jour suivant") }
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(12.dp)) {
                TrackMap(points = points.map { GeoPoint(it.lat, it.lng) }, lineColor = brand.toArgb())
                if (points.isEmpty()) {
                    Text(
                        if (enabled) "Aucun point ce jour-là (le suivi vient peut-être de démarrer)."
                        else "Active le suivi pour enregistrer ton trajet.",
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackMap(points: List<GeoPoint>, lineColor: Int) {
    // Mémorise le nombre de points déjà cadrés pour ne recentrer que quand ça change.
    val fittedFor = remember { mutableStateOf(-1) }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            // Init osmdroid AVANT de créer la carte : load() + dossiers de cache
            // accessibles en écriture, sinon les tuiles s'affichent puis disparaissent.
            Configuration.getInstance().apply {
                load(ctx.applicationContext, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
                userAgentValue = "ArmaCosLife"
                osmdroidBasePath = File(ctx.cacheDir, "osmdroid").also { it.mkdirs() }
                osmdroidTileCache = File(osmdroidBasePath, "tiles").also { it.mkdirs() }
            }
            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                setUseDataConnection(true)
                // OSM n'a pas de tuiles au-delà de ~19 : on plafonne pour ne jamais
                // retomber sur le quadrillage gris, même en pinçant pour zoomer.
                maxZoomLevel = 19.0
                minZoomLevel = 3.0
                controller.setZoom(5.0)
                controller.setCenter(GeoPoint(46.6, 2.4))
                onResume()
            }
        },
        update = { map ->
            map.overlays.clear()
            if (points.isNotEmpty()) {
                val line = Polyline(map).apply {
                    setPoints(points)
                    outlinePaint.color = lineColor
                    outlinePaint.strokeWidth = 12f
                }
                map.overlays.add(line)
                addMarker(map, points.first(), "Départ")
                if (points.size > 1) addMarker(map, points.last(), "Arrivée")
                if (fittedFor.value != points.size) {
                    fittedFor.value = points.size
                    map.post { fitCamera(map, points) }
                }
            } else {
                fittedFor.value = -1
            }
            map.invalidate()
        },
        onRelease = { it.onDetach() },
    )
}

/** Cadre la caméra sur le trajet, sans sur-zoomer (cas d'un seul point inclus). */
private fun fitCamera(map: MapView, points: List<GeoPoint>) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        map.controller.setZoom(16.0)
        map.controller.setCenter(points.first())
        return
    }
    runCatching { map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), false, 120) }
    if (map.zoomLevelDouble > 17.0) map.controller.setZoom(17.0)
}

private fun addMarker(map: MapView, point: GeoPoint, label: String) {
    val marker = Marker(map).apply {
        position = point
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        title = label
    }
    map.overlays.add(marker)
}
