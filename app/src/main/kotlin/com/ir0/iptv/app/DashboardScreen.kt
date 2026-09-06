package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.app.sport.PartitaConCanale
import com.ir0.iptv.app.theme.Colori
import com.ir0.iptv.app.theme.Spazi
import com.ir0.iptv.app.theme.Tipo
import com.ir0.iptv.app.ui.BarraAvanzamento
import com.ir0.iptv.app.ui.sfondoBlocco
import com.ir0.iptv.app.ui.testoSecondarioSuBlocco
import com.ir0.iptv.app.ui.testoSuBlocco
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.customization.ContentCustomization
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.TipoRiga
import com.ir0.iptv.domain.epg.GuidaTv
import com.ir0.iptv.domain.epg.Programma
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.Visto
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val registroVisti = RegistroVisti()
private val guidaTv = GuidaTv()
private val fmtGiorno = SimpleDateFormat("EEEE d MMMM", Locale.ITALY)
private val fmtOra = SimpleDateFormat("HH:mm", Locale.ITALY)

private val FORMA_VOCE = RoundedCornerShape(6.dp)

/**
 * La Home come palinsesto: che ora è, cosa riprendere, cosa è in onda ora sui canali che segui,
 * e gli elenchi curati (nuovi episodi, suggeriti, preferiti). Niente vetrina di locandine.
 */
@Composable
fun DashboardScreen(
    righe: List<RigaDashboard>,
    visti: List<Visto>,
    chiaveDaFocalizzare: String?,
    catalogoVuoto: Boolean,
    canaliInEvidenza: List<ContentCard.Canale> = emptyList(),
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    contenutoDiDefault: String? = null,
    ordine: List<SezioneHome> = SezioneHome.ordinePredefinito,
    sport: List<PartitaConCanale> = emptyList(),
    onContenutoClick: (ContentCard) -> Unit,
    onContenutoLongClick: (ContentCard) -> Unit = {},
    onApriConnessione: () -> Unit = {}
) {
    if (catalogoVuoto) {
        CatalogoVuoto(onApriConnessione)
        return
    }

    val righePerTipo = remember(righe) { righe.associateBy { it.tipo } }
    val continua = righePerTipo[TipoRiga.CONTINUA]?.contenuti ?: emptyList()
    val hero = remember(righe, contenutoDiDefault) {
        continua.firstOrNull()
            ?: righe.flatMap { it.contenuti }.firstOrNull { it.chiaveIdentita == contenutoDiDefault }
    }
    val heroDaRiprendere = hero != null && continua.any { it.chiaveIdentita == hero.chiaveIdentita }

    fun vistoDi(card: ContentCard): Visto? = when (card) {
        is ContentCard.SerieCard -> visti.filter { it.serie == card.title }.maxByOrNull { it.aggiornatoIl }
        else -> visti.firstOrNull { it.chiaveIdentita == card.chiaveIdentita }
    }

    fun contenutiSezione(tipo: TipoRiga): List<ContentCard> {
        val originali = righePerTipo[tipo]?.contenuti ?: emptyList()
        return if (hero != null && tipo == TipoRiga.CONTINUA) {
            originali.filterNot { it.chiaveIdentita == hero.chiaveIdentita }
        } else {
            originali
        }
    }

    // EPG "in onda ora" per i primi canali in evidenza: chiamate di rete al volo, best-effort.
    var inOnda by remember(canaliInEvidenza) { mutableStateOf<Map<String, Programma?>>(emptyMap()) }
    LaunchedEffect(canaliInEvidenza) {
        val fetcher = ContentFetcher()
        val ora = System.currentTimeMillis()
        canaliInEvidenza.take(6).forEach { canale ->
            val prog = canale.xtream?.let {
                runCatching { guidaTv.inOnda(fetcher.palinsesto(it), ora) }.getOrNull()
            }
            inOnda = inOnda + (canale.chiaveIdentita to prog)
        }
    }

    val statoColonna = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(chiaveDaFocalizzare, righe) {
        runCatching { focusRequester.requestFocus() }
        statoColonna.scrollToItem(0)
    }

    LazyColumn(
        state = statoColonna,
        modifier = Modifier.fillMaxSize().background(Colori.inchiostro),
        contentPadding = PaddingValues(
            start = Spazi.l, end = Spazi.bordoSchermo,
            top = Spazi.l, bottom = Spazi.xl
        ),
        verticalArrangement = Arrangement.spacedBy(Spazi.l)
    ) {
        item { IntestazioneGiorno() }

        if (hero != null) {
            item {
                BloccoRiprendi(
                    card = hero,
                    visto = vistoDi(hero),
                    daRiprendere = heroDaRiprendere,
                    focusRequester = focusRequester.takeIf { hero.chiaveIdentita == chiaveDaFocalizzare }
                        ?: focusRequester,
                    onClick = { onContenutoClick(hero) }
                )
            }
        }

        if (canaliInEvidenza.isNotEmpty()) {
            item {
                StripInOnda(
                    canali = canaliInEvidenza,
                    inOnda = inOnda,
                    onCanale = onContenutoClick
                )
            }
        }

        items(ordine) { sezione ->
            when (sezione) {
                SezioneHome.SPORT -> if (sport.isNotEmpty()) {
                    FasciaSport(partite = sport, onCanaleClick = onContenutoClick)
                }
                else -> {
                    val tipo = tipoRigaDi(sezione) ?: return@items
                    Listino(
                        titolo = etichettaSezione(sezione, heroDaRiprendere),
                        sezione = coloreSezione(tipo),
                        voci = contenutiSezione(tipo),
                        vistoDi = ::vistoDi,
                        chiaveDaFocalizzare = chiaveDaFocalizzare,
                        focusRequester = focusRequester,
                        onClick = onContenutoClick,
                        onLongClick = onContenutoLongClick,
                        messaggioVuoto = if (tipo == TipoRiga.CONTINUA && heroDaRiprendere) null
                        else messaggioVuotoDi(tipo)
                    )
                }
            }
        }
    }
}

