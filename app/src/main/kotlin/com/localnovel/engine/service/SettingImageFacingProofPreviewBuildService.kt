package com.localnovel.engine.service

import com.localnovel.engine.model.AcceptedScriptPacketContinuityAnchorsSummary
import com.localnovel.engine.model.ScriptPacketDraft
import com.localnovel.engine.model.ScriptPacketFrameCandidate
import com.localnovel.engine.model.ScriptPacketSegment
import com.localnovel.engine.model.SettingPackScriptPacketDraftBuildReport
import com.localnovel.engine.model.StoryboardPacketAssetCandidateDraft
import com.localnovel.engine.model.StoryboardPacketDraft
import com.localnovel.engine.model.StoryboardPacketFrameDraft
import com.localnovel.engine.model.StoryboardPlanningAssetCarryForward
import com.localnovel.engine.model.StoryboardPlanningBrief
import com.localnovel.engine.model.StoryboardPlanningBriefEvidence
import com.localnovel.engine.model.StoryboardPlanningBriefFinding
import com.localnovel.engine.model.StoryboardPlanningBriefSourceReports
import com.localnovel.engine.model.StoryboardPlanningFrameConstraints
import com.localnovel.engine.model.StoryboardPlanningReviewChecklistItem
import com.localnovel.engine.model.StoryboardPlanningSceneBeatSummary
import com.localnovel.engine.model.ScriptPacketDraftValidationReport
import java.security.MessageDigest

