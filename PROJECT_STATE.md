# AI Novel Engine 專案狀態

> **唯一現況入口。** 新的 GPT、Codex CLI、Reviewer 或外部協作者必須先讀本檔，再讀 `.codex/project_controller.md`、`docs/CURRENT_ROUTE_GATE_V1.md` 與本檔指定的 Work Package。舊 `HANDOFF`、舊 charter、舊 route card 或舊 `PLANS.md` 標示的「目前／Current／Active」不得跳過此入口。

## Current Product Direction

目前正式產品路線為：

```text
Codex 終端版跑引擎、世界資料與劇本流程
→ 劇本／分鏡／圖片交接包 Commit、Push 到 GitHub
→ 使用者要求 GPT 讀指定 Commit 與交接包
→ GPT 使用目前對話中已附加的鎖定參考圖生成圖片候選
→ 使用者接受／退件／要求變體
→ Codex 匯入保留圖片、計算 SHA-256、執行既有審查與 Candidate Reference 鏈
→ 圖片鎖定後建立供應商中立的短鏡頭影片交接
→ 未來可用 Grok 作為影片生成候選提供者
```

最終產品方向是由正式世界與劇本驅動、角色和場景盡量不飄移、可逐鏡重做的圖片／影片作品。

劇本是正式上游，不再是唯一終點。圖片是目前主線輸出；影片是圖片 Pilot 成功後的下游，不是現在立即啟動的 API 或自動產線。

## Mandatory Current Route Gate

- Current gate: `docs/CURRENT_ROUTE_GATE_V1.md`
- Active image blueprint: `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- Execution plan: `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
- Future Grok video addendum: `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1_GROK_VIDEO_ADDENDUM.md`
- Legacy handoff archive pointer: `docs/archive/HANDOFF_LEGACY_INDEX_V1.md`

`docs/HANDOFF.md` 現在只是精簡 Gateway。舊的 500+ 行 handoff 內容已由 Git 歷史保存，不再作第一讀入口。

## Accepted Foundation

### WP-002A

`WP-002A Codex/GPT Image Handoff Foundation` 已由總控審查通過，並納入預設分支。

- Accepted commit: `e02700ecf370e53f289f5a77364119639ac4bb5f`
- Result: `WP002A_PASS_WITH_SCHEMA_VALIDATOR_GAP`
- Added: six production JSON Schemas, six example-only instances, existing image-pipeline inventory, and `world/chenxing/production/README.md`.
- Verified: JSON parsing, internal refs/regex, Repository paths, source/reference hashes, candidate-only wording, and Git scope.
- Known limitation: no preinstalled JSON Schema meta-validator was available; no dependency was added.
- Boundary preserved: no Kotlin, Test, Migration, SQLite, Canon, image, video, provider API, or automated approval change.

### WP-003

`WP-003 Chenxing Visual Bible Inventory` 已由總控審查通過，並納入預設分支。

- Accepted commit: `7cd3f82f49d05703c95c1b58e313a03d6967d6f4`
- Result: `WP003_SCHEMA_GAP_FOUND`
- Verified: 30 unique image files, 31 asset bindings, hashes, byte sizes, dimensions, authority classifications, rejected drift, location gaps, and Shot 01–03 readiness.
- Confirmed: required Lu Chenxing, opening costume, Aijiang, and opening style references exist.
- Blocking gap: Daping Coast had no master panorama, top-down plan, directional views, spatial anchors, scale reference, or accepted location geometry.
- Schema gap: strict story `shot_queue_v1` cannot honestly represent a pure Location Bible task without a fake character.

### WP-003A

`WP-003A Daping Coast Location Bible Foundation Handoff` 已由總控審查通過，並以 fast-forward 納入預設分支。

