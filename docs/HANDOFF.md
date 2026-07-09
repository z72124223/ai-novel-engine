# Project Handoff Index

This file is the first stop for a new Codex window. It records the current stable point, what is safe to touch, and what must not be started without explicit user approval.

## Current First-Read Override

For the whole-project voiced-comic route, future windows should use
`OPTIMIZED_FIRST_READ_PATH_V0` before reading long route docs:

1. `docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md`; read the optimized
   first-read path and required first-read rules first.
2. `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`; read only the top
   `CURRENT_ACTIVE_ROUTE_DIGEST_V0` section first.
3. `docs/CODEX_MULTI_WINDOW_OPERATING_MODEL_V0.md` only when the task involves
   window authority, routing, or dispatch.
4. `docs/CODEX_ROUTE_LOOP_CONVERGENCE_RULE_SET_V0.md` only when the task
   involves optimization, repeated same-family layers, route classification, or
   the A06 convergence rule.
5. This file only for exact current thread ids, exact file paths, current
   evidence, and stable-point lookup.

When AGENTS/project rules require `PLANS.md`, read only the top
`CURRENT_PLAN_WINDOW_V0` section first. Older `## Active ...` sections in
`PLANS.md` are historical evidence unless the current plan window names one as
current.

All other route/control docs below are on-demand evidence, not default startup
reading.

## 目前定案 — 有聲小說試驗中止 / 只產劇本（2026-07-09）

作者定案：**中止有聲小說（Audio Novel／YT 試聽出片）開發**；產品主線**退回只產生劇本**。

- **放棄：** `export-audio-novel-episode`、Edge TTS 預覽 MP4、燒錄字幕出片鏈（凍結保留，不擴充）
- **恢復主線：** 互動劇本 + 分鏡文字 + `story-turn` 回合協議 + MVP-8 讀者試玩
- **完整交接：** `docs/AUDIO_NOVEL_PHASE_HANDOFF_AND_ABORT_V1.md`
- **章程對齊：** `world/chenxing/formal/29_project_script_first_charter_v1.json`
- **試驗證據（只讀）：** `out/chenxing/audio_novel/episode01_v4_full/`、`out/chenxing/reader_play_v4/`
- **下一位窗口：** 勿把有聲出片當驗收；從分鏡 JSON 與 story-turn 繼續

## 目前定案 — MVP-8 試用層 / 讀者優先（2026-07-09）

作者定案：**MVP-8 = 試用層 + 讀者優先**，為引擎主線。

- **讀者優先**：不必先寫完世界；輸入意圖、讀故事即可。
- **留白區**：標記「還沒寫完」的地方，AI 可補有靈魂的臨時內容，不能補工具人。
- **延後升格**：試用只屬本場遊玩；滿意再走升格成正式設定。
- **劇本交付仍有效**，不擋讀者試玩。
- 中文規格：`docs/MVP8_PROVISIONAL_LAYER_SPEC.md`
- 決策摘要：`docs/MVP8_PROJECT_DECISION.md`
- 塵行章程：`world/chenxing/formal/35_provisional_layer_charter_v1.json`
- 實作步驟 8.1–8.5 已批准；MVP-8 **不是**手機 App。

## Current User Override - Script-First Product Pivot (2026-07-08)

The user approved a **script-first** product charter. This supersedes
voiced-comic / storyboard-image-as-terminal-product wording for active work.

- **Primary deliverable:** interactive script and shot text for `塵行`.
- **Not the repo goal:** voiced comic MP4, in-repo video generation, or art-style
  lock as the mainline.
- **Downstream only:** Seedance 2.0 and other external video tools after script
  packets are ready.
- **Active work:** polish script content in
  `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json` and
  `world/chenxing/formal/28_volume_one_ruin_interior_storyboard_v1.json`.
- **Paused:** style candidate selection, opening preview image generation,
  comic-video renderer chain, asset registry implementation, Seedance API
  integration, crossing beats 19–22 unless the author reopens them during
  script polish.
- Charter: `world/chenxing/formal/29_project_script_first_charter_v1.json`

## Current User Override - Grok Build / Codex Production Lane

The user approved a simple two-owner production workflow（生產工作流） on
2026-07-08:

- Grok Build owns story production, plot, dialogue, and story sections Codex
  cannot process.
- Codex owns safe production support: scene organization, shot planning, image
  prompt preparation, reference tracking, candidate metadata, validation, and
  integration.
- When Codex cannot process Grok story material, Codex must return it as
  `RETURN_TO_GROK` instead of rewriting or expanding it.
- Chenxing image work must be reference-first（先餵參考圖） and candidate-only
  until the user explicitly approves it.

Read this short current workflow before older image/provider restrictions:

- `docs/CODEX_GROK_BUILD_HANDOFF_AND_IMAGE_PRODUCTION_LANE_V0.md`

## Project Cleanup Baseline (2026-07-07)

Housekeeping baseline for non-engineer handoff:

- Large batch commit: `3249db2`; follow-up housekeeping: `fa13693` and ongoing
  cleanup commits on `codex/mvp6-checkpoint-branching-spec`.
- `PLANS.md`: read only `CURRENT_PLAN_WINDOW_V0` first; 200+ old `## Active`
  sections are now labeled `## Historical` and are archive evidence only.
- CLI code is being split out of the oversized `Main.kt` into
  `app/src/main/kotlin/com/localnovel/engine/cli/` modules.
- Default active story samples live in `samples/chenxing/` only; Rain Gate
  sample files were physically removed in commit `6322d0b`.
- Full Gradle test suite must pass before each cleanup commit.

## Active Creative World Source Lock

Current active creative worldview is locked to `塵行`, not Rain Gate.

SQL source authority update:

- The user-provided `塵行` / `穿越4000年` worldview is now treated as official
  source story data, not a loose draft.
- Controlled CLI import wrote it to `data/story.db` with
  `WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS`.
- SQL tables: `world_source_archives`, `world_source_documents`,
  `world_source_chunks`.
- Import evidence: `memory/chenxing_world_source_sql_import_result.json`.
- Query samples:
  `memory/chenxing_world_source_query_aijiang_sample.json` and
  `memory/chenxing_world_source_query_huiguan_sample.json`.
- Required lookup entrypoint for future windows:
  `query-world-source --db data/story.db --q <text> [--classification <id>]`.
- Do not use slow whole-workspace scans as the first method for worldview
  lookup. Use SQL first, then raw files only when exact source-text audit is
  needed.

AI task queue update:

- `data/story.db` now has a controlled local task queue:
  `ai_task_queue` and `ai_task_events`.
- Queue status evidence: `memory/ai_task_queue_status.json`.
- Current queue count at creation: `0`; no fake task was inserted.
- Main commands:
  `upsert-ai-task`, `claim-ai-task`, `complete-ai-task`, `list-ai-tasks`.
- Intended use: after an AI window completes current work, B組01 / the worker can
  call `claim-ai-task --db data/story.db --worker <id> [--target <role>]` to
  fetch the next `READY` task instead of relying only on chat context.
- This queue is not a background daemon; an active window or scheduler must
  invoke the CLI.

First-read source authority for worldview/story-source work:

1. `memory/active_world_source.json`
2. `data/story.db` via `query-world-source`
3. `memory/chenxing_world_source_sql_import_result.json`
4. `memory/chenxing_world_source_manifest.json`
5. `world/chenxing/raw/`
6. `memory/quarterfull_world_memory_capsule.json`
7. `docs/QUARTERFULL_WORLD_ALIGNMENT.md`
8. `docs/CODEX_ACTIVE_WORLD_SOURCE_LOCK_V0.md`
9. `docs/CODEX_CHENXING_WORLD_SOURCE_ARCHIVE_V0.md`

