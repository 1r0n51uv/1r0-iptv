package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ir0.iptv.app.sport.PartitaConCanale
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.util.DownsampleBlurTransformation
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.TipoRiga
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.Visto

private val registroVisti = RegistroVisti()
private val elencoPreferiti = ElencoPreferiti()

/** Le righe curate compaiono sempre, anche vuote: la Dashboard deve far capire cosa puo'
 * mostrare, non solo cosa mostra in questo momento. */
@Composable
fun DashboardScreen(
    righe: List<RigaDashboard>,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    catalogoVuoto: Boolean,
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    contenutoDiDefault: String? = null,
    ordine: List<SezioneHome> = SezioneHome.ordinePredefinito,
    sport: List<PartitaConCanale> = emptyList(),
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {}
) {
    if (catalogoVuoto) {
        SchermataVuota(
            "Nessun contenuto trovato nelle Sorgenti configurate. Se un abbonamento è scaduto, " +
                "apri Connessione nella barra laterale e inquadra il QR per aggiornare le credenziali " +
                "dal Pannello Web."
        )
        return
    }

    val righePerTipo = remember(righe) { righe.associateBy { it.tipo } }
    val rigaContinuaOriginale = righePerTipo[TipoRiga.CONTINUA]?.contenuti ?: emptyList()
    // Il primo contenuto della Dashboard e' quello da riprendere (o il Contenuto di default se
    // non si e' ancora guardato nulla): mostrato come banda in evidenza, non piu' dentro la riga
    // (qualunque essa sia: col ripiego l'hero puo' venire da Preferiti o Nuovi episodi).
    val hero = remember(righe, contenutoDiDefault) {
        rigaContinuaOriginale.firstOrNull()
            ?: righe.flatMap { it.contenuti }.firstOrNull { it.chiaveIdentita == contenutoDiDefault }
    }
    val tipoRigaHero = remember(righe, hero) {
        hero?.let { h -> righe.firstOrNull { riga -> riga.contenuti.any { it.chiaveIdentita == h.chiaveIdentita } }?.tipo }
    }
    val heroDaRiprendere = tipoRigaHero == TipoRiga.CONTINUA

    fun contenutiRigaVisibili(tipo: TipoRiga): List<ContentCard> {
        val originali = righePerTipo[tipo]?.contenuti ?: emptyList()
        return if (hero != null && tipo == tipoRigaHero) {
            originali.filterNot { it.chiaveIdentita == hero.chiaveIdentita }
        } else {
            originali
        }
    }

    // Colonna a scorrimento "normale", non LazyColumn: le sezioni sono poche (max 5) e con la
    // LazyColumn, scendendo tra le righe, l'hero in cima veniva smontato — poi da riga 1 non si
    // riusciva piu' a risalire su di lui col D-pad (niente bersaglio di focus sopra), e la banda
    // "Continua a guardare" tornava visibile solo riaprendo la Home. Qui resta tutto montato.
    val statoColonna = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(chiaveDaFocalizzare, righe, ordine, hero) {
        when {
            hero != null && hero.chiaveIdentita == chiaveDaFocalizzare -> {
                statoColonna.scrollTo(0)
                runCatching { focusRequester.requestFocus() }
                // Dare il focus al pulsante Riprendi innesca un bring-into-view che puo'
                // spingere fuori dallo schermo il bordo alto dell'hero, che invece ci sta
                // tutto: dopo un frame si rimette la colonna in cima.
                withFrameNanos { }
                statoColonna.scrollTo(0)
            }
            else -> {
                // Le righe sono tutte montate: dare il focus alla card giusta basta, il
                // bring-into-view della colonna la porta in vista da solo.
                runCatching { focusRequester.requestFocus() }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .verticalScroll(statoColonna)
            .padding(vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        if (hero != null) {
            HeroContinua(
                card = hero,
                percentuale = registroVisti.percentuale(visti, hero.chiaveIdentita),
                preferito = elencoPreferiti.preferito(personalizzazioni, hero),
                daRiprendere = heroDaRiprendere,
                focusRequester = focusRequester.takeIf { hero.chiaveIdentita == chiaveDaFocalizzare },
                // Risalendo col D-pad, quando il pulsante prende il focus si riporta la colonna
                // in cima cosi' la banda "Continua a guardare" si vede tutta. Va rifatto per
                // qualche frame: il bring-into-view del focus vorrebbe mostrare solo il pulsante
                // (in basso nell'hero) e altrimenti avrebbe l'ultima parola.
                onFocalizzato = {
                    scope.launch { repeat(4) { withFrameNanos {}; statoColonna.scrollTo(0) } }
                },
                onClick = { onContenutoClick(hero) }
            )
        }
        ordine.forEach { sezione ->
            if (sezione == SezioneHome.SPORT) {
                FasciaSport(partite = sport, onCanaleClick = onContenutoClick)
            } else {
                val tipo = tipoRigaDi(sezione)!!
                RigaContenuti(
                    titolo = sezione.etichetta,
                    contenuti = contenutiRigaVisibili(tipo),
                    visti = visti,
                    personalizzazioni = personalizzazioni,
                    chiaveDaFocalizzare = chiaveDaFocalizzare,
                    focusRequester = focusRequester,
                    onClick = onContenutoClick,
                    onLongClick = onContenutoLongClick,
                    // Quando l'hero e' il contenuto da riprendere copre gia' il messaggio di
                    // ripiego di Continua; col ripiego al Contenuto di default resta utile.
                    messaggioVuoto = if (tipo == TipoRiga.CONTINUA && heroDaRiprendere) null else messaggioVuotoDi(tipo)
                )
            }
        }
    }
}

private fun tipoRigaDi(sezione: SezioneHome): TipoRiga? = when (sezione) {
    SezioneHome.SPORT -> null
    SezioneHome.CONTINUA -> TipoRiga.CONTINUA
    SezioneHome.NUOVI_EPISODI -> TipoRiga.NUOVI_EPISODI
    SezioneHome.SUGGERITI -> TipoRiga.SUGGERITI
    SezioneHome.PREFERITI -> TipoRiga.PREFERITI
}

private val ALTEZZA_HERO = 340.dp

/** La banda in evidenza con il contenuto da riprendere: sfondo della locandina sfocato, titolo e
 * pulsante Riprendi in basso a sinistra, come nella Home precedente al refactor con Sidebar.
 * Il focus (e quindi il D-pad all'avvio) va sul pulsante, non sull'intera banda: cosi' premere
 * OK riproduce subito, senza dover indovinare dove sia l'area cliccabile. */
@Composable
private fun HeroContinua(
    card: ContentCard,
    percentuale: Int,
    preferito: Boolean,
    daRiprendere: Boolean,
    focusRequester: FocusRequester?,
    onFocalizzato: () -> Unit = {},
    onClick: () -> Unit
) {
    var pulsanteInfocato by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val accento = LocalAccento.current
    val etichetta = when {
        daRiprendere -> "CONTINUA A GUARDARE"
        card is ContentCard.SerieCard -> "SERIE"
        card is ContentCard.Canale -> "CANALE"
        else -> "FILM"
    }
    val etichettaPulsante = when {
        daRiprendere -> "Riprendi"
        card is ContentCard.SerieCard -> "Vai alla Serie"
        else -> "Riproduci"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTEZZA_HERO)
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF262B33))
            .border(
                2.dp,
                if (pulsanteInfocato) accento else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
    ) {
        val imageUrl = card.imageUrl
        if (imageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .transformations(DownsampleBlurTransformation())
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            PlaceholderLocandina(card, modifier = Modifier.fillMaxSize())
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x9914161A), Color(0xE614161A)),
                        startY = 0f
                    )
                )
        )
        if (preferito) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0x99000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Preferito",
                    tint = accento,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = etichetta,
                color = accento,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = card.title,
                color = Color(0xFFF2F2F0),
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (percentuale > 0) {
                Box(
                    modifier = Modifier
                        .width(240.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(Color(0x333A3F48))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentuale / 100f)
                            .fillMaxHeight()
                            .background(accento)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                    .onFocusChanged {
                        pulsanteInfocato = it.isFocused
                        if (it.isFocused) onFocalizzato()
                    }
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onClick)
                    .background(accento)
                    .border(
                        2.dp,
                        if (pulsanteInfocato) Color(0xFFF2F2F0) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF14161A),
                    modifier = Modifier.size(18.dp)
                )
                Text(text = etichettaPulsante, color = Color(0xFF14161A), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RigaContenuti(
    titolo: String,
    contenuti: List<ContentCard>,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    focusRequester: FocusRequester?,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit = {},
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    /** Quando non null, la riga resta visibile (con questo messaggio) anche a contenuti vuoti;
     * quando null, una riga vuota semplicemente non compare (comportamento delle righe di
     * catalogo su Sfoglia/Cerca/Sport, dove una sezione vuota non aggiunge nulla da leggere). */
    messaggioVuoto: String? = null
) {
    if (contenuti.isEmpty()) {
        if (messaggioVuoto == null) return
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = titolo,
                color = Color(0xFFF2F2F0),
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Text(
                text = messaggioVuoto,
                color = Color(0xFF6D7380),
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        return
    }

    val statoRiga = rememberLazyListState()
    val indiceDaFocalizzare = contenuti.indexOfFirst { it.chiaveIdentita == chiaveDaFocalizzare }

    LaunchedEffect(chiaveDaFocalizzare, contenuti) {
        if (indiceDaFocalizzare > 0) statoRiga.scrollToItem(indiceDaFocalizzare)
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = titolo,
            color = Color(0xFFF2F2F0),
            fontSize = 19.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        LazyRow(
            state = statoRiga,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            // Il padding verticale lascia respirare la card in focus, ingrandita, senza tagliarla:
            // con lo zoom ancorato in basso la crescita e' tutta verso l'alto.
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 20.dp)
        ) {
            items(contenuti) { card ->
                CardContenuto(
                    card = card,
                    percentuale = registroVisti.percentuale(visti, card.chiaveIdentita),
                    preferito = elencoPreferiti.preferito(personalizzazioni, card),
                    focusRequester = focusRequester.takeIf { card.chiaveIdentita == chiaveDaFocalizzare },
                    onClick = { onClick(card) },
                    onLongClick = { onLongClick(card) }
                )
            }
        }
    }
}

@Composable
fun SchermataVuota(messaggio: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF14161A))
            .padding(32.dp)
    ) {
        Text(text = messaggio, color = Color(0xFF9AA0AA), fontSize = 16.sp)
    }
}

private fun messaggioVuotoDi(tipo: TipoRiga): String = when (tipo) {
    TipoRiga.CONTINUA -> "Quello che guardi appare qui, per riprendere da dove avevi lasciato."
    TipoRiga.NUOVI_EPISODI ->
        "Segui una Serie (guardala o aggiungila ai Preferiti) per vedere qui i nuovi episodi."
    TipoRiga.SUGGERITI -> "Aggiungi la chiave API di Claude dalle Impostazioni per ricevere suggerimenti."
    TipoRiga.PREFERITI -> "Nessun Preferito ancora: aggiungine uno dalla sua pagina di Dettaglio."
}
