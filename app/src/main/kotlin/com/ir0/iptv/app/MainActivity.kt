package com.ir0.iptv.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ir0.iptv.app.content.CatalogoRepository
import com.ir0.iptv.app.content.ContentFetcher
import com.ir0.iptv.app.content.DettaglioCache
import com.ir0.iptv.app.customization.PersonalizzazioneRepository
import com.ir0.iptv.app.dashboard.NuoviEpisodi
import com.ir0.iptv.app.dashboard.NuoviEpisodiRepository
import com.ir0.iptv.app.dashboard.SuggerimentiAi
import com.ir0.iptv.app.logging.RegistroApp
import com.ir0.iptv.app.navigation.Destinazione
import com.ir0.iptv.app.navigation.Sidebar
import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.app.playback.RiproduciCon
import com.ir0.iptv.app.playback.VistoRepository
import com.ir0.iptv.app.settings.Impostazioni
import com.ir0.iptv.app.settings.ImpostazioniRepository
import com.ir0.iptv.app.sport.PartitaConCanale
import com.ir0.iptv.app.sport.SportInEvidenza
import com.ir0.iptv.app.theme.Accento
import com.ir0.iptv.app.theme.LocalAccento
import com.ir0.iptv.app.webpanel.EsitoSorgente
import com.ir0.iptv.app.webpanel.PonteTv
import com.ir0.iptv.app.webpanel.QrCodeGenerator
import com.ir0.iptv.app.webpanel.SorgenteRepository
import com.ir0.iptv.app.webpanel.WebPanelServer
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.dashboard.CostruttoreDashboard
import com.ir0.iptv.domain.dashboard.MemoriaFocus
import com.ir0.iptv.domain.dashboard.RigaDashboard
import com.ir0.iptv.domain.dashboard.TipoRiga
import com.ir0.iptv.domain.playback.NavigazioneSerie
import com.ir0.iptv.domain.playback.ProssimaVisione
import com.ir0.iptv.domain.playback.ProssimaVisioneResolver
import com.ir0.iptv.domain.playback.RegistroVisti
import com.ir0.iptv.domain.playback.TipoVisto
import com.ir0.iptv.domain.source.Sorgente
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.SocketException
import java.util.Collections
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SORGENTI_POLL_INTERVAL_MS = 2000L

private const val QR_CODE_SIZE_PX = 768
private val MIN_TEXT_COLUMN_WIDTH = 280.dp
private val ROW_SPACING = 64.dp

