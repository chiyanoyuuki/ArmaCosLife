package com.armacos.life.ui.manage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.armacos.life.ui.LocalAppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    onNewStat: () -> Unit,
    onEditStat: (Long) -> Unit,
    onOpenPeople: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val repo = LocalAppContainer.current.repository
    val statsFlow = remember { repo.allStats() }
    val stats by statsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gérer mes stats") },
                actions = {
                    IconButton(onClick = onOpenPeople) {
                        Icon(Icons.Filled.Groups, contentDescription = "Personnes & lieux")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Réglages")
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
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
            items(stats, key = { it.id }) { stat ->
                Card(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .clickable { onEditStat(stat.id) },
                ) {
                    ListItem(
                        leadingContent = { Text(stat.emoji, fontSize = 26.sp) },
                        headlineContent = {
                            Text(
                                stat.name,
                                fontWeight = FontWeight.SemiBold,
                                color = if (stat.archived) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        supportingContent = {
                            Text(
                                buildString {
                                    append(stat.type.label)
                                    if (stat.archived) append(" · archivée")
                                },
                            )
                        },
                        trailingContent = {
                            if (stat.pinnedToWidget && !stat.archived) {
                                Icon(
                                    Icons.Filled.PushPin,
                                    contentDescription = "Épinglée au widget",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}
