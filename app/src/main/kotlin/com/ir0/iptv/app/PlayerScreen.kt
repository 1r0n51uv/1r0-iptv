package com.ir0.iptv.app

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.ir0.iptv.app.playback.RichiestaRiproduzione
import com.ir0.iptv.app.theme.LocalAccento
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val INTERVALLO_SALVATAGGIO_MS = 10_000L
private const val RITENTATIVI_MASSIMI_ERRORE = 5
private const val ATTESA_RITENTATIVO_MS = 1_000L
private const val DURATA_TITOLO_MS = 5_000L
private const val PASSO_SALTO_MS = 10_000L
private const val DURATA_INDICATORE_SALTO_MS = 900L
private const val NASCONDI_CONTROLLI_MS = 4_000L

@Composable
fun PlayerScreen(
    richiesta: RichiestaRiproduzione,
    posizioneIniziale: Long = 0,
    /** Se c'e' un Episodio dopo questo in coda: mostra il pulsante "Prossimo episodio". */
    haProssimoEpisodio: Boolean = false,
    onProgresso: (posizioneMs: Long, durataMs: Long) -> Unit = { _, _ -> },
    /** Chiamato sia a fine riproduzione naturale sia dal comando "Prossimo episodio" nei
     * controlli: chi ascolta fa partire l'Episodio successivo in coda. */
    onProssimoEpisodio: () -> Unit = {},
    /** Chiamato quando l'app va in background mentre questo Player e' aperto: la riproduzione si
     * ferma esplicitamente (oggi Android non lo garantisce da solo), e chi ascolta puo' ricordarsi
     * dove riprendere alla riapertura invece di tornare sempre alla Dashboard. */
    onVaInBackground: (posizioneMs: Long) -> Unit = {}
) {
    val context = LocalContext.current
    val scopeRitentativo = rememberCoroutineScope()
    val exoPlayer = remember(richiesta.streamUrl) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(richiesta.streamUrl))
            if (posizioneIniziale > 0) seekTo(posizioneIniziale)
            prepare()
            playWhenReady = true
        }
    }

    // Un Canale e' diretta: nessuna posizione da riprendere (ADR 0004), quindi ne' salvataggio
    // della posizione ne' seek/scrubbing nei controlli.
    val tracciaProgresso = richiesta.tipo != null

    var mostraTitolo by remember(richiesta.streamUrl) { mutableStateOf(true) }
    LaunchedEffect(richiesta.streamUrl) {
        delay(DURATA_TITOLO_MS)
        mostraTitolo = false
    }

    // Stato dei controlli custom (ADR 0007): sinistra/destra saltano sempre avanti/indietro,
    // un tasto dedicato (su' + centro) porta al pulsante "Prossimo episodio".
    var inRiproduzione by remember(richiesta.streamUrl) { mutableStateOf(true) }
    var mostraControlli by remember(richiesta.streamUrl) { mutableStateOf(false) }
    var focusSulProssimo by remember(richiesta.streamUrl) { mutableStateOf(false) }
    var indicatoreSalto by remember(richiesta.streamUrl) { mutableStateOf<String?>(null) }
    var posizioneMostrata by remember(richiesta.streamUrl) { mutableStateOf(posizioneIniziale) }
    var contatoreInterazioni by remember(richiesta.streamUrl) { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    LaunchedEffect(exoPlayer, mostraControlli) {
        while (mostraControlli) {
            posizioneMostrata = exoPlayer.currentPosition
            delay(500)
        }
    }

    // I controlli restano a schermo mentre in pausa; altrimenti spariscono da soli.
    LaunchedEffect(contatoreInterazioni, inRiproduzione, richiesta.streamUrl) {
        if (!mostraControlli || !inRiproduzione) return@LaunchedEffect
        delay(NASCONDI_CONTROLLI_MS)
        mostraControlli = false
        focusSulProssimo = false
    }

    LaunchedEffect(indicatoreSalto) {
        if (indicatoreSalto == null) return@LaunchedEffect
        delay(DURATA_INDICATORE_SALTO_MS)
        indicatoreSalto = null
    }

    fun segnalaInterazione() {
        mostraControlli = true
        contatoreInterazioni++
    }

    fun salta(deltaMs: Long, etichetta: String) {
        if (!tracciaProgresso) return
        val durata = exoPlayer.durataNota()
        val limite = if (durata > 0) durata else Long.MAX_VALUE
        exoPlayer.seekTo((exoPlayer.currentPosition + deltaMs).coerceIn(0, limite))
        indicatoreSalto = etichetta
        focusSulProssimo = false
        segnalaInterazione()
    }

    fun alternaPausa() {
        exoPlayer.playWhenReady = !exoPlayer.isPlaying
        segnalaInterazione()
    }

    if (tracciaProgresso) {
        LaunchedEffect(exoPlayer) {
            while (true) {
                delay(INTERVALLO_SALVATAGGIO_MS)
                onProgresso(exoPlayer.currentPosition, exoPlayer.durataNota())
            }
        }
    }

    DisposableEffect(exoPlayer) {
        var ritentativi = 0
        var terminataNotificata = false
        // Le Sorgenti IPTV cadono spesso per pochi secondi: senza un retry esplicito
        // ExoPlayer resta fermo in STATE_IDLE dopo un errore invece di riprendere da solo.
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                if (ritentativi >= RITENTATIVI_MASSIMI_ERRORE) return
                ritentativi++
                scopeRitentativo.launch {
                    delay(ATTESA_RITENTATIVO_MS)
                    exoPlayer.prepare()
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                inRiproduzione = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) ritentativi = 0
                // STATE_ENDED puo' arrivare piu' volte (es. un seek dopo la fine): il primo basta.
                if (playbackState == Player.STATE_ENDED && tracciaProgresso && !terminataNotificata) {
                    terminataNotificata = true
                    onProssimoEpisodio()
                }
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            if (tracciaProgresso) {
                onProgresso(exoPlayer.currentPosition, exoPlayer.durataNota())
            }
            exoPlayer.release()
        }
    }

    // L'Activity va in background (Home, multitasking, schermo spento) senza che la
    // composizione si smonti: senza questo la riproduzione continuerebbe non vista finche'
    // Android non decide da solo di liberare risorse, il che non e' garantito ne' immediato.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(exoPlayer, lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                exoPlayer.playWhenReady = false
                if (tracciaProgresso) onProgresso(exoPlayer.currentPosition, exoPlayer.durataNota())
                onVaInBackground(exoPlayer.currentPosition)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    MaterialTheme {
        Surface(color = Color.Black) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { evento ->
                        if (evento.type != KeyEventType.KeyDown) return@onKeyEvent false
                        when (evento.key) {
                            Key.DirectionLeft -> {
                                salta(-PASSO_SALTO_MS, "« 10s")
                                true
                            }

                            Key.DirectionRight -> {
                                salta(PASSO_SALTO_MS, "10s »")
                                true
                            }

                            Key.DirectionCenter, Key.Enter, Key.NumPadEnter -> {
                                if (focusSulProssimo && haProssimoEpisodio) {
                                    onProssimoEpisodio()
                                } else {
                                    alternaPausa()
                                }
                                true
                            }

                            Key.DirectionUp -> {
                                if (haProssimoEpisodio) {
                                    focusSulProssimo = true
                                    segnalaInterazione()
                                }
                                true
                            }

                            Key.DirectionDown -> {
                                if (focusSulProssimo) {
                                    focusSulProssimo = false
                                    segnalaInterazione()
                                }
                                true
                            }

                            else -> false
                        }
                    }
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    // Senza questo, il PlayerView (creato una sola volta) resta agganciato al
                    // primo ExoPlayer: al cambio episodio la vista continua a mostrare l'ultimo
                    // frame del player precedente, gia' rilasciato.
                    update = { vista -> vista.player = exoPlayer }
                )
                if (mostraTitolo) {
                    Text(
                        text = richiesta.titolo,
                        color = Color(0xFFF2F2F0),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(24.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x99000000))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }
                if (indicatoreSalto != null) {
                    Text(
                        text = indicatoreSalto.orEmpty(),
                        color = Color(0xFFF2F2F0),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x99000000))
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    )
                }
                if (mostraControlli && tracciaProgresso) {
                    BarraControlli(
                        inRiproduzione = inRiproduzione,
                        posizioneMs = posizioneMostrata,
                        durataMs = exoPlayer.durataNota(),
                        haProssimoEpisodio = haProssimoEpisodio,
                        focusSulProssimo = focusSulProssimo,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
private fun BarraControlli(
    inRiproduzione: Boolean,
    posizioneMs: Long,
    durataMs: Long,
    haProssimoEpisodio: Boolean,
    focusSulProssimo: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(listOf(Color(0x00000000), Color(0xCC000000)))
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        BarraAvanzamento(posizioneMs = posizioneMs, durataMs = durataMs)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (inRiproduzione) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = Color(0xFFF2F2F0)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "${formattaTempo(posizioneMs)} / ${formattaTempo(durataMs)}",
                color = Color(0xFFF2F2F0),
                fontSize = 14.sp
            )
            if (haProssimoEpisodio) {
                Spacer(modifier = Modifier.width(24.dp))
                PulsanteProssimoEpisodio(evidenziato = focusSulProssimo)
            }
        }
    }
}

