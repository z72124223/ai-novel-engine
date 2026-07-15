# AI Novel Engine — 現在狀態

> **這是唯一現況入口。** 新的 GPT、Codex、Reviewer 或其他 Agent 必須先讀本檔。舊 REBOOT、SCREEN-01、圖片 Work Package、HANDOFF、README 狀態與 `PLANS.md` 舊標題都是歷史資料，不能蓋過本檔。

## Current Override — NPC-ACTOR-09C Real Post-Safety Validation Diagnosis

- Status: `SAFE_PROGRAM_REJECTION / NO_CANDIDATE / NO_RESOLVE`.
- Branch: `codex/npc-actor09c-real-rejection-diagnosis`.
- Base SHA: `5f0d214a25520cbbe681a024398ac397d9d16963` from
  `origin/codex/npc-actor09b-rejection-observability`.
- Required plan: `PLANS.md` Current Plan Window V21.
- Required work package: `docs/work-packages/NPC-ACTOR-09C_REAL_REJECTION_DIAGNOSIS.md`.
- Evidence report: `docs/npc_actor/NPC_ACTOR09C_REAL_REJECTION_DIAGNOSIS_REPORT.md`.
- Result: one fresh offscreen non-Canon Session passed all setup Gates, then exactly one local Qwen3-8B prepare
  reached formal Runtime validation and was rejected with `low_character_fit` because the first character action
  had `characterFitScore < 70`.
- The local safety loop and Game Master had accepted the generated proposal before formal validation rejected it.
  No candidate, no NPC-ACTOR-09B safety sidecar and no formal turn were written.
- Existing failure cleanup removed the pending Context. Before／after official Session evidence is byte-identical:
  `stateVersion=0`, `nextTurnNumber=1`, player and NPC locations unchanged, pending Context 0.
- Verification: focused `9/9`, related NPC Actor `74/74`, JSON `5/5`, manifest Hash `4/4`, secret／path／ignored DB／
  Git scope and `git diff --check` PASS. No Production Kotlin, Test or Migration was modified.
- Current stop: do not retry Qwen3-8B and do not resolve. The smallest next package is a separately authorized
  `NPC-ACTOR-09D_POST_SAFETY_VALIDATION_OBSERVABILITY`, which may add a sanitized sidecar for this later failure
  stage without weakening `characterFitScore >= 70` or preserving model text.
- No cloud API, Provider, server, port, Canon, Memory／Knowledge authoring, media or next chapter.

## Previous Override — NPC-ACTOR-09B Safe Rejection Observability

- Status: `NPC_ACTOR09B_TECHNICAL_PASS / REAL_MODEL_RETRY_AUTHORIZATION_REQUIRED`.
- Branch: `codex/npc-actor09b-rejection-observability`.
- Base SHA: `fa7ed12f108d15ef0f7bb1ea089e39538fd839d2` from
  `origin/codex/npc-actor09a-session-setup-repair`.
- Required plan: `PLANS.md` Current Plan Window V20.
- Required work package: `docs/work-packages/NPC-ACTOR-09B_REJECTION_OBSERVABILITY.md`.
- Evidence report: `docs/npc_actor/NPC_ACTOR09B_REJECTION_OBSERVABILITY_REPORT.md`.
- Result: formal NPC candidate preparation now emits a sanitized rejection sidecar after exhausting the bounded
  safety loop. It records stage/code, invocation Hash/metadata and unchanged-state evidence only.
- Excluded by contract: prompt text, raw output, generated responses, actor-visible content, author secrets and
  engine-owned physical measurements.
- Rejection clears the pending Context, creates no candidate, returns a non-zero CLI status and applies no formal turn.
- Verification: focused `9/9`, related NPC checks PASS, full `976/976`, strict JSON, Schema, secret, path,
  server/port, Git scope and `git diff --check` PASS.
- Current stop: no real Qwen3-8B retry or resolve is authorized. A separate reviewed package must run exactly one
  supervised retry if the Product Owner wants a real rejection sidecar or candidate.
- No cloud API, Provider, server, port, Canon, Memory／Knowledge authoring, media or next chapter.

## Previous Override — NPC-ACTOR-09A Session Setup Repair

- Status: `SAFE_REJECTION / SETUP_REPAIR_PASS / NO_CANDIDATE_CREATED`.
- Branch: `codex/npc-actor09a-session-setup-repair`.
- Base SHA: `679ec9afbff3706ea76eccdd17e062203d6c1758`.
- Required plan: `PLANS.md` Current Plan Window V19.
- Required work package: `docs/work-packages/NPC-ACTOR-09A_SESSION_SETUP_REPAIR.md`.
- Evidence Commit: `710382b6f45b24b836fe8c0290b3173df58d01e9`.
- Remote branch: `origin/codex/npc-actor09a-session-setup-repair`.
- Result: fresh Session evidence proved the player and NPC were in different locations. One Qwen3-8B prepare
  then ended as `REJECTED_AFTER_MAX_ATTEMPTS`; no candidate package was created and no formal turn was resolved.
- Current stop: preserve and Push this valid rejection evidence. Do not retry until a separately reviewed
  package adds safe rejection observability or otherwise identifies the failed safety conditions.
- There is no candidate to approve. This authorization does not permit another attempt or
  `resolve-local-npc-actor-formal-turn`.
- No Production Kotlin, cloud API, Provider, server, port, Canon, Memory／Knowledge authoring or media.

## Previous Override — NPC-ACTOR-09 Safe Rejection Closure

- Status: `SAFE_REJECTION / SESSION_BOOTSTRAP_REPAIR_REQUIRED / NO_CANDIDATE_CREATED`.
- Branch: `codex/npc-actor09-real-supervised-pilot`.
- Base SHA: `54ed365514b228dc20cb3b3a48a21d774705249a`.
- Required plan: `PLANS.md` Current Plan Window V18.
- Required work package: `docs/work-packages/NPC-ACTOR-09_REAL_SUPERVISED_PILOT.md`.
- Result: Qwen3-8B prepare ended as `REJECTED_AFTER_MAX_ATTEMPTS`; no candidate package was created and no
  formal turn was resolved.
