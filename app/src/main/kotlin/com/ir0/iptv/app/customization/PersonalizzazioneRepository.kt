package com.ir0.iptv.app.customization

import android.content.Context
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.classification.ContentType
import com.ir0.iptv.domain.customization.ContentCustomization
import java.io.File
import org.json.JSONObject

class PersonalizzazioneRepository(
    context: Context,
    private val elencoPreferiti: ElencoPreferiti = ElencoPreferiti()
) {
    private val file = File(context.applicationContext.filesDir, "personalizzazioni.json")

    @Synchronized
    fun elenco(): Map<String, ContentCustomization> {
        if (!file.exists()) return emptyMap()
        return try {
            val oggetto = JSONObject(file.readText())
            oggetto.keys().asSequence().associateWith { chiave ->
                oggetto.getJSONObject(chiave).toPersonalizzazione()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @Synchronized
    fun preferito(card: ContentCard): Boolean = elencoPreferiti.preferito(elenco(), card)

    @Synchronized
    fun cambiaPreferito(card: ContentCard): Boolean {
        val aggiornate = elencoPreferiti.cambiaPreferito(elenco(), card)
        salva(aggiornate)
        return elencoPreferiti.preferito(aggiornate, card)
    }

    @Synchronized
    fun preferiti(catalogo: ContentCatalog): List<ContentCard> = elencoPreferiti.preferiti(catalogo, elenco())

    private fun salva(personalizzazioni: Map<String, ContentCustomization>) {
        val oggetto = JSONObject()
        personalizzazioni.forEach { (chiave, personalizzazione) ->
            oggetto.put(chiave, personalizzazione.toJson())
        }
        file.writeText(oggetto.toString())
    }
}

private fun ContentCustomization.toJson(): JSONObject = JSONObject()
    .put("hidden", hidden)
    .put("favorite", favorite)
    .put("manualType", manualType?.name)

private fun JSONObject.toPersonalizzazione(): ContentCustomization = ContentCustomization(
    hidden = optBoolean("hidden", false),
    favorite = optBoolean("favorite", false),
    manualType = if (has("manualType") && !isNull("manualType")) {
        ContentType.valueOf(getString("manualType"))
    } else {
        null
    }
)
