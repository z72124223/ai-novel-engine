package com.localnovel.engine.service

import com.localnovel.engine.model.FoundationCard
import com.localnovel.engine.model.FoundationCardBoundaryFlags
import com.localnovel.engine.model.FoundationCardDownstreamUse
import com.localnovel.engine.model.FoundationCardPack
import com.localnovel.engine.model.FoundationCardPackValidationBoundarySummary
import com.localnovel.engine.model.FoundationCardPackValidationCheck
import com.localnovel.engine.model.FoundationCardPackValidationEvidence
import com.localnovel.engine.model.FoundationCardPackValidationFinding
import com.localnovel.engine.model.FoundationCardPackValidationReport
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class FoundationCardPackValidationService {
    private val requiredSchemaVersion = "foundation-card-pack-v0"
    private val requiredStatus = "FOUNDATION_CARD_PACK_DRAFT_NON_CANON"
    private val passStatus = "FOUNDATION_CARD_PACK_VALIDATION_PASS"
    private val failStatus = "FOUNDATION_CARD_PACK_VALIDATION_FAIL"
    private val downstreamBoundary = "foundation_card_pack_validation_v0_only_not_canon_or_runtime"
    private val nextAllowedDownstreamStep = "09_foundation_card_pack_review"
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

    fun validateFile(
        inputPath: Path,
        outPath: Path? = null,
        validationId: String = "",
    ): FoundationCardPackValidationReport {
        val pack = JsonSupport.json.decodeFromString<FoundationCardPack>(
            Files.readString(inputPath).removePrefix("\uFEFF"),
        )
        val report = validate(
            pack = pack,
            sourcePackFile = inputPath.toString(),
            validationId = validationId,
        )
        if (outPath != null) {
            outPath.parent?.let { Files.createDirectories(it) }
            Files.writeString(outPath, JsonSupport.json.encodeToString(report))
        }
        return report
    }

    fun validate(
        pack: FoundationCardPack,
        sourcePackFile: String = "",
        validationId: String = "",
    ): FoundationCardPackValidationReport {
        val findings = mutableListOf<FoundationCardPackValidationFinding>()
        val duplicateIds = duplicateValues(pack.cards.map { it.id.trim() }.filter { it.isNotBlank() })
        val presentFamilies = pack.cards.map { it.family }.filter { it.isNotBlank() }.distinct().sorted()
        val missingFamilies = requiredFamilies.filterNot { it in presentFamilies }
        val missingSourceNotesCount = missingSourceNotesCount(pack)
        val missingRetrievalTagsCount = missingRetrievalTagsCount(pack)
        val missingDownstreamUseCount = missingDownstreamUseCount(pack)
        val unsafeBoundaryFlagCount = unsafeBoundaryFlagCount(pack)
        val readinessClaimCount = readinessClaimCount(pack)
        val eligibilityClaimCount = eligibilityClaimCount(pack)
        val boundarySummary = boundarySummary(pack)

        validatePackFields(pack, findings)
        validateCardFields(pack.cards, findings)
        duplicateIds.forEach { duplicate ->
            findings += finding(
                ruleId = "FOUNDATION_CARD_ID_DUPLICATE",
                category = "structure",
                severity = "CRITICAL",
                path = "$.cards",
                message = "Foundation card ids must be unique.",
                evidence = listOf(duplicate),
                suggestedFix = "Use stable unique 09 card ids.",
            )
        }
        missingFamilies.forEach { family ->
            findings += finding(
                ruleId = "FOUNDATION_CARD_REQUIRED_FAMILY_MISSING",
                category = "structure",
                severity = "CRITICAL",
                path = "$.cards",
                message = "Foundation card pack must include all required 09 card families.",
                evidence = listOf(family),
                suggestedFix = "Add one draft non-canon card for the missing family.",
            )
        }

        val checks = buildChecks(
            pack = pack,
            sourcePackFile = sourcePackFile,
            duplicateIds = duplicateIds,
            missingFamilies = missingFamilies,
            missingSourceNotesCount = missingSourceNotesCount,
            missingRetrievalTagsCount = missingRetrievalTagsCount,
            missingDownstreamUseCount = missingDownstreamUseCount,
            unsafeBoundaryFlagCount = unsafeBoundaryFlagCount,
            readinessClaimCount = readinessClaimCount,
            eligibilityClaimCount = eligibilityClaimCount,
            boundarySummary = boundarySummary,
            findings = findings,
        )
        val failedCheckCount = checks.count { it.status != "PASS" }
        val verdict = if (findings.hasBlocking() || failedCheckCount > 0) "FAIL" else "PASS"
        val resolvedValidationId = validationId.ifBlank {
            pack.packId.takeIf { it.isNotBlank() }?.let { "$it-validation-v0" }.orEmpty()
        }

        return FoundationCardPackValidationReport(
            verdict = verdict,
            status = if (verdict == "PASS") passStatus else failStatus,
            validationId = resolvedValidationId,
            sourcePackFile = sourcePackFile,
            packId = pack.packId,
            title = pack.title,
            locale = pack.locale,
            checks = checks,
            evidence = FoundationCardPackValidationEvidence(
                cardCount = pack.cards.size,
                requiredFamilyCount = requiredFamilies.size,
                presentRequiredFamilyCount = requiredFamilies.count { it in presentFamilies },
                presentFamilies = presentFamilies,
                missingFamilies = missingFamilies,
                duplicateCardIdCount = duplicateIds.size,
                missingSourceNotesCount = missingSourceNotesCount,
                missingRetrievalTagsCount = missingRetrievalTagsCount,
                missingDownstreamUseCount = missingDownstreamUseCount,
                unsafeBoundaryFlagCount = unsafeBoundaryFlagCount,
                readinessClaimCount = readinessClaimCount,
                eligibilityClaimCount = eligibilityClaimCount,
                failedCheckCount = failedCheckCount,
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
        sourcePackFile: String,
        duplicateIds: List<String>,
        missingFamilies: List<String>,
        missingSourceNotesCount: Int,
        missingRetrievalTagsCount: Int,
        missingDownstreamUseCount: Int,
        unsafeBoundaryFlagCount: Int,
        readinessClaimCount: Int,
        eligibilityClaimCount: Int,
        boundarySummary: FoundationCardPackValidationBoundarySummary,
        findings: MutableList<FoundationCardPackValidationFinding>,
    ): List<FoundationCardPackValidationCheck> =
        listOf(
            check(
                checkId = "explicit-source-pack",
                category = "source_validation",
                passed = sourcePackFile.isNotBlank(),
                message = "Foundation card pack validation requires an explicit input file.",
                evidence = listOf("sourcePackFile=$sourcePackFile"),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_SOURCE_FILE_MISSING",
                path = "$.input",
                suggestedFix = "Pass --input <pack.json>.",
            ),
            check(
                checkId = "pack-metadata",
                category = "structure",
                passed = pack.schemaVersion == requiredSchemaVersion &&
                    pack.status == requiredStatus &&
                    pack.packId.isNotBlank() &&
                    pack.title.isNotBlank() &&
                    pack.locale.isNotBlank(),
                message = "FoundationCardPack requires schema version, draft non-canon status, pack id, title, and locale.",
                evidence = listOf(
                    "schemaVersion=${pack.schemaVersion}",
                    "status=${pack.status}",
                    "packId=${pack.packId}",
                    "titlePresent=${pack.title.isNotBlank()}",
                    "locale=${pack.locale}",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_METADATA_INVALID",
                path = "$.pack",
                suggestedFix = "Use schemaVersion=$requiredSchemaVersion and status=$requiredStatus with nonblank metadata.",
            ),
            check(
                checkId = "candidate-status",
                category = "boundary",
                passed = candidateStatusOk(pack),
                message = "FoundationCardPack and cards must stay candidate, unapproved, and not setting-pack ready.",
                evidence = listOf(
                    "packCanonStatus=${pack.canonStatus}",
                    "packApprovalStatus=${pack.approvalStatus}",
                    "packSettingPackReadiness=${pack.settingPackReadiness}",
                    "readinessClaimCount=$readinessClaimCount",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_CANDIDATE_STATUS_INVALID",
                path = "$.statusFields",
                suggestedFix = "Use non_canon_candidate, not_approved, and not_ready.",
            ),
            check(
                checkId = "required-families",
                category = "structure",
                passed = missingFamilies.isEmpty() && pack.cards.isNotEmpty() && pack.cards.all { it.family in requiredFamilies },
                message = "FoundationCardPack must include all seven accepted 09 foundation card families and no unknown family.",
                evidence = listOf(
                    "cardCount=${pack.cards.size}",
                    "missingFamilies=${missingFamilies.joinToString(",")}",
                    "unknownFamilies=${pack.cards.map { it.family }.filter { it !in requiredFamilies }.distinct().joinToString(",")}",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_REQUIRED_FAMILIES_INVALID",
                path = "$.cards[*].family",
                suggestedFix = "Include world_premise, character_foundation, location_foundation, system_foundation, constraint, asset_seed, and situation_seed.",
            ),
            check(
                checkId = "card-ids-unique",
                category = "structure",
                passed = duplicateIds.isEmpty() && pack.cards.all { it.id.isNotBlank() },
                message = "FoundationCardPack card ids must be nonblank and unique.",
                evidence = listOf("duplicateCardIds=${duplicateIds.joinToString(",")}"),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_CARD_IDS_INVALID",
                path = "$.cards[*].id",
                suggestedFix = "Use unique stable ids for every card.",
            ),
            check(
                checkId = "source-and-tags-present",
                category = "traceability",
                passed = missingSourceNotesCount == 0 && missingRetrievalTagsCount == 0,
                message = "FoundationCardPack and every card require source notes and retrieval tags.",
                evidence = listOf(
                    "missingSourceNotesCount=$missingSourceNotesCount",
                    "missingRetrievalTagsCount=$missingRetrievalTagsCount",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_TRACEABILITY_MISSING",
                path = "$.sourceNotes",
                suggestedFix = "Add nonblank sourceNotes and retrievalTags to the pack and every card.",
            ),
            check(
                checkId = "downstream-use-present-and-safe",
                category = "boundary",
                passed = missingDownstreamUseCount == 0 && eligibilityClaimCount == 0,
                message = "FoundationCardPack downstreamUse must be present and must not claim query, setting-pack, scene-packet, or asset-registry eligibility.",
                evidence = listOf(
                    "missingDownstreamUseCount=$missingDownstreamUseCount",
                    "eligibilityClaimCount=$eligibilityClaimCount",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_DOWNSTREAM_USE_INVALID",
                path = "$.downstreamUse",
                suggestedFix = "Set all downstreamUse eligibility flags to false.",
            ),
            check(
                checkId = "boundary-flags-safe",
                category = "boundary",
                passed = unsafeBoundaryFlagCount == 0 && boundarySummary.candidateOnly,
                message = "FoundationCardPack boundary flags must prohibit canon, setting pack, runtime SQLite, approved assets, provider/API, renderer/ffmpeg/TTS, media, final media approval, release, and production lock.",
                evidence = listOf(
                    "unsafeBoundaryFlagCount=$unsafeBoundaryFlagCount",
                    "candidateOnly=${boundarySummary.candidateOnly}",
                    "allRendererInvocationNone=${boundarySummary.allRendererInvocationNone}",
                    "allFfmpegInvocationNone=${boundarySummary.allFfmpegInvocationNone}",
                    "allTtsInvocationNone=${boundarySummary.allTtsInvocationNone}",
                ),
                findings = findings,
                ruleId = "FOUNDATION_CARD_PACK_BOUNDARY_FLAGS_UNSAFE",
                path = "$.boundaryFlags",
                suggestedFix = "Keep all provider/media/runtime/approval/production boundary flags false and invocations none.",
            ),
        )

    private fun validatePackFields(
        pack: FoundationCardPack,
        findings: MutableList<FoundationCardPackValidationFinding>,
    ) {
        requireField(pack.packId, "$.packId", "FOUNDATION_CARD_PACK_ID_MISSING", "pack id", findings)
        requireField(pack.title, "$.title", "FOUNDATION_CARD_PACK_TITLE_MISSING", "title", findings)
        requireField(pack.locale, "$.locale", "FOUNDATION_CARD_PACK_LOCALE_MISSING", "locale", findings)
        if (pack.sourceNotes.none { it.isNotBlank() }) {
            findings += finding("FOUNDATION_CARD_PACK_SOURCE_NOTES_MISSING", "traceability", "CRITICAL", "$.sourceNotes", "FoundationCardPack requires source notes.")
        }
        if (pack.retrievalTags.none { it.isNotBlank() }) {
            findings += finding("FOUNDATION_CARD_PACK_RETRIEVAL_TAGS_MISSING", "traceability", "CRITICAL", "$.retrievalTags", "FoundationCardPack requires retrieval tags.")
        }
    }

    private fun validateCardFields(
        cards: List<FoundationCard>,
        findings: MutableList<FoundationCardPackValidationFinding>,
    ) {
        if (cards.isEmpty()) {
            findings += finding("FOUNDATION_CARD_PACK_CARDS_MISSING", "structure", "CRITICAL", "$.cards", "FoundationCardPack requires cards.")
        }
        cards.forEachIndexed { index, card ->
            val path = "$.cards[$index]"
            requireField(card.id, "$path.id", "FOUNDATION_CARD_ID_MISSING", "card id", findings)
            requireField(card.family, "$path.family", "FOUNDATION_CARD_FAMILY_MISSING", "card family", findings)
            requireField(card.title, "$path.title", "FOUNDATION_CARD_TITLE_MISSING", "card title", findings)
            requireField(card.visibility, "$path.visibility", "FOUNDATION_CARD_VISIBILITY_MISSING", "visibility", findings)
            requireField(card.publicSummary, "$path.publicSummary", "FOUNDATION_CARD_PUBLIC_SUMMARY_MISSING", "public summary", findings)
            if (card.sourceNotes.none { it.isNotBlank() }) {
                findings += finding("FOUNDATION_CARD_SOURCE_NOTES_MISSING", "traceability", "CRITICAL", "$path.sourceNotes", "Foundation card requires source notes.")
            }
            if (card.retrievalTags.none { it.isNotBlank() }) {
                findings += finding("FOUNDATION_CARD_RETRIEVAL_TAGS_MISSING", "traceability", "CRITICAL", "$path.retrievalTags", "Foundation card requires retrieval tags.")
            }
            if (card.reviewOnlyNotes.none { it.isNotBlank() }) {
                findings += finding("FOUNDATION_CARD_REVIEW_NOTES_MISSING", "traceability", "MAJOR", "$path.reviewOnlyNotes", "Foundation card requires review-only notes.")
            }
            if (card.constraints.none { it.isNotBlank() }) {
                findings += finding("FOUNDATION_CARD_CONSTRAINTS_MISSING", "boundary", "MAJOR", "$path.constraints", "Foundation card requires constraints.")
            }
            if (card.forbiddenImplications.none { it.isNotBlank() }) {
                findings += finding("FOUNDATION_CARD_FORBIDDEN_IMPLICATIONS_MISSING", "boundary", "MAJOR", "$path.forbiddenImplications", "Foundation card requires forbidden implications.")
            }
        }
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

    private fun missingSourceNotesCount(pack: FoundationCardPack): Int =
        (if (pack.sourceNotes.none { it.isNotBlank() }) 1 else 0) +
            pack.cards.count { it.sourceNotes.none { note -> note.isNotBlank() } }

    private fun missingRetrievalTagsCount(pack: FoundationCardPack): Int =
        (if (pack.retrievalTags.none { it.isNotBlank() }) 1 else 0) +
            pack.cards.count { it.retrievalTags.none { tag -> tag.isNotBlank() } }

    private fun missingDownstreamUseCount(pack: FoundationCardPack): Int =
        (if (pack.downstreamUse == null) 1 else 0) + pack.cards.count { it.downstreamUse == null }

    private fun unsafeBoundaryFlagCount(pack: FoundationCardPack): Int =
        listOf(pack.boundaryFlags).count { !safeBoundaryFlags(it) } +
            pack.cards.count { !safeBoundaryFlags(it.boundaryFlags) }

    private fun readinessClaimCount(pack: FoundationCardPack): Int =
        (if (pack.settingPackReadiness != requiredSettingPackReadiness) 1 else 0) +
            pack.cards.count { it.settingPackReadiness != requiredSettingPackReadiness }

    private fun eligibilityClaimCount(pack: FoundationCardPack): Int =
        listOf(pack.downstreamUse).count { !safeDownstreamUse(it) } +
            pack.cards.count { !safeDownstreamUse(it.downstreamUse) }

    private fun boundarySummary(pack: FoundationCardPack): FoundationCardPackValidationBoundarySummary {
        val boundaryFlags = listOf(pack.boundaryFlags) + pack.cards.map { it.boundaryFlags }
        return FoundationCardPackValidationBoundarySummary(
            candidateOnly = candidateStatusOk(pack) &&
                boundaryFlags.all { safeBoundaryFlags(it) } &&
                listOf(pack.downstreamUse).plus(pack.cards.map { it.downstreamUse }).all { safeDownstreamUse(it) },
            canonApproved = boundaryFlags.any { it?.canonApproved == true },
            settingPackData = boundaryFlags.any { it?.settingPackData == true },
            runtimeSqliteData = boundaryFlags.any { it?.runtimeSqliteData == true },
            approvedAssetData = boundaryFlags.any { it?.approvedAssetData == true },
            providerApiCalls = boundaryFlags.any { it?.providerApiCalls == true },
            mediaGenerated = boundaryFlags.any { it?.mediaGenerated == true },
            finalMediaApproval = boundaryFlags.any { it?.finalMediaApproval == true },
            releaseApproved = boundaryFlags.any { it?.releaseApproved == true },
            productionLock = boundaryFlags.any { it?.productionLock == true },
            allRendererInvocationNone = boundaryFlags.all { it?.rendererInvocation == "none" },
            allFfmpegInvocationNone = boundaryFlags.all { it?.ffmpegInvocation == "none" },
            allTtsInvocationNone = boundaryFlags.all { it?.ttsInvocation == "none" },
        )
    }

    private fun safeDownstreamUse(downstreamUse: FoundationCardDownstreamUse?): Boolean =
        downstreamUse != null &&
            !downstreamUse.queryEligible &&
            !downstreamUse.scriptReadyScenePacketEligible &&
            !downstreamUse.settingPackConversionEligible &&
            !downstreamUse.assetRegistryEligible

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

    private fun requireField(
        value: String,
        path: String,
        ruleId: String,
        label: String,
        findings: MutableList<FoundationCardPackValidationFinding>,
    ) {
        if (value.isBlank()) {
            findings += finding(ruleId, "structure", "CRITICAL", path, "FoundationCardPack requires nonblank $label.")
        }
    }

    private fun check(
        checkId: String,
        category: String,
        passed: Boolean,
        message: String,
        evidence: List<String>,
        findings: MutableList<FoundationCardPackValidationFinding>,
        ruleId: String,
        path: String,
        suggestedFix: String,
    ): FoundationCardPackValidationCheck {
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
        return FoundationCardPackValidationCheck(
            checkId = checkId,
            status = if (passed) "PASS" else "FAIL",
            category = category,
            message = message,
            evidence = evidence,
        )
    }

    private fun duplicateValues(values: List<String>): List<String> =
        values.groupingBy { it }.eachCount().filter { it.key.isNotBlank() && it.value > 1 }.keys.toList()

    private fun List<FoundationCardPackValidationFinding>.hasBlocking(): Boolean =
        any { it.severity == "CRITICAL" || it.severity == "MAJOR" }

    private fun finding(
        ruleId: String,
        category: String,
        severity: String,
        path: String,
        message: String,
        evidence: List<String> = emptyList(),
        suggestedFix: String = "",
    ): FoundationCardPackValidationFinding =
        FoundationCardPackValidationFinding(ruleId, category, severity, path, message, evidence, suggestedFix)
}
