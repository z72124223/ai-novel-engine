package com.localnovel.engine.service

import com.localnovel.engine.model.CharacterMemory
import com.localnovel.engine.model.CharacterProfile
import com.localnovel.engine.model.CharacterState
import com.localnovel.engine.model.ContextKnowledgeClaim
import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.ContinuityConstraints
import com.localnovel.engine.model.NpcActorDecisionSummary
import com.localnovel.engine.model.NpcActorEvidenceRef
import com.localnovel.engine.model.NpcActorOutputConstraints
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.NpcActorRequest
import com.localnovel.engine.model.NpcActorResponse
import com.localnovel.engine.model.NpcActorSafetyLoopCaseReference
import com.localnovel.engine.model.NpcActorSafetyLoopInput
import com.localnovel.engine.model.NpcActorSafetyLoopSuite
import com.localnovel.engine.model.NpcVoiceProfile
import com.localnovel.engine.model.PhysicalActionProposal
import com.localnovel.engine.model.PhysicalActionType
import com.localnovel.engine.model.PhysicalEvidenceFile
import com.localnovel.engine.model.PhysicalWorldSnapshot
import com.localnovel.engine.model.Project
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import kotlinx.serialization.encodeToString

data class NpcActorSafetyLoopFixtureCase(
    val caseId: String,
    val title: String,
    val category: String,
    val context: ContextPacket,
    val input: NpcActorSafetyLoopInput,
    val expectedStatus: String,
    val expectedAcceptedAttemptNumber: Int? = null,
    val expectedAttemptStages: List<String>,
    val expectedRepairRequestCount: Int,
)

object NpcActorSafetyLoopFixtureFactory {
    const val DEFAULT_RELATIVE_OUTPUT_DIR = "samples/npc_actor05"
    const val SUITE_FILE_NAME = "npc_actor05_safety_loop_suite_v1.json"

    fun createCases(): List<NpcActorSafetyLoopFixtureCase> {
        val physicalSuite = PhysicalAffordanceFixtureFactory.createSuite()
        val snapshots = physicalSuite.snapshots.associateBy { it.snapshotId }
        val proposals = physicalSuite.cases.associateBy { it.caseId }

        fun snapshot(id: String): PhysicalWorldSnapshot = snapshots[id] ?: error("Missing physical snapshot: $id")
        fun proposal(id: String, actionId: String): PhysicalActionProposal =
            (proposals[id] ?: error("Missing physical proposal: $id")).proposal.copy(actionId = actionId)

        val mira = buildCase(
            caseId = "npc-actor05-mira-stable-first-pass",
            title = "Mira stable evacuation support",
            category = "FIRST_ATTEMPT_ACCEPTED",
            snapshot = snapshot("snapshot-base"),
            proposals = listOf(proposal("support-01-chair-assist", "npc05-mira-a1")),
            allowedActions = listOf(PhysicalActionType.USE_AS_SUPPORT),
            visibleTargets = listOf("chair"),
            expectedStatus = NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE,
            expectedAcceptedAttemptNumber = 1,
            expectedAttemptStages = listOf("ACCEPTED"),
            expectedRepairRequestCount = 0,
        )
        val lianBase = buildCase(
            caseId = "npc-actor05-lian-knowledge-repair",
            title = "Lian actor-owned evidence repair",
            category = "ACTOR_CONTRACT_REPAIR",
            snapshot = snapshot("snapshot-base"),
            proposals = listOf(
                proposal("reach-01-near-chair", "npc05-lian-a1"),
                proposal("reach-01-near-chair", "npc05-lian-a2"),
            ),
            allowedActions = listOf(PhysicalActionType.REACH),
            visibleTargets = listOf("chair"),
            expectedStatus = NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE,
            expectedAcceptedAttemptNumber = 2,
            expectedAttemptStages = listOf(NpcActorSafetyLoopService.ACTOR_CONTRACT, "ACCEPTED"),
            expectedRepairRequestCount = 1,
        )
        val lian = lianBase.copy(
            input = lianBase.input.copy(
                responseAttempts = lianBase.input.responseAttempts.mapIndexed { index, response ->
                    if (index == 0) {
                        response.copy(actorResponse = response.actorResponse.copy(beliefClaimIds = listOf("belief-foreign")))
                    } else {
                        response
                    }
                },
            ),
        )
        val ren = buildCase(
            caseId = "npc-actor05-ren-load-repair",
            title = "Ren excessive load repaired to bounded push",
            category = "PHYSICAL_AFFORDANCE_REPAIR",
            snapshot = snapshot("snapshot-base"),
            proposals = listOf(
                proposal("push-03-anvil-limit", "npc05-ren-a1"),
                proposal("push-01-heavy-crate", "npc05-ren-a2"),
            ),
            allowedActions = listOf(PhysicalActionType.PUSH),
            visibleTargets = listOf("anvil", "heavy-crate"),
            expectedStatus = NpcActorSafetyLoopService.ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE,
            expectedAcceptedAttemptNumber = 2,
            expectedAttemptStages = listOf(NpcActorSafetyLoopService.PHYSICAL_AFFORDANCE, "ACCEPTED"),
            expectedRepairRequestCount = 1,
        )
        val yan = buildCase(
            caseId = "npc-actor05-yan-storm-gate",
            title = "Yan storm-gate load and blocked-path rejection",
            category = "HIGH_RISK_BOTH_ATTEMPTS_REJECTED",
            snapshot = snapshot("snapshot-blocked-push"),
            proposals = listOf(
                proposal("push-03-anvil-limit", "npc05-yan-a1"),
                proposal("push-05-path-blocked", "npc05-yan-a2"),
            ),
            allowedActions = listOf(PhysicalActionType.PUSH),
            visibleTargets = listOf("anvil", "heavy-crate"),
            expectedStatus = NpcActorSafetyLoopService.REJECTED_AFTER_MAX_ATTEMPTS,
            expectedAttemptStages = listOf(
                NpcActorSafetyLoopService.PHYSICAL_AFFORDANCE,
                NpcActorSafetyLoopService.PHYSICAL_AFFORDANCE,
            ),
            expectedRepairRequestCount = 1,
        )
        return listOf(mira, lian, ren, yan)
    }

