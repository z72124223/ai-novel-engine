# Pilot-2 Refusal Heuristic Investigation Brief

Date: 2026-06-21

Status: READ_ONLY_INVESTIGATION

## 1. VERDICT

VERDICT: INVESTIGATION_PASS_NO_RUNTIME_APPROVAL

The read-only investigation（只讀調查） found a plausible source-level explanation for Pilot-2 Turn 12: context（上下文） transition recognition uses exact required-intent substring matching, while resolve-time transition validation uses a broader refusal heuristic（拒絕啟發式） over the full player input. The Turn 12 input contained the exact legal intent `go to awning with witness`, but also contained the standard section heading `明確拒絕 / 邊界` and many boundary phrases. This can explain why the target transition had no `unmetPrerequisites` in context, while `resolve-turn` later failed with `player_refused_transition`.

This is an investigation brief（調查簡報）, not a fix. It does not approve Candidate B, Validator（驗證器） changes, StateReducer（狀態化約器） changes, `resolve-turn` changes, runtime（執行期） changes, schema（資料結構） changes, migrations（資料庫遷移）, or tests.

## 2. Investigation Scope And Non-Goals

Scope:

- Read existing Pilot-2 docs and Turn 12 artifacts.
- Read source/test files found by:

```powershell
rg "player_refused_transition|refused_transition|refuse|transitionProposal|unmetPrerequisites" app/src/main/kotlin app/src/test/kotlin
```

- Document source-level observations and risk categories.
- Recommend non-code operating guidance and future decision options.

Non-goals:

- No Pilot-2 turn reruns.
- No `prepare-turn`, `validate-result`, or `resolve-turn`.
- No Kotlin, test, runtime, schema, migration, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or StateReducer edits.
- No direct SQLite writes.
- No Candidate B or MVP-8.
- No forced transition.
- No treatment of Codex-authored evidence as real human-player evidence.

## 3. Confirmed Turn 12 Facts

Turn 12 was Codex-authored player input（Codex 代寫玩家輸入）. It is useful system-probe evidence（系統探針證據）, but it is not real human-player evidence.

Context facts:

- `contextId`: `ctx-04485d23-0ab5-470d-96d2-2af2df48a2af`
- `baseStateVersion`: `11`
- `contextHash`: `31198815d7c384a91de97c2369cc503075c25ed630a44cd4ce730cbe15848d39`
- `contextBudget.actualBytes`: `21366`
- `contextBudget.maxBytes`: `24576`
- Target transition: `transition-perimeter-to-awning-witnessed`
- Required player intent: `go to awning with witness`
- Target transition `unmetPrerequisites`: none recorded

First result facts:

- `resultId`: `result-pilot2-turn-012-awning-transition-attempt`
- `transitionProposal`: present
- `proposalType`: `PLAYER_INITIATED`
- Proposed target scene: `scene-qingli-herb-awning`
- `playerIntentEvidence`: cites the exact player line containing `go to awning with witness`
- Recorded outcome: `validate-result` PASS, `resolve-turn` FAIL
- Failure code: `player_refused_transition`

Retry facts:

- `resultId`: `result-pilot2-turn-012-awning-transition-retry-no-transition`
- `transitionProposal`: absent
- Recorded outcome: `validate-result` PASS, `resolve-turn` PASS
- Final `stateVersion`: `12`
- Final `nextTurnNumber`: `13`
- Final `currentSceneId`: `scene-qingli-herb-warehouse-perimeter`
- Final `pendingContexts`: `0`

## 4. Source-Level Read-Only Observations

These are observations from source files, not changes.

### Available Transition Recognition

Observed files/functions:

- `app/src/main/kotlin/com/localnovel/engine/service/SceneThreadServices.kt`
  - `TransitionPlanner.plan(sessionId, playerInput)` delegates to `repository.listAvailableTransitions(sessionId, playerInput)`.
- `app/src/main/kotlin/com/localnovel/engine/repository/SqliteStoryRepository.kt`
  - `listAvailableTransitions(sessionId, playerInput)` calls internal selection logic.
  - Around lines 2341-2358, source reads `required_player_intent`, then computes `intentSatisfied = requirement.isBlank() || playerInput.contains(requirement, ignoreCase = true)`.
  - It builds `unmetPrerequisites`; if the intent is present and other checks pass, `prerequisitesSatisfied = unmet.isEmpty()` and `unmetPrerequisites = unmet`.

