package com.ir0.iptv.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.ir0.iptv.app.R

enum class Accento(val etichetta: String, val colore: Color) {
    ROSSO("Rosso", Color(0xFFE23B3B)),
    AMBRA("Ambra", Color(0xFFFFB454)),
    BLU("Blu", Color(0xFF60A5FA)),
    VERDE("Verde", Color(0xFF4ADE80)),
    ROSA("Rosa", Color(0xFFF472B6));

    companion object {
        fun daNome(nome: String?): Accento = entries.firstOrNull { it.name == nome } ?: ROSSO
    }
}

/** Il colore di accento scelto nelle Impostazioni; il resto della palette resta fisso. */
val LocalAccento = compositionLocalOf { Accento.ROSSO.colore }

/** Palette fissa, un'unica famiglia di grigi freddi: l'accento resta l'unico colore "vivo". */
object Palette {
    val inchiostro = Color(0xFF0A0B0E)
    val superficie = Color(0xFF17191F)
    val superficieAlta = Color(0xFF1E2027)
    val testo = Color(0xFFF5F5F2)
    val testoSecondario = Color(0xFFA3A7B0)
    val testoTerziario = Color(0xFF6B707B)
}

/** Outfit (OFL, in res/font): geometrica con carattere, leggibile a distanza dal divano. */
val Outfit = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
    Font(R.font.outfit_extrabold, FontWeight.Black)
)

private fun TextStyle.conOutfit() = copy(fontFamily = Outfit)

private val tipografia = Typography().run {
    Typography(
        displayLarge = displayLarge.conOutfit(),
        displayMedium = displayMedium.conOutfit(),
        displaySmall = displaySmall.conOutfit(),
        headlineLarge = headlineLarge.conOutfit(),
        headlineMedium = headlineMedium.conOutfit(),
        headlineSmall = headlineSmall.conOutfit(),
        titleLarge = titleLarge.conOutfit(),
        titleMedium = titleMedium.conOutfit(),
        titleSmall = titleSmall.conOutfit(),
        bodyLarge = bodyLarge.conOutfit(),
        bodyMedium = bodyMedium.conOutfit(),
        bodySmall = bodySmall.conOutfit(),
        labelLarge = labelLarge.conOutfit(),
        labelMedium = labelMedium.conOutfit(),
        labelSmall = labelSmall.conOutfit()
    )
}

/** Radice del tema: ogni `Text` dell'app eredita Outfit da qui (anche dentro i `MaterialTheme {}`
 * annidati, che senza argomenti riprendono la tipografia corrente). */
@Composable
fun TemaIptv(accento: Color, contenuto: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = accento,
            background = Palette.inchiostro,
            surface = Palette.superficie,
            onBackground = Palette.testo,
            onSurface = Palette.testo
        ),
        typography = tipografia
    ) {
        // Solo la famiglia, niente interlinea/tracking di bodyLarge: le schermate a radice sono
        // state misurate con lo stile di testo di default, e cosi' restano.
        ProvideTextStyle(TextStyle(fontFamily = Outfit), content = contenuto)
    }
}
