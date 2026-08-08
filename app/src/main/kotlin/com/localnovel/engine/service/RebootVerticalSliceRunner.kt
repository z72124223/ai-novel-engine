package com.localnovel.engine.service

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.AudienceExpectationLedger
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.DramaEvaluationInput
import com.localnovel.engine.model.DramaticQuestion
import com.localnovel.engine.model.IrreversibleApprovalArtifact
import com.localnovel.engine.model.IrreversibleChangeContract
import com.localnovel.engine.model.NarrativeSignals
import com.localnovel.engine.model.NpcAutonomyProfile
import com.localnovel.engine.model.SceneDramaFrame
import com.localnovel.engine.model.SceneValueState
import com.localnovel.engine.model.ValueConflict
import com.localnovel.engine.repository.SqliteStoryRepository
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

@Serializable
data class RebootVerticalSliceRunSummary(
    val sessionId: String,
    val appliedTurns: Int,
    val stateVersion: Int,
    val elapsedMinutes: Int,
    val decisionTraceCount: Int,
    val dramaEvaluationCount: Int,
    val runEvidenceCount: Int,
    val irreversibleApprovalCount: Int,
    val outputDir: String,
    val scenarioSeed: String = "reboot01-chenxing-depth-v1",
    val ruleProfile: String = RebootVerticalSliceRuleProfile.PREFLIGHT_RECOGNIZED_DANGER_DRAFT.name,
)

@Serializable
data class RebootIrreversibleApprovalGrant(
    val grantId: String,
    val scenarioSeed: String,
    val changeId: String,
    val approvedChangeTypes: List<String>,
    val affectedEntityIds: List<String>,
    val humanActorId: String,
    val decision: String,
    val approvedAt: String,
    val authoritySource: String,
    val nonCanonOnly: Boolean,
) {
    fun requireAuthorizes(contract: IrreversibleChangeContract, expectedSeed: String) {
        require(scenarioSeed == expectedSeed) { "APPROVAL_GRANT_SEED_MISMATCH" }
        require(changeId == contract.changeId) { "APPROVAL_GRANT_CHANGE_MISMATCH" }
        require(approvedChangeTypes.sorted() == contract.changeTypes.sorted()) { "APPROVAL_GRANT_TYPE_MISMATCH" }
        require(affectedEntityIds.sorted() == contract.affectedEntityIds.sorted()) { "APPROVAL_GRANT_ENTITY_MISMATCH" }
        require(decision == "APPROVE") { "APPROVAL_GRANT_NOT_APPROVED" }
        require(nonCanonOnly) { "APPROVAL_GRANT_MUST_BE_NON_CANON" }
        require(authoritySource.isNotBlank()) { "APPROVAL_GRANT_AUTHORITY_REQUIRED" }
        require(humanActorId.startsWith("human:")) { "APPROVAL_GRANT_HUMAN_ACTOR_REQUIRED" }
        Instant.parse(approvedAt)
    }
}

enum class RebootVerticalSliceRuleProfile {
    CURRENT,
    PREFLIGHT_RECOGNIZED_DANGER_DRAFT,
}

