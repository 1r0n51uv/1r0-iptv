package com.ir0.iptv.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
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
import com.ir0.iptv.app.theme.LocalAccento
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.app.util.DownsampleBlurTransformation
import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.DettaglioEsteso
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import com.ir0.iptv.domain.playback.NavigazioneSerie
import com.ir0.iptv.domain.playback.ProssimaVisione
import com.ir0.iptv.domain.playback.ProssimaVisioneResolver
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.playback.Visto

private val registro = RegistroVisti()
private val resolver = ProssimaVisioneResolver()
private val navigazione = NavigazioneSerie()

@Composable
fun DetailScreen(
    card: ContentCard,
    visti: List<Visto>,
    preferito: Boolean,
    onCambiaPreferito: () -> Unit,
    onRiproduci: (RichiestaRiproduzione, Long, List<RichiestaRiproduzione>) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit,
    /** Azzera i Visti la cui Chiave di Identita' e' tra quelle date: un singolo Episodio o
     * tutti gli Episodi di una Stagione (vedi VistoRepository.rimuoviVisti). */
    onResetVisti: (Set<String>) -> Unit = {}
) {
    when (card) {
        is ContentCard.Film -> DettaglioFilm(card, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon)

        is ContentCard.SerieCard.Pronta ->
            DettaglioSerie(card, card.serie, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon, onResetVisti)

        is ContentCard.SerieCard.DaCaricare -> {
            var serie by remember(card) { mutableStateOf<Serie?>(null) }
            var fallita by remember(card) { mutableStateOf(false) }
            LaunchedEffect(card) {
                val risultato = ContentFetcher().dettaglioSerie(card)
                serie = risultato
                fallita = risultato == null
            }
            val serieCorrente = serie
            when {
                serieCorrente != null -> DettaglioSerie(
                    card, serieCorrente, visti, preferito, onCambiaPreferito, onRiproduci, onRiproduciCon, onResetVisti
                )

                fallita -> DettaglioErrore(card.title)
                else -> LoadingScreen()
            }
        }

        is ContentCard.Canale -> DettaglioErrore(card.title)
    }
}

@Composable
private fun DettaglioFilm(
    card: ContentCard.Film,
    visti: List<Visto>,
    preferito: Boolean,
    onCambiaPreferito: () -> Unit,
    onRiproduci: (RichiestaRiproduzione, Long, List<RichiestaRiproduzione>) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit
) {
    val richiesta = RichiestaRiproduzione(
        titolo = card.title,
        streamUrl = card.streamUrl,
        tipo = TipoVisto.FILM,
        posterUrl = card.imageUrl
    )
    val posizione = registro.posizioneDiRipresa(visti, card.chiaveIdentita)
    val percentuale = registro.percentuale(visti, card.chiaveIdentita)
    val focusPrincipale = remember(card) { FocusRequester() }
    LaunchedEffect(card) { runCatching { focusPrincipale.requestFocus() } }

    var dettagli by remember(card) { mutableStateOf<DettaglioEsteso?>(null) }
    LaunchedEffect(card) {
        dettagli = card.xtream?.let { ContentFetcher().dettaglioFilm(it) }
    }

    Pagina(sfondo = card.imageUrl) {
        Testata(
            copertina = card.imageUrl,
            etichetta = "FILM",
            coloreEtichetta = Color(0xFF3B82F6),
            titolo = card.title,
            meta = card.categoria,
            plot = card.plot ?: dettagli?.trama,
            dettagli = dettagli
        ) {
            PulsanteAzione(
                // Un Film mai visto e' "Play"; ripreso mostra solo "Riprendi", senza il minutaggio.
                testo = if (posizione != null) "Riprendi" else "Play",
                principale = true,
                icona = Icons.Filled.PlayArrow,
                focusRequester = focusPrincipale,
                onClick = { onRiproduci(richiesta, posizione ?: 0L, emptyList()) }
            )
            PulsantePreferito(preferito, onCambiaPreferito)
            PulsanteAzione(testo = "Riproduci con…", onClick = { onRiproduciCon(richiesta) })
        }
        if (percentuale > 0) {
            BarraProgresso(percentuale, modifier = Modifier.width(340.dp))
        }
    }
}

