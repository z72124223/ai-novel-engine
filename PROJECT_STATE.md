# AI Novel Engine 專案狀態

> 這是目前工作入口。正式規範以使用者最新指示、`AGENTS.md`、已驗證程式碼／測試，以及本檔案指向的 active blueprint / Work Package 為準。

## Current Product Direction

目前正式產品路線為：

```text
Codex 終端版跑引擎、世界資料與劇本流程
→ 劇本／分鏡／圖片交接包 Commit、Push 到 GitHub
→ 使用者要求 GPT 讀指定 Commit 與交接包
→ GPT 使用已上傳的鎖定參考圖生成圖片候選
→ 使用者接受／退件
→ Codex 匯入核准圖、計算 SHA、驗證 Candidate Reference、Commit
→ 圖片鎖定後建立短鏡頭影片交接
```

最終產品方向是由正式世界與劇本驅動、角色和場景盡量不飄移、可逐鏡重做的圖片／影片作品。

劇本是正式上游，不再是唯一終點。圖片生成重新進入主線，但目前不在 Kotlin 引擎中直接接外部圖片／影片 API。

## Accepted Foundation

`WP-002A Codex/GPT Image Handoff Foundation` 已由總控審查通過，並以 fast-forward 納入預設分支。

- Accepted commit: `e02700ecf370e53f289f5a77364119639ac4bb5f`
- Result: `WP002A_PASS_WITH_SCHEMA_VALIDATOR_GAP`
- Added: six production JSON Schemas, six example-only instances, existing image-pipeline inventory, and `world/chenxing/production/README.md`.
- Verified: JSON parsing, internal refs/regex, Repository paths, source/reference hashes, candidate-only wording, and Git scope.
- Known limitation: no preinstalled JSON Schema meta-validator was available; no dependency was added.
- Boundary preserved: no Kotlin, Test, Migration, SQLite, Canon, image, video, provider API, or automated approval change.

## Current Work Package

- ID: `WP-003 Chenxing Visual Bible Inventory`
- Status: `WP-003_COMPLETED_AWAITING_CONTROLLER_REVIEW`
- Work Package: `docs/work-packages/WP-003_CHENXING_VISUAL_BIBLE_INVENTORY.md`
- Parent blueprint: `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- Execution plan: `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
- Accepted foundation: `e02700ecf370e53f289f5a77364119639ac4bb5f`
- Recommended Codex: strongest available Codex coding model, reasoning `HIGH`
- Base SHA: `dfd3b0ec24c032ba67a75fdd5dd36127188d060d`
- Working branch: `feature/wp003-chenxing-visual-bible-inventory`
- Primary result: `WP003_SCHEMA_GAP_FOUND`
- Current action: controller reviews the WP-003 inventory and decides how a pure Location Bible task is represented before WP-004.

## Active Work-Package Order

1. `WP-002A` — **COMPLETED / ACCEPTED**: file protocol, JSON Schemas, examples, existing pipeline inventory.
2. `WP-003` — **COMPLETED / AWAITING CONTROLLER REVIEW**: Chenxing visual Bible inventory found complete required character/costume/animal files, a blocking Daping Coast Location Bible gap, and a pure-location task schema gap.
3. `WP-004` — **NOT STARTED / BLOCKED PENDING CONTROLLER DECISION**: create the first real GPT handoff only after the location-only preliminary handoff or bounded schema path is resolved and the required Location Bible exists.
4. `WP-005` — GPT reads the specified Commit and generates image candidates.
5. `WP-006` — Codex imports approved images and runs the existing hash/review/reference chain.
6. `WP-002B` — conditional Kotlin compiler/validator only if the real Pilot proves it is necessary.
7. `WP-007` — first 3–8 second image-to-video handoff Pilot.

Do not start `WP-004` or `WP-002B` automatically. WP-003 must first determine actual visual-reference readiness and any bounded schema/location gap.

## WP-003 Verified Result

- Author-locked present files: Lu Chenxing production turnaround, opening modern costume turnaround, Aijiang production turnaround, face reference/photo set, and phase-scoped style references.
- Missing referenced character files: lean-base, opening-stocky, cinematic, and two superseded 2D turnaround paths; these are not required bindings for Shot 01–03 because the current production references exist.
- Daping Coast and ruin geometry: textual authority only; no master panorama, top-down plan, directional views, spatial-anchor map, detail/scale views, or approved location images exist.
- Shot 01–03 disposition: `SCHEMA_ADJUSTMENT_REQUIRED`; storyboard text alone is not enough.
- Schema finding: `shot_queue_v1` requires at least one character per shot, so it cannot safely represent a pure `LOCATION_BIBLE_CANDIDATE` pre-shot task without a fake binding.
- Future ChatGPT upload list: ten existing required files; every current conversation status remains `UPLOAD_REQUIRED`.
- Rejected drift: the ten old text-only preview frames remain rejected evidence and were not copied or promoted.
- No Kotlin, Test, Migration, SQLite, Canon, image, video, provider API, approval, or Runtime change was made.

