package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.RicercaCatalogo
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.Visto

private val ricerca = RicercaCatalogo()
private val registro = RegistroVisti()

@Composable
fun SearchScreen(
    catalogo: ContentCatalog,
    visti: List<Visto>,
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var infocato by remember { mutableStateOf(false) }
    val risultati = remember(query, catalogo) { ricerca.cerca(catalogo, query) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFFF2F2F0), fontSize = 18.sp),
            cursorBrush = SolidColor(Color(0xFFFFB454)),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { infocato = it.isFocused }
                .background(Color(0xFF1F232A), RoundedCornerShape(10.dp))
                .border(
                    2.dp,
                    if (infocato) Color(0xFFFFB454) else Color(0xFF262B33),
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
                fontSize = 15.sp
            )

            risultati.isEmpty() -> Text(
                text = "Nessun risultato per \"$query\".",
                color = Color(0xFF9AA0AA),
                fontSize = 15.sp
            )

            else -> GrigliaContenuti(
                contenuti = risultati,
                visti = visti,
                onClick = onContenutoClick,
                onLongClick = onContenutoLongClick
            )
        }
    }
}

@Composable
fun GrigliaContenuti(
    contenuti: List<ContentCard>,
    visti: List<Visto>,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit = {}
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        items(contenuti) { card ->
            CardContenuto(
                card = card,
                percentuale = registro.percentuale(visti, card.chiaveIdentita),
                onClick = { onClick(card) },
                onLongClick = { onLongClick(card) }
            )
        }
    }
}
