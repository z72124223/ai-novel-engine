# Pilot-2 Transition Refusal Decision Brief

Date: 2026-06-21

Status: DECISION_BRIEF_ONLY

## 1. VERDICT

VERDICT: DECISION_REQUIRED_NO_RUNTIME_APPROVAL

Pilot-2 Turn 12 provides useful evidence（證據） of a layer difference: context（上下文） generation recognized the exact legal transition intent `go to awning with witness`, while the first `resolve-turn` path refused the transition with `player_refused_transition`. This is a design decision point, not approval to modify Validator（驗證器）, StateReducer（狀態化約器）, schema（資料結構）, migrations（資料庫遷移）, or runtime（執行期） logic.

## 2. Problem Summary

Turn 12 shows a split between two surfaces:

- Context / transition intent recognition（轉場意圖識別） surface: the target transition `transition-perimeter-to-awning-witnessed` was present, used required intent `go to awning with witness`, and had no unmet prerequisites in `turn_012_context.json`.
- Resolve / refusal heuristic（拒絕啟發式） surface: the first transition result passed `validate-result`, but `resolve-turn` failed with `player_refused_transition` and the message that player input refuses transition, so a normal transition cannot be forced.

The observed behavior means the system can recognize a legal intent at context time, while a later refusal check can still decide that the broader player input refuses transition. That difference is observable and should be handled by an explicit 01/user decision.

## 3. Turn 12 Facts

Turn 12 was Codex-authored player input（Codex 代寫玩家輸入） and is not real human-player evidence. It is valid as a controlled system probe, but it must not be counted as真人玩家證據.

Context evidence:

- `contextId`: `ctx-04485d23-0ab5-470d-96d2-2af2df48a2af`
- `baseStateVersion`: `11`
- `contextHash`: `31198815d7c384a91de97c2369cc503075c25ed630a44cd4ce730cbe15848d39`
- `contextBudget.actualBytes`: `21366`
- `contextBudget.maxBytes`: `24576`
- Target transition: `transition-perimeter-to-awning-witnessed`
- Required intent: `go to awning with witness`
- Target unmet prerequisites: none recorded

First result evidence:

- `resultId`: `result-pilot2-turn-012-awning-transition-attempt`
- `transitionProposal`: present
- Proposed transition: `transition-perimeter-to-awning-witnessed`
- Proposed target scene: `scene-qingli-herb-awning`
- `validate-result`: PASS
- `resolve-turn`: FAIL
- Failure code: `player_refused_transition`

Retry evidence:

- `resultId`: `result-pilot2-turn-012-awning-transition-retry-no-transition`
- `transitionProposal`: absent
- `validate-result`: PASS
- `resolve-turn`: PASS
- Final `stateVersion`: `12`
- Final `nextTurnNumber`: `13`
- Final `currentSceneId`: `scene-qingli-herb-warehouse-perimeter`
- Final `pendingContexts`: `0`

## 4. Possible Cause Categories

These are evidence-based categories, not confirmed implementation findings:

1. Player input contains both an exact transition intent and many boundary/refusal phrases. The input explicitly says `go to awning with witness`, but also includes restrictions such as not entering the sealed warehouse, not crossing the powder line, not touching seals, not touching the door, not touching goods, and not forcing opening.
2. The transition intent parser（轉場意圖解析器） and resolve refusal heuristic may judge different surfaces. Context can match a required phrase and expose the transition, while resolve may scan the broader input for refusal semantics.
3. The first result narrative may have been conservative enough for validation, but still not enough for resolve to treat the transition as accepted. It proposed a legal awning transition and avoided forbidden sealed-warehouse actions, yet the refusal heuristic still blocked.
4. The retry result proves a safe fallback path exists: if the transition is not proposed, Turn 12 can still record procedural progress and advance the session without bypassing validation.

## 5. Options

| Option | Description | Main Risk | Done Criteria |
| --- | --- | --- | --- |
| A. Wait for real human Turn 13 | Ask the user for真人 Turn 13 player input before further Pilot-2 execution. | Slower progress; the transition finding remains unresolved for now. | New human input exists; 01 can authorize normal Turn 13 prepare/audit/result/validate/resolve. |
| B. Codex-authored Turn 13 | Run another controlled Codex-authored player input, clearly marked as non-human evidence. | More evidence may not generalize to真人 behavior; provenance can be overstated later. | 01 explicitly approves, input is labeled Codex-authored, and reports separate system-probe evidence from human evidence. |
| C. More evidence collection only | Continue docs/audit consolidation without runtime changes. | May create clearer summaries but not answer whether the refusal heuristic should change. | Evidence reports are complete, limits are clear, no lifecycle commands are rerun. |
| D. Small refusal heuristic investigation | Open a separate read-only or tests-only investigation（調查） of the refusal heuristic behavior without implementation changes. | Scope can drift into unapproved runtime repair. | 01/user explicitly approves investigation boundaries; no Validator/StateReducer behavior change is made; findings are documented. |
| E. Explicit runtime/Validator patch | Authorize a scoped repair to transition/refusal logic. | Highest risk: could weaken player consent, bypass safety checks, or destabilize transition semantics. | 01/user explicitly approves the fix, design spec, tests, rollback expectations, and implementation scope. This is not recommended as an automatic next step. |

## 6. Risks And Completion Conditions

Recommended risk controls:

- Human evidence and Codex-authored evidence must stay separated in all future reports.
- Any future transition test must preserve `contextHash`, `stateVersion`, Validator, and StateReducer boundaries.
- If a future result attempts transition and resolve refuses it, the permitted fallback is to revise the result JSON to avoid transition, not to bypass runtime checks.
- If a future investigation touches tests or source, it must be separately authorized; this brief is documents-only.

Completion conditions for the current brief:

- The layer difference is documented.
- Turn 12 provenance and final state are documented.
- Options, risks, non-goals, and recommended order are documented.
- No Kotlin, tests, runtime, schema, migration, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or StateReducer files are changed.

## 7. Not Recommended

Do not:

- Raise the context budget beyond `24576`.
- Bypass Validator or `contextHash`.
- Directly edit StateReducer.
- Directly write SQLite.
- Force a transition after `player_refused_transition`.
- Treat Codex-authored Turn 12 as真人玩家 evidence.
- Enter Candidate B or MVP-8 from this finding.
- Convert this brief into implicit approval for runtime or Validator changes.

## 8. Recommended Ranking

Recommended order for 01/user:

1. Prefer waiting for真人 Turn 13 input if Pilot-2 should continue as human evidence.
2. If human input is not available, either pause Pilot-2 for summary acceptance or approve a clearly labeled Codex-authored Turn 13 as system-probe evidence only.
3. If the transition/refusal gap must be understood before more turns, open a small refusal heuristic investigation with explicit read-only/tests-only boundaries.
4. Consider runtime/Validator repair only after an explicit decision, design note, and test scope. Do not auto-enter this path.

## 9. Suggested Next Smallest Milestone For 01

Preferred next milestone:

`Pilot-2 Turn 13 human-input request checkpoint`

Scope:

- Ask the user for真人 Turn 13 input.
- Do not run lifecycle commands until input exists and 01 approves the next execution step.
- Keep the Turn 12 transition/refusal finding as evidence for future design decisions.

Fallback safe milestone if no human input is available:

`Pilot-2 refusal heuristic investigation brief`

Scope:

- Documents-only or read-only/tests-only planning.
- No Validator, runtime, StateReducer, schema, migration, or resolve behavior changes without new explicit approval.
