package com.ir0.iptv.app

import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.domain.catalog.ContentCard

sealed interface Screen {
    data object Home : Screen
    data object Canali : Screen
    data object Film : Screen
    data object Serie : Screen
    data object Preferiti : Screen
    data object Impostazioni : Screen
    data class Detail(val card: ContentCard) : Screen
    data class Player(val richiesta: RichiestaRiproduzione, val posizioneIniziale: Long) : Screen
}

/** Maps a [Sidebar] icon index (0-4 top icons, 5 = pinned Impostazioni) to its destination. */
fun screenForSidebarIndex(index: Int): Screen = when (index) {
    0 -> Screen.Home
    1 -> Screen.Canali
    2 -> Screen.Film
    3 -> Screen.Serie
    4 -> Screen.Preferiti
    else -> Screen.Impostazioni
}

/** Inverse of [screenForSidebarIndex]; -1 for destinations reached by drilling into content
 * (Detail, Player) rather than by picking a sidebar icon directly. */
fun sidebarIndexForScreen(screen: Screen): Int = when (screen) {
    Screen.Home -> 0
    Screen.Canali -> 1
    Screen.Film -> 2
    Screen.Serie -> 3
    Screen.Preferiti -> 4
    Screen.Impostazioni -> 5
    else -> -1
}
