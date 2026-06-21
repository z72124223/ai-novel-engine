# Pilot-2 Post-Turn13 Decision Handoff

Date: 2026-06-21

Status: `DECISION_GATE_READY`

## 1. VERDICT

`STOP_FOR_01_USER_DECISION`

Pilot-2 has validated evidence through Turn 13, including the post-Turn13 checkpoint commit `fcdbc5a docs: checkpoint Pilot-2 evidence after Turn 13`, which 03 reviewed with `PASS`. The project is now at a decision gate. The safe default is to stop and ask 01/user to choose the next path rather than starting Turn 14 or repairing runtime behavior automatically.

This document is a docs-only handoff. It does not start Turn 14, approve a new Pilot lifecycle run, or authorize runtime, Validator, StateReducer, transition refusal, schema, migration, `stateVersion`, `contextHash`, or `resolve-turn` changes.

## 2. Scope And Provenance

This handoff summarizes the already validated state after Turn 13. It uses existing docs and ignored Pilot artifacts as evidence, but it does not rerun any Pilot command.

Sources:

- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_13.md`
- `docs/PILOT2_CODEX_AUTHORED_INPUT_WORKFLOW.md`
- `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md`
- `docs/PILOT2_TRANSITION_REFUSAL_DECISION_BRIEF.md`
- `docs/PILOT2_REFUSAL_HEURISTIC_INVESTIGATION_BRIEF.md`
- `docs/PILOT2_TURN_TEMPLATE.md`
- `PLANS.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`

Non-goals:

- no Turn 14 start;
- no `prepare-turn`, `validate-result`, or `resolve-turn`;
- no runtime repair;
- no Kotlin, tests, schema, migration, Validator, StateReducer, `stateVersion`, `contextHash`, or `resolve-turn` edits.

## 3. Current Verified State

Review baseline:

- latest validated checkpoint: `fcdbc5a docs: checkpoint Pilot-2 evidence after Turn 13`;
- 03 review: `PASS`;
- branch: `codex/mvp6-checkpoint-branching-spec`;
- `mvp-6` tag target remains `be527031335efe8e2b0facae0145e1bbc084f8c6`.

Pilot-2 state:

- `stateVersion=13`;
- `nextTurnNumber=14`;
- `pendingContexts=0`;
- current scene remains `scene-qingli-herb-warehouse-perimeter`;
- legal awning transition has not been formally applied.

Evidence provenance:

- Turns 1-11 remain the inherited Pilot-2 player-input evidence set, with the limits already recorded in prior checkpoints.
- Turn 12 was Codex-authored system stress evidence, not real human-player evidence.
- Turn 13 was Codex-authored system stress evidence, not real human-player evidence.

## 4. What Is Settled

The following points are settled for planning purposes:

- Turn 13 context budget fit the hard ceiling: `21213/24576`.
- Turn 13 context hash validation passed: `hashValid=true`.
- Turn 13 first result passed `validate-result`.
- Turn 13 first result failed `resolve-turn` with `player_refused_transition`.
- Turn 13 retry result omitted `transitionProposal`, passed validation, and resolved successfully.
- Turn 13 advanced the formal session to `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`.
- The scene remained `scene-qingli-herb-warehouse-perimeter`.
- Turn 13 narrative audit passed with score `92` and warning `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`.
- The test evidence gap was closed by `e4e7a18 docs: record Pilot-2 Turn 13 test evidence`.
- The Gradle test evidence records `239` tests, `0` failures, `0` errors, and `0` skipped tests.
- The Turn 1-13 checkpoint docs have already been validated by 03.

## 5. What Is Not Settled

The following points remain open:

- Legal awning transition has still not been formally applied.
- `player_refused_transition` supports a mismatch or over-conservative-refusal hypothesis, but it does not prove a runtime bug by itself.
- The context-layer transition availability and resolve-layer refusal behavior are observably different in Turns 12-13, but the exact implementation cause is not settled.
- Codex-authored Turns 12-13 cannot be promoted to real human-player evidence.
- No automatic next step may modify Validator, runtime, StateReducer, transition parser, schema, migrations, `stateVersion`, `contextHash`, or `resolve-turn`.

## 6. Decision Gate

### A. Wait For Real Human Turn 14 Input

Recommended if Pilot-2 should continue as human-player evidence.

Completion conditions:

- user provides real Turn 14 input;
- 01 explicitly authorizes the next Pilot-2 lifecycle run;
- 02 labels the provenance clearly and executes only the approved turn flow.

Risk:

- slower progress, but strongest evidence provenance.

### B. Allow Another Codex-Authored Probe

Allowed only if 01/user wants more system stress evidence.

Completion conditions:

- 01 supplies or explicitly selects the Codex-authored player input;
- all artifacts label the input as Codex-authored and non-human evidence;
- no result is counted as real human-player evidence.

Risk:

- more controlled probes may still not generalize to human-player behavior.

### C. Allow Read-Only Or Tests-Only Reproduction Brief

Useful if 01/user wants better diagnosis before any runtime change.

Completion conditions:

- scope is explicitly read-only or tests-only;
- no runtime behavior changes are made;
- any proposed repair remains a decision brief, not an implementation.

Risk:

- can drift toward Validator/runtime repair unless the boundary stays explicit.

### D. Explicitly Approve Runtime / Validator / Transition Refusal Repair

Not approved by the current handoff and not recommended as an automatic next step.

Minimum approval should name:

- the exact behavior to change;
- the safety property that must remain protected;
- required regression tests;
- whether this is Validator, transition parser, `resolve-turn`, or StateReducer work;
- rollback expectations.

Risk:

- highest risk because it can weaken player-consent protection or destabilize transition semantics.

## 7. Automation Guidance

If there is no real Turn 14 input and no explicit runtime approval, 01 should only assign safe docs-only or read-only work, such as:

- completion audit;
- evidence consolidation;
- Pilot-2 summary for user decision;
- read-only source/test investigation brief;
- verification inventory.

01 should not assign 02 to:

- execute Turn 14 lifecycle;
- repair Validator/runtime/StateReducer;
- change transition refusal behavior;
- bypass `resolve-turn`;
- treat Codex-authored evidence as human-player evidence.

## 8. Stop Conditions

The following require explicit 01/user decision before 02 proceeds:

- real human Turn 14 input;
- another Codex-authored Turn 14 probe;
- MVP-7 Candidate B;
- MVP-8;
- schema or migration work;
- Validator, `stateVersion`, `contextHash`, `resolve-turn`, StateReducer, or runtime changes;
- deleting `data/`, `out/`, or `backups/`;
- retagging `mvp-6`;
- changing GitHub repository settings;
- product direction decisions, including whether to build UI, alter story gameplay, or open a new thread/window.

## 9. Recommended Next Action For 01

01 should notify the user and ask them to choose one of A/B/C/D:

- A: wait for real human Turn 14 input;
- B: allow another Codex-authored probe, clearly marked as non-human evidence;
- C: allow a read-only or tests-only reproduction brief;
- D: explicitly approve runtime/Validator/transition refusal repair.

If the user wants unattended continuation, 01 should assign only safe completion audit or docs consolidation. The next automatic milestone should not start Turn 14 and should not change runtime behavior.

## 10. 03 Review Checklist

03 should verify:

1. This document is docs-only and does not start Turn 14.
2. Current state is recorded as `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`.
3. Current scene is recorded as `scene-qingli-herb-warehouse-perimeter`.
4. Turn 13 is labeled Codex-authored system stress evidence, not human-player evidence.
5. The document records that legal awning transition has not been formally applied.
6. The document does not claim `player_refused_transition` is a proven bug.
7. Decision options A/B/C/D are framed as choices requiring 01/user authorization.
8. Automation guidance blocks Turn 14 lifecycle and runtime repair without explicit approval.
9. Stop conditions include Candidate B, MVP-8, schema/migrations, Validator/runtime/StateReducer, deletion of `data/out/backups`, and `mvp-6` retagging.
10. Tracked changes are limited to `PLANS.md` and this handoff document.
11. No ignored `out/` or `data/` artifacts are committed.

## 11. Handoff Conclusion

Pilot-2 should now pause at a decision gate. The evidence is useful, but it is not enough to justify automatic runtime repair, and Codex-authored Turns 12-13 are not human-player evidence. The safest next action is for 01/user to choose the next path explicitly.
