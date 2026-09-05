package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.playback.Visto

private const val CATEGORIA_ALTRO = "Altro"

@Composable
fun CanaliScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    SchermataCategoria("Canali", catalogo.canali, visti, onContenutoClick, onContenutoLongClick)
}

@Composable
fun FilmScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    SchermataOrganizzataPerCategoria(
        titolo = "Film",
        contenuti = catalogo.film,
        categoriaDi = { it.categoria },
        visti = visti,
        onClick = onContenutoClick,
        onLongClick = onContenutoLongClick
    )
}

@Composable
fun SerieScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    SchermataOrganizzataPerCategoria(
        titolo = "Serie",
        contenuti = catalogo.serie,
        categoriaDi = { it.categoria },
        visti = visti,
        onClick = onContenutoClick,
        onLongClick = onContenutoLongClick
    )
}

@Composable
private fun SchermataCategoria(
    titolo: String,
    contenuti: List<ContentCard>,
    visti: List<Visto>,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit
) {
    if (contenuti.isEmpty()) {
        SchermataVuota("Nessun contenuto trovato in $titolo.")
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        GrigliaContenuti(
            contenuti = contenuti,
            visti = visti,
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}

/** Film e Serie si sfogliano per categoria (genere Xtream, o gruppo M3U per i Film) invece che
 * come un'unica griglia: piu' facile orientarsi quando il catalogo e' grande. Le Serie M3U non
 * hanno una categoria distinta dal proprio nome (ADR 0002) e cadono tutte in "Altro". */
@Composable
private fun <T : ContentCard> SchermataOrganizzataPerCategoria(
    titolo: String,
    contenuti: List<T>,
    categoriaDi: (T) -> String?,
    visti: List<Visto>,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit
) {
    if (contenuti.isEmpty()) {
        SchermataVuota("Nessun contenuto trovato in $titolo.")
        return
    }
    val gruppi = remember(contenuti) {
        contenuti.groupBy { card ->
            categoriaDi(card)?.trim()?.takeIf { it.isNotBlank() } ?: CATEGORIA_ALTRO
        }.toSortedMap(compareBy { if (it == CATEGORIA_ALTRO) "￿" else it })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = titolo,
            color = Color(0xFFF2F2F0),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            items(gruppi.entries.toList()) { (categoria, contenutiCategoria) ->
                RigaContenuti(
                    titolo = categoria,
                    contenuti = contenutiCategoria,
                    visti = visti,
                    chiaveDaFocalizzare = null,
                    focusRequester = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                )
            }
        }
    }
}
