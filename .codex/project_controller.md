# Project Controller — NPC-ACTOR-10 Cloud API Route Pivot

## Current route — NPC-ACTOR-10

The Product Owner retired Qwen3-8B from the active NPC Actor generation route and selected a controlled OpenAI
Responses API route. Preserve every local model file and NPC-ACTOR-06～09C evidence item, but never invoke Qwen3-8B
as a hidden fallback or rerun it to tune until it passes.

Read in order:

1. `PROJECT_STATE.md`;
2. `PLANS.md` Current Plan Window V22;
3. `docs/work-packages/NPC-ACTOR-10_CLOUD_API_ROUTE_PIVOT.md`;
4. `docs/npc_actor/NPC_ACTOR_CLOUD_API_BLUEPRINT_V1.md`;
5. `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`;
6. `docs/npc_actor/NPC_ACTOR10_CLOUD_API_ROUTE_PIVOT_REPORT.md`.

The selected default is `gpt-5.6-terra` with `medium` reasoning. `gpt-5.6-sol` with `high` is a recorded escalation
only for a predeclared critical scene or measured evaluation failure. Never use `max`, Pro mode or Sol for every
ordinary turn. Never silently retry with a more expensive model.

The next implementation package is not authorized by this documentation pivot. Repository `AGENTS.md` still blocks
OpenAI API integration. After a separate governance revision, the smallest implementation package must build an
offline `OpenAiResponsesNpcActorGateway` contract with Fake HTTP only. It must not send a network request.

Do not place an API Key in Git, use a server／port, send full SQLite or author-only secrets, weaken any local Gate,
write formal state directly, call a paid Provider, resolve a real candidate, change Canon／Memory／Knowledge, or
generate media. A first live non-Canon Pilot requires an environment-only Key, explicit privacy packet and explicit
per-call／daily budget approval.

## Previous route — NPC-ACTOR-09C

Current package: `NPC_ACTOR09C_REAL_REJECTION_DIAGNOSIS` is complete as a safe program rejection.

Exactly one local Qwen3-8B prepare ran against a fresh valid offscreen Session. The proposal passed the local
safety loop and Game Master, but formal Runtime validation rejected it with `low_character_fit`. No candidate or
09B safety sidecar was written; failure cleanup removed the pending Context and official before／after Session
evidence is byte-identical. No resolve or second prepare ran.

Read in order:

1. `PROJECT_STATE.md`;
2. `PLANS.md` Current Plan Window V21;
3. `docs/work-packages/NPC-ACTOR-09C_REAL_REJECTION_DIAGNOSIS.md`;
4. `docs/npc_actor/NPC_ACTOR09C_REAL_REJECTION_DIAGNOSIS_REPORT.md`;
5. `samples/npc_actor09c/npc_actor09c_evidence_manifest.json` and only directly referenced evidence.

Do not retry Qwen3-8B, call resolve, lower `characterFitScore >= 70`, expose model text, connect a cloud API,
start a server／port, or change Canon／Memory／Knowledge. A new reviewed package is required before adding the
post-safety formal-validation rejection sidecar.

## Previous route — NPC-ACTOR-09B

Current package: `NPC_ACTOR09B_REJECTION_OBSERVABILITY` is complete and technically verified.

When formal local NPC preparation exhausts its two safety attempts, the CLI now writes a strict sanitized sidecar,
clears the pending Context, creates no candidate and applies no formal turn. The sidecar excludes prompt text, raw
output, generated responses, author secrets and engine-owned physical measurements.

Read in order:

1. `PROJECT_STATE.md`;
2. `PLANS.md` Current Plan Window V20;
3. `docs/work-packages/NPC-ACTOR-09B_REJECTION_OBSERVABILITY.md`;
4. `docs/npc_actor/NPC_ACTOR09B_REJECTION_OBSERVABILITY_REPORT.md`;
5. only directly required NPC-ACTOR-08／09A implementation evidence.

Do not rerun Qwen3-8B, call resolve, weaken a safety Gate, expose rejected content, connect a cloud API, start a
server／port, or change Canon／Memory／Knowledge without a new reviewed package and explicit Product Owner authority.

## Previous route — NPC-ACTOR-09A

## Current route — NPC-ACTOR-09A

