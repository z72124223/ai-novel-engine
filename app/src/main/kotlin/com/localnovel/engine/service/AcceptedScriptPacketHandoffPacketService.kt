package com.localnovel.engine.service

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.AcceptedScriptPacketHandoffEvidence
import com.localnovel.engine.model.AcceptedScriptPacketHandoffFinding
import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.AcceptedScriptPacketSourceReports
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftStructureBridgeReport
import java.security.MessageDigest

class AcceptedScriptPacketHandoffPacketService {
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")
    private val nextAllowedDownstreamStep = "storyboard_planning_preflight"

    fun build(
        scriptPacketDraftText: String,
        scriptPacketDraft: ScriptPacketDraft,
        structureBridgeReport: ScriptPacketDraftStructureBridgeReport,
        scriptPacketDraftFile: String = "",
        structureBridgeReportFile: String = "",
        packetId: String = "",
    ): AcceptedScriptPacketHandoffPacket {
        val findings = mutableListOf<AcceptedScriptPacketHandoffFinding>()
        val bridgeVerdict = structureBridgeReport.verdict.normalizedVerdict()
        val sourceBindingVerdict = structureBridgeReport.sourceBindingVerdict.normalizedVerdict()
        val structureVerdict = structureBridgeReport.structureVerdict.normalizedVerdict()
        val resolvedPacketId = packetId.ifBlank {
            scriptPacketDraft.packetId.takeIf { it.isNotBlank() }?.let { "accepted-script-packet-$it" }.orEmpty()
        }

        validateRequiredString(
            value = scriptPacketDraftText,
            path = "$.scriptPacketDraft",
            ruleId = "SCRIPT_PACKET_DRAFT_EMPTY",
            message = "Accepted script packet handoff cannot be built from an empty script packet draft file.",
            suggestedFix = "Provide the same script packet draft that passed structure bridge validation.",
            severity = "CRITICAL",
            findings = findings,
        )
        validateRequiredString(
            value = resolvedPacketId,
            path = "$.packetId",
            ruleId = "PACKET_ID_MISSING",
            message = "Accepted script packet handoff should carry a stable packetId.",
            suggestedFix = "Use a script packet draft with packetId or pass a packet id when this gate grows that option.",
            severity = "WARNING",
            findings = findings,
        )
        validateBridgeVerdict(bridgeVerdict, structureBridgeReport.verdict, findings)

        if (!structureBridgeReport.downstreamAllowed) {
            findings.add(
                finding(
                    ruleId = "STRUCTURE_BRIDGE_DOWNSTREAM_BLOCKED",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.structureBridgeReport.downstreamAllowed",
                    message = "Accepted script packet handoff requires structure bridge downstreamAllowed=true.",
                    evidence = listOf(structureBridgeReport.blockedReason).filter { it.isNotBlank() },
                    suggestedFix = "Fix the bridge blocked reason before creating an accepted script packet handoff packet.",
                ),
            )
        }
        if (sourceBindingVerdict != "PASS") {
            findings.add(
                finding(
                    ruleId = "SOURCE_BINDING_VERDICT_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.structureBridgeReport.sourceBindingVerdict",
                    message = "Accepted script packet handoff requires source-binding verdict PASS.",
                    evidence = listOf(structureBridgeReport.sourceBindingVerdict),
                    suggestedFix = "Rerun the source-binding and structure bridge gates with a passing script packet draft.",
                ),
            )
        }
        if (structureVerdict !in setOf("PASS", "PASS_WITH_WARNINGS")) {
            findings.add(
                finding(
                    ruleId = "STRUCTURE_VERDICT_NOT_ACCEPTABLE",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.structureBridgeReport.structureVerdict",
                    message = "Accepted script packet handoff requires structure verdict PASS or PASS_WITH_WARNINGS.",
                    evidence = listOf(structureBridgeReport.structureVerdict),
                    suggestedFix = "Fix script packet structure before creating the handoff packet.",
                ),
            )
        }
        validateSourceLinkage(scriptPacketDraft, structureBridgeReport, scriptPacketDraftFile, findings)

        val protectedAnchors = scriptPacketDraft.storyboardNotes.continuityLocks
            .filter { it.isNotBlank() }
            .distinct()
        if (protectedAnchors.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHORS_SUMMARY_EMPTY",
                    category = "continuity",
                    severity = "MAJOR",
                    path = "$.scriptPacketDraft.storyboardNotes.continuityLocks",
                    message = "Accepted script packet handoff must carry a protected continuity anchor summary.",
                    suggestedFix = "Preserve protected continuity anchors in storyboardNotes.continuityLocks before handoff.",
                ),
            )
        }

        val forbiddenClaimCount = validateForbiddenClaims(scriptPacketDraft, findings)
        val residualRisks = residualRisks(structureBridgeReport, forbiddenClaimCount)
        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return AcceptedScriptPacketHandoffPacket(
            verdict = verdict,
            packetId = resolvedPacketId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = sha256(scriptPacketDraftText),
            sourceReports = AcceptedScriptPacketSourceReports(
                structureBridgeReportFile = structureBridgeReportFile,
                structureBridgeVerdict = structureBridgeReport.verdict,
                sourceBindingVerdict = structureBridgeReport.sourceBindingVerdict,
                structureVerdict = structureBridgeReport.structureVerdict,
                scriptPacketPreflightReportFile = structureBridgeReport.scriptPacketPreflightFile,
                acceptedDraftPacketFile = structureBridgeReport.acceptedDraftPacketFile,
            ),
            protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(
                protectedContinuityAnchorCount = protectedAnchors.size,
                protectedContinuityAnchors = protectedAnchors,
            ),
            remainingResidualRisks = residualRisks,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            findingCount = findings.size,
            findings = findings,
            evidence = AcceptedScriptPacketHandoffEvidence(
                bridgeDownstreamAllowed = structureBridgeReport.downstreamAllowed,
                scriptPacketCharacters = scriptPacketDraftText.length,
                protectedContinuityAnchorCount = protectedAnchors.size,
                sourceBindingFindingCount = structureBridgeReport.sourceBindingFindingCount,
                structureFindingCount = structureBridgeReport.structureFindingCount,
                forbiddenClaimCount = forbiddenClaimCount,
                residualRiskCount = residualRisks.size,
            ),
        )
    }

    private fun validateBridgeVerdict(
        verdict: String,
        originalVerdict: String,
        findings: MutableList<AcceptedScriptPacketHandoffFinding>,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding(
                    ruleId = "STRUCTURE_BRIDGE_VERDICT_MISSING",
                    category = "upstream",
                    severity = "MAJOR",
                    path = "$.structureBridgeReport.verdict",
                    message = "Structure bridge report has no verdict.",
                    suggestedFix = "Rerun validate-script-packet-structure-bridge before creating this handoff packet.",
                ),
            )
            verdict !in knownVerdicts -> findings.add(
                finding(
                    ruleId = "STRUCTURE_BRIDGE_VERDICT_UNKNOWN",
                    category = "upstream",
                    severity = "MAJOR",
                    path = "$.structureBridgeReport.verdict",
                    message = "Structure bridge report has an unknown verdict.",
                    evidence = listOf(originalVerdict),
                    suggestedFix = "Use a report with verdict PASS, PASS_WITH_WARNINGS, or FAIL.",
                ),
            )
            verdict != "PASS" -> findings.add(
                finding(
                    ruleId = "STRUCTURE_BRIDGE_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.structureBridgeReport.verdict",
                    message = "Accepted script packet handoff requires structure bridge verdict PASS.",
                    evidence = listOf(originalVerdict),
                    suggestedFix = "Fix and rerun structure bridge validation before creating this handoff packet.",
                ),
            )
        }
    }

    private fun validateSourceLinkage(
        scriptPacketDraft: ScriptPacketDraft,
        structureBridgeReport: ScriptPacketDraftStructureBridgeReport,
        scriptPacketDraftFile: String,
        findings: MutableList<AcceptedScriptPacketHandoffFinding>,
    ) {
        if (structureBridgeReport.scriptPacketPreflightFile.isBlank()) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_PREFLIGHT_LINK_MISSING",
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = "$.structureBridgeReport.scriptPacketPreflightFile",
                    message = "Accepted script packet handoff must retain the script packet preflight report file path.",
                    suggestedFix = "Create the handoff packet from a bridge report produced by the CLI with --script-packet-preflight.",
                ),
            )
        }
        if (structureBridgeReport.acceptedDraftPacketFile.isBlank()) {
            findings.add(
                finding(
                    ruleId = "ACCEPTED_DRAFT_PACKET_LINK_MISSING",
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = "$.structureBridgeReport.acceptedDraftPacketFile",
                    message = "Accepted script packet handoff must retain the source accepted draft packet file path.",
                    suggestedFix = "Create the handoff packet from a bridge report produced by the CLI with --accepted-draft-packet.",
                ),
            )
        }
        if (structureBridgeReport.scriptPacketDraftFile.isNotBlank() &&
            scriptPacketDraftFile.isNotBlank() &&
            normalizePath(structureBridgeReport.scriptPacketDraftFile) != normalizePath(scriptPacketDraftFile)
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_FILE_MISMATCH",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.scriptPacketDraftFile",
                    message = "Input script packet draft path does not match the structure bridge report.",
                    evidence = listOf(scriptPacketDraftFile, structureBridgeReport.scriptPacketDraftFile),
                    suggestedFix = "Use the same script packet draft file that produced the bridge report.",
                ),
            )
        }
        if (structureBridgeReport.scriptPacketId.isNotBlank() &&
            scriptPacketDraft.packetId.isNotBlank() &&
            structureBridgeReport.scriptPacketId != scriptPacketDraft.packetId
        ) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_ID_MISMATCH",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.scriptPacketDraft.packetId",
                    message = "Input script packet draft packetId does not match the structure bridge report.",
                    evidence = listOf(scriptPacketDraft.packetId, structureBridgeReport.scriptPacketId),
                    suggestedFix = "Use the same script packet draft content that produced the bridge report.",
                ),
            )
        }
    }

    private fun validateForbiddenClaims(
        scriptPacketDraft: ScriptPacketDraft,
        findings: MutableList<AcceptedScriptPacketHandoffFinding>,
    ): Int {
        var count = 0
        collectTextFields(scriptPacketDraft).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                val hit = pattern.find(field.value) ?: return@forEach
                count += 1
                findings.add(
                    finding(
                        ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = field.path,
                        message = "Accepted script packet handoff must not claim generated storyboard, approved asset, final video, runtime scene, or setting pack canon status.",
                        evidence = listOf(hit.value.take(160)),
                        suggestedFix = "Keep the packet as a storyboard-planning preflight handoff only.",
                    ),
                )
            }
        }
        return count
    }

    private fun residualRisks(
        structureBridgeReport: ScriptPacketDraftStructureBridgeReport,
        forbiddenClaimCount: Int,
    ): List<String> {
        val risks = mutableListOf(
            "Storyboard planning preflight has not yet validated shot continuity, asset readiness, image generation, video generation, or voice handoff.",
        )
        structureBridgeReport.sourceBindingFindings
            .filter { it.severity == "WARNING" }
            .mapTo(risks) { "Source-binding warning remains review-relevant: ${it.ruleId}" }
        structureBridgeReport.structureFindings
            .filter { it.severity == "WARNING" }
            .mapTo(risks) { "Script packet structure warning remains review-relevant: ${it.ruleId}" }
        if (structureBridgeReport.blockedReason.isNotBlank()) {
            risks.add("Bridge blocked reason remains review-relevant: ${structureBridgeReport.blockedReason}")
        }
        if (forbiddenClaimCount > 0) {
            risks.add("Forbidden downstream completion claims must be removed before storyboard planning.")
        }
        return risks.distinct()
    }

    private fun validateRequiredString(
        value: String,
        path: String,
        ruleId: String,
        message: String,
        suggestedFix: String,
        severity: String,
        findings: MutableList<AcceptedScriptPacketHandoffFinding>,
    ) {
        if (value.isBlank()) {
            findings.add(
                finding(
                    ruleId = ruleId,
                    category = "structure",
                    severity = severity,
                    path = path,
                    message = message,
                    suggestedFix = suggestedFix,
                ),
            )
        }
    }

    private fun collectTextFields(packet: ScriptPacketDraft): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields.add(TextField(path, value))
        }
        add("$.packetId", packet.packetId)
        add("$.source.inputType", packet.source.inputType)
        add("$.source.sourceId", packet.source.sourceId)
        add("$.source.sourceTitle", packet.source.sourceTitle)
        add("$.source.excerptSummary", packet.source.excerptSummary)
        add("$.source.transformationStatus", packet.source.transformationStatus)
        add("$.source.acceptedDraftPacketId", packet.source.acceptedDraftPacketId)
        add("$.source.acceptedDraftContentSha256", packet.source.acceptedDraftContentSha256)
        add("$.source.acceptedDraftFile", packet.source.acceptedDraftFile)
        packet.segments.forEachIndexed { segmentIndex, segment ->
            val segmentPath = "$.segments[$segmentIndex]"
            add("$segmentPath.segmentId", segment.segmentId)
            add("$segmentPath.sceneReference.type", segment.sceneReference.type)
            add("$segmentPath.sceneReference.sceneId", segment.sceneReference.sceneId)
            add("$segmentPath.sceneReference.candidateId", segment.sceneReference.candidateId)
            add("$segmentPath.sceneReference.notes", segment.sceneReference.notes)
            add("$segmentPath.scenePurpose", segment.scenePurpose)
            add("$segmentPath.playerObjective", segment.playerObjective)
            add("$segmentPath.locationZone.zoneId", segment.locationZone.zoneId)
            add("$segmentPath.locationZone.publicName", segment.locationZone.publicName)
            add("$segmentPath.locationZone.constraints", segment.locationZone.constraints)
            add("$segmentPath.visualSetup.publicDescription", segment.visualSetup.publicDescription)
            add("$segmentPath.visualSetup.cameraIntent", segment.visualSetup.cameraIntent)
            segment.visualSetup.mustShow.forEachIndexed { index, value ->
                add("$segmentPath.visualSetup.mustShow[$index]", value)
            }
            segment.visualSetup.mustNotShow.forEachIndexed { index, value ->
                add("$segmentPath.visualSetup.mustNotShow[$index]", value)
            }
            segment.knowledgePass.publicFacts.forEachIndexed { index, value ->
                add("$segmentPath.knowledgePass.publicFacts[$index]", value)
            }
            segment.knowledgePass.characterKnowledgeClaims.forEachIndexed { index, value ->
                add("$segmentPath.knowledgePass.characterKnowledgeClaims[$index]", value)
            }
            segment.knowledgePass.authorOnlyExtracted.forEachIndexed { index, value ->
                add("$segmentPath.knowledgePass.authorOnlyExtracted[$index]", value)
            }
            segment.beatList.forEachIndexed { beatIndex, beat ->
                add("$segmentPath.beatList[$beatIndex].beatId", beat.beatId)
                add("$segmentPath.beatList[$beatIndex].visibleAction", beat.visibleAction)
                add("$segmentPath.beatList[$beatIndex].causalLink", beat.causalLink)
                beat.characterIds.forEachIndexed { index, value ->
                    add("$segmentPath.beatList[$beatIndex].characterIds[$index]", value)
                }
                beat.requiredKnowledgeClaimIds.forEachIndexed { index, value ->
                    add("$segmentPath.beatList[$beatIndex].requiredKnowledgeClaimIds[$index]", value)
                }
                add("$segmentPath.beatList[$beatIndex].outcome", beat.outcome)
            }
            segment.actionLines.forEachIndexed { actionIndex, action ->
                add("$segmentPath.actionLines[$actionIndex].lineId", action.lineId)
                add("$segmentPath.actionLines[$actionIndex].actorId", action.actorId)
                add("$segmentPath.actionLines[$actionIndex].line", action.line)
                add("$segmentPath.actionLines[$actionIndex].blockingZone", action.blockingZone)
                add("$segmentPath.actionLines[$actionIndex].visibleCost", action.visibleCost)
            }
            segment.dialogueLines.forEachIndexed { dialogueIndex, dialogue ->
                add("$segmentPath.dialogueLines[$dialogueIndex].lineId", dialogue.lineId)
                add("$segmentPath.dialogueLines[$dialogueIndex].speakerId", dialogue.speakerId)
                add("$segmentPath.dialogueLines[$dialogueIndex].line", dialogue.line)
                add("$segmentPath.dialogueLines[$dialogueIndex].knowledgeBoundary", dialogue.knowledgeBoundary)
                add("$segmentPath.dialogueLines[$dialogueIndex].subtext", dialogue.subtext)
            }
            segment.interactionChoices.forEachIndexed { choiceIndex, choice ->
                add("$segmentPath.interactionChoices[$choiceIndex].choiceId", choice.choiceId)
                add("$segmentPath.interactionChoices[$choiceIndex].label", choice.label)
                add("$segmentPath.interactionChoices[$choiceIndex].allowedBecause", choice.allowedBecause)
                add("$segmentPath.interactionChoices[$choiceIndex].likelyStateEffect", choice.likelyStateEffect)
            }
        }
        add("$.storyboardNotes.cameraLanguage", packet.storyboardNotes.cameraLanguage)
        packet.storyboardNotes.continuityLocks.forEachIndexed { index, value ->
            add("$.storyboardNotes.continuityLocks[$index]", value)
        }
        packet.storyboardNotes.frameCandidates.forEachIndexed { index, frame ->
            add("$.storyboardNotes.frameCandidates[$index].frameId", frame.frameId)
            add("$.storyboardNotes.frameCandidates[$index].beatId", frame.beatId)
            add("$.storyboardNotes.frameCandidates[$index].framePurpose", frame.framePurpose)
            add("$.storyboardNotes.frameCandidates[$index].cameraPurpose", frame.cameraPurpose)
            add("$.storyboardNotes.frameCandidates[$index].visibleSubject", frame.visibleSubject)
            frame.doNotShow.forEachIndexed { doNotShowIndex, value ->
                add("$.storyboardNotes.frameCandidates[$index].doNotShow[$doNotShowIndex]", value)
            }
        }
        packet.assetNeeds.forEachIndexed { index, asset ->
            add("$.assetNeeds[$index].assetType", asset.assetType)
            add("$.assetNeeds[$index].existingAssetId", asset.existingAssetId)
            add("$.assetNeeds[$index].candidateName", asset.candidateName)
            add("$.assetNeeds[$index].candidateReason", asset.candidateReason)
            add("$.assetNeeds[$index].lockStatus", asset.lockStatus)
        }
        add("$.secrecyKnowledgeNotes.authorOnlyExcluded", packet.secrecyKnowledgeNotes.authorOnlyExcluded.toString())
        add("$.secrecyKnowledgeNotes.reviewOnlyNotes", packet.secrecyKnowledgeNotes.reviewOnlyNotes)
        add("$.reviewSummary.verdict", packet.reviewSummary.verdict)
        add("$.reviewSummary.reviewerNotes", packet.reviewSummary.reviewerNotes)
        return fields
    }

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private fun normalizePath(path: String): String =
        path.trim()
            .replace("\\", "/")
            .lowercase()

    private fun String.normalizedVerdict(): String =
        trim().uppercase()

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): AcceptedScriptPacketHandoffFinding =
        AcceptedScriptPacketHandoffFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private data class TextField(
        val path: String,
        val value: String,
    )

    private companion object {
        val forbiddenClaimPatterns = listOf(
            Regex("""(?i)\bstoryboard\s+(generated|created|complete|completed|approved|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|approved|final)\s+storyboard\b"""),
            Regex("""(?i)\bimage\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\b(generated|created|completed|final)\s+image\b"""),
            Regex("""(?i)\bapproved\s+assets?\b"""),
            Regex("""(?i)\bassets?\s+(?:are\s+)?approved\b"""),
            Regex("""(?i)\bassets?\s+approval\s+(?:is\s+)?(complete|completed|ready|done)\b"""),
            Regex("""(?i)\bfinal\s+video\b"""),
            Regex("""(?i)\bvideo\s+(generated|created|complete|completed|final)\b"""),
            Regex("""(?i)\bruntime\s+scene\b"""),
            Regex("""(?i)\bsetting\s+pack\s+canon\b"""),
            Regex("""(?i)\bis\s+canon\b"""),
            Regex("""(?i)\bcanonical\b"""),
            Regex("\u5df2\u7522\u751f\u5206\u93e1"),
            Regex("\u5df2\u751f\u6210\u5206\u93e1"),
            Regex("\u5df2\u6838\u51c6\u7d20\u6750"),
            Regex("\u6700\u7d42\u5f71\u7247"),
            Regex("\u57f7\u884c\u671f\u5834\u666f"),
            Regex("\u8a2d\u5b9a\u5305\u6b63\u5178"),
        )
    }
}
