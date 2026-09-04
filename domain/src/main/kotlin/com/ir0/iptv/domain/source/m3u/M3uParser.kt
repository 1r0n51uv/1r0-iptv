package com.ir0.iptv.domain.source.m3u

class M3uParser {

    fun parse(content: String): List<M3uEntry> {
        val lines = content.lines()
        val entries = mutableListOf<M3uEntry>()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                val commaIndex = line.lastIndexOf(',')
                val attributes = line.substring(0, commaIndex)
                val title = line.substring(commaIndex + 1).trim()
                val url = lines.getOrNull(i + 1)?.trim().orEmpty()
                entries += M3uEntry(
                    title = title,
                    url = url,
                    tvgId = attribute(attributes, "tvg-id"),
                    tvgLogo = attribute(attributes, "tvg-logo"),
                    groupTitle = attribute(attributes, "group-title")
                )
                i += 2
            } else {
                i += 1
            }
        }
        return entries
    }

    private fun attribute(attributes: String, name: String): String? =
        Regex("""$name="([^"]*)"""").find(attributes)?.groupValues?.get(1)
}