@Composable
private fun PulsanteProssimoEpisodio(evidenziato: Boolean) {
    val sfondo = if (evidenziato) LocalAccento.current else Color(0xFF262B33)
    val colore = if (evidenziato) Color(0xFF14161A) else Color(0xFFF2F2F0)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(sfondo)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(imageVector = Icons.Filled.SkipNext, contentDescription = null, tint = colore)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "Prossimo episodio", color = colore, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BarraAvanzamento(posizioneMs: Long, durataMs: Long, modifier: Modifier = Modifier) {
    val percentuale = if (durataMs > 0) (posizioneMs.toFloat() / durataMs).coerceIn(0f, 1f) else 0f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(CircleShape)
            .background(Color(0x40FFFFFF))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentuale)
                .fillMaxSize()
                .background(LocalAccento.current)
        )
    }
}

private fun formattaTempo(ms: Long): String {
    val secondiTotali = (ms / 1000).coerceAtLeast(0)
    val minuti = secondiTotali / 60
    val secondi = secondiTotali % 60
    return "%d:%02d".format(minuti, secondi)
}

/** ExoPlayer riporta `C.TIME_UNSET` finché non conosce la durata, e mai per i live. */
private fun ExoPlayer.durataNota(): Long = duration.takeIf { it > 0 } ?: 0L
