package com.armacos.life.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
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
import com.armacos.life.domain.DayKey
import com.armacos.life.domain.Format
import com.armacos.life.ui.LocalAppContainer
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(onOpenRetro: (Long) -> Unit, onOpenDay: (String) -> Unit) {
    val repo = LocalAppContainer.current.repository
    val statsFlow = remember { repo.activeStats() }
    val stats by statsFlow.collectAsState(initial = emptyList())
    val recentDays = remember {
        (0 until 14).map { DayKey.fromLocalDate(LocalDate.now().minusDays(it.toLong())) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Historique") }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            item {
                SectionTitle("Parcourir un jour")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(recentDays) { day ->
                        AssistChip(
                            onClick = { onOpenDay(day) },
                            label = { Text(Format.smartDate(day)) },
                        )
                    }
                }
            }

            item { SectionTitle("Rétrospective par stat") }
            items(stats, key = { it.id }) { stat ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .clickable { onOpenRetro(stat.id) },
                ) {
                    ListItem(
                        leadingContent = { Text(stat.emoji, fontSize = 26.sp) },
                        headlineContent = { Text(stat.name, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text("Résumé : ${stat.aggregation.label}") },
                        trailingContent = {
                            Icon(Icons.Filled.ChevronRight, contentDescription = null)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
