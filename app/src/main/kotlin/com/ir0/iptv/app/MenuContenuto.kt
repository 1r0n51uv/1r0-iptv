package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.util.DownsampleBlurTransformation
import com.ir0.iptv.domain.catalog.ContentCard

/** Le azioni rapide su un contenuto, aperte tenendo premuto OK su una card. Cosa compare dipende
 * dal tipo: un Canale non ha un Dettaglio ne' una posizione da riprendere, una Serie si gioca
 * dalla sua pagina e non dalla card. Vive in un Dialog cosi' il D-pad resta dentro il menu e
 * BACK lo chiude. */
@Composable
fun MenuContenuto(
    card: ContentCard,
    preferito: Boolean,
    haRipresa: Boolean,
    onRiproduci: () -> Unit,
    onRiproduciDallInizio: () -> Unit,
    onRiproduciCon: () -> Unit,
    onApriDettaglio: () -> Unit,
    onCambiaPreferito: () -> Unit,
    onChiudi: () -> Unit
) {
    val riproducibile = card is ContentCard.Canale || card is ContentCard.Film
    val haDettaglio = card is ContentCard.Film || card is ContentCard.SerieCard
    val primoFocus = remember { FocusRequester() }

    Dialog(
        onDismissRequest = onChiudi,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        LaunchedEffect(card) { runCatching { primoFocus.requestFocus() } }

        val context = LocalContext.current
        val forma = RoundedCornerShape(14.dp)
        Box(
            modifier = Modifier
                .width(380.dp)
                .clip(forma)
                .background(Color(0xFF1F232A))
                .border(1.dp, Color(0xFF2E343E), forma)
        ) {
            // La cover del contenuto, sfocata, fa da sfondo al menu.
            val cover = card.imageUrl
            if (cover != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(cover)
                        .transformations(DownsampleBlurTransformation(targetWidth = 300, radius = 5, passes = 2))
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
            // Velo scuro: piu' leggero in alto (si vede la cover), piu' fitto in basso (righe leggibili).
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xA61A1D23), Color(0xF21A1D23))
                        )
                    )
            )
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
            Text(
                text = card.title,
                color = Color(0xFFF2F2F0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            var primoAssegnato = false
            fun focusDiTesta(): FocusRequester? =
                if (!primoAssegnato) primoFocus.also { primoAssegnato = true } else null

            if (riproducibile) {
                VoceMenu(
                    testo = if (haRipresa) "Riprendi" else "Riproduci",
                    icona = Icons.Filled.PlayArrow,
                    focusRequester = focusDiTesta(),
                    onClick = onRiproduci
                )
            }
            if (card is ContentCard.Film && haRipresa) {
                VoceMenu(
                    testo = "Riproduci dall'inizio",
                    icona = Icons.Filled.Replay,
                    focusRequester = focusDiTesta(),
                    onClick = onRiproduciDallInizio
                )
            }
            if (riproducibile) {
                VoceMenu(
                    testo = "Riproduci con…",
                    icona = Icons.AutoMirrored.Filled.Launch,
                    focusRequester = focusDiTesta(),
                    onClick = onRiproduciCon
                )
            }
            if (haDettaglio) {
                VoceMenu(
                    testo = "Apri dettaglio",
                    icona = Icons.Filled.Info,
                    focusRequester = focusDiTesta(),
                    onClick = onApriDettaglio
                )
            }
            VoceMenu(
                testo = if (preferito) "Rimuovi dai Preferiti" else "Aggiungi ai Preferiti",
                icona = if (preferito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                coloreIcona = if (preferito) LocalAccento.current else null,
                focusRequester = focusDiTesta(),
                onClick = onCambiaPreferito
            )
            }
        }
    }
}

@Composable
private fun VoceMenu(
    testo: String,
    icona: ImageVector,
    focusRequester: FocusRequester?,
    coloreIcona: Color? = null,
    onClick: () -> Unit
) {
    var infocata by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { infocata = it.isFocused }
            .clickable(onClick = onClick)
            .background(if (infocata) Color(0xFF2E343E) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icona,
            contentDescription = null,
            tint = coloreIcona ?: Color(0xFFC7CAD0),
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = testo,
            color = Color(0xFFF2F2F0),
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
