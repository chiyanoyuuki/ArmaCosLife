package com.armacos.life.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.armacos.life.ArmaApp
import com.armacos.life.R
import com.armacos.life.domain.WidgetItem
import com.armacos.life.domain.WidgetSnapshot
import com.armacos.life.ui.MainActivity

/** Le widget d'écran d'accueil (Glance). Recharge ses données à chaque updateAll(). */
class ArmaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as ArmaApp
        val snapshot = app.container.repository.widgetSnapshot()
        provideContent { WidgetBody(snapshot) }
    }
}

private val TextDark = ColorProvider(Color(0xFF1C1B1F))
private val TextMuted = ColorProvider(Color(0xFF6B7280))
private val Brand = ColorProvider(Color(0xFF6750A4))

@Composable
private fun WidgetBody(snapshot: WidgetSnapshot) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(R.drawable.widget_background))
            .padding(12.dp),
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    snapshot.dateLabel,
                    style = TextStyle(color = TextDark, fontWeight = FontWeight.Bold, fontSize = 15.sp),
                )
                Text(snapshot.headline, style = TextStyle(color = TextMuted, fontSize = 11.sp), maxLines = 1)
            }
            Text(
                "＋",
                style = TextStyle(color = Brand, fontWeight = FontWeight.Bold, fontSize = 24.sp),
                modifier = GlanceModifier
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                    .padding(horizontal = 8.dp),
            )
        }
        Spacer(GlanceModifier.height(8.dp))

        // Quelques stats épinglées, en lignes de 3 (Column simple = robuste sur tous les lanceurs).
        val rows = snapshot.items.take(9).chunked(3)
        rows.forEach { rowItems ->
            Row(modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp)) {
                rowItems.forEach { item ->
                    Box(modifier = GlanceModifier.defaultWeight()) { Tile(item, context) }
                }
                repeat(3 - rowItems.size) {
                    Box(modifier = GlanceModifier.defaultWeight()) {}
                }
            }
        }
    }
}

@Composable
private fun Tile(item: WidgetItem, context: Context) {
    val action = if (item.isCounter) {
        actionRunCallback<IncrementAction>(actionParametersOf(StatIdKey to item.statId))
    } else {
        actionStartActivity(
            Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_STAT_ID, item.statId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
        )
    }
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(3.dp)
            .background(ImageProvider(R.drawable.widget_tile_background))
            .clickable(action)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(item.emoji, style = TextStyle(fontSize = 20.sp))
        Text(
            item.display,
            style = TextStyle(color = TextDark, fontWeight = FontWeight.Bold, fontSize = 13.sp),
            maxLines = 1,
        )
        Text(item.name, style = TextStyle(color = TextMuted, fontSize = 10.sp), maxLines = 1)
    }
}