private val costruttoreDashboard = CostruttoreDashboard()
private val memoriaFocus = MemoriaFocus()
private val elencoPreferiti = ElencoPreferiti()
private val registroVisti = RegistroVisti()
private val prossimaVisioneResolver = ProssimaVisioneResolver()
private val navigazioneSerie = NavigazioneSerie()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RegistroApp.inizializza(applicationContext)
        // Un solo handler per l'intero processo: si registra una volta sola, non ad ogni
        // ricreazione dell'Activity, altrimenti si accatasterebbero le chiamate precedenti.
        if (Thread.getDefaultUncaughtExceptionHandler() !is RegistroCrashHandler) {
            Thread.setDefaultUncaughtExceptionHandler(
                RegistroCrashHandler(Thread.getDefaultUncaughtExceptionHandler())
            )
        }
        // Nessuna Bundle salvata: e' un vero avvio a freddo (app chiusa/appena installata), non
        // una ricreazione dell'Activity dopo che il sistema l'ha uccisa in background per
        // liberare memoria col task ancora vivo (quel caso arriva con una Bundle non nulla).
        val eraChiusa = savedInstanceState == null
        val sorgenteRepository = SorgenteRepository(applicationContext)
        val catalogoRepository = CatalogoRepository(applicationContext)
        val vistoRepository = VistoRepository(applicationContext)
        val personalizzazioneRepository = PersonalizzazioneRepository(applicationContext)
        val impostazioniRepository = ImpostazioniRepository(applicationContext)
        val nuoviEpisodi = NuoviEpisodi(NuoviEpisodiRepository(applicationContext))
        val suggerimentiAi = SuggerimentiAi()
        val sportInEvidenza = SportInEvidenza()
        setContent {
            // Nessun BackHandler qui: durante l'avvio o il caricamento del catalogo non c'e'
            // nessuna schermata da cui "tornare indietro", quindi Back deve comportarsi come da
            // default di Android e lasciare che il sistema mostri il suo dialogo "Uscire
            // dall'app?" (prima veniva soppresso da un BackHandler no-op, e quel dialogo non
            // compariva mai nemmeno a schermata vuota).
            var sorgenti by remember { mutableStateOf(sorgenteRepository.elenco()) }
            // Il poll non si fermava piu' appena la lista non era vuota: modificare o sostituire
            // la Sorgente dal Pannello Web (es. passare da un account Xtream rotto a un M3U
            // valido) restava senza alcun effetto finche' l'app non veniva chiusa e riaperta -
            // confermato sul dispositivo, la sincronizzazione ripeteva la vecchia Sorgente
            // Xtream anche minuti dopo averla sostituita con una M3U funzionante. Qui il poll
            // continua per tutta la vita dell'app; si aggiorna lo stato solo quando la lista e'
            // davvero cambiata, per non ricomporre/risincronizzare ad ogni giro a vuoto.
            LaunchedEffect(Unit) {
                while (true) {
                    delay(SORGENTI_POLL_INTERVAL_MS)
                    val aggiornate = sorgenteRepository.elenco()
                    if (aggiornate != sorgenti) sorgenti = aggiornate
                }
            }

            if (sorgenti.isEmpty()) {
                OnboardingScreen(webPanelAddress = remember { localWebPanelAddress() })
            } else {
                ContentScreen(
                    sorgenti = sorgenti,
                    catalogoRepository = catalogoRepository,
                    vistoRepository = vistoRepository,
                    eraChiusa = eraChiusa,
                    personalizzazioneRepository = personalizzazioneRepository,
                    impostazioniRepository = impostazioniRepository,
                    nuoviEpisodi = nuoviEpisodi,
                    suggerimentiAi = suggerimentiAi,
                    sportInEvidenza = sportInEvidenza
                )
            }
        }
    }

    // Race nota di Compose (non risolta lato libreria): un Modifier.focusProperties { exit = ... }
    // che restituisce un FocusRequester condiviso (qui focusSidebarSezione, riassegnato ad
    // un'icona diversa della Sidebar ad ogni cambio Destinazione) puo' arrivare a un tasto
    // direzionale nel breve istante in cui non e' ancora attaccato a nessuna icona, facendo
    // sollevare a Compose stesso IllegalStateException("FocusRequester is not initialized") fuori
    // da qualunque nostro codice richiamabile - confermato da un crash reale raccolto sul
    // dispositivo (dumpsys dropbox, data_app_crash) durante la navigazione col D-pad. Qui si
    // ignora solo quella singola eccezione, consumando il tasto invece di far cadere l'app.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean = try {
        super.dispatchKeyEvent(event)
    } catch (e: IllegalStateException) {
        // Solo questa race nota va ignorata: qualunque altra IllegalStateException risale
        // normalmente, per non mascherare un bug diverso e reale.
        if (e.message?.contains("FocusRequester is not initialized") != true) throw e
        RegistroApp.errore("Focus", "Ignorato un tasto durante una race di focus di Compose", e)
        true
    }
}

/** Registra ogni crash nel registro dell'app (visibile dalle Impostazioni) prima di passare la
 * mano al comportamento di default di Android, che resta invariato (log su logcat, chiusura del
 * processo): questo handler osserva soltanto, non sostituisce nulla. */