class SettingImageFacingProofPreviewBuildService {
    fun createPlanningBrief(
        scriptPacketDraft: ScriptPacketDraft,
        scriptPacketDraftText: String,
        scriptPacketDraftFile: String,
        scriptPacketValidation: ScriptPacketDraftValidationReport,
        settingBuildReport: SettingPackScriptPacketDraftBuildReport,
        settingBuildReportFile: String = "",
        briefId: String = "",
    ): StoryboardPlanningBrief {
        val findings = mutableListOf<StoryboardPlanningBriefFinding>()
        val validationPass = scriptPacketValidation.verdict.trim().uppercase() == "PASS"
        if (!validationPass) {
            findings += planningFinding(
                ruleId = "SCRIPT_PACKET_DRAFT_VALIDATION_NOT_PASS",
                category = "upstream",
                severity = "CRITICAL",
                path = "$.scriptPacketValidation.verdict",
                message = "Setting image-facing preview requires PASS script packet draft validation.",
                evidence = listOf(scriptPacketValidation.verdict),
            )
        }
        if (settingBuildReport.verdict.trim().uppercase() !in setOf("PASS", "PASS_WITH_WARNINGS")) {
            findings += planningFinding(
                ruleId = "SETTING_SCRIPT_DRAFT_BUILD_NOT_PASS",
                category = "upstream",
                severity = "CRITICAL",
                path = "$.settingScriptPacketDraftBuildReport.verdict",
                message = "Setting image-facing preview requires a non-failing setting script draft build report.",
                evidence = listOf(settingBuildReport.verdict),
            )
        }

        val anchors = scriptPacketDraft.storyboardNotes.continuityLocks
            .filter { it.isNotBlank() }
            .distinct()
        if (anchors.isEmpty()) {
            findings += planningFinding(
                ruleId = "SETTING_PREVIEW_CONTINUITY_ANCHORS_MISSING",
                category = "continuity",
                severity = "MAJOR",
                path = "$.scriptPacketDraft.storyboardNotes.continuityLocks",
                message = "Setting image-facing preview requires continuity locks for storyboard validation.",
            )
        }

        val verdict = if (findings.any { it.severity == "CRITICAL" || it.severity == "MAJOR" }) "FAIL" else "PASS"
        val scriptHash = sha256(scriptPacketDraftText)
        val resolvedBriefId = briefId.ifBlank {
            "setting-image-facing-preview-brief-${slug(scriptPacketDraft.packetId)}"
        }
        val localScriptSourceId = "setting-script-source-${slug(scriptPacketDraft.packetId)}"

        return StoryboardPlanningBrief(
            verdict = verdict,
            briefId = resolvedBriefId,
            acceptedScriptPacketFile = settingBuildReportFile.ifBlank {
                settingBuildReport.scriptPacketDraftFile
            },
            storyboardPlanningPreflightFile = settingBuildReport.scriptPacketValidationFile,
            acceptedScriptPacketId = localScriptSourceId,
            scriptPacketDraftFile = scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = scriptHash,
            sourceReports = StoryboardPlanningBriefSourceReports(
                acceptedScriptPacketVerdict = settingBuildReport.verdict,
                storyboardPlanningPreflightReportFile = settingBuildReport.scriptPacketValidationFile,
                storyboardPlanningPreflightVerdict = scriptPacketValidation.verdict,
                structureBridgeReportFile = settingBuildReportFile,
                structureBridgeVerdict = scriptPacketValidation.verdict,
                sourceBindingVerdict = settingBuildReport.verdict,
                structureVerdict = scriptPacketValidation.verdict,
                scriptPacketPreflightReportFile = settingBuildReport.scriptPacketValidationFile,
                acceptedDraftPacketFile = settingBuildReport.settingPackFile,
            ),
            protectedContinuityAnchorsSummary = AcceptedScriptPacketContinuityAnchorsSummary(
                protectedContinuityAnchorCount = anchors.size,
                protectedContinuityAnchors = anchors,
            ),
            remainingResidualRisks = listOf(
                "Local preview bridge has not recorded a human decision.",
                "Asset candidates remain requirements only.",
                "Media rendering waits for later comic-video gates.",
            ),
            sceneBeatPlanningSummary = StoryboardPlanningSceneBeatSummary(
                planningBasis = "setting_script_packet_draft_preview",
                scriptPacketDraftFile = scriptPacketDraftFile,
                scriptPacketDraftContentSha256 = scriptHash,
                sceneBeatExtractionStatus = "derived_from_setting_script_packet_draft",
                notes = listOf(
                    "Use the generated script packet frames as local preview storyboard input.",
                    "This planning brief does not create media output.",
                ),
            ),
            framePlanningConstraints = StoryboardPlanningFrameConstraints(
                frameDraftingStatus = "ready_for_local_preview_storyboard_draft",
                continuityPolicy = "Carry every continuity lock into storyboard frames.",
                cameraPolicy = "Use script packet camera notes and visible public setting constraints.",
                downstreamBoundary = "Local preview planning only; no media output or registry state.",
            ),
            mustShowBoundary = anchors,
            mustNotShowBoundary = listOf(
                "Do not expose non-public source material.",
                "Do not create media output in this planning step.",
                "Do not mark assets or scenes as decided.",
            ),
            assetNeedsCarriedForward = StoryboardPlanningAssetCarryForward(
                sourceScriptPacketDraftFile = scriptPacketDraftFile,
                extractionStatus = "carried_from_setting_script_packet_draft",
                policy = "Carry asset needs as candidate requirements only.",
            ),
            reviewChecklist = listOf(
                StoryboardPlanningReviewChecklistItem(
                    itemId = "setting-preview-source",
                    category = "source_linkage",
                    requiredCheck = "Confirm generated storyboard draft links back to the setting script packet draft.",
                ),
                StoryboardPlanningReviewChecklistItem(
                    itemId = "setting-preview-continuity",
                    category = "continuity",
                    requiredCheck = "Carry all continuity locks into every generated storyboard frame.",
                ),
                StoryboardPlanningReviewChecklistItem(
                    itemId = "setting-preview-boundary",
                    category = "downstream_boundary",
                    requiredCheck = "Keep output as local preview JSON and avoid media/provider/registry state.",
                ),
            ),
            nextAllowedDownstreamStep = if (verdict == "PASS") "storyboard_packet_draft" else "",
            blockedReason = if (verdict == "FAIL") {
                findings.filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }.joinToString("; ") { it.ruleId }
            } else {
                ""
            },
            findingCount = findings.size,
            findings = findings,
            evidence = StoryboardPlanningBriefEvidence(
                acceptedScriptPacketVerdict = settingBuildReport.verdict,
                storyboardPlanningPreflightVerdict = scriptPacketValidation.verdict,
                scriptPacketDraftHashPresent = scriptHash.matches(Regex("""[0-9a-f]{64}""")),
                sourceReportFileCount = listOf(
                    settingBuildReportFile,
                    settingBuildReport.scriptPacketValidationFile,
                    settingBuildReport.settingPackFile,
                ).count { it.isNotBlank() },
                sourceReportVerdictCount = listOf(
                    settingBuildReport.verdict,
                    scriptPacketValidation.verdict,
                ).count { it.isNotBlank() },
                protectedContinuityAnchorCount = anchors.size,
                residualRiskCount = 3,
                forbiddenClaimCount = 0,
                reviewChecklistCount = 3,
            ),
        )
    }

    fun createStoryboardDraft(
        scriptPacketDraft: ScriptPacketDraft,
        planningBrief: StoryboardPlanningBrief,
        planningBriefFile: String,
        draftId: String = "",
    ): StoryboardPacketDraft {
        val frames = frameCandidates(scriptPacketDraft).mapIndexed { index, frameCandidate ->
            val segment = segmentForFrame(scriptPacketDraft, frameCandidate)
            val beat = segment?.beatList?.firstOrNull { it.beatId == frameCandidate.beatId }
            val frameId = frameCandidate.frameId.ifBlank {
                "frame-${(index + 1).toString().padStart(2, '0')}"
            }
            StoryboardPacketFrameDraft(
                frameId = frameId,
                sceneReference = segment?.sceneReference?.candidateId
                    ?.takeIf { it.isNotBlank() }
                    ?: segment?.sceneReference?.sceneId
                    ?: segment?.segmentId
                    ?: "setting-preview-scene",
                beatReference = frameCandidate.beatId.ifBlank { beat?.beatId.orEmpty() },
                visualIntent = listOf(
                    frameCandidate.framePurpose,
                    beat?.visibleAction.orEmpty(),
                    movementEvidenceSentence(),
                ).filter { it.isNotBlank() }.joinToString(" "),
                cameraIntent = frameCandidate.cameraPurpose.ifBlank {
                    segment?.visualSetup?.cameraIntent.orEmpty()
                },
                mustShow = (
                    planningBrief.protectedContinuityAnchorsSummary.protectedContinuityAnchors +
                        (segment?.visualSetup?.mustShow ?: emptyList()) +
                        listOf(frameCandidate.visibleSubject)
                    ).filter { it.isNotBlank() }.distinct(),
                doNotShow = (
                    frameCandidate.doNotShow +
                        (segment?.visualSetup?.mustNotShow ?: emptyList()) +
                        planningBrief.mustNotShowBoundary
                    ).filter { it.isNotBlank() }.distinct(),
                continuityAnchors = planningBrief.protectedContinuityAnchorsSummary.protectedContinuityAnchors
                    .filter { it.isNotBlank() },
                assetCandidateRefs = assetCandidateIds(scriptPacketDraft),
                reviewNotes = "Local preview storyboard frame derived from setting script packet draft.",
            )
        }
        val resolvedDraftId = draftId.ifBlank {
            "storyboard-draft-${slug(scriptPacketDraft.packetId)}"
        }
        return StoryboardPacketDraft(
            packetId = resolvedDraftId,
            storyboardPlanningBriefId = planningBrief.briefId,
            acceptedScriptPacketId = planningBrief.acceptedScriptPacketId,
            scriptPacketDraftFile = planningBrief.scriptPacketDraftFile,
            scriptPacketDraftContentSha256 = planningBrief.scriptPacketDraftContentSha256,
            sourceStoryboardPlanningBriefFile = planningBriefFile,
            protectedContinuityAnchors = planningBrief.protectedContinuityAnchorsSummary.protectedContinuityAnchors,
            frames = frames,
            assetCandidates = scriptPacketDraft.assetNeeds.mapIndexed { index, asset ->
                StoryboardPacketAssetCandidateDraft(
                    candidateId = "asset-${slug(asset.candidateName.ifBlank { asset.existingAssetId.ifBlank { "candidate-${index + 1}" } })}",
                    requirement = listOf(asset.assetType, asset.candidateName, asset.candidateReason)
                        .filter { it.isNotBlank() }
                        .joinToString(": "),
                    sourceFrameIds = frames.map { it.frameId },
                    approvalStatus = "candidate_only",
                    reviewNotes = "Carried from setting script packet draft as a candidate requirement.",
                )
            },
            reviewNotes = "Local preview storyboard draft; requires downstream review-ready gates before image-facing proof.",
        )
    }

    private fun frameCandidates(scriptPacketDraft: ScriptPacketDraft): List<ScriptPacketFrameCandidate> =
        scriptPacketDraft.storyboardNotes.frameCandidates.ifEmpty {
            scriptPacketDraft.segments.flatMap { segment ->
                segment.beatList.mapIndexed { index, beat ->
                    ScriptPacketFrameCandidate(
                        frameId = "frame-${slug(segment.segmentId)}-${(index + 1).toString().padStart(2, '0')}",
                        beatId = beat.beatId,
                        framePurpose = beat.outcome,
                        cameraPurpose = segment.visualSetup.cameraIntent,
                        visibleSubject = segment.visualSetup.mustShow.joinToString(", "),
                        doNotShow = segment.visualSetup.mustNotShow,
                    )
                }
            }
        }

    private fun segmentForFrame(
        scriptPacketDraft: ScriptPacketDraft,
        frameCandidate: ScriptPacketFrameCandidate,
    ): ScriptPacketSegment? =
        scriptPacketDraft.segments.firstOrNull { segment ->
            segment.beatList.any { it.beatId == frameCandidate.beatId }
        } ?: scriptPacketDraft.segments.firstOrNull()

    private fun assetCandidateIds(scriptPacketDraft: ScriptPacketDraft): List<String> =
        scriptPacketDraft.assetNeeds.mapIndexed { index, asset ->
            "asset-${slug(asset.candidateName.ifBlank { asset.existingAssetId.ifBlank { "candidate-${index + 1}" } })}"
        }

    private fun movementEvidenceSentence(): String =
        "Action evidence: the visible actor checks the scene constraint, keeps continuity anchors readable, and chooses whether to move before the next beat."

    private fun sha256(text: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(text.toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }

    private fun slug(value: String): String =
        value.lowercase()
            .replace(Regex("""[^a-z0-9]+"""), "-")
            .trim('-')
            .ifBlank { "setting" }

    private fun planningFinding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): StoryboardPlanningBriefFinding =
        StoryboardPlanningBriefFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
