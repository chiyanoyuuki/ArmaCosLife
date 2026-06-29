package com.armacos.life.ui.nav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.armacos.life.ui.history.DayDetailScreen
import com.armacos.life.ui.history.HistoryScreen
import com.armacos.life.ui.history.RetrospectiveScreen
import com.armacos.life.ui.entry.EntryScreen
import com.armacos.life.ui.manage.ManageScreen
import com.armacos.life.ui.manage.SettingsScreen
import com.armacos.life.ui.manage.StatEditorScreen
import com.armacos.life.ui.people.PeopleScreen
import com.armacos.life.ui.today.TodayScreen
import com.armacos.life.ui.trajet.TrajetScreen

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.TODAY, "Aujourd'hui", Icons.Filled.Today),
    BottomItem(Routes.HISTORY, "Historique", Icons.Filled.Insights),
    BottomItem(Routes.TRAJET, "Trajet", Icons.Filled.Map),
    BottomItem(Routes.MANAGE, "Gérer", Icons.Filled.Tune),
)

@Composable
fun ArmaNavHost(deepLinkStatId: Long?, onDeepLinkConsumed: () -> Unit) {
    val navController = rememberNavController()

    LaunchedEffect(deepLinkStatId) {
        if (deepLinkStatId != null) {
            navController.navigate(Routes.entry(deepLinkStatId))
            onDeepLinkConsumed()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(Routes.TODAY) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.TODAY,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.TODAY) {
                TodayScreen(
                    onOpenEntry = { navController.navigate(Routes.entry(it)) },
                    onNewStat = { navController.navigate(Routes.editor()) },
                    onOpenRetro = { navController.navigate(Routes.retro(it)) },
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(
                    onOpenRetro = { navController.navigate(Routes.retro(it)) },
                    onOpenDay = { navController.navigate(Routes.day(it)) },
                )
            }
            composable(Routes.TRAJET) {
                TrajetScreen()
            }
            composable(Routes.MANAGE) {
                ManageScreen(
                    onNewStat = { navController.navigate(Routes.editor()) },
                    onEditStat = { navController.navigate(Routes.editor(it)) },
                    onOpenPeople = { navController.navigate(Routes.PEOPLE) },
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                )
            }
            composable(
                route = "${Routes.ENTRY}/{statId}",
                arguments = listOf(navArgument("statId") { type = NavType.LongType }),
            ) { entry ->
                val statId = entry.arguments?.getLong("statId") ?: return@composable
                EntryScreen(statId = statId, onClose = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.EDITOR}?statId={statId}",
                arguments = listOf(navArgument("statId") { type = NavType.LongType; defaultValue = -1L }),
            ) { entry ->
                val statId = entry.arguments?.getLong("statId")?.takeIf { it > 0 }
                StatEditorScreen(statId = statId, onDone = { navController.popBackStack() })
            }
            composable(
                route = "${Routes.RETRO}/{statId}",
                arguments = listOf(navArgument("statId") { type = NavType.LongType }),
            ) { entry ->
                val statId = entry.arguments?.getLong("statId") ?: return@composable
                RetrospectiveScreen(
                    statId = statId,
                    onBack = { navController.popBackStack() },
                    onOpenDay = { navController.navigate(Routes.day(it)) },
                )
            }
            composable(
                route = "${Routes.DAY}/{dayKey}",
                arguments = listOf(navArgument("dayKey") { type = NavType.StringType }),
            ) { entry ->
                val dayKey = entry.arguments?.getString("dayKey") ?: return@composable
                DayDetailScreen(dayKey = dayKey, onBack = { navController.popBackStack() })
            }
            composable(Routes.PEOPLE) {
                PeopleScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
