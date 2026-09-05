package com.ir0.iptv.app.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

enum class Accento(val etichetta: String, val colore: Color) {
    AMBRA("Ambra", Color(0xFFFFB454)),
    BLU("Blu", Color(0xFF60A5FA)),
    VERDE("Verde", Color(0xFF4ADE80)),
    ROSA("Rosa", Color(0xFFF472B6));

    companion object {
        fun daNome(nome: String?): Accento = entries.firstOrNull { it.name == nome } ?: AMBRA
    }
}

/** Il colore di accento scelto nelle Impostazioni; il resto della palette resta fisso. */
val LocalAccento = compositionLocalOf { Accento.AMBRA.colore }
