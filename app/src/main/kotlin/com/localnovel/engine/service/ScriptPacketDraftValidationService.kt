package com.localnovel.engine.service

import com.localnovel.engine.model.ScriptPacketActionLine
import com.localnovel.engine.model.ScriptPacketAssetNeed
import com.localnovel.engine.model.ScriptPacketBeat
import com.localnovel.engine.model.ScriptPacketDialogueLine
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketDraftFinding
import com.localnovel.engine.model.ScriptPacketDraftValidationReport
import com.localnovel.engine.model.ScriptPacketFrameCandidate
import com.localnovel.engine.model.ScriptPacketInteractionChoice
import com.localnovel.engine.model.ScriptPacketSegment

class ScriptPacketDraftValidationService {
    private val allowedSceneReferenceTypes = setOf("approvedscene", "prologuebridge", "scenecandidate")
    private val draftStatuses = setOf("draft", "review_draft", "manual_draft", "converted_draft")
    private val pretendedFinalStatuses = setOf("approved", "final", "pass", "review_passed")
    private val approvalLockStatuses = setOf("approved", "locked", "final")
    private val authorOnlyLeakNeedles = listOf(
        "author-only",
        "author only",
        "director goal",
        "hidden culprit",
        "sealed causal",
        "future plot",
        "never prompt",
        "作者祕",
        "作者秘",
        "導演目標",
        "隱藏真兇",
        "真兇",
        "未來劇情",
    )

