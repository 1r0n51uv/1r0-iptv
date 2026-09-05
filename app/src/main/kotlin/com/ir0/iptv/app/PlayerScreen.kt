package com.ir0.iptv.app

import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
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

@Composable
fun PlayerScreen(
    richiesta: RichiestaRiproduzione,
    posizioneIniziale: Long = 0,
    onProgresso: (posizioneMs: Long, durataMs: Long) -> Unit = { _, _ -> }
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
            }
        }
    }
}

/** ExoPlayer riporta `C.TIME_UNSET` finché non conosce la durata, e mai per i live. */
private fun ExoPlayer.durataNota(): Long = duration.takeIf { it > 0 } ?: 0L