- Root cause: the opening scene had a `locationId`, so existing `start-session` behavior moved every project
  character, including the player, to the opening location. The offscreen NPC condition became false and the
  existing safety Gate correctly rejected the run.
- Current stop: preserve and Push the rejection evidence. Do not retry this Session. A separately reviewed
  setup-repair package is required before another real-model attempt.
- No Production Kotlin, cloud API, Provider, server, port, Canon, Memory／Knowledge authoring or media.

## Current Override — NPC-ACTOR-08 Human-Approved Formal Resolve

- Status: `NPC_ACTOR08_TECHNICAL_PASS / REAL_CANDIDATE_APPROVAL_REQUIRED`.
- Implementation branch: `codex/npc-actor08-human-approved-formal-resolve`.
- Implementation Commit: `f119f4eefa6cd4f8237bf68cf498eb7c3c8735e2`.
- Base SHA: `66d86467cfcdf759656d9e0d276ae8ff4008366c` from the remote default branch.
- Required plan: `PLANS.md` Current Plan Window V17.
- Required work package: `docs/work-packages/NPC-ACTOR-08_HUMAN_APPROVED_FORMAL_RESOLVE.md`.
- Product Owner authorization permits building and testing the reviewed non-Canon approval path.
- The authorization does not approve any unseen model candidate. Every real candidate still requires
  a separate decision file bound to candidate, Session, Context, turn, state-version and result Hashes.
- Mandatory path: local candidate → existing safety loop → engine-owned result compilation → exact
  human decision validation → existing `resolveTurn` validators and SQLite transaction.
- Verification: focused 14／14、related 129／129、full 975／975、JSON 4／4 and all Hash／scope／
  secret／path／server-port checks pass.
- Current stop: do not generate or apply a real Qwen3-8B candidate until that exact candidate package
  is presented for review and receives its own bound decision file.
- No cloud API, paid Provider, server, port, Canon promotion, Memory／Knowledge authoring, media or
  next-chapter work is authorized.

## Previous Override — NPC-ACTOR-07 Formal Runtime Shadow

- Status: `NPC_ACTOR07_TECHNICAL_PASS / NPC_ACTOR08_AUTHORIZATION_REQUIRED`.
- Current branch: `codex/npc-actor07-formal-runtime-shadow`.
- Base SHA: `d7064355cc44c55ade90843d62017d29ae98af22` from the remote default branch.
- Required plan: `PLANS.md` Current Plan Window V16.
- Required work package: `docs/work-packages/NPC-ACTOR-07_FORMAL_RUNTIME_SHADOW.md`.
- Authorized scope: connect the verified local Qwen3-8B actor to a real formal Session through
  read-only `previewTurnContext`, actor-visible filtering and the existing safety loop.
- Mandatory boundary: no pending context, no `resolve-turn`, no SQLite / Canon / Memory /
  Knowledge / formal physical-state write.
- Implemented evidence: `docs/npc_actor/NPC_ACTOR07_FORMAL_RUNTIME_SHADOW_REPORT.md` and
  `samples/npc_actor07/npc_actor07_evidence_manifest.json`.
- Real 8B Pilot: safely rejected in the demo Session because the player and NPC share a location;
  database SHA-256, Session version, turn logs, events and physical state all remained unchanged.
- Verification: focused `6/6`, related `82/82`, full `967/967`, JSON / Hash / secret / path /
  server-port / Git scope checks all PASS.
- Current step: stop. Do not implement human-approved formal execution until NPC-ACTOR-08 receives
  a new explicit Product Owner authorization.
- True stop: any required formal mutation, Validator weakening, private-data exposure, paid API,
  server, port or Canon decision.

## Previous Override — NPC-ACTOR-06 Local Model Pilot

- Status: `NPC_ACTOR06_LOCAL_MODEL_TECHNICAL_PASS / FORMAL_RUNTIME_AUTOMATION_NOT_AUTHORIZED`.
- Completed work: 已在 GTX 1080 Ti 上安裝並驗證 Qwen3-8B Q5_K_M，以直接 `llama.cpp` CLI
  程序接入 provider-neutral NPC Actor 候選流程，並通過 NPC-ACTOR-05 安全閉環。
- Implementation branch: `codex/npc-actor06-local-model-pilot`.
- Required work package: `docs/work-packages/NPC-ACTOR-06_LOCAL_MODEL_PILOT.md`.
- Required plan: `PLANS.md` Current Plan Window V15.
- Base SHA: `9b7851736c0a80d59e2b407e12ff50777d9ed264` from the remote default branch.
- Authorization: Product Owner 已明確授權安裝建議的本機 7B／8B 文本模型，並按需求調用
  本機既有模型。
- Verified model: official `Qwen/Qwen3-8B-GGUF`, `Qwen3-8B-Q5_K_M.gguf`，SHA-256
  `068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738`，Vulkan GPU 載入通過。
- Runtime boundary: 只允許直接 `llama-cli` 程序；不得啟動 server、port 或雲端 API。
- Trust boundary: 模型只產生 actor-visible JSON 候選；既有角色、知識、物理、世界合法性與
  時間預算驗證不變，模型不得寫 SQLite、Canon、Memory 或 Knowledge。
- Real Pilot: Lian 第一次產生的候選通過；Yan 第一次失敗後只取得已清理修訂單，第二次仍錯後
  `REJECTED_AFTER_MAX_ATTEMPTS`；兩案 `formalStateWritePerformed=false`。
- Verification: focused 8/8、NPC Actor／Physics related 84/84、full 961/961 PASS；JSON、Schema、
  Hash、秘密、絕對路徑、零 port 與 Git scope 均完成檢查。
- Evidence: `docs/npc_actor/NPC_ACTOR06_LOCAL_MODEL_PILOT_REPORT.md` 與
  `samples/npc_actor06/npc_actor06_evidence_hashes.json`。
