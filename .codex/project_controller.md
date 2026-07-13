# Project Controller — NPC-ACTOR-01 Offline Pilot

## Current route

```text
RETRIEVAL-00 PASS
→ RETRIEVAL-01 SOURCE-LEVEL PASS
→ NPC-ACTOR-01 TECHNICAL PASS
→ HUMAN BLIND PREFERENCE GATE PENDING
→ explicit API Key / cost / privacy gate
→ optional live GPT API pilot
```

GitHub remote is the team authority. Start from `PROJECT_STATE.md`, then read:

1. `PLANS.md` current window;
2. `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`;
3. `docs/work-packages/NPC-ACTOR-01_SINGLE_NPC_ACTOR_PILOT.md`;
4. existing `NpcAutonomyModels.kt`, `NpcAutonomyServices.kt` and tests;
5. only files required by the current plan step.

## Authorized work

- provider-neutral `NpcActorGateway` contract;
- strict request/response JSON Schema;
- Fake and File implementations without network access;
- visibility-safe packet construction and response validation;
- mapping to the existing `NpcActionProposal` proposal boundary;
- 20 fixed `NON_CANON_EVALUATION` scenarios;
- reproducible baseline/actor outputs, anonymous A/B review packet and metrics;
- focused/full tests, reports, Commit and ordinary Push.

## Hard boundaries

- no API Key, paid call, Provider SDK or live network request;
- no direct SQLite, Canon, Memory or Knowledge write;
- no bypass of GameMaster, `AiTurnResultValidator`, `StateReducer`,
  `contextHash`, `stateVersion` or transaction boundaries;
- no author-only, director-goal, future-reveal or foreign-character private
  evidence in an NPC request;
- no hidden chain-of-thought persistence;
- no image/video/audio generation, Web server, port or public deployment;
- no force push, broad staging or work in the original dirty Worktree.

## Completion truth

Codex may prove technical safety and produce an anonymous blind-review packet.
It must not invent a human preference result. Record technical completion and
the human blind-review gate separately. Passing offline work never approves
live API or production rollout.

The implementation and technical evidence are complete. The current true stop
is the actual human rating of the anonymous 20-item packet. Do not open the
answer key before rating, and do not start live API work automatically.