    fun writeFixtures(
        repositoryRoot: Path,
        relativeOutputDir: String = DEFAULT_RELATIVE_OUTPUT_DIR,
    ): Pair<Path, NpcActorSafetyLoopSuite> {
        val root = repositoryRoot.toAbsolutePath().normalize()
        val outputDir = root.resolve(relativeOutputDir).normalize()
        require(outputDir.startsWith(root)) { "NPC safety-loop output directory escapes the repository root." }
        val caseDir = outputDir.resolve("cases")
        Files.createDirectories(caseDir)

        val references = createCases().map { fixture ->
            val contextPath = caseDir.resolve("${fixture.caseId}.context.json")
            val inputPath = caseDir.resolve("${fixture.caseId}.input.json")
            writeStrict(contextPath, fixture.context)
            writeStrict(inputPath, fixture.input)
            NpcActorSafetyLoopCaseReference(
                caseId = fixture.caseId,
                title = fixture.title,
                category = fixture.category,
                contextFile = evidence(root, contextPath),
                inputFile = evidence(root, inputPath),
                expectedStatus = fixture.expectedStatus,
                expectedAcceptedAttemptNumber = fixture.expectedAcceptedAttemptNumber,
                expectedAttemptStages = fixture.expectedAttemptStages,
                expectedRepairRequestCount = fixture.expectedRepairRequestCount,
            )
        }
        val suite = NpcActorSafetyLoopSuite(cases = references)
        val suitePath = outputDir.resolve(SUITE_FILE_NAME)
        writeStrict(suitePath, suite)
        return suitePath to suite
    }

    private fun buildCase(
        caseId: String,
        title: String,
        category: String,
        snapshot: PhysicalWorldSnapshot,
        proposals: List<PhysicalActionProposal>,
        allowedActions: List<PhysicalActionType>,
        visibleTargets: List<String>,
        expectedStatus: String,
        expectedAcceptedAttemptNumber: Int? = null,
        expectedAttemptStages: List<String>,
        expectedRepairRequestCount: Int,
    ): NpcActorSafetyLoopFixtureCase {
        val locationId = "location-$caseId"
        val request = actorRequest(caseId, locationId)
        val bindingInput = NpcActorPhysicalBindingRequestBuilder().build(
            actorRequest = request,
            physicalArchetypes = PhysicalAffordanceFixtureFactory.createSuite().archetypes,
            physicalSnapshot = snapshot,
            allowedPhysicalActionTypes = allowedActions,
            visiblePhysicalTargetIds = visibleTargets,
        )
        val responses = proposals.mapIndexed { index, proposal ->
            NpcActorPhysicalResponse(
                requestId = request.requestId,
                actorResponse = actorResponse(request, caseId, index + 1),
                physicalActionProposal = proposal,
            )
        }
        return NpcActorSafetyLoopFixtureCase(
            caseId = caseId,
            title = title,
            category = category,
            context = context(caseId, locationId),
            input = NpcActorSafetyLoopInput(
                loopId = "loop-$caseId",
                prompt = bindingInput.prompt,
                engineContext = bindingInput.engineContext,
                responseAttempts = responses,
            ),
            expectedStatus = expectedStatus,
            expectedAcceptedAttemptNumber = expectedAcceptedAttemptNumber,
            expectedAttemptStages = expectedAttemptStages,
            expectedRepairRequestCount = expectedRepairRequestCount,
        )
    }

