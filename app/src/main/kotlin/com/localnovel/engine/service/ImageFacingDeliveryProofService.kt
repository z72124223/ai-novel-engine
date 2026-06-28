package com.localnovel.engine.service

import com.localnovel.engine.model.ImageFacingDeliveryFrameCard
import com.localnovel.engine.model.ImageFacingDeliveryProof
import com.localnovel.engine.model.ImageFacingDeliveryProofEvidence
import com.localnovel.engine.model.ImageFacingDeliveryProofFinding
import com.localnovel.engine.model.ReviewableStoryboardPacket

class ImageFacingDeliveryProofService {
    private val sourcePacketStatus = "reviewable_storyboard_packet_candidate"
    private val sourceNextStep = "reviewable_storyboard_packet_ready"
    private val sourceBoundary = "reviewable_storyboard_packet_packaging_only"
    private val proofStatus = "image_facing_delivery_proof_candidate"
    private val proofNextStep = "image_facing_delivery_proof_ready"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val embeddedUrlRegex = Regex("""(?i)\b[a-z][a-z0-9+.-]*://""")
    private val forbiddenClaimPatterns = listOf(
        Regex("""(?i)\basset\s+approval\b|\basset\s+approved\b|\buser\s+approval\b"""),
        Regex("""(?i)\bapproved[-_\s]?reference\b"""),
        Regex("""(?i)\bregistered[-_\s]?reference\b"""),
        Regex("""(?i)\bcanon\b"""),
        Regex("""(?i)\bproduction[-_\s]?lock(?:ed)?\b|\bproduction[-_\s]?ready\b"""),
        Regex("""(?i)\bimage\s+generated\b|\bgenerated\s+image\b"""),
        Regex("""(?i)\bvideo\s+generated\b|\bgenerated\s+video\b"""),
        Regex("""(?i)\bprovider[-_\s]?job\b|\bprovider\s+handoff\b"""),
        Regex("""(?i)\bapi\s+(response|result)\b|\bgeneration\s+api\b"""),
        Regex("""(?i)\bruntime\s+scene\s+created\b"""),
        Regex("""(?i)\b(final|completed)\s+(storyboard|asset|image|video|media)\b"""),
        Regex("\u6838\u51c6|\u6279\u51c6|\u767b\u8a18\u5b8c\u6210|\u6b63\u5178|\u9396\u5b9a|\u6b63\u5f0f\u7d20\u6750"),
    )

