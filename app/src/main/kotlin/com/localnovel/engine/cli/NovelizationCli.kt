package com.localnovel.engine.cli

import com.localnovel.engine.cli.readCliJson
import com.localnovel.engine.cli.requiredCliOption
import com.localnovel.engine.cli.writeCliJson
import com.localnovel.engine.model.ChapterContinuityAnchorFile
import com.localnovel.engine.model.CharacterNuwaGateFile
import com.localnovel.engine.model.CharacterNuwaGateValidationReport
import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationDraftIntakeReport
import com.localnovel.engine.model.NovelizationPreflightReport
import com.localnovel.engine.model.NovelizationProseQualityAuditReport
import com.localnovel.engine.model.NovelizationRevisedDraftIntakeReport
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionContinuityAnchors
import com.localnovel.engine.model.NovelizationRevisionHandlingDeclaration
import com.localnovel.engine.model.NovelizationWriterBrief
import com.localnovel.engine.model.NovelizationWriterBriefValidationReport
import com.localnovel.engine.service.ChapterContinuityAuditService
import com.localnovel.engine.service.CharacterNuwaGateValidationService
import com.localnovel.engine.service.NovelizationAcceptedDraftPacketService
import com.localnovel.engine.service.NovelizationDraftIntakeValidationService
import com.localnovel.engine.service.NovelizationPreflightValidationService
import com.localnovel.engine.service.NovelizationProseQualityAuditService
import com.localnovel.engine.service.NovelizationRevisedDraftIntakeValidationService
import com.localnovel.engine.service.NovelizationRevisionBriefService
import com.localnovel.engine.service.NovelizationScriptPacketPreflightService
import com.localnovel.engine.service.NovelizationWriterBriefValidationService
import java.nio.file.Files
import java.nio.file.Path

