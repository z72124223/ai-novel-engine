package com.localnovel.engine.cli

import com.localnovel.engine.model.StoryChapterDraftIntake
import com.localnovel.engine.model.StoryChapterDraftFileIntake
import com.localnovel.engine.model.StoryChapterHandoffRequest
import com.localnovel.engine.model.StoryChapterPilotSelection
import com.localnovel.engine.model.StoryChapterReviewBatch
import com.localnovel.engine.model.StoryChapterRevisionIntake
import com.localnovel.engine.model.StoryChapterRevisionFileIntake
import java.nio.file.Files
import com.localnovel.engine.repository.StoryChapterProductionRepository
import com.localnovel.engine.service.StoryChapterProductionService
import java.nio.file.Path

internal object StoryChapterProductionCli {
    fun createHandoff(options: Map<String, String>): Int {
        val artifact = StoryChapterProductionService(cliRepository(options)).createHandoff(
            readCliJson<StoryChapterHandoffRequest>(Path.of(options.requiredCliOption("input"))),
        )
        writeCliJson(Path.of(options.requiredCliOption("out")), artifact)
        println("status=DRAFT_REQUESTED")
        println("productionId=${artifact.packet.productionId}")
        println("sourcePacketSha256=${artifact.sourcePacketSha256}")
        return 0
    }

    fun ingestDraft(options: Map<String, String>): Int = mutate(options) { chapter ->
        val input = readCliJson<StoryChapterDraftIntake>(Path.of(options.requiredCliOption("input")))
        chapter.ingestDraft(input)
        input.productionId
    }

    fun ingestDraftFile(options: Map<String, String>): Int = mutate(options) { chapter ->
        val input = readCliJson<StoryChapterDraftFileIntake>(Path.of(options.requiredCliOption("input")))
        chapter.ingestDraft(
            StoryChapterDraftIntake(
                productionId = input.productionId,
                sourcePacketSha256 = input.sourcePacketSha256,
                draft = Files.readString(Path.of(input.draftPath)).removePrefix("\uFEFF"),
                draftSha256 = input.draftSha256,
                modelMetadataJson = input.modelMetadataJson,
            ),
        )
        input.productionId
    }

    fun ingestReviews(options: Map<String, String>): Int = mutate(options) { chapter ->
        val batch = readCliJson<StoryChapterReviewBatch>(Path.of(options.requiredCliOption("input")))
        chapter.recordReviewBatch(batch.productionId, batch.reviews)
        batch.productionId
    }

    fun ingestRevision(options: Map<String, String>): Int = mutate(options) { chapter ->
        val input = readCliJson<StoryChapterRevisionIntake>(Path.of(options.requiredCliOption("input")))
        chapter.ingestRevision(input)
        input.productionId
    }

    fun ingestRevisionFile(options: Map<String, String>): Int = mutate(options) { chapter ->
        val input = readCliJson<StoryChapterRevisionFileIntake>(Path.of(options.requiredCliOption("input")))
        chapter.ingestRevision(
            StoryChapterRevisionIntake(
                productionId = input.productionId,
                sourceDraftSha256 = input.sourceDraftSha256,
                revisedDraft = Files.readString(Path.of(input.revisedDraftPath)).removePrefix("\uFEFF"),
                revisedDraftSha256 = input.revisedDraftSha256,
                modelMetadataJson = input.modelMetadataJson,
            ),
        )
        input.productionId
    }

    fun selectPilot(options: Map<String, String>): Int = mutate(options) { chapter ->
        val input = readCliJson<StoryChapterPilotSelection>(Path.of(options.requiredCliOption("input")))
        chapter.selectPilot(input)
        input.productionId
    }

    fun show(options: Map<String, String>): Int {
        val repository = cliRepository(options).also { it.initialize() }
        val view = StoryChapterProductionRepository(repository).get(options.requiredCliOption("production"))
        options["out"]?.let { writeCliJson(Path.of(it), view) }
        println("productionId=${view.productionId}")
        println("status=${view.status}")
        println("reviews=${view.reviews.size}")
        println("revisionCount=${view.revisionCount}")
        println("canonStatus=${view.canonStatus}")
        return 0
    }

    private inline fun mutate(
        options: Map<String, String>,
        block: (StoryChapterProductionRepository) -> String,
    ): Int {
        val repository = cliRepository(options).also { it.initialize() }
        val chapter = StoryChapterProductionRepository(repository)
        val productionId = block(chapter)
        val view = chapter.get(productionId)
        options["out"]?.let { writeCliJson(Path.of(it), view) }
        println("productionId=$productionId")
        println("status=${view.status}")
        println("revisionCount=${view.revisionCount}")
        println("canonStatus=${view.canonStatus}")
        return 0
    }
}