- Current step: 工作包完成並停在正式 Runtime 自動調用授權 Gate，不自行開始下一包。
- True stop: 正式 Runtime 自動推進、付費／雲端 Provider、弱化 Validator／Transaction、
  Canon／角色永久命運決策，或必須進行破壞性 Git／資料操作。

## Previous Override — NPC-ACTOR-05 Offline NPC Action Safety Loop

- Status: `NPC_ACTOR05_COMPLETE / TECHNICAL_PASS / MODEL_INTEGRATION_AUTHORIZATION_REQUIRED`.
- Current work: NPC 行動提案現在會先經過一個有上限、可重現、離線的安全閉環，通過後也只會成為正式 `resolve-turn` 的候選，不會直接寫入世界狀態。
- Implementation branch: `codex/npc-actor05-offline-safety-loop`.
- Required work package: `docs/work-packages/NPC-ACTOR-05_OFFLINE_SAFETY_LOOP.md`.
- Required plan: `PLANS.md` Current Plan Window V14.
- Base SHA: `e3f75fa3d6c93e080cbd9501d450c5931e245368` from the remote default branch.
- Implementation Commit: `d21b1a2eb0fa669ad905028c4b65b2acd366b133`; the feature branch and remote default received it by ordinary fast-forward Push without force.
- Pipeline: Actor Contract → Physical Binding → Physical Affordance → Game Master world legality → 10-minute time budget.
- Retry rule: 每個提案最多兩次；第一次失敗只會產生經過清理的修訂要求，第二次仍失敗就終止。
- Privacy rule: 修訂要求只暴露角色可見的 ID、允許清單、錯誤碼與固定修訂指示，不暴露引擎內部測量值或作者秘密。
- Fixed evidence: Mira 首次通過；Lian 修訂後通過；Ren 修訂後通過；Yan 暴風城門兩次都被拒絕。
- Verification: focused 9/9、NPC Actor／Physics related 43/43、full 953/953 PASS；JSON、Schema、Hash、秘密與 Git scope 另有固定證據。
- Evidence report: `docs/npc_actor/NPC_ACTOR05_IMPLEMENTATION_REPORT.md`.
- Frozen suite: `samples/npc_actor05/npc_actor05_safety_loop_suite_v1.json`.
- Frozen result: `samples/npc_actor05/npc_actor05_safety_loop_report_v1.json`.
- Formal state: `formalStateWritePerformed=false`; 沒有 SQLite、Migration、Canon、Memory、Knowledge 或正式 Runtime 狀態寫入。
- Known limit: 繩索張力、摩擦係數、濕滑坡面與更細緻的多物體動力學仍未建模。
- True stop: 任何 live model、API、Provider 或自動 NPC 生成整合都需要新的計畫與 Product Owner 明確授權。
- No API、Provider、模型下載、圖片、影片、音訊、Canon 或下一章工作已執行。
- Final governance Commit: the Git commit containing this state update; it changes documentation only.

## Previous Override — NPC-ACTOR-04A Internal Review Closure

- Status: `NPC_ACTOR04A_COMPLETE / INTERNAL_REVIEW_COMPLETE / PRODUCT_OWNER_A_B_NOT_REQUIRED`.
- Current work: NPC-ACTOR-04 的重複使用者 A/B Gate 已由三個內部獨立評審封存，現在停止，不自動啟動下一包。
- Implementation branch: `codex/npc-actor04-internal-review-closure`.
- Required work package: `docs/work-packages/NPC-ACTOR-04A_INTERNAL_REVIEW_CLOSURE.md`.
- Required plan: `PLANS.md` Current Plan Window V13.
- Base SHA: `dc4ff06670eafbf9c1ff4661935491a8bffa451f` from the remote default branch.
- Implementation Commit: `cb9063d` (`test: close npc actor embodied internal review`).
- Product decision: Product Owner 拒絕再替 Agent 重複完成 A/B，Codex 必須自行完成可自動化的評審與測試。
- Review method: 三位互相獨立的內部評審先讀匿名包；三份結果完成、驗證與 Hash 後，彙整器才讀封存答案。
- Result: Actor 2、Control 1、`BOTH_FAIL` 1、TIE 0、無共識 0；overall decision 為 `INTERNAL_REVIEW_COMPLETE`。
- Known weakness: Yan 暴風城門的兩個候選都存在受力、繩索、濕滑坡面或跨位置操作缺陷，不能宣稱演員版全面通過。
- Verification: focused 4/4、NPC Actor related 42/42、full 944/944、Schema 4/4、frozen evidence 6/6 PASS。
- Evidence report: `docs/npc_actor/NPC_ACTOR04A_INTERNAL_REVIEW_CLOSURE_REPORT.md`.
- Aggregate: `samples/npc_actor04/internal_review/npc_actor04_internal_review_aggregate.json`.
- True stop: 使用者不需要再完成 NPC-ACTOR-04 A/B；任何 live model、API、Provider、production、Canon 或下一包都需要新的計畫與明確授權。
- No API、Provider、模型下載、圖片、影片、音訊、Canon、Migration、SQLite 或正式 Runtime 變更已執行。

## Previous Override — WORLD-PHYSICS-05 Embodied Scene Pilot