@Composable
private fun DettaglioSerie(
    card: ContentCard.SerieCard,
    serie: Serie,
    visti: List<Visto>,
    preferito: Boolean,
    onCambiaPreferito: () -> Unit,
    onRiproduci: (RichiestaRiproduzione, Long, List<RichiestaRiproduzione>) -> Unit,
    onRiproduciCon: (RichiestaRiproduzione) -> Unit,
    onResetVisti: (Set<String>) -> Unit
) {
    val prossima = remember(serie, visti) { resolver.risolvi(serie, visti) }
    var stagioneSelezionata by remember(serie) {
        mutableStateOf(navigazione.stagioneIniziale(serie, visti))
    }
    val focusPrincipale = remember(serie) { FocusRequester() }
    // All'apertura del Dettaglio il D-pad parte sul pulsante Play/Riprendi.
    LaunchedEffect(prossima) { runCatching { focusPrincipale.requestFocus() } }

    val primoEpisodio = serie.seasons.flatMap { it.episodes }.firstOrNull()

    /** "S{stagione} E{episodio}" quando entrambi i numeri sono noti (le Sorgenti M3U senza
     * pattern riconoscibile non li hanno). */
    fun siglaDi(episodio: Episodio): String? {
        val stagione = serie.seasons.firstOrNull { st -> st.episodes.any { it.url == episodio.url } }?.number
        val numero = episodio.episodeNumber
        return if (stagione != null && numero != null) "S$stagione E$numero" else null
    }

    fun etichettaRiprendi(episodio: Episodio) = "Riprendi" + (siglaDi(episodio)?.let { " $it" } ?: "")

    /** Il primo Episodio in assoluto e' un "Play" secco (come un Film mai visto); dal secondo in
     * poi si aggiunge la sigla, cosi' si sa da dove riparte senza aprire la lista. */
    fun etichettaPlay(episodio: Episodio) =
        if (episodio.url == primoEpisodio?.url) "Play" else "Play" + (siglaDi(episodio)?.let { " $it" } ?: "")

    fun richiestaDi(episodio: Episodio) = RichiestaRiproduzione(
        titolo = episodio.title,
        streamUrl = episodio.url,
        tipo = TipoVisto.EPISODIO,
        serie = serie.name,
        // Immagine: prima l'Episodio, poi la copertina della Stagione, poi la locandina della Serie.
        posterUrl = episodio.immagine
            ?: navigazione.stagioneDi(serie, episodio)?.immagine
            ?: serie.poster
            ?: card.imageUrl
    )

    /** Gli Episodi dopo questo, gia' pronti da riprodurre: il player li fa partire da solo
     * quando la riproduzione arriva in fondo. */
    fun codaDopo(episodio: Episodio): List<RichiestaRiproduzione> =
        navigazione.episodiSuccessivi(serie, episodio.url).map(::richiestaDi)

    Pagina(sfondo = serie.poster ?: card.imageUrl) {
        Testata(
            copertina = serie.poster ?: card.imageUrl,
            etichetta = "SERIE",
            coloreEtichetta = Color(0xFF8B5CF6),
            titolo = serie.name,
            meta = listOfNotNull(
                serie.seasons.size.takeIf { it > 0 }?.let { "$it ${if (it == 1) "stagione" else "stagioni"}" },
                serie.seasons.sumOf { it.episodes.size }.takeIf { it > 0 }?.let { "$it episodi" }
            ).joinToString(" · ").ifBlank { null },
            plot = serie.plot
        ) {
            when (prossima) {
                is ProssimaVisione.Riprendi -> PulsanteAzione(
                    testo = etichettaRiprendi(prossima.episodio),
                    principale = true,
                    icona = Icons.Filled.PlayArrow,
                    focusRequester = focusPrincipale,
                    onClick = {
                        onRiproduci(
                            richiestaDi(prossima.episodio),
                            prossima.posizioneMs,
                            codaDopo(prossima.episodio)
                        )
                    }
                )

                is ProssimaVisione.Inizia -> PulsanteAzione(
                    testo = etichettaPlay(prossima.episodio),
                    principale = true,
                    icona = Icons.Filled.PlayArrow,
                    focusRequester = focusPrincipale,
                    onClick = {
                        onRiproduci(richiestaDi(prossima.episodio), 0L, codaDopo(prossima.episodio))
                    }
                )

                ProssimaVisione.Completata -> {
                    if (primoEpisodio != null) {
                        PulsanteAzione(
                            testo = "Play",
                            principale = true,
                            icona = Icons.Filled.PlayArrow,
                            focusRequester = focusPrincipale,
                            onClick = { onRiproduci(richiestaDi(primoEpisodio), 0L, codaDopo(primoEpisodio)) }
                        )
                    }
                }
            }
            PulsantePreferito(preferito, onCambiaPreferito)
        }

        if (serie.seasons.isNotEmpty()) {
            SelettoreStagioni(
                stagioni = serie.seasons,
                selezionata = stagioneSelezionata,
                visti = visti,
                onSeleziona = { stagioneSelezionata = it },
                onSegnaStagioneNonVista = { stagione ->
                    onResetVisti(stagione.episodes.map { it.url }.toSet())
                }
            )
        }

        val stagione = stagioneSelezionata ?: serie.seasons.firstOrNull()
        if (stagione != null) {
            if (stagione.number == null) {
                Text(
                    text = "Episodi rilevati dalla Sorgente senza numero di stagione o episodio riconoscibile.",
                    color = Color(0xFF6D7380),
                    fontSize = 13.sp
                )
            }
            CarouselEpisodi(
                episodi = stagione.episodes,
                posterSerie = serie.poster ?: card.imageUrl,
                visti = visti,
                onEpisodioClick = { episodio ->
                    onRiproduci(
                        richiestaDi(episodio),
                        registro.posizioneDiRipresa(visti, episodio.url) ?: 0L,
                        codaDopo(episodio)
                    )
                },
                onEpisodioRiproduciCon = { episodio -> onRiproduciCon(richiestaDi(episodio)) },
                onEpisodioSegnaNonVisto = { episodio -> onResetVisti(setOf(episodio.url)) }
            )
        }
    }
}

