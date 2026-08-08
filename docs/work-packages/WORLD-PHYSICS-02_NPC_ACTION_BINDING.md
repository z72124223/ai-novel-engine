# WORLD-PHYSICS-02 — NPC Action Binding

Status: `ACTIVE`

Date: `2026-07-14`

Parent blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`

## Goal

Bind the deterministic WORLD-PHYSICS-01 validation core to an offline NPC Actor
physical-action proposal path while preserving all existing NPC Actor V1 JSON,
knowledge boundaries and formal transaction gates.

## Contract Split

The model-facing prompt may contain only:

- the existing visibility-safe `NpcActorRequest`;
- allowlisted physical action types;
- visible physical target identifiers.

The engine-owned evaluation context contains:

- reviewed physical archetypes;
- the metric world snapshot and barriers;
- the exact allowlist used to validate the response.

The response contains the existing `NpcActorResponse` plus one structured
`PhysicalActionProposal`. The response may select identifiers, but it may not
provide or override geometry, mass, reach, support capacity or barriers.

## Required Validation Order

1. Validate prompt/context boundary, request identity, target binding and bounded sizes.
2. Validate the existing NPC Actor response contract.
3. Reject if physical actor, target, secondary target or action is not allowlisted.
4. Run `PhysicalAffordanceValidator` against the engine-owned context.
5. Emit `NpcActionProposal` only if every prior gate passes.
6. Preserve physical rejection codes and required effects in the result.

## Mandatory Cases

- stable chair support is accepted for Game Master review;
- adult using a medium dog's shoulder is rejected with stable physical codes;
- a reach through a wall is rejected;
- an action type outside the request allowlist is rejected before mapping;
- a target outside the visible/bound target list is rejected;
- actor identity mismatch is rejected;
- an invalid base Actor response cannot be rescued by valid physics;
- a valid held-object drop carries gravity-derived required effects;
- validation does not mutate engine-owned archetypes or snapshot;
- existing NPC Actor V1 JSON round trips and tests remain unchanged.

## Allowed Scope

- governance and WORLD-PHYSICS documents;
- new NPC physical binding model/service/test files;
- one strict Schema and non-Canon evaluation evidence under
  `schemas/world_physics/` and `samples/world_physics02/`;
- narrow `.gitattributes` additions for generated JSON evidence if needed.

## Prohibited Scope

- modifying `NpcActorModels.kt`, existing NPC Actor V1 Schemas or sealed evidence;
- modifying migrations, SQLite, Setting Packs, Canon, `AiTurnResultValidator`,
  `StateReducer`, `Main.kt` or Repository state-write code;
- sending engine-owned physical truth to a live model or Provider;
- generating images, video or audio;
- claiming complete real-world physics or formal Runtime enforcement;
- force push, broad staging or edits in the original dirty Worktree.

## Verification

- focused binding tests and all existing Actor/physical focused tests pass;
- full Gradle suite passes without deleting or weakening tests;
- strict JSON and Schema contract pass;
- committed report is reproducible and all SHA-256 entries match;
- no secrets, private absolute paths, network endpoints or Provider credentials;
- input engine-owned context is byte-for-byte unchanged after validation;
- Git diff contains only the allowed package scope.

## Completion Decision

Use `NPC_ACTION_BINDING_TECHNICAL_PASS_FORMAL_RUNTIME_NOT_CONNECTED` only when
all gates pass. This status does not authorize WORLD-PHYSICS-03, Canon physical
data, formal turn validation, SQLite writes or live model use.