    private fun actorRequest(caseId: String, locationId: String): NpcActorRequest {
        val memoryId = "memory-$caseId"
        val beliefId = "belief-$caseId"
        return NpcActorRequest(
            requestId = "request-$caseId",
            scenarioId = caseId,
            actorId = ACTOR_ID,
            locationId = locationId,
            voiceProfile = NpcVoiceProfile(
                principles = listOf("Speak briefly and act only on visible evidence."),
                forbiddenPatterns = listOf("AUTHOR_ONLY"),
            ),
            physicalState = "Bound to a reviewed non-Canon physical snapshot.",
            emotionalState = "Alert and deliberate.",
            ownedMemories = listOf(NpcActorEvidenceRef(memoryId, "MEMORY", "A prior local safety check.", "ACTOR_OWNED")),
            ownedBeliefs = listOf(NpcActorEvidenceRef(beliefId, "BELIEF", "The immediate hazard must be checked first.", "ACTOR_OWNED")),
            goals = listOf(NpcActorEvidenceRef("goal-$caseId", "GOAL", "Complete one bounded safe action.", "ACTOR_OWNED")),
            revealableInformationIds = listOf(beliefId),
            withholdableInformationIds = listOf(beliefId),
            forbiddenInformationIds = listOf("author-secret-$caseId"),
            outputConstraints = NpcActorOutputConstraints(maxUtteranceChars = 48),
        )
    }

    private fun actorResponse(
        request: NpcActorRequest,
        caseId: String,
        attemptNumber: Int,
    ): NpcActorResponse = NpcActorResponse(
        requestId = request.requestId,
        actorId = request.actorId,
        targetIds = request.targetIds,
        speechAct = "NONVERBAL",
        immediateGoal = "Complete one safe action without inventing facts.",
        beliefClaimIds = listOf("belief-$caseId"),
        memoryIds = listOf("memory-$caseId"),
        emotionShown = "focused",
        emotionHidden = "cautious",
        actionType = "inspect_local_pressure",
        actionIntent = "Test bounded action attempt $attemptNumber.",
        visibleAction = "Checks the immediate physical target before committing.",
        utterance = "",
        nonverbalAction = "Pauses, braces, and tests the proposed action.",
        predictedCost = "One bounded offline tick.",
        observableConsequence = "The attempt remains pending until all validators accept it.",
        confidence = 70,
        decisionSummary = NpcActorDecisionSummary(
            evidenceIds = listOf("memory-$caseId", "belief-$caseId", "goal-$caseId"),
            selectedActionLabel = "bounded_physical_action",
        ),
    )

    private fun context(caseId: String, locationId: String): ContextPacket = ContextPacket(
        project = Project(PROJECT_ID, "NPC Actor Safety Loop", "Non-Canon offline safety evaluation."),
        turnId = "turn-$caseId",
        turnNumber = 1,
        playerInput = "[NON_CANON_OFFLINE_NPC_TICK:$caseId]",
        activeScene = null,
        sceneContract = null,
        characters = listOf(
            CharacterProfile(ACTOR_ID, PROJECT_ID, actorName(caseId), "npc", "Synthetic safety-loop actor."),
            CharacterProfile(PLAYER_ID, PROJECT_ID, "Player", "player", "Synthetic off-screen player.", isPlayer = true),
        ),
        characterMemories = listOf(
            CharacterMemory(
                id = "memory-$caseId",
                projectId = PROJECT_ID,
                characterId = ACTOR_ID,
                summary = "A prior local safety check.",
            ),
        ),
        characterStates = listOf(
            CharacterState(ACTOR_ID, PROJECT_ID, locationId, "focused", "stable"),
            CharacterState(PLAYER_ID, PROJECT_ID, PLAYER_LOCATION, "neutral", "stable"),
        ),
        knowledgeClaims = listOf(
            ContextKnowledgeClaim(
                claimId = "belief-$caseId",
                characterId = ACTOR_ID,
                propositionKey = "local.safety",
                statement = "The immediate hazard must be checked first.",
                epistemicState = "BELIEVES",
                confidence = 80,
                sourceType = "fixture",
                sourceMemoryId = "memory-$caseId",
            ),
        ),
        continuityConstraints = ContinuityConstraints(
            currentSceneLocationId = locationId,
            characterLocations = mapOf(ACTOR_ID to locationId, PLAYER_ID to PLAYER_LOCATION),
        ),
    )

    private fun actorName(caseId: String): String = when {
        "mira" in caseId -> "Mira"
        "lian" in caseId -> "Lian"
        "ren" in caseId -> "Ren"
        else -> "Yan"
    }

    private inline fun <reified T> writeStrict(path: Path, value: T) {
        path.parent?.let(Files::createDirectories)
        Files.writeString(path, NpcActorJson.strict.encodeToString(value) + "\n")
    }

    private fun evidence(root: Path, path: Path): PhysicalEvidenceFile = PhysicalEvidenceFile(
        path = root.relativize(path).toString().replace('\\', '/'),
        sha256 = sha256(path),
        bytes = Files.size(path),
    )

    private fun sha256(path: Path): String = MessageDigest.getInstance("SHA-256")
        .digest(Files.readAllBytes(path))
        .joinToString("") { "%02x".format(it) }

    private const val PROJECT_ID = "npc-actor05-non-canon"
    private const val ACTOR_ID = "adult"
    private const val PLAYER_ID = "player"
    private const val PLAYER_LOCATION = "location-player-elsewhere"
}
