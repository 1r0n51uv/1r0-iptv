package com.ir0.iptv.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.theme.Palette
import coil.request.ImageRequest
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
    /** Bersaglio esplicito di un altro elemento (es. il pulsante Riprendi dell'hero, per GIU'):
     * un secondo `FocusRequester` sullo stesso nodo, indipendente da [focusRequester] che invece
     * serve al ripristino del focus sulla card aperta l'ultima volta. */
    focusRequesterAggiuntivo: FocusRequester? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    /** Chiamato quando la card prende il focus: la Home lo usa per cambiare lo sfondo. */
    onFocalizzata: () -> Unit = {}
) {
    var infocata by remember { mutableStateOf(false) }
    val larghezza = if (card.locandinaVerticale) LARGHEZZA_CARD_VERTICALE else LARGHEZZA_CARD_ORIZZONTALE
    val altezza = if (card.locandinaVerticale) ALTEZZA_CARD_VERTICALE else ALTEZZA_CARD_ORIZZONTALE
    // La card in focus e' ingrandita da `zoomInFocus` e sconfina sulle vicine: senza alzarne
    // l'ordine di disegno la sua meta' verso la card successiva (bordo e ombra inclusi) finisce
    // coperta dalla vicina, e nel passaggio orizzontale da una card all'altra si vede uno sfarfallio.
    // L'elevazione resta > 0 finche' l'animazione di rientro non e' finita, cosi' anche la card
    // che perde il focus rimpicciolisce sopra le vicine a riposo, non sotto.
    val elevazione by animateFloatAsState(
        targetValue = if (infocata) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "cardElevazione"
    )
    Column(
        modifier = Modifier
            .width(larghezza)
            // Card in focus sempre in cima; quella che sta rientrando resta sopra le vicine a
            // riposo finche' l'animazione non finisce.
            .zIndex(if (infocata) 2f else elevazione),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val forma = RoundedCornerShape(8.dp)
        Box(
            // Contenitore a dimensione fissa: e' questo il bersaglio del focus e del
            // "bring into view" del contenitore a scorrimento. Lo zoom NON va messo qui: la
            // scala di `graphicsLayer` verrebbe letta dal contenitore verticale, che ad ogni
            // cambio di focus tra le card scrollerebbe di qualche px per "rimettere in vista"
            // la card ingrandita — ed e' proprio l'ondeggiamento su e giu' della riga.
            modifier = Modifier
                .width(larghezza)
                .height(altezza)
                .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                .let { if (focusRequesterAggiuntivo != null) it.focusRequester(focusRequesterAggiuntivo) else it }
                .onFocusChanged {
                    infocata = it.isFocused
                    if (it.isFocused) onFocalizzata()
                }
                .pressabile(onClick = onClick, onLongClick = onLongClick)
        ) {
          Box(
            modifier = Modifier
                .fillMaxSize()
                // Ancorata in basso: la card in focus cresce solo verso l'alto, la linea di base
                // della riga (bordo inferiore + titolo sotto) non si sposta scorrendo tra le card.
                // 1.08 e non di piu': oltre, la crescita supera i 20dp di contentPadding verticale
                // delle righe e la card si taglia in alto.
                .zoomInFocus(infocata, forma, scalaMax = 1.08f, ombraMax = 28.dp, origine = TransformOrigin(0.5f, 1f))
                .clip(forma)
                .background(Palette.superficieAlta)
        ) {
            val imageUrl = card.imageUrl
            if (imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(320)
                        .build(),
                    contentDescription = card.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Velo in basso: stacca la barra di avanzamento e il cuore dalla locandina, e a
                // fuoco si schiarisce per far "accendere" la card.
                val velo by animateFloatAsState(
                    targetValue = if (infocata) 0f else 0.28f,
                    animationSpec = tween(durationMillis = 220),
                    label = "veloCard"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.55f to Color.Transparent,
                                1f to Palette.inchiostro.copy(alpha = 0.75f)
                            )
                        )
                        .background(Palette.inchiostro.copy(alpha = velo))
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
                        .background(Palette.inchiostro.copy(alpha = 0.6f)),
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
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Palette.testo.copy(alpha = 0.25f))
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
        }
        Text(
            text = card.title,
            color = if (infocata) Palette.testo else Palette.testoSecondario,
            fontSize = 14.sp,
            fontWeight = if (infocata) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = if (card.locandinaVerticale) 2 else 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Riempie il posto della locandina quando un contenuto non ha (ancora) un'immagine: un fondo
 * sfumato con il titolo in grande (come le tessere "senza artwork" delle app di streaming), cosi'
 * un Canale senza logo si riconosce comunque a colpo d'occhio. */
@Composable
fun PlaceholderLocandina(card: ContentCard, modifier: Modifier = Modifier) {
    val icona = when (card) {
        is ContentCard.Canale -> Icons.Filled.Tv
        is ContentCard.Film -> Icons.Filled.Movie
        is ContentCard.SerieCard -> Icons.Filled.LiveTv
    }
    Box(
        modifier = modifier.background(
            Brush.linearGradient(listOf(Palette.superficieAlta, Palette.superficie, Palette.inchiostro))
        )
    ) {
        Icon(
            imageVector = icona,
            contentDescription = null,
            tint = Palette.testoTerziario,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp)
                .size(18.dp)
        )
        Text(
            text = card.title,
            color = Palette.testo.copy(alpha = 0.85f),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 19.sp,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}
