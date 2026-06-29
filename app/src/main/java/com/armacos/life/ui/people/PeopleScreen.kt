package com.armacos.life.ui.people

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armacos.life.data.entity.Person
import com.armacos.life.data.entity.Place
import com.armacos.life.ui.LocalAppContainer
import com.armacos.life.ui.components.NameDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeopleScreen(onBack: () -> Unit) {
    val repo = LocalAppContainer.current.repository
    val scope = rememberCoroutineScope()
    val peopleFlow = remember { repo.peopleFlow() }
    val placesFlow = remember { repo.placesFlow() }
    val people by peopleFlow.collectAsState(initial = emptyList())
    val places by placesFlow.collectAsState(initial = emptyList())

    var showAddPerson by remember { mutableStateOf(false) }
    var showAddPlace by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Personnes & lieux") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Retour") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
        ) {
            item { SectionHeader("Personnes") { showAddPerson = true } }
            items(people, key = { "p${it.id}" }) { person ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ListItem(
                        leadingContent = { Text(person.emoji, fontSize = 24.sp) },
                        headlineContent = { Text(person.name) },
                        trailingContent = {
                            IconButton(onClick = { scope.launch { repo.deletePerson(person) } }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        },
                    )
                }
            }

            item { SectionHeader("Lieux") { showAddPlace = true } }
            items(places, key = { "l${it.id}" }) { place ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    ListItem(
                        leadingContent = { Text(place.emoji, fontSize = 24.sp) },
                        headlineContent = { Text(place.name) },
                        trailingContent = {
                            IconButton(onClick = { scope.launch { repo.deletePlace(place) } }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
                            }
                        },
                    )
                }
            }
        }
    }

    if (showAddPerson) {
        NameDialog(title = "Nouvelle personne", onDismiss = { showAddPerson = false }) { name ->
            showAddPerson = false
            scope.launch { repo.addPerson(Person(name = name)) }
        }
    }
    if (showAddPlace) {
        NameDialog(title = "Nouveau lieu", onDismiss = { showAddPlace = false }) { name ->
            showAddPlace = false
            scope.launch { repo.addPlace(Place(name = name)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onAdd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
        )
        TextButton(onClick = onAdd) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("Ajouter")
        }
    }
}
