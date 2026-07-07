package com.localnovel.engine.service

import com.localnovel.engine.model.FoundationCard
import com.localnovel.engine.model.FoundationCardBoundaryFlags
import com.localnovel.engine.model.FoundationCardDownstreamUse
import com.localnovel.engine.model.FoundationCardPack
import com.localnovel.engine.model.FoundationCardPackReviewBoundarySummary
import com.localnovel.engine.model.FoundationCardPackReviewCheck
import com.localnovel.engine.model.FoundationCardPackReviewEvidence
import com.localnovel.engine.model.FoundationCardPackReviewFinding
import com.localnovel.engine.model.FoundationCardPackReviewReport
import com.localnovel.engine.model.FoundationCardPackValidationReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class FoundationCardPackReviewService {
    private val requiredSchemaVersion = "foundation-card-pack-v0"
    private val requiredPackStatus = "FOUNDATION_CARD_PACK_DRAFT_NON_CANON"
    private val requiredValidationStatus = "FOUNDATION_CARD_PACK_VALIDATION_PASS"
    private val requiredValidationDownstreamBoundary = "foundation_card_pack_validation_v0_only_not_canon_or_runtime"
    private val requiredValidationNextStep = "09_foundation_card_pack_review"
    private val passStatus = "FOUNDATION_CARD_PACK_REVIEW_PASS"
    private val failStatus = "FOUNDATION_CARD_PACK_REVIEW_FAIL"
    private val downstreamBoundary = "foundation_card_pack_review_only_not_canon_or_runtime"
    private val nextAllowedDownstreamStep = "demand_driven_query_contract_spec_v0"
    private val requiredFamilies = listOf(
        "world_premise",
        "character_foundation",
        "location_foundation",
        "system_foundation",
        "constraint",
        "asset_seed",
        "situation_seed",
    )
    private val allowedCanonStatus = setOf("non_canon_candidate", "candidate_non_canon")
    private val allowedApprovalStatus = setOf("not_approved", "draft_unapproved", "draft", "unapproved")
    private val requiredSettingPackReadiness = "not_ready"

    fun reviewFile(
        packPath: Path,
        validationPath: Path,
        outPath: Path? = null,
        reviewId: String = "",
    ): FoundationCardPackReviewReport {
        val pack = JsonSupport.json.decodeFromString<FoundationCardPack>(
            Files.readString(packPath).removePrefix("\uFEFF"),
        )
        val validation = JsonSupport.json.decodeFromString<FoundationCardPackValidationReport>(
            Files.readString(validationPath).removePrefix("\uFEFF"),
        )
        val report = review(
            pack = pack,
            validation = validation,
            sourcePackFile = packPath.toString(),
            sourceValidationFile = validationPath.toString(),
            reviewId = reviewId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(report))
        }
        return report
    }

    fun review(
        pack: FoundationCardPack,
        validation: FoundationCardPackValidationReport,
        sourcePackFile: String = "",
        sourceValidationFile: String = "",
        reviewId: String = "",
    ): FoundationCardPackReviewReport {
        val findings = mutableListOf<FoundationCardPackReviewFinding>()
        val presentFamilies = pack.cards.map { it.family }.filter { it.isNotBlank() }.distinct().sorted()
        val missingFamilies = requiredFamilies.filterNot { it in presentFamilies }
        val duplicateCardIds = duplicateValues(pack.cards.map { it.id.trim() }.filter { it.isNotBlank() })
        val sourcePackFileMatches = pathAligned(sourcePackFile, validation.sourcePackFile)
        val packIdAligned = pack.packId.isNotBlank() && pack.packId == validation.packId
        val titleAligned = pack.title.isNotBlank() && pack.title == validation.title
        val localeAligned = pack.locale.isNotBlank() && pack.locale == validation.locale
        val sourceAligned = sourcePackFileMatches && packIdAligned && titleAligned && localeAligned
        val validationFailedCheckCount = validation.evidence.failedCheckCount +
            validation.checks.count { it.status != "PASS" }
        val validationFindingCount = validation.findingCount + validation.findings.size
        val missingTraceabilityCount = missingTraceabilityCount(pack)
        val missingDownstreamUseCount = missingDownstreamUseCount(pack)
        val unsafeBoundaryFlagCount = unsafeBoundaryFlagCount(pack)
        val readinessClaimCount = readinessClaimCount(pack)
        val eligibilityClaimCount = eligibilityClaimCount(pack)
        val boundarySummary = boundarySummary(
            pack = pack,
            validation = validation,
            validationFailedCheckCount = validationFailedCheckCount,
            validationFindingCount = validationFindingCount,
        )

        val checks = buildChecks(
            pack = pack,
            validation = validation,
            sourcePackFile = sourcePackFile,
            sourceValidationFile = sourceValidationFile,
            sourceAligned = sourceAligned,
            sourcePackFileMatches = sourcePackFileMatches,
            packIdAligned = packIdAligned,
            titleAligned = titleAligned,
            localeAligned = localeAligned,
            missingFamilies = missingFamilies,
            duplicateCardIds = duplicateCardIds,
            validationFailedCheckCount = validationFailedCheckCount,
            validationFindingCount = validationFindingCount,
            missingTraceabilityCount = missingTraceabilityCount,
            missingDownstreamUseCount = missingDownstreamUseCount,
            unsafeBoundaryFlagCount = unsafeBoundaryFlagCount,
            readinessClaimCount = readinessClaimCount,
            eligibilityClaimCount = eligibilityClaimCount,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val reviewFailedCheckCount = checks.count { it.status != "PASS" }
        val verdict = if (findings.hasBlocking() || reviewFailedCheckCount > 0) "FAIL" else "PASS"
        val resolvedReviewId = reviewId.ifBlank {
            pack.packId.takeIf { it.isNotBlank() }?.let { "$it-review-v0" }.orEmpty()
        }

        return FoundationCardPackReviewReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            reviewId = resolvedReviewId,
            sourcePackFile = sourcePackFile,
            sourceValidationFile = sourceValidationFile,
            sourceValidationId = validation.validationId,
            packId = pack.packId,
            title = pack.title,
            locale = pack.locale,
            checks = checks,
            evidence = FoundationCardPackReviewEvidence(
                sourceAligned = sourceAligned,
                sourcePackFileMatches = sourcePackFileMatches,
                packIdAligned = packIdAligned,
                titleAligned = titleAligned,
                localeAligned = localeAligned,
                cardCount = pack.cards.size,
                requiredFamilyCount = requiredFamilies.size,
                presentRequiredFamilyCount = requiredFamilies.count { it in presentFamilies },
                presentFamilies = presentFamilies,
                missingFamilies = missingFamilies,
                validationFailedCheckCount = validationFailedCheckCount,
                validationFindingCount = validationFindingCount,
                reviewFailedCheckCount = reviewFailedCheckCount,
                duplicateCardIdCount = duplicateCardIds.size,
                missingTraceabilityCount = missingTraceabilityCount,
                missingDownstreamUseCount = missingDownstreamUseCount,
                unsafeBoundaryFlagCount = unsafeBoundaryFlagCount,
                readinessClaimCount = readinessClaimCount,
                eligibilityClaimCount = eligibilityClaimCount,
            ),
            boundarySummary = boundarySummary,
            downstreamBoundary = downstreamBoundary,
            nextAllowedDownstreamStep = if (verdict == "PASS") nextAllowedDownstreamStep else "",
            blockedReason = if (verdict == "PASS") "" else findings
                .filter { it.severity == "CRITICAL" || it.severity == "MAJOR" }
                .joinToString("; ") { it.ruleId }
                .ifBlank { checks.filter { it.status != "PASS" }.joinToString("; ") { it.checkId } },
            findingCount = findings.size,
            findings = findings,
        )
    }

    private fun buildChecks(
        pack: FoundationCardPack,
        validation: FoundationCardPackValidationReport,
        sourcePackFile: String,
        sourceValidationFile: String,
        sourceAligned: Boolean,
        sourcePackFileMatches: Boolean,
        packIdAligned: Boolean,
        titleAligned: Boolean,
        localeAligned: Boolean,
        missingFamilies: List<String>,
        duplicateCardIds: List<String>,
        validationFailedCheckCount: Int,
        validationFindingCount: Int,
        missingTraceabilityCount: Int,
        missingDownstreamUseCount: Int,
        unsafeBoundaryFlagCount: Int,
        readinessClaimCount: Int,
        eligibilityClaimCount: Int,
        boundarySummary: FoundationCardPackReviewBoundarySummary,
        findings: MutableList<FoundationCardPackReviewFinding>,
    ): List<FoundationCardPackReviewCheck> =
        listOf(
            check(
                checkId = "explicit-inputs",
                category = "source_validation",
                passed = sourcePackFile.isNotBlank() && sourceValidationFile.isNotBlank(),
                message = "Foundation card pack review requires explicit pack and validation report inputs.",
                evidence = listOf("sourcePackFile=$sourcePackFile", "sourceValidationFile=$sourceValidationFile"),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REVIEW_INPUTS_MISSING",
                path = "$.inputs",
                suggestedFix = "Pass --pack <pack.json> and --validation <validation-report.json>.",
            ),
            check(
                checkId = "validation-pass-state",
                category = "validation",
                passed = validation.verdict == "PASS" &&
                    validation.status == requiredValidationStatus &&
                    validation.downstreamBoundary == requiredValidationDownstreamBoundary &&
                    validation.nextAllowedDownstreamStep == requiredValidationNextStep,
                message = "Review requires a PASS foundation card pack validation report at the review-gate step.",
                evidence = listOf(
                    "validationVerdict=${validation.verdict}",
                    "validationStatus=${validation.status}",
                    "validationDownstreamBoundary=${validation.downstreamBoundary}",
                    "validationNextAllowedDownstreamStep=${validation.nextAllowedDownstreamStep}",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_VALIDATION_STATE_INVALID",
                path = "$.validation",
                suggestedFix = "Use a PASS $requiredValidationStatus report whose next step is $requiredValidationNextStep.",
            ),
            check(
                checkId = "source-alignment",
                category = "source_validation",
                passed = sourceAligned,
                message = "Review requires exact source alignment between the pack and validation report.",
                evidence = listOf(
                    "sourcePackFileMatches=$sourcePackFileMatches",
                    "packIdAligned=$packIdAligned",
                    "titleAligned=$titleAligned",
                    "localeAligned=$localeAligned",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REVIEW_SOURCE_ALIGNMENT_INVALID",
                path = "$.sourceAlignment",
                suggestedFix = "Review the same pack file and metadata that produced the validation report.",
            ),
            check(
                checkId = "validation-health",
                category = "validation",
                passed = validationFailedCheckCount == 0 &&
                    validationFindingCount == 0 &&
                    validation.boundarySummary.candidateOnly,
                message = "Validation report must have zero failed checks, zero findings, and candidate-only boundaries.",
                evidence = listOf(
                    "validationFailedCheckCount=$validationFailedCheckCount",
                    "validationFindingCount=$validationFindingCount",
                    "validationCandidateOnly=${validation.boundarySummary.candidateOnly}",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_VALIDATION_HEALTH_INVALID",
                path = "$.validation.checks",
                suggestedFix = "Fix the pack and rerun validation before review.",
            ),
            check(
                checkId = "required-families-present",
                category = "structure",
                passed = missingFamilies.isEmpty() &&
                    pack.cards.isNotEmpty() &&
                    pack.cards.all { it.family in requiredFamilies } &&
                    validation.evidence.presentRequiredFamilyCount == requiredFamilies.size &&
                    validation.evidence.missingFamilies.isEmpty(),
                message = "Review requires all seven 09 foundation card families and no unknown family.",
                evidence = listOf(
                    "cardCount=${pack.cards.size}",
                    "missingFamilies=${missingFamilies.joinToString(",")}",
                    "validationPresentRequiredFamilyCount=${validation.evidence.presentRequiredFamilyCount}",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REVIEW_REQUIRED_FAMILIES_INVALID",
                path = "$.cards[*].family",
                suggestedFix = "Include world_premise, character_foundation, location_foundation, system_foundation, constraint, asset_seed, and situation_seed.",
            ),
            check(
                checkId = "candidate-status",
                category = "boundary",
                passed = pack.schemaVersion == requiredSchemaVersion &&
                    pack.status == requiredPackStatus &&
                    pack.packId.isNotBlank() &&
                    pack.title.isNotBlank() &&
                    pack.locale.isNotBlank() &&
                    candidateStatusOk(pack) &&
                    duplicateCardIds.isEmpty(),
                message = "Pack and cards must remain draft, non-canon, unapproved candidates.",
                evidence = listOf(
                    "schemaVersion=${pack.schemaVersion}",
                    "status=${pack.status}",
                    "packCanonStatus=${pack.canonStatus}",
                    "packApprovalStatus=${pack.approvalStatus}",
                    "packSettingPackReadiness=${pack.settingPackReadiness}",
                    "duplicateCardIds=${duplicateCardIds.joinToString(",")}",
                    "readinessClaimCount=$readinessClaimCount",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REVIEW_CANDIDATE_STATUS_INVALID",
                path = "$.statusFields",
                suggestedFix = "Keep cards non-canon, unapproved, not setting-pack ready, and uniquely identified.",
            ),
            check(
                checkId = "traceability-present",
                category = "traceability",
                passed = missingTraceabilityCount == 0,
                message = "Pack and cards must keep source notes, retrieval tags, and review-only notes.",
                evidence = listOf("missingTraceabilityCount=$missingTraceabilityCount"),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REVIEW_TRACEABILITY_MISSING",
                path = "$.sourceNotes",
                suggestedFix = "Add nonblank sourceNotes, retrievalTags, and reviewOnlyNotes to the pack and cards.",
            ),
            check(
                checkId = "downstream-use-safe",
                category = "boundary",
                passed = missingDownstreamUseCount == 0 && eligibilityClaimCount == 0,
                message = "Review must not approve query, setting-pack, scene-packet, or asset-registry eligibility.",
                evidence = listOf(
                    "missingDownstreamUseCount=$missingDownstreamUseCount",
                    "eligibilityClaimCount=$eligibilityClaimCount",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REVIEW_DOWNSTREAM_USE_INVALID",
                path = "$.downstreamUse",
                suggestedFix = "Keep downstream eligibility flags false before the next contract/spec task.",
            ),
            check(
                checkId = "technical-only-boundary",
                category = "boundary",
                passed = unsafeBoundaryFlagCount == 0 &&
                    boundarySummary.technicalReviewOnly &&
                    boundarySummary.candidateOnly &&
                    boundarySummary.validationPass &&
                    !boundarySummary.humanApproval &&
                    !boundarySummary.canonApproval &&
                    !boundarySummary.settingPackConversion &&
                    !boundarySummary.runtimeSqliteData &&
                    !boundarySummary.assetRegistryApproval &&
                    !boundarySummary.providerApiCalls &&
                    !boundarySummary.mediaGenerated &&
                    !boundarySummary.finalMediaApproval &&
                    !boundarySummary.releaseApproved &&
                    !boundarySummary.productionLock &&
                    !boundarySummary.demandDrivenQueryImplementation &&
                    boundarySummary.allRendererInvocationNone &&
                    boundarySummary.allFfmpegInvocationNone &&
                    boundarySummary.allTtsInvocationNone,
                message = "Review PASS is technical only and must not imply approval, canon, runtime, provider/API, renderer/ffmpeg/TTS, media, release, or production lock.",
                evidence = listOf(
                    "unsafeBoundaryFlagCount=$unsafeBoundaryFlagCount",
                    "technicalReviewOnly=${boundarySummary.technicalReviewOnly}",
                    "humanApproval=${boundarySummary.humanApproval}",
                    "providerApiCalls=${boundarySummary.providerApiCalls}",
                    "mediaGenerated=${boundarySummary.mediaGenerated}",
                    "productionLock=${boundarySummary.productionLock}",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REVIEW_BOUNDARY_INVALID",
                path = "$.boundary",
                suggestedFix = "Keep review output technical-only and all provider/media/runtime/approval flags false.",
            ),
        )

    private fun boundarySummary(
        pack: FoundationCardPack,
        validation: FoundationCardPackValidationReport,
        validationFailedCheckCount: Int,
        validationFindingCount: Int,
    ): FoundationCardPackReviewBoundarySummary {
        val boundaryFlags = listOf(pack.boundaryFlags) + pack.cards.map { it.boundaryFlags }
        return FoundationCardPackReviewBoundarySummary(
            candidateOnly = candidateStatusOk(pack) &&
                boundaryFlags.all { safeBoundaryFlags(it) } &&
                listOf(pack.downstreamUse).plus(pack.cards.map { it.downstreamUse }).all { safeDownstreamUse(it) },
            validationPass = validation.verdict == "PASS" &&
                validation.status == requiredValidationStatus &&
                validationFailedCheckCount == 0 &&
                validationFindingCount == 0,
            humanApproval = false,
            canonApproval = boundaryFlags.any { it?.canonApproved == true },
            settingPackConversion = boundaryFlags.any { it?.settingPackData == true },
            runtimeSqliteData = boundaryFlags.any { it?.runtimeSqliteData == true },
            assetRegistryApproval = boundaryFlags.any { it?.approvedAssetData == true },
            providerApiCalls = boundaryFlags.any { it?.providerApiCalls == true },
            mediaGenerated = boundaryFlags.any { it?.mediaGenerated == true },
            finalMediaApproval = boundaryFlags.any { it?.finalMediaApproval == true },
            releaseApproved = boundaryFlags.any { it?.releaseApproved == true },
            productionLock = boundaryFlags.any { it?.productionLock == true },
            demandDrivenQueryImplementation = false,
            allRendererInvocationNone = boundaryFlags.all { it?.rendererInvocation == "none" },
            allFfmpegInvocationNone = boundaryFlags.all { it?.ffmpegInvocation == "none" },
            allTtsInvocationNone = boundaryFlags.all { it?.ttsInvocation == "none" },
        )
    }

    private fun candidateStatusOk(pack: FoundationCardPack): Boolean =
        pack.canonStatus in allowedCanonStatus &&
            pack.approvalStatus in allowedApprovalStatus &&
            pack.settingPackReadiness == requiredSettingPackReadiness &&
            pack.cards.all {
                it.canonStatus in allowedCanonStatus &&
                    it.approvalStatus in allowedApprovalStatus &&
                    it.settingPackReadiness == requiredSettingPackReadiness
            }

    private fun missingTraceabilityCount(pack: FoundationCardPack): Int =
        (if (pack.sourceNotes.none { it.isNotBlank() }) 1 else 0) +
            (if (pack.retrievalTags.none { it.isNotBlank() }) 1 else 0) +
            pack.cards.count { it.sourceNotes.none { note -> note.isNotBlank() } } +
            pack.cards.count { it.retrievalTags.none { tag -> tag.isNotBlank() } } +
            pack.cards.count { it.reviewOnlyNotes.none { note -> note.isNotBlank() } }

    private fun missingDownstreamUseCount(pack: FoundationCardPack): Int =
        (if (pack.downstreamUse == null) 1 else 0) + pack.cards.count { it.downstreamUse == null }

    private fun unsafeBoundaryFlagCount(pack: FoundationCardPack): Int =
        listOf(pack.boundaryFlags).count { !safeBoundaryFlags(it) } +
            pack.cards.count { !safeBoundaryFlags(it.boundaryFlags) }

    private fun readinessClaimCount(pack: FoundationCardPack): Int =
        (if (pack.settingPackReadiness != requiredSettingPackReadiness) 1 else 0) +
            pack.cards.count { it.settingPackReadiness != requiredSettingPackReadiness }

    private fun eligibilityClaimCount(pack: FoundationCardPack): Int =
        listOf(pack.downstreamUse).count { eligibilityClaimed(it) } +
            pack.cards.count { eligibilityClaimed(it.downstreamUse) }

    private fun safeDownstreamUse(downstreamUse: FoundationCardDownstreamUse?): Boolean =
        downstreamUse != null &&
            !downstreamUse.queryEligible &&
            !downstreamUse.scriptReadyScenePacketEligible &&
            !downstreamUse.settingPackConversionEligible &&
            !downstreamUse.assetRegistryEligible

    private fun eligibilityClaimed(downstreamUse: FoundationCardDownstreamUse?): Boolean =
        downstreamUse != null && (
            downstreamUse.queryEligible ||
                downstreamUse.scriptReadyScenePacketEligible ||
                downstreamUse.settingPackConversionEligible ||
                downstreamUse.assetRegistryEligible
            )

    private fun safeBoundaryFlags(boundaryFlags: FoundationCardBoundaryFlags?): Boolean =
        boundaryFlags != null &&
            !boundaryFlags.canonApproved &&
            !boundaryFlags.settingPackData &&
            !boundaryFlags.runtimeSqliteData &&
            !boundaryFlags.approvedAssetData &&
            !boundaryFlags.providerApiCalls &&
            boundaryFlags.rendererInvocation == "none" &&
            boundaryFlags.ffmpegInvocation == "none" &&
            boundaryFlags.ttsInvocation == "none" &&
            !boundaryFlags.mediaGenerated &&
            !boundaryFlags.finalMediaApproval &&
            !boundaryFlags.releaseApproved &&
            !boundaryFlags.productionLock

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<FoundationCardPackReviewFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): FoundationCardPackReviewCheck {
        if (!passed) {
            findings += finding(
                ruleId = ruleId,
                category = category,
                severity = "CRITICAL",
                path = path,
                message = message,
                evidence = evidence,
                suggestedFix = suggestedFix,
            )
        }
        return FoundationCardPackReviewCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun duplicateValues(values: List<String>): List<String> =
        values.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys.toList()

    private fun pathAligned(actual: String, expected: String): Boolean {
        if (actual.isBlank() || expected.isBlank()) return false
        val normalizedActual = normalizePath(actual)
        val normalizedExpected = normalizePath(expected)
        return normalizedActual == normalizedExpected ||
            normalizedActual.endsWith("/$normalizedExpected") ||
            normalizedExpected.endsWith("/$normalizedActual")
    }

    private fun normalizePath(value: String): String =
        runCatching { Path.of(value).normalize().toString() }
            .getOrDefault(value)
            .replace('\\', '/')
            .trim()

    private fun List<FoundationCardPackReviewFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): FoundationCardPackReviewFinding =
        FoundationCardPackReviewFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
