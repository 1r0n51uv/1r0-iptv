package com.ir0.iptv.domain.source.xtream

data class XtreamLiveStreamDto(
    val name: String,
    val streamId: Int,
    val streamIcon: String?,
    val epgChannelId: String?,
    val categoryName: String?
)

data class XtreamVodStreamDto(
    val name: String,
    val streamId: Int,
    val streamIcon: String?,
    val plot: String?,
    val categoryName: String?,
    val containerExtension: String
)

data class XtreamMovie(
    val title: String,
    val url: String,
    val poster: String?,
    val plot: String?,
    val categoryName: String?
)

data class XtreamEpisodeDto(
    val id: Int,
    val episodeNum: Int,
    val title: String,
    val containerExtension: String,
    val immagine: String? = null
)

data class XtreamSeriesInfoDto(
    val seriesName: String,
    val episodesBySeason: Map<Int, List<XtreamEpisodeDto>>,
    val cover: String? = null,
    val plot: String? = null
)
