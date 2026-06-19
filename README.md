# Local Interactive Fiction Engine

Kotlin/JVM CLI + SQLite + JSON 檔案協議的本地單進程互動小說引擎。

目標流程：

```text
玩家輸入
→ compile context_packet.json
→ 人工或 Codex 產生 ai_turn_result.json
→ validate result
→ apply state_delta
→ 寫回 SQLite
→ 輸出 narrative
```

## Constraints

- 不建立 web server。
- 不開任何 port。
- 不使用 FastAPI、Express、Ktor server、Spring server、React、Next.js、Docker。
- 不接 OpenAI API。
- 不做 Android UI。
- 不加入 Rust。

## Requirements

- JDK 17+
- 使用 repo 內的 Gradle wrapper 執行

## Build And Test

```powershell
./gradlew.bat clean test --no-daemon
```

## Demo Flow

```powershell
./gradlew :app:run --args="init-db --db build/demo/story.db"
./gradlew :app:run --args="seed-demo --db build/demo/story.db --seed samples/demo_project_seed.json"
./gradlew :app:run --args="compile-context --db build/demo/story.db --project demo --player-input `"我推開門。`" --out build/demo/context_packet.json"
./gradlew :app:run --args="validate-result --db build/demo/story.db --context build/demo/context_packet.json --result samples/sample_ai_turn_result.json"
./gradlew :app:run --args="apply-result --db build/demo/story.db --context build/demo/context_packet.json --result samples/sample_ai_turn_result.json"
./gradlew :app:run --args="show-state --db build/demo/story.db --project demo"
```

`run-manual-turn` 會先產生 context。若 result 檔不存在，它會停下來讓你人工填寫：

```powershell
./gradlew :app:run --args="run-manual-turn --db build/demo/story.db --project demo --player-input `"我試著轉動黃銅鑰匙。`" --context-out build/demo/context_packet.json --result build/demo/ai_turn_result.json"
```

## Commands

- `init-db --db <path>`
- `seed-demo --db <path> --seed <path>`
- `compile-context --db <path> --project <id> --player-input <text> --out <path>`
- `validate-result --db <path> --context <path> --result <path>`
- `apply-result --db <path> --context <path> --result <path>`
- `run-manual-turn --db <path> --project <id> --player-input <text> --context-out <path> --result <path>`
- `show-state --db <path> --project <id>`
- `start-session --db <path> --project <id> --scene <id>`
- `prepare-turn --db <path> --session <id> --input <text> --out <path>`
- `resolve-turn --db <path> --session <id> --result <path>`
- `show-session --db <path> --session <id>`
- `abort-pending-turn --db <path> --session <id> [--context <id>]`
- `show-character-mind --db <path> --session <id> --character <id>`
- `preview-recall --db <path> --session <id> --character <id> --input <text>`
- `explain-context --file <path>`
- `show-scene --db <path> --session <id> [--input <text>]`
- `show-threads --db <path> --session <id>`
- `preview-transitions --db <path> --session <id> --input <text>`
- `show-world-events --db <path> --session <id>`
- `explain-scene-state --db <path> --session <id>`
- `create-setting-pack-template --project <id> --out <path> [--pack <id>] [--version <n>] [--mode ADD_ON]`
- `validate-setting-pack --db <path> --file <path> [--out <path>]`
- `preview-setting-import --db <path> --file <path> [--out <path>]`
- `apply-setting-pack --db <path> --file <path> [--out <path>]`
- `export-setting-pack --db <path> --project <id> --pack <id> --version <n> --out <path>`
- `diff-setting-pack --db <path> --file <path> [--out <path>]`
- `impact-report --db <path> --file <path> [--out <path>]`
- `show-dependencies --db <path> --project <id> [--entity-type <type> --entity-id <id>] [--depth <n>]`
- `validate-project-canon --db <path> --project <id> [--out <path>]`
- `audit-context --file <path> [--out <path>]`
- `audit-narrative-quality --context <path> --result <path> [--evidence <path>] [--out <path>]`
- `export-session-evidence --db <path> --session <id> --out <path>`
- `pilot-preflight --db <path> --project <id> --session <id> [--out <path>]`
- `explain-context-budget --db <path> --session <id> (--input <text> | --input-file <path>) [--out <path>]`
- `explain-capability --db <path> --session <id> --entity-type <technique|item|weapon> --entity-id <id> [--actor <character-id>] [--out <path>]`

