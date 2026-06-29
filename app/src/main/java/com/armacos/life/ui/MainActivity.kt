package com.armacos.life.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import com.armacos.life.ArmaApp
import com.armacos.life.ui.nav.ArmaNavHost
import com.armacos.life.ui.theme.ArmaTheme

class MainActivity : ComponentActivity() {

    private val deepLinkStatId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as ArmaApp
        deepLinkStatId.value = readStatId(intent)

        setContent {
            ArmaTheme {
                CompositionLocalProvider(LocalAppContainer provides app.container) {
                    RequestActivityRecognitionOnce()
                    ArmaNavHost(
                        deepLinkStatId = deepLinkStatId.value,
                        onDeepLinkConsumed = { deepLinkStatId.value = null },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkStatId.value = readStatId(intent)
    }

    private fun readStatId(intent: Intent?): Long? =
        intent?.getLongExtra(EXTRA_STAT_ID, -1L)?.takeIf { it > 0 }

    companion object {
        const val EXTRA_STAT_ID = "stat_id"
    }
}
