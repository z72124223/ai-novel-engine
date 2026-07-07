package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestAdapterContractDraft
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestAdapterContractDraftCliTest {
    @Test
    fun cliWritesLocalRendererManifestAdapterContractDraft() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-adapter-contract-draft")
        val outPath = tempDir.resolve("manifest_adapter_contract_draft.json")

        val exit = Cli().run(
            listOf(
                "draft-local-renderer-manifest-adapter-contract",
                "--plan",
                LocalRendererManifestAdapterContractDraftServiceTest.samplePlanPath.toString(),
                "--plan-review",
                LocalRendererManifestAdapterContractDraftServiceTest.sampleReviewPath.toString(),
                "--draft",
                "manifest-adapter-contract-draft-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val draft = readJson<LocalRendererManifestAdapterContractDraft>(outPath)
        assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT", draft.status)
        assertEquals("manifest-adapter-contract-draft-cli-001", draft.draftId)
        assertEquals(
            "rain-gate-local-renderer-manifest-adapter-contract-plan-v0-001",
            draft.sourcePlanId,
        )
        assertEquals(
            "rain-gate-local-renderer-manifest-adapter-contract-plan-review-v0-001",
            draft.sourcePlanReviewId,
        )
        assertEquals("local-story-input-rain-gate-v0-001", draft.sourceStoryInputPackId)
        assertTrue(draft.checks.isNotEmpty())
        assertTrue(draft.checks.all { it.status == "PASS" })
        assertEquals(2, draft.requiredInputs.size)
        assertEquals(2, draft.normalizedFieldMapping.size)
        assertEquals(3, draft.adapterObligations.size)
        assertEquals(6, draft.failStopRules.size)
        assertEquals(3, draft.verificationRequirements.size)
        assertTrue(draft.boundaries.contractDraftGenerated)
        assertFalse(draft.boundaries.productionManifestGenerated)
        assertFalse(draft.boundaries.productionReady)
        assertFalse(draft.boundaries.productionApproved)
        assertFalse(draft.boundaries.mediaGenerated)
        assertEquals("none", draft.boundaries.rendererInvocation)
        assertEquals("none", draft.boundaries.providerInvocation)
        assertEquals("none", draft.boundaries.ttsInvocation)
        assertFalse(draft.boundaries.adapterImplementationExecuted)
        assertFalse(draft.boundaries.adapterExecuted)
        assertTrue(draft.boundaries.noExternalPaidProviderTools)
        assertFalse(draft.boundaries.rendererReady)
        assertFalse(draft.boundaries.mp4ReadinessClaimed)
        assertEquals(
            "local_renderer_manifest_adapter_contract_draft_only_not_adapter_execution",
            draft.downstreamBoundary,
        )
        assertEquals("local_renderer_manifest_adapter_contract_draft_review", draft.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
