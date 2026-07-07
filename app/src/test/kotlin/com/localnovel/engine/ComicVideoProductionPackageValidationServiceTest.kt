package com.localnovel.engine

import com.localnovel.engine.model.ComicVideoAudioCue
import com.localnovel.engine.model.ComicVideoProductionPackageDraft
import com.localnovel.engine.model.ComicVideoProductionPackageValidationReport
import com.localnovel.engine.model.ComicVideoSubtitleCue
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ComicVideoVoiceCast
import com.localnovel.engine.model.ComicVideoVoiceTimelineFill
import com.localnovel.engine.model.ImageFacingDeliveryFrameCard
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.service.ComicVideoProductionPackageDraftService
import com.localnovel.engine.service.ComicVideoProductionPackageValidationService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComicVideoProductionPackageValidationServiceTest {
    @Test
    fun validatesCleanProductionPackageDraftForReview() {
        val draft = productionPackageDraft()
        val report = ComicVideoProductionPackageValidationService().validate(
            draft = draft,
            draftFile = "production_package_draft.json",
            validationId = "production-package-validation-001",
        )

        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("production-package-validation-001", report.validationId)
        assertEquals("comic_video_production_package_validation_only", report.downstreamBoundary)
        assertEquals("comic_video_production_package_validated_for_review", report.nextAllowedDownstreamStep)
        assertEquals(2, report.evidence.storyboardRowCount)
        assertEquals(2, report.evidence.shotPromptCount)
        assertEquals(3, report.evidence.assetCardCount)
        assertEquals(1, report.evidence.voiceStyleCardCount)
        assertEquals(0, report.evidence.crossReferenceMismatchCount)
        assertEquals(0, report.evidence.promptSafetyFindingCount)
        assertEquals(4400, report.evidence.totalDurationMillis)
        assertEquals(2, report.storyboardProductionTable.size)
        assertEquals(2, report.shotPrompts.size)
        assertSafeValidationBoundary(report)
    }

    @Test
    fun missingAssetCardReferenceFailsValidation() {
        val draft = productionPackageDraft()
        val broken = draft.copy(
            shotPrompts = listOf(
                draft.shotPrompts.first().copy(referenceAssetCardIds = listOf("asset-card-missing")),
            ) + draft.shotPrompts.drop(1),
        )
        val report = ComicVideoProductionPackageValidationService().validate(
            draft = broken,
            draftFile = "production_package_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "SHOT_PROMPT_ASSET_CARD_REF_MISSING" })
        assertTrue(report.evidence.crossReferenceMismatchCount > 0)
        assertTrue(report.shotPrompts.isEmpty())
    }

    @Test
    fun unsafePromptTextFailsValidation() {
        val draft = productionPackageDraft()
        val unsafe = draft.copy(
            shotPrompts = listOf(
                draft.shotPrompts.first().copy(
                    positivePromptDraft = "Prompt submitted to https://example.invalid with provider job and hidden truth.",
                ),
            ) + draft.shotPrompts.drop(1),
        )
        val report = ComicVideoProductionPackageValidationService().validate(
            draft = unsafe,
            draftFile = "production_package_draft.json",
        )

        assertEquals("FAIL", report.verdict)
        assertTrue(report.findings.any { it.ruleId == "REMOTE_OR_PROVIDER_TEXT_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(report.findings.any { it.ruleId == "AUTHOR_ONLY_PROMPT_TEXT_FORBIDDEN" })
        assertTrue(report.evidence.promptSafetyFindingCount >= 3)
    }

    @Test
    fun cliWritesProductionPackageValidationReportWithoutDatabase() {
        val tempDir = Files.createTempDirectory("comic-video-production-package-validation")
        val draftPath = tempDir.resolve("production_package_draft.json")
        val out = tempDir.resolve("production_package_validation.json")
        writeJson(draftPath, productionPackageDraft())

        val exit = Cli().run(
            listOf(
                "validate-comic-video-production-package",
                "--production-package-draft",
                draftPath.toString(),
                "--validation",
                "production-package-validation-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val report = readJson<ComicVideoProductionPackageValidationReport>(out)
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("production-package-validation-cli-001", report.validationId)
        assertEquals("comic_video_production_package_validated_for_review", report.nextAllowedDownstreamStep)
        assertEquals(2, report.evidence.storyboardRowCount)
        assertEquals(2, report.evidence.shotPromptCount)
        assertEquals(3, report.evidence.assetCardCount)
        assertEquals(1, report.evidence.voiceStyleCardCount)
        assertSafeValidationBoundary(report)
    }

    private fun productionPackageDraft(): ComicVideoProductionPackageDraft =
        ComicVideoProductionPackageDraftService().create(
            imageFacingDeliveryProof = proof(),
            voiceTimelineFill = voiceFill("comic-video-package-packet-001"),
            imageFacingDeliveryProofFile = "image_facing_delivery_proof.json",
            voiceTimelineFillFile = "voice_timeline_fill.json",
            packageId = "comic-video-production-package-001",
            packetId = "comic-video-package-packet-001",
            panelDurationMillis = 2200,
        )

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

    private fun assertSafeValidationBoundary(report: ComicVideoProductionPackageValidationReport) {
        val text = JsonSupport.json.encodeToString(report)
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
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
