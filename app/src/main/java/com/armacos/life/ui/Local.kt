package com.armacos.life.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.armacos.life.AppContainer

/** Permet à n'importe quel écran d'accéder au repository sans le passer en paramètre. */
val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("AppContainer non fourni")
}
