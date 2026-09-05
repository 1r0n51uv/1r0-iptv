package com.ir0.iptv.app.settings

import android.content.Context
import java.io.File
import org.json.JSONObject

data class Impostazioni(
    val contenutoDiDefault: String? = null,
    val chiaveApiAi: String? = null,
    val chiaveApiSport: String? = null,
    val sportInDashboard: Boolean = false,
    val accento: String? = null
)

class ImpostazioniRepository(context: Context) {
    private val file = File(context.applicationContext.filesDir, "impostazioni.json")

    @Synchronized
    fun leggi(): Impostazioni {
        if (!file.exists()) return Impostazioni()
        return try {
            val oggetto = JSONObject(file.readText())
            Impostazioni(
                contenutoDiDefault = oggetto.optStringOrNull("contenutoDiDefault"),
                chiaveApiAi = oggetto.optStringOrNull("chiaveApiAi"),
                chiaveApiSport = oggetto.optStringOrNull("chiaveApiSport"),
                sportInDashboard = oggetto.optBoolean("sportInDashboard", false),
                accento = oggetto.optStringOrNull("accento")
            )
        } catch (e: Exception) {
            Impostazioni()
        }
    }

    @Synchronized
    fun salva(impostazioni: Impostazioni) {
        file.writeText(
            JSONObject()
                .put("contenutoDiDefault", impostazioni.contenutoDiDefault)
                .put("chiaveApiAi", impostazioni.chiaveApiAi)
                .put("chiaveApiSport", impostazioni.chiaveApiSport)
                .put("sportInDashboard", impostazioni.sportInDashboard)
                .put("accento", impostazioni.accento)
                .toString()
        )
    }
}

private fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) getString(key) else null