    fun create(
        reviewableStoryboardPacket: ReviewableStoryboardPacket,
        reviewableStoryboardPacketFile: String = "",
        proofId: String = "",
    ): ImageFacingDeliveryProof {
        val findings = mutableListOf<ImageFacingDeliveryProofFinding>()
        validateSourceState(reviewableStoryboardPacket, findings)
        val sourceBindingFieldCount = validateSourceBinding(
            packet = reviewableStoryboardPacket,
            packetFile = reviewableStoryboardPacketFile,
            findings = findings,
        )
        validateFrameDeliveryCards(reviewableStoryboardPacket, findings)
        val forbiddenClaimCount = scanForbiddenClaims(reviewableStoryboardPacket, findings)
        val remoteReferenceCount = scanRemoteReferences(reviewableStoryboardPacket, findings)

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val blockedReason = if (verdict == "FAIL") {
            findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
        } else {
            ""
        }
        val resolvedProofId = proofId.ifBlank {
            reviewableStoryboardPacket.packetId.takeIf { it.isNotBlank() }?.let { "$it-image-facing-delivery-proof" }.orEmpty()
        }

        return ImageFacingDeliveryProof(
            verdict = verdict,
            proofId = resolvedProofId,
            proofStatus = if (verdict == "PASS") proofStatus else "FAIL",
            reviewableStoryboardPacketFile = reviewableStoryboardPacketFile,
            reviewableStoryboardPacketId = reviewableStoryboardPacket.packetId,
            reviewableStoryboardPacketStatus = reviewableStoryboardPacket.packetStatus,
            acceptedScriptPacketId = reviewableStoryboardPacket.acceptedScriptPacketId,
            scriptPacketDraftFile = reviewableStoryboardPacket.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = reviewableStoryboardPacket.scriptPacketDraftContentSha256,
            sourceVerdict = reviewableStoryboardPacket.verdict,
            sourceNextAllowedDownstreamStep = reviewableStoryboardPacket.nextAllowedDownstreamStep,
            frameDeliveryCards = if (verdict == "PASS") {
                reviewableStoryboardPacket.frameSummaries.map { frame ->
                    ImageFacingDeliveryFrameCard(
                        frameId = frame.frameId,
                        sceneReference = frame.sceneReference,
                        beatReference = frame.beatReference,
                        visualIntent = frame.visualIntent,
                        cameraIntent = frame.cameraIntent,
                        mustShow = frame.mustShow,
                        doNotShow = frame.doNotShow,
                        continuityAnchors = frame.continuityAnchors,
                        assetCandidateRefs = frame.assetCandidateRefs,
                    )
                }
            } else {
                emptyList()
            },
            nextAllowedDownstreamStep = if (verdict == "PASS") proofNextStep else "",
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = ImageFacingDeliveryProofEvidence(
                sourceBindingFieldCount = sourceBindingFieldCount,
                frameCount = reviewableStoryboardPacket.frameSummaries.size,
                uniqueFrameIdCount = reviewableStoryboardPacket.frameSummaries.map { it.frameId.trim() }.filter { it.isNotBlank() }.distinct().size,
                frameWithVisualIntentCount = reviewableStoryboardPacket.frameSummaries.count { it.visualIntent.isNotBlank() },
                frameWithCameraIntentCount = reviewableStoryboardPacket.frameSummaries.count { it.cameraIntent.isNotBlank() },
                frameWithMustShowCount = reviewableStoryboardPacket.frameSummaries.count { it.mustShow.any { value -> value.isNotBlank() } },
                frameWithDoNotShowCount = reviewableStoryboardPacket.frameSummaries.count { it.doNotShow.any { value -> value.isNotBlank() } },
                frameWithContinuityAnchorCount = reviewableStoryboardPacket.frameSummaries.count { it.continuityAnchors.any { value -> value.isNotBlank() } },
                assetCandidateRefCount = reviewableStoryboardPacket.frameSummaries.sumOf { it.assetCandidateRefs.count { value -> value.isNotBlank() } },
                forbiddenClaimCount = forbiddenClaimCount,
                remoteReferenceCount = remoteReferenceCount,
            ),
        )
    }

    private fun validateSourceState(
        packet: ReviewableStoryboardPacket,
        findings: MutableList<ImageFacingDeliveryProofFinding>,
    ) {
        if (packet.verdict.trim().uppercase() != "PASS") {
            findings.add(finding("REVIEWABLE_STORYBOARD_PACKET_NOT_PASS", "upstream", "CRITICAL", "$.reviewableStoryboardPacket.verdict", "Image-facing delivery proof requires PASS reviewable storyboard packet.", listOf(packet.verdict)))
        }
        if (packet.packetStatus != sourcePacketStatus) {
            findings.add(finding("REVIEWABLE_STORYBOARD_PACKET_STATUS_MISMATCH", "upstream", "CRITICAL", "$.reviewableStoryboardPacket.packetStatus", "Image-facing delivery proof requires packetStatus=$sourcePacketStatus.", listOf(packet.packetStatus)))
        }
        if (packet.nextAllowedDownstreamStep != sourceNextStep) {
            findings.add(finding("REVIEWABLE_STORYBOARD_PACKET_NEXT_STEP_MISMATCH", "upstream", "CRITICAL", "$.reviewableStoryboardPacket.nextAllowedDownstreamStep", "Image-facing delivery proof requires nextAllowedDownstreamStep=$sourceNextStep.", listOf(packet.nextAllowedDownstreamStep)))
        }
        if (packet.downstreamBoundary != sourceBoundary) {
            findings.add(finding("REVIEWABLE_STORYBOARD_PACKET_BOUNDARY_MISMATCH", "upstream", "CRITICAL", "$.reviewableStoryboardPacket.downstreamBoundary", "Image-facing delivery proof requires downstreamBoundary=$sourceBoundary.", listOf(packet.downstreamBoundary)))
        }
    }