The raw archive stores the user-provided `穿越4000年` worldbuilding files under
the active `塵行` source lane. The title relationship remains unresolved; do
not rename or merge raw source labels without a later explicit source-authority
decision. The preferred next content step is
`chenxing_09_foundation_card_pack_contract_sample`.

Rain Gate, Pilot turn logs, local renderer/manifest chain samples, and old
sample setting packs are test/demo evidence only. They are not active creative
worldview, not author worldview, not canon, not approved setting-pack data, not
script source, and not production source material. Do not physically delete
referenced sample/test files until a dependency scan and replacement plan have
passed; instead, treat them as non-active test fixtures.

The whole-project blueprint package and blueprint execution control layer both
have 03 read-only PASS, but that PASS is not human approval, final media
approval, canon, registry, release, provider/media handoff, production lock, or
permission to resume Phase 20 / Phase 21. The next implementation route still
requires the AI-governed decision lane or an explicit user redirect; routine
technical approval should not be blocked on 00 manual review.

Current route selection should come from the top
`CURRENT_ACTIVE_ROUTE_DIGEST_V0` section in
`docs/CODEX_CURRENT_ROUTE_CARD_V0.md`. The A-group evolution docs remain
available on demand when a task involves the `EVOLUTION_EVENT`,
`ROOT_CAUSE_ANALYSIS`, `RULE_UPGRADE_PROPOSAL`, or `REGRESSION_CHECK` loop.
00 should route planning control through `00-01 分流接頭`; the splitter routes
planning control to `A組01`, and only mature A01-approved packages go to
`B組01`.

Current Codex window model is now group-prefixed Chinese titles plus a
single-lane Group B implementation pipeline. See
`docs/CODEX_MULTI_WINDOW_OPERATING_MODEL_V0.md` and
`docs/CODEX_WINDOW_TITLE_AND_SINGLE_LANE_ALIGNMENT_V0.md`.
`docs/CODEX_A_GROUP_OPTIMIZATION_AUDIT_LANE_V0.md` adds `A組06 優化稽核` as
the planning-only optimization lane for project lightening, duplicate-layer
reduction, route convergence, and drift prevention. A組06 reports to A組01 only
and must not dispatch B組02/B組03 or cut required final-product scope.
Authority rule: `00` owns user-facing direction and true human-required
decisions; `A組01` integrates Group A thinking and may apply
`docs/CODEX_AI_GOVERNED_DECISION_LANE_V0.md` to approve mature technical
packages for `B組01`; `B組01` is the only Group B dispatch authority. `00` and
`A組01` must not directly assign implementation or review work to `B組02` or
`B組03`.
The current preferred operating model separates Group A planning from Group B
implementation. Group A may iterate through A組02 proposal, A組03 critique,
A組04 synthesis, and bounded `SPAWN_REQUEST` planning branches so the project
can discover reusable fixed cores, variable modules, and composition rules.
Group B is single-lane only:
`accepted package -> B組01 -> B組02 -> B組01 -> B組03 -> B組01 -> 00/A組01`.
Planning branch windows report `BRANCH_REPORT` to `A組04` / `A組01` only and do
 not authorize implementation.
A/B hold relay rule: if `B組01` asks `A組01` and receives
`A_GROUP_BRANCH_REQUIRED`, `HOLD_FOR_A_GROUP_PLANNING`,
`HOLD_FOR_EXPLICIT_CLASSIFICATION`, or a non-stop route-authority gap, this is
not a routine user stop. `B組01` must route one bounded planning/control request
back to `A組01`; `A組01` owns the A-group planning branch or returns
`TRUE_STOP_CONDITION`. `00` is contacted only for true user-only decisions.
Current active ids: `00 總控決策=019f0dd1-1543-7f22-b646-5c12d07d168d`,
`A組01 整合規劃=019f2227-aeea-7870-9418-c7f5d399a2b1`,
`A組02 發散提案=019f258f-f29d-7843-bd2d-725476bf346a`,
`A組03 風險審閱=019f2590-1ce1-7271-ad94-036b27662b52`,
`A組04 綜合分支=019f2590-461b-7502-afb0-f5f65d79fc45`,
`A組05 分支提示詞邊界=019f25cb-dce1-78c1-9c93-5acd3c48f7aa`,
`A組06 優化稽核=019f30fe-b469-7ed1-aeea-2cfd3d97ccc3`,
`B組01 派工=019f25e7-7b64-7c91-b552-7287ef206e7d`,
`B組02 實作=019f1882-d5c6-72a3-893c-6d43cd097835`,
`B組03 審核=019f1ad1-2926-70c3-b85c-267df6fe331e`.
Archived obsolete side lanes: `90 封存 舊02B副線=019f2228-1aaf-7513-9457-7e7557fca40c`,
`91 封存 舊03B副線=019f2228-756a-7091-8294-885dc5ddaadd`; do not route work to
archived windows.

## Stable Point

- Current stable integration branch: `codex/mvp6-checkpoint-branching-spec`
- Remote: `origin https://github.com/z72124223/ai-novel-engine.git`
- Current branch should track `origin/codex/mvp6-checkpoint-branching-spec`.
- Use `git status --short --branch` and `git log --oneline --decorate -10` for the latest mutable `HEAD`.
- Latest product-contract baseline before this control-doc refresh: `63cc2f17b968dd9f1c9fff602150802d1ebf2ff2`.
- Use `git log --oneline --decorate -10` for the actual latest `HEAD` after any newer control-doc alignment commits.
- MVP-6 release tag: `mvp-6`
- `mvp-6` target commit: `be527031335efe8e2b0facae0145e1bbc084f8c6`
- Main pushed branches:
  - `codex/mvp6-checkpoint-branching-spec`
  - `pilot/real-use-vertical-slice`
- Pushed tags:
  - `mvp-2`
  - `mvp-3`
  - `mvp-4`
  - `mvp-5`
  - `mvp-6`
  - `pilot-1-budget-v2`
  - `pilot-1-capability-contract`
  - `pilot-1-context-normalized`
  - `pilot-1-context-relevance`
  - `pilot-1-stabilized`
  - `pilot-1b-coverage-repair`

## Current Verified Baseline

Verified on 2026-06-20 during the automatic stable-completion loop:

- Branch and origin synchronized at `a2e5a6f31f7d10483022e6e9e04a45c7cdeba63c`.
- `mvp-6` still points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `git diff --check` passed.
- `gradlew.bat clean test --no-daemon` passed with Temurin JDK 17.0.19.
- Test result: 233 tests, 0 failures, 0 errors, 0 skipped.
- `:app:installDist` was exercised during the diverse audit rerun milestone.
- `audit-narrative-quality` was run read-only on 16 additional non-Pilot samples; generated reports were ignored under `out/mvp7_diverse_audit_rerun/`.

Additional docs-only product-control baseline as of 2026-06-23:

- Authoring quality guidelines, narrative review gate, scene lock / storyboard asset boundary, script-ready scene packet contracts, Qingli scene registry seed brief, Qingli script packet registry dry-run review, storyboard / camera planning, production asset registry planning, and asset registry implementation blueprint are completed and pushed.
- User product decision (2026-07-08 pivot): current final deliverable is
  **interactive script / shot packets** for `塵行`. High-quality animation,
  voiced comic MP4, and in-repo video generation are **not** the repo goal.
- Seedance / Oiioii / video generation remain **downstream only** after script
  packets; not implemented in this repo and not the active mainline.