Current package: `NPC-ACTOR-09A_SESSION_SETUP_REPAIR` is closing as a valid safe rejection.

The fresh Session correctly preserved different player and NPC locations. One Qwen3-8B prepare still failed
the bounded safety loop. Save the rejection and unchanged-state evidence, Push, and stop. Do not retry,
weaken the Gate, call resolve, or invent a candidate that the engine did not accept.

Read in order:

1. `PROJECT_STATE.md`;
2. `PLANS.md` Current Plan Window V19;
3. `docs/work-packages/NPC-ACTOR-09A_SESSION_SETUP_REPAIR.md`;
4. NPC-ACTOR-09 rejection report and only directly required NPC-ACTOR-08 implementation evidence.

Do not modify Production Kotlin, reuse either failed Session, write SQL directly, use a server／port or cloud
API, call resolve, or start rejection-observability work without a new reviewed package.

## Previous route — NPC-ACTOR-09

## Current route — NPC-ACTOR-09

Current package: `NPC-ACTOR-09_REAL_SUPERVISED_PILOT` is closing as a safe rejection.

The first real Qwen3-8B prepare was safely rejected because `start-session` moved the player into the
opening scene and invalidated the offscreen condition. Save the rejection evidence to GitHub. Do not
retry this Session, weaken the safety Gate, or call resolve. There is no candidate for Product Owner review.

Read in order:

1. `PROJECT_STATE.md`;
2. `PLANS.md` Current Plan Window V18;
3. `docs/work-packages/NPC-ACTOR-09_REAL_SUPERVISED_PILOT.md`;
4. NPC-ACTOR-08 formal resolve contract and only directly required implementation files.

Do not edit Production Kotlin, run SQL directly, start a server／port, connect cloud API, or call
`resolve-local-npc-actor-formal-turn`. A new reviewed package is required before correcting the fixture
and attempting another real candidate.

## Current route — NPC-ACTOR-08

Current package: `NPC-ACTOR-08_HUMAN_APPROVED_FORMAL_RESOLVE` is `TECHNICAL_PASS`.

The two-stage prepare／resolve path, exact human-decision binding and existing transaction reuse are
implemented and verified. The current stop is a real-candidate review Gate, not an implementation
failure. Do not generate or apply an unseen Qwen3-8B candidate without first presenting its package
and receiving a separate exact-bound decision file.

The Product Owner authorized building NPC-ACTOR-08. This is not approval for an unseen candidate.
Every real candidate must be written to a reviewable package and receive a separate exact-bound
human decision before entering the existing formal resolve pipeline.

```text
NPC-ACTOR-05 provides the bounded safety loop
→ Product Owner authorizes one local 8B model Pilot
→ direct llama.cpp CLI process, no server or port
→ strict JSON candidate enters existing validators
→ at most one sanitized repair attempt
→ local-model technical evidence, never an automatic formal-state write
```

GitHub remote is the team authority. Start from `PROJECT_STATE.md`, then read:

1. `PLANS.md` Current Plan Window V17;
2. `docs/work-packages/NPC-ACTOR-08_HUMAN_APPROVED_FORMAL_RESOLVE.md`;
3. `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`;
4. `docs/npc_actor/NPC_ACTOR05_IMPLEMENTATION_REPORT.md`;
5. only files required by NPC-ACTOR-08.

## NPC-ACTOR-08 mandatory rules

- keep prepare and resolve as separate commands;
- never interpret package authorization or ordinary chat consent as candidate approval;
- bind the human decision to candidate and formal Context hashes;
- reject stale, replayed, tampered or mismatched candidates;
- never compile model-authored arbitrary state deltas, Canon, Memory or Knowledge writes;
- call only the existing `MultiTurnRuntime.resolveTurn` path for an approved candidate;
- use test-only approvals for automated proof and label them as test evidence, not Product Owner review;
- do not run a real approved 8B candidate without presenting that candidate for separate review.

## Completed work

- NPC-ACTOR-08 adds separate prepare and resolve commands, candidate and decision Hash binding,
  stale／tamper／replay rejection and exact pending-context cleanup;
- test-only approval proves one complete non-Canon transaction through the existing validators and
  SQLite transaction; no real unseen model candidate was approved;
- focused 14／14, related 129／129, full 975／975 and JSON 4／4 pass;
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
