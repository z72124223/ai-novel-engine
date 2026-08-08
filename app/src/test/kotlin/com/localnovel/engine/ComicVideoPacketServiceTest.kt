package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ImageFacingDeliveryFrameCard
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.service.ComicVideoPacketService
import com.localnovel.engine.service.ComicVideoPacketValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ComicVideoPacketServiceTest {
    @Test
    fun createsMetadataOnlyComicVideoPacketFromImageFacingProof() {
        val packet = ComicVideoPacketService().create(
            imageFacingDeliveryProof = proof(),
            imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            packetId = "comic-video-packet-001",
            panelDurationMillis = 2500,
        )

        assertEquals("comic-video-packet-001", packet.packetId)
        assertEquals("comic_video_packet_candidate", packet.packetStatus)
        assertEquals("image_facing_delivery_proof.json", packet.imageFacingDeliveryProofFile)
        assertEquals("reviewable_storyboard_packet.json", packet.reviewableStoryboardPacketFile)
        assertEquals("reviewable-storyboard-packet-001", packet.sourceReviewableStoryboardPacketId)
        assertEquals("image-facing-delivery-proof-001", packet.sourceImageFacingDeliveryProofId)
        assertEquals("accepted-script-packet-001", packet.acceptedScriptPacketId)
        assertEquals("script_packet_draft.json", packet.scriptPacketDraftFile)
        assertEquals("a".repeat(64), packet.scriptPacketDraftContentSha256)
        assertEquals("PASS", packet.sourceVerdicts.reviewableStoryboardPacketVerdict)
        assertEquals("PASS", packet.sourceVerdicts.imageFacingDeliveryProofVerdict)
        assertEquals("image_facing_delivery_proof_ready", packet.sourceVerdicts.imageFacingDeliveryProofNextAllowedDownstreamStep)
        assertEquals(2, packet.panelTracks.size)
        assertEquals("panel-frame-001", packet.panelTracks.first().panelId)
        assertEquals("frame-001", packet.panelTracks.first().sourceFrameId)
        assertEquals("Show the checkpoint gate and visible mark.", packet.panelTracks.first().visualIntent)
        assertEquals(2500, packet.panelTracks.first().displayDurationMillis)
        assertEquals(5000, packet.timing.totalDurationMillis)
        assertTrue(packet.voiceCast.isEmpty())
        assertTrue(packet.audioCues.isEmpty())
        assertTrue(packet.subtitleCues.isEmpty())
        assertEquals("comic_video_packet_metadata_only", packet.downstreamBoundary)
        assertEquals("comic_video_packet_validation", packet.nextAllowedDownstreamStep)
        assertEquals("", packet.panelTracks.first().localImagePath)
        assertEquals("", packet.panelTracks.first().remoteImageUrl)
        assertEquals("", packet.timing.outputVideoPath)
    }

    @Test
    fun autoDraftStillRequiresVoiceAudioAndSubtitleFillBeforeValidationPasses() {
        val packet = ComicVideoPacketService().create(
            imageFacingDeliveryProof = proof(),
            imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
        )
        val report = ComicVideoPacketValidationService().validate(
            packet = packet,
            packetFile = "comic_video_packet.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "VOICE_CAST_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "AUDIO_CUES_MISSING" })
        assertTrue(report.findings.any { it.ruleId == "SUBTITLE_CUES_MISSING" })
        assertEquals(2, report.evidence.panelTrackCount)
        assertEquals(0, report.evidence.generatedMediaFieldCount)
        assertEquals(0, report.evidence.remoteOrProviderFieldCount)
    }

    @Test
    fun nonPassOrNotReadyImageFacingProofIsRejected() {
        assertFailsWith<IllegalArgumentException> {
            ComicVideoPacketService().create(
                imageFacingDeliveryProof = proof(verdict = "FAIL"),
                imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            )
        }
        assertFailsWith<IllegalArgumentException> {
            ComicVideoPacketService().create(
                imageFacingDeliveryProof = proof(nextAllowedDownstreamStep = ""),
                imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            )
        }
        assertFailsWith<IllegalArgumentException> {
            ComicVideoPacketService().create(
                imageFacingDeliveryProof = proof(frameDeliveryCards = emptyList()),
                imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            )
        }
    }

    @Test
    fun cliWritesComicVideoPacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-packet-service")
        val proofPath = tempDir.resolve("image_facing_delivery_proof.json")
        val out = tempDir.resolve("comic_video_packet.json")
        writeJson(proofPath, proof())

        val exit = Cli().run(
            listOf(
                "create-comic-video-packet",
                "--image-facing-delivery-proof",
                proofPath.toString(),
                "--packet",
                "comic-video-packet-cli-001",
                "--panel-duration-ms",
                "2400",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<ComicVideoPacket>(out)
        assertEquals("comic-video-packet-cli-001", packet.packetId)
        assertEquals(2, packet.panelTracks.size)
        assertEquals(4800, packet.timing.totalDurationMillis)
        assertEquals("image_facing_delivery_proof.json", packet.imageFacingDeliveryProofFile.substringAfterLast('\\').substringAfterLast('/'))
        assertTrue(packet.voiceCast.isEmpty())
        assertTrue(packet.audioCues.isEmpty())
        assertTrue(packet.subtitleCues.isEmpty())
    }

    private fun proof(
        verdict: String = "PASS",
        proofStatus: String = "image_facing_delivery_proof_candidate",
        nextAllowedDownstreamStep: String = "image_facing_delivery_proof_ready",
        downstreamBoundary: String = "image_facing_delivery_proof_only",
        frameDeliveryCards: List<ImageFacingDeliveryFrameCard> = listOf(frame(), frame("frame-002", "beat-check")),
    ): ImageFacingDeliveryProof =
        ImageFacingDeliveryProof(
            verdict = verdict,
            proofId = "image-facing-delivery-proof-001",
            proofStatus = proofStatus,
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
            reviewableStoryboardPacketId = "reviewable-storyboard-packet-001",
            reviewableStoryboardPacketStatus = "reviewable_storyboard_packet_candidate",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            sourceVerdict = "PASS",
            sourceNextAllowedDownstreamStep = "reviewable_storyboard_packet_ready",
            frameDeliveryCards = frameDeliveryCards,
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun frame(
        frameId: String = "frame-001",
        beatReference: String = "beat-arrival",
    ): ImageFacingDeliveryFrameCard =
        ImageFacingDeliveryFrameCard(
            frameId = frameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = "Show the checkpoint gate and visible mark.",
            cameraIntent = "Medium shot clarifying gate distance.",
            mustShow = listOf("Visible mark remains on screen."),
            doNotShow = listOf("Hidden culprit evidence."),
            continuityAnchors = listOf("The gate guard asks for the visible mark."),
            assetCandidateRefs = listOf("asset-visible-mark"),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
