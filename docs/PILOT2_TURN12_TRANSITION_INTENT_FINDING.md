# Pilot-2 Turn 12 Transition Intent Finding

Date: 2026-06-21

Status: `EVIDENCE_CONSOLIDATION`

## 1. VERDICT

`PASS_WITH_FINDING`

Pilot-2 Turn 12 produced useful transition evidence（轉場證據）. The exact transition intent（精確轉場意圖） `go to awning with witness` was recognized by the context（上下文） / available-transition（可用轉場） layer, but the first result（第一版結果） formal `transitionProposal` was later blocked during `resolve-turn` by `player_refused_transition`. The retry result（重試結果） avoided forced movement, resolved successfully, and advanced the formal session to Turn 13 without a scene transition（場景轉換）.

This document is evidence-only（僅證據整理）. It does not approve Candidate B, MVP-8, Validator（驗證器） changes, runtime（執行期） changes, schema（資料結構） changes, parser（解析器） changes, or any change to `resolve-turn` / `StateReducer`.

## 2. Input Classification

Turn 12 used Codex-authored player input（Codex 代寫玩家輸入）. It is useful operator-authored test evidence（操作者代寫測試證據）, but it is not real human-player evidence（真人玩家證據） and must not be counted toward Pilot-2 human-turn collection targets.

Evidence:

- Player input path: `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_player_input.txt`
- Result notes mark the input as Codex-authored.
- Retry result notes repeat that the input must not be counted as real human player evidence.

## 3. Context Layer Evidence

Source:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_context_audit.json`

Confirmed context data:

- `contextId`: `ctx-04485d23-0ab5-470d-96d2-2af2df48a2af`
- `baseStateVersion`: `11`
- `contextHash`: `31198815d7c384a91de97c2369cc503075c25ed630a44cd4ce730cbe15848d39`
- `contextBudget.actualBytes`: `21366`
- `contextBudget.maxBytes`: `24576`
- `audit-context`: `valid=true`, `hashValid=true`

The target transition appears in `availableTransitions`:

```text
transitionId: transition-perimeter-to-awning-witnessed
toSceneId: scene-qingli-herb-awning
targetLocation: loc-qingli-herb-awning
playerIntentRequired: go to awning with witness
travelMinutes: 1
visibleReason: reachable from current scene
```

The target transition entry has no `unmetPrerequisites`. By contrast, the other two transitions in the same context explicitly include `prerequisitesSatisfied=false` and `unmetPrerequisites` entries for their missing player intents. This supports the finding that Turn 12's exact intent was recognized at the context / available-transition layer.

Use this wording carefully: the emitted target transition did not include a literal `prerequisitesSatisfied=true` field; the evidence is the absence of `unmetPrerequisites` on the target transition combined with the presence of unmet prerequisite fields on the non-selected transitions.

## 4. First Result Evidence

Source:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_result.json`
- Prior execution output recorded in `PLANS.md`

First result:

- `resultId`: `result-pilot2-turn-012-awning-transition-attempt`
- `contextId`: `ctx-04485d23-0ab5-470d-96d2-2af2df48a2af`
- `baseStateVersion`: `11`
- includes `transitionProposal`: yes

Transition proposal:

```text
fromSceneInstanceId: scene-instance-93d1aea0-1e2a-4214-a98f-f7ed8eb16402
transitionId: transition-perimeter-to-awning-witnessed
toSceneId: scene-qingli-herb-awning
proposalType: PLAYER_INITIATED
playerIntentEvidence: 玩家台詞明確包含 go to awning with witness
participatingCharacterIds: char-player, char-patrol-he, char-clerk-luo, char-ai-chan
```

Observed command outcomes:

- `validate-result`: PASS
- `resolve-turn`: FAIL
- Failure code: `player_refused_transition`
- Failure message: `Player input refuses transition; normal transition cannot be forced.`

Meaning:

The result was structurally acceptable to `validate-result`, including the `transitionProposal`, but the formal resolve path still rejected normal transition because the player input contained broad refusal / boundary wording.

## 5. Retry Evidence

Source:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_result_retry_01.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_evidence.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_012_audit_report.json`

Retry result:

- `resultId`: `result-pilot2-turn-012-awning-transition-retry-no-transition`
- `contextId`: `ctx-04485d23-0ab5-470d-96d2-2af2df48a2af`
- `baseStateVersion`: `11`
- includes `transitionProposal`: no
- records first result failure in `validationNotes`

Retry command outcomes:

- `validate-result`: PASS
- `resolve-turn`: PASS
- formal transition: no

Final formal state from evidence:

- `stateVersion`: `12`
- `nextTurnNumber`: `13`
- `currentSceneId`: `scene-qingli-herb-warehouse-perimeter`
- `pendingContexts`: `0`
- `turnLogs`: `12`
- `stateDeltas`: `138`

Narrative audit:

- `verdict`: `PASS`
- `score`: `92`
- `severity`: `WARNING`
- findings: `1`
- warning: active story threads exist at a later turn, but no pending scheduled world event pressure is present.

## 6. System Meaning

Turn 12 shows an observable difference between two layers:

1. Context / transition recognition layer
   - The exact required player intent appeared in the Turn 12 input.
   - The target awning transition appeared in `availableTransitions` without unmet prerequisites.
   - The context was within the byte budget and hash-valid.

2. Resolve-layer refusal heuristic
   - The first result's `transitionProposal` passed `validate-result`.
   - Formal `resolve-turn` still rejected the transition with `player_refused_transition`.
   - The likely trigger is the broad refusal / boundary wording in the player input, such as refusal to enter the sealed warehouse, cross the powder line, touch the seal, touch the door, or touch goods.

This is not necessarily a bug by itself. It is a useful design finding: intent recognition and refusal detection can disagree when the same player input contains both an exact movement intent and strong safety boundaries.

## 7. Non-Recommended Fixes

Do not use Turn 12 as permission to:

- raise the `24576` context budget;
- bypass Validator;
- write SQLite directly;
- directly alter `StateReducer`;
- change `resolve-turn` behavior without approval;
- weaken `contextHash` or `stateVersion` protections;
- treat audit warnings as hard resolve gates;
- treat this Codex-authored turn as human-player evidence;
- enter Candidate B or MVP-8;
- infer that the transition must be forced because the context layer recognized the intent.

## 8. Recommended Next Step

Keep this finding as Pilot-2 evidence for future design decisions.

If 01 or the user wants to investigate a fix, it should be a separately approved milestone with a decision brief first. Possible future directions include:

- a read-only Validator / transition parser design brief;
- a human-authored retest with less broad refusal wording;
- a Candidate A read-only report comparing transition intent recognition against refusal detection;
- no code change, simply documenting that player inputs should separate movement intent from refusal boundaries more carefully.

Any milestone that changes Validator, transition parsing, `resolve-turn`, `StateReducer`, schema, migrations, `stateVersion`, or `contextHash` requires explicit 01 / user approval before implementation.

## 9. Handoff Summary

Turn 12 is accepted as evidence, not as proof that runtime behavior should change. The strongest actionable finding is the layer mismatch:

```text
context layer: exact intent recognized
first result: transitionProposal validate PASS
resolve layer: transition blocked by player_refused_transition
retry result: no transition, resolve PASS, state advanced to Turn 13
```

This should be reviewed by 01 before selecting any future milestone.
