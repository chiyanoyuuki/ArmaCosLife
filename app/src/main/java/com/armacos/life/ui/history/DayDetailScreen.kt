package com.armacos.life.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatEntry
import com.armacos.life.data.entity.StatType
import com.armacos.life.domain.Aggregator
import com.armacos.life.domain.Format
import com.armacos.life.ui.LocalAppContainer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(dayKey: String, onBack: () -> Unit) {
    val repo = LocalAppContainer.current.repository
    val entriesFlow = remember(dayKey) { repo.entriesForDay(dayKey) }
    val statsFlow = remember { repo.allStats() }
    val entries by entriesFlow.collectAsState(initial = emptyList())
    val stats by statsFlow.collectAsState(initial = emptyList())

    val byStat = remember(entries) { entries.groupBy { it.statId } }
    val statById = remember(stats) { stats.associateBy { it.id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Format.longDate(dayKey)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Retour") }
                },
            )
        },
    ) { padding ->
        if (entries.isEmpty()) {
            Column(Modifier.padding(padding).padding(24.dp)) {
                Text("Aucune saisie ce jour-là.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
        ) {
            byStat.forEach { (statId, list) ->
                val def = statById[statId] ?: return@forEach
                item(key = statId) {
                    DayStatCard(def, list)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun DayStatCard(stat: StatDefinition, entries: List<StatEntry>) {
    val value = Aggregator.dayValue(entries, stat.aggregation)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("${stat.emoji}  ${stat.name}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(
                    Format.value(stat.type, value, stat.unit),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(6.dp))
            entries.sortedByDescending { it.timestamp }.forEach { e ->
                val time = remember(e.timestamp) {
                    Instant.ofEpochMilli(e.timestamp).atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Text(time, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(0.dp))
                    Text(
                        "   " + entryLabel(stat, e),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private fun entryLabel(stat: StatDefinition, e: StatEntry): String = when (stat.type) {
    StatType.TEXT -> e.textValue ?: ""
    StatType.CHOICE -> e.choiceValue ?: ""
    StatType.PEOPLE -> "${e.personIds.size} personne(s)"
    StatType.PLACE -> "Lieu noté"
    else -> Format.value(stat.type, e.numericValue ?: 1.0, stat.unit)
}.let { base -> if (e.note.isNullOrBlank()) base else "$base · ${e.note}" }
