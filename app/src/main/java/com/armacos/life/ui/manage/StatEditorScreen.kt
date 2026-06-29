package com.armacos.life.ui.manage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.armacos.life.data.entity.Aggregation
import com.armacos.life.data.entity.StatDefinition
import com.armacos.life.data.entity.StatType
import com.armacos.life.ui.LocalAppContainer
import com.armacos.life.ui.components.EmojiChoices
import com.armacos.life.ui.components.StatPalette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun StatEditorScreen(statId: Long?, onDone: () -> Unit) {
    val container = LocalAppContainer.current
    val repo = container.repository
    val scope = rememberCoroutineScope()

    var editing by remember { mutableStateOf<StatDefinition?>(null) }
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("📊") }
    var type by remember { mutableStateOf(StatType.COUNTER) }
    var colorArgb by remember { mutableStateOf(StatPalette.first()) }
    var unit by remember { mutableStateOf("") }
    var aggregation by remember { mutableStateOf(Aggregation.SUM) }
    var step by remember { mutableStateOf("1") }
    var goal by remember { mutableStateOf("") }
    var presets by remember { mutableStateOf("") }
    var options by remember { mutableStateOf("") }
    var pinned by remember { mutableStateOf(true) }
    var showAdvanced by remember { mutableStateOf(false) }

    LaunchedEffect(statId) {
        if (statId != null) {
            repo.stat(statId)?.let { s ->
                editing = s
                name = s.name
                emoji = s.emoji
                type = s.type
                colorArgb = s.colorArgb
                unit = s.unit
                aggregation = s.aggregation
                step = com.armacos.life.domain.Format.number(s.step)
                goal = s.dailyGoal?.let { com.armacos.life.domain.Format.number(it) } ?: ""
                presets = s.quickAddPresets.joinToString(", ") { com.armacos.life.domain.Format.number(it) }
                options = s.choiceOptions.joinToString(", ")
                pinned = s.pinnedToWidget
            }
        }
    }

    fun applyType(newType: StatType) {
        type = newType
        aggregation = newType.defaultAggregation
        if (unit.isBlank() || editing == null) unit = newType.defaultUnit
    }

    fun save() {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        val parsedPresets = presets.split(",", ";", " ")
            .mapNotNull { it.trim().replace(',', '.').toDoubleOrNull() }
        val parsedOptions = options.split(",", ";").map { it.trim() }.filter { it.isNotEmpty() }
        val base = editing ?: StatDefinition(name = cleanName)
        val updated = base.copy(
            name = cleanName,
            emoji = emoji,
            type = type,
            colorArgb = colorArgb,
            unit = unit.trim(),
            aggregation = aggregation,
            step = step.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0 } ?: 1.0,
            dailyGoal = goal.replace(',', '.').toDoubleOrNull(),
            quickAddPresets = parsedPresets,
            choiceOptions = parsedOptions,
            pinnedToWidget = pinned,
        )
        scope.launch {
            if (editing == null) repo.addStat(updated) else repo.updateStat(updated)
            container.refreshWidget()
            onDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing == null) "Nouvelle stat" else "Modifier") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.ArrowBack, "Retour") }
                },
                actions = {
                    if (editing != null) {
                        IconButton(onClick = {
                            scope.launch {
                                editing?.let { repo.deleteStat(it) }
                                container.refreshWidget()
                                onDone()
                            }
                        }) { Icon(Icons.Filled.Delete, "Supprimer") }
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            // 1) Nom + emoji
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 40.sp, modifier = Modifier.padding(end = 12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la stat") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Emoji")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                EmojiChoices.forEach { e ->
                    val selected = e == emoji
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { emoji = e }
                            .border(
                                BorderStroke(if (selected) 2.dp else 0.dp, MaterialTheme.colorScheme.primary),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) { Text(e, fontSize = 22.sp) }
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Type")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatType.pickerOrder.forEach { t ->
                    ElevatedFilterChip(
                        selected = type == t,
                        onClick = { applyType(t) },
                        label = { Text("${t.emojiHint} ${t.label}") },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionLabel("Couleur")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatPalette.forEach { c ->
                    val selected = c == colorArgb
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { colorArgb = c }
                            .background(Color(c), CircleShape)
                            .border(
                                BorderStroke(if (selected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface),
                                CircleShape,
                            ),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Épingler au widget", modifier = Modifier.weight(1f))
                Switch(checked = pinned, onCheckedChange = { pinned = it })
            }

            Spacer(Modifier.height(8.dp))
            Text(
                if (showAdvanced) "Masquer les options avancées" else "Options avancées (facultatif)",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { showAdvanced = !showAdvanced }.padding(vertical = 8.dp),
            )

            if (showAdvanced) {
                AdvancedSection(
                    type = type,
                    unit = unit, onUnit = { unit = it },
                    aggregation = aggregation, onAggregation = { aggregation = it },
                    step = step, onStep = { step = it },
                    goal = goal, onGoal = { goal = it },
                    presets = presets, onPresets = { presets = it },
                    options = options, onOptions = { options = it },
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { save() },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(54.dp),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(if (editing == null) "Créer la stat" else "Enregistrer", fontSize = 16.sp)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSection(
    type: StatType,
    unit: String, onUnit: (String) -> Unit,
    aggregation: Aggregation, onAggregation: (Aggregation) -> Unit,
    step: String, onStep: (String) -> Unit,
    goal: String, onGoal: (String) -> Unit,
    presets: String, onPresets: (String) -> Unit,
    options: String, onOptions: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = unit, onValueChange = onUnit,
                label = { Text("Unité (€, min, km, verres…)") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(12.dp))
            SectionLabel("Résumé (pour les rétrospectives)")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Aggregation.values().forEach { agg ->
                    FilterChip(
                        selected = aggregation == agg,
                        onClick = { onAggregation(agg) },
                        label = { Text(agg.label) },
                    )
                }
            }

            if (type == StatType.COUNTER) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = step, onValueChange = onStep,
                    label = { Text("Incrément par tap (+1, +0.5…)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = goal, onValueChange = onGoal,
                label = { Text("Objectif quotidien (facultatif)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true, modifier = Modifier.fillMaxWidth(),
            )

            if (type == StatType.MONEY || type == StatType.NUMBER || type == StatType.DURATION || type == StatType.COUNTER) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = presets, onValueChange = onPresets,
                    label = { Text("Accès rapides, séparés par des virgules (5, 10, 20)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                )
            }

            if (type == StatType.CHOICE) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = options, onValueChange = onOptions,
                    label = { Text("Options, séparées par des virgules") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