private class RegistroCrashHandler(
    private val precedente: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        RegistroApp.crash(throwable)
        precedente?.uncaughtException(thread, throwable)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ContentScreen(
    sorgenti: List<Sorgente>,
    catalogoRepository: CatalogoRepository,
    vistoRepository: VistoRepository,
    /** Vero solo per un avvio a freddo vero e proprio (app chiusa): la sync automatica parte solo
     * in quel caso, mai per una ricreazione dopo il background (vedi ADR 0009). */
    eraChiusa: Boolean,
    personalizzazioneRepository: PersonalizzazioneRepository,
    impostazioniRepository: ImpostazioniRepository,
    nuoviEpisodi: NuoviEpisodi,
    suggerimentiAi: SuggerimentiAi,
    sportInEvidenza: SportInEvidenza
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    // La copia locale (ADR 0008) si legge subito, cosi' un avvio a freddo mostra qualcosa
    // invece di aspettare la rete; resta null solo se non e' mai stata salvata una volta.
    var catalogo by remember(sorgenti) { mutableStateOf(catalogoRepository.leggi()) }
    // Il catalogo appena scaricato dalla rete, distinto da quello a schermo: tiene le righe
    // Nuovi episodi/Suggeriti/Sport (chiamate a pagamento o a rate limitato) legate a un vero
    // fetch, cosi' non ripartono anche per la sola copia locale mostrata all'avvio.
    var catalogoScaricato by remember(sorgenti) { mutableStateOf<ContentCatalog?>(null) }
    var richiesteDiAggiornamento by remember(sorgenti) { mutableStateOf(0) }
    var inAggiornamento by remember(sorgenti) { mutableStateOf(false) }
    // Nome della Sorgente in corso e avanzamento (indice/totale): letti dalla schermata di
    // caricamento del primissimo avvio.
    var sorgenteInCorso by remember(sorgenti) { mutableStateOf<String?>(null) }
    var progressoSync by remember(sorgenti) { mutableStateOf<Pair<Int, Int>?>(null) }
    // Da 0f a 1f, quanto e' avanzata la Sorgente in corso: riempie il bordo dell'icona "Aggiorna
    // catalogo" in Sidebar (vedi ContentFetcher.onProgressoFrazionale).
    var progressoFrazionale by remember(sorgenti) { mutableStateOf<Float?>(null) }
    // Il Pannello Web puo' chiedere di forzare una sincronizzazione (bottone "Forza
    // sincronizzazione" sulla pagina Sorgenti): ogni incremento del contatore vale come un tocco
    // su "Aggiorna catalogo" in Sidebar, stesso effetto sia che arrivi dalla TV sia dal browser.
    val richiestaSyncWeb by PonteTv.richiestaSincronizzazione.collectAsState()
    LaunchedEffect(richiestaSyncWeb) {
        if (richiestaSyncWeb > 0) richiesteDiAggiornamento++
    }
    LaunchedEffect(sorgenti, richiesteDiAggiornamento) {
        // Al primo giro (nessun tocco su "Aggiorna catalogo") la sync automatica parte solo se
        // l'app era davvero chiusa: tornando dal background si resta sulla sola copia locale
        // gia' in memoria/su disco (vedi ADR 0009).
        if (richiesteDiAggiornamento == 0 && !eraChiusa) return@LaunchedEffect
        inAggiornamento = true
        // Il catalogo vecchio resta a schermo durante un aggiornamento: azzerarlo
        // riporterebbe allo scheletro di caricamento ad ogni refresh.
        val esiti = mutableListOf<EsitoSorgente>()
        val aggiornato = ContentFetcher().catalogo(
            sorgenti,
            onProgresso = { indice, totale, sorgente ->
                sorgenteInCorso = sorgente.nome
                progressoSync = indice to totale
                progressoFrazionale = 0f
            },
            onProgressoFrazionale = { frazione -> progressoFrazionale = frazione },
            onEsito = { sorgente, errore ->
                esiti += EsitoSorgente(sorgente.nome, System.currentTimeMillis(), errore)
            }
        )
        val fallita = esiti.firstOrNull { it.erroreMessaggio != null }
        // Una sincronizzazione fallita (Sorgente irraggiungibile) restituisce un catalogo vuoto:
        // sovrascrivere con quello, come si faceva prima, cancellava un catalogo buono gia' a
        // schermo (e su disco) per un problema di rete transitorio - confermato in campo, il
        // provider di questo account e' andato giu' proprio durante una prova. Un fallimento non
        // tocca ne' lo schermo ne' la copia locale quando c'e' gia' qualcosa di buono da tenere;
        // al primissimo avvio (nessuna copia locale) si mostra comunque il risultato, vuoto o no,
        // altrimenti la schermata di caricamento resterebbe bloccata per sempre.
        val vecchioNonVuoto = catalogo?.isEmpty == false
        if (aggiornato.isEmpty && fallita != null && vecchioNonVuoto) {
            RegistroApp.errore(
                "Sorgente",
                "Sincronizzazione fallita e catalogo risultante vuoto: tenuta la copia precedente"
            )
        } else {
            catalogo = aggiornato
            catalogoScaricato = aggiornato
            catalogoRepository.salva(aggiornato)
            // Riaggancia Preferiti e Visti la cui Chiave non esiste piu' in questo catalogo (es.
            // la Sorgente e' passata da un formato all'altro dello stesso provider - Fase 13),
            // invece di lasciarli semplicemente spariti.
            vistoRepository.riconcilia(aggiornato)
            personalizzazioneRepository.riconcilia(aggiornato)
        }
        PonteTv.pubblicaEsitiSincronizzazione(esiti)
        // Prima di questo fix un fallimento di sincronizzazione era visibile solo nel Registro
        // dell'app o nel Pannello Web: chi ha in mano solo il telecomando non lo vedeva mai. Un
        // Toast basta: non serve un banner persistente per un evento che si puo' solo riprovare.
        fallita?.let {
            Toast.makeText(
                context,
                "Sincronizzazione fallita per '${it.nomeSorgente}': ${it.erroreMessaggio}",
                Toast.LENGTH_LONG
            ).show()
        }
        // Una Serie in cache potrebbe avere nuovi Episodi arrivati proprio con questo refresh.
        DettaglioCache.pulisci()
        inAggiornamento = false
        sorgenteInCorso = null
        progressoSync = null
        progressoFrazionale = null
    }
    val catalogoCorrente = catalogo
    if (catalogoCorrente == null) {
        LoadingScreen(
            messaggio = sorgenteInCorso?.let { nome ->
                val totale = progressoSync?.second ?: sorgenti.size
                val indice = progressoSync?.first ?: 0
                "Caricamento $nome… (${indice + 1} di $totale)"
            } ?: "Caricamento contenuti…"
        )
        return
    }

    var impostazioni by remember { mutableStateOf(impostazioniRepository.leggi()) }
    var destinazione by remember { mutableStateOf(Destinazione.DASHBOARD) }
    // L'app apre sempre sulla Dashboard, mai dritta sul Player (ADR 0009, ribalta la scelta
    // della Fase 9): la posizione di ripresa resta comunque salvata tramite il Visto, si rientra
    // col pulsante "Riprendi".
    var sovrapposte by remember { mutableStateOf(emptyList<Screen>()) }
    // Il contenuto per cui e' aperto il menu rapido (pressione lunga su una card).
    var cardMenu by remember { mutableStateOf<ContentCard?>(null) }
    // Bumped dopo un'azione del menu rapido (es. Preferiti) per rileggere Visti e Personalizzazioni
    // senza dover cambiare schermata.
    var refreshDati by remember { mutableStateOf(0) }
    // Back chiude prima le schermate aperte sopra (Dettaglio, player), poi riporta alla
    // Dashboard da qualsiasi sezione della Sidebar. Sulla Dashboard, radice dell'app, non c'e'
    // piu' nulla da chiudere: li' Back mostra un dialogo di conferma invece di uscire subito.
    // Un tentativo precedente contava sul dialogo di sistema "Uscire dall'app?" che Android TV
    // mostrerebbe di default lasciando che Back finisca l'Activity - verificato sul dispositivo
    // reale (Xiaomi/Mediatek) che quel dialogo non esiste affatto su questa TV: Back finiva
    // l'app in silenzio. Qui si mostra un dialogo nostro, cosi' non serve contare sul sistema.
    var mostraDialogoUscita by remember { mutableStateOf(false) }
    BackHandler(enabled = !mostraDialogoUscita && (sovrapposte.isNotEmpty() || destinazione != Destinazione.DASHBOARD)) {
        if (sovrapposte.isNotEmpty()) {
            sovrapposte = sovrapposte.dropLast(1)
        } else {
            destinazione = Destinazione.DASHBOARD
        }
    }
    BackHandler(enabled = !mostraDialogoUscita && sovrapposte.isEmpty() && destinazione == Destinazione.DASHBOARD) {
        mostraDialogoUscita = true
    }

    // Rileggere ad ogni cambio di schermata tiene aggiornate le barre di avanzamento
    // e la riga Continua a guardare dopo una riproduzione.
    val visti = remember(sovrapposte, destinazione, catalogoCorrente, refreshDati) { vistoRepository.elenco() }
    val personalizzazioni = remember(sovrapposte, destinazione, catalogoCorrente, refreshDati) {
        personalizzazioneRepository.elenco()
    }
    var rigaNuoviEpisodi by remember(catalogoScaricato) {
        mutableStateOf(RigaDashboard(TipoRiga.NUOVI_EPISODI, emptyList()))
    }
    var rigaSuggeriti by remember(catalogoScaricato) {
        mutableStateOf(RigaDashboard(TipoRiga.SUGGERITI, emptyList()))
    }
    var partiteInEvidenza by remember(catalogoScaricato) { mutableStateOf(emptyList<PartitaConCanale>()) }
    LaunchedEffect(catalogoScaricato) {
        val scaricato = catalogoScaricato ?: return@LaunchedEffect
        rigaNuoviEpisodi = nuoviEpisodi.riga(scaricato, visti, personalizzazioni)
            ?: RigaDashboard(TipoRiga.NUOVI_EPISODI, emptyList())
        rigaSuggeriti = suggerimentiAi.riga(
            chiaveApi = impostazioni.chiaveApiAi,
            catalogo = scaricato,
            visti = visti,
            personalizzazioni = personalizzazioni
        ) ?: RigaDashboard(TipoRiga.SUGGERITI, emptyList())
        partiteInEvidenza = sportInEvidenza.partite(
            attivo = impostazioni.sportInDashboard,
            chiaveApi = impostazioni.chiaveApiSport,
            canali = scaricato.canali,
            limite = 20
        )
    }

    val righe = remember(catalogoCorrente, visti, personalizzazioni, rigaNuoviEpisodi, rigaSuggeriti) {
        costruttoreDashboard.costruisci(
            catalogo = catalogoCorrente,
            visti = visti,
            personalizzazioni = personalizzazioni,
            righeExtra = listOf(rigaNuoviEpisodi, rigaSuggeriti)
        )
    }

    var chiaveDaFocalizzare by remember(catalogoCorrente) {
        mutableStateOf(
            memoriaFocus.focusIniziale(
                righe = righe,
                contenutoDiDefault = impostazioniRepository.leggi().contenutoDiDefault
            )
        )
    }

    fun apri(card: ContentCard) {
        chiaveDaFocalizzare = card.chiaveIdentita
        sovrapposte = sovrapposte + when (card) {
            is ContentCard.Canale -> Screen.Player(card.toRichiesta(), 0L)
            else -> Screen.Detail(card)
        }
    }

    // Il pulsante "Riprendi" nell'hero della Dashboard, mostrato mentre carica la Serie.
    var caricandoRipresa by remember { mutableStateOf(false) }

    /** Il pulsante "Riprendi" nell'hero della Dashboard riproduce subito, senza passare dal
     * Dettaglio (a differenza di un click su qualunque altra card, che continua ad aprirlo). Per
     * un Film basta quel che si sa gia'; per un Episodio serve la Serie completa per calcolare la
     * coda dei prossimi episodi, quindi si attende il suo caricamento (istantaneo se gia' in
     * cache di sessione) mostrando uno stato di caricamento sul pulsante stesso. Se il
     * caricamento fallisce si ripiega sull'apertura del Dettaglio, come per qualunque altro fetch
     * fallito nell'app. */
    fun riprendiDaHero(card: ContentCard) {
        when (card) {
            is ContentCard.Film -> {
                val richiesta = richiestaDaCard(card) ?: return
                val posizione = vistoRepository.posizioneDiRipresa(card.chiaveIdentita) ?: 0L
                chiaveDaFocalizzare = card.chiaveIdentita
                sovrapposte = sovrapposte + Screen.Player(richiesta, posizione)
            }

            is ContentCard.SerieCard -> {
                caricandoRipresa = true
                scope.launch {
                    val serie = DettaglioCache.serie(card.chiaveIdentita) ?: when (card) {
                        is ContentCard.SerieCard.Pronta ->
                            card.serie.also { DettaglioCache.salvaSerie(card.chiaveIdentita, it) }
                        is ContentCard.SerieCard.DaCaricare ->
                            ContentFetcher().dettaglioSerie(card)
                                ?.also { DettaglioCache.salvaSerie(card.chiaveIdentita, it) }
                    }
                    caricandoRipresa = false
                    val prossima = serie?.let { prossimaVisioneResolver.risolvi(it, visti) }
                    if (serie == null || prossima !is ProssimaVisione.Riprendi) {
                        apri(card)
                        return@launch
                    }
                    val richiesta = richiestaDiEpisodio(prossima.episodio, serie, card.imageUrl)
                    val coda = navigazioneSerie.episodiSuccessivi(serie, prossima.episodio.url)
                        .map { richiestaDiEpisodio(it, serie, card.imageUrl) }
                    chiaveDaFocalizzare = card.chiaveIdentita
                    sovrapposte = sovrapposte + Screen.Player(richiesta, prossima.posizioneMs, coda)
                }
            }

            is ContentCard.Canale -> apri(card)
        }
    }

    LaunchedEffect(catalogoCorrente) { PonteTv.pubblicaCatalogo(catalogoCorrente) }
    val richiestoDalWeb by PonteTv.daAprire.collectAsState()
    LaunchedEffect(richiestoDalWeb) {
        richiestoDalWeb?.let {
            apri(it)
            PonteTv.aperturaConsumata()
        }
    }

    val sopra = sovrapposte.lastOrNull()
    if (sopra is Screen.Player) {
        PlayerScreen(
            richiesta = sopra.richiesta,
            posizioneIniziale = sopra.posizioneIniziale,
            haProssimoEpisodio = sopra.coda.isNotEmpty(),
            onProgresso = { posizioneMs, durataMs ->
                vistoRepository.registraProgresso(sopra.richiesta, posizioneMs, durataMs)
            },
            onProssimoEpisodio = {
                // Scatta a fine Episodio (parte da solo il successivo, anche di una Stagione
                // dopo) o dal pulsante "Prossimo episodio" nei controlli. Senza coda (Film,
                // Canale, ultimo Episodio) non succede nulla.
                sopra.coda.firstOrNull()?.let { prossima ->
                    sovrapposte = sovrapposte.dropLast(1) +
                        Screen.Player(prossima, 0L, sopra.coda.drop(1))
                }
            }
        )
        return
    }

    val focusSidebarSezione = remember { FocusRequester() }
    CompositionLocalProvider(LocalAccento provides Accento.daNome(impostazioni.accento).colore) {
        Row(modifier = Modifier.fillMaxSize().background(Color(0xFF14161A))) {
            Sidebar(
                selezionata = destinazione,
                onSeleziona = {
                    destinazione = it
                    sovrapposte = emptyList()
                },
                focusSezioneCorrente = focusSidebarSezione,
                inAggiornamento = inAggiornamento,
                progressoFrazionale = progressoFrazionale,
                onAggiorna = { richiesteDiAggiornamento++ }
            )
            Box(
                // La Sidebar si raggiunge solo con SINISTRA, e da qualsiasi punto dei contenuti
                // il focus atterra sull'icona della sezione corrente. Verso alto/basso/destra il
                // focus non esce sui contenuti stessi.
                modifier = Modifier
                    .fillMaxSize()
                    .focusProperties {
                        exit = { direzione ->
                            if (direzione == FocusDirection.Left) focusSidebarSezione
                            else FocusRequester.Cancel
                        }
                    }
                    .focusGroup()
            ) {
                when (sopra) {
                    is Screen.Detail -> {
                        var preferito by remember(sopra.card) {
                            mutableStateOf(personalizzazioneRepository.preferito(sopra.card))
                        }
                        DetailScreen(
                            card = sopra.card,
                            visti = visti,
                            preferito = preferito,
                            onCambiaPreferito = { preferito = personalizzazioneRepository.cambiaPreferito(sopra.card) },
                            onRiproduci = { richiesta, posizione, coda ->
                                sovrapposte = sovrapposte + Screen.Player(richiesta, posizione, coda)
                            },
                            onRiproduciCon = { richiesta -> RiproduciCon.avvia(context, richiesta) },
                            onResetVisti = { chiavi ->
                                vistoRepository.rimuoviVisti(chiavi)
                                refreshDati++
                            }
                        )
                    }

                    else -> when (destinazione) {
                        Destinazione.DASHBOARD -> DashboardScreen(
                            righe = righe,
                            visti = visti,
                            personalizzazioni = personalizzazioni,
                            chiaveDaFocalizzare = chiaveDaFocalizzare,
                            catalogoVuoto = catalogoCorrente.isEmpty,
                            contenutoDiDefault = impostazioni.contenutoDiDefault,
                            ordine = remember(impostazioni.ordineHome) { SezioneHome.daSalvato(impostazioni.ordineHome) },
                            sport = partiteInEvidenza.take(2),
                                onContenutoClick = { apri(it) },
                            onContenutoLongClick = { cardMenu = it },
                            onRiprendiClick = { riprendiDaHero(it) },
                            caricandoRipresa = caricandoRipresa
                        )

                        Destinazione.CANALI -> CanaliScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            personalizzazioni = personalizzazioni,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { cardMenu = it }
                        )

                        Destinazione.FILM -> FilmScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            personalizzazioni = personalizzazioni,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { cardMenu = it }
                        )

                        Destinazione.SERIE -> SerieScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            personalizzazioni = personalizzazioni,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { cardMenu = it }
                        )

                        Destinazione.GUIDA -> GuidaTvScreen(
                            catalogo = catalogoCorrente,
                            onCanaleClick = { apri(it) }
                        )

                        Destinazione.SPORT -> SportScreen(
                            catalogo = catalogoCorrente,
                            partite = partiteInEvidenza,
                            visti = visti,
                            personalizzazioni = personalizzazioni,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { cardMenu = it }
                        )

                        Destinazione.CERCA -> SearchScreen(
                            catalogo = catalogoCorrente,
                            visti = visti,
                            personalizzazioni = personalizzazioni,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { cardMenu = it }
                        )

                        Destinazione.PREFERITI -> FavoritesScreen(
                            preferiti = elencoPreferiti.preferiti(catalogoCorrente, personalizzazioni),
                            visti = visti,
                            personalizzazioni = personalizzazioni,
                            onContenutoClick = { apri(it) },
                            onContenutoLongClick = { cardMenu = it }
                        )

                        Destinazione.CONNESSIONE -> ConnessioneScreen(
                            indirizzo = remember { localWebPanelAddress() }
                        )

                        Destinazione.IMPOSTAZIONI -> ImpostazioniScreen(
                            impostazioni = impostazioni,
                            indirizzoPannelloWeb = remember { localWebPanelAddress() },
                            onCambia = { aggiornate ->
                                impostazioniRepository.salva(aggiornate)
                                impostazioni = aggiornate
                            }
                        )
                    }
                }

                cardMenu?.let { card ->
                    val posizioneRipresa = (card as? ContentCard.Film)
                        ?.let { vistoRepository.posizioneDiRipresa(it.chiaveIdentita) } ?: 0L
                    val chiaviContinua = registroVisti.continuaAGuardare(visti)
                        .map { it.serie ?: it.chiaveIdentita }.toSet()
                    val inContinua = when (card) {
                        is ContentCard.SerieCard -> card.title in chiaviContinua
                        else -> card.chiaveIdentita in chiaviContinua
                    }
                    fun riproduci(posizione: Long) {
                        val richiesta = richiestaDaCard(card) ?: return
                        cardMenu = null
                        sovrapposte = sovrapposte + Screen.Player(richiesta, posizione)
                    }
                    MenuContenuto(
                        card = card,
                        preferito = personalizzazioneRepository.preferito(card),
                        haRipresa = posizioneRipresa > 0L,
                        inContinuaAGuardare = inContinua,
                        onRiproduci = { riproduci(posizioneRipresa) },
                        onRiproduciDallInizio = { riproduci(0L) },
                        onRiproduciCon = {
                            val richiesta = richiestaDaCard(card)
                            cardMenu = null
                            if (richiesta != null) RiproduciCon.avvia(context, richiesta)
                        },
                        onApriDettaglio = {
                            cardMenu = null
                            apri(card)
                        },
                        onCambiaPreferito = {
                            personalizzazioneRepository.cambiaPreferito(card)
                            refreshDati++
                            cardMenu = null
                        },
                        onRimuoviDaContinua = {
                            vistoRepository.rimuoviDaiVisti(card)
                            refreshDati++
                            cardMenu = null
                        },
                        onChiudi = { cardMenu = null }
                    )
                }

                if (mostraDialogoUscita) {
                    DialogoUscita(
                        onConferma = { (context as? Activity)?.finish() },
                        onAnnulla = { mostraDialogoUscita = false }
                    )
                }
            }
        }
    }
}

