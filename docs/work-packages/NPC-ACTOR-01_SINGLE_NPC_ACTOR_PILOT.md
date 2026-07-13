# NPC-ACTOR-01 — Single NPC Actor Pilot

Status: `NOT_ACTIVE / BLOCKED_BY_RETRIEVAL_00_AND_RETRIEVAL_01`

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
