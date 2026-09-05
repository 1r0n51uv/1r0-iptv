package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ir0.iptv.app.theme.LocalAccento
import coil.compose.AsyncImage
import com.ir0.iptv.domain.catalog.ContentCard

private val LARGHEZZA_CARD_ORIZZONTALE = 200.dp
private val ALTEZZA_CARD_ORIZZONTALE = 112.dp
private val LARGHEZZA_CARD_VERTICALE = 148.dp
private val ALTEZZA_CARD_VERTICALE = 210.dp

/** Canali restano in landscape (frame TV); Film e Serie usano la locandina in verticale. */
private val ContentCard.locandinaVerticale: Boolean get() = this !is ContentCard.Canale

@Composable
fun CardContenuto(
    card: ContentCard,
    percentuale: Int = 0,
    preferito: Boolean = false,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    var infocata by remember { mutableStateOf(false) }
    val larghezza = if (card.locandinaVerticale) LARGHEZZA_CARD_VERTICALE else LARGHEZZA_CARD_ORIZZONTALE
    val altezza = if (card.locandinaVerticale) ALTEZZA_CARD_VERTICALE else ALTEZZA_CARD_ORIZZONTALE
    Column(
        modifier = Modifier.width(larghezza),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val forma = RoundedCornerShape(8.dp)
        Box(
            modifier = Modifier
                .width(larghezza)
                .height(altezza)
                .zoomInFocus(infocata, forma)
                .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                .onFocusChanged { infocata = it.isFocused }
                .pressabile(onClick = onClick, onLongClick = onLongClick)
                .clip(forma)
                .background(Color(0xFF262B33))
                .border(
                    2.dp,
                    if (infocata) LocalAccento.current else Color.Transparent,
                    forma
                )
        ) {
            val imageUrl = card.imageUrl
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                PlaceholderLocandina(card, modifier = Modifier.fillMaxSize())
            }
            if (preferito) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0x99000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Preferito",
                        tint = LocalAccento.current,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            if (percentuale > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(8.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3A3F48))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentuale / 100f)
                            .fillMaxHeight()
                            .background(LocalAccento.current)
                    )
                }
            }
        }
        Text(
            text = card.title,
            color = if (infocata) LocalAccento.current else Color(0xFFF2F2F0),
            fontSize = 14.sp,
            maxLines = if (card.locandinaVerticale) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Riempie il posto della locandina quando un contenuto non ha (ancora) un'immagine: un'icona
 * neutra sul fondo della card, cosi' la card non resta un rettangolo vuoto. */
@Composable
fun PlaceholderLocandina(card: ContentCard, modifier: Modifier = Modifier) {
    val icona = when (card) {
        is ContentCard.Canale -> Icons.Filled.Tv
        is ContentCard.Film -> Icons.Filled.Movie
        is ContentCard.SerieCard -> Icons.Filled.LiveTv
    }
    Box(modifier = modifier.background(Color(0xFF262B33)), contentAlignment = Alignment.Center) {
        Icon(
            imageVector = icona,
            contentDescription = null,
            tint = Color(0xFF4A505C),
            modifier = Modifier.size(44.dp)
        )
    }
}
