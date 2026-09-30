package com.ir0.iptv.app

import com.ir0.iptv.app.theme.RuoloPulsante
import com.ir0.iptv.app.theme.coloriPulsante
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import com.ir0.iptv.app.navigation.LARGHEZZA_SIDEBAR
import com.ir0.iptv.app.theme.Palette
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
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
    onContenutoLongClick: (ContentCard) -> Unit = {},
    /** Solo per il pulsante "Riprendi" dell'hero: riproduce subito invece di aprire il Dettaglio
     * (a differenza di [onContenutoClick], usato per ogni altra card). */
    onRiprendiClick: (ContentCard) -> Unit = onContenutoClick,
    /** Vero mentre il pulsante "Riprendi" attende il caricamento della Serie (per un Episodio, se
     * non gia' in cache di sessione) prima di poter partire. */
    caricandoRipresa: Boolean = false
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
    // Bersaglio esplicito di GIU' dal pulsante Riprendi: la ricerca 2D di default di Compose, da
    // un elemento largo come l'hero dentro una Column scorrevole, a volte non trova la riga sotto
    // (resta ferma sul pulsante) o salta altrove invece di scendere alla card piu' vicina — con
    // un FocusRequester dedicato sulla prima card della prima riga non vuota non c'e' da indovinare.
    val focusRequesterPrimaRiga = remember { FocusRequester() }
    val primaSezioneConContenuto = remember(righe, ordine, hero) {
        ordine.firstOrNull { sezione ->
            sezione != SezioneHome.SPORT && contenutiRigaVisibili(tipoRigaDi(sezione)!!).isNotEmpty()
        }
    }
    // Lo stesso contenuto (es. una Serie nei Preferiti) puo' comparire in piu' righe insieme
    // all'hero: senza questo, ogni riga la cui card corrisponde a chiaveDaFocalizzare attaccava
    // lo stesso FocusRequester condiviso, e Compose finiva per dare il focus alla card sbagliata
    // (l'ultima composta) invece che all'hero — un salto silenzioso subito dopo l'apertura, prima
    // di qualunque tasto premuto. Qui si decide UNA sola riga bersaglio, mai l'hero se gia' lui
    // stesso corrisponde a chiaveDaFocalizzare.
    // Senza una card da ripristinare (primo avvio della Home) il focus va comunque sull'hero: altrimenti
    // restava senza bersaglio e finiva sulla Sidebar.
    val heroHaChiaveDaFocalizzare = hero != null && (hero.chiaveIdentita == chiaveDaFocalizzare || chiaveDaFocalizzare == null)
    val rigaConChiaveDaFocalizzare = remember(righe, ordine, hero, chiaveDaFocalizzare) {
        if (heroHaChiaveDaFocalizzare || chiaveDaFocalizzare == null) {
            null
        } else {
            ordine.firstOrNull { sezione ->
                sezione != SezioneHome.SPORT &&
                    contenutiRigaVisibili(tipoRigaDi(sezione)!!).any { it.chiaveIdentita == chiaveDaFocalizzare }
            }
        }
    }

    LaunchedEffect(chiaveDaFocalizzare, righe, ordine, hero) {
        when {
            heroHaChiaveDaFocalizzare -> {
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

    // Il contenuto "in evidenza" guida lo sfondo a tutto schermo: l'hero all'apertura, poi la card
    // che ha il focus mentre si scorre tra le righe.
    var inEvidenza by remember(hero?.chiaveIdentita) { mutableStateOf(hero) }
    val focalizza: (ContentCard) -> Unit = { inEvidenza = it }

    Box(modifier = Modifier.fillMaxSize().background(Palette.inchiostro)) {
    SfondoAmbientale(inEvidenza = inEvidenza)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(statoColonna)
            .padding(top = if (hero == null) 32.dp else 0.dp, bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        if (hero != null) {
            HeroContinua(
                card = hero,
                percentuale = registroVisti.percentuale(visti, hero.chiaveIdentita),
                preferito = elencoPreferiti.preferito(personalizzazioni, hero),
                daRiprendere = heroDaRiprendere,
                caricando = heroDaRiprendere && caricandoRipresa,
                focusRequester = focusRequester.takeIf { heroHaChiaveDaFocalizzare },
                focusRequesterGiu = focusRequesterPrimaRiga.takeIf { primaSezioneConContenuto != null },
                // Risalendo col D-pad, quando il pulsante prende il focus si riporta la colonna
                // in cima cosi' la banda "Continua a guardare" si vede tutta. Va rifatto per
                // qualche frame: il bring-into-view del focus vorrebbe mostrare solo il pulsante
                // (in basso nell'hero) e altrimenti avrebbe l'ultima parola.
                onFocalizzato = {
                    inEvidenza = hero
                    scope.launch { repeat(4) { withFrameNanos {}; statoColonna.scrollTo(0) } }
                },
                // "Riprendi" (Continua a guardare) parte subito; per qualunque altro hero (Serie
                // mai iniziata, Film, Canale dal Contenuto di default) il click apre il Dettaglio
                // come per qualsiasi altra card.
                onClick = { if (heroDaRiprendere) onRiprendiClick(hero) else onContenutoClick(hero) }
            )
        }
        ordine.forEachIndexed { indice, sezione ->
          // Le righe entrano una dopo l'altra (solo disegno: sono focalizzabili da subito).
          Box(modifier = Modifier.entrataScaglionata(indice)) {
            if (sezione == SezioneHome.SPORT) {
                FasciaSport(partite = sport, onCanaleClick = onContenutoClick)
            } else {
                val tipo = tipoRigaDi(sezione)!!
                RigaContenuti(
                    onFocalizzata = focalizza,
                    titolo = sezione.etichetta,
                    contenuti = contenutiRigaVisibili(tipo),
                    visti = visti,
                    personalizzazioni = personalizzazioni,
                    chiaveDaFocalizzare = chiaveDaFocalizzare.takeIf { sezione == rigaConChiaveDaFocalizzare },
                    focusRequester = focusRequester,
                    focusRequesterPrimoElemento = focusRequesterPrimaRiga.takeIf { sezione == primaSezioneConContenuto },
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
    }
}

/**
 * Sfondo a tutto schermo della Home, dietro hero e righe: la locandina del contenuto in evidenza,
 * allargata a destra e sfumata verso sinistra e verso il basso, con un lento zoom continuo.
 *
 * - Cambia solo dopo che il focus si e' fermato ~300ms su una card: scorrendo veloce una riga non
 *   si caricano (ne' si dissolvono) dieci immagini di fila.
 * - I Canali non lo cambiano: un logo ingrandito a tutto schermo e' solo una macchia.
 * - La locandina e' leggermente sfocata: e' pensata per una card, ingrandita a questa scala
 *   sarebbe comunque sgranata; sfocata diventa "atmosfera" e non distrae dal testo sopra.
 * - Si estende anche sotto la Sidebar (che e' semitrasparente), per un'immagine davvero a bordo
 *   schermo.
 */
@Composable
private fun SfondoAmbientale(inEvidenza: ContentCard?) {
    var immagine by remember { mutableStateOf(inEvidenza?.takeUnless { it is ContentCard.Canale }?.imageUrl) }
    val corrente by rememberUpdatedState(inEvidenza)
    LaunchedEffect(Unit) {
        snapshotFlow { corrente }
            .collectLatest { card ->
                val url = card?.takeUnless { it is ContentCard.Canale }?.imageUrl ?: return@collectLatest
                delay(300)
                immagine = url
            }
    }
    val zoomLento = rememberInfiniteTransition(label = "kenBurns")
    val scala by zoomLento.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 22_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "kenBurnsScala"
    )
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .estendiASinistra(LARGHEZZA_SIDEBAR)
    ) {
        Crossfade(
            targetState = immagine,
            animationSpec = tween(durationMillis = 900, easing = EasingCinema),
            modifier = Modifier.fillMaxSize(),
            label = "sfondoAmbientale"
        ) { url ->
            Box(modifier = Modifier.fillMaxSize()) {
                if (url != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(url)
                            .transformations(DownsampleBlurTransformation(targetWidth = 480, radius = 2, passes = 1))
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .fillMaxWidth(0.74f)
                            .fillMaxHeight(0.82f)
                            .sfumaBordiScenografia()
                            .graphicsLayer {
                                scaleX = scala
                                scaleY = scala
                                transformOrigin = TransformOrigin(0.7f, 0.3f)
                            }
                    )
                }
            }
        }
        // Sfumatura da sinistra: il testo dell'hero e le icone della Sidebar restano leggibili.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Palette.inchiostro,
                        0.28f to Palette.inchiostro.copy(alpha = 0.92f),
                        0.55f to Palette.inchiostro.copy(alpha = 0.45f),
                        1f to Palette.inchiostro.copy(alpha = 0.15f)
                    )
                )
        )
        // Sfumatura dal basso: le righe poggiano su un fondo pieno, non sull'immagine.
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        0f to Palette.inchiostro.copy(alpha = 0.35f),
                        0.18f to Color.Transparent,
                        0.5f to Palette.inchiostro.copy(alpha = 0.55f),
                        0.78f to Palette.inchiostro.copy(alpha = 0.96f),
                        1f to Palette.inchiostro
                    )
                )
        )
    }
}

