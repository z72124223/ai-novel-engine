package com.localnovel.engine

import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.model.SettingImageFacingProofPreviewBuildReport
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingImageFacingProofPreviewBuildCliTest {
    @Test
    fun settingSceneBuildsPassImageFacingProofPreviewChain() {
        val tempDir = Files.createTempDirectory("setting-image-facing-proof-preview")
        val reportPath = tempDir.resolve("build_report.json")

        val exit = Cli().run(
            listOf(
                "build-setting-image-facing-proof-preview",
                "--setting-pack",
                samplePath("comic_video_setting_source/setting_pack_rain_gate_sample.json").toString(),
                "--scene",
                "scene-rain-gate",
                "--output-dir",
                tempDir.toString(),
                "--build",
                "setting-image-facing-preview-test-001",
                "--script-packet",
                "script-packet-draft-rain-gate-preview-test-001",
                "--storyboard-brief",
                "storyboard-brief-rain-gate-preview-test-001",
                "--storyboard-draft",
                "storyboard-draft-rain-gate-preview-test-001",
                "--storyboard-review",
                "storyboard-review-rain-gate-preview-test-001",
                "--human-review-preflight",
                "storyboard-human-review-preflight-rain-gate-preview-test-001",
                "--story-movement-audit",
                "storyboard-story-movement-audit-rain-gate-preview-test-001",
                "--reviewable-storyboard",
                "reviewable-storyboard-rain-gate-preview-test-001",
                "--proof",
                "image-facing-delivery-proof-rain-gate-preview-test-001",
                "--report-out",
                reportPath.toString(),
            ),
        )

        assertEquals(0, exit)
        listOf(
            "script_packet_draft.json",
            "script_packet_draft_validation.json",
            "setting_script_packet_draft_build_report.json",
            "storyboard_planning_brief.json",
            "storyboard_packet_draft.json",
            "storyboard_packet_draft_validation.json",
            "storyboard_packet_review.json",
            "storyboard_human_review_preflight.json",
            "storyboard_story_movement_audit.json",
            "reviewable_storyboard_packet.json",
            "image_facing_delivery_proof.json",
        ).forEach { fileName ->
            assertTrue(Files.exists(tempDir.resolve(fileName)), "Missing preview artifact: $fileName")
        }
        assertTrue(Files.exists(reportPath))

        val report = readJson<SettingImageFacingProofPreviewBuildReport>(reportPath)
        val proof = readJson<ImageFacingDeliveryProof>(tempDir.resolve("image_facing_delivery_proof.json"))
        assertEquals("PASS", report.verdict, report.findings.joinToString { it.ruleId })
        assertEquals("local_preview_image_facing_proof_built", report.buildStatus)
        assertEquals("image_facing_delivery_proof_ready", report.nextAllowedDownstreamStep)
        assertEquals("PASS", report.evidence.imageFacingDeliveryProofVerdict)
        assertEquals("PASS", report.evidence.storyboardStoryMovementAuditVerdict)
        assertEquals(3, report.evidence.generatedStoryboardFrameCount)
        assertEquals(3, report.evidence.generatedImageFrameCardCount)
        assertEquals(1, report.evidence.excludedSecretCount)
        assertEquals("PASS", proof.verdict, proof.findings.joinToString { it.ruleId })
        assertEquals("image_facing_delivery_proof_ready", proof.nextAllowedDownstreamStep)
        assertEquals(3, proof.frameDeliveryCards.size)

        val publicArtifacts = listOf(
            tempDir.resolve("script_packet_draft.json"),
            tempDir.resolve("storyboard_packet_draft.json"),
            tempDir.resolve("image_facing_delivery_proof.json"),
            reportPath,
        ).joinToString("\n") { Files.readString(it) }
        assertFalse(publicArtifacts.contains("second bell", ignoreCase = true))
        assertFalse(publicArtifacts.contains("author_only", ignoreCase = true))
        assertFalse(publicArtifacts.contains("provider job", ignoreCase = true))
        assertFalse(publicArtifacts.contains("production lock", ignoreCase = true))
        assertFalse(publicArtifacts.contains("final asset", ignoreCase = true))
    }

    @Test
    fun missingSceneWritesFailReportAndReturnsNonZero() {
        val tempDir = Files.createTempDirectory("setting-image-facing-proof-preview-missing-scene")
        val reportPath = tempDir.resolve("build_report.json")

        val exit = Cli().run(
            listOf(
                "build-setting-image-facing-proof-preview",
                "--setting-pack",
                samplePath("comic_video_setting_source/setting_pack_rain_gate_sample.json").toString(),
                "--scene",
                "scene-missing",
                "--output-dir",
                tempDir.toString(),
                "--report-out",
                reportPath.toString(),
            ),
        )

        assertEquals(1, exit)
        assertTrue(Files.exists(reportPath))
        assertFalse(Files.exists(tempDir.resolve("script_packet_draft.json")))
        assertFalse(Files.exists(tempDir.resolve("image_facing_delivery_proof.json")))
        val report = readJson<SettingImageFacingProofPreviewBuildReport>(reportPath)
        assertEquals("FAIL", report.verdict)
        assertEquals("FAIL", report.buildStatus)
        assertTrue(report.findings.any { it.ruleId == "SETTING_SCRIPT_PACKET_DRAFT_BUILD_FAILED" })
        assertEquals("FAIL", report.evidence.settingScriptDraftBuildVerdict)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private fun samplePath(relative: String): Path {
        val candidates = listOf(
            Path.of("samples", relative),
            Path.of("..", "samples", relative),
        )
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $relative")
    }
}
