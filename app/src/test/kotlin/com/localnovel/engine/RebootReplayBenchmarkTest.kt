package com.localnovel.engine

import com.localnovel.engine.service.RebootReplayBenchmarkRunner
import com.localnovel.engine.service.RebootReplayAdoptionGate
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RebootReplayBenchmarkTest {
    @Test
    fun adoptsSinglePreflightRuleOnlyWhenReplayStateIsEquivalentAndRetryDrops() {
        val root = Files.createTempDirectory("reboot01-benchmark")
        val report = RebootReplayBenchmarkRunner(root).run(
            basePackPath = projectPath("world/chenxing/engine/chenxing_setting_pack_v1.json"),
            addonPackPath = projectPath("samples/reboot01/chenxing_vertical_slice_addon.json"),
            npcProfilePath = projectPath("samples/reboot01/lin_yao_offscreen_profile.json"),
            approvalGrantPath = projectPath("samples/reboot01/irreversible_approval_grant.json"),
        )

        assertEquals("ADOPT", report.decision)
        assertTrue(report.normalizedReplayEquivalent)
        assertEquals(1.0, report.baseline.legalApplyRate)
        assertEquals(1.0, report.evolved.legalApplyRate)
        assertEquals(report.baseline.autonomyScore, report.evolved.autonomyScore)
        assertEquals(report.baseline.irreversibleChangeCount, report.evolved.irreversibleChangeCount)
        assertEquals(report.baseline.fairSurprise, report.evolved.fairSurprise)
        assertEquals(report.baseline.readerAttachment, report.evolved.readerAttachment)
        assertEquals(1, report.baseline.retryCount)
        assertEquals(0, report.evolved.retryCount)
        assertTrue(Files.exists(root.resolve("benchmark-summary.json")))

        assertEquals(
            "REJECT",
            RebootReplayAdoptionGate.evaluate(report.baseline, report.evolved.copy(normalizedReplayFingerprint = "different")).decision,
        )
        assertEquals(
            "REJECT",
            RebootReplayAdoptionGate.evaluate(report.baseline, report.evolved.copy(irreversibleChangeCount = 0)).decision,
        )
        assertEquals(
            "REJECT",
            RebootReplayAdoptionGate.evaluate(report.baseline, report.evolved.copy(readerAttachment = report.baseline.readerAttachment - 1)).decision,
        )
    }

    private fun projectPath(relative: String): Path {
        val candidates = listOf(Path.of(relative), Path.of("..", relative))
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find project path: $relative")
    }
}