@Composable
private fun Pagina(sfondo: String? = null, contenuto: @Composable () -> Unit) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (sfondo != null) {
                    // La cover del contenuto, sfocata e fissa, fa da sfondo alla pagina.
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(sfondo)
                            .transformations(DownsampleBlurTransformation(targetWidth = 360, radius = 6, passes = 2))
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xCC14161A), Color(0xF214161A))
                                )
                            )
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(36.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    contenuto()
                }
            }
        }
    }
}

@Composable
private fun Testata(
    copertina: String?,
    etichetta: String,
    coloreEtichetta: Color,
    titolo: String,
    meta: String?,
    plot: String?,
    dettagli: DettaglioEsteso? = null,
    azioni: @Composable () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(36.dp)) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(340.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF262B33))
        ) {
            if (copertina != null) {
                AsyncImage(
                    model = copertina,
                    contentDescription = titolo,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = etichetta,
                color = Color(0xFF14161A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(coloreEtichetta, RoundedCornerShape(4.dp))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            )
            Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            if (meta != null) {
                Text(text = meta, color = Color(0xFF9AA0AA), fontSize = 15.sp)
            }
            if (plot != null) {
                Text(
                    text = plot,
                    color = Color(0xFFC7CAD0),
                    fontSize = 14.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(640.dp)
                )
            }
            if (dettagli != null && !dettagli.isEmpty) {
                DettagliEstesi(dettagli)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { azioni() }
        }
    }
}

@Composable
private fun DettagliEstesi(dettagli: DettaglioEsteso) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val riepilogo = listOfNotNull(
            dettagli.anno,
            dettagli.durata,
            dettagli.valutazione?.let { "★ %.1f".format(it) }
        ).joinToString(" · ")
        if (riepilogo.isNotBlank()) {
            Text(text = riepilogo, color = Color(0xFF9AA0AA), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        dettagli.regista?.let { RigaMetaEstesa("Regia", it) }
        dettagli.cast?.let { RigaMetaEstesa("Cast", it) }
        dettagli.genere?.let { RigaMetaEstesa("Genere", it) }
    }
}

@Composable
private fun RigaMetaEstesa(etichetta: String, valore: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = "$etichetta:", color = Color(0xFF6D7380), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = valore,
            color = Color(0xFFC7CAD0),
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(520.dp)
        )
    }
}

