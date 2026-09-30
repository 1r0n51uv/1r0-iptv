package com.ir0.iptv.app.customization

import android.content.Context
import com.ir0.iptv.domain.catalog.ContentCard
import com.ir0.iptv.domain.catalog.ContentCatalog
import com.ir0.iptv.domain.catalog.ElencoPreferiti
import com.ir0.iptv.domain.catalog.RiconciliazionePersonalizzazioni
import com.ir0.iptv.domain.catalog.migraChiaveIdentita
import com.ir0.iptv.domain.classification.ContentType
import com.ir0.iptv.domain.customization.ContentCustomization
import java.io.File
import org.json.JSONObject

class PersonalizzazioneRepository(
    context: Context,
    private val elencoPreferiti: ElencoPreferiti = ElencoPreferiti(),
    private val riconciliazione: RiconciliazionePersonalizzazioni = RiconciliazionePersonalizzazioni()
) {
    private val file = File(context.applicationContext.filesDir, "personalizzazioni.json")

    @Synchronized
    fun elenco(): Map<String, ContentCustomization> {
        if (!file.exists()) return emptyMap()
        return try {
            val oggetto = JSONObject(file.readText())
            // Riscrive al volo una vecchia Chiave (l'URL completo dello stream Xtream, che
            // cambia se il provider ruota host/credenziali) nella nuova forma stabile: vedi
            // ChiaveIdentitaXtream.
            oggetto.keys().asSequence().associate { chiave ->
                migraChiaveIdentita(chiave) to oggetto.getJSONObject(chiave).toPersonalizzazione()
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

    /** Riaggancia al catalogo appena sincronizzato le personalizzazioni la cui Chiave e' cambiata
     * (es. la Sorgente e' passata da Xtream a M3U dello stesso provider o viceversa - Fase 13),
     * abbinandole per titolo quando non e' ambiguo. Va chiamata dopo ogni sincronizzazione
     * riuscita: se non cambia nulla non riscrive il file. */
    @Synchronized
    fun riconcilia(catalogo: ContentCatalog) {
        val correnti = elenco()
        val riconciliate = riconciliazione.riconcilia(correnti, catalogo)
        if (riconciliate != correnti) salva(riconciliate)
    }

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
    .put("titolo", titolo)

private fun JSONObject.toPersonalizzazione(): ContentCustomization = ContentCustomization(
    hidden = optBoolean("hidden", false),
    favorite = optBoolean("favorite", false),
    manualType = if (has("manualType") && !isNull("manualType")) {
        ContentType.valueOf(getString("manualType"))
    } else {
        null
    },
    titolo = if (has("titolo") && !isNull("titolo")) getString("titolo") else null
)
