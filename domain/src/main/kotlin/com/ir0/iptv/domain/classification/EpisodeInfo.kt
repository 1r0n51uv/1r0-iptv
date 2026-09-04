package com.ir0.iptv.domain.classification

data class EpisodeInfo(
    val seriesName: String,
    val season: Int,
    val episode: Int,
    val episodeTitle: String? = null
)