Observation:

- Context-time transition recognition is phrase-oriented and permissive: if the required phrase appears anywhere in player input, the intent prerequisite can be satisfied.

### Resolve-Time Transition Validation

Observed files/functions:

- `app/src/main/kotlin/com/localnovel/engine/service/MultiTurnRuntime.kt`
  - `resolveTurn` fetches the pending context, calls `validate(context, result)`, and stops before applying if the report is invalid.
  - `MultiTurnRuntime.validate` includes `TransitionValidator(repository).validate(context, result)`.
- `app/src/main/kotlin/com/localnovel/engine/service/SceneThreadServices.kt`
  - `TransitionValidator.validate(context, result)` returns early when `result.transitionProposal` is absent.
  - When a transition proposal exists, it checks available transition, target scene, prerequisites, exit gates, world-forced evidence, and character-initiated actor evidence.
  - Around lines 192-195, it builds a refusal match from the full `context.playerInput` using the keywords `不走`, `留下`, `拒絕`, `不離開`, `不要走`, `stay`, and `refuse`.
  - If that refusal match is true and the proposal is not `WORLD_FORCED`, it adds `player_refused_transition`.

Observation:

- Resolve-time refusal detection is broad and scans the full player input, not only the target transition phrase or the sentence immediately around `playerIntentEvidence`.
- The word `拒絕` appears in the standard Pilot-2 preferred input heading `明確拒絕 / 邊界`, so a transition-attempt turn written with the template may match the refusal keyword even when the body explicitly consents to a narrower legal transition.

### CLI Validation Surface Difference

Observed files/functions:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
  - `validate-result` reads a context/result file pair and calls `AiTurnResultValidator(repository).validate(context, result)`.
  - `resolve-turn` reads the result file and calls `MultiTurnRuntime(repository).resolveTurn(sessionId, result)`.
- `app/src/main/kotlin/com/localnovel/engine/service/AiTurnResultValidator.kt`
  - The validator checks project/turn identity, blank narrative, forbidden reveals, and delta payload target validity.
  - It does not include `TransitionValidator`.
- `app/src/main/kotlin/com/localnovel/engine/service/MultiTurnRuntime.kt`
  - `MultiTurnRuntime.validate` includes the multi-turn checks, including transition validation and refusal detection.

Observation:

- The recorded Turn 12 `validate-result` PASS followed by `resolve-turn` FAIL is consistent with different validation surfaces. `validate-result` is not the same as full `resolve-turn` validation for transition semantics.

### Existing Tests

Observed tests:

- `app/src/test/kotlin/com/localnovel/engine/Mvp4SceneThreadRuntimeTest.kt`
  - `playerRefusalBlocksNormalTransition` prepares input `我不走，我要留下。`, proposes a normal transition, and expects `player_refused_transition`.
  - Other transition tests cover required prerequisites, unreachable transitions, world-forced evidence, scene changes, and implicit transition without `transitionProposal`.
- `app/src/test/kotlin/com/localnovel/engine/Pilot1bCoverageRepairTest.kt`
  - `legalTransitionCoverageIncludesContextAndApply` confirms a transition can resolve when input accepts moving to perimeter.
  - `playerRefusalCannotBeResolvedAsNormalTransition` prepares input `I refuse to leave and stay exactly where I am.`, proposes a normal transition, and expects either `transition_prerequisite_unmet` or `player_refused_transition`.

Observation:

- Existing tests intentionally protect against forcing a normal transition when a player directly refuses movement.
- The tests read so far do not appear to cover the narrower Turn 12 shape: exact target-transition consent plus boundary refusals about other actions or template heading text.

## 5. Why Context Recognition And Resolve Refusal Can Diverge

The divergence appears understandable from the observed source:

1. Context recognition asks: does the full player input contain the required transition intent phrase?
2. Resolve refusal asks: does the full player input contain any broad refusal keyword while a non-`WORLD_FORCED` transition is proposed?
3. Turn 12 answered yes to both:
   - yes, it contains `go to awning with witness`;
   - yes, it contains the heading `明確拒絕 / 邊界`, plus many boundary sentences.

This means the two layers can both be operating as currently written and still disagree on the turn outcome.

## 6. Bug, Conservative Design, Or Semantic Ambiguity?

Current evidence supports a mixed classification:

- Bug-like risk: the refusal scan can be triggered by the Pilot-2 template heading `明確拒絕 / 邊界`, even when the player explicitly consents to the target transition.
- Conservative design: blocking normal transitions when the player says `不走`, `留下`, `refuse`, or similar is appropriate for player-consent safety.
- Semantic ambiguity: Turn 12 uses many "do not" boundaries. Some boundaries reject entering the sealed warehouse, touching seals, opening goods, or accepting liability; these are not the same as refusing to walk to the awning, but a broad scan cannot distinguish them.

Conclusion:

- It looks like a likely heuristic precision gap, especially because the standard input template contains `拒絕`.
- It should not be called a confirmed runtime bug without an approved tests-only or source investigation milestone.
- No automatic runtime or Validator patch is recommended from this brief alone.

## 7. Non-Code Operating Suggestions

For future player input:

- When intentionally testing a legal transition, keep the exact target intent in a short affirmative sentence.
- Avoid using the literal heading `明確拒絕 / 邊界` in a transition-attempt turn; use wording such as `邊界 / 不同意事項` if 01 approves a template-level wording change later.
- Separate movement consent from other refusals:
  - good: "I agree to move only to the awning with witness."
  - safer boundary wording: "I do not enter the sealed warehouse, do not cross the powder line, and do not touch goods."
  - avoid broad movement refusal near the transition proposal: "I refuse to leave" or "我不走".

For future result drafting:

- If context exposes a transition and the player input includes any broad refusal words, prefer a no-transition result unless 01 explicitly approves a controlled retest.
- If proposing transition, cite playerIntentEvidence narrowly and keep the narrative aligned with the proposed target only.
- Do not narrate entry into a target scene without `transitionProposal`; existing validation can flag implicit transition.
- If resolve returns `player_refused_transition`, retry by removing `transitionProposal` and documenting procedural progress rather than forcing movement.

For future docs:

- Treat `validate-result` PASS as basic result-file validation unless the current command is updated or a separate full-runtime validation command is introduced.
- Distinguish `validate-result` from `resolve-turn` in turn handoff reports.

## 8. If Source Is Changed Later: Minimum Design Questions

Any future source or tests-only investigation needs explicit 01/user approval. Minimum design questions:

1. Should `validate-result` remain a basic `AiTurnResultValidator` command, or should docs/CLI distinguish it from full multi-turn resolve validation more clearly?
2. Should the refusal heuristic inspect only movement-refusal phrases, or also boundary-refusal sections?
3. Should exact target transition consent override generic refusal keywords when the refusal targets other actions?
4. Should player input templates avoid refusal keywords in headings, or should runtime ignore headings?
5. Should refusal detection look at `transitionProposal.playerIntentEvidence` rather than the full `context.playerInput`?
6. Should future tests cover "explicit transition consent plus boundary refusals" as a separate case?
7. How should Chinese negation and legal-boundary language be separated from movement refusal without weakening player-consent safety?

Required approvals/risks before implementation:

- Validator and `resolve-turn` behavior are protected boundaries.
- Any change could accidentally force movement against player consent.
- Any change could make `validate-result` / `resolve-turn` reports diverge less, but also destabilize existing tests.
- Any StateReducer or transaction-path change is out of scope unless explicitly authorized.

## 9. Not Recommended

Do not:

- Bypass Validator or `contextHash`.
- Directly edit StateReducer.
- Force a transition after `player_refused_transition`.
- Directly write SQLite.
- Raise the context budget beyond `24576`.
- Enter Candidate B or MVP-8 from this finding.
- Treat Codex-authored Turn 12 as real human-player evidence.
- Treat this brief as approval to modify runtime, tests, or source.

## 10. Recommended Next Step

Recommended order:

1. Wait for真人 Turn 13 input if Pilot-2 should continue as human evidence.
2. If 01 wants another system probe, explicitly approve Codex-authored Turn 13 and label it non-human evidence.
3. If the heuristic must be investigated further, open a separate tests-only/source investigation milestone with explicit approval and a narrow test plan.
4. Do not automatically change runtime or Validator from this brief.

Suggested next smallest milestone for 01:

`Pilot-2 Turn 13 human-input request checkpoint`

Fallback if no human input is available:

`Pilot-2 refusal heuristic tests-only design brief`

That fallback should still be a decision artifact first; implementation should require another explicit approval.
