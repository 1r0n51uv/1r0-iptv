package com.ir0.iptv.domain.classification

class EpisodeInfoExtractor {

    private val pattern = Regex("""^(.*?)\s*S(\d{1,2})E(\d{1,3})\s*(.*)$""", RegexOption.IGNORE_CASE)

    fun extract(title: String): EpisodeInfo? {
        val match = pattern.find(title) ?: return null
        val (seriesName, season, episode, rest) = match.destructured
        return EpisodeInfo(
            seriesName = seriesName.trim(),
            season = season.toInt(),
            episode = episode.toInt(),
            episodeTitle = rest.trim().ifEmpty { null }
        )
    }
}