- Qingli is historical sample / process evidence after `docs/WORLD_BIBLE_REBUILD_DECISION_BRIEF.md`; do not use it as the new-world opening base or assetization starting point.
- Storyboard / camera planning, production asset registry planning, and asset registry implementation blueprint are future-planning docs. They are not runtime, schema, image-generation, Seedance, or Oiioii implementations.
- `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` is the current user / 00 input entry for rebuilding the non-Qingli world foundation. It is not canon, a setting pack, or a runtime artifact.
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` defines future World Card Queryer, Asset Queryer, and Story Card Queryer contracts for scoped product-layer retrieval. It is not implemented runtime, schema, CLI, SQLite, external API, GPT automation, Seedance, or Oiioii work.
- `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md` defines product-layer guardrails that treat bottom-layer truth as world causality and distributed actor knowledge, not as a plot objective, forced reveal, forced concealment, or mystery product. It is not runtime enforcement or canon world content.
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` registers the current 01-managed blueprint planning pool as `BLUEPRINT_PLANNING_REGISTERED`. Entries are non-canon candidates only: not canon, not setting-pack content, not runtime data, and not approved assets.
- `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md` registers `小說朗讀` as the canonical standalone `novel-reader-video` Node.js CLI project at `C:\Users\f7212\OneDrive\文件\小說朗讀`; the previous in-repo `C:\Users\f7212\OneDrive\文件\AI 劇本\小說朗讀` path has been removed after merge-copy verification.
- Script packet contract baseline commit: `63cc2f17b968dd9f1c9fff602150802d1ebf2ff2`.
- Qingli scene registry seed baseline commit: `c248cefd0dcde8a878aae290bbb3215ca4d5cb1b`.
- Qingli script packet registry dry-run baseline commit: `ee80a16f83825ac56b2449285c19981a81f746a1`.
- Use `git status --short` before assuming remote state. After the blueprint register milestone, there should be no expected uncommitted storyboard / camera or asset registry planning files.
- 04 / 05 / 06 / 07 / 08 are paused. They are not active runtime and must not be resumed without 01 explicit handoff.
- Manual handoff only: `01 explicit handoff -> 04 generation -> 05 review -> 06 display -> user explicit approval -> report to 01 -> 07 asset candidates -> 08 GPT storyboard pickup / verification -> user-reviewable storyboard delivery`.
- General project-completion task handoff mode is chat-window only: `01 assigns one smallest verified slice -> 02 implements and verifies -> 02 sends review handoff to 03 -> 03 performs read-only review -> 03 reports PASS / REVISE / FAIL to 01 -> 01 chooses the next slice`.
- This loop may repeat until the project is complete, but it is not an unattended infinite loop. Stop on safety boundaries, failed verification, dirty-scope ambiguity, product decisions, missing 03 review, user stop, or any need for explicit 01/user approval.
- Concrete Codex chat-window handoff procedure: `docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md`. Current known `01` Codex thread id: `019f0dd1-1543-7f22-b646-5c12d07d168d`. Use it only for bounded completion reports and one-task assignment requests. If the current worker is already this same `01` thread, do not send a message to itself; stop and ask for a distinct worker/control window.
- Model-selection policy for governed `01` / worker task turns is in `docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md`.
- First PASS evidence for the governed `01 -> 02 -> 01` loop: `docs/CODEX_CHAT_WINDOW_HANDOFF_SMOKE_TEST.md`.
- Accepted local comic-video demo package evidence: `docs/CODEX_COMIC_VIDEO_LOCAL_DEMO_PACKAGE.md`; package path `out/codex_local_demo_package/`; review-bundle manifest `docs/CODEX_LOCAL_DEMO_REVIEW_BUNDLE_MANIFEST.md`; no-external-tools local automation roadmap `docs/CODEX_NO_EXTERNAL_TOOLS_LOCAL_VIDEO_AUTOMATION_ROADMAP.md`. This has 02 local package PASS and 03 read-only PASS, plus full Gradle regression PASS from the prior slice. It is no staging approval, no commit approval, no release, no production lock, and no external provider/media line.
- Chenxing default sample chain: `samples/chenxing/README.md`; active story input `samples/chenxing/chenxing_story_input_pack_sample.json`; 09 foundation contract `samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json`; full pipeline artifacts under `samples/chenxing/pipeline/`.
- Local story input pack v0 entry point: `docs/CODEX_LOCAL_STORY_INPUT_PACK_V0.md`; default active sample `samples/chenxing/chenxing_story_input_pack_sample.json`;  validation command `validate-local-story-input-pack --input <json> --out <report.json>`. This is file-protocol JSON validation only and does not generate custom video, write SQLite, call provider/API, approve canon/registry/release, or create production lock.
- Local asset/prompt cards v0 entry point: `docs/CODEX_LOCAL_ASSET_PROMPT_CARDS_V0.md`; sample output `samples/chenxing/pipeline/chenxing_asset_prompt_cards_draft_sample.json`; draft command `draft-local-asset-prompt-cards --story-input <json> --story-validation <report.json> --out <cards.json>`. This converts a PASS local story input validation report into reviewable local cards only and does not call provider/API, generate media, approve assets, approve canon/registry/release, or create production lock.
- Local render package / QA draft v0 entry point: `docs/CODEX_LOCAL_RENDER_PACKAGE_V0.md`; sample output `samples/chenxing/pipeline/chenxing_local_render_package_draft_sample.json`; draft command `draft-local-render-package --story-input <json> --asset-prompt-cards <cards.json> --out <render-package.json>`. This converts a local story input pack plus `LOCAL_ASSET_PROMPT_CARDS_DRAFT` into reviewable timeline/panel/QA planning JSON only and does not call provider/API, generate media, produce final video, approve assets, approve canon/registry/release, or create production lock.
- Local story video chain verification v0 entry point: `docs/CODEX_LOCAL_STORY_VIDEO_CHAIN_V0.md`; sample report `samples/chenxing/pipeline/chenxing_story_video_chain_report_sample.json`; command `verify-local-story-video-chain --story-input <json> --output-dir <dir> --out <chain-report.json>`. This regenerates validation/cards/render-package/chain-report JSON from one story input only. It is NOT final video, NOT media generation, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local story video preview handoff draft v0 entry point: `docs/CODEX_LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_V0.md`; sample output `samples/chenxing/pipeline/chenxing_story_video_preview_handoff_draft_sample.json`; command `draft-local-story-video-preview-handoff --chain-report <json> --render-package <json> --asset-prompt-cards <json> --story-input <json> --out <handoff.json>`. This creates a review-only local JSON handoff draft toward future local preview rendering. It is NOT final video, NOT media generation, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local story video preview handoff review gate v0 entry point: `docs/CODEX_LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_REVIEW_V0.md`; sample report `samples/chenxing/pipeline/chenxing_story_video_preview_handoff_review_report_sample.json`; command `review-local-story-video-preview-handoff --handoff <json> --out <review-report.json>`. This validates the Phase 5 handoff JSON before any future adapter dry-run and only allows `local_renderer_adapter_dry_run` on PASS. It is NOT final video, NOT media generation, NOT adapter execution, NOT `ComicVideoRenderHandoffManifest` generation, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer adapter dry-run v0 entry point: `docs/CODEX_LOCAL_RENDERER_ADAPTER_DRY_RUN_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_adapter_dry_run_report_sample.json`; command `dry-run-local-renderer-adapter --handoff <json> --review <json> --out <dry-run-report.json>`. This checks whether the reviewed Phase 5 handoff is mappable toward future adapter implementation and only allows `local_renderer_adapter_implementation_plan` on PASS. It is NOT final video, NOT media generation, NOT adapter implementation execution, NOT `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer adapter implementation plan v0 entry point: `docs/CODEX_LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_adapter_implementation_plan_sample.json`; command `plan-local-renderer-adapter-implementation --dry-run-report <json> --out <plan.json>`. This turns a PASS Phase 7 dry-run report into local JSON planning notes for a future adapter implementation review and only allows `local_renderer_adapter_implementation_review` on PASS. It is NOT final video, NOT media generation, NOT adapter implementation execution, NOT `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer adapter implementation plan review gate v0 entry point: `docs/CODEX_LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_REVIEW_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_adapter_implementation_plan_review_report_sample.json`; command `review-local-renderer-adapter-implementation-plan --plan <json> --out <review-report.json>`. This validates the Phase 8 implementation plan JSON before any future adapter implementation draft and only allows `local_renderer_adapter_implementation_draft` on PASS. It is NOT final video, NOT media generation, NOT adapter implementation execution, NOT adapter implementation draft execution, NOT `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer adapter implementation draft v0 entry point: `docs/CODEX_LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_adapter_implementation_draft_sample.json`; command `draft-local-renderer-adapter-implementation --handoff <json> --plan <json> --plan-review <json> --out <draft.json>`. This combines the accepted Phase 5 handoff, Phase 8 plan, and Phase 9 plan review into a local JSON implementation draft for future review and only allows `local_renderer_adapter_implementation_draft_review` on PASS. It is NOT final video, NOT media generation, NOT adapter execution, NOT `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer adapter implementation draft review gate v0 entry point: `docs/CODEX_LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_adapter_implementation_draft_review_report_sample.json`; command `review-local-renderer-adapter-implementation-draft --draft <json> [--review <id>] --out <json>`. This validates the Phase 10 implementation draft JSON before any future manifest-draft planning task and only allows `local_renderer_manifest_draft_plan` on PASS. Status evidence is `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_DRAFT_REVIEW_PASS`; downstream boundary is `local_renderer_adapter_implementation_draft_review_only_not_manifest_generation`; focused tests PASS with `-Pkotlin.daemon.jvmargs=-Xmx4096m`. Boundary phrase: no media generation. It is NOT final video, NOT media generation, NOT adapter execution, NOT real `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest draft plan v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_draft_plan_sample.json`; command `plan-local-renderer-manifest-draft --implementation-draft <json> --draft-review <json> [--plan <id>] --out <json>`. This reads the PASS Phase 10 implementation draft plus PASS Phase 11 implementation-draft review and creates only a planning JSON for a future manifest-draft review gate, allowing only `local_renderer_manifest_draft_plan_review` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN`; downstream boundary is `local_renderer_manifest_draft_plan_only_not_manifest_generation`. Boundary phrase: no media generation. It is NOT final video, NOT media generation, NOT adapter execution, NOT real `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest draft plan review gate v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_draft_plan_review_report_sample.json`; command `review-local-renderer-manifest-draft-plan --plan <json> [--review <id>] --out <json>`. This validates the PASS Phase 12 manifest draft plan JSON before any future manifest JSON draft task and only allows `local_renderer_manifest_draft_json_draft` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_DRAFT_PLAN_REVIEW_PASS`; downstream boundary is `local_renderer_manifest_draft_plan_review_only_not_manifest_generation`. Boundary phrase: no media generation. It is NOT final video, NOT media generation, NOT adapter execution, NOT real `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest draft JSON draft v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_draft_json_draft_sample.json`; command `draft-local-renderer-manifest-json --plan <json> --plan-review <json> [--draft <id>] --out <json>`. This reads the PASS Phase 12 manifest draft plan plus PASS Phase 13 plan review and creates only a draft-only local JSON packet for future manifest JSON review, allowing only `local_renderer_manifest_draft_json_review` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_DRAFT`; downstream boundary is `local_renderer_manifest_draft_json_draft_only_not_production_manifest`. Boundary phrase: no media generation. It is NOT final video, NOT media generation, NOT adapter execution, NOT real production `ComicVideoRenderHandoffManifest` generation, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest draft JSON review gate v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_draft_json_review_report_sample.json`; command `review-local-renderer-manifest-draft-json --draft <json> [--review <id>] --out <json>`. This validates the PASS Phase 14 draft-only local JSON packet before any future adapter dry-run step and only allows `local_renderer_manifest_draft_json_adapter_dry_run` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_REVIEW_PASS`; downstream boundary is `local_renderer_manifest_draft_json_review_only_not_production_approval`. Boundary phrase: no media generation. It is NOT final video, NOT final video readiness, NOT media generation, NOT adapter execution, NOT real production `ComicVideoRenderHandoffManifest` generation or approval, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest draft JSON adapter dry-run v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_draft_json_adapter_dry_run_report_sample.json`; command `dry-run-local-renderer-manifest-draft-json-adapter --draft <json> --review <json> [--dry-run-id <id>] --out <json>`. This validates the PASS Phase 14 draft-only local JSON packet plus PASS Phase 15 review report and checks adapter dry-run mapping only, allowing only `local_renderer_manifest_adapter_contract_plan` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_DRAFT_JSON_ADAPTER_DRY_RUN_PASS`; downstream boundary is `local_renderer_manifest_draft_json_adapter_dry_run_only_not_renderer_execution`. Boundary phrase: no media generation. It is NOT final video, NOT final video readiness, NOT media generation, NOT adapter execution, NOT renderer execution, NOT real production `ComicVideoRenderHandoffManifest` generation or approval, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest adapter contract plan v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_adapter_contract_plan_sample.json`; command `plan-local-renderer-manifest-adapter-contract --dry-run-report <json> [--plan <id>] --out <json>`. This reads the PASS Phase 16 adapter dry-run report and creates only adapter contract planning JSON, allowing only `local_renderer_manifest_adapter_contract_plan_review` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN`; downstream boundary is `local_renderer_manifest_adapter_contract_plan_only_not_adapter_implementation`. Boundary phrase: no media generation. It is NOT final video, NOT final video readiness, NOT media generation, NOT adapter implementation, NOT adapter execution, NOT renderer execution, NOT real production `ComicVideoRenderHandoffManifest` generation or approval, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest adapter contract plan review gate v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_adapter_contract_plan_review_report_sample.json`; command `review-local-renderer-manifest-adapter-contract-plan --plan <json> [--review <id>] --out <json>`. This validates the PASS Phase 17 adapter contract plan JSON and creates only adapter contract plan review JSON, allowing only `local_renderer_manifest_adapter_contract_draft` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_PLAN_REVIEW_PASS`; downstream boundary is `local_renderer_manifest_adapter_contract_plan_review_only_not_adapter_implementation`. Boundary phrase: no media generation. It is NOT final video, NOT final video readiness, NOT media generation, NOT adapter implementation, NOT adapter execution, NOT renderer execution, NOT real production `ComicVideoRenderHandoffManifest` generation or approval, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Local renderer manifest adapter contract draft v0 entry point: `docs/CODEX_LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_V0.md`; sample report `samples/chenxing/pipeline/chenxing_local_renderer_manifest_adapter_contract_draft_sample.json`; command `draft-local-renderer-manifest-adapter-contract --plan <json> --plan-review <json> [--draft <id>] --out <json>`. This reads the accepted Phase 17 adapter contract plan plus PASS Phase 18 review report and creates only a draft adapter contract artifact, allowing only `local_renderer_manifest_adapter_contract_draft_review` on PASS. Status evidence is `LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT`; downstream boundary is `local_renderer_manifest_adapter_contract_draft_only_not_adapter_execution`. Boundary phrase: no media generation. It is NOT final video, NOT final video readiness, NOT media generation, NOT adapter implementation, NOT adapter execution, NOT renderer execution, NOT real production `ComicVideoRenderHandoffManifest` generation or approval, NOT renderer readiness, NOT MP4 readiness, NOT final media approval, NOT provider/media handoff, NOT canon, NOT registry, NOT release, NOT approval, and NOT production lock; it does not call provider/API or run renderer/ffmpeg/TTS.
- Whole-project blueprint v0 entry point: `docs/CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md`. This is now the top-level blueprint route after the user redirected away from the old renderer-manifest micro-phase chain. Phase 19 remains PASS / 03 PASS, but Phase 20 was stopped before edits and must not resume until the whole-project blueprint is reviewed and 01/user explicitly selects the next route. The blueprint includes user / 00 input, 09 foundation, demand-driven query/retrieval, script-ready scene packets, 04/05/06/07/08, storyboard/camera, asset registry, voice timeline, local renderer execution, human approval gates, and optional provider/funding gates. It is docs/control only: no media generation, no provider/API, no direct SQLite writes, no renderer/ffmpeg/TTS, no final media approval, no provider/media handoff, no canon, no registry, no release, no approval, and no production lock.
- Whole-project work package v0 entry point: `docs/CODEX_WHOLE_PROJECT_WORK_PACKAGE_V0.md`; progress model `docs/CODEX_TRUE_PROJECT_COMPLETION_PROGRESS_MODEL_V0.md`; approval matrix `docs/CODEX_POSITIVE_APPROVAL_MATRIX_V0.md`; query templates `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`; 09 foundation organization `docs/CODEX_09_FOUNDATION_CARD_ORGANIZATION_V0.md`. These define the direction before future small/medium tasks are assigned. They are docs/control only and do not approve Phase 20, Phase 21, renderer execution, media generation, provider/API, direct SQLite writes, canon, registry, release, approval, or production lock.
- 09 foundation card execution preflight v0 entry point: `docs/CODEX_09_FOUNDATION_CARD_EXECUTION_PREFLIGHT_V0.md`. This GAME_MODE_LOW_INTERFERENCE docs/control packet turns the accepted 09 foundation card organization layer into a safe future implementation plan only. It does not implement runtime/CLI/schema, create canon or setting-pack data, resume 04/05/06/07/08, approve assets/registry, call provider/API, run renderer/ffmpeg/TTS, generate media, grant final media approval, release, or production lock.
- 09 foundation card JSON contract v0 entry point: `docs/CODEX_09_FOUNDATION_CARD_JSON_CONTRACT_V0.md`; sample pack `samples/chenxing/pipeline/chenxing_09_foundation_card_pack_contract_sample.json`. This defines `FoundationCardPack` as local file-protocol JSON only and proposes only `foundation_card_pack_validation_v0` as a future next step. It is not canon, not setting-pack data, not runtime SQLite data, not approved assets, not 04/05/06/07/08 resumption, not provider/API, not renderer/ffmpeg/TTS/media generation, not final media approval, not release, and not production lock.
- 09 foundation card pack validation v0 entry point: `docs/CODEX_09_FOUNDATION_CARD_PACK_VALIDATION_V0.md`; sample report `samples/chenxing/pipeline/chenxing_09_foundation_card_pack_validation_report_sample.json`; command `validate-09-foundation-card-pack --input <pack.json> --out <report.json> [--validation <id>]`. This validates draft non-canon `FoundationCardPack` JSON and only allows `09_foundation_card_pack_review` on PASS. It is local file-protocol JSON only: not canon, not setting-pack data, not runtime SQLite data, not approved assets, not 04/05/06/07/08 resumption, not provider/API, not renderer/ffmpeg/TTS/media generation, not final media approval, not release, and not production lock.
- 09 foundation card pack review v0 entry point: `docs/CODEX_09_FOUNDATION_CARD_PACK_REVIEW_V0.md`; sample report `samples/chenxing/pipeline/chenxing_09_foundation_card_pack_review_report_sample.json`; command `review-09-foundation-card-pack --pack <pack.json> --validation <validation-report.json> --out <review-report.json> [--review <id>]`. This is a technical-only local JSON review gate for a PASS `FOUNDATION_CARD_PACK_VALIDATION_PASS` report and its source `FoundationCardPack`; on PASS it only allows `demand_driven_query_contract_spec_v0`. It is not human approval, not canon approval, not registry approval, not setting-pack conversion, not runtime SQLite data, not approved assets, not demand-driven query implementation, not provider/API, not renderer/ffmpeg/TTS/media generation, not final media approval, not release, and not production lock.
- Demand-driven query contract spec v0 entry point: `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`; sample packet `samples/chenxing/pipeline/chenxing_demand_driven_query_contract_spec_sample.json`; template pointer `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`. This is a docs/spec/data-contract packet for future World Card Queryer, Asset Queryer, and Situation / Continuity / Pressure Queryer request/response/review shapes only. It requires minimum-scope citations to 09 foundation cards or accepted planning docs so 04/05/06/07/08 do not read a full world bible by default. It is not canon, no provider/API, no SQLite, no runtime implementation, no 04/05/06/07/08 restart, no asset approval, no registry approval, no release, and no production lock.
- Script-ready scene packet alignment v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md`; related base spec `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`. This docs-only alignment explains how `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` evidence from `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` may feed future scene-packet sections while preserving `author_only`, `review_only`, `production_only`, and `user_facing` boundaries. It is no script packet generation, no 04/05/06/07/08 restart, no provider/API, no SQLite, no runtime implementation, no canon/registry/release/final media approval, and no production lock.
- Prompt surface leakage boundary v0 entry point: `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`. This docs/control packet says prompt surface is deny-by-default, negative prompts are also prompt surface, and `author_only` / `review_only` material must not be copied or paraphrased into `NegativeBoundaryCard`; use `protectedBoundaryRef` plus generic `boundaryCategory` instead. It is no Kotlin/CLI/runtime/schema/sample/provider/API/media implementation, no 04/05/06/07/08 restart, no Phase 20 / Phase 21 restart, no canon/registry/release/final approval, and no production lock.
- Storyboard/camera packet template review v0 entry point: `docs/CODEX_STORYBOARD_CAMERA_PACKET_TEMPLATE_REVIEW_V0.md`. This docs/control packet defines a reviewable `StoryboardCameraPacket` template and checklist bridging script-ready scene packet evidence into future storyboard/camera planning. It is no 04/05/06/07/08 restart, no Kotlin/CLI/runtime/schema/sample JSON, no provider/API, no renderer/ffmpeg/TTS, no media generation, no final approval, and no production lock.
- Asset lifecycle registry decision packet v0 entry point: `docs/CODEX_ASSET_LIFECYCLE_REGISTRY_DECISION_PACKET_V0.md`. This docs/control packet compares `draft_files_only`, `same_runtime_sqlite_db`, `sidecar_production_asset_db`, and optional `deferred_decision` for future asset lifecycle / registry storage review. It does not choose an option and is no Kotlin/CLI/runtime/schema/migration/sample JSON, no direct SQLite writes, no asset approval, no registry approval, no provider/API, no media generation, no final approval, and no production lock.
- Voice timeline / subtitle / SFX contract v0 entry point: `docs/CODEX_VOICE_TIMELINE_SUBTITLE_SFX_CONTRACT_V0.md`. This docs/control packet defines a reviewable `VoiceTimelineSubtitleSfxContract` for future dialogue, subtitle, SFX, ambient, silence, and timing intent planning. It references `allowedPromptSurface` and `protectedBoundaryRef` safety, but is no Kotlin/CLI/runtime/schema/sample JSON, no provider/API, no renderer/ffmpeg/TTS, no media generation, no final media readiness, no final approval, and no production lock.
- Local renderer execution readiness checklist v0 entry point: `docs/CODEX_LOCAL_RENDERER_EXECUTION_READINESS_CHECKLIST_V0.md`. This docs/control packet defines a reviewable `LocalRendererExecutionReadinessChecklist` for future renderer execution readiness decisions after upstream script, storyboard/camera, asset lifecycle, voice timeline/subtitle/SFX, local render package, QA, and human boundary evidence are aligned. It is no Kotlin/CLI/runtime/schema/sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final media readiness, no final approval, and no production lock.
- Local package / QA / delivery definition v0 entry point: `docs/CODEX_LOCAL_PACKAGE_QA_DELIVERY_DEFINITION_V0.md`. This docs/control packet defines a reviewable `LocalPackageQaDeliveryDefinition` for future package content placeholders, QA categories, artifact manifest placeholders, output evidence requirements, known limitations, and reviewer notes. It is no Kotlin/CLI/runtime/schema/sample JSON, no provider/API, no package generation, no QA execution against real media, no renderer/ffmpeg/TTS execution, no media generation, no release approval, no final media readiness, and no production lock.
- Blueprint route completion status and next-route options v0 entry point: `docs/CODEX_BLUEPRINT_ROUTE_COMPLETION_STATUS_AND_NEXT_ROUTE_OPTIONS_V0.md`. This docs/control packet summarizes the reviewed default-route chain from demand-driven query/retrieval through local package / QA / delivery and lists candidate next-route options only. It is not route selection, not implementation, not B組03 dispatch, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate route decision brief v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_ROUTE_DECISION_BRIEF_V0.md`. This docs/control brief evaluates whether reviewed route evidence is sufficient to define a future `script_ready_scene_packet_candidate` package-definition/review-surface task. It recommends only a candidate future package, not actual `ScriptReadyScenePacket` generation, not sample JSON, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate package definition v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_PACKAGE_DEFINITION_V0.md`. This docs/control definition describes a future reviewable `ScriptReadyScenePacketCandidate` package surface as placeholders and references only. It is not an actual `ScriptReadyScenePacket`, not sample JSON, not story generation, not runtime/CLI/schema/validator implementation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate preflight v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_PREFLIGHT_V0.md`. This docs/control preflight checks whether the project can safely enter a future first actual `ScriptReadyScenePacket` candidate task. Outcome is `BLOCKED_BY_MISSING_INPUTS`: reviewed docs/control evidence supplies rules and placeholders, but no selected source scene id, zone id, beat id, character refs, or asset need refs. It is not actual packet generation, not a `ScriptReadyScenePacketCandidate` instance, not sample JSON, not story generation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate source-basis decision brief v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_SOURCE_BASIS_DECISION_BRIEF_V0.md`. This docs/control brief decides whether reviewed docs already contain a selectable concrete source basis for a future first `ScriptReadyScenePacket` candidate. Outcome is `SOURCE_BASIS_MISSING_AI_RESOLVABLE_PLANNING_GAP`: no selected source scene id, zone id, beat id, character refs, asset need refs, or `QueryRequest` / `QueryResponse` / `QueryScopeReviewReport` refs are attached to the first packet yet. It is not actual packet generation, not a `ScriptReadyScenePacketCandidate` instance, not sample JSON, not story generation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate source-basis selection record v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_SOURCE_BASIS_SELECTION_RECORD_V0.md`. This docs/control selection record attempts to select one source-basis bundle from reviewed evidence only. Outcome is `SOURCE_BASIS_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`: query samples include placeholder scene/zone/beat refs and no reviewed bundle ties source scene id, zone id, beat id, character refs, asset need refs, and `QueryRequest` / `QueryResponse` / `QueryScopeReviewReport` refs together. It is not actual packet generation, not an actual `ScriptReadyScenePacketCandidate` instance, not sample JSON, not story generation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate reviewed-evidence bundle gap resolution v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_GAP_RESOLUTION_V0.md`. This docs/control note resolves the prior reviewed-evidence gap as `REVIEWED_EVIDENCE_BUNDLE_GAP_AI_RESOLVABLE` and recommends only a future classification-only `script_ready_scene_packet_candidate_reviewed_evidence_bundle_definition_v0_docs_control` package. It does not select a concrete source-basis bundle, does not create an actual `ScriptReadyScenePacketCandidate` instance, not sample JSON, not story generation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate reviewed-evidence bundle definition v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_DEFINITION_V0.md`. This docs/control definition records `REVIEWED_EVIDENCE_BUNDLE_DEFINITION_READY_FOR_FUTURE_SELECTION` and defines a placeholder/reference-only future evidence bundle structure after `REVIEWED_EVIDENCE_BUNDLE_GAP_AI_RESOLVABLE`. It recommends only a future classification-only `script_ready_scene_packet_candidate_reviewed_evidence_bundle_selection_v0_docs_control` package. It does not select concrete source scene id, zone id, beat id, character refs, asset need refs, `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` refs, does not create an actual `ScriptReadyScenePacketCandidate` instance, not sample JSON, not story generation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate reviewed-evidence bundle selection v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_SELECTION_V0.md`. This docs/control selection-decision record attempts one reviewed-evidence bundle selection after `REVIEWED_EVIDENCE_BUNDLE_DEFINITION_READY_FOR_FUTURE_SELECTION`. Outcome is `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`: the definition exists, but no reviewed bundle ties source scene id, zone id, beat id, character refs, asset need refs, `QueryRequest`, `QueryResponse`, `QueryScopeReviewReport`, continuity, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, and review gates together. It does not select source basis, not actual packet generation, not an actual `ScriptReadyScenePacketCandidate` instance, not sample JSON, not story generation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate existing reviewed evidence bundle inventory v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_EXISTING_REVIEWED_EVIDENCE_BUNDLE_INVENTORY_V0.md`. This docs/control inventory inspects existing reviewed docs only after `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`. Outcome is `EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE`: reviewed docs provide required template/rule citations, but no reviewed bundle supplies concrete source scene id, zone id, beat id, character refs, asset need refs, `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` refs. Missing concrete refs are classified as AI-resolvable planning gaps, not proven user/worldbuilding blockers or boundary blockers. It does not select source basis, not actual packet generation, not an actual `ScriptReadyScenePacketCandidate` instance, not sample JSON, not story generation, not 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate AI-resolvable gap closure path v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_AI_RESOLVABLE_GAP_CLOSURE_PATH_V0.md`. This docs/control path responds to `EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE` and keeps missing source scene id, zone id, beat id, character refs, asset need refs, `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs as AI-resolvable planning gaps unless reviewed evidence later proves a user/worldbuilding blocker or `BLOCKED_BY_BOUNDARY`. It names only future classification-only `script_ready_scene_packet_candidate_reviewed_evidence_bundle_ref_resolution_plan_v0_docs_control`; no source-basis selection, no actual packet, no evidence bundle creation, no sample JSON, no story generation, no 04/05/06/07/08 restart, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate reviewed-evidence bundle ref-resolution plan v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_REF_RESOLUTION_PLAN_V0.md`. This docs/control plan records `REF_RESOLUTION_PLAN_DEFINED_FOR_FUTURE_PACKET_CLASSIFICATION` after `EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE` and `AI_RESOLVABLE_GAP_CLOSURE_PATH_DEFINED_FOR_FUTURE_CLASSIFICATION`; it defines allowed and forbidden evidence sources for future resolution of source scene id, zone id, beat id, character refs, asset need refs, `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs. It keeps each missing ref as an AI-resolvable planning gap unless reviewed evidence proves user/worldbuilding blocker or `BLOCKED_BY_BOUNDARY`; no concrete ref resolution, no source-basis selection, no evidence bundle creation, no actual packet, no sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate reviewed-evidence bundle ref-resolution packet v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_REF_RESOLUTION_PACKET_V0.md`. This docs/control packet records `REF_RESOLUTION_PACKET_BLOCKED_BY_UNRESOLVED_REVIEWED_REFS`: source scene id, zone id, beat id, character refs, asset need refs, `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` slots are all `REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` because reviewed docs/control evidence still does not cite concrete refs. It names no next future package candidate due to the blocker; no concrete ref resolution, no source-basis selection, no evidence bundle creation, no actual packet, no sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate unresolved ref source catalog v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_UNRESOLVED_REF_SOURCE_CATALOG_V0.md`. This docs/control catalog responds to `REF_RESOLUTION_PACKET_BLOCKED_BY_UNRESOLVED_REVIEWED_REFS`. It records partial candidate paths needing review for source scene id, zone id, beat id, character refs, and asset need refs, while `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` remain `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP`. It does not find a coherent set of candidate refs and names no next future package candidate; no concrete ref resolution, no source-basis selection, no evidence bundle creation, no actual packet, no sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate query-ref gap closure decision brief v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_GAP_CLOSURE_DECISION_BRIEF_V0.md`. This docs/control brief responds to the catalog result and selects `QUERY_REF_GAP_AI_RESOLVABLE_DOCS_CONTROL_ROUTE_AVAILABLE`: query refs remain missing, Qingli-derived candidates remain unpromoted, and a future query-ref evidence surface can be classified only through another explicit docs/control task. It does not create actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, not concrete ref resolution, not source-basis selection, not evidence bundle creation, not actual packet generation, not sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate query-ref evidence surface definition v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_SURFACE_DEFINITION_V0.md`. This docs/control definition records `QUERY_REF_EVIDENCE_SURFACE_DEFINED_FOR_FUTURE_REVIEW` and defines placeholder/reference-only metadata for future `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs. It does not create actual query instances, not query execution, not queryer implementation, not promotion of Qingli candidates, not concrete ref resolution, not source-basis selection, not evidence bundle creation, not actual packet generation, not sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate query-ref evidence placeholder packet v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_V0.md`. This docs/control packet records `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`: `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` slots are represented only as placeholder/reference-only metadata and remain unresolved because no concrete reviewed docs/control refs exist. It names no future package candidate due to the blocker; no actual query instances, no query execution, no queryer implementation, no promotion of Qingli candidates, no concrete ref resolution, no source-basis selection, no evidence bundle creation, no actual packet generation, no sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Script-ready scene packet candidate query-ref reviewed-ref discovery matrix v0 entry point: `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_REVIEWED_REF_DISCOVERY_MATRIX_V0.md`. This docs/control matrix responds to `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS` and classifies `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs as `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`; no reviewed discovery candidate is found, so no future package candidate is named. It does not create actual query instances, not query execution, not queryer implementation, not concrete ref resolution, not source-basis selection, not evidence bundle creation, not actual packet generation, not sample JSON, no provider/API, no renderer/ffmpeg/TTS execution, no media generation, no final approval, and no production lock.
- Blueprint execution control layer v0 entry point: `docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md`; current route card `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`. These define `OPTIMIZED_FIRST_READ_PATH_V0` and `CURRENT_ACTIVE_ROUTE_DIGEST_V0` for future windows. Route status tags, default route decision rules, and other control docs are on-demand evidence. These docs reduce over-reading and define defaults, but they are docs/control only and do not start implementation or grant approval.
- Future route gap control note for new windows: `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_GAP_AUDIT.md`. It records that demand-driven queryers, 09 foundation cards, script-ready scene packets, 04/05/06/07/08 storyboard flow, storyboard/camera packet work, asset registry lifecycle, voice timeline, positive human approval gates, and external-provider gates are product-route gaps that must be preserved in the blueprint. They are not implemented by the current renderer-manifest phases and must not be started without explicit 01/user assignment. It also records quota-check cadence for high-cost 01/02/03 transitions.
- Ordered future route integration map: `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_INTEGRATION_MAP_V0.md`. This is the current ordered route view connecting user / 00 input, 09 foundation, demand-driven query, script-ready packet, 04/05/06/07/08, storyboard/camera, asset registry, voice timeline, local renderer, human approval, and external provider gates. It is docs/control only, not Phase 17, no media generation, no provider/API, no production lock, and no implementation approval.
- After every `03` PASS / REVISE / FAIL report, `01` must display `Project completion progress: NN%` using `docs/PROJECT_COMPLETION_PROGRESS.md`. PASS may update the estimate for accepted capability slices; REVISE / FAIL keep the last accepted estimate and name the blocker.
- Do not use automation / scheduler wakeups to advance the 04 / 05 / 06 / 07 / 08 flow.

