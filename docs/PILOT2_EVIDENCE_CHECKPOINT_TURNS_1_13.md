# Pilot-2 Evidence Checkpoint After Turns 1-13

Date: 2026-06-21

Status: `EVIDENCE_DECISION_CHECKPOINT`

## 1. VERDICT

`CHECKPOINT_PASS_WITH_DECISION_REQUIRED`

Pilot-2 now has complete local artifacts（本機產物） through Turn 13, including the Codex-authored Turn 13 player input（玩家輸入）, context（上下文）, first result, retry result, evidence（證據）, narrative audit（敘事稽核）, and Gradle test output sidecar. Turn 13 provides useful system stress evidence（系統壓測證據） for the transition/refusal mismatch, but it does not count as real human-player evidence（真人玩家證據）.

The main Turn 13 conclusion is narrow:

- legal awning transition intent was again present and the first result with `transitionProposal` passed `validate-result`;
- `resolve-turn` still rejected the formal transition with `player_refused_transition`;
- retry without `transitionProposal` resolved successfully and advanced to Turn 14;
- the formal scene remains `scene-qingli-herb-warehouse-perimeter`.

This checkpoint is a decision artifact. It does not approve Validator（驗證器）, runtime（執行期）, StateReducer（狀態化約器）, schema（資料結構）, migration（資料庫遷移）, `stateVersion`, `contextHash`, `resolve-turn`, MVP-7 Candidate B, or MVP-8 work.

## 2. Scope And Provenance

Sources:

- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md`
- `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md`
- `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md`
- `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md`
- `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_player_input.txt`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_context.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_context_audit.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_result.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_result_retry_01.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_evidence.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_audit_report.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_gradle_test_output.txt`

Provenance:

- Turns 1-11: inherited from the prior Pilot-2 workflow as player-input evidence, with the same limitation recorded in the Turns 1-12 checkpoint: local artifacts alone do not independently prove human authorship.
- Turn 12: explicitly Codex-authored; system-probe evidence only.
- Turn 13: explicitly Codex-authored; system stress evidence only.

Do not count Turns 12-13 as real human-player evidence.

## 3. Turn 13 State Summary

Pre-state:

- `stateVersion=12`
- `nextTurnNumber=13`
- current scene: `scene-qingli-herb-warehouse-perimeter`

Post-state:

- `stateVersion=13`
- `nextTurnNumber=14`
- `pendingContexts=0`
- current scene: `scene-qingli-herb-warehouse-perimeter`
- evidence export: `turnLogs=13`, `stateDeltas=150`
- formal transition: not applied

The scene remained the perimeter despite another explicit legal-awning probe.

## 4. Turn 13 Facts

Input:

- `turn_013_player_input.txt`
- Evidence classification: `Codex-authored player input. System stress evidence only; not human-player evidence.`
- Intent: try the legal oil-cloth awning route with less movement-refusal wording.
- Boundary wording used `邊界 / 不同意事項` rather than `明確拒絕 / 邊界`.

Context:

- `contextId=ctx-6a9dee92-6635-4ec0-ab85-c17d5ad3e561`
- `baseStateVersion=12`
- `contextHash=8d531253039441e152ee06779766853810fefdaf67b0917814041209ddb2cdf9`
- context bytes: `21213/24576`
- `audit-context`: PASS
- `hashValid=true`
- available transition included `transition-perimeter-to-awning-witnessed -> scene-qingli-herb-awning`

First result:

- file: `turn_013_result.json`
- `resultId=result-pilot2-turn-013-codex-awning-transition`
- `transitionProposal`: present
- proposed transition: `transition-perimeter-to-awning-witnessed`
- target scene: `scene-qingli-herb-awning`
- `validate-result`: PASS
- `resolve-turn`: FAIL
- failure: `player_refused_transition`

Retry result:

- file: `turn_013_result_retry_01.json`
- `resultId=result-pilot2-turn-013-codex-awning-transition-retry-no-transition`
- `transitionProposal`: absent
- `validate-result`: PASS
- `resolve-turn`: PASS
- final scene remained the perimeter

Narrative audit:

- file: `turn_013_audit_report.json`
- verdict: PASS
- score: 92
- severity: WARNING
- finding: `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`
- finding message: active story threads exist at a later turn, but no pending scheduled world event pressure is present.

## 5. Test Evidence Closure

03 initially returned `PASS_WITH_TEST_EVIDENCE_GAP` after Turn 13 because XML reports were not visible under the expected repo `app/build` location and 03 could not independently confirm the Gradle test count.

Closure commit:

- `e4e7a18 docs: record Pilot-2 Turn 13 test evidence`

Closure artifacts:

- console output sidecar: `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_gradle_test_output.txt`
- console output includes `BUILD SUCCESSFUL in 3m 24s`
- console output includes `6 actionable tasks: 6 executed`

