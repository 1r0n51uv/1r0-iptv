package com.ir0.iptv.domain.classification

import com.ir0.iptv.domain.source.m3u.M3uEntry

class SeriesGrouper(
    private val episodeInfoExtractor: EpisodeInfoExtractor = EpisodeInfoExtractor()
) {

    fun group(entries: List<M3uEntry>): List<Serie> {
        return entries
            .groupBy { it.groupTitle.orEmpty() }
            .map { (seriesName, seriesEntries) ->
                val episodesBySeason = seriesEntries
                    .map { entry -> entry to episodeInfoExtractor.extract(entry.title) }
                    .groupBy({ (_, info) -> info?.season }, { (entry, info) ->
                        Episodio(title = entry.title, url = entry.url, episodeNumber = info?.episode)
                    })
                val seasons = episodesBySeason
                    .toSortedMap(compareBy { it ?: Int.MAX_VALUE })
                    .map { (number, episodes) ->
                        Stagione(number = number, episodes = episodes.sortedBy { it.episodeNumber ?: Int.MAX_VALUE })
                    }
                Serie(name = seriesName, seasons = seasons)
            }
    }
}
