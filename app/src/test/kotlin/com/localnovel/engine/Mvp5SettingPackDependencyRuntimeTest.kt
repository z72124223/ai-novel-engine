package com.localnovel.engine

import com.localnovel.engine.model.DemoProjectSeed
import com.localnovel.engine.model.SettingPack
import com.localnovel.engine.model.SettingPackDependency
import com.localnovel.engine.model.SettingPackEntities
import com.localnovel.engine.model.SettingPackEntity
import com.localnovel.engine.model.SettingPackRelation
import com.localnovel.engine.model.SettingPackSource
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.service.CanonConsistencyValidator
import com.localnovel.engine.service.CanonSemanticValidator
import com.localnovel.engine.service.ContextCompiler
import com.localnovel.engine.service.DependencyGraphService
import com.localnovel.engine.service.MultiTurnRuntime
import com.localnovel.engine.service.SettingContextResolver
import com.localnovel.engine.service.SettingPackDiffService
import com.localnovel.engine.service.SettingPackImportService
import com.localnovel.engine.service.SettingPackParser
import com.localnovel.engine.service.SettingPackSchemaValidator
import com.localnovel.engine.service.canonicalHash
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class Mvp5SettingPackDependencyRuntimeTest {
    @Test
    fun migration005CreatesSettingPackTablesAndDependencyColumns() {
        val repository = newRepository()
        repository.initialize()
        SettingPackImportService(repository).apply(validPack())

        assertTrue(repository.listTables().containsAll(setOf("setting_packs", "setting_pack_imports", "entity_revisions", "canon_validation_runs", "impact_analysis_runs")))
        val dependencyColumns = repository.listDependencyRows("mvp5").first().keys
        assertTrue(dependencyColumns.containsAll(setOf("source_pack_id", "source_pack_version", "visibility", "runtime_relevant", "strength", "reason", "metadata_json", "canonical_hash", "status")))
    }

    @Test
    fun parserReadsUtf8WithBom() {
        val path = Files.createTempDirectory("mvp5-pack").resolve("pack.json")
        Files.writeString(path, "\uFEFF" + JsonSupport.json.encodeToString(validPack(name = "霜雪設定包")))

        val pack = SettingPackParser().parse(path)

        assertEquals("霜雪設定包", pack.name)
    }

    @Test
    fun schemaRejectsMissingSourceAuthor() {
        val repository = newRepository()
        repository.initialize()
        val report = SettingPackSchemaValidator(repository).validate(validPack(source = SettingPackSource()))

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "missing_source_author" })
    }

    @Test
    fun schemaRejectsUnsupportedMode() {
        val repository = newRepository()
        repository.initialize()
        val report = SettingPackSchemaValidator(repository).validate(validPack(mode = "HOT_RELOAD"))

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "invalid_mode" })
    }

    @Test
    fun schemaRejectsDuplicateEntityId() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(entities = validPack().entities.copy(items = validPack().entities.items + validPack().entities.items.first()))

        val report = SettingPackSchemaValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "duplicate_entity" })
    }

    @Test
    fun schemaRejectsBadIdCharacters() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(entities = validPack().entities.copy(items = listOf(validPack().entities.items.first().copy(id = "bad id"))))

        val report = SettingPackSchemaValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "invalid_id" })
    }

    @Test
    fun schemaRequiresImportedDependencies() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack(dependencies = listOf(SettingPackDependency("missing-pack", 1)))

        val report = SettingPackSchemaValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "missing_dependency" })
    }

    @Test
    fun validBasePackPreviewsClean() {
        val repository = newRepository()
        repository.initialize()

        val preview = SettingPackImportService(repository).preview(validPack())

        assertTrue(preview.validation.isValid, preview.validation.issues.joinToString { it.message })
        assertTrue(preview.diff.entries.any { it.action == "ADD" })
    }

    @Test
    fun applyLegalPackCreatesProjectAndEntities() {
        val repository = newRepository()
        repository.initialize()

        SettingPackImportService(repository).apply(validPack())

        assertNotNull(repository.getProject("mvp5"))
        assertTrue(repository.targetExists("mvp5", "character", "char-shen"))
        assertTrue(repository.targetExists("mvp5", "item", "item-jade-bell"))
        assertTrue(repository.targetExists("mvp5", "technique", "technique-frost-step"))
    }

    @Test
    fun applyWritesImportAndEntityRevisions() {
        val repository = newRepository()
        repository.initialize()

        val result = SettingPackImportService(repository).apply(validPack())

        assertTrue(result.applied)
        assertEquals(1, repository.tableCount("setting_pack_imports"))
        assertTrue(repository.listEntityRevisionRows("mvp5").size >= 8)
    }

    @Test
    fun applyWritesDependencyEdgesWithProvenance() {
        val repository = newRepository()
        repository.initialize()

        SettingPackImportService(repository).apply(validPack())

        val edges = repository.listDependencyRows("mvp5")
        assertTrue(edges.any { it["source_pack_id"] == "pack-shrine" && it["runtime_relevant"] == "1" })
        assertTrue(edges.any { it["reason"] == "沈霜持有玉鈴。" })
    }

    @Test
    fun idempotentApplyDoesNotDuplicateRevisions() {
        val repository = newRepository()
        repository.initialize()
        val service = SettingPackImportService(repository)
        service.apply(validPack())
        val before = repository.tableCount("entity_revisions")

        val second = service.apply(validPack())

        assertFalse(second.applied)
        assertEquals(before, repository.tableCount("entity_revisions"))
    }

    @Test
    fun samePackVersionWithDifferentHashIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val service = SettingPackImportService(repository)
        service.apply(validPack())

        assertFailsWith<IllegalStateException> {
            service.apply(validPack(description = "changed"))
        }
    }

    @Test
    fun diffReportsNoChangeAfterApply() {
        val repository = newRepository()
        repository.initialize()
        SettingPackImportService(repository).apply(validPack())

        val diff = SettingPackDiffService(repository).diff(validPack(), canonicalHash(validPack()))

        assertTrue(diff.entries.all { it.action == "NO_CHANGE" })
    }

    @Test
    fun diffReportsUpdateForClosedSessionPatch() {
        val repository = seededDemoRepository()
        val service = SettingPackImportService(repository)
        service.apply(validPack(projectId = "demo", packId = "pack-demo"))
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")
        repository.closeSession(session.id)
        val patch = patchPack(projectId = "demo", packId = "pack-demo-patch", itemVersion = 2)

        val preview = service.preview(patch)

        assertTrue(preview.validation.isValid, preview.validation.issues.joinToString { it.message })
        assertTrue(preview.diff.entries.any { it.action == "UPDATE" && it.entityId == "item-jade-bell" })
    }

    @Test
    fun activeSessionPatchIsBlocked() {
        val repository = seededDemoRepository()
        val service = SettingPackImportService(repository)
        service.apply(validPack(projectId = "demo", packId = "pack-demo"))
        MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        val preview = service.preview(patchPack(projectId = "demo", packId = "pack-demo-patch", itemVersion = 2))

        assertFalse(preview.validation.isValid)
        assertTrue(preview.validation.issues.any { it.code == "active_session_patch_blocked" })
    }

    @Test
    fun closedSessionPatchCanApplyAndCreatesNewRevision() {
        val repository = seededDemoRepository()
        val service = SettingPackImportService(repository)
        service.apply(validPack(projectId = "demo", packId = "pack-demo"))
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")
        repository.closeSession(session.id)

        val result = service.apply(patchPack(projectId = "demo", packId = "pack-demo-patch", itemVersion = 2))

        assertTrue(result.applied)
        assertEquals("2", repository.latestEntityRevision("demo", "item", "item-jade-bell")?.get("entity_version"))
    }

    @Test
    fun impactReportIncludesActiveSessionBlocker() {
        val repository = seededDemoRepository()
        val service = SettingPackImportService(repository)
        service.apply(validPack(projectId = "demo", packId = "pack-demo"))
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        val preview = service.preview(patchPack(projectId = "demo", packId = "pack-demo-patch", itemVersion = 2))

        assertTrue(preview.impact.impactedSessions.contains(session.id))
        assertTrue(preview.impact.blockers.contains("active_session_patch_blocked"))
    }

    @Test
    fun dependencyGraphTraversalIsDeterministic() {
        val repository = newRepository()
        repository.initialize()
        SettingPackImportService(repository).apply(validPack())
        val graph = DependencyGraphService(repository)

        val first = graph.show("mvp5", "character", "char-shen", 2)
        val second = graph.show("mvp5", "character", "char-shen", 2)

        assertEquals(first, second)
        assertTrue(first.any { it.contains("item-jade-bell") })
    }

    @Test
    fun contextResolverIncludesRelevantSettingsSubset() {
        val repository = seededDemoRepository()
        SettingPackImportService(repository).apply(validPack(projectId = "demo", packId = "pack-demo"))
        val context = ContextCompiler(repository).compile("demo", "看向玉鈴。")

        assertTrue(context.relevantSettings.isNotEmpty())
        assertTrue(context.settingContextBudget.actualBytes <= context.settingContextBudget.maxBytes)
        assertFalse(context.relevantSettings.any { it.entityId == "secret-author-root" })
    }

    @Test
    fun authorOnlySecretDoesNotEnterCharacterFacingContext() {
        val repository = seededDemoRepository()
        SettingPackImportService(repository).apply(validPack(projectId = "demo", packId = "pack-demo"))
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "她是否知道源頭？")

        assertFalse(context.relevantSettings.any { it.visibility == "author_only" })
        assertFalse(JsonSupport.json.encodeToString(context).contains("作者專用祕密"))
    }

    @Test
    fun characterStartingKnowledgeCannotContainAuthorOnlySecret() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(
            entities = validPack().entities.copy(
                characters = validPack().entities.characters.map { it.copy(startingKnowledge = listOf("secret-author-root")) },
            ),
        )

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "author_only_knowledge_leak" })
    }

    @Test
    fun authorOnlySecretCanBeKnownByDeclaredCharacter() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(
            entities = validPack().entities.copy(
                characters = validPack().entities.characters.map { it.copy(startingKnowledge = listOf("secret-author-root")) },
                secrets = validPack().entities.secrets.map { it.copy(knownBy = listOf("char-shen")) },
            ),
        )

        val report = CanonSemanticValidator(repository).validate(pack)

        assertTrue(report.isValid, report.issues.joinToString { it.message })
    }

    @Test
    fun powerfulTechniqueWithoutCostIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val technique = validPack().entities.techniques.first().copy(costs = emptyList(), limitations = emptyList(), effects = listOf("powerful frost step"))
        val pack = validPack().copy(entities = validPack().entities.copy(techniques = listOf(technique)))

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "unbounded_powerful_technique" })
    }

    @Test
    fun techniqueCycleIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(
            entities = validPack().entities.copy(
                techniques = listOf(
                    validPack().entities.techniques.first().copy(id = "tech-a", prerequisites = listOf("tech-b")),
                    validPack().entities.techniques.first().copy(id = "tech-b", prerequisites = listOf("tech-a")),
                ),
            ),
        )

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "dependency_cycle" })
    }

    @Test
    fun locationCycleIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(
            entities = validPack().entities.copy(
                locations = listOf(
                    SettingPackEntity("loc-a", name = "A", parentLocationId = "loc-b"),
                    SettingPackEntity("loc-b", name = "B", parentLocationId = "loc-a"),
                ),
            ),
        )

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "dependency_cycle" })
    }

    @Test
    fun unknownRelationEndpointIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(relations = validPack().relations + SettingPackRelation("rel-missing", "character", "char-missing", "item", "item-jade-bell", "OWNS"))

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "unknown_reference" })
    }

    @Test
    fun incompatibleRelationIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(relations = validPack().relations + SettingPackRelation("rel-bad", "item", "item-jade-bell", "character", "char-shen", "OWNS"))

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "incompatible_relation" })
    }

    @Test
    fun itemOwnerAndLocationConflictIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val pack = validPack().copy(entities = validPack().entities.copy(items = listOf(validPack().entities.items.first().copy(initialOwnerId = "char-shen", initialLocationId = "loc-shrine"))))

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "item_location_owner_conflict" })
    }

    @Test
    fun sceneContractUnknownSceneIsRejected() {
        val repository = newRepository()
        repository.initialize()
        val contract = validPack().entities.sceneContracts.first().copy(sceneId = "scene-missing")
        val pack = validPack().copy(entities = validPack().entities.copy(sceneContracts = listOf(contract)))

        val report = CanonSemanticValidator(repository).validate(pack)

        assertFalse(report.isValid)
        assertTrue(report.issues.any { it.code == "unknown_reference" })
    }

    @Test
    fun exportImportRoundTripPreservesUtf8AndHash() {
        val repository = newRepository()
        repository.initialize()
        val service = SettingPackImportService(repository)
        val pack = validPack(name = "霜雪與玉鈴")
        service.apply(pack)

        val exported = service.export("mvp5", "pack-shrine", 1)

        assertEquals("霜雪與玉鈴", exported.name)
        assertEquals(canonicalHash(pack), canonicalHash(exported))
    }

    @Test
    fun validateProjectCanonPassesAfterImport() {
        val repository = newRepository()
        repository.initialize()
        SettingPackImportService(repository).apply(validPack())

        val report = CanonConsistencyValidator(repository).validate("mvp5")

        assertTrue(report.isValid, report.issues.joinToString { it.message })
    }

    @Test
    fun rejectedPackDoesNotWriteFormalTables() {
        val repository = newRepository()
        repository.initialize()
        val beforeImports = repository.tableCount("setting_pack_imports")
        val beforeRevisions = repository.tableCount("entity_revisions")
        val invalid = validPack().copy(entities = validPack().entities.copy(techniques = listOf(validPack().entities.techniques.first().copy(costs = emptyList(), limitations = emptyList(), effects = listOf("powerful")))))

        val preview = SettingPackImportService(repository).preview(invalid)

        assertFalse(preview.validation.isValid)
        assertFailsWith<IllegalStateException> { SettingPackImportService(repository).apply(invalid) }
        assertEquals(beforeImports, repository.tableCount("setting_pack_imports"))
        assertEquals(beforeRevisions, repository.tableCount("entity_revisions"))
    }

    @Test
    fun scale100EntityFixtureDoesNotFloodTurnContext() {
        val repository = seededDemoRepository()
        val pack = validPack(projectId = "demo", packId = "pack-scale").copy(
            entities = validPack(projectId = "demo", packId = "pack-scale").entities.copy(
                items = validPack(projectId = "demo", packId = "pack-scale").entities.items + (1..100).map { index ->
                    SettingPackEntity(
                        id = "item-scale-$index",
                        name = "比例道具 $index",
                        description = "用來測試設定包規模的道具 $index",
                        initialLocationId = null,
                        visibility = "public",
                    )
                },
            ),
            relations = emptyList(),
        )
        SettingPackImportService(repository).apply(pack)
        val session = MultiTurnRuntime(repository).startSession("demo", "scene-shrine-questioning")

        val context = MultiTurnRuntime(repository).prepareTurn(session.id, "檢查附近物件。")

        assertTrue(repository.listEntityRevisionRows("demo", "item").size >= 100)
        assertTrue(context.relevantSettings.size < 100)
        assertTrue(context.settingContextBudget.actualBytes <= context.settingContextBudget.maxBytes)
    }

    @Test
    fun cliValidatePreviewApplyExportE2E() {
        val db = Files.createTempDirectory("mvp5-cli").resolve("story.db")
        val file = Files.createTempDirectory("mvp5-cli-pack").resolve("pack.json")
        Files.writeString(file, JsonSupport.json.encodeToString(validPack(projectId = "cli-mvp5", packId = "pack-cli")))
        val cli = Cli()

        assertEquals(0, cli.run(listOf("init-db", "--db", db.toString())))
        assertEquals(0, cli.run(listOf("validate-setting-pack", "--db", db.toString(), "--file", file.toString())))
        assertEquals(0, cli.run(listOf("preview-setting-import", "--db", db.toString(), "--file", file.toString())))
        assertEquals(0, cli.run(listOf("apply-setting-pack", "--db", db.toString(), "--file", file.toString())))
        val export = file.parent.resolve("export.json")
        assertEquals(0, cli.run(listOf("export-setting-pack", "--db", db.toString(), "--project", "cli-mvp5", "--pack", "pack-cli", "--version", "1", "--out", export.toString())))
        assertTrue(Files.readString(export).contains("沈霜"))
    }

    private fun newRepository(): SqliteStoryRepository {
        val repository = SqliteStoryRepository(Files.createTempDirectory("story-engine-mvp5-test").resolve("story.db"))
        return repository
    }

    private fun seededDemoRepository(): SqliteStoryRepository {
        val repository = newRepository()
        repository.initialize()
        repository.seedDemo(readSeed())
        return repository
    }

    private fun validPack(
        projectId: String = "mvp5",
        packId: String = "pack-shrine",
        name: String = "Shrine Setting Pack",
        description: String = "Adds a compact shrine canon slice.",
        mode: String = "BASE",
        source: SettingPackSource = SettingPackSource(author = "local-author", license = "CC0"),
        dependencies: List<SettingPackDependency> = emptyList(),
    ): SettingPack =
        SettingPack(
            schemaVersion = "1.0",
            packId = packId,
            packVersion = 1,
            projectId = projectId,
            name = name,
            description = description,
            mode = mode,
            source = source,
            dependencies = dependencies,
            entities = SettingPackEntities(
                projects = if (projectId == "mvp5" || projectId == "cli-mvp5") listOf(SettingPackEntity(projectId, name = "霜雪祠堂", summary = "一座被霜雪包圍的祠堂。")) else emptyList(),
                locations = listOf(
                    SettingPackEntity("loc-shrine", name = "祠堂", description = "冷霜覆在門檻。"),
                    SettingPackEntity("loc-archive", name = "藏卷室", description = "紙灰和銅鈴聲停在暗處。", parentLocationId = "loc-shrine"),
                ),
                characters = listOf(
                    SettingPackEntity("char-shen", name = "沈霜", role = "守祠人", description = "她戒備但重諾。", startingItems = listOf("item-jade-bell"), startingTechniques = listOf("technique-frost-step")),
                ),
                worldRules = listOf(
                    SettingPackEntity("rule-vows-bind", title = "誓言有代價", content = "硬誓會留下可追索的代價。", priority = 10, metadata = JsonObject(mapOf("severity" to JsonPrimitive("hard"), "conflictKey" to JsonPrimitive("vows")))),
                ),
                items = listOf(
                    SettingPackEntity("item-jade-bell", name = "玉鈴", description = "鈴舌裂了一線。", initialOwnerId = "char-shen", tags = listOf("bell", "jade")),
                ),
                techniques = listOf(
                    SettingPackEntity("technique-frost-step", name = "霜步", description = "短距離踏霜避險。", ownerCharacterId = "char-shen", effects = listOf("quick evasion"), costs = listOf("breath"), limitations = listOf("cannot cross sealed doors")),
                ),
                facts = listOf(
                    SettingPackEntity("fact-bell-cracked", content = "玉鈴曾經裂過。", visibility = "public"),
                ),
                secrets = listOf(
                    SettingPackEntity("secret-author-root", content = "作者專用祕密：玉鈴源頭在地脈。", visibility = "author_only", revealCondition = "never in v1"),
                ),
                scenes = listOf(
                    SettingPackEntity("scene-archive", title = "藏卷室對峙", summary = "沈霜守在藏卷室門前。", locationId = "loc-archive"),
                ),
                sceneContracts = listOf(
                    SettingPackEntity("contract-archive", sceneId = "scene-archive", allowedSecretIds = emptyList(), forbiddenReveals = listOf("地脈")),
                ),
                storyThreads = listOf(
                    SettingPackEntity("thread-trust", title = "信任裂縫", threadType = "character", publicSummary = "沈霜決定是否相信來者。", directorGoal = "測試信任邊界。", visibility = "director_only"),
                ),
            ),
            relations = listOf(
                SettingPackRelation("rel-shen-located", "character", "char-shen", "location", "loc-archive", "LOCATED_IN", reason = "沈霜守在藏卷室。"),
                SettingPackRelation("rel-shen-owns-bell", "character", "char-shen", "item", "item-jade-bell", "OWNS", reason = "沈霜持有玉鈴。"),
                SettingPackRelation("rel-scene-location", "scene", "scene-archive", "location", "loc-archive", "LOCATED_IN", reason = "場景發生在藏卷室。"),
                SettingPackRelation("rel-tech-fact", "technique", "technique-frost-step", "fact", "fact-bell-cracked", "REQUIRES", reason = "霜步和玉鈴裂痕有關。"),
                SettingPackRelation("rel-thread-character", "story_thread", "thread-trust", "character", "char-shen", "AFFECTS", reason = "信任線影響沈霜。"),
            ),
        )

    private fun patchPack(projectId: String, packId: String, itemVersion: Int): SettingPack =
        validPack(projectId = projectId, packId = packId, mode = "PATCH_CANON").copy(
            entities = SettingPackEntities(
                items = listOf(
                    SettingPackEntity(
                        id = "item-jade-bell",
                        entityVersion = itemVersion,
                        name = "玉鈴",
                        description = "鈴舌裂痕旁多了一枚霜點。",
                        initialOwnerId = "char-shen",
                        tags = listOf("bell", "jade", "patched"),
                    ),
                ),
            ),
            relations = emptyList(),
        )

    private fun readSeed(): DemoProjectSeed =
        JsonSupport.json.decodeFromString(Files.readString(samplePath("demo_project_seed.json")))

    private fun samplePath(fileName: String): Path {
        val candidates = listOf(Path.of("samples", fileName), Path.of("..", "samples", fileName))
        return candidates.firstOrNull { Files.exists(it) } ?: error("Cannot find sample file: $fileName")
    }
}
