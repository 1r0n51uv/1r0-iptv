package com.ir0.iptv.domain.classification

import com.ir0.iptv.domain.source.m3u.M3uEntry

class ContentClassifier {

    fun classify(entry: M3uEntry): ContentType {
        val groupTitle = entry.groupTitle.orEmpty()
        val filmKeywords = listOf("film", "movie", "vod")
        val serieKeywords = listOf("serie", "series")
        if (filmKeywords.any { groupTitle.contains(it, ignoreCase = true) }) {
            return ContentType.FILM
        }
        if (serieKeywords.any { groupTitle.contains(it, ignoreCase = true) }) {
            return ContentType.SERIE
        }
        return ContentType.CANALE
    }
}
