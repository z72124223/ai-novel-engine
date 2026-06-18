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

## Project Layout

- `app/src/main/kotlin/com/localnovel/engine/model`：JSON protocol 與資料模型。
- `app/src/main/kotlin/com/localnovel/engine/repository`：SQLite repository。
- `app/src/main/kotlin/com/localnovel/engine/service`：context compiler、validator、reducer、stall detector、turn runner。
- `app/src/main/resources/migrations`：SQLite schema migrations。
- `samples`：demo seed、context packet、AI result 範例。
- `docs`：架構、資料模型、回合協議、驗證規則、roadmap。