## Legacy Runtime Read First

For legacy runtime, MVP, or non-blueprint tasks that are not governed by the
current first-read override, read these before planning or editing:

1. `AGENTS.md`
2. `README.md`
3. `ROADMAP.md`
4. `PLANS.md`
5. `DONE_CRITERIA.md`
6. `VERIFY.md`
7. `docs/HANDOFF.md`
8. `docs/PROJECT_COMPLETION_AUDIT.md`
9. `docs/TURN_PROTOCOL.md`
10. `docs/VALIDATION_RULES.md`

For MVP-7 audit work, also read:

- `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`
- `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`
- `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md`
- `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`
- `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md`
- `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`
- `docs/MVP7_AUDIT_JSON_ROBUSTNESS_INVESTIGATION.md`
- `docs/MVP7_DIVERSE_AUDIT_SAMPLE_RERUN_REPORT.md`

For Pilot or context-budget work, also read:

- `docs/PILOT1_FINDINGS.md`
- `docs/PILOT1B_COVERAGE_REPAIR.md`
- `docs/CONTEXT_RELEVANCE_FLOOR.md`
- `docs/REFERENCED_CAPABILITY_CONTRACTS.md`

## Completed Since MVP-6

- `:app:installDist` packaging repair.
- Local bundle backup and private GitHub remote push.
- Remote restore verification.
- Project control docs refresh.
- Original blueprint alignment.
- MVP-7 narrative quality spec draft.
- MVP-7 Candidate A read-only narrative quality audit.
- Candidate A.1 calibration refinements.
- Real audit sample collection.
- Deliberately flawed audit sample pack.
- Candidate A.2 calibration planning and implementation.
- A.2 real sample rerun.
- Audit JSON robustness investigation.
- Audit JSON verification docs hardening.
- Automatic stable-completion loop bootstrap.
- Diverse non-Pilot audit sample rerun.
- Authoring quality guidelines / review workflow docs.
- Narrative review gate and eight-point checklist.
- Scene lock and animation asset boundary.
- Script-ready scene packet contracts.
- Qingli scene registry seed brief.
- Qingli script packet registry dry-run review.
- Storyboard / camera planning, aligned to storyboard-first final deliverables.
- Production asset registry planning, aligned to storyboard consistency before optional downstream video.
- Asset registry implementation blueprint, future-only and not schema/runtime approval.
- New world foundation questionnaire, as a docs-only user-input surface before any foundation brief or 04/05/06/07/08 restart.
- Local subproject directory registration for `小說朗讀`, docs/local-memory only and not runtime or asset approval.

