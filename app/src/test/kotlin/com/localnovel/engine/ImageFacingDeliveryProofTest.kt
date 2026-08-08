package com.localnovel.engine

import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.model.ReviewableStoryboardFrameSummary
import com.localnovel.engine.model.ReviewableStoryboardPacket
import com.localnovel.engine.service.ImageFacingDeliveryProofService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ImageFacingDeliveryProofTest {
    @Test
    fun passReviewableStoryboardPacketCreatesImageFacingDeliveryProof() {
        val proof = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = reviewablePacket(),
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
            proofId = "image-facing-delivery-proof-001",
        )

        assertEquals("PASS", proof.verdict, proof.findings.joinToString { it.ruleId })
        assertEquals("image-facing-delivery-proof-001", proof.proofId)
        assertEquals("image_facing_delivery_proof_candidate", proof.proofStatus)
        assertEquals("image_facing_delivery_proof_ready", proof.nextAllowedDownstreamStep)
        assertEquals("image_facing_delivery_proof_only", proof.downstreamBoundary)
        assertEquals("reviewable-storyboard-packet-001", proof.reviewableStoryboardPacketId)
        assertEquals("accepted-script-packet-001", proof.acceptedScriptPacketId)
        assertEquals("script_packet_draft.json", proof.scriptPacketDraftFile)
        assertEquals("a".repeat(64), proof.scriptPacketDraftContentSha256)
        assertEquals(2, proof.frameDeliveryCards.size)
        assertEquals("image_facing_candidate_only", proof.frameDeliveryCards.first().deliveryStatus)
        assertEquals(2, proof.evidence.frameWithVisualIntentCount)
        assertEquals(2, proof.evidence.frameWithCameraIntentCount)
        assertEquals(2, proof.evidence.frameWithMustShowCount)
        assertEquals(2, proof.evidence.frameWithDoNotShowCount)
        assertEquals(2, proof.evidence.frameWithContinuityAnchorCount)
        assertTrue(proof.evidence.assetCandidateRefCount >= 2)
        assertFalse(proof.nextAllowedDownstreamStep.contains("provider", ignoreCase = true))
        assertFalse(proof.nextAllowedDownstreamStep.contains("generated", ignoreCase = true))
        assertFalse(proof.nextAllowedDownstreamStep.contains("approval", ignoreCase = true))
        assertFalse(proof.nextAllowedDownstreamStep.contains("canon", ignoreCase = true))
    }

    @Test
    fun nonPassOrWrongSourceStateFailsAndClearsDeliveryCards() {
        val nonPass = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = reviewablePacket(verdict = "FAIL", nextAllowedDownstreamStep = ""),
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
        )
        val wrongNext = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = reviewablePacket(nextAllowedDownstreamStep = "image_candidate_preflight"),
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
        )

        assertEquals("FAIL", nonPass.verdict)
        assertEquals("", nonPass.nextAllowedDownstreamStep)
        assertTrue(nonPass.frameDeliveryCards.isEmpty())
        assertTrue(nonPass.findings.any { it.ruleId == "REVIEWABLE_STORYBOARD_PACKET_NOT_PASS" })
        assertEquals("FAIL", wrongNext.verdict)
        assertEquals("", wrongNext.nextAllowedDownstreamStep)
        assertTrue(wrongNext.frameDeliveryCards.isEmpty())
        assertTrue(wrongNext.findings.any { it.ruleId == "REVIEWABLE_STORYBOARD_PACKET_NEXT_STEP_MISMATCH" })
    }

    @Test
    fun missingFrameDeliveryFieldsFailProof() {
        val brokenFrame = frame(
            frameId = "frame-001",
            visualIntent = "",
            cameraIntent = "",
            mustShow = emptyList(),
            doNotShow = emptyList(),
            continuityAnchors = emptyList(),
        )
        val proof = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = reviewablePacket(frameSummaries = listOf(brokenFrame)),
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
        )

        assertEquals("FAIL", proof.verdict)
        assertEquals("", proof.nextAllowedDownstreamStep)
        assertTrue(proof.frameDeliveryCards.isEmpty())
        assertTrue(proof.findings.any { it.ruleId == "FRAME_VISUAL_INTENT_MISSING" })
        assertTrue(proof.findings.any { it.ruleId == "FRAME_CAMERA_INTENT_MISSING" })
        assertTrue(proof.findings.any { it.ruleId == "FRAME_MUST_SHOW_MISSING" })
        assertTrue(proof.findings.any { it.ruleId == "FRAME_DO_NOT_SHOW_MISSING" })
        assertTrue(proof.findings.any { it.ruleId == "FRAME_CONTINUITY_ANCHORS_MISSING" })
    }

    @Test
    fun forbiddenStatusUpgradeOrRemoteReferenceFailsProof() {
        val statusUpgrade = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = reviewablePacket(
                frameSummaries = listOf(frame(assetCandidateRefs = listOf("approved_reference"))),
            ),
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
        )
        val remoteReference = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = reviewablePacket(
                frameSummaries = listOf(frame(visualIntent = "manual metadata from https://provider.example/image.png")),
            ),
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
        )

        assertEquals("FAIL", statusUpgrade.verdict)
        assertEquals("", statusUpgrade.nextAllowedDownstreamStep)
        assertTrue(statusUpgrade.findings.any { it.ruleId == "IMAGE_FACING_DOWNSTREAM_CLAIM_FORBIDDEN" })
        assertEquals("FAIL", remoteReference.verdict)
        assertEquals("", remoteReference.nextAllowedDownstreamStep)
        assertTrue(remoteReference.findings.any { it.ruleId == "REMOTE_REFERENCE_FORBIDDEN" })
    }

    @Test
    fun remoteScriptPacketDraftFileFailsProof() {
        val proof = ImageFacingDeliveryProofService().create(
            reviewableStoryboardPacket = reviewablePacket(scriptPacketDraftFile = "https://provider.example/script.json"),
            reviewableStoryboardPacketFile = "reviewable_storyboard_packet.json",
        )

        assertEquals("FAIL", proof.verdict)
        assertEquals("", proof.nextAllowedDownstreamStep)
        assertTrue(proof.frameDeliveryCards.isEmpty())
        assertTrue(proof.findings.any { it.ruleId == "REMOTE_REFERENCE_FORBIDDEN" })
    }

    @Test
    fun cliWritesImageFacingDeliveryProofWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-facing-delivery-proof")
        val packetPath = tempDir.resolve("reviewable_storyboard_packet.json")
        val out = tempDir.resolve("image_facing_delivery_proof.json")
        writeJson(packetPath, reviewablePacket())

        val exit = Cli().run(
            listOf(
                "create-image-facing-delivery-proof",
                "--reviewable-storyboard-packet",
                packetPath.toString(),
                "--proof",
                "image-facing-delivery-proof-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val proof = readJson<ImageFacingDeliveryProof>(out)
        assertEquals("PASS", proof.verdict, proof.findings.joinToString { it.ruleId })
        assertEquals("image-facing-delivery-proof-cli-001", proof.proofId)
        assertEquals("image_facing_delivery_proof_ready", proof.nextAllowedDownstreamStep)
    }

    private fun reviewablePacket(
        verdict: String = "PASS",
        packetStatus: String = "reviewable_storyboard_packet_candidate",
        downstreamBoundary: String = "reviewable_storyboard_packet_packaging_only",
        nextAllowedDownstreamStep: String = "reviewable_storyboard_packet_ready",
        scriptPacketDraftFile: String = "script_packet_draft.json",
        frameSummaries: List<ReviewableStoryboardFrameSummary> = listOf(
            frame(
                frameId = "frame-001",
                visualIntent = "Show Mira arriving after the paid toll while the guard checks the visible mark.",
                cameraIntent = "Medium shot clarifying gate distance and visible mark.",
                mustShow = listOf(protectedAnchors[0], "The checkpoint gate remains visible."),
            ),
            frame(
                frameId = "frame-002",
                beatReference = "beat-check",
                visualIntent = "Show the guard requiring the visible mark before giving route information.",
                cameraIntent = "Close shot on the mark and the guard's restrained response.",
                mustShow = listOf(protectedAnchors[1], "Mira keeps the toll token visible."),
            ),
        ),
    ): ReviewableStoryboardPacket =
        ReviewableStoryboardPacket(
            verdict = verdict,
            packetId = "reviewable-storyboard-packet-001",
            packetStatus = packetStatus,
            storyboardPacketDraftFile = "storyboard_packet_draft.json",
            storyboardPacketReviewFile = "storyboard_packet_review.json",
            storyboardHumanReviewPreflightFile = "storyboard_human_review_preflight.json",
            storyboardStoryMovementAuditFile = "storyboard_story_movement_audit.json",
            storyboardPacketReviewId = "storyboard-review-001",
            storyboardStoryMovementAuditId = "story-movement-audit-001",
            storyboardPacketDraftId = "storyboard-packet-draft-001",
            storyboardPlanningBriefId = "storyboard-planning-brief-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = "a".repeat(64),
            frameSummaries = frameSummaries,
            remainingResidualRisks = listOf("Text-only packet still waits for downstream human decisions."),
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun frame(
        frameId: String = "frame-001",
        beatReference: String = "beat-arrival",
        visualIntent: String = "Show Mira arriving after the paid toll while the guard checks the visible mark.",
        cameraIntent: String = "Medium shot clarifying gate distance and visible mark.",
        mustShow: List<String> = listOf(protectedAnchors[0]),
        doNotShow: List<String> = listOf("Do not reveal hidden culprit evidence."),
        continuityAnchors: List<String> = protectedAnchors,
        assetCandidateRefs: List<String> = listOf("asset-visible-mark"),
    ): ReviewableStoryboardFrameSummary =
        ReviewableStoryboardFrameSummary(
            frameId = frameId,
            sceneReference = "checkpoint-gate",
            beatReference = beatReference,
            visualIntent = visualIntent,
            cameraIntent = cameraIntent,
            mustShow = mustShow,
            doNotShow = doNotShow,
            continuityAnchors = continuityAnchors,
            assetCandidateRefs = assetCandidateRefs,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }

    private companion object {
        val protectedAnchors = listOf(
            "Mira has already paid the door toll before reaching the checkpoint.",
            "The guard requires a visible mark before answering questions.",
        )
    }
}