- Accepted commit: `4dea7f071b58f300c13c16e76fe2f89651b9cc40`
- Result: `WP003A_READY_FOR_GPT_LOCATION_TASK_01`
- Added: provider-neutral Location Bible handoff/return Schemas, example-only instances, and the real Daping Coast preliminary handoff.
- Handoff: `world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_handoff.json`
- Handoff content SHA-256: `a3b6d6f421fb51983f2652c48ff9bf9a9138e1ee1d497dcc5bce8442255798da`
- Exactly one task is initially open: `daping-lb-01-master-panorama`.
- Tasks 02–07 remain dependency-blocked; the DAG is acyclic.
- Boundary preserved: no Kotlin, Test, Migration, Canon, existing Shot Schema, image, provider job, approval, SQLite, or Runtime change.

## Current Work Package

- ID: `WP-003B Daping Coast Master Panorama GPT Session`
- Status: `WP-003B_PAUSED_AFTER_TASK_01`
- Work Package: `docs/work-packages/WP-003B_DAPING_MASTER_PANORAMA_GPT_SESSION.md`
- Accepted handoff commit: `4dea7f071b58f300c13c16e76fe2f89651b9cc40`
- Handoff path: `world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_handoff.json`
- Completed bounded task: `daping-lb-01-master-panorama`
- Candidate path: `world/chenxing/assets/image_candidates/chenxing-daping-location-bible-v1/daping_lb_01_master_panorama_candidate_v1.png`
- Candidate SHA-256: `5059253c18af6a5efabba53a011be62b466cffc79d977bda83cfdbbf03f24cc2`
- Candidate dimensions: `1672 x 941`
- User decision: `APPROVE_CANDIDATE`
- Evidence directory: `world/chenxing/production/location_bible_handoffs/daping_coast_v1/task_01_archive/`
- Current action: none. The image lane is paused and archived after Task 01.

The candidate remains candidate-only. It is not Canon, Production Lock, a final scene, or an accepted Location Reference. The existing Candidate Reference chain is deferred and must not be inferred as complete.

```text
WP-003B_PAUSED_AFTER_TASK_01
```

Tasks `daping-lb-02` through `daping-lb-07` remain:

```text
BLOCKED_BY_PRIOR_TASK_APPROVAL
```

## Controller Architecture Decision

The selected route remains:

```text
separate preliminary Location Bible handoff
```

Do not weaken `shot_queue_v1` and do not insert a fake character.

The Location Bible is an upstream foundation artifact, not a story Shot. It may correctly contain zero visible characters. A separate preliminary handoff breaks the circular dependency:

```text
story Shots require locked location geometry
but location-foundation generation cannot require that same geometry as input
```

WP-004 will continue to use the strict story-shot schemas after location references are approved.

## Active Work-Package Order

1. `WP-002A` — **COMPLETED / ACCEPTED**: generic GPT image handoff foundation.
2. `WP-003` — **COMPLETED / ACCEPTED**: Chenxing visual asset and location-gap inventory.
3. `WP-003A` — **COMPLETED / ACCEPTED**: separate Location Bible schemas and the real Daping Coast preliminary handoff.
4. `WP-003B` — **PAUSED / TASK 01 CANDIDATE ARCHIVED**: the exact panorama candidate and Return/Review/Intake evidence are preserved; no promotion is claimed.
5. `WP-003C` — **NOT STARTED / DEFERRED**: the existing Candidate Reference acceptance chain is not executed while the image lane is paused.
6. `WP-004` — **BLOCKED**: create the real opening Shot 01–03 handoff only after required Daping Coast Location Bible components are accepted.
7. `WP-005` — GPT generates Shot 01–03 image candidates.
8. `WP-006` — Codex imports approved Shot images and runs the existing hash/review/reference chain.
9. `WP-002B` — conditional Kotlin compiler/validator only if real manual Pilots prove it necessary.
10. `WP-007A` — future provider-neutral VideoShotHandoff contract.
11. `WP-007B` — future one-shot manual Grok video Pilot after explicit user authorization.
12. `WP-007C` — future video intake, Hash, review, and continuity evidence.

