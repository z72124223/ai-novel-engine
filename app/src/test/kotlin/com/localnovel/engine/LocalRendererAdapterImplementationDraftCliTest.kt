package com.localnovel.engine

import com.localnovel.engine.model.LocalRendererAdapterImplementationDraft
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LocalRendererAdapterImplementationDraftCliTest {
    @Test
    fun cliWritesLocalRendererAdapterImplementationDraft() {
        val tempDir = Files.createTempDirectory("local-renderer-adapter-implementation-draft")
        val handoffPath = tempDir.resolve("preview_handoff.json")
        val planPath = tempDir.resolve("adapter_implementation_plan.json")
        val planReviewPath = tempDir.resolve("adapter_implementation_plan_review.json")
        val draftPath = tempDir.resolve("adapter_implementation_draft.json")
        val (handoff, plan, planReview) = LocalRendererAdapterImplementationDraftServiceTest.validInputs()
        writeJson(handoffPath, handoff)
        writeJson(planPath, plan)
        writeJson(planReviewPath, planReview)

        val exit = Cli().run(
            listOf(
                "draft-local-renderer-adapter-implementation",
                "--handoff",
                handoffPath.toString(),
                "--plan",
                planPath.toString(),
                "--plan-review",
                planReviewPath.toString(),
                "--draft",
                "adapter-implementation-draft-cli-001",
                "--out",
                draftPath.toString(),
            ),
        )

        assertEquals(0, exit)
        assertTrue(Files.exists(draftPath))
        val draft = readJson<LocalRendererAdapterImplementationDraft>(draftPath)
        assertEquals("PASS", draft.verdict, draft.findings.joinToString { it.ruleId })
        assertEquals("LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT", draft.status)
        assertEquals("adapter-implementation-draft-cli-001", draft.draftId)
        assertEquals(plan.planId, draft.sourcePlanId)
        assertEquals(planReview.reviewId, draft.sourcePlanReviewId)
        assertEquals(plan.sourceDryRunId, draft.sourceDryRunId)
        assertEquals(handoff.handoffId, draft.sourceHandoffId)
        assertEquals(plan.sourceReviewId, draft.sourceHandoffReviewId)
        assertEquals(handoff.sourceStoryInputPackId, draft.sourceStoryInputPackId)
        assertEquals(3, draft.adapterMappingDraft.panelDrafts.size)
        assertTrue(draft.implementationTasks.isNotEmpty())
        assertTrue(draft.verificationPlan.isNotEmpty())
        assertTrue(draft.sourceAlignmentSummary.storyInputPackIdAligned)
        assertFalse(draft.boundaries.mediaGenerated)
        assertEquals("none", draft.boundaries.rendererInvocation)
        assertEquals("none", draft.boundaries.providerInvocation)
        assertEquals("none", draft.boundaries.ttsInvocation)
        assertFalse(draft.boundaries.manifestGenerated)
        assertFalse(draft.boundaries.adapterImplementationExecuted)
        assertTrue(draft.boundaries.noExternalPaidProviderTools)
        assertFalse(draft.boundaries.rendererReady)
        assertFalse(draft.boundaries.mp4ReadinessClaimed)
        assertEquals("local_renderer_adapter_implementation_draft_only_not_manifest_generation", draft.downstreamBoundary)
        assertEquals("local_renderer_adapter_implementation_draft_review", draft.nextAllowedDownstreamStep)
    }

    private inline fun <reified T> readJson(path: Path): T =
        JsonSupport.json.decodeFromString(Files.readString(path).removePrefix("\uFEFF"))

    private inline fun <reified T> writeJson(path: Path, value: T) {
        path.parent?.let { Files.createDirectories(it) }
        Files.writeString(path, JsonSupport.json.encodeToString(value))
    }
}