## Confirmed Repository Facts

- The project remains a local single-process Kotlin/JVM CLI engine using SQLite and JSON file protocols.
- Runtime state changes remain behind existing Validator, StateReducer, `resolve-turn`, `contextHash`, `stateVersion`, and transaction boundaries.
- MVP-8 reader-first provisional content, knowledge boundaries, Canon promotion boundaries, and additive migration rules remain active.
- Formal Chenxing character/costume/style references already exist under `world/chenxing/formal/` and `world/chenxing/assets/`.
- Opening storyboard Shot 01–09 and ruin interior Shot 10–18 exist as author-locked source data.
- The existing Image Candidate / Provider Handoff / External Result / Human Review / Candidate Reference chain must be reused rather than bypassed.
- WP-002A schemas require candidate-only output, explicit user review, reference upload readiness, Git/source identity, and return through the existing CLI chain.
- `world/chenxing/preview/opening_v1/DRIFT_NOTICE.json` rejects the old text-only preview frames as formal references.
- User reported that the local project and GitHub are aligned before this production route. Codex must still start each package from the latest remote default branch and verify its own base SHA.

## Active Boundaries

- No direct SQLite writes or bypass of formal engine validation.
- No external image/video API integration inside Kotlin for WP-003.
- No Web Server, open port, Android UI, Docker, React/Next, Rust, FFmpeg/TTS execution, or automatic renderer work.
- Generated images are always `CANDIDATE` until explicit user decision and Codex hash/reference validation.
- GPT technical success, Codex test PASS, Git file existence, silence, or elapsed time never counts as user approval.
- Author-only secrets, unrevealed causal answers, and character-inaccessible knowledge must not enter image prompts.
- Locked turnaround, costume, and style assets must not be moved, edited, renamed, or silently replaced.
- WP-003 may inventory image files and calculate integrity metadata; it may not create or visually approve images.
- Large video files, bulk failed renders, and repeated rejected images are not committed to ordinary Git by default.

## GPT Reference Boundary

A reference path in GitHub does not guarantee that the image generation tool has that image attached in the current ChatGPT conversation.

When GPT begins image work:

- It reads the specified Git Commit and handoff path.
- It checks the handoff `referenceUploadChecklist`.
- Missing required likeness, costume, animal, or location references produce `REFERENCE_UPLOAD_REQUIRED`.
- GPT must not replace missing formal references with text-only guesses for production frames.

WP-003 prepares the exact future upload-source list, but it must not claim any image is already attached to ChatGPT.

## Source Of Truth Order

1. User's latest explicit instruction.
2. This `PROJECT_STATE.md` current product direction and active Work Package pointer.
3. Repository-scope `AGENTS.md` and `.codex/project_controller.md`.
4. Active blueprint, execution plan, and WP-003 document named above.
5. Verified code, tests, CLI output, formal world files, and locked visual references.
6. Applicable route/spec documents.
7. Historical plans and handoffs as evidence only.

Historical statements that images/video are permanently paused or that script is the only terminal product are superseded by the current active blueprint. Their safety and approval boundaries remain valid unless explicitly replaced.

## Next User Action

The controller reviews:

```text
docs/production/WP003_CHENXING_VISUAL_BIBLE_INVENTORY_REPORT.md
world/chenxing/production/visual_bible/chenxing_location_bible_gap_inventory_v1.json
world/chenxing/production/visual_bible/opening_shots_01_03_readiness_v1.json
```

Controller decision required: use a bounded schema amendment or a separate preliminary Location Bible handoff. Do not start WP-004 until that decision and the blocking Daping Coast visual-reference gap are resolved.

After Codex later pushes a real WP-004 image handoff, the user tells GPT:

```text
讀取 z72124223/ai-novel-engine，Commit <sha>，交接包 <path>，依 CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1 執行。
```

## Update Rule

Every Work Package completion must update:

- current package status;
- verified facts and failures;
- exact Commit/branch evidence;
- next package pointer;
- whether user-only approval or reference upload is required.

Do not record unverified assumptions as completed capabilities.
