# WORLD-PHYSICS-01 — Physical Affordance Foundation

Status: `FOUNDATION_TECHNICAL_PASS_RUNTIME_NOT_CONNECTED`

Date: `2026-07-14`

Parent blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`

## Goal

Implement a deterministic, offline and side-effect-free physical affordance
validator that rejects common morphology, support, reach, load, barrier,
container, passage and gravity contradictions before an action can be treated
as a valid state proposal.

## Required Production Work

- serializable metric vector, dimensions, bounds, entity profile/state,
  barrier, world snapshot, action proposal and validation-result models;
- `PhysicalAffordanceValidator` with stable rejection codes;
- a fixture factory for reproducible, explicitly non-Canon evaluation cases;
- no file, database, network or global-state mutation from validation.

## Required Rules

1. Every referenced actor, target, support, container or opening must exist.
2. Physical values must be finite and non-negative where required.
3. Reach uses world-snapshot geometry, not a distance claimed by the proposal.
4. A blocking axis-aligned barrier rejects a reach or movement segment.
5. Support requires a stable support surface and enough remaining load capacity.
6. A medium dog profile is not a valid adult standing support.
7. Lift and push enforce actor limits and target movability.
8. Placement requires held ownership, reachable support, capacity and footprint fit.
9. Container entry and passage enforce opening/interior dimensions and load.
10. Drop requires a held movable object and positive gravity, and emits required
    effects for downward fall and release.
11. Missing evidence fails closed; no Canon value may be invented.
12. Evaluation must not mutate its input snapshot.

## Evaluation Suite

Commit at least 60 fixed `NON_CANON_EVALUATION` cases covering all eight action
types, with both valid and invalid variants. Mandatory cases include:

- adult uses stable chair arm or floor rail as reasonable support;
- adult uses medium dog's shoulder as support and is rejected;
- child-sized load versus adult-sized load on limited support;
- light and excessive lift/push loads;
- reachable, out-of-reach and through-wall targets;
- fitting and non-fitting containers/openings;
- placement on stable, undersized and overloaded surfaces;
- held and unheld drop proposals, with gravity-derived effects.

The expected result and rejection codes are part of each fixture.

## Allowed Scope

- `PLANS.md`, `PROJECT_STATE.md`, `.codex/project_controller.md`;
- WORLD-PHYSICS blueprint, work package and implementation report;
- new Kotlin model/service/test files dedicated to WORLD-PHYSICS-01;
- `schemas/world_physics/` and `samples/world_physics01/`;
- narrow `.gitattributes` additions for new JSON evidence.

## Prohibited Scope

- modifying existing migrations, SQLite data, Canon or Setting Packs;
- changing `AiTurnResultValidator`, `StateReducer`, NPC-ACTOR-03/04 files or
  historical evidence;
- adding CLI or `Main.kt` registration in this package;
- live API, Provider, SDK, model download, server, port or paid execution;
- image, video or audio generation;
- claiming complete physical simulation or production runtime enforcement.

## Verification

- focused test class passes every mandatory rule and all committed fixtures;
- full Gradle suite passes without reducing existing coverage;
- committed JSON parses strictly and validates against the V1 Schema;
- SHA-256 manifest matches file bytes after checkout;
- no secret, private absolute path, network endpoint or Provider credential;
- Git diff contains only the allowed scope;
- input world snapshot remains byte-for-byte equal before and after validation.

## Completion Decision

Use `FOUNDATION_TECHNICAL_PASS_RUNTIME_NOT_CONNECTED` only when every required
rule and evidence check passes. This decision authorizes no Runtime, SQLite,
Canon or live-model integration. The only next candidate is WORLD-PHYSICS-02,
which requires a separately updated plan.
