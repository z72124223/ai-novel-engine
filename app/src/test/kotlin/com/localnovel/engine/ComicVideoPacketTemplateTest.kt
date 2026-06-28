package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComicVideoPacketTemplateTest {
    @Test
    fun cliWritesMetadataOnlyComicVideoPacketTemplateWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-packet-template")
        val out = tempDir.resolve("comic_video_packet_template.json")

        val exit = Cli().run(
            listOf(
                "create-comic-video-packet-template",
                "--packet",
                "comic-video-packet-template-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<ComicVideoPacket>(out)
        assertEquals("comic-video-packet-template-001", packet.packetId)
        assertEquals("comic_video_packet_candidate", packet.packetStatus)
        assertEquals("comic_video_packet_metadata_only", packet.downstreamBoundary)
        assertEquals("comic_video_packet_validation", packet.nextAllowedDownstreamStep)
        assertEquals(1, packet.panelTracks.size)
        assertEquals(1, packet.voiceCast.size)
        assertEquals(1, packet.audioCues.size)
        assertEquals(1, packet.subtitleCues.size)
        assertEquals("", packet.panelTracks.first().localImagePath)
        assertEquals("", packet.panelTracks.first().remoteImageUrl)
        assertEquals("", packet.panelTracks.first().providerJobId)
        assertEquals("", packet.audioCues.first().audioAssetPath)
        assertEquals("", packet.audioCues.first().remoteAudioUrl)
        assertEquals("", packet.audioCues.first().providerJobId)
        assertEquals("", packet.timing.outputVideoPath)
        assertTrue(packet.boundaryChecklist.contains("metadata_only"))
        val templateJson = JsonSupport.json.encodeToString(packet)
        assertFalse(templateJson.contains("provider job", ignoreCase = true))
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
