package com.armacos.life.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.armacos.life.domain.Aggregator
import com.armacos.life.domain.Format
import com.armacos.life.domain.Granularity
import com.armacos.life.ui.LocalAppContainer
import com.armacos.life.ui.components.BarChart

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RetrospectiveScreen(statId: Long, onBack: () -> Unit, onOpenDay: (String) -> Unit) {
    val repo = LocalAppContainer.current.repository
    val statFlow = remember { repo.statFlow(statId) }
    val entriesFlow = remember { repo.allEntriesForStat(statId) }
    val stat by statFlow.collectAsState(initial = null)
    val entries by entriesFlow.collectAsState(initial = emptyList())
    var granularity by remember { mutableStateOf(Granularity.DAY) }

    val current = stat
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current?.let { "${it.emoji}  ${it.name}" } ?: "Rétrospective") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Retour") }
                },
            )
        },
    ) { padding ->
        if (current == null) {
            Column(Modifier.padding(padding).padding(24.dp)) { Text("Chargement…") }
            return@Scaffold
        }
        val retro = remember(entries, granularity, current) {
            Aggregator.retrospective(entries, current.aggregation, granularity)
        }
        val color = Color(current.colorArgb)

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Granularity.values().forEach { g ->
                    FilterChip(
                        selected = granularity == g,
                        onClick = { granularity = g },
                        label = { Text(g.label) },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "${current.aggregation.label} · sur tout l'historique",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                Format.value(current.type, retro.overall, current.unit),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )

            Spacer(Modifier.height(16.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    BarChart(
                        buckets = retro.buckets,
                        barColor = color,
                        goal = if (granularity == Granularity.DAY) current.dailyGoal else null,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryTile("Jours suivis", "${retro.activeDays}", Modifier.weight(1f))
                SummaryTile("Série en cours", "${retro.currentStreak} j", Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SummaryTile(
                    "Meilleur(e) ${granularity.label.lowercase()}",
                    retro.best?.let { Format.value(current.type, it.value, current.unit) } ?: "—",
                    Modifier.weight(1f),
                )
                SummaryTile(
                    "Moyenne / ${granularity.label.lowercase()}",
                    Format.value(current.type, retro.perPeriodAverage, current.unit),
                    Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(20.dp))
            Text("Détail", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            retro.buckets.asReversed().forEach { b ->
                val rowModifier = if (granularity == Granularity.DAY && b.count > 0) {
                    Modifier.fillMaxWidth().clickable { onOpenDay(b.key) }
                } else {
                    Modifier.fillMaxWidth()
                }
                Column(rowModifier) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(b.label)
                        Text(
                            Format.value(current.type, b.value, current.unit),
                            fontWeight = FontWeight.SemiBold,
                            color = if (b.count > 0) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    HorizontalDivider()
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