/** Chiesto da Back sulla Dashboard, radice dell'app (nessuna schermata sopra da chiudere): l'unico
 * modo per uscire, visto che questa TV non mostra da sola nessun dialogo di sistema. Il focus di
 * default va su "Annulla", cosi' un Back ripetuto per sbaglio non chiude l'app. */
@Composable
private fun DialogoUscita(onConferma: () -> Unit, onAnnulla: () -> Unit) {
    val focusAnnulla = remember { FocusRequester() }
    Dialog(onDismissRequest = onAnnulla) {
        LaunchedEffect(Unit) { runCatching { focusAnnulla.requestFocus() } }
        Column(
            modifier = Modifier
                .width(360.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1F232A))
                .border(1.dp, Color(0xFF2E343E), RoundedCornerShape(14.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Uscire dall'app?",
                color = Color(0xFFF2F2F0),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PulsanteDialogo(testo = "Annulla", focusRequester = focusAnnulla, onClick = onAnnulla)
                PulsanteDialogo(testo = "Esci", onClick = onConferma)
            }
        }
    }
}

@Composable
private fun PulsanteDialogo(testo: String, focusRequester: FocusRequester? = null, onClick: () -> Unit) {
    var infocato by remember { mutableStateOf(false) }
    val accento = LocalAccento.current
    Text(
        text = testo,
        color = if (infocato) Color(0xFF14161A) else Color(0xFFF2F2F0),
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { infocato = it.isFocused }
            .clip(RoundedCornerShape(8.dp))
            .background(if (infocato) accento else Color(0xFF262B33))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp)
    )
}

