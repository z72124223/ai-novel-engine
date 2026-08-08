package com.localnovel.engine.service

import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationScriptPacketPreflightReport
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftSourceBindingEvidence
import com.localnovel.engine.model.ScriptPacketDraftSourceBindingFinding
import com.localnovel.engine.model.ScriptPacketDraftSourceBindingReport
import kotlin.math.ceil

class ScriptPacketDraftSourceBindingValidationService {
    fun validate(
        scriptPacketDraft: ScriptPacketDraft,
        scriptPacketPreflight: NovelizationScriptPacketPreflightReport,
        acceptedDraftPacket: NovelizationAcceptedDraftPacket,
        scriptPacketDraftFile: String = "",
        scriptPacketPreflightFile: String = "",
        acceptedDraftPacketFile: String = "",
    ): ScriptPacketDraftSourceBindingReport {
        val findings = mutableListOf<ScriptPacketDraftSourceBindingFinding>()

        if (scriptPacketPreflight.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_PREFLIGHT_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.scriptPacketPreflight.verdict",
                    message = "Script packet draft source binding requires script packet preflight PASS.",
                    evidence = listOf(scriptPacketPreflight.verdict),
                    suggestedFix = "Fix and rerun script packet preflight before validating source binding.",
                ),
            )
        }
        if (acceptedDraftPacket.verdict.trim().uppercase() != "PASS") {
            findings.add(
                finding(
                    ruleId = "ACCEPTED_DRAFT_PACKET_NOT_PASS",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.acceptedDraftPacket.verdict",
                    message = "Script packet draft source binding requires accepted draft packet PASS.",
                    evidence = listOf(acceptedDraftPacket.verdict),
                    suggestedFix = "Fix upstream accepted draft packet before validating a script packet draft.",
                ),
            )
        }
        if (scriptPacketPreflight.acceptedDraftPacketId.isNotBlank() &&
            acceptedDraftPacket.packetId.isNotBlank() &&
            scriptPacketPreflight.acceptedDraftPacketId != acceptedDraftPacket.packetId
        ) {
            findings.add(
                finding(
                    ruleId = "PREFLIGHT_ACCEPTED_PACKET_ID_MISMATCH",
                    category = "upstream",
                    severity = "CRITICAL",
                    path = "$.scriptPacketPreflight.acceptedDraftPacketId",
                    message = "Script packet preflight points at a different accepted draft packet id.",
                    evidence = listOf(scriptPacketPreflight.acceptedDraftPacketId, acceptedDraftPacket.packetId),
                    suggestedFix = "Use the matching preflight report for the accepted draft packet.",
                ),
            )
        }

        val bindingMatchedBy = sourceBindingMatches(scriptPacketDraft, acceptedDraftPacket)
        if (bindingMatchedBy.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ACCEPTED_DRAFT_SOURCE_BINDING_MISSING",
                    category = "source_binding",
                    severity = "CRITICAL",
                    path = "$.source",
                    message = "Script packet draft must cite the accepted draft packet id, revised draft content hash, or revised draft source path.",
                    suggestedFix = "Fill source.acceptedDraftPacketId, source.acceptedDraftContentSha256, source.acceptedDraftFile, or source.sourceId with verifiable accepted draft packet evidence.",
                ),
            )
        }

        val textFields = collectTextFields(scriptPacketDraft)
        val referencedAnchorCount = validateProtectedAnchors(acceptedDraftPacket, textFields, findings)
        val boundaryClaimCount = validateDownstreamBoundaryClaims(scriptPacketDraft, textFields, findings)

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return ScriptPacketDraftSourceBindingReport(
            verdict = verdict,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketPreflightFile = scriptPacketPreflightFile,
            acceptedDraftPacketFile = acceptedDraftPacketFile,
            scriptPacketId = scriptPacketDraft.packetId,
            acceptedDraftPacketId = acceptedDraftPacket.packetId,
            findingCount = findings.size,
            findings = findings,
            evidence = ScriptPacketDraftSourceBindingEvidence(
                preflightVerdict = scriptPacketPreflight.verdict,
                acceptedDraftPacketVerdict = acceptedDraftPacket.verdict,
                bindingMatchedBy = bindingMatchedBy,
                protectedContinuityAnchorCount = acceptedDraftPacket.protectedContinuityAnchors.count { it.isNotBlank() },
                referencedContinuityAnchorCount = referencedAnchorCount,
                boundaryClaimCount = boundaryClaimCount,
            ),
        )
    }

    private fun sourceBindingMatches(
        scriptPacketDraft: ScriptPacketDraft,
        acceptedDraftPacket: NovelizationAcceptedDraftPacket,
    ): List<String> {
        val source = scriptPacketDraft.source
        val sourceText = listOf(
            source.inputType,
            source.sourceId,
            source.sourceTitle,
            source.excerptSummary,
            source.acceptedDraftPacketId,
            source.acceptedDraftContentSha256,
            source.acceptedDraftFile,
        ).joinToString("\n")
        val normalizedSourceText = normalizeForSearch(sourceText)
        val matches = mutableListOf<String>()

        if (acceptedDraftPacket.packetId.isNotBlank()) {
            val id = acceptedDraftPacket.packetId.trim()
            if (source.acceptedDraftPacketId.trim() == id || source.sourceId.trim() == id || normalizedSourceText.contains(normalizeForSearch(id))) {
                matches.add("acceptedDraftPacketId")
            }
        }
        if (acceptedDraftPacket.revisedDraftContentSha256.isNotBlank()) {
            val hash = acceptedDraftPacket.revisedDraftContentSha256.trim()
            if (source.acceptedDraftContentSha256.trim() == hash || normalizedSourceText.contains(normalizeForSearch(hash))) {
                matches.add("revisedDraftContentSha256")
            }
        }
        if (acceptedDraftPacket.revisedDraftFile.isNotBlank()) {
            val acceptedPath = normalizePath(acceptedDraftPacket.revisedDraftFile)
            val sourcePath = normalizePath(source.acceptedDraftFile)
            val textPath = normalizePath(sourceText)
            if (sourcePath == acceptedPath || textPath.contains(acceptedPath)) {
                matches.add("revisedDraftFile")
            }
        }
        return matches.distinct()
    }

    private fun validateProtectedAnchors(
        acceptedDraftPacket: NovelizationAcceptedDraftPacket,
        textFields: List<TextField>,
        findings: MutableList<ScriptPacketDraftSourceBindingFinding>,
    ): Int {
        val combinedText = textFields.joinToString("\n") { it.value }
        var referencedCount = 0
        acceptedDraftPacket.protectedContinuityAnchors.filter { it.isNotBlank() }.forEachIndexed { index, anchor ->
            if (anchorReferenced(anchor, combinedText)) {
                referencedCount += 1
            } else {
                findings.add(
                    finding(
                        ruleId = "PROTECTED_CONTINUITY_ANCHOR_NOT_REFERENCED",
                        category = "continuity",
                        severity = "MAJOR",
                        path = "$.acceptedDraftPacket.protectedContinuityAnchors[$index]",
                        message = "Script packet draft does not preserve or explicitly reference a protected continuity anchor.",
                        evidence = listOf(anchor),
                        suggestedFix = "Add the anchor meaning to public facts, continuity locks, beats, action lines, or source notes before downstream storyboard/video work.",
                    ),
                )
            }
        }
        return referencedCount
    }

    private fun validateDownstreamBoundaryClaims(
        scriptPacketDraft: ScriptPacketDraft,
        textFields: List<TextField>,
        findings: MutableList<ScriptPacketDraftSourceBindingFinding>,
    ): Int {
        var count = 0
        textFields.forEach { field ->
            forbiddenBoundaryPatterns.forEach { pattern ->
                val hit = pattern.find(field.value) ?: return@forEach
                count += 1
                findings.add(
                    finding(
                        ruleId = "DOWNSTREAM_APPROVAL_CLAIM_FORBIDDEN",
                        category = "downstream_boundary",
                        severity = "MAJOR",
                        path = field.path,
                        message = "Script packet draft must not claim canon, runtime scene, approved storyboard, or final video status.",
                        evidence = listOf(hit.value.take(160)),
                        suggestedFix = "Rewrite the field as draft/source-binding language and leave approval to later gates.",
                    ),
                )
            }
        }
        val reviewVerdict = scriptPacketDraft.reviewSummary.verdict.trim().lowercase()
        if (reviewVerdict in setOf("approved", "final", "pass", "review_passed", "locked")) {
            count += 1
            findings.add(
                finding(
                    ruleId = "SCRIPT_PACKET_DRAFT_REVIEW_CLAIMS_APPROVAL",
                    category = "downstream_boundary",
                    severity = "MAJOR",
                    path = "$.reviewSummary.verdict",
                    message = "Script packet draft review summary must not claim approval or finality.",
                    evidence = listOf(scriptPacketDraft.reviewSummary.verdict),
                    suggestedFix = "Use unreviewed or draft until a separate approval gate runs.",
                ),
            )
        }
        return count
    }

    private fun anchorReferenced(anchor: String, combinedText: String): Boolean {
        val normalizedAnchor = normalizeForSearch(anchor)
        val normalizedText = normalizeForSearch(combinedText)
        if (normalizedAnchor.isNotBlank() && normalizedText.contains(normalizedAnchor)) {
            return true
        }
        val terms = significantTerms(anchor)
        if (terms.isEmpty()) return false
        val matched = terms.count { normalizedText.contains(it) }
        val required = if (terms.size <= 2) terms.size else ceil(terms.size * 0.55).toInt().coerceAtLeast(2)
        return matched >= required
    }

    private fun significantTerms(text: String): List<String> {
        val normalized = normalizeForSearch(text)
        val latinTerms = Regex("""[a-z0-9][a-z0-9'-]{2,}""")
            .findAll(normalized)
            .map { it.value }
            .filterNot { it in stopWords }
        val cjkTerms = Regex("""[\u4E00-\u9FFF]{2,}""")
            .findAll(normalized)
            .flatMap { match -> match.value.windowed(size = 2, step = 1).asSequence() }
        return (latinTerms + cjkTerms)
            .distinct()
            .take(32)
            .toList()
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
            add("$segmentPath.sceneReference.type", segment.sceneReference.type)
            add("$segmentPath.sceneReference.sceneId", segment.sceneReference.sceneId)
            add("$segmentPath.sceneReference.candidateId", segment.sceneReference.candidateId)
            add("$segmentPath.sceneReference.notes", segment.sceneReference.notes)
            add("$segmentPath.scenePurpose", segment.scenePurpose)
            add("$segmentPath.playerObjective", segment.playerObjective)
            add("$segmentPath.locationZone.publicName", segment.locationZone.publicName)
            add("$segmentPath.locationZone.constraints", segment.locationZone.constraints)
            add("$segmentPath.visualSetup.publicDescription", segment.visualSetup.publicDescription)
            add("$segmentPath.visualSetup.cameraIntent", segment.visualSetup.cameraIntent)
            segment.visualSetup.mustShow.forEachIndexed { index, value -> add("$segmentPath.visualSetup.mustShow[$index]", value) }
            segment.visualSetup.mustNotShow.forEachIndexed { index, value -> add("$segmentPath.visualSetup.mustNotShow[$index]", value) }
            segment.knowledgePass.publicFacts.forEachIndexed { index, value -> add("$segmentPath.knowledgePass.publicFacts[$index]", value) }
            segment.knowledgePass.characterKnowledgeClaims.forEachIndexed { index, value -> add("$segmentPath.knowledgePass.characterKnowledgeClaims[$index]", value) }
            segment.beatList.forEachIndexed { beatIndex, beat ->
                add("$segmentPath.beatList[$beatIndex].visibleAction", beat.visibleAction)
                add("$segmentPath.beatList[$beatIndex].causalLink", beat.causalLink)
                add("$segmentPath.beatList[$beatIndex].outcome", beat.outcome)
            }
            segment.actionLines.forEachIndexed { actionIndex, action ->
                add("$segmentPath.actionLines[$actionIndex].line", action.line)
                add("$segmentPath.actionLines[$actionIndex].blockingZone", action.blockingZone)
                add("$segmentPath.actionLines[$actionIndex].visibleCost", action.visibleCost)
            }
            segment.dialogueLines.forEachIndexed { dialogueIndex, dialogue ->
                add("$segmentPath.dialogueLines[$dialogueIndex].line", dialogue.line)
                add("$segmentPath.dialogueLines[$dialogueIndex].knowledgeBoundary", dialogue.knowledgeBoundary)
                add("$segmentPath.dialogueLines[$dialogueIndex].subtext", dialogue.subtext)
            }
            segment.interactionChoices.forEachIndexed { choiceIndex, choice ->
                add("$segmentPath.interactionChoices[$choiceIndex].label", choice.label)
                add("$segmentPath.interactionChoices[$choiceIndex].allowedBecause", choice.allowedBecause)
                add("$segmentPath.interactionChoices[$choiceIndex].likelyStateEffect", choice.likelyStateEffect)
            }
        }
        packet.storyboardNotes.continuityLocks.forEachIndexed { index, value -> add("$.storyboardNotes.continuityLocks[$index]", value) }
        packet.storyboardNotes.frameCandidates.forEachIndexed { index, frame ->
            add("$.storyboardNotes.frameCandidates[$index].framePurpose", frame.framePurpose)
            add("$.storyboardNotes.frameCandidates[$index].cameraPurpose", frame.cameraPurpose)
            add("$.storyboardNotes.frameCandidates[$index].visibleSubject", frame.visibleSubject)
        }
        add("$.secrecyKnowledgeNotes.reviewOnlyNotes", packet.secrecyKnowledgeNotes.reviewOnlyNotes)
        add("$.reviewSummary.verdict", packet.reviewSummary.verdict)
        add("$.reviewSummary.reviewerNotes", packet.reviewSummary.reviewerNotes)
        return fields
    }

    private fun normalizeForSearch(text: String): String =
        text.lowercase()
            .replace(Regex("""\s+"""), " ")
            .trim()

    private fun normalizePath(path: String): String =
        path.trim()
            .replace("\\", "/")
            .lowercase()

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ScriptPacketDraftSourceBindingFinding =
        ScriptPacketDraftSourceBindingFinding(
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
        val stopWords = setOf(
            "the",
            "and",
            "but",
            "for",
            "from",
            "that",
            "this",
            "with",
            "before",
            "after",
            "already",
            "requires",
        )
        val forbiddenBoundaryPatterns = listOf(
            Regex("""(?i)\bis\s+canon\b"""),
            Regex("""(?i)\bcanon\s+approved\b"""),
            Regex("""(?i)\bcanonical\b"""),
            Regex("""(?i)\bruntime\s+scene\b"""),
            Regex("""(?i)\bapproved\s+storyboard\b"""),
            Regex("""(?i)\bstoryboard\s+approved\b"""),
            Regex("""(?i)\bfinal\s+video\b"""),
            Regex("""正典"""),
            Regex("""執行期場景"""),
            Regex("""已核准分鏡"""),
            Regex("""最終影片"""),
        )
    }
}