## Hard Boundaries

Do not start or add any of these unless the user explicitly approves a new milestone:

- Candidate B extensions beyond the already-shipped MVP-7 Candidate B gate unless a new milestone says otherwise.
- Candidate A.3 unless explicitly approved.
- Web server, HTTP API, port listener, Web UI, Android UI.
- Docker, Rust, embedding, vector DB, or multi-agent runtime.
- Automation / scheduler advancement for 04 / 05 / 06 / 07 / 08 handoff.
- Unattended infinite project-completion loops that bypass `01 -> 02 -> 03 -> 01` chat-window review.
- Self-triggering Codex thread loops that treat silence, lack of review, or an implied next step as approval.
- Resuming 04 / 05 / 06 generation, review, display, playtest, 07 asset-candidate creation, or 08 storyboard pickup without 01 explicit handoff and user approval where required.
- Seedance / Oiioii integration, high-quality video generation, or animation asset pipeline implementation.
- Production asset registry schema, migrations, import CLI, generated image storage, or direct SQLite asset writes.
- Demand-driven queryer implementation, query audit tables, query CLI commands, runtime query enforcement, or external retrieval services.
- Treating simulation-first guardrails as schema, Validator behavior, StateReducer behavior, setting-pack canon, or approval to start 09 / 04 / 05 / 06 / 07 / 08.
- Treating blueprint register entries as canon, setting-pack content, runtime data, approved assets, first-batch conversion content, or approval to start 09 / 04 / 05 / 06 / 07 / 08.
- OpenAI API or any external AI call.
- Schema changes or migration edits.
- Validator, `stateVersion`, or `contextHash` bypass.
- `resolve-turn` or `StateReducer` write-path changes.
- Direct SQLite writes outside approved CLI/runtime flow.
- Retagging `mvp-6`.
- Pushing ignored/generated content such as `data/`, `out/`, `backups/`, build output, Gradle cache, or temp JDK.