- Status: `WORLD_PHYSICS05_COMPLETE / WORLD_PHYSICS05_EMBODIED_SCENE_PILOT_PASS`.
- Current work: WORLD-PHYSICS-05 is complete and integrated; the project is at a true stop.
- Implementation branch: `codex/world-physics05-embodied-scene-pilot`.
- Required blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`.
- Required work package: `docs/work-packages/WORLD-PHYSICS-05_EMBODIED_SCENE_PILOT.md`.
- Required plan: `PLANS.md` Current Plan Window V12.
- Base SHA: `7ec8d2b42a6d1024db91e5a04d3ee0438ff354f1` from the remote default branch.
- Authorization: Product Owner replied `開始` on 2026-07-14 and asked Codex to complete the reviewed non-paid route without intermediate questions.
- Story source: SCREEN-01 〈潮線之外〉 `scene-03`, still `NON_CANON_PILOT`.
- Physical overlay: `NON_CANON_EVALUATION`; no metric value becomes Chenxing Canon.
- Mandatory regression: adult support on Aijiang's shoulder is rejected without world-state mutation.
- Mandatory continuity: a held pry bar follows its holder through a passage, then supports placement and safe release across later turns.
- Runtime result: 5 resolved turns, 1 rejected dog-support attempt and 5 persisted physical transitions; every declared check passed.
- Bug fixed: held/supported/contained dependents now follow their moved root entity instead of remaining at stale coordinates.
- Verification: focused 2/2, related 42/42, full 940/940, JSON 3/3 and SHA-256 4/4 PASS.
- Implementation Commit: `56d525289f59e09543fdf450bd00f343daf1ef98`; feature and remote default integration completed by ordinary Push without force.
- Evidence report: `docs/world_physics/WORLD_PHYSICS05_IMPLEMENTATION_REPORT.md`.
- True stop: a new measured failure, reviewed plan and explicit Product Owner authorization are required before optional richer physics or any other package.
- No API, Provider, model download, image, video, audio, Canon or next-chapter work is authorized.

## Previous Override — WORLD-PHYSICS-04 Physical State Persistence

- Status: `WORLD_PHYSICS04_COMPLETE / WORLD_PHYSICS04_PHYSICAL_STATE_PERSISTENCE_PASS`
- Current work: WORLD-PHYSICS-04 is complete; no later physical, API, Provider or media package is authorized automatically.
- Implementation branch: `codex/world-physics04-physical-state-persistence`
- Required blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`
- Required work package: `docs/work-packages/WORLD-PHYSICS-04_PHYSICAL_STATE_PERSISTENCE.md`
- Required plan: `PLANS.md` Current Plan Window V11.
- Base SHA: `931c7d6463a71fe8e8a0706f8ca8c7902ea620c9` from the remote default branch.
- Authorization: Product Owner replied `授權` at the WORLD-PHYSICS-03 persistence gate on 2026-07-14.
- Boundary: runtime physical state remains `FORMAL_TURN_RUNTIME_NON_CANON`; no measurement becomes Canon.
- Runtime result: V2 `LIFT`, `PUSH`, `PLACE_ON`, `DROP`, `ENTER_CONTAINER` and `PASS_THROUGH` persist through Migration 016 inside the existing formal turn transaction.
- Compatibility: V1 transient support/reach remains valid; V1 stateful input and stale V2 version/hash input fail closed.
- Verification: focused 7/7, related 33/33, rebuild 3/3, full 938/938, JSON 7/7 and SHA-256 6/6 PASS.
- Implementation Commit: `54d7a49e30a3b28dc4f8f2f16c8d82a832f07426`; feature and remote default integration completed by ordinary Push without force.
- Evidence report: `docs/world_physics/WORLD_PHYSICS04_IMPLEMENTATION_REPORT.md`.
- True stop: a new reviewed plan and explicit Product Owner authorization are required before any later physical package.
- No API, Provider, model download, image, video or audio is authorized.

## Previous Override — WORLD-PHYSICS-03 Formal Turn Physical Gate

- Status: `WORLD_PHYSICS03_COMPLETE / FORMAL_TURN_PHYSICAL_GATE_PASS_STATEFUL_ACTIONS_BLOCKED`
- Current work: WORLD-PHYSICS-03 is complete; no later physical persistence package is authorized automatically.
- Implementation branch: `codex/world-physics03-formal-turn-gate`
- Required blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`
- Required work package: `docs/work-packages/WORLD-PHYSICS-03_FORMAL_TURN_GATE.md`
- Required plan: `PLANS.md` Current Plan Window V10.
- Base SHA: `3aace566e133fd89a50167d350c780c7ec95c996` from the remote default branch.
- Persistence decision: no Migration, Repository or `StateReducer` change; stateful physical actions remain fail-closed until a reviewed physical relation state exists.
- Scope boundary: only structured proposals are checked; undeclared physical prose is not inferred automatically.
- Verification: evidence 8/8, focused 10/10, related 57/57, full 931/931, JSON 5/5 and SHA-256 4/4 PASS.
- Implementation Commit: `bb05fd99c95b752278a000f1028014414ae755b9`; feature and remote default branch push completed by ordinary fast-forward.
- Evidence report: `docs/world_physics/WORLD_PHYSICS03_IMPLEMENTATION_REPORT.md`.
- No API, Provider, model download, Canon measurement, image, video or audio is authorized.

## Previous Override — WORLD-PHYSICS-02 NPC Action Binding

- Status: `WORLD_PHYSICS02_TECHNICAL_PASS / FORMAL_TURN_RUNTIME_NOT_CONNECTED`
- Current work: `WORLD-PHYSICS-02 complete — WORLD-PHYSICS-03 not authorized.`
- Implementation branch: `codex/world-physics02-npc-action-binding`
- Required blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`
- Required work package: `docs/work-packages/WORLD-PHYSICS-02_NPC_ACTION_BINDING.md`
- Required plan: `PLANS.md` Current Plan Window V9.
- Base SHA: `f67d48a78e5943e477de3e855778cfc45a915a38` from the remote default branch.
- Compatibility rule: existing `NpcActorRequest/Response V1`, Schemas and sealed NPC-ACTOR evidence remain unchanged; physical actions use a separate strict binding envelope.
- Trust boundary: model-visible data lists only Actor-visible context and allowed action/target identifiers. Metric archetypes, geometry, load and barriers are engine-owned validation evidence and may not be authored by the model.
- Acceptance rule: both `NpcActorResponseValidator` and `PhysicalAffordanceValidator` must pass before an `NpcActionProposal` may reach Game Master review.
- Verification: 8/8 binding cases, new focused 9/9, related focused 23/23, full 921/921, strict JSON 4/4, Schema contract and SHA-256 3/3 PASS.
- Mandatory regression: adult use of a medium dog's shoulder is rejected; stable chair support is accepted only as a Game Master review candidate.
- No API, Provider, model download, formal Canon data, migration, SQLite write, image, video or audio is authorized.
- True stops: invented Canon measurements, formal transaction changes, paid/live execution, force push or weakening knowledge/hash/approval boundaries.
- Next candidate: `WORLD-PHYSICS-03` formal turn integration, but it requires a new reviewed plan and is not authorized automatically.
- Implementation Commit: `96451e4c4f60027d403c5aab11252f26d3215df2`; feature branch push completed.

