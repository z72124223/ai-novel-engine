package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererManifestDraftJsonDraft
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.decodeFromString

class LocalRendererManifestDraftJsonDraftCliTest {
    @Test
    fun cliWritesLocalRendererManifestDraftJsonDraft() {
        val tempDir = Files.createTempDirectory("local-renderer-manifest-draft-json-draft")
        val outPath = tempDir.resolve("manifest_draft_json_draft.json")

        val exit = Cli().run(
            listOf(
                "draft-local-renderer-manifest-json",
                "--plan",
                LocalRendererManifestDraftJsonDraftServiceTest.samplePlanPath.toString(),
                "--plan-review",
                LocalRendererManifestDraftJsonDraftServiceTest.sampleReviewPath.toString(),
                "--draft",
                "manifest-draft-json-draft-cli-001",
                "--out",
                outPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(outPath))
        val draft = readJson<LocalRendererManifestDraftJsonDraft>(outPath)
        assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT", draft.status)
        assertEquals("manifest-draft-json-draft-cli-001", draft.draftId)
        assertEquals("chenxing-local-renderer-manifest-draft-plan-v0-001", draft.sourceManifestDraftPlanId)
        assertEquals("chenxing-local-renderer-manifest-draft-plan-review-v0-001", draft.sourceManifestDraftPlanReviewId)
        assertEquals("local-story-input-chenxing-v0-001", draft.sourceStoryInputPackId)
        assertTrue(draft.checks.isNotEmpty())
        assertTrue(draft.checks.all { it.status == "PASS" })
        assertTrue(draft.draftPanelManifestEntries.isNotEmpty())
        assertTrue(draft.draftFieldCoverageSummary.totalDraftFields > 0)
        assertTrue(draft.boundaries.draftManifestGenerated)
        assertFalse(draft.boundaries.productionManifestGenerated)
        assertFalse(draft.boundaries.productionReady)
        assertFalse(draft.boundaries.mediaGenerated)
        assertEquals("none", draft.boundaries.rendererInvocation)
        assertEquals("none", draft.boundaries.providerInvocation)
        assertEquals("none", draft.boundaries.ttsInvocation)
        assertFalse(draft.boundaries.adapterImplementationExecuted)
        assertTrue(draft.boundaries.noExternalPaidProviderTools)
        assertFalse(draft.boundaries.rendererReady)
        assertFalse(draft.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_manifest_draft_json_draft_only_not_production_manifest", draft.downstreamBoundary)
        assertEquals("local_renderer_manifest_draft_json_review", draft.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))
}
