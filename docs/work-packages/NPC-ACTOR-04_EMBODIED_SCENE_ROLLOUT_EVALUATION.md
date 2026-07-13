# NPC-ACTOR-04 — Embodied Scene Rollout Evaluation

Status: `TECHNICAL_PASS / PRODUCT_OWNER_EMBODIED_REVIEW_PENDING / LIVE_API_BLOCKED`

Date: `2026-07-13`

Parent blueprint: `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`

## Goal

Replace sentence-level preference as the product Gate with a full embodied
scene-rollout evaluation. Judge whether an NPC perceives, prioritizes, moves,
acts, withholds, speaks and adapts coherently inside a persistent environment,
not merely whether one reply sounds better than another.

## Trigger Evidence

- NPC-ACTOR-03 passed its technical gates, but before rating the Product Owner
  identified a design limitation: the packet remained point-to-point dialogue
  and did not provide enough environment to evaluate a three-dimensional world.
- The eight-item packet and sealed answer key remain preserved as micro-dialogue
  evidence. They are not completed, revealed or used as a product decision.
- This package does not create a second calibration revision. It evaluates the
  already frozen revision-1 policy at a wider scene-state level.

## Required Evaluation Unit

Build four new non-Canon scenes, one for each recurring profile. Every scene
must contain:

- a zone map and explicit adjacency;
- at least four present actors with locations and visibility;
- at least four objects or props with stable identity and location;
- at least two simultaneous hazards, pressures or active needs;
- a monotonic scene clock;
- three shared environment beats;
- two complete candidate trajectories over those same beats;
- action, nonverbal behavior, optional speech, other-character reaction and
  explicit post-beat state changes;
- actor-owned evidence and separately forbidden author-only evidence.

The reviewer must compare complete trajectories, not isolated lines.

## Deterministic Gates

- exactly 4 new scenarios and 4 distinct recurring profiles;
- exactly 3 decision beats per candidate trajectory;
- both candidates receive the same environment event at every beat;
- every referenced actor, zone, object, hazard and fact exists;
- every perception reference is visible to the actor at that beat;
- movement uses an allowed adjacency edge and cannot teleport;
- scene time increases monotonically;
- state-delta targets are declared and resulting summaries remain continuous;
- each candidate contains at least one action before speech, one silent actor
  decision and one multi-agent/environment-dependent response;
- forbidden author-only tokens never appear in candidate trajectories;
- both trajectories remain plausible and spoken length stays within 25 percent;
- deterministic anonymous A/B placement is balanced 2/2;
- source identity, evidence, policy, seed and answer identity remain absent from
  the Product Owner packet.

## Human Review Dimensions

- `ENVIRONMENT_AWARENESS`
- `SPATIAL_COHERENCE`
- `PRIORITY_AND_TIMING`
- `SOCIAL_REALISM`
- `VOICE_NATURALNESS`
- `STATE_CONTINUITY`
- `KNOWLEDGE_BOUNDARY`

Selections remain `A`, `B`, `TIE` or `BOTH_FAIL`.

## Allowed Scope

- evaluation-only models, fixture factory, validation service and CLI under the
  existing NPC Actor package;
- focused tests and compatibility checks;
- `schemas/npc_actor/npc_actor_embodied_rollout_v1.schema.json`;
- `samples/npc_actor04/` evidence, reports, anonymous packet and sealed answer;
- governance, implementation report and Product Owner review guide.

## Prohibited Scope

- changing the NPC-ACTOR-03 revision-1 policy or its stored artifacts;
- opening or modifying the NPC-ACTOR-03 answer key;
- live Provider/API calls, API Key, cost or network adapter;
- SQLite, migration, Canon, Knowledge, Memory or production-state writes;
- Validator, StateReducer or transaction weakening;
- image, video or audio generation;
- claiming that text fixtures prove physical simulation or live-model quality.

## Verification

- focused environment, adjacency, visibility, timing, continuity, anonymity and
  human-result-contract tests;
- full Gradle test suite;
- JSON/Schema, SHA-256, secret, private-path, network and Git-scope checks;
- explicit proof that every scenario is new relative to NPC-ACTOR-02/03;
- Product Owner result saved and validated before answer-key disclosure.

## True Stop Conditions

Stop when the anonymous four-scene full-trajectory packet is ready for Product
Owner review. Stop separately before any live API/API Key/cost/privacy action,
production rollout, Canon decision, safety-boundary weakening or revision-1
policy change.

## Completion Evidence

- four scenes, four recurring profiles and twelve environment beats;
- deterministic anonymous candidate placement balanced 2/2;
- all reference, visibility, adjacency, timing, continuity, behavior, secret
  and length gates at zero violations;
- focused `7/7` and full `905/905` tests passed;
- four generated artifacts passed the strict JSON Schema;
- the sealed answer remains separate and no Product Owner result is inferred.