/** Misura il contenuto [extra] piu' largo e lo sposta a sinistra della stessa quantita', lasciando
 * invariata la dimensione dichiarata al genitore: serve a far passare lo sfondo sotto la Sidebar. */
private fun Modifier.estendiASinistra(extra: Dp): Modifier = layout { misurabile, vincoli ->
    val px = extra.roundToPx()
    val larghezza = vincoli.maxWidth + px
    val piazzabile = misurabile.measure(vincoli.copy(minWidth = larghezza, maxWidth = larghezza))
    layout(vincoli.maxWidth, piazzabile.height) { piazzabile.place(-px, 0) }
}

private fun tipoRigaDi(sezione: SezioneHome): TipoRiga? = when (sezione) {
    SezioneHome.SPORT -> null
    SezioneHome.CONTINUA -> TipoRiga.CONTINUA
    SezioneHome.NUOVI_EPISODI -> TipoRiga.NUOVI_EPISODI
    SezioneHome.SUGGERITI -> TipoRiga.SUGGERITI
    SezioneHome.PREFERITI -> TipoRiga.PREFERITI
}

private val ALTEZZA_HERO = 400.dp

/** La banda in evidenza con il contenuto da riprendere, a tutta larghezza sopra lo sfondo
 * ambientale (vedi [SfondoAmbientale]): titolo grande e pulsante in basso a sinistra, la locandina
 * nitida a destra. Il focus (e quindi il D-pad all'avvio) va sul pulsante, non sull'intera banda:
 * cosi' premere OK riproduce subito, senza dover indovinare dove sia l'area cliccabile. */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun HeroContinua(
    card: ContentCard,
    percentuale: Int,
    preferito: Boolean,
    daRiprendere: Boolean,
    focusRequester: FocusRequester?,
    /** Bersaglio esplicito di GIU' dal pulsante: null quando nessuna riga sotto ha contenuti (in
     * quel caso GIU' non porta da nessuna parte, invece di far indovinare alla ricerca di focus
     * di Compose). */
    focusRequesterGiu: FocusRequester? = null,
    /** Vero mentre "Riprendi" attende il caricamento della Serie prima di poter partire: il
     * pulsante mostra uno spinner al posto dell'icona Play e non risponde al click. */
    caricando: Boolean = false,
    onFocalizzato: () -> Unit = {},
    onClick: () -> Unit
) {
    var pulsanteInfocato by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val accento = LocalAccento.current
    val etichetta = when {
        daRiprendere -> "Continua a guardare"
        card is ContentCard.SerieCard -> "Serie"
        card is ContentCard.Canale -> "Canale"
        else -> "Film"
    }
    val etichettaPulsante = when {
        caricando -> "Caricamento…"
        daRiprendere -> "Riprendi"
        card is ContentCard.SerieCard -> "Vai alla Serie"
        else -> "Riproduci"
    }

    // Entrata dell'hero: il testo sale e compare, la locandina scivola da destra poco dopo.
    val entrata = remember(card.chiaveIdentita) { Animatable(0f) }
    LaunchedEffect(card.chiaveIdentita) {
        entrata.animateTo(1f, tween(durationMillis = 900, easing = EasingCinema))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(ALTEZZA_HERO)
    ) {
        val imageUrl = card.imageUrl
        if (imageUrl != null) {
            val verticale = card !is ContentCard.Canale
            AsyncImage(
                model = ImageRequest.Builder(context).data(imageUrl).crossfade(400).build(),
                contentDescription = card.title,
                contentScale = if (verticale) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 72.dp, top = 24.dp)
                    .height(if (verticale) 300.dp else 180.dp)
                    .aspectRatio(if (verticale) 2f / 3f else 16f / 9f)
                    .graphicsLayer {
                        alpha = entrata.value
                        translationX = (1f - entrata.value) * 60.dp.toPx()
                    }
                    .shadow(elevation = 32.dp, shape = RoundedCornerShape(12.dp), clip = false)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Palette.superficieAlta)
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 32.dp, bottom = 8.dp)
                .width(560.dp)
                .graphicsLayer {
                    alpha = entrata.value
                    translationY = (1f - entrata.value) * 24.dp.toPx()
                },
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(14.dp)
                        .background(accento)
                )
                Text(
                    text = etichetta.uppercase(),
                    color = Palette.testoSecondario,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                if (preferito) {
                    Icon(
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = "Preferito",
                        tint = accento,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Text(
                text = card.title,
                color = Palette.testo,
                fontSize = 46.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (percentuale > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(220.dp)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Palette.testo.copy(alpha = 0.22f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percentuale / 100f)
                                .fillMaxHeight()
                                .background(accento)
                        )
                    }
                    Text(
                        text = "$percentuale% visto",
                        color = Palette.testoSecondario,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            val coloriRiprendi = coloriPulsante(RuoloPulsante.PRIMARIO, pulsanteInfocato)
            val fondoPulsante = coloriRiprendi.sfondo
            val colorePulsante = coloriRiprendi.contenuto
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
                    // GIU' va sempre sulla prima card sotto quando esiste, invece di lasciare che
                    // la ricerca 2D di default (da un elemento largo come l'hero dentro una Column
                    // scorrevole) non trovi nulla o salti a una card lontana. SINISTRA non va
                    // toccata qui: Cancel bloccherebbe il movimento sul posto invece di lasciarlo
                    // risalire all'exit gia' gestito in MainActivity (atterra sulla Sidebar).
                    .focusProperties {
                        down = focusRequesterGiu ?: FocusRequester.Cancel
                    }
                    .onFocusChanged {
                        pulsanteInfocato = it.isFocused
                        if (it.isFocused) onFocalizzato()
                    }
                    .zoomInFocus(pulsanteInfocato, RoundedCornerShape(10.dp), scalaMax = 1.06f, ombraMax = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = !caricando, onClick = onClick)
                    .background(fondoPulsante)
                    .padding(horizontal = 26.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (caricando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = colorePulsante,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = colorePulsante,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(text = etichettaPulsante, color = colorePulsante, fontSize = 17.sp, fontWeight = FontWeight.Bold)
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
    /** Bersaglio di GIU' dal pulsante Riprendi dell'hero: quando non null va sulla prima card
     * visibile di questa riga (solo la riga che l'hero ha scelto come prima con contenuti lo passa). */
    focusRequesterPrimoElemento: FocusRequester? = null,
    onClick: (ContentCard) -> Unit,
    onLongClick: (ContentCard) -> Unit = {},
    personalizzazioni: Map<String, ContentCustomization> = emptyMap(),
    /** Quando non null, la riga resta visibile (con questo messaggio) anche a contenuti vuoti;
     * quando null, una riga vuota semplicemente non compare (comportamento delle righe di
     * catalogo su Sfoglia/Cerca/Sport, dove una sezione vuota non aggiunge nulla da leggere). */
    messaggioVuoto: String? = null,
    /** Chiamato quando una card della riga prende il focus (la Home ci cambia lo sfondo). */
    onFocalizzata: (ContentCard) -> Unit = {}
) {
    if (contenuti.isEmpty()) {
        if (messaggioVuoto == null) return
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TitoloRiga(titolo)
            Text(
                text = messaggioVuoto,
                color = Palette.testoTerziario,
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
    // Il bersaglio di GIU' dall'hero va sulla prima card VISIBILE, non sulla card 0: appena la
    // riga era scorsa (ripristino del focus su una card lontana, o scorrendo a destra e poi
    // risalendo sull'hero) la card 0 usciva di composizione, il FocusRequester restava senza nodo
    // e GIU' dal pulsante Riprendi non faceva nulla (l'eccezione di Compose veniva inghiottita da
    // MainActivity.dispatchKeyEvent). La prima visibile e' sempre composta.
    val primaVisibile by remember { derivedStateOf { statoRiga.firstVisibleItemIndex } }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        TitoloRiga(titolo)
        LazyRow(
            state = statoRiga,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            // Il padding verticale lascia respirare la card in focus, ingrandita, senza tagliarla:
            // con lo zoom ancorato in basso la crescita e' tutta verso l'alto.
            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 20.dp)
        ) {
            itemsIndexed(contenuti) { indice, card ->
                CardContenuto(
                    card = card,
                    percentuale = registroVisti.percentuale(visti, card.chiaveIdentita),
                    preferito = elencoPreferiti.preferito(personalizzazioni, card),
                    focusRequester = focusRequester.takeIf { card.chiaveIdentita == chiaveDaFocalizzare },
                    focusRequesterAggiuntivo = focusRequesterPrimoElemento.takeIf { indice == primaVisibile },
                    onClick = { onClick(card) },
                    onLongClick = { onLongClick(card) },
                    onFocalizzata = { onFocalizzata(card) }
                )
            }
        }
    }
}

@Composable
private fun TitoloRiga(titolo: String) {
    Text(
        text = titolo,
        color = Palette.testo,
        fontSize = 21.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

/** Stato vuoto composto: un titolo che dice cosa succede e il messaggio operativo sotto, invece
 * di una riga di testo grigio sperduta in un angolo. */
@Composable
fun SchermataVuota(messaggio: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(Palette.superficieAlta, Palette.inchiostro),
                    radius = 1400f
                )
            )
            .padding(48.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.width(620.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(LocalAccento.current)
            )
            Text(
                text = "Niente da mostrare, per ora",
                color = Palette.testo,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )
            Text(text = messaggio, color = Palette.testoSecondario, fontSize = 17.sp, lineHeight = 25.sp)
        }
    }
}

private fun messaggioVuotoDi(tipo: TipoRiga): String = when (tipo) {
    TipoRiga.CONTINUA -> "Quello che guardi appare qui, per riprendere da dove avevi lasciato."
    TipoRiga.NUOVI_EPISODI ->
        "Segui una Serie (guardala o aggiungila ai Preferiti) per vedere qui i nuovi episodi."
    TipoRiga.SUGGERITI -> "Aggiungi la chiave API di Claude dalle Impostazioni per ricevere suggerimenti."
    TipoRiga.PREFERITI -> "Nessun Preferito ancora: aggiungine uno dalla sua pagina di Dettaglio."
}
