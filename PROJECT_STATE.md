# AI Novel Engine — 現在狀態

> **這是唯一現況入口。** 新的 GPT、Codex、Reviewer 或其他 Agent 必須先讀本檔。舊 REBOOT、SCREEN-01、圖片 Work Package、HANDOFF、README 狀態與 `PLANS.md` 舊標題都是歷史資料，不能蓋過本檔。

## Current Override — WORLD-PHYSICS-02 NPC Action Binding

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
- Status: `RETRIEVAL00_PASS / RETRIEVAL01_SOURCE_LEVEL_PASS / NPC_ACTOR03_REVIEW_DESIGN_LIMITATION / NPC_ACTOR04_TECHNICAL_PASS_HUMAN_REVIEW_PENDING`
- Completed foundation: `RETRIEVAL-00 — Authority, Rebuild And Retention Foundation`
- Completed benchmark: `RETRIEVAL-01 — Offline Local Dual-Model Retrieval Benchmark`
- Current work package: `docs/work-packages/NPC-ACTOR-04_EMBODIED_SCENE_ROLLOUT_EVALUATION.md`
- `NPC-ACTOR-01 — Single NPC Actor Pilot` has completed provider-neutral Fake/File implementation and the first human blind review. The 20／20 preference is accepted only as a basic anti-exposition signal because the comparison was too easy.
- `NPC-ACTOR-02` human review is complete: Actor 4, natural control 6 and 2 ties. No Actor advantage was observed, so live API remains blocked.
- `NPC-ACTOR-03` revision 1 and its eight-item micro-dialogue packet remain technically valid, but Product Owner review is deferred because the evaluation lacks persistent environmental depth. Its answer key remains sealed.
- `NPC-ACTOR-04` evaluates the frozen revision through complete embodied scene trajectories. Technical gates passed; Product Owner review is pending. This is an evaluation-layer expansion, not a second calibration revision or production activation.
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