/* ---------- Intestazione ---------- */

@Composable
private fun IntestazioneGiorno() {
    var ora by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            ora = System.currentTimeMillis()
            kotlinx.coroutines.delay(30_000)
        }
    }
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spazi.m)) {
        Text(
            text = fmtGiorno.format(Date(ora)).replaceFirstChar { it.uppercase() },
            style = Tipo.corpo,
            color = Colori.testoFioco
        )
        Spacer(Modifier.weight(1f))
        Text(text = fmtOra.format(Date(ora)), style = Tipo.oraGrande, color = Colori.testo)
    }
}

/* ---------- Riprendi ---------- */

@Composable
private fun BloccoRiprendi(
    card: ContentCard,
    visto: Visto?,
    daRiprendere: Boolean,
    focusRequester: FocusRequester,
    onClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    val infocato by src.collectIsFocusedAsState()
    val sezione = Colori.live

    val minutiRimasti = visto?.takeIf { it.durataMs > 0 }?.let {
        ((it.durataMs - it.posizioneMs).coerceAtLeast(0L) / 60_000L).toInt()
    }
    val perc = visto?.takeIf { it.durataMs > 0 }?.let {
        (it.posizioneMs * 100 / it.durataMs).toInt().coerceIn(0, 100)
    } ?: 0
    val sottotitolo = when {
        visto?.serie != null -> visto.titolo
        card is ContentCard.SerieCard -> card.categoria ?: "Serie"
        card is ContentCard.Film -> card.categoria ?: "Film"
        else -> "Canale"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sfondoBlocco(infocato, sezione, RoundedCornerShape(10.dp))
            .focusRequester(focusRequester)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Spazi.m, vertical = Spazi.m),
        verticalArrangement = Arrangement.spacedBy(Spazi.s)
    ) {
        Text(
            text = if (daRiprendere) "Riprendi" else "Inizia",
            style = Tipo.etichetta,
            color = if (infocato) sezione else sezione
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = card.title,
                style = Tipo.titolo.copy(fontSize = 34.sp, lineHeight = 38.sp),
                color = testoSuBlocco(infocato),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "▸",
                fontSize = 28.sp,
                color = if (infocato) Colori.inchiostro else sezione
            )
        }
        Text(
            text = sottotitolo,
            style = Tipo.corpo,
            color = testoSecondarioSuBlocco(infocato),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (perc > 0) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spazi.m)) {
                BarraAvanzamento(
                    percentuale = perc,
                    colore = if (infocato) Colori.inchiostro else sezione,
                    fondo = if (infocato) Color(0x33000000) else Colori.linea,
                    modifier = Modifier.width(280.dp).height(3.dp)
                )
                if (minutiRimasti != null) {
                    Text(
                        text = "$minutiRimasti min rimasti",
                        style = Tipo.ora,
                        color = testoSecondarioSuBlocco(infocato)
                    )
                }
            }
        }
    }
}

