# Project Controller — NPC-ACTOR-06

## Current route

```text
NPC-ACTOR-05 provides the bounded safety loop
→ Product Owner authorizes one local 8B model Pilot
→ direct llama.cpp CLI process, no server or port
→ strict JSON candidate enters existing validators
→ at most one sanitized repair attempt
→ local-model technical evidence, never an automatic formal-state write
```

GitHub remote is the team authority. Start from `PROJECT_STATE.md`, then read:

1. `PLANS.md` Current Plan Window V15;
2. `docs/work-packages/NPC-ACTOR-06_LOCAL_MODEL_PILOT.md`;
3. `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`;
4. `docs/npc_actor/NPC_ACTOR05_IMPLEMENTATION_REPORT.md`;
5. only files required by NPC-ACTOR-06.

## Completed work

- NPC-ACTOR-06 已在 GTX 1080 Ti 上驗證 Qwen3-8B Q5_K_M 與 llama.cpp Vulkan 直接 CLI；
- 本機模型輸出已接入既有安全鏈，最多一次已清理修訂，不開 server／port；
- Lian 真實模型候選第一次通過，Yan 真實模型候選兩次都被安全拒絕；
- focused 8/8、NPC Actor／Physics related 84/84、full 961/961 tests pass；
- 本機模型只證明技術 Pilot，正式 Runtime 自動調用仍未授權；
- a bounded offline action-safety loop now composes the existing actor contract, physical binding, physical affordance, Game Master legality and time-budget checks;
- each action proposal receives at most two attempts;
- first-attempt failures return only a sanitized repair request;
- accepted output is only `ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE`, never an automatic state write;
- four frozen cases prove first-pass acceptance, knowledge repair, physical repair and bounded final rejection;
- focused 9/9, related 43/43 and full 953/953 tests pass;
- no user A/B rating is required.

## NPC-ACTOR-06 authorization boundary

- local 7B／8B model installation and direct CLI inference are authorized;
- the approved route is a direct process with strict JSON files, not a server, port or cloud API;
- model output is only a non-Canon candidate and must enter all existing validators;
- model/runtime files stay outside the Repository; Git stores identity, Hash, contract and evidence only.

## Hard boundaries

- the fixed offline cases are engineering evidence, not live-model or human-naturalness proof;
- do not connect a cloud API, paid Provider, model server or port listener; local direct-CLI authorization is limited to NPC-ACTOR-06;
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

NPC-ACTOR-05's model authorization Gate was cleared only for the reviewed
NPC-ACTOR-06 local direct-CLI Pilot, which is now complete. Do not extend that
authorization to cloud, paid Providers, servers, ports or automatic formal-state
progression. A new reviewed work package and Product Owner authorization are required.
