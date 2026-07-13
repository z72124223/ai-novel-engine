# WORLD-PHYSICS-03 Implementation Report

Date: `2026-07-14`

Decision: `FORMAL_TURN_PHYSICAL_GATE_PASS_STATEFUL_ACTIONS_BLOCKED`

Branch: `codex/world-physics03-formal-turn-gate`

Base: `3aace566e133fd89a50167d350c780c7ec95c996`

Implementation Commit: `bb05fd99c95b752278a000f1028014414ae755b9`

## Outcome

WORLD-PHYSICS-03 connects declared `PhysicalActionProposal` values to both
`AiTurnResultValidator` and `MultiTurnRuntime.resolveTurn`. The same gate is
available from:

```text
validate-result ... --physical-context <path>
resolve-turn ... --physical-context <path>
```

The sidecar is parsed with unknown keys rejected and is bound to:

- project, session, turn and context identity;
- `contextHash`, `turnNumber` and `baseStateVersion`;
- `resultId` and canonical `AiTurnResult` SHA-256;
- engine-owned archetypes, physical snapshot and allowlists.

If an `AiTurnResult` contains physical proposals without the sidecar, or a
sidecar is supplied without proposals, validation fails closed. The model may
select action and entity identifiers but cannot author metric truth.

## Transaction Boundary

The physical gate runs before narrative-quality evaluation and before
`repository.resolvePendingTurn`. A rejected dog-shoulder proposal was exercised
through the real multi-turn `resolveTurn` path. After rejection:

- `stateVersion` was unchanged;
- `nextTurnNumber` was unchanged;
- applied turn count remained zero;
- the pending context remained available;
- no `StateReducer`, Repository or Migration change was required.

The ordinary evidence-attempt record may still be written by the pre-existing
rejection reporting path. No formal story state transaction is applied.

## Persistence Decision

The current formal data model has no durable position or
held/supported/contained relation. Therefore V1 formal validation allows only:

- `USE_AS_SUPPORT`;
- `REACH`.

The following action types fail with
`physical_state_persistence_unavailable`, even if their geometry is feasible:

- `LIFT`;
- `PUSH`;
- `PLACE_ON`;
- `DROP`;
- `ENTER_CONTAINER`;
- `PASS_THROUGH`.

This avoids accepting a state change that the transaction cannot actually
persist. No speculative table or Canon measurement was added.

## Fixed Evidence

Eight reproducible evidence cases are committed:

1. stable chair support passes;
2. adult use of a medium dog's shoulder fails;
3. proposal without physical context fails;
4. physical context without proposal fails;
5. Context binding mismatch fails;
6. canonical Result hash mismatch fails;
7. non-allowlisted action fails;
8. stateful `DROP` fails until physical relation persistence exists.

Result: `8/8` matched the committed expectations.

The dog-shoulder rejection preserves the underlying deterministic evidence,
including `BODY_PLAN_NOT_SUPPORTED`, `SUPPORT_CAPACITY_EXCEEDED`,
`SUPPORT_USE_NOT_ALLOWED` and `TARGET_NOT_STABLE_SUPPORT`.

## Evidence Paths

- `schemas/world_physics/formal_turn_physical_context_v1.schema.json`
- `samples/world_physics03/formal_turn_physical_context_example_v1.json`
- `samples/world_physics03/formal_turn_physical_gate_suite_v1.json`
- `samples/world_physics03/formal_turn_physical_gate_report_v1.json`
- `samples/world_physics03/formal_turn_physical_gate_evidence_hashes.json`

SHA-256:

- Schema: `c8596c893f911609b9a60c6c827df0b750db046e08465ba8500e3c8ff4d92b3b`;
- example context: `02f89af2209880b520ec9fe4ed09480371272613afde560beac69b3b96e16186`;
- suite: `6ee6166048ac03069fbad12827a2e03013ec78a3513846babf8c4cd22d6feeb0`;
- report: `0598d17969a78ad5a75d86db66b9333409d83b24a077c801f6473868a558ad22`.

## Verification

- New focused formal-gate tests: `10/10 PASS`.
- Related focused tests: `57/57 PASS` across formal gate, engine, NPC binding
  and physical affordance suites.
- Full Gradle suite: `931/931 PASS`, zero failures, errors or skipped tests.
- JSON syntax: `5/5 PASS`.
- Schema contract: strict top-level keys, constants, bounds and referenced
  physical Schema presence PASS; the example also passes strict Kotlin decode
  and committed round-trip comparison.
- SHA-256 manifest: `4/4 PASS` against committed bytes.
- `git diff --check`: PASS.
- New-content private-path and secret scan: PASS; only JSON Schema identifier
  URLs were reviewed as non-runtime metadata.
- No Migration, Repository, `StateReducer`, Canon, media, Provider or network
  execution change.

## Claim Boundary

This package proves that declared, structured transient physical actions can be
validated reproducibly before a formal turn state transaction. It does not
extract undeclared actions from prose, persist physical relations, prove all
real-world physics, or establish Canon measurements.

The next physical persistence package, if ever authorized, must begin with a
separate reviewed state model and additive migration. It is not activated by
this report.
