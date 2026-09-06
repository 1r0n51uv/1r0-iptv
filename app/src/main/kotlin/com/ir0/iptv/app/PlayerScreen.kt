package com.ir0.iptv.app

import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val INTERVALLO_SALVATAGGIO_MS = 10_000L
private const val RITENTATIVI_MASSIMI_ERRORE = 5
private const val ATTESA_RITENTATIVO_MS = 1_000L
private const val DURATA_TITOLO_MS = 5_000L

@Composable
fun PlayerScreen(
    richiesta: RichiestaRiproduzione,
    posizioneIniziale: Long = 0,
    onProgresso: (posizioneMs: Long, durataMs: Long) -> Unit = { _, _ -> },
    /** Chiamato quando la riproduzione arriva in fondo: chi ascolta fa partire il prossimo
     * Episodio, se c'e'. Non scatta su stop manuale o su errore. */
    onRiproduzioneTerminata: () -> Unit = {},
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

    val tracciaProgresso = richiesta.tipo != null

    var mostraTitolo by remember(richiesta.streamUrl) { mutableStateOf(true) }
    LaunchedEffect(richiesta.streamUrl) {
        delay(DURATA_TITOLO_MS)
        mostraTitolo = false
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

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) ritentativi = 0
                // STATE_ENDED puo' arrivare piu' volte (es. un seek dopo la fine): il primo basta.
                if (playbackState == Player.STATE_ENDED && tracciaProgresso && !terminataNotificata) {
                    terminataNotificata = true
                    onRiproduzioneTerminata()
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
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = {
                        PlayerView(context).apply {
                            player = exoPlayer
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    }
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
            }
        }
    }
}

/** ExoPlayer riporta `C.TIME_UNSET` finché non conosce la durata, e mai per i live. */
private fun ExoPlayer.durataNota(): Long = duration.takeIf { it > 0 } ?: 0L
