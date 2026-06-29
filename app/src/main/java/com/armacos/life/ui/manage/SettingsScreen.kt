package com.armacos.life.ui.manage

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.armacos.life.domain.DayKey
import com.armacos.life.ui.LocalAppContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val container = LocalAppContainer.current
    val backup = container.backupManager
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = backup.exportJson()
                    context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                }.onSuccess {
                    Toast.makeText(context, "Sauvegarde exportée ✅", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Échec de l'export", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val text = context.contentResolver.openInputStream(uri)
                        ?.bufferedReader()?.use { it.readText() }
                    if (text != null) backup.importJson(text)
                    container.refreshWidget()
                }.onSuccess {
                    Toast.makeText(context, "Sauvegarde importée ✅", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Fichier invalide", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réglages & sauvegarde") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Retour") }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            InfoCard("Sauvegarde locale") {
                Text(
                    "Toutes tes données restent sur ce téléphone (aucun cloud, 100 % privé). " +
                        "Exporte un fichier JSON pour les sauvegarder ou changer de téléphone.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Button(
                        onClick = { exportLauncher.launch("armacoslife-${DayKey.today()}.json") },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Exporter")
                    }
                    Spacer(Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Importer")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            InfoCard("Comment ça marche") {
                Bullet("Chaque stat se crée en 2 taps : un nom, un type. Rien n'est codé en dur.")
                Bullet("La journée se réinitialise à minuit ; l'historique, lui, est conservé.")
                Bullet("Appui long sur une carte « Aujourd'hui » → rétrospective de la stat.")
                Bullet("Le widget d'écran d'accueil note les compteurs en un seul tap.")
            }

            Spacer(Modifier.height(16.dp))
            InfoCard("Bientôt (V2)") {
                Bullet("Trajet GPS de la journée (carte des lieux).")
                Bullet("Temps passé par application.")
                Bullet("Activité de messagerie via les notifications reçues.")
                Spacer(Modifier.height(8.dp))
                Text(
                    "À noter : Android interdit de lire le contenu des messages que tu envoies " +
                        "dans WhatsApp, Insta, etc. Le plus proche possible sera de compter les " +
                        "notifications reçues par appli — jamais le texte envoyé.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "ArmaCos Life · v1.0",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("• ", style = MaterialTheme.typography.bodyMedium)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}