class RebootVerticalSliceRunner(
    private val repository: SqliteStoryRepository,
    private val outputDir: Path,
    private val ruleProfile: RebootVerticalSliceRuleProfile = RebootVerticalSliceRuleProfile.PREFLIGHT_RECOGNIZED_DANGER_DRAFT,
) {
    private val runtime = MultiTurnRuntime(repository)
    private val scenarioSeed = "reboot01-chenxing-depth-v1"

    companion object {
        val defaultPlayerInputs = listOf(
            "先離開潮間帶，別讓看得見的危險替我們做決定。",
            "讓留在現場的人先保存證據，我們從高處觀察。",
            "等水線穩住再回去，看誰趁我們不在動過現場。",
            "先找退路，再追碎片。明知道是陷阱還直走，那叫替對方省事。",
            "潮溝要塌了，先把人拉出來。",
            "先救人，再問他把東西交給了誰。線索可以追，手接不回來。",
        )
    }

    fun run(
        basePackPath: Path,
        addonPackPath: Path,
        npcProfilePath: Path,
        approvalGrantPath: Path,
        playerInputs: List<String> = defaultPlayerInputs,
        maxTurns: Int = 6,
    ): RebootVerticalSliceRunSummary {
        require(maxTurns in 1..6) { "REBOOT_SLICE_TURN_LIMIT_INVALID" }
        require(playerInputs.size >= maxTurns && playerInputs.take(maxTurns).all { it.isNotBlank() && it.length <= 2000 }) {
            "REBOOT_SLICE_PLAYER_INPUT_INVALID"
        }
        Files.createDirectories(outputDir)
        repository.initialize()
        val basePack = SettingPackParser().parse(basePackPath)
        val addonPack = SettingPackParser().parse(addonPackPath)
        requireSandboxWorldPatch(addonPack)
        writeJson("setting-pack-base-apply.json", SettingPackImportService(repository).apply(basePack, basePackPath.toString()))
        val patchPreview = SettingPackImportService(repository).preview(addonPack)
        writeJson("world-patch-parse.json", buildJsonObject {
            put("path", addonPackPath.toString().replace('\\', '/'))
            put("sha256", sha256(addonPackPath))
            put("packId", addonPack.packId)
            put("packVersion", addonPack.packVersion)
            put("projectId", addonPack.projectId)
            put("nonCanon", addonPack.metadata["nonCanon"] ?: JsonPrimitive(false))
            put("resettable", addonPack.metadata["resettable"] ?: JsonPrimitive(false))
        })
        writeJson("world-patch-validation.json", patchPreview.validation)
        writeJson("world-patch-diff.json", patchPreview.diff)
        writeJson("world-patch-impact-preview.json", patchPreview.impact)
        val patchApply = SettingPackImportService(repository).apply(addonPack, addonPackPath.toString())
        writeJson("world-patch-sandbox-apply.json", patchApply)
        writeJson("setting-pack-addon-apply.json", patchApply)
        val session = runtime.startSession("chenxing", "scene-daping-coast-opening", readerMode = true)
        val profile = JsonSupport.json.decodeFromString<NpcAutonomyProfile>(Files.readString(npcProfilePath).removePrefix("\uFEFF"))
        val approvalGrant = JsonSupport.json.decodeFromString<RebootIrreversibleApprovalGrant>(Files.readString(approvalGrantPath).removePrefix("\uFEFF"))

        val recognizedDangerDraft = "陸塵行看著水線，只說了一句『這地方連退場提示都省了。』下一秒，他收起玩笑，把愛醬帶向山坡。"
        val usePreflightDraft = ruleProfile == RebootVerticalSliceRuleProfile.PREFLIGHT_RECOGNIZED_DANGER_DRAFT
        runTurn(
            index = 1,
            sessionId = session.id,
            playerInput = playerInputs[0],
            rawDraft = if (usePreflightDraft) recognizedDangerDraft else "陸塵行看著水線，嘴上嫌這地方連退場提示都省了，手上卻已把愛醬帶向高處。",
            revisedDraft = if (usePreflightDraft) "" else recognizedDangerDraft,
            signals = goodSignals(sarcasm = true, danger = true, socialCost = true, warm = true),
            actions = listOf(action("char-lu-chenxing", "帶愛醬撤往高處", "確認明顯危險後撤離"), action("char-aijiang", "跟隨陸塵行並回頭警戒", "不離開家人")),
            deltas = listOf(locationDelta("char-lu-chenxing", "loc-hillside-ruin"), locationDelta("char-aijiang", "loc-hillside-ruin"), playerIntentDelta(1, playerInputs[0])),
            frame = frame(1, "安全", "求知", 55, 40, cost = "暫時放棄直接調查", emotional = "吐槽後轉為警覺"),
        )
        if (maxTurns == 1) return finish(session.id)

        val offscreen = OffscreenSimulationService(repository).run(
            sessionId = session.id,
            profile = profile,
            scenarioSeed = "$scenarioSeed-offscreen",
            turnInput = playerInputs[1],
        )
        writeJson("turn-02-offscreen-result.json", offscreen)
        repository.tableRows("npc_decision_traces").lastOrNull()?.let { writeJson("turn-02-decision-trace-row.json", it) }
        repository.tableRows("reboot_run_evidence").lastOrNull()?.let { evidence ->
            Files.writeString(outputDir.resolve("turn-02-context.json"), evidence["context_snapshot_json"].orEmpty())
            Files.writeString(outputDir.resolve("turn-02-result.json"), evidence["raw_result_json"].orEmpty())
            Files.writeString(outputDir.resolve("turn-02-reader-narrative.md"), evidence["raw_draft"].orEmpty())
        }
        writeJson(
            "turn-02-drama.json",
            evaluateDrama(
                2,
                session.id,
                "林遙在陸塵行離場時自行封存足跡；她不等待主角授權，也不掌握主角未公開的資訊。",
                "",
                goodSignals(serious = true),
                frame(2, "證據", "暴露", 45, 50, cost = "林遙增加自身暴露", emotional = "獨立行動帶來不安"),
            ),
        )
        if (maxTurns == 2) return finish(session.id)

        runTurn(
            index = 3,
            sessionId = session.id,
            playerInput = playerInputs[2],
            rawDraft = "周克在海岸吹了聲短哨。陸塵行折返時瞥他一眼：『你這個安全哨，保固是不是只到退潮？』周克沒笑，只把沾鹽的繩結塞進他手裡：『想活就先學會聽一次。』愛醬靠過來蹭了蹭他的膝側；林遙的標記還在，原本的碎片卻少了一塊。",
            signals = goodSignals(sarcasm = true, daily = true, socialCost = true, warm = true),
            actions = listOf(action("char-zhou-ke-sandbox", "發出安全折返信號", "先保人再說明真相")),
            deltas = listOf(locationDelta("char-lu-chenxing", "loc-daping-coast"), locationDelta("char-aijiang", "loc-daping-coast"), eventDelta("return-observes-offscreen", "林遙的記號證明有人在陸塵行離場時轉移過碎片。"), playerIntentDelta(3, playerInputs[2])),
            frame = frame(3, "信任", "證據", 40, 50, relationship = "周克不接陸塵行的吐槽，陸塵行仍接受繩結；他也開始相信林遙的記錄", reveal = "碎片被第三方移走", emotional = "日常摩擦與愛醬的親近短暫回暖，隨後轉為不安"),
        )
        if (maxTurns == 3) return finish(session.id)

        runTurn(
            index = 4,
            sessionId = session.id,
            playerInput = playerInputs[3],
            rawDraft = "陸塵行認出假足跡，繞開最明顯的追蹤線；拾潮者卻利用這段延遲，把真正證據推入另一條潮溝。",
            signals = goodSignals(sarcasm = true, danger = true, socialCost = true, antagonist = true, secondOrder = true),
            actions = listOf(action("char-lu-chenxing", "辨認假足跡並改走安全線", "不為追線索犧牲愛醬"), action("char-tide-scavenger-sandbox", "趁延遲轉移證據", "維持先占者優勢")),
            deltas = listOf(eventDelta("fair-second-order", "主角避開明顯陷阱，但拾潮者利用延遲轉移了真正證據。"), playerIntentDelta(4, playerInputs[3])),
            frame = frame(4, "安全", "真相", 50, 35, cost = "安全判斷換來證據流失", reveal = "拾潮者預判了反制", emotional = "冷靜判斷後的挫敗"),
        )
        if (maxTurns == 4) return finish(session.id)

        val irreversibleContext = runtime.prepareTurn(session.id, playerInputs[4])
        writeJson("turn-05-context.json", irreversibleContext)
        val irreversibleResult = resultFor(
            irreversibleContext,
            "turn-05-result",
            "碎石落下時，陸塵行只來得及拉回愛醬。拾潮者護住證據的手被壓在石縫裡，從此再也無法像以前那樣用力。海浪沒有替任何人辯解。",
            listOf(action("char-tide-scavenger-sandbox", "抓住證據而拒絕撤手", "不肯失去唯一籌碼")),
            listOf(
                DeltaOperation(
                    type = DeltaOperationType.UPDATE_CHARACTER_STATE,
                    targetType = "character_state",
                    targetId = "char-tide-scavenger-sandbox",
                    payload = JsonObject(mapOf("health" to JsonPrimitive("permanent_hand_injury"))),
                ),
                eventDelta("sandbox-irreversible-injury", "拾潮者在潮溝崩落中留下永久手傷。"),
                playerIntentDelta(5, playerInputs[4]),
            ),
        )
        writeJson("turn-05-result.json", irreversibleResult)
        val contract = IrreversibleChangeContract(
            changeId = "sandbox-permanent-hand-injury",
            changeTypes = listOf("permanent_injury"),
            summary = "A resettable non-Canon antagonist receives a permanent hand injury.",
            contextHash = irreversibleContext.contextHash,
            resultId = irreversibleResult.resultId,
            affectedEntityIds = listOf("char-tide-scavenger-sandbox"),
            requiredStateDeltaTargetIds = listOf("char-tide-scavenger-sandbox"),
        )
        approvalGrant.requireAuthorizes(contract, scenarioSeed)
        writeJson("turn-05-approval-grant.json", approvalGrant)
        val approval = IrreversibleApprovalArtifact(
            approvalId = approvalGrant.grantId,
            changeId = contract.changeId,
            contextHash = contract.contextHash,
            resultId = contract.resultId,
            humanActorId = approvalGrant.humanActorId,
            decision = approvalGrant.decision,
            approvedAt = approvalGrant.approvedAt,
        )
        val drama5 = evaluateDrama(5, session.id, irreversibleResult.narrative, "", goodSignals(serious = true, grief = true), frame(5, "憐憫", "追索", 35, 20, cost = "拾潮者永久受傷", irreversible = "永久手傷", emotional = "憤怒後的沉默"))
        writeJson("turn-05-drama.json", drama5)
        runtime.resolveTurn(session.id, irreversibleResult, ResolveTurnOptions(scenarioSeed = scenarioSeed, irreversibleChangeContract = contract, irreversibleApprovalArtifact = approval))
        Files.writeString(outputDir.resolve("turn-05-reader-narrative.md"), irreversibleResult.narrative)
        if (maxTurns == 5) return finish(session.id)

        runTurn(
            index = 6,
            sessionId = session.id,
            playerInput = playerInputs[5],
            rawDraft = "陸塵行把布條遞給周克，自己壓住傷口。愛醬靠著他的小腿，沒有叫。遠處還有一串腳印，朝山坡更深處去。",
            signals = goodSignals(warm = true, serious = true, grief = true, secondOrder = true),
            actions = listOf(action("char-lu-chenxing", "先救治對手再追問", "不讓求真變成合理化殘酷"), action("char-zhou-ke-sandbox", "接手固定傷處", "償還過去欠下的人情")),
            deltas = listOf(eventDelta("local-closure-open-question", "眾人暫時穩住傷勢，但另一串腳印通往山坡深處。"), playerIntentDelta(6, playerInputs[5])),
            frame = frame(6, "救人", "追索", 20, 45, cost = "暫停追擊", relationship = "敵意轉為有條件的停火", reveal = "還有第二名搬運者", emotional = "沉重後留下微弱希望"),
        )
        return finish(session.id)
    }

    private fun finish(sessionId: String): RebootVerticalSliceRunSummary {
        val tables = listOf("story_sessions", "turn_logs", "state_deltas", "events", "character_states", "reboot_run_evidence", "npc_decision_traces", "reboot_drama_evaluations", "sandbox_world_patch_candidates")
        writeJson("sql-state-snapshot.json", tables.associateWith(repository::tableRows))
        writeJson("session-evidence.json", SessionEvidenceExporter(repository).export(sessionId))
        writeJson("protagonist-growth-evidence.json", buildJsonObject {
            put("combatCapability", "NOT_EXERCISED")
            put("knowledge", "EVIDENCE_CUSTODY_AND_SECOND_ACTOR_REVEALED")
            put("strategicJudgment", "RECOGNIZED_DANGER_AND_ACCEPTED_EVIDENCE_COST")
            put("relationships", "TRUST_WITH_LIN_YAO_AND_CONDITIONAL_TRUCE")
            put("reputation", "SANDBOX_NOT_FORMALLY_MEASURED")
            put("socialInfluence", "RESCUE_PRIORITY_CHANGED_GROUP_ACTION")
            put("leadershipBurden", "ACCEPTED_RESPONSIBILITY_FOR_RIVAL_RESCUE")
            put("moralCompromiseRisk", "REFUSED_TO_TRADE_A_PERSON_FOR_EVIDENCE")
            put("lossAndSacrifice", "LOST_EVIDENCE_AND_PURSUIT_TIME")
            put("falseBeliefCorrection", "SAFETY_ALONE_DOES_NOT_PRESERVE_TRUTH")
        })
        val sessionRow = repository.tableRows("story_sessions").single { it["id"] == sessionId }
        val summary = RebootVerticalSliceRunSummary(
            sessionId = sessionId,
            appliedTurns = repository.appliedTurnCount(sessionId),
            stateVersion = sessionRow["state_version"]!!.toInt(),
            elapsedMinutes = sessionRow["elapsed_minutes"]!!.toInt(),
            decisionTraceCount = repository.tableCount("npc_decision_traces"),
            dramaEvaluationCount = repository.tableCount("reboot_drama_evaluations"),
            runEvidenceCount = repository.tableCount("reboot_run_evidence"),
            irreversibleApprovalCount = repository.tableRows("reboot_run_evidence").count { it["irreversible_approval_granted"] == "1" },
            outputDir = outputDir.toString(),
            scenarioSeed = scenarioSeed,
            ruleProfile = ruleProfile.name,
        )
        writeJson("run-summary.json", summary)
        return summary
    }

    private fun runTurn(
        index: Int,
        sessionId: String,
        playerInput: String,
        rawDraft: String,
        revisedDraft: String = "",
        signals: NarrativeSignals,
        actions: List<CharacterAction>,
        deltas: List<DeltaOperation>,
        frame: SceneDramaFrame,
    ) {
        val context = runtime.prepareTurn(sessionId, playerInput)
        writeJson("turn-${index.toString().padStart(2, '0')}-context.json", context)
        val drama = evaluateDrama(index, sessionId, rawDraft, revisedDraft, signals, frame)
        writeJson("turn-${index.toString().padStart(2, '0')}-drama.json", drama)
        val result = resultFor(context, "turn-${index.toString().padStart(2, '0')}-result", drama.selectedDraft, actions, deltas)
        writeJson("turn-${index.toString().padStart(2, '0')}-result.json", result)
        runtime.resolveTurn(sessionId, result, ResolveTurnOptions(scenarioSeed = scenarioSeed, revisionSuggestion = drama.revisionSuggestion, revisedDraft = revisedDraft))
        Files.writeString(outputDir.resolve("turn-${index.toString().padStart(2, '0')}-reader-narrative.md"), result.narrative)
    }

    private fun evaluateDrama(index: Int, sessionId: String, raw: String, revised: String, signals: NarrativeSignals, frame: SceneDramaFrame) =
        RebootDramaLoopService(repository).evaluateAndPersist(
            DramaEvaluationInput(
                evaluationId = "slice-drama-$index-${UUID.randomUUID()}",
                runId = "reboot01-chenxing-slice",
                projectId = "chenxing",
                sessionId = sessionId,
                turnNumber = index,
                scenarioSeed = scenarioSeed,
                rawDraft = raw,
                revisedDraftCandidate = revised,
                rawSignals = if (revised.isBlank()) signals else NarrativeSignals(riskTakingHasReason = false, cheapTwist = true),
                revisedSignals = if (revised.isBlank()) null else signals,
                sceneFrame = frame,
                expectationLedger = AudienceExpectationLedger(
                    obviousDanger = "The tide and unstable coast are visible.",
                    characterRecognition = "Lu Chenxing explicitly recognizes the danger.",
                    firstLayerCountermeasure = "The group chooses a safer route.",
                    worldOrAntagonistAdaptation = "The rival uses time and terrain instead of waiting.",
                    secondOrderSurprise = "Safety succeeds but creates an evidence cost.",
                    retrospectiveClueValidity = "Fresh footprints and moved fragments precede the reveal.",
                ),
            ),
        )

    private fun resultFor(context: ContextPacket, resultId: String, narrative: String, actions: List<CharacterAction>, deltas: List<DeltaOperation>) = AiTurnResult(
        protocolVersion = context.protocolVersion,
        projectId = context.project.id,
        turnId = context.turnId,
        resultId = resultId,
        contextId = context.contextId,
        sessionId = context.sessionId,
        turnNumber = context.turnNumber,
        baseStateVersion = context.baseStateVersion,
        contextHash = context.contextHash,
        narrative = narrative,
        turnSummary = narrative.take(180),
        characterActions = actions,
        stateDeltas = deltas,
    )

    private fun action(id: String, visible: String, motive: String) = CharacterAction(id, visible, visible, motive, characterFitScore = 90, storyProgressScore = 80)
    private fun locationDelta(id: String, location: String) = DeltaOperation(DeltaOperationType.UPDATE_CHARACTER_STATE, "character_state", id, JsonObject(mapOf("locationId" to JsonPrimitive(location))))
    private fun eventDelta(id: String, summary: String) = DeltaOperation(DeltaOperationType.ADD_EVENT, "event", id, JsonObject(mapOf("summary" to JsonPrimitive(summary))))
    private fun playerIntentDelta(index: Int, input: String) = eventDelta("player-intent-$index", input)
    private fun frame(index: Int, a: String, b: String, before: Int, after: Int, cost: String = "", irreversible: String = "", relationship: String = "", reveal: String = "", emotional: String) = SceneDramaFrame(
        sceneGoal = "Advance slice beat $index through character choice.",
        dramaticQuestion = DramaticQuestion("Can they preserve $a without surrendering $b?"),
        valueConflict = ValueConflict(a, b, true),
        sceneValue = SceneValueState("control", before, after),
        stakes = "$a and $b",
        cost = cost,
        irreversibleChange = irreversible,
        relationshipChange = relationship,
        reveal = reveal,
        emotionalBeat = emotional,
    )
    private fun goodSignals(
        sarcasm: Boolean = false,
        daily: Boolean = false,
        danger: Boolean = false,
        socialCost: Boolean = false,
        grief: Boolean = false,
        warm: Boolean = false,
        serious: Boolean = false,
        antagonist: Boolean = false,
        secondOrder: Boolean = false,
    ) = NarrativeSignals(
        sarcasmPresent = sarcasm,
        banterPresent = sarcasm,
        dailyQuip = daily,
        dangerObservation = danger,
        griefRestraint = grief,
        sarcasmCreatesSocialCost = socialCost,
        griefOrInjuryBeat = grief,
        genreDangerRecognized = true,
        riskTakingHasReason = true,
        antagonistAdapts = antagonist,
        secondOrderConsequence = secondOrder,
        warmBeat = warm,
        seriousBeat = serious,
    )
    private inline fun <reified T> writeJson(name: String, value: T) {
        Files.writeString(outputDir.resolve(name), JsonSupport.json.encodeToString(value))
    }

    private fun requireSandboxWorldPatch(pack: com.localnovel.engine.model.SettingPack) {
        require(pack.packId == "reboot01-chenxing-world-patch-candidate-v1") { "REBOOT_WORLD_PATCH_ID_REQUIRED" }
        require(pack.projectId == "chenxing" && pack.mode == "ADD_ON") { "REBOOT_WORLD_PATCH_SCOPE_INVALID" }
        fun flag(name: String): Boolean = (pack.metadata[name] as? JsonPrimitive)?.booleanOrNull == true
        require(flag("nonCanon")) { "REBOOT_WORLD_PATCH_NON_CANON_REQUIRED" }
        require(flag("resettable")) { "REBOOT_WORLD_PATCH_RESETTABLE_REQUIRED" }
        require(flag("promotionForbidden")) { "REBOOT_WORLD_PATCH_PROMOTION_FORBIDDEN_REQUIRED" }
        require((pack.metadata["canonMutationAllowed"] as? JsonPrimitive)?.booleanOrNull == false) {
            "REBOOT_WORLD_PATCH_CANON_MUTATION_MUST_BE_FALSE"
        }
    }

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }
}