## Previous Override — WORLD-PHYSICS-01 Physical Affordance Foundation

- Status: `WORLD_PHYSICS01_TECHNICAL_PASS_RUNTIME_NOT_CONNECTED / NPC_ACTOR04_REVIEW_DEFERRED_BY_PHYSICAL_GROUNDING_LIMITATION`
- Current work: `WORLD-PHYSICS-01 complete — WORLD-PHYSICS-02 not authorized`
- Implementation branch: `codex/world-physics01-physical-affordance`
- Required blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`
- Required work package: `docs/work-packages/WORLD-PHYSICS-01_PHYSICAL_AFFORDANCE_FOUNDATION.md`
- Required plan: `PLANS.md` Current Plan Window V8.
- Base SHA: `3b86e967891c48ea52789daf25698b8ca611e3b0` from the remote default branch.
- Product trigger: the existing text scene checks cannot reject morphology errors such as an adult using a medium dog's shoulder as a load-bearing aid while standing up.
- First implementation is deterministic and offline: metric 3D positions, body/object profiles, support, reach, barriers, load, containers, openings and gravity-derived effects.
- This package creates a reusable validation core and adversarial evidence only. It does not yet wire physical data into formal Setting Packs, `AiTurnResultValidator`, `StateReducer` or SQLite.
- Verification: 64/64 expected case outcomes, focused 7/7, full 912/912, JSON Schema contract 3/3 and SHA-256 3/3 PASS.
- Data reuse: 20 physical archetypes are stored once in the suite registry; 18 snapshots contain only instance state and barriers. The generated suite shrank from 592,691 to 255,294 bytes during implementation without reducing case coverage.
- NPC-ACTOR-03/04 packets and sealed answer keys remain preserved and unchanged. No human result is inferred.
- No API, model download, Provider, Canon promotion, media generation, migration or formal state write is authorized.
- True stops: any proposal to invent Canon physical values, change formal transaction boundaries, use a paid/live model, or claim full real-world simulation.
- Next candidate after Git integration: `WORLD-PHYSICS-02`, but it is not authorized automatically.
- Implementation Commit: `0a10024bce817a32d989f4203d6ef17e7ee320e1`; feature branch push completed.
- Current true stop: a new reviewed plan is required before binding physical archetypes to formal world data or NPC/turn Runtime.

## Previous Override — RETRIEVAL-00 / NPC Actor Route

- Status: `RETRIEVAL00_PASS / RETRIEVAL01_SOURCE_LEVEL_ACCEPTANCE_PASS / NPC_ACTOR03_REVIEW_DESIGN_LIMITATION / NPC_ACTOR04_TECHNICAL_PASS_HUMAN_REVIEW_PENDING`
- Current work: `NPC-ACTOR-04 — Product Owner Full-Trajectory Review Gate`
- Implementation branch: `codex/npc-actor04-embodied-scene-rollout`
- Required blueprint: `docs/LOCAL_HYBRID_RETRIEVAL_KNOWLEDGE_BLUEPRINT_V1.md`
- Required work package: `docs/work-packages/NPC-ACTOR-04_EMBODIED_SCENE_ROLLOUT_EVALUATION.md`
- NPC route blueprint: `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`
- NPC pilot package: `docs/work-packages/NPC-ACTOR-01_SINGLE_NPC_ACTOR_PILOT.md`
- Current work package: `docs/work-packages/NPC-ACTOR-04_EMBODIED_SCENE_ROLLOUT_EVALUATION.md`
- Order: `RETRIEVAL-00 → RETRIEVAL-01 → NPC-ACTOR-01 → NPC-ACTOR-02 → NPC-ACTOR-03 → NPC-ACTOR-04`; gates may not be skipped.
- User explicitly authorized blueprint implementation on 2026-07-13.
- Codex implements/tests/reviews; ChatGPT is manual audition; an optional GPT API adapter is the future production NPC actor surface.
- No live API call, API Key, paid Provider, Canon promotion, image/video/audio generation or state-write bypass is authorized by this activation.
- RETRIEVAL-00 implementation evidence: 21/21 inputs verified; 15 migrations; 5 Setting Packs; Canon PASS; 9 documents / 255 chunks; logical SHA-256 `30fcd67770e4977548d1eaa7777baca0518bd09085fb6397d7fd93fdb3056c32`; focused 3/3 and full 875/875 PASS.
- Rebuild runbook: `docs/PROJECT_DATA_REBUILD_RUNBOOK_V1.md`.
- Validation evidence: `docs/retrieval/RETRIEVAL00_IMPLEMENTATION_REPORT.md` and `docs/retrieval/RETRIEVAL00_VALIDATION_SUMMARY.json`.
- RETRIEVAL-00 implementation Commit `82ed30bf94b69902e92737fdbdb32074db565840` is present on the remote default branch.
- The user explicitly authorized the bounded RETRIEVAL-01 model download, isolated runtime and GTX 1080 Ti execution on 2026-07-13.
- RETRIEVAL-01 branch: `codex/retrieval01-offline-dual-model-benchmark` from default Commit `0922bb2ce1599101c2726431f8b81b1259abc56d`.
- RETRIEVAL-01 work package: `docs/work-packages/RETRIEVAL-01_OFFLINE_LOCAL_DUAL_MODEL_BENCHMARK.md`.
- Supply-chain evidence: `docs/retrieval/RETRIEVAL01_SUPPLY_CHAIN_REVIEW.md` and `docs/retrieval/RETRIEVAL01_MODEL_ENVIRONMENT_LOCK.json`; pinned official model/tokenizer hashes were verified and all binaries remain outside Git.
- RETRIEVAL-01 baseline evidence: `docs/retrieval/RETRIEVAL01_EXACT_BASELINE_REPORT.md` and `docs/retrieval/RETRIEVAL01_BASELINE_SUMMARY.json`.
- Baseline result: 50 queries; 38 retrievable; LIKE top-8 10/38; lexical top-8 38/38; 6 knowledge-boundary blocks; 6 code/docs corpus gaps; 0 visibility leaks; full test suite 876/876 PASS.
- RETRIEVAL-01 final evidence: `docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_REPORT.md`, `docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_SUMMARY.json` and `docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_FULL.json`.
- Final source-document-level result: 38/38 Embedding top-50, 38/38 final Reranker top-8, semantic 10/10 versus LIKE 0/10, exact 10/10 with 0 regression, 0 visibility leaks and 1,821 MiB observed minimum system-free VRAM.
- Label limitation: expected relevance is currently source-document-level, not precise answer-bearing chunk-level. Do not claim 100% chunk precision or enable an actor-visible production path from this benchmark alone.
- RETRIEVAL-01 used no cloud/API/Provider, port, vector database, Canon write or runtime state write. The external model runtime is not part of Gradle or the shipped Kotlin runtime.
- On 2026-07-13 the user explicitly activated a long-running continuation from RETRIEVAL-01; this authorizes the bounded NPC-ACTOR-01 offline Fake/File Pilot.
- NPC-ACTOR-01 technical evidence: 20/20 actor outputs accepted for Game Master review; 0 knowledge leaks; 0 continuity violations; AI exposition patterns 26→0; average utterance length 37.4→13.25 characters; focused 15/15 and full 886/886 tests PASS.
- NPC-ACTOR-01 evidence: `docs/npc_actor/NPC_ACTOR01_IMPLEMENTATION_REPORT.md`, `samples/npc_actor01/npc_actor01_evaluation_report.json`, anonymous blind packet, separate answer key and `samples/npc_actor01/npc_actor01_human_blind_review_result.json`.
- NPC-ACTOR-01 implementation Commit: `b53a2ce69a80f7917f0cddd77ba3746b5619d7d5`; present on the feature branch and remote default branch.
- Product Owner completed all 20 blind ratings before answer-key disclosure: Actor preferred 20／20, baseline 0, ties 0, both-fail 0, knowledge/safety flags 0.
- Evaluation limitation: the baseline repeatedly used obvious AI exposition, so this supports only removal of gross AI phrasing. It does not prove general human likeness, ChatGPT-window naturalness or multi-turn character stability.
- The next evidence-only task is a harder randomized multi-turn blind test where both options are concise and plausible. This does not authorize a live model call.
- NPC-ACTOR-02 technical evidence is ready: 12 unseen non-Canon scenes, 4 recurring profiles, 4 turns per option path, deterministic A/B 6/6, 0 turn/strawman/secret/length violations and maximum spoken-length ratio 1.2174.
- NPC-ACTOR-02 evidence: `docs/npc_actor/NPC_ACTOR02_HARD_BLIND_IMPLEMENTATION_REPORT.md`, `samples/npc_actor02/npc_actor02_hard_blind_review_packet_v1.json`, separate answer key, strict Schema and SHA-256 manifest.
- NPC-ACTOR-02 human result: Actor 4, natural control 6, TIE 2, BOTH_FAIL 0, knowledge/safety concerns 0; decision `NO_ACTOR_ADVANTAGE_OBSERVED`.
- The result does not justify live API or production rollout. Profile-level variation is real, but the cause is unknown because no qualitative issue tags were supplied.
- On 2026-07-13 the user explicitly requested continuation after completing NPC-ACTOR-02; this authorizes one bounded offline calibration revision under NPC-ACTOR-03 only.
- NPC-ACTOR-03 technical evidence is ready: one evaluation-only revision, 8 new holdout scenes, four profiles exactly twice each, NPC-ACTOR-02 id/content overlap 0, deterministic A/B 4/4, maximum spoken-length ratio 1.25 and 0 structure/strawman/secret/length violations.
- NPC-ACTOR-03 verification: focused 6/6 and full 898/898 PASS; JSON 7/7, SHA-256 6/6, anonymity, secret, private-path, network and Git-scope checks PASS.
- Before rating NPC-ACTOR-03, the Product Owner identified that point-to-point dialogue did not contain enough persistent environment to judge a three-dimensional world. The packet remains preserved and its answer key sealed; no human result is inferred.
- On 2026-07-13 the user explicitly authorized implementation of NPC-ACTOR-04: four complete embodied scene trajectories with spatial, object, multi-agent, time, perception, action and consequence state.
- NPC-ACTOR-04 technical evidence is complete: 4 new scenes, 4 profiles, 12 beats, A/B 2/2, all structural violation counts 0, focused 7/7 and full 905/905 PASS.
- Current next step: Product Owner reviews all four complete anonymous trajectories before the sealed answer may be opened. No review result may be inferred.
- Current true stops: any live API/API Key/paid Provider/privacy export, production rollout, Canon decision, or weakening of visibility, transaction, hash and validator boundaries.
- RETRIEVAL-01 pre-model implementation Commit `e01abc792dfe9444ba65dc8cfaa3cae08d536fd8` is present on the remote default branch.

## Completed Baseline — ENGINE-RULES-01 / DIALOGUE-02

- Status: `ENGINE_RULES01_COMPLETE_MERGED`
- Current work: `ENGINE_RULES01_DIALOGUE02`
- Collaboration: `GPT_PLANS_CODEX_IMPLEMENTS`
- Implementation branch: `codex/engine-rules01-dialogue02`
- Goal: `ENGINE_ENFORCED_PRODUCTION_RULES`
- Key rule: bad drafts may exist, but any BLOCKER prevents final selection.
- Mandatory negative fixture: `好。先碰你。`
- Required gate: `docs/CURRENT_ROUTE_GATE_V7.md`
- Required charter: `docs/ENGINE_ENFORCED_PRODUCTION_RULES_V1.md`
- Required work package: `docs/work-packages/ENGINE-RULES-01_DIALOGUE-02.md`
- DIALOGUE-01 is historical evidence and must be re-evaluated, not silently trusted.
- Rule Set: `engine-production-rules@1.0.0`
- Initial Artifact: `NOT_SELECTABLE`, SHA-256 `805cb62cd19ab42ca10df6b98cdde315fb6580a6d098ded7e235cd178f28a0a9`, blockers `9`.
- Revised Artifact: `SELECTED`, SHA-256 `0d7c137a5180d87f32f88d8a42b9475ec8a6a4e12b185ca1abedbebd0b69c0f2`, blockers `0`, warnings `1`.
- Verification: focused PASS; full suite `872/872` PASS; High review `PASS_WITH_RISKS`, release blockers `NONE`.
- Completion classification: `ENGINE_RULES01_PASS_WITH_SEMANTIC_WARNINGS`.
- Implementation commits: `31f7072da143559c6a1280b3631ea2f70cad404d`, `8064d3746c1a8454bf1d0ba432537249a7ed124e`.
- Remote default branch contains the complete implementation and evidence through `8064d3746c1a8454bf1d0ba432537249a7ed124e`; the final governance-only commit is the Git commit containing this state update.
- The next active work package is RETRIEVAL-00. Do not start images, video, media Provider, Canon or next chapter.
- GitHub remote is the highest team authority.
- Do not start image, video, Provider, Canon, next chapter or interactive game.

## Active Blueprint Route — Local Hybrid Retrieval And NPC Actor

- Blueprint: `docs/LOCAL_HYBRID_RETRIEVAL_KNOWLEDGE_BLUEPRINT_V1.md`
- Status: `RETRIEVAL00_PASS / RETRIEVAL01_SOURCE_LEVEL_PASS / NPC_ACTOR04A_INTERNAL_REVIEW_COMPLETE / NPC_ACTOR05_TECHNICAL_PASS`
- Completed foundation: `RETRIEVAL-00 — Authority, Rebuild And Retention Foundation`
- Completed benchmark: `RETRIEVAL-01 — Offline Local Dual-Model Retrieval Benchmark`
- Current closure package: `docs/work-packages/NPC-ACTOR-05_OFFLINE_SAFETY_LOOP.md`
- `NPC-ACTOR-01 — Single NPC Actor Pilot` has completed provider-neutral Fake/File implementation and the first human blind review. The 20／20 preference is accepted only as a basic anti-exposition signal because the comparison was too easy.
- `NPC-ACTOR-02` human review is complete: Actor 4, natural control 6 and 2 ties. No Actor advantage was observed, so live API remains blocked.
- `NPC-ACTOR-03` revision 1 and its eight-item micro-dialogue packet remain technically valid, but Product Owner review is deferred because the evaluation lacks persistent environmental depth. Its answer key remains sealed.
- `NPC-ACTOR-04` evaluates the frozen revision through complete embodied scene trajectories. NPC-ACTOR-04A has now closed the repeated Product Owner rating request with three internal independent reviews: Actor 2, Control 1 and BOTH_FAIL 1. This is internal engineering evidence only, not human preference, live-model proof or production activation.
- `NPC-ACTOR-05` composes the existing actor, physical and Game Master validators into a maximum-two-attempt offline safety loop. Four fixed cases match expectation; accepted output remains only a formal-resolve candidate, and live-model integration still requires separate authorization.
- Intended route: deterministic import and optional local extraction → SQL/FTS/BM25 exact retrieval → Embedding candidate recall → Reranker ordering → a small visibility-safe evidence packet for downstream GPT use.
- Formal SQLite state, Canon, `AiTurnResultValidator`, `StateReducer`, character knowledge visibility, `contextHash`, `stateVersion`, and transaction boundaries remain authoritative and unchanged.
- Two pinned official 0.6B Qwen retrieval models were downloaded and benchmarked only in an isolated external runtime. They are not promoted into the Kotlin runtime or an actor-visible production path.
- No extraction model, GBrain component, MCP service, vector database, provider, or live API has been activated. Live GPT API calls remain separately blocked by API Key, cost and privacy approval.

## 1. 最終產品

本專案的主要終點是：

> **可長期連載的動畫／影片故事。**

不是單純小說生成器，也不是目前優先做互動遊戲。

正式生產路線：

```text
世界觀與角色資料
→ 故事事件主檔
→ 影視劇本
→ 場景與正式對白
→ 分鏡與鏡頭表
→ 角色／場景固定資產
→ 關鍵影格
→ 單鏡影片
→ 剪輯、聲音、字幕
→ 完整影片
```

小說可以是故事測試、出版版本或補充內容，但不是最高生產權威。

## 2. GitHub 是團隊最高工作場所

GitHub 遠端 Repository 是多人團隊協作、版本、交接、批准紀錄與長期記憶的最高權威。

以下成果不能只留在本機：

- 世界資料；
- 故事主檔；
- 小說試稿；
- 影視劇本；
- 對白軌；
- 場景表；
- 分鏡／鏡頭表；
- 評審；
- 作者決策；
- 圖片／影片交接包；
- Hash；
- 測試與開發日誌。

正式交接必須包含：

```text
Repository
Branch
Commit SHA
Repo-relative Path
Content SHA-256（需要時）
```

私人絕對路徑、未 Push 工作樹和「我本機最新版」不是正式交接。

## 3. SCREEN-01 已完成

SCREEN-01 已完成並納入目前預設分支。

Selected screenplay：

```text
samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md
```

SHA-256：

```text
4b2ac13f84541dfa904096fe650ed99a9f8d58e1519ba9f0b0816d85e56a554c
```

Selected dialogue：

```text
samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json
```

SHA-256：

```text
6936ec2099f5a61ab07db631e9d19ef2bb49a8154f572fb5f360e0f7fdcec65c
```

Boundary：

```text
NON_CANON_PILOT
```

SCREEN-01 的四場、三十一鏡、82 秒 Pilot 與歷史版本不得被 DIALOGUE-01 覆寫。

## 4. 現在唯一工作包

```text
DIALOGUE-01 V2 — 華語真人對白定稿
```

狀態：

```text
DIALOGUE01_COMPLETE_MERGED
```

Codex 實作分支：

```text
codex/dialogue01-human-voice-finalization
```

Codex 開始前必須：

1. `git fetch --prune origin`
2. 從最新遠端預設分支建立乾淨 Worktree／Branch
3. 記錄 Base SHA
4. 完整執行 DIALOGUE-01 V2
5. 所有成果 Commit／Push
6. 測試通過後安全合併預設分支
7. 一次最終回報

## 5. 必讀順序

1. `PROJECT_STATE.md`
2. `.codex/dialogue01_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V6.md`
4. `docs/GPT_CODEX_COLLABORATION_V2.md`
5. `docs/work-packages/DIALOGUE-01_HUMAN_CHINESE_DIALOGUE_FINALIZATION_V2.md`
6. `docs/handoffs/DIALOGUE01_CODEX_ONLY_HANDOFF.md`
7. `docs/handoffs/DIALOGUE01_MERGE_PLAN_V2.md`
8. `samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md`
9. `samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json`
10. `samples/screen01/screen01_dialogue_bible_zh-Hant-TW.json`
11. `samples/screen01/screen01_shot_list.json`
12. `samples/screen01/screen01_video_pilot_60_90s.json`
13. `samples/screen01/screen01_video_pilot_zh-Hant-TW.srt`
14. 只有 DIALOGUE-01 真正需要的程式、測試和資料

## 6. 對白方向

母稿：

```text
繁體中文 / zh-Hant-TW
```

陸塵行口語基礎：

```text
自然台灣華語
```

同時讓中國大陸、香港、馬來西亞、新加坡及其他華語讀者容易理解。

禁止：

- 兩個 AI 輪流講完整論文；
- 每句都說清楚價值觀；
- 每句都像金句；
- 每個角色同一語氣；
- 危急時仍講很長的工整句；
- 過多「不是 A，而是 B」；
- 在同一母稿亂混各地詞彙。

優先：

- 短句；
- 打斷；
- 停頓；
- 未完整回答；
- 誤會；
- 沉默；
- 動作代替解釋；
- 每個角色有不同語氣。

## 7. 主角方向

陸塵行可以聰明、可靠、敏銳。

不需要為了製造戲劇而故意變笨或犯低級錯誤。

他的主要成長線是進入新世界後：

- 適應文化與生活方式；
- 理解制度、階級、人情與政治；
- 理解力量帶來的責任；
- 從保護自己和愛醬，走向承擔更多人的命運；
- 最後能影響戰局與世界。

衝突應來自資訊不完整、價值兩難、對手適應、正確決定的代價，以及新世界與地球經驗的差異，不靠角色集體降智。

## 8. 模型

一般工程與整合：

```text
gpt-5.6-sol / medium
```

對白初次修改與唯一修訂，可在限定範圍使用：

```text
gpt-5.6-sol / high
```

三個獨立評審：

```text
gpt-5.6-sol / medium
```

搜尋與測試分析使用 Terra Low／Medium；Hash、JSON、字幕時間、Diff 和統計使用程式。

Max／Ultra 禁止。

## 9. 現在禁止

DIALOGUE-01 期間不得：

- 使用獨立 WORK；
- 覆寫 SCREEN-01 歷史版本；
- 改主要劇情；
- 讓主角故意變笨；
- 第二次自動修訂；
- 寫下一章；
- 生成圖片；
- 生成影片；
- 呼叫或購買 Provider；
- 升格 Canon；
- 決定永久死亡或戀愛結果；
- 新增互動 RPG 功能；
- 公開部署網站；
- 把必要成果只放在本機；
- force push。

## 10. 真正停止條件

只有以下情況 Codex 才停下詢問：

- 必須決定正式 Canon；
- 必須決定永久角色命運或戀愛；
- 必須決定正式美術；
- 必須花錢、使用 Provider 或 API Key；
- 無法避免破壞性 Git／資料操作；
- 必須弱化 Transaction、Knowledge、Hash 或批准邊界；
- 必須 force push；
- 修改核心情節才能解決對白問題；
- 同一核心問題經一次 Medium 和一次 High 有證據修復仍失敗。

## 11. 完成條件

DIALOGUE-01 只有在遠端 Git 可以讓另一台電腦上的 Agent 取得下列成果時才完成：

- 31 句逐句稽核；
- AI 模擬朗讀紀錄；
- 三份獨立評審；
- 最終影視劇本對白版；
- 最終對白軌；
- 最終 82 秒 SRT；
- Selection Decision；
- Hash Manifest；
- Video Handoff Overlay；
- 測試與報告；
- 最終 Branch／Commit／Merge SHA。

## 12. 直接交給 Codex 的命令

```text
先執行 git fetch --prune origin。
讀取最新遠端預設分支的 PROJECT_STATE.md，依其中必讀順序執行。
從最新遠端預設分支建立乾淨的 codex/dialogue01-human-voice-finalization 分支與 Worktree。
完整執行 DIALOGUE-01 V2：逐句稽核 31 句對白、AI 模擬朗讀、三個獨立 Codex 子評審、最多一次修訂，並同步更新最終影視劇本、對白軌、Shot 引用、82 秒 SRT、Selection、Hash 與 Git Handoff。
所有成果都必須 Commit 並 Push 到 GitHub。GitHub 遠端是團隊最高權威，不得把必要成果只留在本機、SQLite、out 或終端輸出。
一般工程使用 gpt-5.6-sol / medium；對白初次修改與唯一修訂需要時使用 high；三個評審使用 medium；不得使用 Max／Ultra。
除 PROJECT_STATE.md 的真正停止條件外，不要中途詢問。
不要使用 WORK，不要改主要劇情，不要讓陸塵行故意變笨，不要生成圖片／影片，不要呼叫 Provider，不要升格 Canon，不要寫下一章或新增互動遊戲。
完成實作、驗證、Push 並安全合併預設分支後，一次回報。
```