internal object NovelizationCli {
    fun validateNovelizationPreflight(options: Map<String, String>): Int {
        val reportPath = options["character-nuwa-report"]?.let { Path.of(it) }
        val gatePath = options["character-nuwa-gate"]?.let { Path.of(it) }
        if ((reportPath == null) == (gatePath == null)) {
            error("Provide exactly one of --character-nuwa-report or --character-nuwa-gate")
        }
        val characterNuwaReport = reportPath?.let {
            readCliJson<CharacterNuwaGateValidationReport>(it)
        } ?: CharacterNuwaGateValidationService().validate(
            gate = readCliJson<CharacterNuwaGateFile>(gatePath!!),
            gateFile = gatePath.toString(),
        )
        val report = NovelizationPreflightValidationService().validate(
            characterNuwaReport = characterNuwaReport,
            characterNuwaReportFile = reportPath?.toString().orEmpty(),
            characterNuwaGateFile = gatePath?.toString().orEmpty(),
            preflightId = options["preflight"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("characterNuwaGateId=${report.characterNuwaGateId}")
        println("characterNuwaVerdict=${report.evidence.characterNuwaVerdict}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createNovelizationWriterBriefTemplate(options: Map<String, String>): Int {
        val projectId = options["project"] ?: "project-id"
        val out = Path.of(options.requiredCliOption("out"))
        val template = NovelizationWriterBrief(
            briefId = options["brief"] ?: "$projectId-writer-brief",
            projectId = projectId,
            targetChapter = options["chapter"] ?: "chapter-id",
            sourcePreflightReport = options["preflight-report"] ?: "path/to/novelization_preflight_report.json",
            previousChapterRefs = listOf("path/to/accepted_previous_chapter.md"),
            acceptedChapterFacts = listOf("Replace with a fact that the next chapter must not contradict."),
            characterStates = listOf("Replace with a current character state that affects the next chapter."),
            openThreads = listOf("Replace with an unresolved thread the next chapter must respect."),
            mustAdvance = listOf("Replace with one concrete progression target."),
            mustNotRepeat = listOf("Replace with one completed beat that must not be replayed."),
            allowedScenes = listOf("Replace with allowed scene or location id."),
            knownUnknowns = listOf("Replace with a fact the draft must not invent."),
            tonePacingTarget = "Replace with tone and pacing target.",
            styleContract = "Replace with style contract or expression DNA reference.",
            characterNuwaReferences = listOf("Replace with Character Nuwa gate report or card id."),
            antiAiProseConstraints = listOf("No generic calibration slogans; use concrete pressure, action, cost, or refusal."),
            authorOnlyExcluded = true,
            reviewerNotes = "Draft planning only. This brief is not accepted canon and does not generate prose.",
        )
        writeCliJson(out, template)
        println("wrote=$out")
        println("briefId=${template.briefId}")
        println("targetChapter=${template.targetChapter}")
        return 0
    }

    fun validateNovelizationWriterBrief(options: Map<String, String>): Int {
        val briefPath = Path.of(options.requiredCliOption("brief"))
        val brief = readCliJson<NovelizationWriterBrief>(briefPath)
        val preflightReportPath = options["preflight-report"]?.let { Path.of(it) }
        val report = NovelizationWriterBriefValidationService().validate(
            brief = brief,
            briefFile = briefPath.toString(),
            sourcePreflightReport = preflightReportPath?.let { readCliJson<NovelizationPreflightReport>(it) },
            sourcePreflightReportFile = preflightReportPath?.toString().orEmpty(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("briefId=${report.briefId}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateNovelizationDraftIntake(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val writerBriefReportPath = Path.of(options.requiredCliOption("writer-brief-report"))
        val report = NovelizationDraftIntakeValidationService().validate(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            writerBriefReport = readCliJson<NovelizationWriterBriefValidationReport>(writerBriefReportPath),
            draftFile = draftPath.toString(),
            writerBriefReportFile = writerBriefReportPath.toString(),
            sourceType = options.requiredCliOption("source-type"),
            sourceLabel = options["source-label"] ?: "",
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("sourceType=${report.sourceType}")
        println("sourceLabel=${report.sourceLabel}")
        println("writerBriefVerdict=${report.evidence.writerBriefVerdict}")
        println("draftCharacters=${report.evidence.draftCharacters}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun auditNovelizationProseQuality(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val draftIntakeReportPath = Path.of(options.requiredCliOption("draft-intake-report"))
        val report = NovelizationProseQualityAuditService().audit(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            draftIntakeReport = readCliJson<NovelizationDraftIntakeReport>(draftIntakeReportPath),
            draftFile = draftPath.toString(),
            draftIntakeReportFile = draftIntakeReportPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("draftIntakeVerdict=${report.evidence.draftIntakeVerdict}")
        println("draftCharacters=${report.evidence.draftCharacters}")
        println("dialogueLines=${report.evidence.dialogueLineCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createNovelizationRevisionBrief(options: Map<String, String>): Int {
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val draftIntakeReportPath = Path.of(options.requiredCliOption("draft-intake-report"))
        val proseQualityReportPath = Path.of(options.requiredCliOption("prose-quality-report"))
        val continuityAnchorsPath = Path.of(options.requiredCliOption("continuity-anchors"))
        val report = NovelizationRevisionBriefService().build(
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            draftIntakeReport = readCliJson<NovelizationDraftIntakeReport>(draftIntakeReportPath),
            proseQualityReport = readCliJson<NovelizationProseQualityAuditReport>(proseQualityReportPath),
            continuityAnchors = readCliJson<NovelizationRevisionContinuityAnchors>(continuityAnchorsPath),
            briefId = options["brief"] ?: "",
            draftFile = draftPath.toString(),
            draftIntakeReportFile = draftIntakeReportPath.toString(),
            proseQualityAuditReportFile = proseQualityReportPath.toString(),
            continuityAnchorsFile = continuityAnchorsPath.toString(),
        )
        val out = Path.of(options.requiredCliOption("out"))
        writeCliJson(out, report)
        println("verdict=${report.verdict}")
        println("briefId=${report.briefId}")
        println("revisionRequired=${report.revisionRequired}")
        println("requiredFixes=${report.requiredFixes.size}")
        println("continuityAnchors=${report.continuityAnchors.size}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateNovelizationRevisedDraftIntake(options: Map<String, String>): Int {
        val originalDraftPath = Path.of(options.requiredCliOption("original-draft"))
        val revisedDraftPath = Path.of(options.requiredCliOption("revised-draft"))
        val revisionBriefReportPath = Path.of(options.requiredCliOption("revision-brief-report"))
        val declaredHandlingPath = options["declared-handling"]?.let { Path.of(it) }
        val report = NovelizationRevisedDraftIntakeValidationService().validate(
            originalDraftText = Files.readString(originalDraftPath).removePrefix("\uFEFF"),
            revisedDraftText = Files.readString(revisedDraftPath).removePrefix("\uFEFF"),
            revisionBriefReport = readCliJson<NovelizationRevisionBriefReport>(revisionBriefReportPath),
            declaredHandling = declaredHandlingPath?.let { readCliJson<NovelizationRevisionHandlingDeclaration>(it) }
                ?: NovelizationRevisionHandlingDeclaration(),
            originalDraftFile = originalDraftPath.toString(),
            revisedDraftFile = revisedDraftPath.toString(),
            revisionBriefReportFile = revisionBriefReportPath.toString(),
            declaredHandlingFile = declaredHandlingPath?.toString().orEmpty(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("revisionBriefId=${report.revisionBriefId}")
        println("revisionBriefVerdict=${report.evidence.revisionBriefVerdict}")
        println("originalCharacters=${report.evidence.originalCharacters}")
        println("revisedCharacters=${report.evidence.revisedCharacters}")
        println("declaredChecklist=${report.evidence.declaredChecklistCount}/${report.evidence.mandatoryChecklistCount}")
        println("declaredFixes=${report.evidence.declaredFixCount}/${report.evidence.requiredFixCount}")
        println("bannedResiduals=${report.evidence.bannedResidualCount}")
        println("continuityAnchors=${report.evidence.continuityAnchorPreservedCount}/${report.evidence.continuityAnchorCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun createNovelizationAcceptedDraftPacket(options: Map<String, String>): Int {
        val revisedDraftPath = Path.of(options.requiredCliOption("revised-draft"))
        val revisedDraftIntakeReportPath = Path.of(options.requiredCliOption("revised-draft-intake-report"))
        val revisionBriefReportPath = Path.of(options.requiredCliOption("revision-brief-report"))
        val continuityAnchorsPath = Path.of(options.requiredCliOption("continuity-anchors"))
        val report = NovelizationAcceptedDraftPacketService().build(
            revisedDraftText = Files.readString(revisedDraftPath).removePrefix("\uFEFF"),
            revisedDraftIntakeReport = readCliJson<NovelizationRevisedDraftIntakeReport>(revisedDraftIntakeReportPath),
            revisionBriefReport = readCliJson<NovelizationRevisionBriefReport>(revisionBriefReportPath),
            continuityAnchors = readCliJson<NovelizationRevisionContinuityAnchors>(continuityAnchorsPath),
            packetId = options["packet"] ?: "",
            revisedDraftFile = revisedDraftPath.toString(),
            revisedDraftIntakeReportFile = revisedDraftIntakeReportPath.toString(),
            revisionBriefReportFile = revisionBriefReportPath.toString(),
            continuityAnchorsFile = continuityAnchorsPath.toString(),
        )
        val out = Path.of(options.requiredCliOption("out"))
        writeCliJson(out, report)
        println("verdict=${report.verdict}")
        println("packetId=${report.packetId}")
        println("revisedDraftSha256=${report.revisedDraftContentSha256}")
        println("revisedDraftIntakeVerdict=${report.sourceReports.revisedDraftIntakeVerdict}")
        println("revisionBriefVerdict=${report.sourceReports.revisionBriefVerdict}")
        println("requiredFixesHandled=${report.requiredFixesHandledSummary.size}")
        println("protectedContinuityAnchors=${report.protectedContinuityAnchors.size}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun validateNovelizationScriptPacketPreflight(options: Map<String, String>): Int {
        val packetPath = Path.of(options.requiredCliOption("accepted-draft-packet"))
        val report = NovelizationScriptPacketPreflightService().validate(
            acceptedDraftPacket = readCliJson<NovelizationAcceptedDraftPacket>(packetPath),
            preflightId = options["preflight"] ?: "",
            acceptedDraftPacketFile = packetPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("preflightId=${report.preflightId}")
        println("acceptedDraftPacketId=${report.acceptedDraftPacketId}")
        println("nextAllowedDownstreamStep=${report.nextAllowedDownstreamStep}")
        println("packetVerdict=${report.evidence.packetVerdict}")
        println("sourceReportFiles=${report.evidence.sourceReportFileCount}")
        println("sourceReportVerdicts=${report.evidence.sourceReportVerdictCount}")
        println("protectedContinuityAnchors=${report.evidence.protectedContinuityAnchorCount}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.path}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }

    fun auditChapterContinuity(options: Map<String, String>): Int {
        val previousPath = Path.of(options.requiredCliOption("previous"))
        val draftPath = Path.of(options.requiredCliOption("draft"))
        val anchorPath = Path.of(options.requiredCliOption("anchors"))
        val report = ChapterContinuityAuditService().audit(
            previousText = Files.readString(previousPath).removePrefix("\uFEFF"),
            draftText = Files.readString(draftPath).removePrefix("\uFEFF"),
            anchors = readCliJson<ChapterContinuityAnchorFile>(anchorPath),
            previousFile = previousPath.toString(),
            draftFile = draftPath.toString(),
            anchorFile = anchorPath.toString(),
        )
        options["out"]?.let { writeCliJson(Path.of(it), report) }
        println("verdict=${report.verdict}")
        println("auditId=${report.auditId}")
        println("previous=${report.previousFile}")
        println("draft=${report.draftFile}")
        println("findings=${report.findingCount}")
        report.findings.forEach {
            println("${it.ruleId}:${it.severity}:${it.checkId}:${it.message}")
        }
        return if (report.verdict == "FAIL") 1 else 0
    }
}