Do not start WP-003C, task 02, WP-004, WP-002B, or WP-007 automatically. Each needs a controller pointer after the preceding candidate and intake evidence are reviewed.

## WP-003 Verified Result

- Author-locked present files: Lu Chenxing production turnaround, opening modern costume turnaround, Aijiang production turnaround, face reference/photo set, and phase-scoped style references.
- Missing referenced character files: lean-base, opening-stocky, cinematic, and two superseded 2D turnaround paths; these are not required bindings for Shot 01–03 because the current production references exist.
- Daping Coast and ruin geometry initially had textual authority only; no accepted location geometry existed before WP-003B.
- Future ChatGPT Shot upload list: ten existing required files; these remain separate from the current panorama task.
- Rejected drift: the ten old text-only preview frames remain rejected evidence and were not copied or promoted.

## WP-003A Verified Result

- Added provider-neutral `location_bible_handoff_v1` and `location_bible_return_to_codex_v1` contracts without modifying `shot_queue_v1`.
- Added example-only instances and the real Daping Coast handoff under `world/chenxing/production/location_bible_handoffs/daping_coast_v1/`.
- Exactly one task is `READY_FOR_GPT`: `daping-lb-01-master-panorama`.
- Tasks 02–07 remain `BLOCKED_BY_PRIOR_TASK_APPROVAL`; the dependency graph is acyclic.
- The initial ChatGPT upload requirement is only `world/chenxing/assets/style_reference/chenxing_opening_style_cinematic_misty_v1.png`.
- The handoff uses public textual anchors only; author-only causal explanations, crossing mechanics, later reveals, post-crossing content, and private source paths are excluded.
- All new JSON parses; local refs and regexes pass; Repository paths, SHA-256, byte sizes, normalized handoff Hash, dependency graph, candidate boundary, and Git scope pass.
- Full JSON Schema meta-validation remains `SCHEMA_META_VALIDATION_NOT_AVAILABLE`; no dependency was installed.

## WP-003B Generation Boundary

The active task creates one `16:9` ordinary-perspective master-panorama candidate with no visible characters.

It must establish:

- sea-facing direction;
- intertidal rock platform;
- tide pools;
- localized unnatural-seam area;
- spatially legible safe/return route;
- enough stable geometry for a later top-down plan and directional views.

It must not include:

- characters;
- fantasy architecture;
- ancient or post-crossing imagery;
- machinery or a glowing portal;
- supernatural effects;
- readable pseudo-text;
- hidden causal explanations or later reveals;
- approval, Canon, final-media, release, or production-lock marks.

The generated file is always `CANDIDATE`. User choices are limited to:

```text
APPROVE_CANDIDATE
REJECT_DRIFT
REQUEST_VARIATION
CHANGE_VISUAL_DIRECTION
```

Even `APPROVE_CANDIDATE` does not unlock task 02 by itself. The candidate must return to Codex for file transfer, SHA-256, controlled intake, human-review evidence, and the existing Candidate Reference chain.

## Confirmed Repository Facts

- Project remains a local single-process Kotlin/JVM CLI engine using SQLite and JSON file protocols.
- Runtime state changes remain behind Validator, StateReducer, `resolve-turn`, `contextHash`, `stateVersion`, and transaction boundaries.
- MVP-8 reader-first provisional content, knowledge boundaries, Canon promotion boundaries, and additive migration rules remain active.
- Formal Chenxing character/costume/style references exist under `world/chenxing/formal/` and `world/chenxing/assets/`.
- Opening Storyboard Shot 01–09 and ruin interior Shot 10–18 exist as author-locked source data.
- The existing Image Candidate / Provider Handoff / External Result / Human Review / Candidate Reference chain must be reused.
- `world/chenxing/preview/opening_v1/DRIFT_NOTICE.json` rejects the old text-only preview frames as formal references.
- A Location Bible requires more than one panorama: master panorama, top-down/zone plan, directional views, spatial anchors, scale reference, and required details. A 360 panorama is optional supplement only.

