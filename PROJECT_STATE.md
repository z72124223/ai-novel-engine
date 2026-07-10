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

`WP-002A Codex/GPT Image Handoff Foundation` 已由總控審查通過，並納入預設分支。

- Accepted commit: `e02700ecf370e53f289f5a77364119639ac4bb5f`
- Result: `WP002A_PASS_WITH_SCHEMA_VALIDATOR_GAP`
- Added: six production JSON Schemas, six example-only instances, existing image-pipeline inventory, and `world/chenxing/production/README.md`.
- Verified: JSON parsing, internal refs/regex, Repository paths, source/reference hashes, candidate-only wording, and Git scope.
- Known limitation: no preinstalled JSON Schema meta-validator was available; no dependency was added.
- Boundary preserved: no Kotlin, Test, Migration, SQLite, Canon, image, video, provider API, or automated approval change.

## Current Work Package

- ID: `WP-003 Chenxing Visual Bible Inventory`
- Status: `WP-003_REVALIDATION_IN_PROGRESS`
- Work Package: `docs/work-packages/WP-003_CHENXING_VISUAL_BIBLE_INVENTORY.md`
- Accepted foundation: `e02700ecf370e53f289f5a77364119639ac4bb5f`
- Recommended Codex: strongest available Codex coding model, reasoning `HIGH`
- Base SHA: `02c32925ed5c2dc5101f429fda2e6fa9184d732a`
- Working branch: `feature/wp003-chenxing-visual-bible-inventory`
- Existing remote result: `WP003_SCHEMA_GAP_FOUND` at `eef2e05be88155da47b8b552cd82c68c1f5f145c`.
- Current action: Codex revalidates the existing inventory against the latest route gate, Repository files, hashes, dimensions, authority evidence, and Shot 01–03 readiness before final Push.

## Active Work-Package Order

1. `WP-002A` — **COMPLETED / ACCEPTED**: file protocol, JSON Schemas, examples, existing pipeline inventory.
2. `WP-003` — **ACTIVE / REVALIDATING**: Chenxing visual Bible and Shot 01–03 readiness inventory.
3. `WP-004` — **NOT STARTED / BLOCKED PENDING CONTROLLER DECISION**: create the first real GPT handoff only after the location-only preliminary handoff or bounded schema path is resolved and the required Location Bible exists.
4. `WP-005` — GPT reads the specified Commit and generates image candidates.
5. `WP-006` — Codex imports approved images and runs the existing hash/review/reference chain.
6. `WP-002B` — conditional Kotlin compiler/validator only if the real Pilot proves it is necessary.
7. `WP-007A` — future provider-neutral VideoShotHandoff contract.
8. `WP-007B` — future one-shot manual Grok video Pilot after explicit user authorization.
9. `WP-007C` — future video intake, Hash, review, and continuity evidence.

Do not start WP-002B or WP-007 before the real image Pilot and user-approved reference chain.

## WP-003 Existing Result Under Revalidation

- Author-locked present files: Lu Chenxing production turnaround, opening modern costume turnaround, Aijiang production turnaround, face reference/photo set, and phase-scoped style references.
- Missing referenced character files: lean-base, opening-stocky, cinematic, and two superseded 2D turnaround paths; these are not required bindings for Shot 01–03 because the current production references exist.
- Daping Coast and ruin geometry: textual authority only; no master panorama, top-down plan, directional views, spatial-anchor map, detail/scale views, or approved location images exist.
- Shot 01–03 disposition: `SCHEMA_ADJUSTMENT_REQUIRED`; storyboard text alone is not enough.
- Schema finding: `shot_queue_v1` requires at least one character per shot, so it cannot safely represent a pure `LOCATION_BIBLE_CANDIDATE` pre-shot task without a fake binding.
- Future ChatGPT upload list: ten existing required files; every current conversation status remains `UPLOAD_REQUIRED`.
- Rejected drift: the ten old text-only preview frames remain rejected evidence and were not copied or promoted.
- No Kotlin, Test, Migration, SQLite, Canon, image, video, provider API, approval, or Runtime change was made.

## Confirmed Repository Facts

- Project remains a local single-process Kotlin/JVM CLI engine using SQLite and JSON file protocols.
- Runtime state changes remain behind Validator, StateReducer, `resolve-turn`, `contextHash`, `stateVersion`, and transaction boundaries.
- MVP-8 reader-first provisional content, knowledge boundaries, Canon promotion boundaries, and additive migration rules remain active.
- Formal Chenxing character/costume/style references exist under `world/chenxing/formal/` and `world/chenxing/assets/`.
- Opening Storyboard Shot 01–09 and ruin interior Shot 10–18 exist as author-locked source data.
- The existing Image Candidate / Provider Handoff / External Result / Human Review / Candidate Reference chain must be reused.
- WP-002A requires candidate-only output, explicit user review, reference-upload readiness, Git/source identity, and return through the existing CLI chain.
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

No broad deletion is active now. A future cleanup package must first inventory inbound links, code/test/sample dependencies, authoritative sources, duplicate hashes, and required historical evidence.

Do not delete formal Canon, world sources, migrations, tests/fixtures still used, accepted or locked assets, provenance/Hash/review evidence, rejected-drift evidence that supports a decision, or files named by an active package.

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

When GPT begins image work:

- read the exact Handoff Commit and path;
- inspect `referenceUploadChecklist`;
- missing required likeness, costume, animal, or location geometry produces `REFERENCE_UPLOAD_REQUIRED`;
- do not replace missing locked references with text-only guesses for formal frames.

WP-003 prepares the exact future upload list; it must not claim anything is already attached.

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

If the computer sleeps, the terminal closes, authentication expires, or a true stop condition occurs, no completion should be assumed.

## Source Of Truth Order

1. latest explicit user instruction;
2. this `PROJECT_STATE.md`;
3. `.codex/project_controller.md`;
4. `docs/CURRENT_ROUTE_GATE_V1.md`;
5. active Work Package and active blueprint;
6. verified code, tests, CLI output, formal world files, and locked visual references;
7. historical plans, handoffs, charters, and route cards.

## Next User Action

Codex completes the current WP-003 revalidation, pushes the bounded inventory branch, reports to the controller, and stops:

```text
WP-003 validation
→ Commit and Push
→ GPT controller review
→ stop; do not start WP-004
```

## Update Rule

Every package completion must update:

- current package status;
- verified facts and failures;
- exact Commit/branch evidence;
- next package pointer;
- whether user-only approval, provider cost decision, or reference upload is required.

Do not record an assumption as a completed capability.