## Normal Verification

Use these before committing any milestone:

```powershell
git status
git diff --stat
git diff --check
gradlew.bat clean test --no-daemon
```

For packaging work, also run:

```powershell
gradlew.bat :app:installDist
```

Then run the installed CLI from the generated distribution and verify at least:

```powershell
app.bat help
app.bat init-db --db <temp-db>
```

For MVP-7 audit report JSON validation on Windows PowerShell 5.1, read generated report JSON with explicit UTF-8:

```powershell
Get-Content -LiteralPath <audit-report.json> -Encoding UTF8 -Raw | ConvertFrom-Json
```

If `JAVA_HOME` is missing, a temporary JDK outside the repo may be used. Do not copy a JDK into the repo and do not write absolute JDK paths into project files.

For explicitly approved docs-only documentation/control milestones, 01 or the user may allow the narrow docs-only exception in `VERIFY.md`: `git diff --check`, diff/stat/staged inspection, no code/runtime touched, no ignored artifacts staged, final explanation for skipped Gradle, and direct 03 read-only verification. Do not apply that exception to code/runtime/schema/CLI/Pilot work.

## Remote Restore Check

To verify the GitHub backup from a clean location:

```powershell
$Clone = Join-Path $env:TEMP "ai-novel-engine-restore-check"
Remove-Item -LiteralPath $Clone -Recurse -Force -ErrorAction SilentlyContinue
git clone https://github.com/z72124223/ai-novel-engine.git $Clone
git -C $Clone switch codex/mvp6-checkpoint-branching-spec
git -C $Clone status
git -C $Clone tag --list
git -C $Clone rev-parse "mvp-6^{}"
```

