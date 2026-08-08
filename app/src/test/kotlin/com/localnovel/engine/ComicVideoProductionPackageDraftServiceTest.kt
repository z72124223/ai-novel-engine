package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoProductionPackageDraft
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.ImageFacingDeliveryFrameCard
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.service.ComicVideoProductionPackageDraftService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComicVideoProductionPackageDraftServiceTest {
    @Test
    fun createsReviewableProductionPackageDraftFromProofAndVoiceFill() {
        val draft = ComicVideoProductionPackageDraftService().create(
            imageFacingDeliveryProof = proof(),
            voiceTimelineFill = voiceFill("comic-video-package-packet-001"),
            imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            voiceTimelineFillFile = "voice_timeline_fill.json",
            packageId = "comic-video-production-package-001",
            packetId = "comic-video-package-packet-001",
            panelDurationMillis = 2200,
        )

        assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
        assertEquals("comic_video_production_package_candidate", draft.packageStatus)
        assertEquals("comic-video-production-package-001", draft.packageId)
        assertEquals("comic-video-package-packet-001", draft.generatedComicVideoPacketId)
        assertEquals("comic_video_production_package_review", draft.nextAllowedDownstreamStep)
        assertEquals("comic_video_production_package_draft_only", draft.downstreamBoundary)
        assertEquals(2, draft.storyboardProductionTable.size)
        assertEquals(2, draft.shotPrompts.size)
        assertEquals(3, draft.assetPromptCards.size)
        assertEquals(1, draft.voiceStyleCards.size)
        assertEquals(4400, draft.evidence.totalDurationMillis)
        assertTrue(draft.shotPrompts.first().positivePromptDraft.contains("Show Mira at the checkpoint gate."))
        assertTrue(draft.shotPrompts.first().negativePromptDraft.contains("non-public source material"))
        assertTrue(draft.assetPromptCards.any { it.assetCardId == "asset-card-asset-mira" })
        assertTrue(draft.voiceStyleCards.first().sourceCueIds.contains("audio-001"))
        assertSafePackageBoundary(draft)
    }

    @Test
    fun mismatchedVoiceTimelineFillFailsWithoutProducingPromptPackage() {
        val draft = ComicVideoProductionPackageDraftService().create(
            imageFacingDeliveryProof = proof(),
            voiceTimelineFill = voiceFill("other-packet"),
            imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            voiceTimelineFillFile = "voice_timeline_fill.json",
            packageId = "comic-video-production-package-001",
            packetId = "comic-video-package-packet-001",
        )

        assertEquals("FAIL", draft.verdict)
        assertTrue(draft.findings.any { it.ruleId == "VOICE_TIMELINE_FILL_FAILED" })
        assertTrue(draft.storyboardProductionTable.isEmpty())
        assertTrue(draft.shotPrompts.isEmpty())
        assertTrue(draft.assetPromptCards.isEmpty())
    }

    @Test
    fun cliWritesProductionPackageDraftWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-production-package")
        val proofPath = tempDir.resolve("image_facing_delivery_proof.json")
        val fillPath = tempDir.resolve("voice_timeline_fill.json")
        val out = tempDir.resolve("production_package_draft.json")
        writeJson(proofPath, proof())
        writeJson(fillPath, voiceFill("comic-video-package-packet-001"))

        val exit = Cli().run(
            listOf(
                "create-comic-video-production-package-draft",
                "--image-facing-delivery-proof",
                proofPath.toString(),
                "--voice-timeline-fill",
                fillPath.toString(),
                "--package",
                "comic-video-production-package-cli-001",
                "--packet",
                "comic-video-package-packet-001",
                "--panel-duration-ms",
                "2200",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val draft = readJson<ComicVideoProductionPackageDraft>(out)
        assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
        assertEquals("comic-video-production-package-cli-001", draft.packageId)
        assertEquals(2, draft.storyboardProductionTable.size)
        assertEquals(2, draft.shotPrompts.size)
        assertEquals(3, draft.assetPromptCards.size)
        assertEquals(1, draft.voiceStyleCards.size)
        assertSafePackageBoundary(draft)
    }

    private fun proof(): ImageFacingDeliveryProof =
        ImageFacingDeliveryProof(
            verdict = "PASS",
            proofId = "image-facing-delivery-proof-package-001",
            proofStatus = "image_facing_delivery_proof_candidate",
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
            reviewableStoryboardPacketId = "reviewable-storyboard-packet-package-001",
            reviewableStoryboardPacketStatus = "reviewable_storyboard_packet_candidate",
            acceptedScriptPacketId = "accepted-script-packet-package-001",
            scriptPacketDraftFile = "script_packet_draft_package.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            sourceVerdict = "PASS",
            sourceNextAllowedDownstreamStep = "reviewable_storyboard_packet_ready",
            frameDeliveryCards = listOf(
                frame("frame-001", "beat-arrival", "Show Mira at the checkpoint gate."),
                frame("frame-002", "beat-mark", "Show the guard asking to see the visible mark."),
            ),
            downstreamBoundary = "image_facing_delivery_proof_only",
            nextAllowedDownstreamStep = "image_facing_delivery_proof_ready",
        )

    private fun frame(frameId: String, beatReference: String, visualIntent: String): ImageFacingDeliveryFrameCard =
        ImageFacingDeliveryFrameCard(
            frameId = frameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = visualIntent,
            cameraIntent = "Medium shot preserving gate distance.",
            mustShow = listOf("Mira remains visible.", "The visible mark remains readable."),
            doNotShow = listOf("No hidden culprit evidence.", "No media artifact."),
            continuityAnchors = listOf("The guard requires the visible mark before answering."),
            assetCandidateRefs = listOf("asset-mira", "asset-checkpoint-gate", "asset-visible-mark"),
        )

    private fun voiceFill(packetId: String): ComicVideoVoiceTimelineFill =
        ComicVideoVoiceTimelineFill(
            fillId = "voice-timeline-fill-package-001",
            sourceComicVideoPacketId = packetId,
            voiceCast = listOf(
                ComicVideoVoiceCast(
                    voiceId = "voice-mira",
                    characterId = "char-mira",
                    displayName = "Mira",
                    voiceLabel = "local-voice-mira",
                    notes = "Manual voice plan.",
                ),
            ),
            audioCues = listOf(
                audioCue("audio-001", "panel-frame-001", "The gate is close now.", 0, 1800),
                audioCue("audio-002", "panel-frame-002", "I keep the mark visible.", 2200, 2000),
            ),
            subtitleCues = listOf(
                subtitleCue("subtitle-001", "panel-frame-001", "audio-001", "The gate is close now.", 0, 1800),
                subtitleCue("subtitle-002", "panel-frame-002", "audio-002", "I keep the mark visible.", 2200, 4200),
            ),
            timing = ComicVideoTiming(
                totalDurationMillis = 4400,
                frameRate = "24",
                resolution = "1920x1080",
                backgroundPolicy = "panel_track_only",
            ),
            boundaryChecklist = listOf("metadata_only", "manual_voice_plan"),
            notes = listOf("Test voice fill remains local metadata."),
        )

    private fun audioCue(
        cueId: String,
        panelId: String,
        text: String,
        startMillis: Int,
        durationMillis: Int,
    ): ComicVideoAudioCue =
        ComicVideoAudioCue(
            cueId = cueId,
            panelId = panelId,
            voiceId = "voice-mira",
            characterId = "char-mira",
            text = text,
            startMillis = startMillis,
            durationMillis = durationMillis,
        )

    private fun subtitleCue(
        cueId: String,
        panelId: String,
        audioCueId: String,
        text: String,
        startMillis: Int,
        endMillis: Int,
    ): ComicVideoSubtitleCue =
        ComicVideoSubtitleCue(
            cueId = cueId,
            panelId = panelId,
            audioCueId = audioCueId,
            text = text,
            startMillis = startMillis,
            endMillis = endMillis,
        )

    private fun assertSafePackageBoundary(draft: ComicVideoProductionPackageDraft) {
        val text = JsonSupport.json.encodeToString(draft)
        listOf(
            "providerJobId",
            "remoteImageUrl",
            "remoteAudioUrl",
            "audioAssetPath",
            "outputVideoPath",
            "renderedVideoPath",
            "production lock",
            "canon",
        ).forEach { forbidden ->
            assertFalse(text.contains(forbidden, ignoreCase = true), "Forbidden boundary text leaked: $forbidden")
        }
        assertTrue(draft.boundaryChecklist.contains("external_handoff_absent"))
        assertTrue(draft.boundaryChecklist.contains("media_execution_absent"))
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
