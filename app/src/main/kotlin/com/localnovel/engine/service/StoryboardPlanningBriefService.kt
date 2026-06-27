package com.localnovel.engine.service

import com.localnovel.engine.model.AcceptedScriptPacketHandoffPacket
import com.localnovel.engine.model.StoryboardPlanningAssetCarryForward
import com.localnovel.engine.model.StoryboardPlanningBrief
import com.localnovel.engine.model.StoryboardPlanningBriefEvidence
import com.localnovel.engine.model.StoryboardPlanningBriefFinding
import com.localnovel.engine.model.StoryboardPlanningBriefSourceReports
import com.localnovel.engine.model.StoryboardPlanningFrameConstraints
import com.localnovel.engine.model.StoryboardPlanningPreflightReport
import com.localnovel.engine.model.StoryboardPlanningReviewChecklistItem
import com.localnovel.engine.model.StoryboardPlanningSceneBeatSummary

class StoryboardPlanningBriefService {
    private val requiredPreflightStep = "storyboard_planning_brief"
    private val nextAllowedDownstreamStep = "storyboard_packet_draft"
    private val sha256Regex = Regex("""[0-9a-f]{64}""")
    private val knownVerdicts = setOf("PASS", "PASS_WITH_WARNINGS", "FAIL")

    fun build(
        acceptedScriptPacket: AcceptedScriptPacketHandoffPacket,
        storyboardPlanningPreflight: StoryboardPlanningPreflightReport,
        briefId: String = "",
        acceptedScriptPacketFile: String = "",
        storyboardPlanningPreflightFile: String = "",
    ): StoryboardPlanningBrief {
        val findings = mutableListOf<StoryboardPlanningBriefFinding>()
        val acceptedVerdict = acceptedScriptPacket.verdict.normalizedVerdict()
        val preflightVerdict = storyboardPlanningPreflight.verdict.normalizedVerdict()
        val resolvedBriefId = briefId.ifBlank {
            acceptedScriptPacket.packetId.takeIf { it.isNotBlank() }?.let { "$it-storyboard-planning-brief" }.orEmpty()
        }

        requirePassVerdict(
            verdict = acceptedVerdict,
            originalVerdict = acceptedScriptPacket.verdict,
            path = "$.acceptedScriptPacket.verdict",
            source = "accepted script packet",
            missingRuleId = "ACCEPTED_SCRIPT_PACKET_VERDICT_MISSING",
            unknownRuleId = "ACCEPTED_SCRIPT_PACKET_VERDICT_UNKNOWN",
            failRuleId = "ACCEPTED_SCRIPT_PACKET_NOT_PASS",
            findings = findings,
        )
        requirePassVerdict(
            verdict = preflightVerdict,
            originalVerdict = storyboardPlanningPreflight.verdict,
            path = "$.storyboardPlanningPreflight.verdict",
            source = "storyboard planning preflight",
            missingRuleId = "STORYBOARD_PLANNING_PREFLIGHT_VERDICT_MISSING",
            unknownRuleId = "STORYBOARD_PLANNING_PREFLIGHT_VERDICT_UNKNOWN",
            failRuleId = "STORYBOARD_PLANNING_PREFLIGHT_NOT_PASS",
            findings = findings,
        )
        if (storyboardPlanningPreflight.nextAllowedDownstreamStep != requiredPreflightStep) {
            findings.add(
                finding(
                    ruleId = "PREFLIGHT_NEXT_STEP_NOT_PLANNING_BRIEF",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.storyboardPlanningPreflight.nextAllowedDownstreamStep",
                    message = "Storyboard planning brief requires preflight nextAllowedDownstreamStep=storyboard_planning_brief.",
                    evidence = listOf(storyboardPlanningPreflight.nextAllowedDownstreamStep),
                    suggestedFix = "Use the storyboard planning preflight report that passed into the planning brief stage.",
                ),
            )
        }
        val acceptedScriptPacketId = acceptedScriptPacket.packetId.trim()
        val preflightAcceptedScriptPacketId = storyboardPlanningPreflight.acceptedScriptPacketId.trim()
        if (acceptedScriptPacketId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "ACCEPTED_SCRIPT_PACKET_ID_MISSING",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.acceptedScriptPacket.packetId",
                    message = "Storyboard planning brief requires accepted script packet packetId.",
                    suggestedFix = "Recreate the accepted script packet handoff packet with a stable packetId.",
                ),
            )
        }
        if (preflightAcceptedScriptPacketId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "PREFLIGHT_ACCEPTED_SCRIPT_PACKET_ID_MISSING",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.storyboardPlanningPreflight.acceptedScriptPacketId",
                    message = "Storyboard planning preflight must identify the accepted script packet id it validated.",
                    suggestedFix = "Rerun storyboard planning preflight with the accepted script packet packetId preserved.",
                ),
            )
        }
        if (acceptedScriptPacketId.isNotBlank() &&
            preflightAcceptedScriptPacketId.isNotBlank() &&
            acceptedScriptPacketId != preflightAcceptedScriptPacketId
        ) {
            findings.add(
                finding(
                    ruleId = "ACCEPTED_SCRIPT_PACKET_ID_MISMATCH",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.storyboardPlanningPreflight.acceptedScriptPacketId",
                    message = "Storyboard planning preflight points at a different accepted script packet id.",
                    evidence = listOf(acceptedScriptPacketId, preflightAcceptedScriptPacketId),
                    suggestedFix = "Use the matching accepted script packet and storyboard planning preflight report.",
                ),
            )
        }
        validateAcceptedPacketSources(acceptedScriptPacket, findings)
        validatePreflightLinks(storyboardPlanningPreflight, acceptedScriptPacketFile, findings)

        val forbiddenClaimCount = validateForbiddenClaims(acceptedScriptPacket, storyboardPlanningPreflight, findings)
        val reviewChecklist = reviewChecklist()
        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }
        val blockedReason = if (verdict == "FAIL") {
            findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
        } else {
            ""
        }

        return StoryboardPlanningBrief(
            verdict = verdict,
            briefId = resolvedBriefId,
            acceptedScriptPacketFile = acceptedScriptPacketFile,
            storyboardPlanningPreflightFile = storyboardPlanningPreflightFile,
            acceptedScriptPacketId = acceptedScriptPacket.packetId,
            scriptPacketDraftFile = acceptedScriptPacket.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = acceptedScriptPacket.scriptPacketDraftContentSha256,
            sourceReports = StoryboardPlanningBriefSourceReports(
                acceptedScriptPacketVerdict = acceptedScriptPacket.verdict,
                storyboardPlanningPreflightReportFile = storyboardPlanningPreflightFile,
                storyboardPlanningPreflightVerdict = storyboardPlanningPreflight.verdict,
                structureBridgeReportFile = acceptedScriptPacket.sourceReports.structureBridgeReportFile,
                structureBridgeVerdict = acceptedScriptPacket.sourceReports.structureBridgeVerdict,
                sourceBindingVerdict = acceptedScriptPacket.sourceReports.sourceBindingVerdict,
                structureVerdict = acceptedScriptPacket.sourceReports.structureVerdict,
                scriptPacketPreflightReportFile = acceptedScriptPacket.sourceReports.scriptPacketPreflightReportFile,
                acceptedDraftPacketFile = acceptedScriptPacket.sourceReports.acceptedDraftPacketFile,
            ),
            protectedContinuityAnchorsSummary = acceptedScriptPacket.protectedContinuityAnchorsSummary,
            remainingResidualRisks = acceptedScriptPacket.remainingResidualRisks.filter { it.isNotBlank() }.distinct(),
            sceneBeatPlanningSummary = sceneBeatPlanningSummary(acceptedScriptPacket),
            framePlanningConstraints = framePlanningConstraints(acceptedScriptPacket),
            mustShowBoundary = acceptedScriptPacket.protectedContinuityAnchorsSummary.protectedContinuityAnchors
                .filter { it.isNotBlank() }
                .distinct(),
            mustNotShowBoundary = mustNotShowBoundary(),
            assetNeedsCarriedForward = assetCarryForward(acceptedScriptPacket),
            reviewChecklist = reviewChecklist,
            nextAllowedDownstreamStep = if (verdict == "FAIL") "" else nextAllowedDownstreamStep,
            blockedReason = blockedReason,
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardPlanningBriefEvidence(
                acceptedScriptPacketVerdict = acceptedScriptPacket.verdict,
                storyboardPlanningPreflightVerdict = storyboardPlanningPreflight.verdict,
                scriptPacketDraftHashPresent = sha256Regex.matches(acceptedScriptPacket.scriptPacketDraftContentSha256),
                sourceReportFileCount = sourceReportFileCount(acceptedScriptPacket, storyboardPlanningPreflightFile),
                sourceReportVerdictCount = sourceReportVerdictCount(acceptedScriptPacket, storyboardPlanningPreflight),
                protectedContinuityAnchorCount = acceptedScriptPacket.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount,
                residualRiskCount = acceptedScriptPacket.remainingResidualRisks.count { it.isNotBlank() },
                forbiddenClaimCount = forbiddenClaimCount,
                reviewChecklistCount = reviewChecklist.size,
            ),
        )
    }

    private fun validateAcceptedPacketSources(
        packet: AcceptedScriptPacketHandoffPacket,
        findings: MutableList<StoryboardPlanningBriefFinding>,
    ) {
        if (!sha256Regex.matches(packet.scriptPacketDraftContentSha256)) {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_HASH_INVALID",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.acceptedScriptPacket.scriptPacketDraftContentSha256",
                    message = "Storyboard planning brief requires a valid script packet draft SHA-256 hash.",
                    evidence = listOf(packet.scriptPacketDraftContentSha256),
                    suggestedFix = "Recreate the accepted script packet handoff packet from the script packet draft JSON file.",
                ),
            )
        }
        requireNonBlank(packet.scriptPacketDraftFile, "$.acceptedScriptPacket.scriptPacketDraftFile", "SCRIPT_PACKET_DRAFT_FILE_MISSING", "Accepted script packet must retain script packet draft path.", findings)
        requireNonBlank(packet.sourceReports.structureBridgeReportFile, "$.acceptedScriptPacket.sourceReports.structureBridgeReportFile", "STRUCTURE_BRIDGE_REPORT_FILE_MISSING", "Accepted script packet must retain structure bridge report path.", findings)
        requireNonBlank(packet.sourceReports.scriptPacketPreflightReportFile, "$.acceptedScriptPacket.sourceReports.scriptPacketPreflightReportFile", "SCRIPT_PACKET_PREFLIGHT_REPORT_FILE_MISSING", "Accepted script packet must retain script packet preflight report path.", findings)
        requireNonBlank(packet.sourceReports.acceptedDraftPacketFile, "$.acceptedScriptPacket.sourceReports.acceptedDraftPacketFile", "ACCEPTED_DRAFT_PACKET_FILE_MISSING", "Accepted script packet must retain accepted draft packet path.", findings)
        requireNonBlank(packet.sourceReports.structureBridgeVerdict, "$.acceptedScriptPacket.sourceReports.structureBridgeVerdict", "STRUCTURE_BRIDGE_VERDICT_MISSING", "Accepted script packet must retain structure bridge verdict.", findings)
        requireNonBlank(packet.sourceReports.sourceBindingVerdict, "$.acceptedScriptPacket.sourceReports.sourceBindingVerdict", "SOURCE_BINDING_VERDICT_MISSING", "Accepted script packet must retain source-binding verdict.", findings)
        requireNonBlank(packet.sourceReports.structureVerdict, "$.acceptedScriptPacket.sourceReports.structureVerdict", "STRUCTURE_VERDICT_MISSING", "Accepted script packet must retain structure verdict.", findings)

        val anchors = packet.protectedContinuityAnchorsSummary.protectedContinuityAnchors.filter { it.isNotBlank() }
        if (packet.protectedContinuityAnchorsSummary.protectedContinuityAnchorCount <= 0 || anchors.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "PROTECTED_CONTINUITY_ANCHORS_MISSING",
                    category = "continuity",
                    severity = "CRITICAL",
                    path = "$.acceptedScriptPacket.protectedContinuityAnchorsSummary",
                    message = "Storyboard planning brief requires protected continuity anchors.",
                    suggestedFix = "Recreate the accepted script packet handoff packet with protected continuity anchors.",
                ),
            )
        }
        if (packet.remainingResidualRisks.none { it.isNotBlank() }) {
            findings.add(
                finding(
                    ruleId = "RESIDUAL_RISKS_MISSING",
                    category = "risk",
                    severity = "MAJOR",
                    path = "$.acceptedScriptPacket.remainingResidualRisks",
                    message = "Storyboard planning brief must carry remaining residual risks.",
                    suggestedFix = "Recreate the accepted script packet handoff packet with downstream residual risks preserved.",
                ),
            )
        }
    }

    private fun validatePreflightLinks(
        preflight: StoryboardPlanningPreflightReport,
        acceptedScriptPacketFile: String,
        findings: MutableList<StoryboardPlanningBriefFinding>,
    ) {
        if (preflight.acceptedScriptPacketFile.isBlank()) {
            findings.add(
                finding(
                    ruleId = "PREFLIGHT_ACCEPTED_SCRIPT_PACKET_FILE_MISSING",
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = "$.storyboardPlanningPreflight.acceptedScriptPacketFile",
                    message = "Storyboard planning preflight must retain accepted script packet file path.",
                    suggestedFix = "Rerun storyboard planning preflight with the accepted script packet path.",
                ),
            )
        }
        if (preflight.acceptedScriptPacketFile.isNotBlank() &&
            acceptedScriptPacketFile.isNotBlank() &&
            normalizePath(preflight.acceptedScriptPacketFile) != normalizePath(acceptedScriptPacketFile)
        ) {
            findings.add(
                finding(
                    ruleId = "PREFLIGHT_ACCEPTED_SCRIPT_PACKET_FILE_MISMATCH",
                    category = "source_linkage",
                    severity = "CRITICAL",
                    path = "$.storyboardPlanningPreflight.acceptedScriptPacketFile",
                    message = "Storyboard planning preflight points at a different accepted script packet file.",
                    evidence = listOf(acceptedScriptPacketFile, preflight.acceptedScriptPacketFile),
                    suggestedFix = "Use the matching accepted script packet and preflight report.",
                ),
            )
        }
    }

    private fun validateForbiddenClaims(
        packet: AcceptedScriptPacketHandoffPacket,
        preflight: StoryboardPlanningPreflightReport,
        findings: MutableList<StoryboardPlanningBriefFinding>,
    ): Int {
        var count = 0
        collectTextFields(packet, preflight).forEach { field ->
            forbiddenClaimPatterns.forEach { pattern ->
                val hit = pattern.find(field.value) ?: return@forEach
                count += 1
                findings.add(
                    finding(
                        ruleId = "DOWNSTREAM_COMPLETION_CLAIM_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = field.path,
                        message = "Storyboard planning brief must not carry claims that storyboard, image, video, runtime scene, canon, or asset approval is complete.",
                        evidence = listOf(hit.value.take(160)),
                        suggestedFix = "Keep the brief as planning-only input for a future storyboard packet draft gate.",
                    ),
                )
            }
        }
        return count
    }

    private fun sceneBeatPlanningSummary(packet: AcceptedScriptPacketHandoffPacket): StoryboardPlanningSceneBeatSummary =
        StoryboardPlanningSceneBeatSummary(
            planningBasis = "accepted_script_packet_handoff",
            scriptPacketDraftFile = packet.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = packet.scriptPacketDraftContentSha256,
            notes = listOf(
                "Use the linked script packet draft as the source for future scene and beat extraction.",
                "This brief does not create storyboard frames or rewrite script content.",
            ),
        )

    private fun framePlanningConstraints(packet: AcceptedScriptPacketHandoffPacket): StoryboardPlanningFrameConstraints =
        StoryboardPlanningFrameConstraints(
            continuityPolicy = "Preserve protected continuity anchors before drafting any storyboard frame.",
            cameraPolicy = "Use only script packet camera/storyboard notes when the next gate reads the draft.",
            downstreamBoundary = "No image, video, runtime scene, canon, or asset approval is produced by this brief.",
        )

    private fun assetCarryForward(packet: AcceptedScriptPacketHandoffPacket): StoryboardPlanningAssetCarryForward =
        StoryboardPlanningAssetCarryForward(
            sourceScriptPacketDraftFile = packet.scriptPacketDraftFile,
            policy = "Carry script packet asset needs forward for future extraction; do not mark assets approved in this brief.",
        )

    private fun mustNotShowBoundary(): List<String> =
        listOf(
            "Do not invent storyboard frames in this brief.",
            "Do not generate images or video in this brief.",
            "Do not mark runtime scenes, canon, or assets as approved.",
            "Do not expose author-only or review-only material in future user-facing storyboard surfaces.",
        )

    private fun reviewChecklist(): List<StoryboardPlanningReviewChecklistItem> =
        listOf(
            StoryboardPlanningReviewChecklistItem(
                itemId = "storyboard-brief-check-source-links",
                category = "source_linkage",
                requiredCheck = "Verify accepted packet, preflight, script packet draft path/hash, and upstream report links before storyboard packet drafting.",
            ),
            StoryboardPlanningReviewChecklistItem(
                itemId = "storyboard-brief-check-continuity",
                category = "continuity",
                requiredCheck = "Carry protected continuity anchors into every future storyboard frame planning decision.",
            ),
            StoryboardPlanningReviewChecklistItem(
                itemId = "storyboard-brief-check-visual-boundary",
                category = "visual_boundary",
                requiredCheck = "Separate must-show and must-not-show constraints before drafting storyboard packet frames.",
            ),
            StoryboardPlanningReviewChecklistItem(
                itemId = "storyboard-brief-check-assets",
                category = "assets",
                requiredCheck = "Treat asset needs as candidates only; do not approve or finalize assets in this gate.",
            ),
            StoryboardPlanningReviewChecklistItem(
                itemId = "storyboard-brief-check-downstream",
                category = "downstream_boundary",
                requiredCheck = "Confirm no storyboard, image, video, runtime scene, canon, or asset approval completion claims are present.",
            ),
        )

    private fun collectTextFields(
        packet: AcceptedScriptPacketHandoffPacket,
        preflight: StoryboardPlanningPreflightReport,
    ): List<TextField> {
        val fields = mutableListOf<TextField>()
        fun add(path: String, value: String) {
            if (value.isNotBlank()) fields.add(TextField(path, value))
        }
        add("$.acceptedScriptPacket.packetId", packet.packetId)
        add("$.acceptedScriptPacket.scriptPacketDraftFile", packet.scriptPacketDraftFile)
        add("$.acceptedScriptPacket.sourceReports.structureBridgeReportFile", packet.sourceReports.structureBridgeReportFile)
        add("$.acceptedScriptPacket.sourceReports.scriptPacketPreflightReportFile", packet.sourceReports.scriptPacketPreflightReportFile)
        add("$.acceptedScriptPacket.sourceReports.acceptedDraftPacketFile", packet.sourceReports.acceptedDraftPacketFile)
        packet.protectedContinuityAnchorsSummary.protectedContinuityAnchors.forEachIndexed { index, value ->
            add("$.acceptedScriptPacket.protectedContinuityAnchorsSummary.protectedContinuityAnchors[$index]", value)
        }
        packet.remainingResidualRisks.forEachIndexed { index, value ->
            add("$.acceptedScriptPacket.remainingResidualRisks[$index]", value)
        }
        packet.findings.forEachIndexed { index, finding ->
            add("$.acceptedScriptPacket.findings[$index].message", finding.message)
            finding.evidence.forEachIndexed { evidenceIndex, value ->
                add("$.acceptedScriptPacket.findings[$index].evidence[$evidenceIndex]", value)
            }
            add("$.acceptedScriptPacket.findings[$index].suggestedFix", finding.suggestedFix)
        }
        add("$.storyboardPlanningPreflight.preflightId", preflight.preflightId)
        add("$.storyboardPlanningPreflight.acceptedScriptPacketFile", preflight.acceptedScriptPacketFile)
        add("$.storyboardPlanningPreflight.blockedReason", preflight.blockedReason)
        preflight.findings.forEachIndexed { index, finding ->
            add("$.storyboardPlanningPreflight.findings[$index].message", finding.message)
            finding.evidence.forEachIndexed { evidenceIndex, value ->
                add("$.storyboardPlanningPreflight.findings[$index].evidence[$evidenceIndex]", value)
            }
            add("$.storyboardPlanningPreflight.findings[$index].suggestedFix", finding.suggestedFix)
        }
        return fields
    }

    private fun requirePassVerdict(
        verdict: String,
        originalVerdict: String,
        path: String,
        source: String,
        missingRuleId: String,
        unknownRuleId: String,
        failRuleId: String,
        findings: MutableList<StoryboardPlanningBriefFinding>,
    ) {
        when {
            verdict.isBlank() -> findings.add(
                finding(missingRuleId, "upstream", "MAJOR", path, "Linked $source has no verdict.", suggestedFix = "Regenerate the upstream $source before creating a storyboard planning brief."),
            )
            verdict !in knownVerdicts -> findings.add(
                finding(unknownRuleId, "upstream", "MAJOR", path, "Linked $source has an unknown verdict.", evidence = listOf(originalVerdict), suggestedFix = "Use a report with verdict PASS, PASS_WITH_WARNINGS, or FAIL."),
            )
            verdict != "PASS" -> findings.add(
                finding(failRuleId, "upstream", "CRITICAL", path, "Storyboard planning brief requires linked $source PASS.", evidence = listOf(originalVerdict), suggestedFix = "Fix and rerun the upstream $source gate before creating a storyboard planning brief."),
            )
        }
    }

    private fun requireNonBlank(
        value: String,
        path: String,
        ruleId: String,
        message: String,
        findings: MutableList<StoryboardPlanningBriefFinding>,
    ) {
        if (value.isBlank()) {
            findings.add(
                finding(
                    ruleId = ruleId,
                    category = "source_linkage",
                    severity = "MAJOR",
                    path = path,
                    message = message,
                    suggestedFix = "Recreate the accepted script packet handoff packet and storyboard planning preflight from the required upstream files.",
                ),
            )
        }
    }

    private fun sourceReportFileCount(packet: AcceptedScriptPacketHandoffPacket, preflightFile: String): Int =
        listOf(
            preflightFile,
            packet.sourceReports.structureBridgeReportFile,
            packet.sourceReports.scriptPacketPreflightReportFile,
            packet.sourceReports.acceptedDraftPacketFile,
        ).count { it.isNotBlank() }

    private fun sourceReportVerdictCount(
        packet: AcceptedScriptPacketHandoffPacket,
        preflight: StoryboardPlanningPreflightReport,
    ): Int =
        listOf(
            packet.verdict,
            preflight.verdict,
            packet.sourceReports.structureBridgeVerdict,
            packet.sourceReports.sourceBindingVerdict,
            packet.sourceReports.structureVerdict,
        ).count { it.isNotBlank() }

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
    ): StoryboardPlanningBriefFinding =
        StoryboardPlanningBriefFinding(
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