Expected `mvp-6` target:

```text
be527031335efe8e2b0facae0145e1bbc084f8c6
```

## Branch Map

- `codex/mvp6-checkpoint-branching-spec`: current stable integration branch.
- `pilot/real-use-vertical-slice`: Pilot evidence branch pushed for preservation.
- `master`: older baseline branch, not the current stable line.
- Historical `feat/*` and `fix/*` branches may exist locally or in history. Do not delete or merge without a branch hygiene milestone and user approval.

## Current Next-Step Advice

Do not enter Candidate B automatically.

Recommended safe next steps:

1. Wait for user / 00 answers to `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`.
2. Draft `NEW_WORLD_FOUNDATION_BRIEF` only after the user provides enough world-preference material.
3. Draft a 09 world-card / situation-continuity-pressure card organization brief only after a foundation brief exists; keep it docs-only unless 01/user separately approves implementation, and preserve the simulation-first rule that causality is not a reveal schedule.
4. Draft a blueprint first-batch conversion decision brief only if 01 asks whether any non-canon blueprint candidates should be considered for a future minimal setting-pack skeleton. Do not convert or apply data.
5. Treat `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md` as the docs-only bridge from the accepted demand-driven query contract/spec into future script-ready scene packet work. After review PASS, 01 may consider only a docs-only `script_ready_scene_packet_template_or_review_v0` decision; this is not approval, not auto-start, no queryer implementation, no script packet generation, no provider/API, no SQLite, no 04/05/06/07/08 restart, and no production lock.
6. Draft a 04/05/06/07/08 manual resumption decision brief only if 01/user asks whether to resume that manual handoff flow. It must not itself generate, review, display, retrieve storyboards, or create assets.
7. Collect Pilot-2 human turn input only when the user/player provides explicit input or approval.
8. Collect real author-led story arc audit samples when the user provides or approves them.
9. Draft a new explicit MVP-7 Candidate A.3 read-only audit brief only if 01/user explicitly asks.
10. Run a remote restore smoke check on demand.

