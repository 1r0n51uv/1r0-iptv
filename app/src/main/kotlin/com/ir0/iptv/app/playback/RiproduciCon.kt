package com.ir0.iptv.app.playback

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/** Manda lo stream a un player esterno (VLC, MX Player…) invece che al player interno. */
object RiproduciCon {

    fun avvia(context: Context, richiesta: RichiestaRiproduzione) {
        val riproduzione = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(Uri.parse(richiesta.streamUrl), "video/*")
            putExtra("title", richiesta.titolo)
        }
        val scelta = Intent.createChooser(riproduzione, "Riproduci con")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(scelta)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Nessun player esterno installato", Toast.LENGTH_SHORT).show()
        }
    }
}
