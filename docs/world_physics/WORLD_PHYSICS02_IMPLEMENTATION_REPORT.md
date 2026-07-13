# WORLD-PHYSICS-02 Implementation Report

Date: `2026-07-14`

Decision: `NPC_ACTION_BINDING_TECHNICAL_PASS_FORMAL_RUNTIME_NOT_CONNECTED`

Branch: `codex/world-physics02-npc-action-binding`

Base: `f67d48a78e5943e477de3e855778cfc45a915a38`

Implementation Commit: `96451e4c4f60027d403c5aab11252f26d3215df2`

## Outcome

WORLD-PHYSICS-02 adds a provider-neutral, offline binding layer between the
existing NPC Actor contract and the deterministic physical affordance core.
An Actor response now reaches Game Master review through this new path only
when both gates pass:

```text
visibility-safe NPC Actor prompt
→ NpcActorResponseValidator
→ engine-owned physical context binding checks
→ PhysicalAffordanceValidator
→ NpcActionProposal for Game Master review
```

The implementation does not connect to formal turn validation, SQLite, Canon,
`AiTurnResultValidator` or `StateReducer`.

## Compatibility Decision

The historical `NpcActorRequest` and `NpcActorResponse` V1 models, Schemas and
sealed NPC-ACTOR evidence were not modified. WORLD-PHYSICS-02 uses a separate
strict contract:

- `NpcActorPhysicalPrompt`: provider-visible Actor request plus allowlisted
  physical action types and visible target identifiers;
- `NpcActorPhysicalEngineContext`: engine-owned archetypes, metric snapshot,
  barriers and the matching allowlists;
- `NpcActorPhysicalResponse`: existing Actor response plus one structured
  physical action proposal;
- `NpcActorPhysicalBindingResult`: separate Actor and physical validation
  evidence, with no proposal on rejection.

The provider-visible prompt contains no `massKg`, `dimensionsCm`,
`gravityMps2`, barriers or archetype registry. The model cannot author or
override these values.

## Fixed Evidence

Eight `NON_CANON_EVALUATION` cases are committed:

1. stable chair support accepted for Game Master review;
2. adult using a medium dog's shoulder rejected;
3. reach through a wall rejected;
4. valid held-object drop accepted with gravity-derived effects;
5. non-allowlisted physical action rejected;
6. non-bound physical target rejected;
7. physical actor identity mismatch rejected;
8. invalid base Actor response rejected before physical mapping.

Result: `8/8` matched the committed expectations. Two cases were accepted and
six rejected. The dog-shoulder case returned:

```text
BODY_PLAN_NOT_SUPPORTED
SUPPORT_CAPACITY_EXCEEDED
SUPPORT_USE_NOT_ALLOWED
TARGET_NOT_STABLE_SUPPORT
```

The wall case returned `REACH_BLOCKED_BY_BARRIER`. The drop case preserved
`FALL_DOWNWARD` and `RELEASE_HELD_OBJECT` as required effects. Validation left
the engine-owned context byte-for-byte unchanged.

## Evidence Paths

- `schemas/world_physics/npc_actor_physical_binding_v1.schema.json`
- `samples/world_physics02/npc_actor_physical_binding_suite_v1.json`
- `samples/world_physics02/npc_actor_physical_binding_report_v1.json`
- `samples/world_physics02/npc_actor_physical_binding_evidence_hashes.json`

SHA-256:

- suite: `42517d591d03d828d8ea7cbdd1a0036a42eb6a37d75e86a542c7eb7b0c0f781a`
- report: `a9774a61a04b43f1e7500bbd024ccf02afb3509850f8ac8a18aa2d69ba77fe89`
- Schema: `3ad50949db5d621ab38fa625b4cae951493bfc1ba27ab7eec20798563966a456`

## Verification

- New focused test: `9/9 PASS`.
- Related focused tests: `23/23 PASS` across binding, Actor contract, Actor
  services and physical affordance validation.
- Full Gradle suite: `921/921 PASS`, zero failures, errors or skipped tests.
- JSON syntax: Schema, suite, report and manifest `4/4 PASS`.
- Schema contract: strict top-level/case objects and exact eight-case bounds PASS.
- SHA-256: `3/3 PASS` against committed file bytes.
- `git diff --check`: PASS.
- No API, Provider credential, private absolute path, migration, Canon, SQLite,
  media or formal state-write change.

## Claim Boundary

This package proves only that the committed offline binding path applies the
implemented V1 Actor and physical rules reproducibly. It does not prove full
real-world understanding. A physical pass remains a Game Master review
candidate, not an automatic state change.

WORLD-PHYSICS-03 may later consider formal turn integration, but it requires a
new reviewed plan, additive data decisions and explicit authorization.