/* ---------- In onda ora ---------- */

@Composable
private fun StripInOnda(
    canali: List<ContentCard.Canale>,
    inOnda: Map<String, Programma?>,
    onCanale: (ContentCard.Canale) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spazi.s)) {
        EtichettaSezione("In onda ora", Colori.live, "su ${canali.size} canali che segui")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spazi.s), contentPadding = PaddingValues(vertical = 2.dp)) {
            items(canali) { canale ->
                TesseraCanale(
                    canale = canale,
                    programma = inOnda[canale.chiaveIdentita],
                    onClick = { onCanale(canale) }
                )
            }
        }
    }
}

@Composable
private fun TesseraCanale(
    canale: ContentCard.Canale,
    programma: Programma?,
    onClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    val infocato by src.collectIsFocusedAsState()
    val ora = System.currentTimeMillis()
    Column(
        modifier = Modifier
            .width(184.dp)
            .height(96.dp)
            .sfondoBlocco(infocato, Colori.live, FORMA_VOCE)
            .then(if (!infocato) Modifier.background(Colori.superficie, FORMA_VOCE) else Modifier)
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Spazi.s, vertical = Spazi.s),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(if (infocato) Colori.inchiostro else Colori.inOnda))
            Text(
                text = canale.title,
                style = Tipo.corpoForte,
                color = testoSuBlocco(infocato),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = programma?.titolo ?: "diretta",
            style = Tipo.corpo.copy(fontSize = 13.sp, lineHeight = 16.sp),
            color = testoSecondarioSuBlocco(infocato),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.weight(1f))
        if (programma != null) {
            Text(
                text = "fino alle ${fmtOra.format(Date(programma.fineMs))}",
                style = Tipo.siglaEpisodio,
                color = testoSecondarioSuBlocco(infocato)
            )
            BarraAvanzamento(
                percentuale = guidaTv.percentuale(programma, ora),
                colore = if (infocato) Colori.inchiostro else Colori.inOnda,
                fondo = if (infocato) Color(0x33000000) else Colori.linea,
                modifier = Modifier.fillMaxWidth().height(2.dp)
            )
        }
    }
}

/* ---------- Elenchi curati ---------- */

@Composable
private fun Listino(
    titolo: String,
    sezione: Color,
    voci: List<ContentCard>,
    vistoDi: (ContentCard) -> Visto?,
    chiaveDaFocalizzare: String?,
    focusRequester: FocusRequester,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit,
    messaggioVuoto: String?
) {
    if (voci.isEmpty()) {
        if (messaggioVuoto == null) return
        Column(verticalArrangement = Arrangement.spacedBy(Spazi.s)) {
            EtichettaSezione(titolo, sezione)
            Text(messaggioVuoto, style = Tipo.corpo, color = Colori.testoDebole)
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        EtichettaSezione(titolo, sezione)
        Spacer(Modifier.height(Spazi.xs))
        voci.take(6).forEach { card ->
            RigaListino(
                card = card,
                visto = vistoDi(card),
                sezione = sezione,
                focusRequester = focusRequester.takeIf { card.chiaveIdentita == chiaveDaFocalizzare },
                onClick = { onClick(card) },
                onLongClick = { onLongClick(card) }
            )
        }
    }
}

@Composable
private fun RigaListino(
    card: ContentCard,
    visto: Visto?,
    sezione: Color,
    focusRequester: FocusRequester?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val src = remember { MutableInteractionSource() }
    val infocato by src.collectIsFocusedAsState()
    val tipo = when (card) {
        is ContentCard.Canale -> "canale"
        is ContentCard.Film -> "film"
        is ContentCard.SerieCard -> "serie"
    }
    val perc = visto?.takeIf { it.durataMs > 0 }?.let {
        (it.posizioneMs * 100 / it.durataMs).toInt().coerceIn(0, 100)
    } ?: 0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sfondoBlocco(infocato, sezione, FORMA_VOCE)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .clickable(interactionSource = src, indication = null, onClick = onClick)
            .padding(horizontal = Spazi.s, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spazi.m)
    ) {
        Text(
            text = tipo,
            style = Tipo.siglaEpisodio,
            color = if (infocato) Color(0xFF4A4C50) else sezione,
            modifier = Modifier.width(52.dp)
        )
        Text(
            text = card.title,
            style = Tipo.corpoForte,
            color = testoSuBlocco(infocato),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (perc in 1..99) {
            BarraAvanzamento(
                percentuale = perc,
                colore = if (infocato) Colori.inchiostro else sezione,
                fondo = if (infocato) Color(0x33000000) else Colori.linea,
                modifier = Modifier.width(96.dp).height(2.dp)
            )
        }
    }
}

/* ---------- Pezzi comuni ---------- */

@Composable
private fun EtichettaSezione(testo: String, sezione: Color, coda: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spazi.s)) {
        Box(Modifier.width(18.dp).height(3.dp).background(sezione))
        Text(testo, style = Tipo.sezione, color = Colori.testo)
        if (coda != null) {
            Text(coda, style = Tipo.corpo, color = Colori.testoDebole)
        }
    }
}

