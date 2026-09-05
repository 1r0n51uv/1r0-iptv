package com.ir0.iptv.domain.dashboard

import com.ir0.iptv.domain.catalog.ContentCard

/**
 * Nelle righe della Dashboard la card di una Serie sta al posto di un suo Episodio (quello da
 * riprendere, o quello appena arrivato). Per l'immagine si prova, in ordine:
 *  1. l'immagine dell'Episodio in gioco;
 *  2. la copertina della sua Stagione;
 *  3. la locandina della Serie (l'`imageUrl` che la card ha gia');
 *  4. niente: la UI mostra un placeholder.
 *
 * Vale solo per le Serie gia' caricate ([ContentCard.SerieCard.Pronta]): una
 * [ContentCard.SerieCard.DaCaricare] non ha ancora Stagioni ed Episodi in memoria, quindi ricade
 * sulla locandina della Serie.
 */
fun ContentCard.SerieCard.conImmagineDiEpisodio(
    urlEpisodi: Collection<String>
): ContentCard.SerieCard {
    if (this !is ContentCard.SerieCard.Pronta || urlEpisodi.isEmpty()) return this
    val stagione = serie.seasons.firstOrNull { st -> st.episodes.any { it.url in urlEpisodi } }
        ?: return this
    val episodio = stagione.episodes.firstOrNull { it.url in urlEpisodi }
    val immagine = episodio?.immagine?.takeIf { it.isNotBlank() }
        ?: stagione.immagine?.takeIf { it.isNotBlank() }
        ?: return this
    return if (immagine == imageUrl) this else copy(imageUrl = immagine)
}