## Multi-Turn Flow

MVP-2 adds reliable session-based turns:

```powershell
./gradlew.bat run --args="start-session --db data/story.db --project demo --scene scene-shrine-questioning"
./gradlew.bat run --args="prepare-turn --db data/story.db --session SESSION_ID --input `"玩家輸入`" --out out/turn_001_context.json"
./gradlew.bat run --args="resolve-turn --db data/story.db --session SESSION_ID --result out/turn_001_result.json"
./gradlew.bat run --args="show-session --db data/story.db --session SESSION_ID"
```

`prepare-turn` writes a pending context row but does not change formal story state. `resolve-turn` validates `contextId`、`sessionId`、`turnNumber`、`baseStateVersion`、`contextHash` and `resultId` before applying all deltas in one SQLite transaction.

## Memory And Knowledge Runtime

MVP-3 adds deterministic character memory retrieval, character-facing knowledge claims, context budgeting and emotion-causal validation.

```powershell
./gradlew.bat run --args="show-character-mind --db data/story.db --session SESSION_ID --character char-shen-shuang"
./gradlew.bat run --args="preview-recall --db data/story.db --session SESSION_ID --character char-shen-shuang --input `"我展示沈家銅牌。`""
./gradlew.bat run --args="explain-context --file out/turn_001_context.json"
```

`prepare-turn` may include `retrievedMemories` and `knowledgeClaims`, but it remains read-only: recall counts and recall logs are written only after a successful `resolve-turn`. AI output may propose `memoryCandidates` and `knowledgeUpdates`; the engine decides what to accept inside the same transaction as the turn resolution.

## Scene Thread Runtime

MVP-4 adds scene instances, requirements, story threads, location-aware transitions, scheduled world events, a world clock and continuity constraints.