/** La richiesta per il player interno/esterno a partire da una card giocabile (Canale o Film);
 * null per una Serie, che si riproduce dalla sua pagina di Dettaglio. */
private fun richiestaDaCard(card: ContentCard): RichiestaRiproduzione? = when (card) {
    is ContentCard.Canale -> RichiestaRiproduzione(
        titolo = card.title,
        streamUrl = card.streamUrl,
        posterUrl = card.imageUrl,
        chiaveIdentita = card.chiaveIdentita
    )
    is ContentCard.Film -> RichiestaRiproduzione(
        titolo = card.title,
        streamUrl = card.streamUrl,
        tipo = TipoVisto.FILM,
        posterUrl = card.imageUrl,
        chiaveIdentita = card.chiaveIdentita
    )
    is ContentCard.SerieCard -> null
}

private fun ContentCard.Canale.toRichiesta() =
    RichiestaRiproduzione(titolo = title, streamUrl = streamUrl, posterUrl = imageUrl, chiaveIdentita = chiaveIdentita)

/** La richiesta per un Episodio di una Serie gia' caricata, usata dal pulsante "Riprendi"
 * dell'hero in Dashboard (vedi [ContentScreen]/riprendiDaHero) — stessa costruzione della card
 * usata in `DetailScreen.richiestaDi`. */
