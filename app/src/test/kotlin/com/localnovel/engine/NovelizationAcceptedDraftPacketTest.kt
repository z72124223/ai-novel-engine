package com.localnovel.engine

import com.localnovel.engine.model.NovelizationAcceptedDraftPacket
import com.localnovel.engine.model.NovelizationRevisedDraftIntakeReport
import com.localnovel.engine.model.NovelizationRevisionBriefReport
import com.localnovel.engine.model.NovelizationRevisionContinuityAnchors
import com.localnovel.engine.model.NovelizationRevisionItem
import com.localnovel.engine.service.NovelizationAcceptedDraftPacketService
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NovelizationAcceptedDraftPacketTest {
    @Test
    fun passRevisedDraftIntakeBuildsAcceptedPacket() {
        val packet = NovelizationAcceptedDraftPacketService().build(
            revisedDraftText = revisedDraft(),
            revisedDraftIntakeReport = revisedDraftIntakeReport("PASS"),
            revisionBriefReport = revisionBrief(),
            continuityAnchors = continuityAnchors(),
            packetId = "accepted-draft-packet-001",
            revisedDraftFile = "drafts/revised.md",
            revisedDraftIntakeReportFile = "reports/revised_draft_intake.json",
            revisionBriefReportFile = "reports/revision_brief.json",
            continuityAnchorsFile = "reports/continuity_anchors.json",
        )

        assertEquals("PASS", packet.verdict)
        assertEquals("accepted-draft-packet-001", packet.packetId)
        assertTrue(packet.revisedDraftContentSha256.matches(Regex("[0-9a-f]{64}")))
        assertEquals("PASS", packet.sourceReports.revisedDraftIntakeVerdict)
        assertEquals("PASS", packet.sourceReports.revisionBriefVerdict)
        assertEquals(1, packet.requiredFixesHandledSummary.size)
        assertTrue(packet.protectedContinuityAnchors.contains("Mira already paid the door toll before reaching the checkpoint."))
        assertTrue(packet.remainingResidualRisks.isNotEmpty())
        assertEquals("script_packet_draft", packet.nextAllowedDownstreamStep)
    }

    @Test
    fun nonPassRevisedDraftIntakeBlocksPassPacket() {
        val packet = NovelizationAcceptedDraftPacketService().build(
            revisedDraftText = revisedDraft(),
            revisedDraftIntakeReport = revisedDraftIntakeReport("FAIL"),
            revisionBriefReport = revisionBrief(),
            continuityAnchors = continuityAnchors(),
            packetId = "accepted-draft-packet-001",
        )

        assertEquals("FAIL", packet.verdict)
        assertTrue(packet.findings.any { it.ruleId == "REVISED_DRAFT_INTAKE_NOT_PASS" })
        assertEquals("", packet.nextAllowedDownstreamStep)
    }

    @Test
    fun cliWritesAcceptedDraftPacketWithoutDatabase() {
        val tempDir = Files.createTempDirectory("accepted-draft-packet")
        val revisedDraftPath = tempDir.resolve("revised.md")
        val revisedDraftIntakePath = tempDir.resolve("revised_draft_intake_report.json")
        val revisionBriefPath = tempDir.resolve("revision_brief_report.json")
        val continuityAnchorsPath = tempDir.resolve("continuity_anchors.json")
        val out = tempDir.resolve("accepted_draft_packet.json")
        Files.writeString(revisedDraftPath, revisedDraft())
        writeJson(revisedDraftIntakePath, revisedDraftIntakeReport("PASS"))
        writeJson(revisionBriefPath, revisionBrief())
        writeJson(continuityAnchorsPath, continuityAnchors())

        val exit = Cli().run(
            listOf(
                "create-novelization-accepted-draft-packet",
                "--revised-draft",
                revisedDraftPath.toString(),
                "--revised-draft-intake-report",
                revisedDraftIntakePath.toString(),
                "--revision-brief-report",
                revisionBriefPath.toString(),
                "--continuity-anchors",
                continuityAnchorsPath.toString(),
                "--packet",
                "accepted-draft-packet-cli-001",
                "--out",
                out.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(out))
        val packet = readJson<NovelizationAcceptedDraftPacket>(out)
        assertEquals("PASS", packet.verdict)
        assertEquals("accepted-draft-packet-cli-001", packet.packetId)
        assertTrue(packet.revisedDraftContentSha256.matches(Regex("[0-9a-f]{64}")))
        assertEquals("script_packet_draft", packet.nextAllowedDownstreamStep)
    }

    private fun revisedDraft(): String =
        """
        Mira pressed the paid door toll receipt flat against the checkpoint board.
        The guard kept his hand on the visible mark slot before answering questions.
        She did not argue about destiny. She asked what the delay would cost.
        """.trimIndent()

    private fun revisedDraftIntakeReport(verdict: String): NovelizationRevisedDraftIntakeReport =
        NovelizationRevisedDraftIntakeReport(
            verdict = verdict,
            revisionBriefId = "revision-brief-sample-001",
        )

    private fun revisionBrief(): NovelizationRevisionBriefReport =
        NovelizationRevisionBriefReport(
            verdict = "PASS",
            briefId = "revision-brief-sample-001",
            revisionRequired = true,
            requiredFixes = listOf(
                NovelizationRevisionItem(
                    itemId = "revision-item-001",
                    fixType = "banned_phrase_removal",
                    sourceRuleId = "BANNED_CALIBRATION_STRUCTURE_CALIBRATION_EN_NOT_BUT",
                    category = "anti_ai_prose",
                    severity = "CRITICAL",
                ),
            ),
            continuityAnchors = listOf(
                "Mira already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
            doNotChange = listOf("Do not reset the toll payment as unpaid."),
        )

    private fun continuityAnchors(): NovelizationRevisionContinuityAnchors =
        NovelizationRevisionContinuityAnchors(
            anchorFileId = "continuity-anchors-sample-001",
            anchors = listOf(
                "Mira already paid the door toll before reaching the checkpoint.",
                "The guard requires a visible mark before answering questions.",
            ),
            doNotChange = listOf("Do not reset the toll payment as unpaid."),
        )

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
