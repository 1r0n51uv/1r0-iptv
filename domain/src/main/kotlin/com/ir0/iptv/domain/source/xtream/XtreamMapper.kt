package com.ir0.iptv.domain.source.xtream

import com.ir0.iptv.domain.classification.Episodio
import com.ir0.iptv.domain.classification.Serie
import com.ir0.iptv.domain.classification.Stagione
import com.ir0.iptv.domain.source.m3u.M3uEntry

class XtreamMapper {

    fun toChannel(dto: XtreamLiveStreamDto, connection: XtreamConnection): M3uEntry {
        val url = "http://${connection.host}:${connection.port}/live/" +
            "${connection.username}/${connection.password}/${dto.streamId}.m3u8"
        return M3uEntry(
            title = dto.name,
            url = url,
            tvgId = dto.epgChannelId,
            tvgLogo = dto.streamIcon,
            groupTitle = dto.categoryName
        )
    }

    fun toMovie(dto: XtreamVodStreamDto, connection: XtreamConnection): XtreamMovie {
        val url = "http://${connection.host}:${connection.port}/movie/" +
            "${connection.username}/${connection.password}/${dto.streamId}.${dto.containerExtension}"
        return XtreamMovie(
            title = dto.name,
            url = url,
            poster = dto.streamIcon,
            plot = dto.plot,
            categoryName = dto.categoryName
        )
    }

    fun toSerie(dto: XtreamSeriesInfoDto, connection: XtreamConnection): Serie {
        val seasons = dto.episodesBySeason
            .toSortedMap()
            .map { (seasonNumber, episodes) ->
                Stagione(
                    number = seasonNumber,
                    episodes = episodes
                        .sortedBy { it.episodeNum }
                        .map { episode ->
                            val url = "http://${connection.host}:${connection.port}/series/" +
                                "${connection.username}/${connection.password}/${episode.id}.${episode.containerExtension}"
                            Episodio(title = episode.title, url = url, episodeNumber = episode.episodeNum)
                        }
                )
            }
        return Serie(name = dto.seriesName, seasons = seasons, poster = dto.cover)
    }
}