private fun richiestaDiEpisodio(episodio: Episodio, serie: Serie, immagineCard: String?): RichiestaRiproduzione =
    RichiestaRiproduzione(
        titolo = episodio.title,
        streamUrl = episodio.url,
        tipo = TipoVisto.EPISODIO,
        serie = serie.name,
        chiaveIdentita = episodio.chiaveIdentita,
        posterUrl = episodio.immagine
            ?: navigazioneSerie.stagioneDi(serie, episodio)?.immagine
            ?: serie.poster
            ?: immagineCard
    )

private fun localWebPanelAddress(): String? {
    val interfaces = try {
        NetworkInterface.getNetworkInterfaces()
    } catch (e: SocketException) {
        null
    } ?: return null

    return Collections.list(interfaces)
        .asSequence()
        .mapNotNull { it.inetAddresses }
        .flatMap { Collections.list(it).asSequence() }
        .firstOrNull { it is Inet4Address && !it.isLoopbackAddress }
        ?.hostAddress
        ?.let { "http://$it:${WebPanelServer.PORT}" }
}

@Composable
private fun OnboardingScreen(webPanelAddress: String?) {
    MaterialTheme {
        Surface(color = Color(0xFF14161A)) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF14161A))
                    .padding(48.dp),
                contentAlignment = Alignment.Center
            ) {
                val textColumnWidth = if (webPanelAddress != null) {
                    (maxWidth - maxHeight - ROW_SPACING).coerceAtLeast(MIN_TEXT_COLUMN_WIDTH)
                } else {
                    maxWidth
                }
                val qrSize = (maxWidth - textColumnWidth - ROW_SPACING).coerceIn(0.dp, maxHeight)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ROW_SPACING)
                ) {
                    Column(
                        modifier = Modifier.width(textColumnWidth),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text(
                            text = "PRIMO AVVIO",
                            color = LocalAccento.current,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Nessuna Sorgente configurata",
                            color = Color(0xFFF2F2F0),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Apri il Pannello Web da un telefono o computer collegato alla stessa rete Wi-Fi per aggiungere la tua prima playlist M3U o il tuo account Xtream Codes.",
                            color = Color(0xFF9AA0AA),
                            fontSize = 16.sp
                        )
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .background(Color(0xFF1F232A), RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF262B33), RoundedCornerShape(10.dp))
                                .padding(horizontal = 20.dp, vertical = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "INDIRIZZO PANNELLO WEB",
                                color = Color(0xFF9AA0AA),
                                fontSize = 12.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = webPanelAddress ?: "Indirizzo non disponibile: verifica la connessione Wi-Fi",
                                color = Color(0xFFF2F2F0),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "In attesa di connessione dal Pannello Web…",
                            color = Color(0xFF6D7380),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (webPanelAddress != null) {
                        val qrBitmap = remember(webPanelAddress) {
                            QrCodeGenerator.generate(webPanelAddress, QR_CODE_SIZE_PX)
                        }
                        if (qrBitmap != null) {
                            Box(
                                modifier = Modifier
                                    .size(qrSize)
                                    .background(Color(0xFFF2F2F0), RoundedCornerShape(16.dp))
                                    .padding(18.dp)
                            ) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR code per aprire il Pannello Web",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(device = "id:tv_1080p")
@Composable
private fun OnboardingScreenPreview() {
    OnboardingScreen(webPanelAddress = "http://192.168.1.42:8080")
}
