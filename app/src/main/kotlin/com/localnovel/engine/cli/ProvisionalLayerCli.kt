package com.localnovel.engine.cli

import com.localnovel.engine.service.ProvisionalLayerCharterImporter
import com.localnovel.engine.service.ProvisionalLayerService
import java.nio.file.Path

internal object ProvisionalLayerCli {
    fun importCanonThinZones(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val result = ProvisionalLayerCharterImporter(repository).importCharterFile(
            projectId = options.requiredCliOption("project"),
            charterPath = Path.of(options.requiredCliOption("file")),
        )
        options["out"]?.let { writeCliJson(Path.of(it), result) }
        println("verdict=${result.verdict}")
        println("status=${result.status}")
        println("projectId=${result.projectId}")
        println("importedCount=${result.importedCount}")
        println("activeCount=${result.activeCount}")
        return if (result.verdict == "PASS") 0 else 1
    }

    fun listCanonThinZones(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val zones = repository.listCanonThinZones(options.requiredCliOption("project"))
        options["out"]?.let { writeCliJson(Path.of(it), zones) }
        zones.forEach {
            println("- ${it.thinZoneId} [${it.status}] ${it.label} -> ${it.allowedProposalTypes.joinToString(",")}")
        }
        return 0
    }

    fun listProvisionalEntities(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val entities = repository.listProvisionalEntities(options.requiredCliOption("session"))
        options["out"]?.let { writeCliJson(Path.of(it), entities) }
        entities.forEach {
            println("- ${it.entityId} ${it.displayName} [${it.status}] zone=${it.thinZoneId} promo=${it.promotionQueueStatus.orEmpty()}")
        }
        return 0
    }

    fun exportProvisionalBacklog(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val backlog = ProvisionalLayerService(repository).exportBacklog(options.requiredCliOption("session"))
        options["out"]?.let { writeCliJson(Path.of(it), backlog) } ?: error("Missing required option --out")
        println("sessionId=${backlog.sessionId}")
        println("activeEntities=${backlog.activeEntities.size}")
        println("pendingAiRecommendations=${backlog.pendingAiRecommendations.size}")
        return 0
    }

    fun discardProvisionalEntity(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        ProvisionalLayerService(repository).discardEntity(
            sessionId = options.requiredCliOption("session"),
            entityId = options.requiredCliOption("entity"),
            actor = options["actor"] ?: "author",
            note = options["note"] ?: "author discard",
        )
        println("discarded=${options.requiredCliOption("entity")}")
        return 0
    }

    fun promoteProvisionalEntity(options: Map<String, String>): Int {
        val repository = cliRepository(options)
        repository.initialize()
        val draft = ProvisionalLayerService(repository).buildPromotionPatchDraft(
            sessionId = options.requiredCliOption("session"),
            entityId = options.requiredCliOption("entity"),
        )
        val out = Path.of(options.requiredCliOption("out"))
        writeCliJson(out, draft)
        println("patchId=${draft.patchId}")
        println("displayName=${draft.displayName}")
        println("out=$out")
        println("note=${draft.note}")
        return 0
    }
}