```powershell
./gradlew.bat run --args="start-session --db data/story.db --project demo --scene scene-rear-hall"
./gradlew.bat run --args="show-scene --db data/story.db --session SESSION_ID"
./gradlew.bat run --args="preview-transitions --db data/story.db --session SESSION_ID --input `"我離開後殿。`""
./gradlew.bat run --args="show-world-events --db data/story.db --session SESSION_ID"
```

`prepare-turn` now includes `sceneLifecycle`, `sceneRequirements`, `activeStoryThreads`, `availableTransitions`, `pendingWorldEvents`, `worldClock` and `continuityConstraints`. `resolve-turn` accepts the matching result fields only after Validator approval and applies scene/thread/time/world-event writes in the same transaction as the turn.

See `docs/SCENE_THREAD_RUNTIME.md`.

## Setting Pack Runtime

MVP-5 adds local setting pack import, dependency graph traversal, impact preview, provenance revisions and setting-context budgeting.

```powershell
./gradlew.bat run --args="create-setting-pack-template --project demo --out samples/setting_pack_template.json"
./gradlew.bat run --args="validate-setting-pack --db data/story.db --file samples/sample_setting_pack.json"
./gradlew.bat run --args="preview-setting-import --db data/story.db --file samples/sample_setting_pack.json --out out/setting_preview.json"
./gradlew.bat run --args="apply-setting-pack --db data/story.db --file samples/sample_setting_pack.json"
./gradlew.bat run --args="export-setting-pack --db data/story.db --project demo --pack sample-shrine-pack --version 1 --out out/exported_setting_pack.json"
```

The import path is always parse -> validate -> diff -> impact preview -> transaction apply. `PATCH_CANON` updates are blocked while active sessions exist. `prepare-turn` includes only relevant setting excerpts through `relevantSettings`, `settingContextBudget` and `canonRevisionSummary`; it never includes a full setting pack or full dependency graph.

See `docs/SETTING_PACKS_AND_DEPENDENCIES.md`.

## Pilot Stabilization

Pilot-1 stabilization keeps the hard context ceiling at `24576` UTF-8 bytes. `prepare-turn` writes only a finalized context whose `contextBudget.actualBytes` matches the real JSON bytes and whose `contextHash` matches that finalized payload. If mandatory context cannot fit, it fails before creating a pending turn.

Before writing an AI result for a manual turn:

```powershell
./gradlew.bat run --args="audit-context --file out/turn_001_context.json --out out/turn_001_context_audit.json"
```

Before starting a fresh Pilot rerun:

```powershell
./gradlew.bat run --args="pilot-preflight --db data/pilot1-rerun.db --project PROJECT_ID --session SESSION_ID --out out/pilot1/preflight.json"
```

`pilot-preflight` and `export-session-evidence` are read-only. Setting-pack opening characters must provide `initialRuntimeTemplate`; `start-session` uses it to create missing runtime `character_states` in the session transaction.

When a Pilot turn is near the hard budget, run the read-only forensics command before `prepare-turn`:

```powershell
./gradlew.bat run --args="explain-context-budget --db data/pilot1-rerun.db --session SESSION_ID --input-file out/turn_input.txt --out out/context_budget_explain.json"
```

Budget V2 keeps the same `24576` byte limit. It adds deterministic duplicate compaction, compact mandatory views, dynamic allocation across settings/memories/knowledge/summaries, and section-level failure reports. See `docs/CONTEXT_BUDGET_FORENSICS.md`.

Pilot-1 context metadata normalization keeps diagnostics out of the AI-facing `ContextPacket`. Final turn context now uses a top-level allowlist, omits empty/default optional fields, removes legacy aliases such as `characterMemories` when `retrievedMemories` is present, and stores detailed budget forensics only in `explain-context-budget` / `audit-context` report files. See `docs/CONTEXT_PACKET_NORMALIZATION.md`.

Pilot-1 context relevance floor prevents a byte-valid but semantically empty turn context. `prepare-turn`, `audit-context`, and `explain-context-budget` now evaluate deterministic semantic requirements for explicit recall cues, relevant beliefs, transition/location questions, active character goals, hard world rules, and referenced item/technique constraints. If required memory, knowledge, or legality records cannot fit under `24576` bytes, the command fails with `CONTEXT_QUALITY_UNSATISFIABLE` before a pending turn is created. See `docs/CONTEXT_RELEVANCE_FLOOR.md`.

Referenced capability contracts prevent AI results from using a technique, item, or weapon from name/description alone. Explicit capability use must keep a Tier 1 `activeCapabilityContracts` card with authorization, effects, prerequisites, requirements, costs, limitations, forbidden uses, and current eligibility. `resolve-turn` requires matching `capabilityUseChecks` and a cost state delta when a successful use has costs. Use `explain-capability` for read-only provenance. See `docs/REFERENCED_CAPABILITY_CONTRACTS.md`.

## Narrative Quality Audit

MVP-7 Candidate A adds a read-only narrative quality audit for a context/result pair. It does not open SQLite, does not rewrite the story, and does not affect `resolve-turn`.

```powershell
./gradlew.bat :app:run --args="audit-narrative-quality --context samples/sample_context_packet.json --result samples/sample_ai_turn_result.json --out out/mvp7_narrative_quality_report.json"
```

The report includes `verdict`, `score`, `severity`, stable `findings`, evidence, suggested fixes, affected file or turn, and rule ids.

## Project Layout

- `app/src/main/kotlin/com/localnovel/engine/model`：JSON protocol 與資料模型。
- `app/src/main/kotlin/com/localnovel/engine/repository`：SQLite repository。
- `app/src/main/kotlin/com/localnovel/engine/service`：context compiler、validator、reducer、stall detector、turn runner。
- `app/src/main/resources/migrations`：SQLite schema migrations。
- `samples`：demo seed、context packet、AI result 範例。
- `docs`：架構、資料模型、回合協議、驗證規則、roadmap。
