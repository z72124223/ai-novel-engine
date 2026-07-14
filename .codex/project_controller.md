# Project Controller — NPC-ACTOR-05

## Current route

```text
NPC-ACTOR-04A closes repeated Product Owner A/B labor
→ NPC-ACTOR-05 composes existing actor, physical and Game Master validators
→ first failure may request one sanitized repair
→ second result becomes a formal-resolve candidate or a final rejection
→ TECHNICAL_PASS / MODEL_INTEGRATION_AUTHORIZATION_REQUIRED
```

GitHub remote is the team authority. Start from `PROJECT_STATE.md`, then read:

1. `PLANS.md` Current Plan Window V14;
2. `docs/work-packages/NPC-ACTOR-05_OFFLINE_SAFETY_LOOP.md`;
3. `docs/npc_actor/NPC_ACTOR05_IMPLEMENTATION_REPORT.md`;
4. `samples/npc_actor05/npc_actor05_safety_loop_report_v1.json`;
5. only files required by a newly authorized plan.

## Completed work

- a bounded offline action-safety loop now composes the existing actor contract, physical binding, physical affordance, Game Master legality and time-budget checks;
- each action proposal receives at most two attempts;
- first-attempt failures return only a sanitized repair request;
- accepted output is only `ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE`, never an automatic state write;
- four frozen cases prove first-pass acceptance, knowledge repair, physical repair and bounded final rejection;
- focused 9/9, related 43/43 and full 953/953 tests pass;
- no user A/B rating is required.

## Hard boundaries

- the fixed offline cases are engineering evidence, not live-model or human-naturalness proof;
- do not connect a model, API, Provider or paid service without a new plan and explicit Product Owner authorization;
- do not let a safety-loop result write SQLite or bypass `AiTurnResultValidator`, `StateReducer`, `contextHash`, `stateVersion` or transactions;
- do not expose engine-only measurements, author secrets, hidden plans or hidden knowledge through repair requests;
- do not weaken Game Master, validator, physical, knowledge, transaction or hash boundaries;
- do not change sealed NPC-ACTOR-03/04/04A evidence;
- no Canon, Migration, Memory, Knowledge or formal Runtime state mutation;
- no image, video, audio, next chapter, Web server, port or public deployment;
- no force push, broad staging or work in the original dirty Worktree.

## Completion truth

NPC-ACTOR-05 proves that existing deterministic validators can form a bounded,
offline repair-and-reject loop around an NPC action proposal. It does not prove
that a live model will produce natural dialogue, understand every physical
environment or be ready for production. Rope tension, friction, wet slopes and
richer multi-object dynamics remain explicit limits.

Stop at the model integration authorization Gate. Do not start provider or live
model integration without a new reviewed plan and explicit Product Owner approval.
