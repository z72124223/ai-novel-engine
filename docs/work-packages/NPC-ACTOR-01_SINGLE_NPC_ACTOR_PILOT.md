# NPC-ACTOR-01 — Single NPC Actor Pilot

Status: `TECHNICAL_PASS / HUMAN_BLIND_REVIEW_PENDING`

Date: `2026-07-13`

Parent blueprint: `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`

## Goal

Implement one provider-neutral NPC Actor path and prove with fixed scenarios
that character-scoped decision and dialogue are more natural without leaking
knowledge or bypassing formal runtime safety.

## First Implementation Boundary

- extend existing `NpcAutonomyProfile`/Perception/Memory/Goal/Proposal flow;
- add `NpcActorRequest`, `NpcActorResponse` and strict JSON Schema;
- add `NpcActorGateway`, Fake and File implementations;
- add Validator mapping into existing `NpcActionProposal`/`AiTurnResult`;
- add 20 fixed non-Canon evaluation scenarios and baseline outputs;
- produce A/B metrics and a readiness report;
- do not make a live network request.

## Optional Live API Gate

Only after offline acceptance and explicit API Key/cost/privacy approval:

- add the smallest OpenAI Responses adapter;
- select model and reasoning per routing policy;
- set `store=false` by default;
- use strict Structured Outputs;
- allow no state-writing tools;
- run the same 20 scenarios and compare against ChatGPT audition baselines.

## Completion

The offline package passes only when all schema, focused/full tests and safety
checks pass and human blind review shows a measurable naturalness improvement
without any knowledge, Canon or transaction violation.

Passing the offline package does not approve paid API use or production rollout.

## Technical completion evidence

- provider-neutral Fake/File gateway and strict contracts implemented;
- 20/20 actor outputs accepted for downstream Game Master review;
- knowledge leak 0; continuity violation 0;
- focused 15/15 and full 886/886 tests PASS;
- anonymous review packet and separate answer key committed;
- no Provider, network, API Key, SQLite write or Canon promotion.

The remaining completion condition is a real human blind review. Codex must not
fabricate it, and no live API or production rollout may start from technical
completion alone.

## Activation evidence

- RETRIEVAL-00 and RETRIEVAL-01 are complete on remote default Commit
  `363b6535a6166e41a52577d51214bf9c40af4321`.
- The user explicitly requested the next long-running blueprint workflow on
  2026-07-13.
- Authorization covers only provider-neutral Fake/File execution. Human blind
  preference, paid API, API Key, privacy export and production rollout remain
  independent gates.
