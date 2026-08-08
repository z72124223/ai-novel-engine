# WORLD-PHYSICS-04 — Physical State Persistence

## Authorization

Product Owner explicitly authorized this package on 2026-07-14 after
WORLD-PHYSICS-03 stopped at the physical persistence gate.

## Objective

Persist accepted stateful physical actions inside the existing formal turn
transaction without promoting physical measurements to Canon. The formal V2
context must bind the exact runtime snapshot and archetype registry by SHA-256,
and all later turns must match the persisted physical state version and Hash.

## Data Model

Migration `016_physical_state_runtime.sql` adds only new tables:

1. `physical_archetype_registries`: content-addressed reusable archetype JSON.
2. `formal_physical_states`: one bounded current snapshot per story Session.
3. `formal_physical_state_transitions`: append-only action transition audit.

The tables store `FORMAL_TURN_RUNTIME_NON_CANON` data. They do not establish
Canon facts or modify Setting Packs.

## Formal Context V2

V2 adds:

- `physicalStateVersion`;
- `physicalSnapshotCanonicalSha256`;
- `physicalArchetypeRegistryCanonicalSha256`.

Version 0 means the exact Hash-bound engine sidecar is the reviewed runtime
seed. Once a row exists, every later context must exactly match the persisted
version, registry Hash and snapshot Hash. V1 remains valid only for transient
`USE_AS_SUPPORT` and `REACH` checks.

## Deterministic Effects

- `LIFT`: target becomes held by actor and leaves supported/contained relations.
- `PUSH`: target moves to the validated destination and leaves exclusive relations.
- `PLACE_ON`: held target moves to the destination support surface and records
  support entity/surface; support load is recomputed from relations.
- `DROP`: target leaves held/support/container relations and falls to the V1
  ground plane derived from its engine-owned dimensions.
- `ENTER_CONTAINER`: actor moves to the container anchor and records containment.
- `PASS_THROUGH`: actor moves to the validated destination and leaves prior
  support/containment relations.

`USE_AS_SUPPORT` and `REACH` remain transient and do not advance the physical
state version.

## Transaction Rules

1. Run protocol, identity, stateVersion, knowledge and physical affordance gates.
2. Validate persisted physical version/hash binding before narrative selection.
3. Re-check the physical binding inside `resolvePendingTurn`.
4. Apply every stateful proposal sequentially through `PhysicalStateReducer`.
5. Insert transition evidence and update the current snapshot in the same
   transaction as all other formal turn writes.
6. Any later failure rolls back transition rows, physical state, story deltas,
   turn log and Session version together.

## Required Evidence

- additive/idempotent migration;
- V1 transient compatibility;
- V1 stateful fail-closed;
- V2 first-seed stateful success;
- stale physical version and snapshot Hash rejection;
- sequential lift/drop state reduction;
- push, place, container and passage effects;
- duplicate result/action protection;
- full transaction rollback after a later write failure;
- clean rebuild manifest with Migration 016;
- strict JSON Schema and SHA-256 manifest.

## Non-Goals

- no Canon promotion or invented Chenxing physical measurements;
- no text-to-action extraction;
- no complete collision, fluid, cloth, fracture or animation simulation;
- no API, Provider, model download, image, video or audio;
- no unrelated refactor or work in the original dirty Worktree.

## Completion Decision

Status: `WORLD_PHYSICS04_PHYSICAL_STATE_PERSISTENCE_PASS`.

All focused, rollback, rebuild and full-suite checks passed, and the remote
feature branch was pushed without force. This proves bounded declared stateful
actions persist atomically. It does not prove complete real-world understanding
or Canon truth. No next package is authorized automatically.