@Composable
private fun CatalogoVuoto(onApriConnessione: () -> Unit) {
    val src = remember { MutableInteractionSource() }
    val infocato by src.collectIsFocusedAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colori.inchiostro)
            .padding(Spazi.bordoSchermo),
        verticalArrangement = Arrangement.spacedBy(Spazi.m)
    ) {
        Text("Nessun contenuto dalle Sorgenti", style = Tipo.titolo, color = Colori.testo)
        Text(
            "Una Sorgente non risponde. Se un abbonamento Xtream è scaduto, il catalogo resta vuoto: " +
                "apri Connessione e inquadra il QR per aggiornare le credenziali dal Pannello Web.",
            style = Tipo.corpo,
            color = Colori.testoFioco,
            modifier = Modifier.width(560.dp)
        )
        Row(
            modifier = Modifier
                .sfondoBlocco(infocato, Colori.sistema, RoundedCornerShape(8.dp))
                .then(if (!infocato) Modifier.border(1.dp, Colori.linea, RoundedCornerShape(8.dp)) else Modifier)
                .clickable(interactionSource = src, indication = null, onClick = onApriConnessione)
                .padding(horizontal = Spazi.m, vertical = 10.dp)
        ) {
            Text("Apri Connessione", style = Tipo.corpoForte, color = testoSuBlocco(infocato))
        }
    }
}

/* ---------- Mappe sezione ---------- */

private fun tipoRigaDi(sezione: SezioneHome): TipoRiga? = when (sezione) {
    SezioneHome.SPORT -> null
    SezioneHome.CONTINUA -> TipoRiga.CONTINUA
    SezioneHome.NUOVI_EPISODI -> TipoRiga.NUOVI_EPISODI
    SezioneHome.SUGGERITI -> TipoRiga.SUGGERITI
    SezioneHome.PREFERITI -> TipoRiga.PREFERITI
}

private fun coloreSezione(tipo: TipoRiga): Color = when (tipo) {
    TipoRiga.CONTINUA -> Colori.live
    TipoRiga.NUOVI_EPISODI -> Colori.serie
    TipoRiga.SUGGERITI -> Colori.film
    TipoRiga.PREFERITI -> Colori.preferiti
}

private fun etichettaSezione(sezione: SezioneHome, heroDaRiprendere: Boolean): String = when (sezione) {
    SezioneHome.CONTINUA -> if (heroDaRiprendere) "Continua a guardare" else "Da riprendere"
    else -> sezione.etichetta
}

private fun messaggioVuotoDi(tipo: TipoRiga): String = when (tipo) {
    TipoRiga.CONTINUA -> "Quello che guardi appare qui, per riprendere da dove avevi lasciato."
    TipoRiga.NUOVI_EPISODI ->
        "Segui una Serie (guardala o aggiungila ai Preferiti) per vedere qui i nuovi episodi."
    TipoRiga.SUGGERITI -> "Aggiungi la chiave API di Claude dal Pannello Web per ricevere suggerimenti."
    TipoRiga.PREFERITI -> "Nessun Preferito ancora: aggiungine uno dalla sua pagina di Dettaglio."
}

/** Ancora usata da MainActivity per stati vuoti di altre schermate. */
@Composable
fun SchermataVuota(messaggio: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Colori.inchiostro)
            .padding(Spazi.bordoSchermo)
    ) {
        Text(text = messaggio, style = Tipo.corpo, color = Colori.testoFioco, modifier = Modifier.width(560.dp))
    }
}
