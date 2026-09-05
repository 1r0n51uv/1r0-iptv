package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.catalog.RicercaCatalogo
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.Visto

private val ricerca = RicercaCatalogo()
private val registro = RegistroVisti()
private val elencoPreferiti = ElencoPreferiti()

@Composable
fun SearchScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var infocato by remember { mutableStateOf(false) }
    val risultati = remember(query, catalogo) { ricerca.cerca(catalogo, query) }
    val canali = remember(risultati) { risultati.filterIsInstance<ContentCard.Canale>() }
    val film = remember(risultati) { risultati.filterIsInstance<ContentCard.Film>() }
    val serie = remember(risultati) { risultati.filterIsInstance<ContentCard.SerieCard>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A)),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF2F2F0), fontSize = 18.sp),
            cursorBrush = SolidColor(LocalAccento.current),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp)
                .onFocusChanged { infocato = it.isFocused }
                .background(Color(0xFF1F232A), RoundedCornerShape(10.dp))
                .border(
                    2.dp,
                    if (infocato) LocalAccento.current else Color(0xFF262B33),
                    RoundedCornerShape(10.dp)
                )
                .padding(horizontal = 18.dp, vertical = 14.dp),
            decorationBox = { campo ->
                if (query.isEmpty()) {
                    Text("Cerca fra Canali, Film e Serie…", color = Color(0xFF6D7380), fontSize = 18.sp)
                }
                campo()
            }
        )

        when {
            query.isBlank() -> Text(
                text = "Digita per cercare nel catalogo.",
                color = Color(0xFF6D7380),
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            risultati.isEmpty() -> Text(
                text = "Nessun risultato per \"$query\".",
                color = Color(0xFF9AA0AA),
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            else -> LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                item {
                    RigaContenuti(
                        titolo = "Canali",
                        contenuti = canali,
                        visti = visti,
                        personalizzazioni = personalizzazioni,
                        chiaveDaFocalizzare = null,
                        focusRequester = null,
                        onClick = onContenutoClick,
                        onLongClick = onContenutoLongClick
                    )
                }
                item {
                    RigaContenuti(
                        titolo = "Film",
                        contenuti = film,
                        visti = visti,
                        personalizzazioni = personalizzazioni,
                        chiaveDaFocalizzare = null,
                        focusRequester = null,
                        onClick = onContenutoClick,
                        onLongClick = onContenutoLongClick
                    )
                }
                item {
                    RigaContenuti(
                        titolo = "Serie",
                        contenuti = serie,
                        visti = visti,
                        personalizzazioni = personalizzazioni,
                        chiaveDaFocalizzare = null,
                        focusRequester = null,
                        onClick = onContenutoClick,
                        onLongClick = onContenutoLongClick
                    )
                }
            }
        }
    }
}

@Composable
fun GrigliaContenuti(
    contenuti: List<ContentCard>,
    visti: List<Visto>,
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        // Margini extra perche' la card in focus, ingrandita, non venga tagliata ai bordi.
        contentPadding = PaddingValues(start = 6.dp, end = 6.dp, top = 12.dp, bottom = 28.dp)
    ) {
        items(contenuti) { card ->
            CardContenuto(
                card = card,
                percentuale = registro.percentuale(visti, card.chiaveIdentita),
                preferito = elencoPreferiti.preferito(personalizzazioni, card),
                onClick = { onClick(card) },
                onLongClick = { onLongClick(card) }
            )
        }
    }
}
