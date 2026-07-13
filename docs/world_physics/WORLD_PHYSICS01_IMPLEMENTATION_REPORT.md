# WORLD-PHYSICS-01 Implementation Report

Date: `2026-07-14`

Status: `FOUNDATION_TECHNICAL_PASS_RUNTIME_NOT_CONNECTED`

Base SHA: `3b86e967891c48ea52789daf25698b8ca611e3b0`

Branch: `codex/world-physics01-physical-affordance`

## Outcome

WORLD-PHYSICS-01 adds a deterministic, offline physical-affordance validation
core. It uses engine-owned metric geometry and physical profiles instead of
trusting a model's prose description of distance, support or load.

The mandatory regression is enforced: a 70 kg adult using a medium dog's
shoulder for stand-up assistance is rejected because the canine shoulder is
not a stable support, does not allow that use, does not accept the humanoid body
plan and exceeds its bounded load capacity. A nearby stable chair arm with
sufficient capacity is accepted for the same bounded support use.

## Implemented Rules

- continuous three-dimensional surface distance in centimetres;
- axis-aligned barrier intersection for reach and movement;
- body plan and support-use compatibility;
- stable support, existing load and remaining capacity;
- grasping, movability, lift and push limits;
- held-object ownership, placement footprint and destination capacity;
- container opening, interior dimensions and contained load;
- passage opening and destination path;
- positive gravity and required release/fall effects for drop;
- stable machine-readable rejection codes and fail-closed missing references;
- side-effect-free validation: input snapshots are unchanged.

## Evidence

- Kotlin models: `app/src/main/kotlin/com/localnovel/engine/model/PhysicalAffordanceModels.kt`
- Validator: `app/src/main/kotlin/com/localnovel/engine/service/PhysicalAffordanceValidator.kt`
- Evaluation service and fixture factory under the same service package
- Focused tests: `app/src/test/kotlin/com/localnovel/engine/PhysicalAffordanceValidatorTest.kt`
- Schema: `schemas/world_physics/physical_affordance_v1.schema.json`
- 64-case suite: `samples/world_physics01/physical_affordance_suite_v1.json`
- Technical report: `samples/world_physics01/physical_affordance_technical_report_v1.json`
- Hash manifest: `samples/world_physics01/physical_affordance_evidence_hashes.json`

The suite covers all eight V1 action types with exactly eight cases each. Every
action family includes accepted and rejected evidence. All 64 actual results
match their committed expectations.

## Verification

- Kotlin compile: PASS;
- focused tests: `7/7 PASS`;
- full Gradle suite: `912/912 PASS`, failures 0, errors 0, skipped 0;
- JSON parsing: 4/4 files PASS (Schema, suite, report and manifest);
- Schema contract validation: suite/report/manifest `3/3 PASS` using an offline
  Draft-2020-12 subset checker that covers every keyword used by this Schema;
- SHA-256 manifest: suite/report/Schema `3/3 PASS` against exact file bytes;
- Git scope: PASS; no Migration, SQLite, Canon, Setting Pack, historical NPC
  artifact, `AiTurnResultValidator`, `StateReducer` or `Main.kt` change;
- no network client, API key, Provider call, model download or media generation.

## Data-Growth Correction

The first generated suite repeated all archetypes in every snapshot and was
592,691 bytes. Before commit, archetypes were moved to one suite-level registry;
snapshots retain only instance state and barriers. The final suite is 255,294
bytes, a 56.9 percent reduction, with the same 64 cases and test outcomes.

This registry pattern is the intended future storage rule: common physical
facts are versioned once, while scenes store only instance-specific state.

## Claim Boundary

This is a conservative rigid-body approximation, not a full 3D simulation or
proof that an AI understands real-world physics. A pass means only that no V1
rule disproved the action.

The validator is not connected to formal Runtime, SQLite, Setting Packs,
`AiTurnResultValidator` or `StateReducer`. Production binding is a separate
`WORLD-PHYSICS-02` candidate and requires a new plan and explicit scope review.
