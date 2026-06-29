package com.armacos.life.ui.entry

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armacos.life.data.entity.Person
import com.armacos.life.data.entity.Place
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatEntry
import com.armacos.life.data.entity.StatType
import com.armacos.life.domain.Aggregator
import com.armacos.life.domain.DayKey
import com.armacos.life.domain.Format
import com.armacos.life.ui.LocalAppContainer
import com.armacos.life.ui.components.NameDialog
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(statId: Long, onClose: () -> Unit) {
    val container = LocalAppContainer.current
    val repo = container.repository
    val scope = rememberCoroutineScope()
    val today = remember { DayKey.today() }

    val statFlow = remember { repo.statFlow(statId) }
    val entriesFlow = remember { repo.entriesForStatDay(statId, today) }
    val stat by statFlow.collectAsState(initial = null)
    val dayEntries by entriesFlow.collectAsState(initial = emptyList())

    var note by remember { mutableStateOf("") }

    fun submit(
        num: Double? = null,
        text: String? = null,
        choice: String? = null,
        people: List<Long> = emptyList(),
        place: Long? = null,
        close: Boolean = true,
    ) {
        scope.launch {
            repo.log(
                statId = statId,
                numericValue = num,
                text = text,
                choice = choice,
                personIds = people,
                placeId = place,
                note = note.ifBlank { null },
            )
            container.refreshWidget()
            if (close) onClose()
        }
    }

    val current = stat
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current?.let { "${it.emoji}  ${it.name}" } ?: "Saisie") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.Close, contentDescription = "Fermer")
                    }
                },
            )
        },
    ) { padding ->
        if (current == null) {
            Column(Modifier.padding(padding).padding(24.dp)) { Text("Chargement…") }
            return@Scaffold
        }

        val value = Aggregator.dayValue(dayEntries, current.aggregation)
        val color = Color(current.colorArgb)

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            // Valeur du jour, mise à jour en direct
            Text(
                "Aujourd'hui",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                Format.value(current.type, value, current.unit),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
            Spacer(Modifier.height(20.dp))

            when (current.type) {
                StatType.COUNTER -> CounterInput(current, dayEntries, color)
                StatType.MONEY, StatType.NUMBER, StatType.DURATION -> AmountInput(current) { submit(num = it) }
                StatType.RATING -> RatingInput { submit(num = it.toDouble()) }
                StatType.BOOLEAN -> BooleanInput { submit(num = if (it) 1.0 else 0.0) }
                StatType.CHOICE -> ChoiceInput(current) { choice -> submit(choice = choice) }
                StatType.TEXT -> TextInput { submit(text = it) }
                StatType.PEOPLE -> PeopleInput { ids -> submit(people = ids) }
                StatType.PLACE -> PlaceInput { id -> submit(place = id) }
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (facultatif)") },
                modifier = Modifier.fillMaxWidth(),
            )

            if (dayEntries.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Saisies du jour",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                dayEntries.forEach { e ->
                    EntryRow(current, e) {
                        scope.launch {
                            repo.deleteEntry(e)
                            container.refreshWidget()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CounterInput(
    stat: StatDefinition,
    dayEntries: List<StatEntry>,
    color: Color,
) {
    val container = LocalAppContainer.current
    val repo = container.repository
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedButton(
            onClick = {
                scope.launch {
                    dayEntries.maxByOrNull { it.timestamp }?.let { repo.deleteEntry(it) }
                    container.refreshWidget()
                }
            },
            modifier = Modifier.size(64.dp),
        ) { Icon(Icons.Filled.Remove, contentDescription = "Retirer") }

        Button(
            onClick = {
                scope.launch {
                    repo.increment(stat.id)
                    container.refreshWidget()
                }
            },
            modifier = Modifier
                .weight(1f)
                .height(64.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Ajouter +${Format.number(stat.step)}", fontSize = 18.sp)
        }
    }
    if (stat.quickAddPresets.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        PresetChips(stat.quickAddPresets, stat.unit) { preset ->
            scope.launch {
                repo.increment(stat.id, amount = preset)
                container.refreshWidget()
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountInput(stat: StatDefinition, onSubmit: (Double) -> Unit) {
    var text by remember { mutableStateOf("") }
    Column {
        if (stat.quickAddPresets.isNotEmpty()) {
            PresetChips(stat.quickAddPresets, stat.unit) { onSubmit(it) }
            Spacer(Modifier.height(12.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = {
                    Text(
                        when (stat.type) {
                            StatType.MONEY -> "Montant (${stat.unit.ifBlank { "€" }})"
                            StatType.DURATION -> "Durée (minutes)"
                            else -> "Valeur ${stat.unit}".trim()
                        },
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    text.replace(',', '.').toDoubleOrNull()?.let(onSubmit)
                },
                modifier = Modifier.height(56.dp),
            ) { Text("Ajouter") }
        }
    }
}

@Composable
private fun RatingInput(onSubmit: (Int) -> Unit) {
    var rating by remember { mutableIntStateOf(0) }
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            (1..5).forEach { i ->
                IconButton(onClick = { rating = i }, modifier = Modifier.size(52.dp)) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                        contentDescription = "$i",
                        tint = Color(0xFFEAB308),
                        modifier = Modifier.size(40.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { if (rating > 0) onSubmit(rating) }, enabled = rating > 0) {
            Text("Enregistrer la note")
        }
    }
}

@Composable
private fun BooleanInput(onSubmit: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { onSubmit(true) }, modifier = Modifier.weight(1f).height(60.dp)) {
            Text("Oui", fontSize = 18.sp)
        }
        OutlinedButton(onClick = { onSubmit(false) }, modifier = Modifier.weight(1f).height(60.dp)) {
            Text("Non", fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChoiceInput(stat: StatDefinition, onSubmit: (String) -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    var showAdd by remember { mutableStateOf(false) }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        stat.choiceOptions.forEach { option ->
            AssistChip(onClick = { onSubmit(option) }, label = { Text(option) })
        }
        AssistChip(
            onClick = { showAdd = true },
            label = { Text("Autre…") },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
        )
    }
    if (showAdd) {
        NameDialog(title = "Nouvelle option", onDismiss = { showAdd = false }) { name ->
            showAdd = false
            scope.launch {
                container.repository.updateStat(stat.copy(choiceOptions = stat.choiceOptions + name))
                onSubmit(name)
            }
        }
    }
}

@Composable
private fun TextInput(onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Texte") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = { if (text.isNotBlank()) onSubmit(text) }, enabled = text.isNotBlank()) {
            Text("Enregistrer")
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PeopleInput(onSubmit: (List<Long>) -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val peopleFlow = remember { container.repository.peopleFlow() }
    val people by peopleFlow.collectAsState(initial = emptyList())
    var selected by remember { mutableStateOf(setOf<Long>()) }
    var showAdd by remember { mutableStateOf(false) }

    Column {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            people.forEach { p ->
                FilterChip(
                    selected = p.id in selected,
                    onClick = {
                        selected = if (p.id in selected) selected - p.id else selected + p.id
                    },
                    label = { Text("${p.emoji} ${p.name}") },
                )
            }
            AssistChip(
                onClick = { showAdd = true },
                label = { Text("Ajouter") },
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { if (selected.isNotEmpty()) onSubmit(selected.toList()) }, enabled = selected.isNotEmpty()) {
            Text("Enregistrer (${selected.size})")
        }
    }
    if (showAdd) {
        NameDialog(title = "Nouvelle personne", onDismiss = { showAdd = false }) { name ->
            showAdd = false
            scope.launch { container.repository.addPerson(Person(name = name)) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PlaceInput(onSubmit: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val scope = rememberCoroutineScope()
    val placesFlow = remember { container.repository.placesFlow() }
    val places by placesFlow.collectAsState(initial = emptyList())
    var showAdd by remember { mutableStateOf(false) }

    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        places.forEach { place ->
            AssistChip(onClick = { onSubmit(place.id) }, label = { Text("${place.emoji} ${place.name}") })
        }
        AssistChip(
            onClick = { showAdd = true },
            label = { Text("Nouveau lieu") },
            leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
        )
    }
    if (showAdd) {
        NameDialog(title = "Nouveau lieu", onDismiss = { showAdd = false }) { name ->
            showAdd = false
            scope.launch {
                val id = container.repository.addPlace(Place(name = name))
                onSubmit(id)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun PresetChips(presets: List<Double>, unit: String, onPick: (Double) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        presets.forEach { preset ->
            FilledTonalButton(onClick = { onPick(preset) }) {
                Text("+${Format.number(preset)} ${unit}".trim())
            }
        }
    }
}

@Composable
private fun EntryRow(stat: StatDefinition, entry: StatEntry, onDelete: () -> Unit) {
    val time = remember(entry.timestamp) {
        Instant.ofEpochMilli(entry.timestamp).atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("HH:mm"))
    }
    val label = when (stat.type) {
        StatType.TEXT -> entry.textValue ?: ""
        StatType.CHOICE -> entry.choiceValue ?: ""
        StatType.PEOPLE -> "${entry.personIds.size} personne(s)"
        StatType.PLACE -> "Lieu"
        else -> Format.value(stat.type, entry.numericValue ?: 1.0, stat.unit)
    }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$time" + (entry.note?.let { " · $it" } ?: ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Supprimer")
            }
        }
    }
}
