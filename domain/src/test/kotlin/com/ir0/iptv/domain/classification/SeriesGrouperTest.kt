package com.ir0.iptv.domain.classification

import com.ir0.iptv.domain.source.m3u.M3uEntry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SeriesGrouperTest {

    private fun entry(title: String, groupTitle: String) = M3uEntry(
        title = title,
        url = "http://example.com/${title.replace(" ", "-")}.m3u8",
        groupTitle = groupTitle
    )

    @Test
    fun `groups episodes of the same series and season under one Serie`() {
        val entries = listOf(
            entry("The Bear S01E01", groupTitle = "The Bear"),
            entry("The Bear S01E02", groupTitle = "The Bear")
        )

        val series = SeriesGrouper().group(entries)

        assertEquals(1, series.size)
        assertEquals("The Bear", series[0].name)
        assertEquals(1, series[0].seasons.size)
        assertEquals(1, series[0].seasons[0].number)
        assertEquals(
            listOf("The Bear S01E01", "The Bear S01E02"),
            series[0].seasons[0].episodes.map { it.title }
        )
    }

    @Test
    fun `separates episodes into distinct seasons by extracted season number`() {
        val entries = listOf(
            entry("The Bear S01E01", groupTitle = "The Bear"),
            entry("The Bear S02E01", groupTitle = "The Bear")
        )

        val series = SeriesGrouper().group(entries)

        assertEquals(1, series.size)
        val seasonNumbers = series[0].seasons.map { it.number }
        assertEquals(listOf(1, 2), seasonNumbers)
    }

    @Test
    fun `episodes with no recognizable SxxExx code fall into an unnumbered season, sorted last`() {
        val entries = listOf(
            entry("The Bear S01E01", groupTitle = "The Bear"),
            entry("Speciale dietro le quinte", groupTitle = "The Bear")
        )

        val series = SeriesGrouper().group(entries)

        val seasonNumbers = series[0].seasons.map { it.number }
        assertEquals(listOf(1, null), seasonNumbers)
        assertEquals(
            listOf("Speciale dietro le quinte"),
            series[0].seasons.last { it.number == null }.episodes.map { it.title }
        )
    }

    @Test
    fun `episodes within a season are sorted by episode number regardless of source order`() {
        val entries = listOf(
            entry("The Bear S01E02", groupTitle = "The Bear"),
            entry("The Bear S01E01", groupTitle = "The Bear")
        )

        val series = SeriesGrouper().group(entries)

        assertEquals(
            listOf("The Bear S01E01", "The Bear S01E02"),
            series[0].seasons.single().episodes.map { it.title }
        )
    }
}
