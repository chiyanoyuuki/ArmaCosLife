package com.armacos.life.ui.today

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatType
import com.armacos.life.domain.Aggregator
import com.armacos.life.domain.DayKey
import com.armacos.life.domain.Format
import com.armacos.life.ui.LocalAppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onOpenEntry: (Long) -> Unit,
    onNewStat: () -> Unit,
    onOpenRetro: (Long) -> Unit,
) {
    val container = LocalAppContainer.current
    val repo = container.repository
    val scope = rememberCoroutineScope()

    val statsFlow = remember { repo.activeStats() }
    val entriesFlow = remember { repo.todayEntries() }
    val stats by statsFlow.collectAsState(initial = emptyList())
    val todayEntries by entriesFlow.collectAsState(initial = emptyList())
    val grouped = remember(todayEntries) { todayEntries.groupBy { it.statId } }
    val today = remember { DayKey.today() }

    val moneyToday = remember(stats, grouped) {
        stats.filter { it.type == StatType.MONEY }
            .sumOf { Aggregator.dayValue(grouped[it.id].orEmpty(), it.aggregation) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Aujourd'hui", fontWeight = FontWeight.Bold)
                        Text(
                            Format.longDate(today),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewStat,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nouvelle stat") },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(span = { GridItemSpan(2) }) {
                DaySummaryCard(
                    money = moneyToday,
                    entries = todayEntries.size,
                    statCount = stats.size,
                )
            }
            items(stats, key = { it.id }) { stat ->
                val list = grouped[stat.id].orEmpty()
                val value = Aggregator.dayValue(list, stat.aggregation)
                StatCard(
                    stat = stat,
                    value = value,
                    count = list.size,
                    onIncrement = {
                        scope.launch {
                            repo.increment(stat.id)
                            container.refreshWidget()
                        }
                    },
                    onClick = { onOpenEntry(stat.id) },
                    onLongClick = { onOpenRetro(stat.id) },
                )
            }
        }
    }
}

@Composable
private fun DaySummaryCard(money: Double, entries: Int, statCount: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SummaryItem(if (money > 0) "${Format.number(money)} €" else "—", "Dépensé")
            SummaryItem("$entries", "Saisies")
            SummaryItem("$statCount", "Stats suivies")
        }
    }
}

@Composable
private fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun StatCard(
    stat: StatDefinition,
    value: Double,
    count: Int,
    onIncrement: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val color = Color(stat.colorArgb)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 132.dp)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
    ) {
        Column(modifier = Modifier.padding(14.dp).fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stat.emoji, fontSize = 24.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    stat.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = Format.value(stat.type, value, stat.unit),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            if (!stat.type.numeric && count > 0) {
                Text(
                    "$count fois",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val goal = stat.dailyGoal
                if (goal != null && goal > 0) {
                    LinearProgressIndicator(
                        progress = { (value / goal).toFloat().coerceIn(0f, 1f) },
                        color = color,
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (stat.type == StatType.COUNTER) {
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(40.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = color),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Ajouter")
                    }
                }
            }
        }
    }
}
