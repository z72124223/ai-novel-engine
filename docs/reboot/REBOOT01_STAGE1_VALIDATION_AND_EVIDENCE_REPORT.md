# REBOOT-01 Stage 1 — Validation And Run Evidence Report

Date: 2026-07-12

Status: `STAGE_1_PASS_WITH_KNOWN_UNRELATED_FAILURE`

## Outcome

Stage 1 makes the execution boundary explicit without weakening the existing Validator, knowledge, hash, version, or SQLite transaction controls.

| Class | Runtime behavior |
|---|---|
| `PROTOCOL_HARD_FAIL` | Rejects the turn and persists the rejected attempt. |
| `WORLD_LEGALITY_HARD_FAIL` | Rejects the turn and persists the rejected attempt. |
| `IRREVERSIBLE_APPROVAL_REQUIRED` | Rejects until a separate, bound human approval artifact is supplied. |
| `QUALITY_WARNING` | Persists scores/findings and permits a legal turn by default; strict mode may reject. |

## SQL evidence

Additive migration `010_reboot_run_evidence.sql` creates `reboot_run_evidence` with indexes and no destructive statements. Each attempt can persist:

- run/project/session/turn/scenario identity;
- context id/hash, player input, and serialized context snapshot;
- raw result and raw/revised narrative;
- critic score, revision suggestion, validation findings, and primary class;
- state delta, apply outcome, elapsed time, and model metadata;
- irreversible contract, separate approval artifact, actor, and validated grant state.

Rejected malformed JSON and missing-context attempts receive protocol-envelope rows. A successful story apply updates evidence to `APPLIED` on the same connection and transaction as state mutation. Apply exceptions roll back story state, then record `APPLY_FAILED` outside the failed transaction.

## Irreversible boundary

The AI result cannot declare or approve its own irreversible change. The engine inspects protected state changes, including permanent/terminal health markers, inventory removal, protected relationship/faction/permanent/world-lock flags, and explicitly locked facts.

An irreversible operation requires:

1. a separate `IrreversibleChangeContract` bound to the current context hash and result id;
2. exact coverage of all engine-detected change types, affected entities, and delta targets;
3. a separate `IrreversibleApprovalArtifact` with a unique id, strict ISO-8601 timestamp, `APPROVE` decision, and `human:<nonblank-id>` actor;
4. exact approval binding to change id, context hash, and result id.

Rejected or malformed artifacts never set `irreversible_approval_granted=1`.

## Verification

- Compile: PASS.
- Focused suites: 101 tests, 101 PASS.
- `RebootRunEvidenceTest`: 17 tests, 17 PASS.
- Full suite: 826 tests, 822 PASS, 4 known unrelated World Source failures.
- High reasoning safety review: PASS after correcting contract omission, approval evidence, multi-type scope, and timestamp validation.
- `git diff --check`: PASS.

The four full-suite failures are unchanged from Stage 0 and all concern the tracked `09_aijiang_detailed_setting.md` byte-size/SHA mismatch against its formal manifest.

## Safety invariants retained

- no direct SQLite bypass;
- no weakening of `AiTurnResultValidator` or `StateReducer`;
- no bypass of `contextHash`, `stateVersion`, turn/project/session/context identity, or transaction checks;
- no change to Canon, Setting Pack sources, images, provider/API integration, or the paused image lane;
- original dirty worktree unchanged.

## Stage decision

Stage 1 is complete. Stage 2 may implement the minimum NPC autonomy loop while continuing to route every formal mutation through the same validation and transaction boundary.
