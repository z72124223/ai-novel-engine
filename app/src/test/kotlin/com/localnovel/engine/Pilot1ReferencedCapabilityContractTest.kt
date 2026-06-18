package com.localnovel.engine

import com.localnovel.engine.model.AiTurnResult
import com.localnovel.engine.model.CapabilityContract
import com.localnovel.engine.model.CapabilityUseCheck
import com.localnovel.engine.model.CharacterAction
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextBudget
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeltaOperation
import com.localnovel.engine.model.DeltaOperationType
import com.localnovel.engine.model.Item
import com.localnovel.engine.model.Project
import com.localnovel.engine.model.Scene
import com.localnovel.engine.model.SceneContract
import com.localnovel.engine.model.Technique
import com.localnovel.engine.model.WorldRule
import com.localnovel.engine.service.CapabilityCoverageAnalyzer
import com.localnovel.engine.service.CapabilityUseValidator
import com.localnovel.engine.service.ContextAuditService
import com.localnovel.engine.service.ContextBudgetForensicsService
import com.localnovel.engine.service.ContextPacketFinalizer
import com.localnovel.engine.service.ContextPacketNormalizer
import com.localnovel.engine.service.ContextQualityGuard
import com.localnovel.engine.service.TurnSemanticPlanner
import com.localnovel.engine.support.JsonSupport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.security.MessageDigest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Pilot1ReferencedCapabilityContractTest {
    @Test
    fun explicitTechniqueUseCreatesReferencedTechniqueRequirement() {
        val requirements = TurnSemanticPlanner().plan(contextWithContract())

        assertTrue(requirements.any { it.requirementId == "req-capability-technique-tech-basic-breath" })
    }

    @Test
    fun referencedTechniqueRequirementCarriesRequiredContractFields() {
        val requirement = TurnSemanticPlanner().plan(contextWithContract()).single { it.requirementId.startsWith("req-capability") }

        assertTrue(requirement.requiredContractFields.containsAll(listOf("authorizedActorIds", "costs", "limitations", "currentEligibility")))
    }

    @Test
    fun descriptionOnlyTechniqueDoesNotSatisfyCapabilityRequirement() {
        val context = contextWithContract().copy(activeCapabilityContracts = emptyList())
        val report = ContextQualityGuard().evaluate(context, TurnSemanticPlanner().plan(context))

        assertEquals("FAIL", report.qualityStatus)
        assertTrue(report.missingCriticalRequirements.any { it.requirementId.startsWith("req-capability") })
    }

    @Test
    fun completeTechniqueContractSatisfiesQualityGuard() {
        val report = ContextQualityGuard().evaluate(contextWithContract(), TurnSemanticPlanner().plan(contextWithContract()))

        assertEquals("PASS", report.qualityStatus)
    }

    @Test
    fun selectedContractPathIsActiveCapabilityContracts() {
        val report = ContextQualityGuard().evaluate(contextWithContract(), TurnSemanticPlanner().plan(contextWithContract()))

        assertEquals("$.activeCapabilityContracts[0]", report.satisfiedBy.single { it.requirementId.startsWith("req-capability") }.selectedContextPath)
    }

    @Test
    fun actorMismatchFailsQualityGuard() {
        val context = contextWithContract().copy(activeCapabilityContracts = listOf(techniqueContract().copy(authorizedActorIds = listOf("char-zhao"))))
        val report = ContextQualityGuard().evaluate(context, TurnSemanticPlanner().plan(context))

        assertEquals("FAIL", report.qualityStatus)
    }

    @Test
    fun incompleteContractFailsQualityGuard() {
        val context = contextWithContract().copy(activeCapabilityContracts = listOf(techniqueContract().copy(costs = emptyList())))
        val report = ContextQualityGuard().evaluate(context, TurnSemanticPlanner().plan(context))

        assertEquals("FAIL", report.qualityStatus)
    }

    @Test
    fun capabilityCoverageReportsSelectedContract() {
        val report = CapabilityCoverageAnalyzer().report(contextWithContract())

        assertEquals("PASS", report.qualityStatus)
        assertEquals("tech-basic-breath", report.selectedContracts.single().entityId)
    }

    @Test
    fun capabilityCoverageReportsMissingContract() {
        val context = contextWithContract().copy(activeCapabilityContracts = emptyList())
        val report = CapabilityCoverageAnalyzer().report(context)

        assertEquals("FAIL", report.qualityStatus)
        assertTrue(report.missingContracts.contains("technique:tech-basic-breath"))
    }

    @Test
    fun auditSidecarIncludesCapabilityCoveragePass() {
        val finalized = finalize(contextWithContract())
        val report = ContextAuditService().audit(finalized)

        assertTrue(report.valid, report.violations.joinToString())
        assertEquals("PASS", report.capabilityCoverage.qualityStatus)
    }

    @Test
    fun auditRejectsMissingCapabilityCoverage() {
        val context = contextWithContract().copy(activeCapabilityContracts = emptyList())
        val report = ContextAuditService().audit(context)

        assertFalse(report.valid)
        assertTrue(report.violations.any { it.startsWith("semantic_coverage_failed") || it.startsWith("capability_coverage_failed") })
    }

    @Test
    fun capabilityCoverageDoesNotEnterFinalContextJson() {
        val text = JsonSupport.json.encodeToString(finalize(contextWithContract()))

        assertFalse(text.contains("capabilityCoverage"))
        assertFalse(text.contains("selectedContracts"))
    }

    @Test
    fun legacyTechniqueDescriptionIsCompactedWhenContractExists() {
        val normalized = ContextPacketNormalizer.normalizeForFinalContext(contextWithContract())

        assertEquals("", normalized.techniques.single().description)
    }

    @Test
    fun validatorRequiresCapabilityUseCheckForUsedTechnique() {
        val errors = CapabilityUseValidator().validate(contextWithContract(), result(checks = emptyList()))

        assertTrue(errors.any { it.code == "missing_capability_use_check" })
    }

    @Test
    fun validatorRejectsUnauthorizedActorUse() {
        val action = characterAction(characterId = "char-zhao", usedTechniqueIds = listOf("tech-basic-breath"))
        val check = techniqueCheck(actorId = "char-zhao")
        val errors = CapabilityUseValidator().validate(contextWithContract(), result(actions = listOf(action), checks = listOf(check), deltas = costDelta("char-zhao")))

        assertTrue(errors.any { it.code == "CAPABILITY_ACTOR_NOT_AUTHORIZED" })
    }

    @Test
    fun validatorRejectsSuccessfulUseWithoutCostDelta() {
        val errors = CapabilityUseValidator().validate(contextWithContract(), result(deltas = emptyList()))

        assertTrue(errors.any { it.code == "missing_capability_cost_delta" })
    }

    @Test
    fun validatorAcceptsSuccessfulUseWithCostDelta() {
        val errors = CapabilityUseValidator().validate(contextWithContract(), result())

        assertTrue(errors.isEmpty(), errors.joinToString { it.code })
    }

    @Test
    fun validatorRejectsForbiddenUse() {
        val contract = techniqueContract().copy(forbiddenUses = listOf("辨識影子真相"), forbiddenNarrativeClaims = listOf("辨識影子真相"))
        val context = contextWithContract().copy(activeCapabilityContracts = listOf(contract))
        val errors = CapabilityUseValidator().validate(context, result(narrative = "主角用調息辨識影子真相。", checks = listOf(techniqueCheck(intendedUse = "辨識影子真相"))))

        assertTrue(errors.any { it.code == "capability_forbidden_use" })
    }

    @Test
    fun unsuccessfulUseCanReferenceUnsatisfiedPrerequisite() {
        val contract = techniqueContract().copy(currentEligibility = "INELIGIBLE", prerequisites = listOf("tech-missing"))
        val context = contextWithContract().copy(activeCapabilityContracts = listOf(contract))
        val check = techniqueCheck(eligibilityClaim = "FAILURE", satisfiedPrerequisites = emptyList(), unsatisfiedPrerequisites = listOf("tech-missing"), appliedCosts = emptyList())
        val errors = CapabilityUseValidator().validate(context, result(checks = listOf(check), deltas = emptyList()))

        assertTrue(errors.none { it.code == "CAPABILITY_PREREQUISITE_UNSATISFIED" })
    }

    @Test
    fun successfulUseRejectsUnsatisfiedPrerequisite() {
        val contract = techniqueContract().copy(currentEligibility = "INELIGIBLE", prerequisites = listOf("tech-missing"))
        val context = contextWithContract().copy(activeCapabilityContracts = listOf(contract))
        val errors = CapabilityUseValidator().validate(context, result())

        assertTrue(errors.any { it.code == "CAPABILITY_PREREQUISITE_UNSATISFIED" })
    }

    @Test
    fun noneCostDoesNotRequireCostDelta() {
        val context = contextWithContract().copy(activeCapabilityContracts = listOf(techniqueContract().copy(costs = listOf("NONE"))))
        val errors = CapabilityUseValidator().validate(context, result(deltas = emptyList(), checks = listOf(techniqueCheck(appliedCosts = emptyList()))))

        assertTrue(errors.isEmpty(), errors.joinToString { it.code })
    }

    @Test
    fun itemContractUsesSameValidationPath() {
        val context = contextWithContract().copy(
            playerInput = "我使用裂紋腰牌確認身份。",
            activeCapabilityContracts = listOf(itemContract("item")),
        )
        val action = characterAction(usedTechniqueIds = emptyList(), usedItemIds = listOf("item-token"))
        val errors = CapabilityUseValidator().validate(context, result(actions = listOf(action), checks = listOf(itemCheck("item")), deltas = costDelta("char-player", "item-token")))

        assertTrue(errors.isEmpty(), errors.joinToString { it.code })
    }

    @Test
    fun weaponContractUsesSameValidationPath() {
        val context = contextWithContract().copy(
            playerInput = "我使用短刀護住自己。",
            items = listOf(Item("item-knife", "capability", "短刀", "近身防衛武器。", "char-player", tags = listOf("weapon"))),
            activeCapabilityContracts = listOf(itemContract("weapon", "item-knife")),
        )
        val action = characterAction(usedTechniqueIds = emptyList(), usedItemIds = listOf("item-knife"))
        val errors = CapabilityUseValidator().validate(context, result(actions = listOf(action), checks = listOf(itemCheck("weapon", "item-knife")), deltas = costDelta("char-player", "item-knife")))

        assertTrue(errors.isEmpty(), errors.joinToString { it.code })
    }

    @Test
    fun contractIsReportedAsTierOneBytes() {
        val report = ContextBudgetForensicsService().report(contextWithContract(), finalize(contextWithContract()))

        assertTrue(report.semanticCoverage.tierBytes.getValue("tier1") > 0)
        assertTrue(report.perSectionBytesAfter.any { it.section == "activeCapabilityContracts" && it.utf8Bytes > 0 })
    }

    @Test
    fun plannerIsDeterministicForCapabilityUse() {
        val first = JsonSupport.json.encodeToString(TurnSemanticPlanner().plan(contextWithContract()))
        val second = JsonSupport.json.encodeToString(TurnSemanticPlanner().plan(contextWithContract()))

        assertEquals(first, second)
    }

    @Test
    fun finalContextStaysUnderHardBudgetWithContract() {
        val finalized = finalize(contextWithContract())

        assertTrue(finalized.contextBudget.actualBytes <= 24576)
    }

    @Test
    fun finalContextHashIncludesCapabilityContract() {
        val finalized = finalize(contextWithContract())

        assertEquals(hash(finalized), finalized.contextHash)
    }

    private fun contextWithContract(): ContextPacket =
        ContextPacket(
            project = Project("capability", "Capability Test", "Test project.", "scene-capability"),
            turnId = "turn-1",
            turnNumber = 1,
            playerInput = "我運起粗淺氣血調息，把呼吸和手腳的顫抖壓穩。",
            activeScene = Scene("scene-capability", "capability", "驛亭", "門檻內側。", "loc-post"),
            sceneContract = SceneContract("contract-capability", "capability", "scene-capability", forbiddenReveals = listOf("hidden truth")),
            characters = listOf(
                CharacterProfile("char-player", "capability", "玩家", "無籍異鄉客", "謹慎。", isPlayer = true),
                CharacterProfile("char-zhao", "capability", "趙無咎", "官員", "控場。"),
            ),
            characterStates = listOf(
                CharacterState("char-player", "capability", "loc-post", "戒備", "normal", flags = mapOf("current_goal" to "穩住自己。", "dominant_emotion" to "戒備")),
                CharacterState("char-zhao", "capability", "loc-post", "控場", "normal", flags = mapOf("current_goal" to "控場。", "dominant_emotion" to "責任")),
            ),
            worldRules = listOf(WorldRule("rule-identity", "capability", "身份規則", "無籍者不能自由入城。", 10)),
            items = listOf(Item("item-token", "capability", "裂紋腰牌", "身份憑證。", "char-player")),
            techniques = listOf(Technique("tech-basic-breath", "capability", "粗淺氣血調息", "只能穩住呼吸，不可辨識影子。", "char-player")),
            contextId = "ctx-1",
            sessionId = "session-1",
            baseStateVersion = 1,
            activeCapabilityContracts = listOf(techniqueContract()),
            contextBudget = ContextBudget(),
        )

    private fun techniqueContract(): CapabilityContract =
        CapabilityContract(
            entityType = "technique",
            entityId = "tech-basic-breath",
            name = "粗淺氣血調息",
            authorizedActorIds = listOf("char-player"),
            intendedUse = "穩住呼吸與顫抖",
            effects = listOf("temporary stamina control"),
            prerequisites = listOf("NONE"),
            requirements = listOf("NONE"),
            costs = listOf("fatigue"),
            limitations = listOf("cannot identify shadow truth"),
            forbiddenUses = listOf("NONE"),
            currentEligibility = "ELIGIBLE",
            eligibilityReasons = listOf("actor authorized"),
            allowedNarrativeClaims = listOf("temporary stamina control"),
            forbiddenNarrativeClaims = listOf("cannot identify shadow truth"),
            sourceRevision = "rev-tech@v1",
        )

    private fun itemContract(type: String, id: String = "item-token"): CapabilityContract =
        CapabilityContract(
            entityType = type,
            entityId = id,
            name = if (type == "weapon") "短刀" else "裂紋腰牌",
            authorizedActorIds = listOf("char-player"),
            intendedUse = "limited item use",
            effects = listOf("limited mundane use"),
            prerequisites = listOf("NONE"),
            requirements = listOf("must hold item"),
            costs = listOf("attention"),
            limitations = listOf("no hidden supernatural answer"),
            forbiddenUses = listOf("NONE"),
            currentEligibility = "ELIGIBLE",
            eligibilityReasons = listOf("actor authorized"),
            sourceRevision = "rev-item@v1",
        )

    private fun result(
        narrative: String = "主角只用調息穩住呼吸，沒有得到超自然答案。",
        actions: List<CharacterAction> = listOf(characterAction()),
        checks: List<CapabilityUseCheck> = listOf(techniqueCheck()),
        deltas: List<DeltaOperation> = costDelta("char-player"),
    ): AiTurnResult =
        AiTurnResult(
            projectId = "capability",
            turnId = "turn-1",
            narrative = narrative,
            resultId = "result-1",
            contextId = "ctx-1",
            sessionId = "session-1",
            turnNumber = 1,
            baseStateVersion = 1,
            contextHash = "",
            turnSummary = "能力使用測試。",
            characterActions = actions,
            stateDeltas = deltas,
            capabilityUseChecks = checks,
        )

    private fun characterAction(
        characterId: String = "char-player",
        usedTechniqueIds: List<String> = listOf("tech-basic-breath"),
        usedItemIds: List<String> = emptyList(),
    ): CharacterAction =
        CharacterAction(
            characterId = characterId,
            intent = "穩住自身。",
            visibleAction = "使用已知能力或物品。",
            hiddenMotive = "保護自己。",
            usedTechniqueIds = usedTechniqueIds,
            usedItemIds = usedItemIds,
            characterFitScore = 90,
            storyProgressScore = 60,
        )

    private fun techniqueCheck(
        actorId: String = "char-player",
        intendedUse: String = "穩住呼吸與顫抖",
        eligibilityClaim: String = "SUCCESS",
        satisfiedPrerequisites: List<String> = listOf("NONE"),
        unsatisfiedPrerequisites: List<String> = emptyList(),
        appliedCosts: List<String> = listOf("fatigue"),
    ): CapabilityUseCheck =
        CapabilityUseCheck(
            actorId = actorId,
            entityType = "technique",
            entityId = "tech-basic-breath",
            intendedUse = intendedUse,
            eligibilityClaim = eligibilityClaim,
            satisfiedPrerequisites = satisfiedPrerequisites,
            unsatisfiedPrerequisites = unsatisfiedPrerequisites,
            appliedCosts = appliedCosts,
            observedLimitations = listOf("cannot identify shadow truth"),
            resultScope = "Only temporary stamina control.",
            evidenceStateDeltaIds = listOf(actorId),
            reason = "Contract allows only this scope.",
        )

    private fun itemCheck(type: String, id: String = "item-token"): CapabilityUseCheck =
        CapabilityUseCheck(
            actorId = "char-player",
            entityType = type,
            entityId = id,
            intendedUse = "limited item use",
            eligibilityClaim = "SUCCESS",
            satisfiedPrerequisites = listOf("NONE"),
            appliedCosts = listOf("attention"),
            observedLimitations = listOf("no hidden supernatural answer"),
            resultScope = "Mundane use only.",
            evidenceStateDeltaIds = listOf("char-player"),
            reason = "Contract allows the actor to use it.",
        )

    private fun costDelta(actorId: String, capabilityId: String = "tech-basic-breath"): List<DeltaOperation> =
        listOf(
            DeltaOperation(
                type = DeltaOperationType.UPDATE_CHARACTER_STATE,
                targetType = "character_state",
                targetId = actorId,
                payload = JsonObject(
                    mapOf(
                        "flags" to JsonObject(
                            mapOf("capabilityCost.$capabilityId" to JsonPrimitive("fatigue")),
                        ),
                    ),
                ),
            ),
        )

    private fun finalize(context: ContextPacket): ContextPacket =
        ContextPacketFinalizer().finalize(context, ::hash)

    private fun hash(context: ContextPacket): String =
        MessageDigest.getInstance("SHA-256")
            .digest(JsonSupport.json.encodeToString(context.copy(contextHash = "")).toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
}