XML report location:

```text
C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test
```

XML aggregate:

- report files: 11
- tests: 239
- failures: 0
- errors: 0
- skipped: 0

Reason reports were not under repo `app/build`:

- The project uses a relocated build directory under local appdata, consistent with prior OneDrive build safeguards.

## 6. Interpretation

Turn 13 strengthens the existing transition/refusal finding but still does not prove a runtime bug.

Supported interpretation:

- The context / available-transition layer can expose the legal awning transition.
- The first Turn 13 result can satisfy `validate-result` while still failing full `resolve-turn` validation.
- Reduced movement-refusal wording did not prevent `player_refused_transition`.
- This supports the hypothesis that resolve-layer refusal detection remains broader than the context-layer intent recognition surface, or that the two surfaces are not synchronized for this Pilot-2 phrasing.

Limits:

- Turn 13 is Codex-authored, not human evidence.
- One additional controlled probe is not enough to declare a confirmed bug.
- The player input still contains the literal technical token `player_refused_transition` in the quality-check section, so a residual confound remains possible.
- No runtime, Validator, parser, or StateReducer conclusion should be treated as implementation approval.

## 7. Decision Options

### A. Wait For Real Human Turn 14 Input

Best when Pilot-2 should continue as human-player evidence.

Done criteria:

- user provides real Turn 14 input;
- 01 explicitly approves the next turn execution;
- 02 runs normal prepare/audit/result/validate/resolve only after approval.

Risk:

- slower progress, but strongest evidence provenance.

### B. Run Another Codex-Authored Probe

Useful only if 01/user wants more controlled system stress evidence.

Rules:

- label it Codex-authored and non-human;
- do not count it as real human-player evidence;
- define a narrower probe before execution.

Risk:

- more non-human evidence may still not generalize to real player behavior.

### C. Open Read-Only Or Tests-Only Investigation

Use this if 01/user wants to understand refusal behavior before any runtime change.

Allowed shape:

- read source/tests;
- write investigation or reproduction brief;
- optionally draft tests-only design if explicitly approved.

Risk:

- can drift toward Validator/runtime repair unless boundaries stay explicit.

### D. Explicitly Approve Runtime / Validator / Transition Refusal Repair

Currently not approved and not recommended as an automatic next step.

Minimum approval should include:

- design note;
- exact behavior to change;
- regression tests;
- consent-safety analysis;
- rollback expectation.

Risk:

- highest risk because it may weaken player-consent protection or destabilize transition semantics.

## 8. Recommended Next Safest Milestone

Recommended default:

- stop at 01 decision and ask for real human Turn 14 input if Pilot-2 should continue as human evidence.

If automation should continue without human input:

- choose a docs-only completion audit or Pilot-2 evidence summary;
- do not automatically modify runtime, Validator, StateReducer, transition parser, `resolve-turn`, schema, migrations, `stateVersion`, or `contextHash`.

If the transition/refusal issue must be explored:

- open a separate read-only or tests-only decision milestone first.

## 9. Prohibitions

Do not:

- treat Codex-authored Turn 12 or Turn 13 as real human-player evidence;
- enter MVP-7 Candidate B or MVP-8 from this checkpoint;
- modify Validator, runtime, StateReducer, schema, migrations, `stateVersion`, `contextHash`, or `resolve-turn`;
- bypass `resolve-turn` or force a transition;
- direct-write SQLite;
- retag `mvp-6`;
- raise the `24576` context budget;
- commit ignored `out/` or `data/` artifacts.

## 10. 03 Review Checklist

03 should verify:

1. This document labels Turn 13 as Codex-authored system stress evidence, not human-player evidence.
2. Turn 13 pre/post state is recorded accurately: `12 -> 13`, `13 -> 14`, `pendingContexts=0`, scene remains perimeter.
3. Context facts are accurate: `21213/24576`, `hashValid=true`.
4. First result facts are accurate: `validate-result` PASS, `resolve-turn` FAIL `player_refused_transition`.
5. Retry facts are accurate: no `transitionProposal`, validate/resolve PASS.
6. Audit facts are accurate: PASS, score 92, warning `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`.
7. Test evidence closure references `e4e7a18`, the console sidecar, XML location, and `239/0/0/0`.
8. Interpretation does not overclaim a confirmed bug.
9. Decision options do not authorize runtime repair.
10. No ignored `out/` or `data/` artifacts are committed.

## 11. Checkpoint Conclusion

Pilot-2 through Turn 13 remains useful evidence for NPC procedure pressure, player consequence tracking, and transition/refusal behavior. The new Turn 13 evidence strengthens the decision question around `player_refused_transition`, because the first result still failed at resolve even after the player input reduced refusal-heavy wording. The safest next step is still a human/01 decision, not automatic runtime repair.
