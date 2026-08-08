package com.localnovel.engine.service

import com.localnovel.engine.model.ComicVideoPacket
import com.localnovel.engine.model.ComicVideoPacketSourceVerdicts
import com.localnovel.engine.model.ComicVideoPanelTrack
import com.localnovel.engine.model.ComicVideoTiming
import com.localnovel.engine.model.ImageFacingDeliveryProof

class ComicVideoPacketService {
    private val requiredProofStatus = "image_facing_delivery_proof_candidate"
    private val requiredProofNextStep = "image_facing_delivery_proof_ready"
    private val requiredProofBoundary = "image_facing_delivery_proof_only"
    private val defaultPanelDurationMillis = 3000

    fun create(
        imageFacingDeliveryProof: ImageFacingDeliveryProof,
        imageFacingDeliveryProofFile: String = "",
        packetId: String = "",
        panelDurationMillis: Int = defaultPanelDurationMillis,
    ): ComicVideoPacket {
        requireSourceReady(imageFacingDeliveryProof)
        require(imageFacingDeliveryProofFile.isNotBlank()) {
            "Comic video packet creation requires nonblank imageFacingDeliveryProofFile."
        }
        require(panelDurationMillis > 0) {
            "Comic video packet creation requires positive panelDurationMillis."
        }

        val resolvedPacketId = packetId.ifBlank {
            imageFacingDeliveryProof.proofId
                .takeIf { it.isNotBlank() }
                ?.let { "$it-comic-video-packet" }
                .orEmpty()
        }
        val panelTracks = imageFacingDeliveryProof.frameDeliveryCards.map { card ->
            ComicVideoPanelTrack(
                panelId = "panel-${card.frameId}",
                sourceFrameId = card.frameId,
                sceneReference = card.sceneReference,
                beatReference = card.beatReference,
                visualIntent = card.visualIntent,
                cameraIntent = card.cameraIntent,
                displayDurationMillis = panelDurationMillis,
                mustShow = card.mustShow,
                doNotShow = card.doNotShow,
                continuityAnchors = card.continuityAnchors,
                assetCandidateRefs = card.assetCandidateRefs,
            )
        }

        return ComicVideoPacket(
            packetId = resolvedPacketId,
            reviewableStoryboardPacketFile = imageFacingDeliveryProof.reviewableStoryboardPacketFile,
            imageFacingDeliveryProofFile = imageFacingDeliveryProofFile,
            sourceReviewableStoryboardPacketId = imageFacingDeliveryProof.reviewableStoryboardPacketId,
            sourceImageFacingDeliveryProofId = imageFacingDeliveryProof.proofId,
            acceptedScriptPacketId = imageFacingDeliveryProof.acceptedScriptPacketId,
            scriptPacketDraftFile = imageFacingDeliveryProof.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = imageFacingDeliveryProof.scriptPacketDraftContentSha256,
            sourceVerdicts = ComicVideoPacketSourceVerdicts(
                reviewableStoryboardPacketVerdict = imageFacingDeliveryProof.sourceVerdict,
                imageFacingDeliveryProofVerdict = imageFacingDeliveryProof.verdict,
                imageFacingDeliveryProofNextAllowedDownstreamStep = imageFacingDeliveryProof.nextAllowedDownstreamStep,
            ),
            panelTracks = panelTracks,
            voiceCast = emptyList(),
            audioCues = emptyList(),
            subtitleCues = emptyList(),
            timing = ComicVideoTiming(
                totalDurationMillis = panelTracks.size * panelDurationMillis,
            ),
            boundaryChecklist = listOf(
                "metadata_only",
                "source_image_facing_ready",
                "author_voice_fill_required",
                "candidate_timing_plan",
            ),
            residualRisks = listOf(
                "Voice cast, audio cues, and subtitle cues require author fill before validation can pass.",
            ),
        )
    }

    private fun requireSourceReady(proof: ImageFacingDeliveryProof) {
        require(proof.verdict.trim().uppercase() == "PASS") {
            "Comic video packet creation requires PASS image-facing delivery proof."
        }
        require(proof.proofStatus == requiredProofStatus) {
            "Comic video packet creation requires proofStatus=$requiredProofStatus."
        }
        require(proof.nextAllowedDownstreamStep == requiredProofNextStep) {
            "Comic video packet creation requires nextAllowedDownstreamStep=$requiredProofNextStep."
        }
        require(proof.downstreamBoundary == requiredProofBoundary) {
            "Comic video packet creation requires downstreamBoundary=$requiredProofBoundary."
        }
        require(proof.frameDeliveryCards.isNotEmpty()) {
            "Comic video packet creation requires at least one image-facing frame delivery card."
        }
    }
}
