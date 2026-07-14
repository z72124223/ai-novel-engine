# NPC-ACTOR-04A — Internal Review Closure

Status: `ACTIVE / PRODUCT_OWNER_REPETITIVE_REVIEW_DECLINED`

Date: `2026-07-14`

## Goal

Replace the pending Product Owner A/B labor with one bounded, auditable internal
review. This closes the evaluation workflow; it does not claim human preference
or production NPC quality.

## Inputs

- `samples/npc_actor04/npc_actor04_blind_review_packet_v1.json`
- `samples/npc_actor04/npc_actor04_blind_review_answer_key_v1.json`
- `samples/npc_actor04/npc_actor04_embodied_suite_v1.json`
- `samples/npc_actor04/npc_actor04_technical_report_v1.json`
- `schemas/npc_actor/npc_actor_embodied_rollout_v1.schema.json`

The answer key remains unread by reviewers until all three review artifacts
have been completed, validated and hashed.

## Reviewer Roles

Exactly one result is required for each role:

1. `ENVIRONMENT_AND_PHYSICS_REVIEWER`
   - environment awareness;
   - spatial coherence;
   - priority and timing;
   - state continuity and ordinary physical plausibility.
2. `CHARACTER_AND_DIALOGUE_REVIEWER`
   - social realism;
   - voice naturalness;
   - character consistency;
   - whether speech supports action instead of explaining it.
3. `ADVERSARIAL_BOUNDARY_REVIEWER`
   - knowledge boundary;
   - hidden assumptions;
   - implausible causality;
   - option bias, strawman or answer leakage.

Every reviewer must still score all seven dimensions and select one complete
trajectory per scene. A role only changes the review emphasis.

## Review Contract

- packet id must match the frozen anonymous packet;
- reviewer id and role must be unique;
- `answerKeyReadBeforeCompletion` must be `false`;
- all four review ids must appear exactly once;
- selection is `A`, `B`, `TIE` or `BOTH_FAIL`;
- every dimension score is an integer from 1 to 5 for both options;
- evidence and issue tags are required and may reference only packet-visible facts;
- source labels, policy identity, randomization seed and answer identity are forbidden;
- one review may not quote or copy another review.

## Deterministic Aggregation

For each scene:

- three identical selections produce that consensus;
- two matching selections produce majority consensus;
- otherwise the scene result is `NO_CONSENSUS`;
- `TIE` is a valid consensus only when at least two reviewers choose `TIE`;
- `BOTH_FAIL` is a valid consensus only when at least two reviewers choose it.

After all three results pass validation and their SHA-256 values are recorded,
the aggregator may read the sealed answer key. It reports how many scene
consensus selections favored the frozen actor option, the control option, a tie,
both failed or had no consensus.

Overall decisions:

- `INTERNAL_REVIEW_COMPLETE`: all scenes have consensus and the actor option has
  more wins than the control option;
- `NO_INTERNAL_ADVANTAGE`: all scenes have consensus and the actor option does
  not have more wins than the control option;
- `INSUFFICIENT_INTERNAL_CONSENSUS`: any scene has no consensus.

No decision authorizes live API, production rollout, policy revision, Canon or
media work.

## Revision Limit

At most one revision may correct an invalid or incomplete internal review
artifact. The frozen NPC-ACTOR-04 packet, answer key, suite, policy and candidate
trajectories may not be revised in this package. A failed product conclusion is
recorded as evidence, not converted into another user test.

## Allowed Changes

- `PLANS.md`, `PROJECT_STATE.md`, `.codex/project_controller.md`;
- this work package and one implementation report;
- NPC Actor internal-review model, service, CLI and direct tests;
- one strict JSON Schema;
- `samples/npc_actor04/internal_review/` results, aggregate and Hash manifest;
- minimal `Main.kt` registration for the new CLI only.

## Prohibited Changes

- original NPC-ACTOR-01～04 evidence or sealed answers;
- frozen revision-1 policy or candidate trajectories;
- SQLite, Migration, Canon, Knowledge, Memory or formal state;
- physical rules, validators or reducers;
- API, Provider, model download, image, video or audio;
- force push, broad staging or work in the original dirty Worktree.

## Required Verification

- focused internal-review contract and aggregation tests;
- NPC Actor related tests and full Gradle suite;
- strict JSON and Schema validation;
- SHA-256 validation for inputs and all new evidence;
- answer-leak, secret, private-path, network and Git-scope checks;
- `git diff --check`.

## Completion

Commit and push every review and aggregate artifact. Update `PROJECT_STATE.md`
with the exact internal conclusion and the claim boundary. Stop after ordinary
integration into the remote default branch; do not create another evaluation
or ask the Product Owner to rate the packet.