    fun validate(packet: ScriptPacketDraft, packetFile: String = ""): ScriptPacketDraftValidationReport {
        val findings = mutableListOf<ScriptPacketDraftFinding>()

        requireNonBlank(
            findings = findings,
            value = packet.packetId,
            path = "$.packetId",
            ruleId = "PACKET_ID_MISSING",
            severity = "WARNING",
            message = "Draft packet should carry a stable packetId for review and handoff.",
            suggestedFix = "Add a draft-scoped packetId such as script-packet-draft-001.",
        )
        validateSource(packet, findings)

        if (packet.segments.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "SEGMENTS_EMPTY",
                    category = "structure",
                    severity = "CRITICAL",
                    path = "$.segments",
                    message = "A script packet draft must contain at least one segment.",
                    suggestedFix = "Extract at least one scene segment before storyboard or asset review.",
                ),
            )
        }

        val allBeatIds = packet.segments
            .flatMap { it.beatList }
            .map { it.beatId.trim() }
            .filter { it.isNotBlank() }
            .toSet()

        packet.segments.forEachIndexed { index, segment ->
            validateSegment(segment, "$.segments[$index]", findings)
        }
        packet.assetNeeds.forEachIndexed { index, assetNeed ->
            validateAssetNeed(assetNeed, "$.assetNeeds[$index]", findings)
        }
        packet.storyboardNotes.frameCandidates.forEachIndexed { index, frame ->
            validateFrameCandidate(frame, "$.storyboardNotes.frameCandidates[$index]", allBeatIds, findings)
        }
        if (packet.storyboardNotes.frameCandidates.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "STORYBOARD_FRAME_CANDIDATES_EMPTY",
                    category = "storyboard",
                    severity = "WARNING",
                    path = "$.storyboardNotes.frameCandidates",
                    message = "The draft has no frame candidates for future storyboard pickup.",
                    suggestedFix = "Add at least one frame candidate tied to a beatId when the segment is intended for storyboard review.",
                ),
            )
        }

        val hasAuthorOnlyExtracted = packet.segments.any { segment ->
            segment.knowledgePass.authorOnlyExtracted.any { it.isNotBlank() }
        }
        if (hasAuthorOnlyExtracted && !packet.secrecyKnowledgeNotes.authorOnlyExcluded) {
            findings.add(
                finding(
                    ruleId = "AUTHOR_ONLY_NOT_EXCLUDED",
                    category = "secrecy",
                    severity = "CRITICAL",
                    path = "$.secrecyKnowledgeNotes.authorOnlyExcluded",
                    message = "The draft records author-only extracted notes but does not mark them as excluded from user-facing packet surfaces.",
                    suggestedFix = "Set authorOnlyExcluded=true after moving author-only material out of visible script, storyboard, dialogue, and choice fields.",
                ),
            )
        }

        validateReviewSummary(packet, findings)
        scanUserFacingText(packet, findings)

        val verdict = when {
            findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" } -> "FAIL"
            findings.any { it.severity == "WARNING" } -> "PASS_WITH_WARNINGS"
            else -> "PASS"
        }

        return ScriptPacketDraftValidationReport(
            verdict = verdict,
            packetFile = packetFile,
            packetId = packet.packetId,
            segmentCount = packet.segments.size,
            findingCount = findings.size,
            findings = findings,
        )
    }

    private fun validateSource(packet: ScriptPacketDraft, findings: MutableList<ScriptPacketDraftFinding>) {
        requireNonBlank(
            findings = findings,
            value = packet.source.inputType,
            path = "$.source.inputType",
            ruleId = "SOURCE_INPUT_TYPE_MISSING",
            severity = "MAJOR",
            message = "The draft source must identify whether it came from prose, a novel chapter, author notes, or runtime narrative.",
            suggestedFix = "Set source.inputType to a clear source kind such as novelChapter, authorNotes, or turnNarrative.",
        )
        val status = packet.source.transformationStatus.normalized()
        when {
            status.isBlank() -> findings.add(
                finding(
                    ruleId = "SOURCE_TRANSFORMATION_STATUS_MISSING",
                    category = "structure",
                    severity = "MAJOR",
                    path = "$.source.transformationStatus",
                    message = "The source transformation status is missing.",
                    suggestedFix = "Use a draft status such as draft, review_draft, manual_draft, or converted_draft.",
                ),
            )
            status in pretendedFinalStatuses -> findings.add(
                finding(
                    ruleId = "DRAFT_MARKED_FINAL",
                    category = "review",
                    severity = "MAJOR",
                    path = "$.source.transformationStatus",
                    message = "A script packet draft must not mark itself as approved or final.",
                    evidence = listOf(packet.source.transformationStatus),
                    suggestedFix = "Use a draft status until a separate human review gate approves the packet.",
                ),
            )
            status !in draftStatuses -> findings.add(
                finding(
                    ruleId = "SOURCE_TRANSFORMATION_STATUS_UNKNOWN",
                    category = "structure",
                    severity = "WARNING",
                    path = "$.source.transformationStatus",
                    message = "The transformation status is not one of the known draft statuses.",
                    evidence = listOf(packet.source.transformationStatus),
                    suggestedFix = "Prefer draft, review_draft, manual_draft, or converted_draft.",
                ),
            )
        }
    }

    private fun validateSegment(
        segment: ScriptPacketSegment,
        path: String,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        requireNonBlank(findings, segment.segmentId, "$path.segmentId", "SEGMENT_ID_MISSING", "MAJOR", "Segment is missing a stable id.")
        validateSceneReference(segment, path, findings)
        requireNonBlank(findings, segment.scenePurpose, "$path.scenePurpose", "SCENE_PURPOSE_MISSING", "MAJOR", "Segment is missing a scene purpose.")
        requireNonBlank(findings, segment.playerObjective, "$path.playerObjective", "PLAYER_OBJECTIVE_MISSING", "MAJOR", "Segment is missing the player's local objective.")
        requireNonBlank(findings, segment.locationZone.zoneId, "$path.locationZone.zoneId", "LOCATION_ZONE_ID_MISSING", "MAJOR", "Segment is missing a location zone id.")
        requireNonBlank(findings, segment.locationZone.publicName, "$path.locationZone.publicName", "LOCATION_ZONE_NAME_MISSING", "MAJOR", "Segment is missing a public location zone name.")
        requireNonBlank(findings, segment.visualSetup.publicDescription, "$path.visualSetup.publicDescription", "VISUAL_SETUP_MISSING", "MAJOR", "Segment is missing public visual setup.")

        if (segment.knowledgePass.publicFacts.isEmpty() && segment.knowledgePass.characterKnowledgeClaims.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "KNOWLEDGE_PASS_EMPTY",
                    category = "knowledge",
                    severity = "WARNING",
                    path = "$path.knowledgePass",
                    message = "The segment has no public facts or character knowledge claims.",
                    suggestedFix = "List what is publicly visible and what each actor is allowed to know before dialogue or choice review.",
                ),
            )
        }

        if (segment.beatList.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "BEAT_LIST_EMPTY",
                    category = "structure",
                    severity = "CRITICAL",
                    path = "$path.beatList",
                    message = "A segment must contain at least one visible beat.",
                    suggestedFix = "Extract scene beats before writing action, dialogue, choice, or storyboard fields.",
                ),
            )
        }
        val seenBeatIds = mutableSetOf<String>()
        segment.beatList.forEachIndexed { index, beat ->
            validateBeat(beat, "$path.beatList[$index]", seenBeatIds, findings)
        }

        if (segment.actionLines.isEmpty()) {
            findings.add(
                finding(
                    ruleId = "ACTION_LINES_EMPTY",
                    category = "script",
                    severity = "WARNING",
                    path = "$path.actionLines",
                    message = "The segment has no action lines for production-facing blocking.",
                    suggestedFix = "Add at least one action line when the segment is intended for script or storyboard review.",
                ),
            )
        }
        segment.actionLines.forEachIndexed { index, actionLine ->
            validateActionLine(actionLine, "$path.actionLines[$index]", findings)
        }
        segment.dialogueLines.forEachIndexed { index, dialogueLine ->
            validateDialogueLine(dialogueLine, "$path.dialogueLines[$index]", findings)
        }
        segment.interactionChoices.forEachIndexed { index, choice ->
            validateInteractionChoice(choice, "$path.interactionChoices[$index]", findings)
        }
    }

    private fun validateSceneReference(
        segment: ScriptPacketSegment,
        path: String,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        val type = segment.sceneReference.type.normalized()
        if (type.isBlank()) {
            findings.add(
                finding(
                    ruleId = "SCENE_REFERENCE_TYPE_MISSING",
                    category = "scene",
                    severity = "MAJOR",
                    path = "$path.sceneReference.type",
                    message = "The segment must say whether it references an approved scene, a prologue bridge, or a scene candidate.",
                    suggestedFix = "Set sceneReference.type to approvedScene, prologueBridge, or sceneCandidate.",
                ),
            )
            return
        }
        if (type !in allowedSceneReferenceTypes) {
            findings.add(
                finding(
                    ruleId = "SCENE_REFERENCE_TYPE_UNKNOWN",
                    category = "scene",
                    severity = "MAJOR",
                    path = "$path.sceneReference.type",
                    message = "The scene reference type is not supported by the draft validator.",
                    evidence = listOf(segment.sceneReference.type),
                    suggestedFix = "Use approvedScene, prologueBridge, or sceneCandidate.",
                ),
            )
        }
        if (type == "approvedscene" && segment.sceneReference.sceneId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "APPROVED_SCENE_ID_MISSING",
                    category = "scene",
                    severity = "MAJOR",
                    path = "$path.sceneReference.sceneId",
                    message = "Approved scene references must name the approved scene id.",
                    suggestedFix = "Provide sceneReference.sceneId or downgrade the reference to sceneCandidate.",
                ),
            )
        }
        if (type == "scenecandidate" && segment.sceneReference.candidateId.isBlank()) {
            findings.add(
                finding(
                    ruleId = "SCENE_CANDIDATE_ID_MISSING",
                    category = "scene",
                    severity = "WARNING",
                    path = "$path.sceneReference.candidateId",
                    message = "Scene candidates should carry a candidate id for later review.",
                    suggestedFix = "Add sceneReference.candidateId before handing the packet to scene registry review.",
                ),
            )
        }
    }

    private fun validateBeat(
        beat: ScriptPacketBeat,
        path: String,
        seenBeatIds: MutableSet<String>,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        requireNonBlank(findings, beat.beatId, "$path.beatId", "BEAT_ID_MISSING", "MAJOR", "Beat is missing a stable id.")
        val beatId = beat.beatId.trim()
        if (beatId.isNotBlank() && !seenBeatIds.add(beatId)) {
            findings.add(
                finding(
                    ruleId = "BEAT_ID_DUPLICATE",
                    category = "structure",
                    severity = "MAJOR",
                    path = "$path.beatId",
                    message = "Beat ids must be unique within a segment.",
                    evidence = listOf(beat.beatId),
                    suggestedFix = "Rename one duplicate beat id before storyboard frame linking.",
                ),
            )
        }
        requireNonBlank(
            findings = findings,
            value = beat.visibleAction,
            path = "$path.visibleAction",
            ruleId = "BEAT_VISIBLE_ACTION_MISSING",
            severity = "MAJOR",
            message = "A beat must describe what is visibly happening on screen or in scene.",
            suggestedFix = "Rewrite the beat as observable action instead of summary, mood, or plot intent.",
        )
        requireNonBlank(
            findings = findings,
            value = beat.causalLink,
            path = "$path.causalLink",
            ruleId = "BEAT_CAUSAL_LINK_MISSING",
            severity = "MAJOR",
            message = "A beat must explain why this action follows from prior state, character knowledge, or scene pressure.",
            suggestedFix = "Add a causal link tied to visible state, available knowledge, or a prior beat.",
        )
    }

    private fun validateActionLine(
        actionLine: ScriptPacketActionLine,
        path: String,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        requireNonBlank(findings, actionLine.actorId, "$path.actorId", "ACTION_ACTOR_MISSING", "MAJOR", "Action line is missing an actor id.")
        requireNonBlank(findings, actionLine.line, "$path.line", "ACTION_LINE_MISSING", "MAJOR", "Action line is missing the visible action text.")
        requireNonBlank(findings, actionLine.blockingZone, "$path.blockingZone", "ACTION_BLOCKING_ZONE_MISSING", "MAJOR", "Action line is missing a blocking zone.")
    }

    private fun validateDialogueLine(
        dialogueLine: ScriptPacketDialogueLine,
        path: String,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        requireNonBlank(findings, dialogueLine.speakerId, "$path.speakerId", "DIALOGUE_SPEAKER_MISSING", "MAJOR", "Dialogue line is missing a speaker id.")
        requireNonBlank(findings, dialogueLine.line, "$path.line", "DIALOGUE_LINE_MISSING", "MAJOR", "Dialogue line is missing text.")
        requireNonBlank(
            findings = findings,
            value = dialogueLine.knowledgeBoundary,
            path = "$path.knowledgeBoundary",
            ruleId = "DIALOGUE_KNOWLEDGE_BOUNDARY_MISSING",
            severity = "MAJOR",
            message = "Dialogue must state why the speaker is allowed to know or say this.",
            suggestedFix = "Tie dialogue to public facts, visible evidence, or explicit character knowledge claims.",
        )
    }

    private fun validateInteractionChoice(
        choice: ScriptPacketInteractionChoice,
        path: String,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        requireNonBlank(findings, choice.choiceId, "$path.choiceId", "CHOICE_ID_MISSING", "MAJOR", "Interaction choice is missing an id.")
        requireNonBlank(findings, choice.label, "$path.label", "CHOICE_LABEL_MISSING", "MAJOR", "Interaction choice is missing player-facing label text.")
        requireNonBlank(
            findings = findings,
            value = choice.allowedBecause,
            path = "$path.allowedBecause",
            ruleId = "CHOICE_ALLOWED_BECAUSE_MISSING",
            severity = "MAJOR",
            message = "Interaction choices must explain why the player can choose them in this scene state.",
            suggestedFix = "Tie the choice to visible constraints, player position, known facts, reachable items, or available actions.",
        )
    }

    private fun validateAssetNeed(
        assetNeed: ScriptPacketAssetNeed,
        path: String,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        requireNonBlank(findings, assetNeed.assetType, "$path.assetType", "ASSET_TYPE_MISSING", "MAJOR", "Asset need is missing an asset type.")
        if (assetNeed.existingAssetId.isBlank() && assetNeed.candidateName.isBlank()) {
            findings.add(
                finding(
                    ruleId = "ASSET_ID_OR_CANDIDATE_MISSING",
                    category = "asset",
                    severity = "MAJOR",
                    path = path,
                    message = "Asset needs must identify either an existing asset id or a candidate asset name.",
                    suggestedFix = "Provide existingAssetId for approved assets or candidateName for draft assets.",
                ),
            )
        }
        if (assetNeed.existingAssetId.isBlank() && assetNeed.candidateReason.isBlank()) {
            findings.add(
                finding(
                    ruleId = "ASSET_CANDIDATE_REASON_MISSING",
                    category = "asset",
                    severity = "WARNING",
                    path = "$path.candidateReason",
                    message = "Draft asset candidates should explain why the asset is needed.",
                    suggestedFix = "Add candidateReason so future asset review can accept, merge, or cut the candidate.",
                ),
            )
        }
        if (assetNeed.existingAssetId.isBlank() && assetNeed.lockStatus.normalized() in approvalLockStatuses) {
            findings.add(
                finding(
                    ruleId = "DRAFT_ASSET_MARKED_APPROVED",
                    category = "asset",
                    severity = "MAJOR",
                    path = "$path.lockStatus",
                    message = "Draft asset candidates must not mark themselves as approved, locked, or final.",
                    evidence = listOf(assetNeed.lockStatus),
                    suggestedFix = "Use candidate until a separate asset review process approves the asset.",
                ),
            )
        }
    }

    private fun validateFrameCandidate(
        frame: ScriptPacketFrameCandidate,
        path: String,
        allBeatIds: Set<String>,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        requireNonBlank(findings, frame.frameId, "$path.frameId", "FRAME_ID_MISSING", "WARNING", "Frame candidate is missing a stable id.")
        requireNonBlank(findings, frame.beatId, "$path.beatId", "FRAME_BEAT_ID_MISSING", "MAJOR", "Frame candidate is missing a beatId.")
        if (frame.beatId.isNotBlank() && frame.beatId !in allBeatIds) {
            findings.add(
                finding(
                    ruleId = "FRAME_BEAT_ID_UNKNOWN",
                    category = "storyboard",
                    severity = "MAJOR",
                    path = "$path.beatId",
                    message = "Frame candidate references a beatId that does not exist in the packet.",
                    evidence = listOf(frame.beatId),
                    suggestedFix = "Reference an existing beatId or add the missing beat before storyboard review.",
                ),
            )
        }
        requireNonBlank(findings, frame.framePurpose, "$path.framePurpose", "FRAME_PURPOSE_MISSING", "WARNING", "Frame candidate is missing its frame purpose.")
        requireNonBlank(findings, frame.cameraPurpose, "$path.cameraPurpose", "FRAME_CAMERA_PURPOSE_MISSING", "WARNING", "Frame candidate is missing camera purpose.")
        requireNonBlank(findings, frame.visibleSubject, "$path.visibleSubject", "FRAME_VISIBLE_SUBJECT_MISSING", "WARNING", "Frame candidate is missing visible subject.")
    }

    private fun validateReviewSummary(packet: ScriptPacketDraft, findings: MutableList<ScriptPacketDraftFinding>) {
        val verdict = packet.reviewSummary.verdict.normalized()
        if (verdict.isBlank()) {
            findings.add(
                finding(
                    ruleId = "REVIEW_VERDICT_MISSING",
                    category = "review",
                    severity = "WARNING",
                    path = "$.reviewSummary.verdict",
                    message = "The review summary should state that the packet is unreviewed or draft.",
                    suggestedFix = "Use unreviewed until a separate human review process records a result.",
                ),
            )
            return
        }
        if (verdict in pretendedFinalStatuses || verdict in setOf("approved", "locked")) {
            findings.add(
                finding(
                    ruleId = "DRAFT_REVIEW_VERDICT_PRETENDS_APPROVAL",
                    category = "review",
                    severity = "MAJOR",
                    path = "$.reviewSummary.verdict",
                    message = "The draft packet must not pretend to be formally approved.",
                    evidence = listOf(packet.reviewSummary.verdict),
                    suggestedFix = "Use unreviewed or draft until a separate review gate approves the packet.",
                ),
            )
        }
    }

    private fun scanUserFacingText(packet: ScriptPacketDraft, findings: MutableList<ScriptPacketDraftFinding>) {
        scanForLeak(packet.source.excerptSummary, "$.source.excerptSummary", findings)
        packet.segments.forEachIndexed { segmentIndex, segment ->
            val segmentPath = "$.segments[$segmentIndex]"
            scanForLeak(segment.scenePurpose, "$segmentPath.scenePurpose", findings)
            scanForLeak(segment.playerObjective, "$segmentPath.playerObjective", findings)
            scanForLeak(segment.locationZone.publicName, "$segmentPath.locationZone.publicName", findings)
            scanForLeak(segment.locationZone.constraints, "$segmentPath.locationZone.constraints", findings)
            scanForLeak(segment.visualSetup.publicDescription, "$segmentPath.visualSetup.publicDescription", findings)
            segment.visualSetup.mustShow.forEachIndexed { index, value ->
                scanForLeak(value, "$segmentPath.visualSetup.mustShow[$index]", findings)
            }
            segment.knowledgePass.publicFacts.forEachIndexed { index, value ->
                scanForLeak(value, "$segmentPath.knowledgePass.publicFacts[$index]", findings)
            }
            segment.knowledgePass.characterKnowledgeClaims.forEachIndexed { index, value ->
                scanForLeak(value, "$segmentPath.knowledgePass.characterKnowledgeClaims[$index]", findings)
            }
            segment.beatList.forEachIndexed { beatIndex, beat ->
                scanForLeak(beat.visibleAction, "$segmentPath.beatList[$beatIndex].visibleAction", findings)
                scanForLeak(beat.causalLink, "$segmentPath.beatList[$beatIndex].causalLink", findings)
                scanForLeak(beat.outcome, "$segmentPath.beatList[$beatIndex].outcome", findings)
            }
            segment.actionLines.forEachIndexed { actionIndex, action ->
                scanForLeak(action.line, "$segmentPath.actionLines[$actionIndex].line", findings)
                scanForLeak(action.blockingZone, "$segmentPath.actionLines[$actionIndex].blockingZone", findings)
                scanForLeak(action.visibleCost, "$segmentPath.actionLines[$actionIndex].visibleCost", findings)
            }
            segment.dialogueLines.forEachIndexed { dialogueIndex, dialogue ->
                scanForLeak(dialogue.line, "$segmentPath.dialogueLines[$dialogueIndex].line", findings)
                scanForLeak(dialogue.knowledgeBoundary, "$segmentPath.dialogueLines[$dialogueIndex].knowledgeBoundary", findings)
            }
            segment.interactionChoices.forEachIndexed { choiceIndex, choice ->
                scanForLeak(choice.label, "$segmentPath.interactionChoices[$choiceIndex].label", findings)
                scanForLeak(choice.allowedBecause, "$segmentPath.interactionChoices[$choiceIndex].allowedBecause", findings)
                scanForLeak(choice.likelyStateEffect, "$segmentPath.interactionChoices[$choiceIndex].likelyStateEffect", findings)
            }
        }
        packet.assetNeeds.forEachIndexed { index, assetNeed ->
            scanForLeak(assetNeed.candidateName, "$.assetNeeds[$index].candidateName", findings)
            scanForLeak(assetNeed.candidateReason, "$.assetNeeds[$index].candidateReason", findings)
        }
        packet.storyboardNotes.frameCandidates.forEachIndexed { index, frame ->
            scanForLeak(frame.framePurpose, "$.storyboardNotes.frameCandidates[$index].framePurpose", findings)
            scanForLeak(frame.cameraPurpose, "$.storyboardNotes.frameCandidates[$index].cameraPurpose", findings)
            scanForLeak(frame.visibleSubject, "$.storyboardNotes.frameCandidates[$index].visibleSubject", findings)
        }
    }

    private fun scanForLeak(
        value: String,
        path: String,
        findings: MutableList<ScriptPacketDraftFinding>,
    ) {
        if (value.isBlank()) return
        val lower = value.lowercase()
        val hits = authorOnlyLeakNeedles.filter { lower.contains(it.lowercase()) }
        if (hits.isEmpty()) return
        findings.add(
            finding(
                ruleId = "AUTHOR_ONLY_LEAK_RISK",
                category = "secrecy",
                severity = "CRITICAL",
                path = path,
                message = "A user-facing or production-facing field appears to contain author-only or future-plot language.",
                evidence = hits.distinct(),
                suggestedFix = "Move secret material into review-only notes and rewrite the visible field as observable action, public evidence, or allowed character knowledge.",
            ),
        )
    }

    private fun requireNonBlank(
        findings: MutableList<ScriptPacketDraftFinding>,
        value: String,
        path: String,
        ruleId: String,
        severity: String,
        message: String,
        suggestedFix: String = "Fill this field before handing the draft to review.",
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

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): ScriptPacketDraftFinding =
        ScriptPacketDraftFinding(
            ruleId = ruleId,
            category = category,
            severity = severity,
            path = path,
            message = message,
            evidence = evidence,
            suggestedFix = suggestedFix,
        )

    private fun String.normalized(): String = trim().lowercase()
}