## Historical Status Reclassification

Historical content remains evidence, but these route claims are superseded:

- script is the only terminal product;
- all image/video work is currently paused;
- old `docs/HANDOFF.md` is the first current entry;
- old route cards select the active package without this file.

Still-valid historical boundaries include:

- no automatic approval;
- reference-first production;
- candidate is not approved/reference/Canon/final media;
- no silence-as-approval;
- no direct SQLite or Canon bypass;
- no provider spending without explicit user decision.

Do not modify historical Canon or charter text merely to erase the old decision. Use `docs/CURRENT_ROUTE_GATE_V1.md` to classify it.

## Archive And Deletion Boundary

Current policy:

```text
MARK STALE
→ ARCHIVE OR INDEX
→ DELETE ONLY AFTER DEPENDENCY EVIDENCE
```

No broad deletion is active now. Do not delete formal Canon, world sources, migrations, tests/fixtures still used, accepted or locked assets, provenance/Hash/review evidence, rejected-drift evidence that supports a decision, or files named by an active package.

## Grok Future Video Boundary

Grok is a future video provider candidate, not the current director or source of truth.

Planned default:

```text
approved clean single-shot keyframe
+ motion/camera/continuity contract
→ provider-neutral VideoShotHandoff
→ manual Grok image-to-video Pilot
→ user review
→ Codex file identity / Hash / continuity intake
```

Reference-to-video, edit, extension, and text-to-video remain bounded provider modes. Provider capabilities and limits must be re-checked from official xAI documentation when the route activates.

Do not send a final comic page with speech bubbles, subtitles, panel borders, or multiple panels as the formal video source frame. Use a clean keyframe and separate dialogue/subtitle/audio data.

No xAI API integration, API key storage, provider spending, background video generation, or whole-episode generation is authorized now.

## GPT Reference Boundary

A GitHub image path does not mean the image-generation tool has that image attached in the active ChatGPT conversation.

For WP-003B:

- read the exact accepted handoff Commit and path;
- inspect the task-aware upload checklist;
- missing the required style reference produces `REFERENCE_UPLOAD_REQUIRED`;
- do not substitute a text-only style guess;
- the Location Bible foundation candidate remains candidate-only until user review and Codex intake.

## Automatic Execution Reality

The Repository can instruct an already-running Codex CLI. It cannot launch Codex by itself.

Allowed unattended unit:

```text
one explicitly started Codex session
→ one active Work Package
→ validation
→ Commit and Push
→ report
→ stop
```

Codex must not move to the next product package without a new pointer or explicit controller instruction. The local task queue is not a daemon.

## Source Of Truth Order

1. latest explicit user instruction;
2. this `PROJECT_STATE.md`;
3. `.codex/project_controller.md`;
4. `docs/CURRENT_ROUTE_GATE_V1.md`;
5. active Work Package and active blueprint;
6. verified code, tests, CLI output, formal world files, and locked visual references;
7. historical plans, handoffs, charters, and route cards.

## Next User Action

No image-production action is pending. Keep the lane paused. The former upload path is retained below only as completed historical evidence:

```text
world/chenxing/assets/style_reference/chenxing_opening_style_cinematic_misty_v1.png
```

Do not execute the former generation command below. Only a new explicit user/controller instruction may resume Task 02, start WP-003C, promote a Candidate Reference, or begin WP-004:

```text
參考圖已上傳，執行 daping-lb-01-master-panorama。
```

## Update Rule

Every package completion must update:

- current package status;
- verified facts and failures;
- exact Commit/branch evidence;
- next package pointer;
- whether user-only approval, provider cost decision, or reference upload is required.

Do not record an assumption as a completed capability.