Stop and ask before any work that would alter schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or any formal SQLite write path.

## Suggested New Window Prompt

```text
You are the main integration window for this repo.
First read AGENTS.md, ROADMAP.md, PLANS.md, VERIFY.md, DONE_CRITERIA.md, docs/HANDOFF.md, and docs/PROJECT_COMPLETION_AUDIT.md.
Current integration branch: codex/mvp6-checkpoint-branching-spec.
Remote: https://github.com/z72124223/ai-novel-engine.git.
mvp-6 tag must remain at be527031335efe8e2b0facae0145e1bbc084f8c6.
Do not enter Candidate B, MVP-8, schema changes, Validator/stateVersion/contextHash changes, resolve-turn/StateReducer changes, server/API/UI/Docker/Rust/embedding/Oiioii/Seedance integration work, automation/scheduler handoff, or direct SQLite writes without explicit user approval.
Current final deliverable is interactive script / shot text for 塵行, not voiced comic MP4 or in-repo video. Seedance 2.0 products are downstream only after script packets.
Paused: art-style candidates, opening preview images, comic-video renderer chain, asset registry implementation.
Active: polish script content in formal storyboards 26 and 28.
04/05/06/07/08 are paused. Resuming them requires manual handoff only: 01 explicit handoff -> 04 generation -> 05 review -> 06 display -> user explicit approval -> report to 01 -> 07 asset candidates -> 08 GPT storyboard pickup / verification -> user-reviewable storyboard delivery.
When 02 finishes a milestone, notify 03 using a verifier-window prompt: "You are 03 驗收稽核窗口..." with commit, push, changed files, verification, scope, safety boundaries, and review focus.
Start by running git status --short --branch, git log --oneline --decorate -10, git tag --list, and git remote -v, then choose the next smallest milestone from current evidence.
```
