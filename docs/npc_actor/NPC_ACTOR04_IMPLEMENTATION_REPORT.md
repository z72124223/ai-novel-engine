# NPC-ACTOR-04 Implementation Report

Date: `2026-07-13`

Status: `TECHNICAL_PASS / PRODUCT_OWNER_EMBODIED_REVIEW_PENDING`

## Outcome

NPC-ACTOR-04 replaces isolated dialogue preference with four complete offline
scene trajectories. Each scene contains a persistent zone graph, actors,
objects, hazards, three monotonic environment beats, two anonymous candidate
trajectories and explicit state changes.

The four new non-Canon scenes are:

1. Mira — a kitchen fire, blocked passage, child, crowd and two exits;
2. Yan — a storm gate, rolling cart, child, messenger and controlled entry;
3. Lian — a market awning collapse, bleeding vendor, child and load transfer;
4. Ren — a rising-tide dock, fraying rope, moving cargo and unstable gangplank.

They share no scenario id or normalized title/context fingerprint with the
NPC-ACTOR-02 or NPC-ACTOR-03 development material.

## Implemented Contract

- explicit zone identity, symmetric adjacency and line-of-sight zones;
- stable actor, object and hazard identity plus initial location/state;
- exactly three increasing scene-clock beats;
- visibility-safe perceptions and action targets;
- adjacent-only movement and continuous `before → after` state transitions;
- action-first behavior, at least one silent decision, other-actor reaction and
  environment-dependent perception in both candidates;
- author-only evidence and AI-strawman phrase rejection;
- spoken-length balance within 25 percent;
- deterministic anonymous A/B placement balanced 2/2;
- Product Owner result validation before answer-key disclosure.

## Artifacts

- Schema: `schemas/npc_actor/npc_actor_embodied_rollout_v1.schema.json`
- Suite: `samples/npc_actor04/npc_actor04_embodied_suite_v1.json`
- Technical report: `samples/npc_actor04/npc_actor04_technical_report_v1.json`
- Anonymous packet: `samples/npc_actor04/npc_actor04_blind_review_packet_v1.json`
- Sealed answer: `samples/npc_actor04/npc_actor04_blind_review_answer_key_v1.json`
- Hash manifest: `samples/npc_actor04/npc_actor04_evidence_hashes.json`

## Verification

- focused tests: `7/7 PASS`;
- full Gradle suite: `905/905 PASS`, no failures, errors or skips;
- JSON Schema validation: `4/4 generated evaluation artifacts PASS`;
- report gates: 4 scenes, 4 profiles, 12 beats, A/B 2/2 and all structural,
  visibility, adjacency, timing, continuity, behavior, secret and length
  violation counts are zero;
- SHA-256 manifest covers the frozen revision-1 policy, suite, report, anonymous
  packet, sealed answer and Schema;
- no Provider, API Key, paid call, SQLite/Canon/Knowledge/Memory write, migration,
  image, video or audio generation was added.

The first full-suite attempt was interrupted by the command time limit and left
a temporary test-output file handle. A clean rerun in an isolated external
build directory completed `905/905`; this was an environment retry, not a code
failure.

## Claim Boundary

This is a deterministic, manually authored offline evaluation of scene-state
coherence. It does not prove physics simulation, live-model quality, human
likeness or production readiness. Product Owner review of all four anonymous
full trajectories is the current true stop condition.
