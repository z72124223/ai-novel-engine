package com.localnovel.engine

import com.localnovel.engine.model.ImageCandidateExternalResultItem
import com.localnovel.engine.model.ImageCandidateReferencePacket
import com.localnovel.engine.model.ImageCandidateResultReviewDecisionIntakeReport
import com.localnovel.engine.model.ImageCandidateResultReviewPreflightReport
import com.localnovel.engine.service.ImageCandidateReferencePacketService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageCandidateReferencePacketTest {
    @Test
    fun passPreflightAndDecisionIntakeCreatesCandidateReferencePacket() {
        val packet = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = preflight(),
            decisionIntake = decisionIntake(),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
        )

        assertEquals("PASS", packet.verdict)
        assertEquals("reference_candidate", packet.packetStatus)
        assertEquals("candidate_reference_packet_validation", packet.nextAllowedDownstreamStep)
        assertEquals(1, packet.imageResults.size)
    }

    @Test
    fun upstreamNonPassOrNotDownstreamAllowedFailsPacketCreation() {
        val nonPassPreflight = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = preflight(verdict = "FAIL"),
            decisionIntake = decisionIntake(),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
        )
        val notAllowed = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = preflight(),
            decisionIntake = decisionIntake(decisionAllowedDownstream = false, nextAllowedDownstreamStep = ""),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
        )

        assertEquals("FAIL", nonPassPreflight.verdict)
        assertTrue(nonPassPreflight.findings.any { it.ruleId == "RESULT_REVIEW_PREFLIGHT_NOT_PASS" })
        assertEquals("FAIL", notAllowed.verdict)
        assertTrue(notAllowed.findings.any { it.ruleId == "DECISION_NOT_DOWNSTREAM_ALLOWED" })
    }

    @Test
    fun sourceMismatchOrMissingDecisionIdFailsPacketCreation() {
        val mismatch = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = preflight(),
            decisionIntake = decisionIntake(resultId = "other-result"),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
        )
        val missingDecision = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = preflight(),
            decisionIntake = decisionIntake(decisionId = ""),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
        )

        assertEquals("FAIL", mismatch.verdict)
        assertTrue(mismatch.findings.any { it.ruleId == "RESULT_ID_MISMATCH" })
        assertEquals("FAIL", missingDecision.verdict)
        assertTrue(missingDecision.findings.any { it.ruleId == "DECISION_ID_MISSING" })
    }

    @Test
    fun forgedDecisionIntakeImageResultFailsPacketCreationEvenWhenTopLevelSourceMatches() {
        val packet = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = preflight(
                imageResults = listOf(
                    resultItem(
                        resultItemId = "image-result-001",
                        promptItemId = "img-req-door-mark-prompt",
                        sourceCandidateId = "asset-door-mark-candidate",
                        localImagePath = "outputs/candidates/original.png",
                    ),
                ),
            ),
            decisionIntake = decisionIntake(
                imageResults = listOf(
                    resultItem(
                        resultItemId = "image-result-FORGED",
                        promptItemId = "forged-prompt",
                        sourceCandidateId = "forged-candidate",
                        localImagePath = "outputs/candidates/forged.png",
                    ),
                ),
            ),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.imageResults.isEmpty())
        assertTrue(packet.findings.any { it.ruleId == "IMAGE_RESULT_SET_MISMATCH" })
        assertTrue(packet.findings.any { it.ruleId == "IMAGE_RESULT_NOT_IN_PREFLIGHT" })
    }

    @Test
    fun remoteMetadataProviderFieldsAndApprovalClaimsFailPacketCreation() {
        val packet = ImageCandidateReferencePacketService().create(
            resultReviewPreflight = preflight(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        notes = "asset approved; canon applied",
                    ),
                ),
            ),
            decisionIntake = decisionIntake(
                imageResults = listOf(
                    resultItem(
                        localImagePath = "",
                        metadataOnlyReference = "manual metadata from provider://job-001",
                        providerJobId = "job-001",
                        apiResponse = "api response",
                        remoteUrl = "https://provider.example/image.png",
                        generatedStatus = "image generated",
                        notes = "asset approved; canon applied",
                    ),
                ),
            ),
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionIntakeFile = "decision_intake.json",
        )

        assertEquals("FAIL", packet.verdict)
        assertEquals("", packet.nextAllowedDownstreamStep)
        assertTrue(packet.findings.any { it.ruleId == "REMOTE_METADATA_REFERENCE_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "PROVIDER_JOB_ID_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "API_RESPONSE_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "REMOTE_URL_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "GENERATED_STATUS_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN" })
        assertTrue(packet.findings.any { it.ruleId == "CANDIDATE_REFERENCE_STATUS_FORBIDDEN" })
    }

    @Test
    fun cliWritesCandidateReferencePacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("image-candidate-reference-packet")
        val preflightPath = tempDir.resolve("result_review_preflight.json")
        val intakePath = tempDir.resolve("decision_intake.json")
        val out = tempDir.resolve("candidate_reference_packet.json")
        writeJson(preflightPath, preflight())
        writeJson(intakePath, decisionIntake())

        val exit = Cli().run(
            listOf(
                "create-image-candidate-reference-packet",
                "--result-review-preflight",
                preflightPath.toString(),
                "--result-review-decision-intake",
                intakePath.toString(),
                "--packet",
                "candidate-reference-packet-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<ImageCandidateReferencePacket>(out)
        assertEquals("PASS", packet.verdict)
        assertEquals("candidate-reference-packet-001", packet.packetId)
        assertEquals("candidate_reference_packet_validation", packet.nextAllowedDownstreamStep)
    }

    private fun preflight(
        verdict: String = "PASS",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): ImageCandidateResultReviewPreflightReport =
        ImageCandidateResultReviewPreflightReport(
            verdict = verdict,
            preflightId = "result-review-preflight-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = "external-result-001",
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            packetId = "provider-handoff-packet-001",
            preflightSourceId = "provider-handoff-preflight-001",
            decisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            imageResults = imageResults,
            nextAllowedDownstreamStep = "image_candidate_result_human_review_ready",
        )

    private fun decisionIntake(
        verdict: String = "PASS",
        decisionId: String = "result-human-decision-001",
        decisionAllowedDownstream: Boolean = true,
        resultId: String = "external-result-001",
        nextAllowedDownstreamStep: String = "image_candidate_reference_packet",
        imageResults: List<ImageCandidateExternalResultItem> = listOf(resultItem()),
    ): ImageCandidateResultReviewDecisionIntakeReport =
        ImageCandidateResultReviewDecisionIntakeReport(
            verdict = verdict,
            decisionId = decisionId,
            resultReviewPreflightFile = "result_review_preflight.json",
            resultReviewDecisionFile = "result_review_decision.json",
            resultReviewPreflightId = "result-review-preflight-001",
            externalResultIntakeFile = "external_result_intake.json",
            resultId = resultId,
            providerHandoffPacketValidationReportFile = "provider_handoff_packet_validation.json",
            packetId = "provider-handoff-packet-001",
            preflightSourceId = "provider-handoff-preflight-001",
            providerDecisionId = "provider-handoff-decision-001",
            acceptedScriptPacketId = "accepted-script-packet-001",
            scriptPacketDraftFile = "script_packet_draft.json",
            scriptPacketDraftContentSha256 = "a".repeat(64),
            decisionStatus = "ALLOW_CANDIDATE_REFERENCE_PACKET",
            decisionAllowedDownstream = decisionAllowedDownstream,
            imageResults = imageResults,
            nextAllowedDownstreamStep = nextAllowedDownstreamStep,
        )

    private fun resultItem(
        resultItemId: String = "image-result-001",
        promptItemId: String = "img-req-door-mark-prompt",
        sourceCandidateId: String = "asset-door-mark-candidate",
        localImagePath: String = "outputs/candidates/door-mark-001.png",
        metadataOnlyReference: String = "",
        sourceFileSha256: String = "",
        providerJobId: String = "",
        apiResponse: String = "",
        remoteUrl: String = "",
        generatedStatus: String = "",
        notes: String = "candidate only",
    ): ImageCandidateExternalResultItem =
        ImageCandidateExternalResultItem(
            resultItemId = resultItemId,
            promptItemId = promptItemId,
            sourceCandidateId = sourceCandidateId,
            assetType = "prop",
            candidateStatus = "external_result_candidate",
            localImagePath = localImagePath,
            metadataOnlyReference = metadataOnlyReference,
            sourceFileSha256 = sourceFileSha256,
            doNotShowChecklist = listOf("no hidden truth shown"),
            driftReviewChecklist = listOf("matches prompt item public visual requirement"),
            residualRisks = listOf("needs later human acceptance before any registry work"),
            notes = notes,
            providerJobId = providerJobId,
            apiResponse = apiResponse,
            remoteUrl = remoteUrl,
            generatedStatus = generatedStatus,
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