    private fun validateSourceBinding(
        packet: ReviewableStoryboardPacket,
        packetFile: String,
        findings: MutableList<ImageFacingDeliveryProofFinding>,
    ): Int {
        var count = 0
        fun requireNonBlank(value: String, path: String, ruleId: String, label: String) {
            if (value.isBlank()) {
                findings.add(finding(ruleId, "source_linkage", "CRITICAL", path, "Image-facing delivery proof requires nonblank $label."))
            } else {
                count += 1
            }
        }
        requireNonBlank(packetFile, "$.inputs.reviewableStoryboardPacketFile", "REVIEWABLE_STORYBOARD_PACKET_FILE_MISSING", "reviewable storyboard packet input file")
        requireNonBlank(packet.packetId, "$.reviewableStoryboardPacket.packetId", "REVIEWABLE_STORYBOARD_PACKET_ID_MISSING", "reviewable storyboard packet id")
        requireNonBlank(packet.acceptedScriptPacketId, "$.reviewableStoryboardPacket.acceptedScriptPacketId", "ACCEPTED_SCRIPT_PACKET_ID_MISSING", "accepted script packet id")
        requireNonBlank(packet.scriptPacketDraftFile, "$.reviewableStoryboardPacket.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "script packet draft file")
        requireNonBlank(packet.scriptPacketDraftContentSha256, "$.reviewableStoryboardPacket.scriptPacketDraftContentSha256", "SCRIPT_PACKET_DRAFT_HASH_MISSING", "script packet draft SHA-256")
        if (packet.scriptPacketDraftContentSha256.isNotBlank() && !sha256Regex.matches(packet.scriptPacketDraftContentSha256.trim())) {
            findings.add(finding("SCRIPT_PACKET_DRAFT_HASH_INVALID", "source_linkage", "CRITICAL", "$.reviewableStoryboardPacket.scriptPacketDraftContentSha256", "Image-facing delivery proof requires lowercase 64-character SHA-256.", listOf(packet.scriptPacketDraftContentSha256)))
        }
        return count
    }

    private fun validateFrameDeliveryCards(
        packet: ReviewableStoryboardPacket,
        findings: MutableList<ImageFacingDeliveryProofFinding>,
    ) {
        if (packet.frameSummaries.isEmpty()) {
            findings.add(finding("FRAME_DELIVERY_CARDS_MISSING", "image_facing_delivery", "CRITICAL", "$.reviewableStoryboardPacket.frameSummaries", "Image-facing delivery proof requires at least one frame summary."))
            return
        }
        val ids = packet.frameSummaries.map { it.frameId.trim() }
        ids.forEachIndexed { index, id ->
            if (id.isBlank()) findings.add(finding("FRAME_ID_MISSING", "image_facing_delivery", "CRITICAL", "$.reviewableStoryboardPacket.frameSummaries[$index].frameId", "Every image-facing delivery card requires a nonblank frame id."))
        }
        val duplicates = ids.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys
        if (duplicates.isNotEmpty()) {
            findings.add(finding("FRAME_ID_DUPLICATE", "image_facing_delivery", "CRITICAL", "$.reviewableStoryboardPacket.frameSummaries", "Image-facing delivery card frame ids must be unique.", duplicates.toList()))
        }
        packet.frameSummaries.forEachIndexed { index, frame ->
            val path = "$.reviewableStoryboardPacket.frameSummaries[$index]"
            if (frame.visualIntent.isBlank()) findings.add(finding("FRAME_VISUAL_INTENT_MISSING", "image_facing_delivery", "MAJOR", "$path.visualIntent", "Image-facing delivery proof requires visual intent for every frame."))
            if (frame.cameraIntent.isBlank()) findings.add(finding("FRAME_CAMERA_INTENT_MISSING", "image_facing_delivery", "MAJOR", "$path.cameraIntent", "Image-facing delivery proof requires camera intent for every frame."))
            if (frame.mustShow.none { it.isNotBlank() }) findings.add(finding("FRAME_MUST_SHOW_MISSING", "image_facing_delivery", "MAJOR", "$path.mustShow", "Image-facing delivery proof requires must-show constraints for every frame."))
            if (frame.doNotShow.none { it.isNotBlank() }) findings.add(finding("FRAME_DO_NOT_SHOW_MISSING", "image_facing_delivery", "MAJOR", "$path.doNotShow", "Image-facing delivery proof requires do-not-show constraints for every frame."))
            if (frame.continuityAnchors.none { it.isNotBlank() }) findings.add(finding("FRAME_CONTINUITY_ANCHORS_MISSING", "image_facing_delivery", "MAJOR", "$path.continuityAnchors", "Image-facing delivery proof requires continuity anchors for every frame."))
        }
    }

    private fun scanForbiddenClaims(
        packet: ReviewableStoryboardPacket,
        findings: MutableList<ImageFacingDeliveryProofFinding>,
    ): Int {
        var count = 0
        collectText(packet).filter { it.value.isNotBlank() }.forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                if (pattern.containsMatchIn(field.value)) {
                    count += 1
                    findings.add(finding("IMAGE_FACING_DOWNSTREAM_CLAIM_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Image-facing delivery proof cannot carry approval, generated media, provider job, API, canon, production lock, or runtime scene claims.", listOf(field.value)))
                }
            }
        }
        return count
    }

    private fun scanRemoteReferences(
        packet: ReviewableStoryboardPacket,
        findings: MutableList<ImageFacingDeliveryProofFinding>,
    ): Int {
        var count = 0
        collectText(packet).filter { it.value.isNotBlank() }.forEach { field ->
            if (embeddedUrlRegex.containsMatchIn(field.value)) {
                count += 1
                findings.add(finding("REMOTE_REFERENCE_FORBIDDEN", "safety_boundary", "CRITICAL", field.path, "Image-facing delivery proof cannot carry remote URL or URI references.", listOf(field.value)))
            }
        }
        return count
    }

    private fun collectText(packet: ReviewableStoryboardPacket): List<TextField> {
        val fields = mutableListOf<TextField>()
        fields += TextField("$.reviewableStoryboardPacket.packetId", packet.packetId)
        fields += TextField("$.reviewableStoryboardPacket.acceptedScriptPacketId", packet.acceptedScriptPacketId)
        fields += TextField("$.reviewableStoryboardPacket.scriptPacketDraftFile", packet.scriptPacketDraftFile)
        fields += TextField("$.reviewableStoryboardPacket.packetStatus", packet.packetStatus)
        fields += TextField("$.reviewableStoryboardPacket.downstreamBoundary", packet.downstreamBoundary)
        fields += TextField("$.reviewableStoryboardPacket.nextAllowedDownstreamStep", packet.nextAllowedDownstreamStep)
        packet.remainingResidualRisks.forEachIndexed { index, value -> fields += TextField("$.reviewableStoryboardPacket.remainingResidualRisks[$index]", value) }
        packet.frameSummaries.forEachIndexed { index, frame ->
            val path = "$.reviewableStoryboardPacket.frameSummaries[$index]"
            fields += TextField("$path.sceneReference", frame.sceneReference)
            fields += TextField("$path.beatReference", frame.beatReference)
            fields += TextField("$path.visualIntent", frame.visualIntent)
            fields += TextField("$path.cameraIntent", frame.cameraIntent)
            frame.mustShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.mustShow[$itemIndex]", value) }
            frame.doNotShow.forEachIndexed { itemIndex, value -> fields += TextField("$path.doNotShow[$itemIndex]", value) }
            frame.continuityAnchors.forEachIndexed { itemIndex, value -> fields += TextField("$path.continuityAnchors[$itemIndex]", value) }
            frame.assetCandidateRefs.forEachIndexed { itemIndex, value -> fields += TextField("$path.assetCandidateRefs[$itemIndex]", value) }
        }
        return fields
    }

    private fun finding(ruleId: String, category: String, severity: String, path: String, message: String, evidence: List<String> = emptyList(), suggestedFix: String = ""): ImageFacingDeliveryProofFinding =
        ImageFacingDeliveryProofFinding(ruleId, category, severity, path, message, evidence, suggestedFix)

    private data class TextField(val path: String, val value: String)
}
