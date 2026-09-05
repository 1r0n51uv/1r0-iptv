package com.ir0.iptv.app.dashboard

import android.content.Context
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

/** Gli Episodi già visti passare a un refresh, per Serie: è il metro di confronto del prossimo. */
class NuoviEpisodiRepository(context: Context) {
    private val file = File(context.applicationContext.filesDir, "episodi-noti.json")

    @Synchronized
    fun leggi(): Map<String, Set<String>> {
        if (!file.exists()) return emptyMap()
        return try {
            val oggetto = JSONObject(file.readText())
            oggetto.keys().asSequence().associateWith { chiave ->
                val array = oggetto.getJSONArray(chiave)
                (0 until array.length()).map { array.getString(it) }.toSet()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @Synchronized
    fun salva(impronte: Map<String, Set<String>>) {
        val oggetto = JSONObject()
        impronte.forEach { (chiave, episodi) -> oggetto.put(chiave, JSONArray(episodi.toList())) }
        file.writeText(oggetto.toString())
    }
}
