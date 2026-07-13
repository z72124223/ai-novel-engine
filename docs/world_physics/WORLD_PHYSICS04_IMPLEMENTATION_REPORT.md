# WORLD-PHYSICS-04 Implementation Report

Date: `2026-07-14`

Decision: `WORLD_PHYSICS04_PHYSICAL_STATE_PERSISTENCE_PASS`

Branch: `codex/world-physics04-physical-state-persistence`

Base: `931c7d6463a71fe8e8a0706f8ca8c7902ea620c9`

Implementation Commit: `TBD_AFTER_VERIFIED_COMMIT`

## Outcome

WORLD-PHYSICS-04 adds bounded physical runtime state to the existing formal
turn transaction. A successful V2 turn may now persist declared `LIFT`,
`PUSH`, `PLACE_ON`, `DROP`, `ENTER_CONTAINER` and `PASS_THROUGH` actions only
after the existing identity, allowlist, Hash and physical-affordance gates pass.

The persisted boundary is always:

```text
FORMAL_TURN_RUNTIME_NON_CANON
```

No runtime measurement, location or relation is promoted to Chenxing Canon.
V1 contexts remain compatible for transient `USE_AS_SUPPORT` and `REACH`;
V1 stateful actions remain explicitly rejected.

## Additive SQLite Model

Migration `016_physical_state_runtime.sql` adds three tables without editing
Migrations 001–015:

1. `physical_archetype_registries` stores canonical archetype JSON once by
   SHA-256.
2. `formal_physical_states` stores one current bounded snapshot per Session.
3. `formal_physical_state_transitions` stores append-only action transition
   evidence with before/after versions and snapshot hashes.

The Chenxing rebuild manifest now verifies 22 inputs and 16 migrations. The
existing five Setting Packs, Canon validation and 255 World Source chunks
remain reproducible.

## Formal Context V2

V2 adds exact bindings for:

- `physicalStateVersion`;
- `physicalSnapshotCanonicalSha256`;
- `physicalArchetypeRegistryCanonicalSha256`.

Version 0 is allowed only as the first reviewed runtime seed. Once state
exists, a later sidecar must exactly match the persisted Session version,
snapshot hash and archetype-registry hash. Stale or tampered input fails before
the formal write and is rechecked inside the transaction to cover races.

The optional V2 fields and `supportedBySurfaceId` use non-default encoding so
historical V1 JSON does not gain new null keys during round-trip generation.

## Deterministic State Reduction

`PhysicalStateReducer` applies accepted proposals sequentially:

- `LIFT` records held-by and clears support/containment;
- `PUSH` moves the target to the validated destination;
- `PLACE_ON` records support entity/surface and recomputes surface load;
- `DROP` releases the target and moves it to the V1 ground plane;
- `ENTER_CONTAINER` records actor containment at the container anchor;
- `PASS_THROUGH` moves the actor to the validated destination.

The read-only CLI command below exposes current non-Canon state without
modifying it:

```text
show-physical-state [--db <path>] --session <id> [--out <path>]
```

## Transaction Evidence

Physical transitions are inserted inside
`SqliteStoryRepository.resolvePendingTurn`, before ordinary story deltas but
inside the same SQLite transaction. A fixed negative test allows the physical
transition to be prepared and then forces a later invalid character-state
write. The resulting rollback leaves all of the following unchanged:

- current physical state;
- physical transition count;
- archetype registry rows;
- story state deltas;
- Session `stateVersion` and `nextTurnNumber`;
- pending context and applied-turn count.

The success path applies sequential `LIFT` then `DROP`: the story Session
advances once, physical state advances from version 0 to 2, two transition rows
are committed and the crate finishes released on the bounded ground plane.

## Evidence Paths

- `schemas/world_physics/formal_physical_state_context_v2.schema.json`
- `samples/world_physics04/formal_physical_state_context_v2.json`
- `samples/world_physics04/formal_physical_state_result_v2.json`
- `samples/world_physics04/formal_physical_state_transition_plan_v2.json`
- `samples/world_physics04/formal_physical_state_technical_report_v2.json`
- `samples/world_physics04/formal_physical_state_evidence_hashes.json`

Committed SHA-256 values:

- V2 context: `2e377100aa9ede93cc349f6ab175d092862e84c86dc2dc98cc7114f19094e359`;
- result: `89b332f3911e381f187f00234b591d293609f320c62c22946c78aa098c94cc6f`;
- transition plan: `ee431d8a016efb40e785e4f31c9e5ac77542abdf42240dd8fe23970ab5c107c6`;
- technical report: `b59b9b7cb2d22a15cee342f7a5a9645424d8ea7f458b570e68d8286540f211f4`;
- V2 Schema: `8182b7e8003171d000a385605c067043dab3aeef1b680e1da083a9811cab0766`;
- Migration 016: `ce68f21c09a165b23ab38afdd2ad1d07fe98c6c8e17615125e452c830ddf0bbc`.

## Verification

- New focused persistence tests: `7/7 PASS`.
- Related physical regressions: `33/33 PASS` across WORLD-PHYSICS-01/02/03/04.
- Rebuild foundation tests: `3/3 PASS` with 22 inputs and 16 migrations.
- Full Gradle suite: `938/938 PASS`, zero failures, errors or skipped tests.
- JSON syntax: `7/7 PASS` for changed/new JSON files.
- V2 Schema contract: strict object, 19 required fields, constants, SHA-256
  forms and support-surface relation rule PASS.
- SHA-256 manifest: `6/6 PASS` against exact file bytes.
- `git diff --check`: PASS.
- Added-content private-path, secret, runtime-network and media scans: zero findings.

## Claim Boundary

This package proves atomic persistence only for declared, bounded actions that
the V1 deterministic affordance rules accept. It does not infer actions from
prose, simulate continuous collision, joints, fluids, cloth, fracture or
animation, or prove general real-world understanding. `PLACE_ON` uses a
support-top approximation and `DROP` uses the V1 ground plane. These runtime
approximations are explicitly non-Canon.

No later physical package, API, Provider, model download, image, video, audio
or Canon promotion is authorized by this result.