@Composable
private fun PulsanteAzione(
    testo: String,
    principale: Boolean = false,
    icona: ImageVector? = null,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit
) {
    var infocato by remember { mutableStateOf(false) }
    val sfondo = when {
        principale -> LocalAccento.current
        infocato -> Color(0xFF3A404A)
        else -> Color(0xFF262B33)
    }
    val colore = if (principale) Color(0xFF14161A) else Color(0xFFF2F2F0)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .widthIn(max = 420.dp)
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { infocato = it.isFocused }
            .clickable(onClick = onClick)
            .background(sfondo, RoundedCornerShape(8.dp))
            .border(2.dp, if (infocato) Color(0xFFF2F2F0) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        if (icona != null) {
            Icon(imageVector = icona, contentDescription = null, tint = colore, modifier = Modifier.size(18.dp))
        }
        Text(
            text = testo,
            color = colore,
            fontSize = 14.sp,
            fontWeight = if (principale) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun PulsantePreferito(preferito: Boolean, onClick: () -> Unit) {
    var infocato by remember { mutableStateOf(false) }
    Text(
        text = if (preferito) "★ Nei Preferiti" else "☆ Preferiti",
        color = if (preferito) LocalAccento.current else Color(0xFFF2F2F0),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .onFocusChanged { infocato = it.isFocused }
            .clickable(onClick = onClick)
            .background(Color(0xFF262B33), RoundedCornerShape(8.dp))
            .border(2.dp, if (infocato) Color(0xFFF2F2F0) else Color.Transparent, RoundedCornerShape(8.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

private fun nomeStagione(stagione: Stagione): String = stagione.number?.let { "Stagione $it" } ?: "Altri episodi"

/** Il dropdown Stagioni mostra al massimo 6 voci (48.dp l'una + 8.dp di padding sopra e sotto);
 * oltre, si scorre. Non si adatta a quante ne entrano nello schermo. */
private val ALTEZZA_MAX_DROPDOWN_STAGIONI = 48.dp * 6 + 16.dp

@Composable
private fun SelettoreStagioni(
    stagioni: List<Stagione>,
    selezionata: Stagione?,
    visti: List<Visto>,
    onSeleziona: (Stagione) -> Unit,
    onSegnaStagioneNonVista: (Stagione) -> Unit
) {
    var espanso by remember { mutableStateOf(false) }
    var infocato by remember { mutableStateOf(false) }
    var menuAperto by remember { mutableStateOf(false) }
    val accento = LocalAccento.current
    val stagioneCorrente = selezionata ?: stagioni.firstOrNull()
    val haVistoStagione = stagioneCorrente != null &&
        stagioneCorrente.episodes.any { ep -> visti.any { it.chiaveIdentita == ep.url } }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .onFocusChanged { infocato = it.isFocused }
                .pressabile(
                    onClick = { espanso = true },
                    onLongClick = { menuAperto = true }.takeIf { haVistoStagione }
                )
                .background(Color(0xFF1F232A), RoundedCornerShape(8.dp))
                .border(2.dp, if (infocato || espanso) accento else Color.Transparent, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = selezionata?.let { nomeStagione(it) } ?: "Stagioni",
                color = Color(0xFFF2F2F0),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(imageVector = Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF9AA0AA))
        }
        MaterialTheme(colorScheme = darkColorScheme(surface = Color(0xFF1F232A), onSurface = Color(0xFFF2F2F0))) {
            DropdownMenu(
                expanded = espanso,
                onDismissRequest = { espanso = false },
                modifier = Modifier.heightIn(max = ALTEZZA_MAX_DROPDOWN_STAGIONI)
            ) {
                stagioni.forEach { stagione ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = nomeStagione(stagione),
                                color = if (stagione.number == selezionata?.number) accento else Color(0xFFF2F2F0)
                            )
                        },
                        onClick = {
                            onSeleziona(stagione)
                            espanso = false
                        }
                    )
                }
            }
        }
    }
    if (menuAperto && stagioneCorrente != null) {
        MenuStagione(
            stagione = stagioneCorrente,
            titolo = nomeStagione(stagioneCorrente),
            onSegnaNonVista = {
                menuAperto = false
                onSegnaStagioneNonVista(stagioneCorrente)
            },
            onChiudi = { menuAperto = false }
        )
    }
}

@Composable
private fun CarouselEpisodi(
    episodi: List<Episodio>,
    posterSerie: String?,
    visti: List<Visto>,
    onEpisodioClick: (Episodio) -> Unit,
    onEpisodioRiproduciCon: (Episodio) -> Unit,
    onEpisodioSegnaNonVisto: (Episodio) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        // Spazio verticale perche' la card in focus, ingrandita, non venga tagliata.
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        items(episodi) { episodio ->
            val immagine = episodio.immagine ?: posterSerie
            CardEpisodio(
                episodio = episodio,
                immagine = immagine,
                percentuale = registro.percentuale(visti, episodio.url),
                haVisto = visti.any { it.chiaveIdentita == episodio.url },
                onClick = { onEpisodioClick(episodio) },
                onRiproduciCon = { onEpisodioRiproduciCon(episodio) },
                onSegnaNonVisto = { onEpisodioSegnaNonVisto(episodio) }
            )
        }
    }
}

@Composable
private fun CardEpisodio(
    episodio: Episodio,
    immagine: String?,
    percentuale: Int,
    haVisto: Boolean,
    onClick: () -> Unit,
    onRiproduciCon: () -> Unit,
    onSegnaNonVisto: () -> Unit
) {
    var infocato by remember { mutableStateOf(false) }
    var menuAperto by remember { mutableStateOf(false) }
    val forma = RoundedCornerShape(8.dp)
    Column(
        modifier = Modifier
            .width(240.dp)
            .onFocusChanged { infocato = it.isFocused }
            .pressabile(onClick = onClick, onLongClick = { menuAperto = true }),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(240.dp)
                .height(135.dp)
                .zoomInFocus(infocato, forma)
                .clip(forma)
                .background(Color(0xFF262B33))
                .border(
                    2.dp,
                    if (infocato) LocalAccento.current else Color.Transparent,
                    forma
                )
        ) {
            if (immagine != null) {
                AsyncImage(
                    model = immagine,
                    contentDescription = episodio.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            if (percentuale > 0) {
                BarraProgresso(
                    percentuale = percentuale,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
        Text(
            text = episodio.episodeNumber?.let { "$it. ${episodio.title}" } ?: episodio.title,
            color = Color(0xFFF2F2F0),
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    if (menuAperto) {
        MenuEpisodio(
            episodio = episodio,
            copertina = immagine,
            haVisto = haVisto,
            onRiproduciCon = {
                menuAperto = false
                onRiproduciCon()
            },
            onSegnaNonVisto = {
                menuAperto = false
                onSegnaNonVisto()
            },
            onChiudi = { menuAperto = false }
        )
    }
}

@Composable
private fun BarraProgresso(percentuale: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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

@Composable
private fun DettaglioErrore(titolo: String) {
    Pagina {
        Text(text = titolo, color = Color(0xFFF2F2F0), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Impossibile caricare i dettagli di questo contenuto.",
            color = Color(0xFF9AA0AA),
            fontSize = 16.sp
        )
    }
}
